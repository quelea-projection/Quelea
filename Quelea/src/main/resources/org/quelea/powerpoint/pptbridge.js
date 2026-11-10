// Quelea <-> Microsoft PowerPoint (macOS) long-running bridge.
//
// Run as: osascript -l JavaScript pptbridge.js /path/to/powerpoint.applescript
//
// Compiles powerpoint.applescript once and then answers one request per line
// on stdin, so a command costs one Apple event round trip (a few ms) instead
// of starting osascript and compiling the script every time (~250 ms).
//
// Request:  {"id": 7, "args": ["gotoSlide", "3", "1234"]}
// Response: {"id": 7, "ok": true, "result": "3|12|RUNNING"}
//       or: {"id": 7, "ok": false, "code": -1743, "message": "..."}
// On start: {"event": "ready"}   or {"event": "fatal", "code": n, "message": "..."}
//
// The args are passed to the script's "on run argv" handler, exactly like
// "osascript powerpoint.applescript <args...>" does. The bridge exits when
// stdin is closed.

ObjC.import('Foundation');

const UTF8 = $.NSUTF8StringEncoding;
const stdout = $.NSFileHandle.fileHandleWithStandardOutput;

function send(obj) {
    const line = $.NSString.alloc.initWithUTF8String(JSON.stringify(obj) + '\n');
    stdout.writeData(line.dataUsingEncoding(UTF8));
}

// Out-parameters for NSDictionary errors must be created with $(), not Ref().
function errorInfo(err) {
    const dict = ObjC.deepUnwrap(err) || {};
    const code = (typeof dict.NSAppleScriptErrorNumber === 'number') ? dict.NSAppleScriptErrorNumber : -2700;
    const message = dict.NSAppleScriptErrorMessage || 'Unknown AppleScript error';
    return {code: code, message: String(message)};
}

// kCoreEventClass 'aevt' / kAEOpenApplication 'oapp' is the event that runs a
// script's "run" handler; its direct object becomes argv.
const kCoreEventClass = 0x61657674;   // 'aevt'
const kAEOpenApplication = 0x6F617070; // 'oapp'
const keyDirectObject = 0x2D2D2D2D;   // '----'

function runEvent(args) {
    const list = $.NSAppleEventDescriptor.listDescriptor;
    for (let i = 0; i < args.length; i++) {
        list.insertDescriptorAtIndex($.NSAppleEventDescriptor.descriptorWithString(String(args[i])), i + 1);
    }
    const event = $.NSAppleEventDescriptor.appleEventWithEventClassEventIDTargetDescriptorReturnIDTransactionID(
            kCoreEventClass, kAEOpenApplication, $.NSAppleEventDescriptor.currentProcessDescriptor, -1, 0);
    event.setParamDescriptorForKeyword(list, keyDirectObject);
    return event;
}

function handle(script, line) {
    let req;
    try {
        req = JSON.parse(line);
    } catch (e) {
        send({id: -1, ok: false, code: -50, message: 'Bad request: ' + e});
        return;
    }
    const err = $();
    const result = script.executeAppleEventError(runEvent(req.args || []), err);
    if (result.isNil()) {
        const info = errorInfo(err);
        send({id: req.id, ok: false, code: info.code, message: info.message});
    } else {
        const s = result.stringValue;
        send({id: req.id, ok: true, result: (s && !s.isNil()) ? s.js : ''});
    }
}

function run(argv) {
    const path = argv[0];
    const source = $.NSString.stringWithContentsOfFileEncodingError(path, UTF8, null);
    if (source.isNil()) {
        send({event: 'fatal', code: -43, message: 'Cannot read ' + path});
        return;
    }
    const script = $.NSAppleScript.alloc.initWithSource(source);
    const compileErr = $();
    if (!script.compileAndReturnError(compileErr)) {
        const info = errorInfo(compileErr);
        send({event: 'fatal', code: info.code, message: info.message});
        return;
    }
    send({event: 'ready'});

    const stdin = $.NSFileHandle.fileHandleWithStandardInput;
    let buffer = '';
    while (true) {
        const data = stdin.availableData; // blocks until input or EOF
        if (Number(data.length) === 0) { // NSData.length arrives as a string
            break;
        }
        buffer += $.NSString.alloc.initWithDataEncoding(data, UTF8).js;
        let nl;
        while ((nl = buffer.indexOf('\n')) >= 0) {
            const line = buffer.substring(0, nl).trim();
            buffer = buffer.substring(nl + 1);
            if (line.length > 0) {
                handle(script, line);
            }
        }
    }
}
