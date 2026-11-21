-- Quelea <-> Microsoft PowerPoint (macOS) control script.
--
-- Usage: osascript powerpoint.applescript <command> [args...]
--
-- The slide show commands return the state as "slide|total|mode", where mode
-- is one of RUNNING, BLACK, WHITE, PAUSED, DONE, NOSHOW or NOTRUNNING.
-- Errors keep their AppleScript error number, so the caller can tell
-- "not authorised" (-1743) apart from "no slide show" (-1728/-1719).
--
-- PowerPoint's own dictionary is used wherever it can do the job, so opening,
-- next/previous and reading the state need only the Automation permission
-- for PowerPoint. Black screen and a clean jump to an arbitrary slide send
-- keys straight to the PowerPoint process, and moving the show to Quelea's
-- screen uses System Events; those need the Accessibility permission (and
-- System Events Automation for the move). Without it a jump steps there
-- instead. PowerPoint is never brought to the front.
--
-- Notes for PowerPoint 16.x:
--  * "slide state" of a slide show view is read only, and "go to slide" does
--    not accept a slide show view (-50).
--  * "slide show settings" are read only (-10006), so looping is done by
--    Quelea (go to slide 1 after the last slide) and the show type is whatever
--    the presentation is set to.
--  * tell blocks must name the application literally, otherwise the
--    dictionary is not loaded when the script is compiled.
--  * The slide show always starts on the main screen and its window cannot
--    be moved through the dictionary ("bounds" is ignored, "left position"
--    gives -10006); see openShow for how it gets to Quelea's screen.
--
-- The long-running bridge (pptbridge.js) loads this file once and calls the
-- handlers directly.

on run argv
	if (count of argv) is 0 then error "No command given" number -50
	set cmd to item 1 of argv
	if cmd is "state" then return state()
	if cmd is "openShow" then
		if (count of argv) > 2 then return openShow(item 2 of argv, item 3 of argv)
		return openShow(item 2 of argv, "")
	end if
	if cmd is "showBounds" then return showBounds()
	if cmd is "closeShow" then return closeShow(item 2 of argv)
	if cmd is "nextSlide" then return nextSlide()
	if cmd is "previousSlide" then return previousSlide()
	if cmd is "gotoSlide" then return gotoSlide(item 2 of argv, item 3 of argv)
	if cmd is "setBlack" then return setBlack(item 2 of argv, item 3 of argv)
	if cmd is "focus" then return focus()
	if cmd is "exportPdf" then return exportPdf(item 2 of argv, item 3 of argv)
	if cmd is "ping" then return "pong"
	error "Unknown command: " & cmd number -50
end run

on isRunning()
	return application id "com.microsoft.Powerpoint" is running
end isRunning

on asBool(v)
	if class of v is boolean then return v
	return (v as text) is in {"true", "1", "yes"}
end asBool

-- "slide|total|mode" for the frontmost slide show.
on state()
	if not isRunning() then return "0|0|NOTRUNNING"
	tell application id "com.microsoft.Powerpoint"
		if (count of slide show windows) is 0 then
			set tot to 0
			try
				set tot to count of slides of active presentation
			end try
			return "0|" & tot & "|NOSHOW"
		end if
		set v to slideshow view of slide show window 1
		set showState to slide state of v
		set tot to 0
		try
			set tot to count of slides of presentation of slide show window 1
		end try
		set idx to 0
		try
			-- Fails on the black "end of slide show" slide.
			set idx to slide index of slide of v
		end try
		if showState is slide show state black screen then
			set m to "BLACK"
		else if showState is slide show state white screen then
			set m to "WHITE"
		else if showState is slide show state paused then
			set m to "PAUSED"
		else if showState is slide show state done then
			set m to "DONE"
		else
			set m to "RUNNING"
		end if
	end tell
	return (idx as text) & "|" & tot & "|" & m
end state

on fileName(posixPath)
	set AppleScript's text item delimiters to "/"
	set n to last text item of posixPath
	set AppleScript's text item delimiters to ""
	return n
end fileName

-- The open presentation whose file is posixPath, or missing value. PowerPoint
-- reports /tmp instead of /private/tmp and so on, so match on the file name
-- and use the full path only to choose between files with the same name.
on findPresentation(posixPath)
	set fname to fileName(posixPath)
	set found to missing value
	tell application id "com.microsoft.Powerpoint"
		repeat with i from 1 to (count of presentations)
			set p to presentation i
			if name of p is fname then
				if full name of p is posixPath then return p
				if found is missing value then set found to p
			end if
		end repeat
	end tell
	return found
end findPresentation

-- Open the presentation at posixPath (unless it is open already) and wait at
-- most ~15 s for it.
on openPresentation(posixPath)
	set pres to findPresentation(posixPath)
	if pres is not missing value then return pres
	-- Open through Launch Services like a double click in Finder: that hands
	-- the sandboxed PowerPoint access to the file. PowerPoint's own "open"
	-- command would show a "Grant File Access" dialog for every new folder and
	-- block until someone answers it.
	do shell script "/usr/bin/open -b com.microsoft.Powerpoint " & quoted form of posixPath
	repeat 75 times
		set pres to findPresentation(posixPath)
		if pres is not missing value then return pres
		delay 0.2
	end repeat
	error "PowerPoint did not open the presentation in time" number -1712
end openPresentation

-- Open the presentation at the given POSIX path and start its slide show.
--
-- screenRect is the area Quelea projects to, "left,top,right,bottom" in whole
-- points (origin at the top left of the main screen, as JavaFX uses), or ""
-- to leave the show where PowerPoint puts it. PowerPoint always starts the
-- show on the main screen (whatever window or setting), and its slide show
-- window ignores "bounds", but it is an ordinary borderless window, not a
-- macOS full screen Space, so with the Accessibility permission it can be
-- moved there through System Events (ensureShowOn). The answer ends in
-- "|OFFSCREEN" if the show is elsewhere and that permission is missing.
on openShow(posixPath, screenRect)
	set pres to openPresentation(posixPath)
	tell application id "com.microsoft.Powerpoint"
		run slide show (slide show settings of pres)
		repeat 50 times
			if (count of slide show windows) > 0 then exit repeat
			delay 0.1
		end repeat
	end tell
	set target to parseRect(screenRect)
	set flag to ""
	if target is not missing value then set flag to ensureShowOn(target)
	return state() & flag
end openShow

-- "l,t,r,b" (whole numbers) as {l, t, r, b}, or missing value.
on parseRect(t)
	if t is missing value or t is "" then return missing value
	set AppleScript's text item delimiters to ","
	set parts to text items of t
	set AppleScript's text item delimiters to ""
	if (count of parts) is not 4 then return missing value
	try
		return {(item 1 of parts) as integer, (item 2 of parts) as integer, (item 3 of parts) as integer, (item 4 of parts) as integer}
	on error
		return missing value
	end try
end parseRect

-- True if the middle of the target area lies within the (full screen) show,
-- i.e. the show is on the screen Quelea projects to, even when Quelea uses a
-- custom area smaller than that screen.
on isOnShow(showBounds4, target)
	set {l, t, r, b} to showBounds4
	set cx to ((item 1 of target) + (item 3 of target)) / 2
	set cy to ((item 2 of target) + (item 4 of target)) / 2
	return cx >= l and cx < r and cy >= t and cy < b
end isOnShow

-- Move the slide show window onto the target area unless it is there already.
-- Returns "" if it is (or was moved) there, "|OFFSCREEN" if it could not be
-- moved for lack of the Accessibility permission.
on ensureShowOn(target)
	tell application id "com.microsoft.Powerpoint"
		if (count of slide show windows) is 0 then return ""
		set sb to (get bounds of slide show window 1)
	end tell
	if isOnShow(sb, target) then return ""
	if not isTrusted() then return "|OFFSCREEN"
	set {l, t, r, b} to target
	set showX to (item 1 of sb) as integer
	set showY to (item 2 of sb) as integer
	try
		tell application "System Events"
			set ppt to first process whose bundle identifier is "com.microsoft.Powerpoint"
			repeat with i from 1 to (count of windows of ppt)
				set w to window i of ppt
				set pos to missing value
				try
					set pos to (get position of w)
				end try
				if pos is not missing value then
					if ((item 1 of pos) as integer) is showX and ((item 2 of pos) as integer) is showY then
						set position of w to {l, t}
						set size of w to {r - l, b - t}
						exit repeat
					end if
				end if
			end repeat
		end tell
	end try
	delay 0.3
	return ""
end ensureShowOn

-- Where the slide show is: "left,top,right,bottom" in whole points, or "" if
-- no show is running.
on showBounds()
	if not isRunning() then return ""
	tell application id "com.microsoft.Powerpoint"
		if (count of slide show windows) is 0 then return ""
		set {l, t, r, b} to bounds of slide show window 1
	end tell
	return ((l as integer) as text) & "," & ((t as integer) as text) & "," & ((r as integer) as text) & "," & ((b as integer) as text)
end showBounds

-- End the slide show of the presentation at posixPath and close it without
-- saving. Other presentations the user has open are left alone.
on closeShow(posixPath)
	if not isRunning() then return state()
	set pres to findPresentation(posixPath)
	if pres is missing value then return state()
	tell application id "com.microsoft.Powerpoint"
		repeat with i from (count of slide show windows) to 1 by -1
			try
				if name of presentation of slide show window i is name of pres then
					exit slide show (slideshow view of slide show window i)
				end if
			end try
		end repeat
		close pres saving no
	end tell
	return state()
end closeShow

on nextSlide()
	tell application id "com.microsoft.Powerpoint" to go to next slide (slideshow view of slide show window 1)
	return state()
end nextSlide

on previousSlide()
	tell application id "com.microsoft.Powerpoint" to go to previous slide (slideshow view of slide show window 1)
	return state()
end previousSlide

-- The slide show view has no "go to slide n" (the "go to slide" command only
-- works on the editing view, -50 on a slide show; the slide show settings
-- that would pick the starting slide are read only). So:
--  * next/previous/first/last slide use the exact commands;
--  * with the Accessibility permission, the number is sent to the show like
--    a presenter would type it (main-row key codes plus Return; as typed
--    characters PowerPoint 16.x beeps and advances once per key), straight
--    to the PowerPoint process, so PowerPoint stays in the background;
--  * otherwise, or if the show did not follow, it steps there (stepTo).
-- 1-based slide number. returnPid is accepted for compatibility, unused.
on gotoSlide(n, returnPid)
	set n to n as integer
	set cur to 0
	set tot to 0
	tell application id "com.microsoft.Powerpoint"
		set v to slideshow view of slide show window 1
		try
			set cur to slide index of slide of v
		end try
		try
			set tot to count of slides of presentation of slide show window 1
		end try
		if n < 1 or (tot > 0 and n > tot) then error "No slide " & n number -1728
		if n is cur then return my state()
		if n is cur + 1 then
			go to next slide v
			return my state()
		else if n is cur - 1 then
			go to previous slide v
			return my state()
		else if n is 1 then
			go to first slide v
			return my state()
		else if n is tot then
			go to last slide v
			return my state()
		end if
	end tell
	set digitCodes to {29, 18, 19, 20, 21, 23, 22, 26, 28, 25} -- main row 0-9
	set codes to {}
	repeat with c in characters of (n as text)
		set end of codes to item ((c as integer) + 1) of digitCodes
	end repeat
	set end of codes to 36 -- Return
	if isTrusted() then
		try
			postKeys(codes)
			if waitForSlide(n) then return state()
		end try
	end if
	stepTo(n, tot)
	return state()
end gotoSlide

-- Send key presses (virtual key codes) straight to the PowerPoint process
-- (CGEventPostToPid), without bringing it to the front: a full screen show
-- takes them while another app is in front, it just reports "paused". Needs
-- the Accessibility permission; raises 1002 without it. AppleScript cannot
-- call C functions, so a small JavaScript for Automation snippet does it.
on postKeys(codes)
	set js to "function run(argv) {
	ObjC.import('Cocoa'); ObjC.import('ApplicationServices'); ObjC.import('CoreGraphics');
	if (!$.AXIsProcessTrusted()) { throw new Error('NOT_TRUSTED'); }
	const apps = $.NSRunningApplication.runningApplicationsWithBundleIdentifier('com.microsoft.Powerpoint');
	if (apps.count === 0) { throw new Error('NOT_RUNNING'); }
	const pid = apps.objectAtIndex(0).processIdentifier;
	for (const a of argv) {
		const code = parseInt(a, 10);
		$.CGEventPostToPid(pid, $.CGEventCreateKeyboardEvent($(), code, true));
		delay(0.02);
		$.CGEventPostToPid(pid, $.CGEventCreateKeyboardEvent($(), code, false));
		delay(0.05);
	}
	return 'ok';
}"
	set args to ""
	repeat with c in codes
		set args to args & " " & (c as integer)
	end repeat
	try
		do shell script "/usr/bin/osascript -l JavaScript -e " & quoted form of js & args
	on error errText number errNum
		if errText contains "NOT_TRUSTED" then error "Quelea may not send keys to PowerPoint (Accessibility)" number 1002
		if errText contains "NOT_RUNNING" then error "PowerPoint is not running" number -600
		error errText number errNum
	end try
end postKeys

-- True if this process has the Accessibility permission (asked without
-- sending anything, and without needing System Events). Asking starts a
-- process, so the answer is kept for 30 s (the bridge keeps the script, and
-- with it these properties, between commands).
property trustedAnswer : false
property trustedAskedAt : missing value

on isTrusted()
	if trustedAskedAt is not missing value then
		if ((current date) - trustedAskedAt) < 30 then return trustedAnswer
	end if
	try
		set trustedAnswer to (do shell script "/usr/bin/osascript -l JavaScript -e 'ObjC.import(\"ApplicationServices\"); $.AXIsProcessTrusted() ? \"yes\" : \"no\"'") is "yes"
	on error
		set trustedAnswer to false
	end try
	set trustedAskedAt to current date
	return trustedAnswer
end isTrusted

-- Wait at most ~2 s for the show to be on slide n.
on waitForSlide(n)
	repeat 20 times
		try
			tell application id "com.microsoft.Powerpoint"
				if (slide index of slide of slideshow view of slide show window 1) is n then return true
			end tell
		end try
		delay 0.1
	end repeat
	return false
end waitForSlide

-- Step to slide n with next/previous, starting from the first, the last or
-- the current slide, whichever is nearest. Needs no extra permission, but
-- slides in between show for a moment and a slide with animations takes one
-- step per animation.
on stepTo(n, tot)
	tell application id "com.microsoft.Powerpoint"
		set v to slideshow view of slide show window 1
		set cur to 0
		try
			set cur to slide index of slide of v
		end try
		if cur is n then return
		set fromCur to n - cur
		if fromCur < 0 then set fromCur to -fromCur
		if cur < 1 then set fromCur to tot -- unknown (e.g. past the end)
		if (n - 1) < fromCur and (n - 1) <= (tot - n) then
			go to first slide v
		else if (tot - n) < fromCur then
			go to last slide v
		end if
		-- Animations take extra steps, so allow plenty, but never loop forever.
		repeat (tot * 10 + 20) times
			set cur to 0
			try
				set cur to slide index of slide of v
			end try
			if cur is n then exit repeat
			if cur > n then
				go to previous slide v
			else
				go to next slide v
			end if
		end repeat
		if cur is not n then error "PowerPoint did not reach slide " & n number 1003
	end tell
end stepTo

-- Wait at most ~2 s for the show to be (or not be) blacked out.
on waitForBlack(wantBlack)
	repeat 20 times
		tell application id "com.microsoft.Powerpoint"
			if ((slide state of slideshow view of slide show window 1) is slide show state black screen) is wantBlack then return true
		end tell
		delay 0.1
	end repeat
	return false
end waitForBlack

-- "slide state" is read only (-10006), so toggle black screen by sending the
-- "b" key to the show (needs Accessibility, see postKeys).
on setBlack(flag, returnPid)
	set wantBlack to asBool(flag)
	set isBlack to false
	tell application id "com.microsoft.Powerpoint"
		set isBlack to (slide state of slideshow view of slide show window 1) is slide show state black screen
	end tell
	if isBlack is wantBlack then return state()
	if not isTrusted() then error "Quelea may not send keys to PowerPoint (Accessibility)" number 1002
	repeat with attempt from 1 to 2
		postKeys({11}) -- "b"
		if waitForBlack(wantBlack) then return state()
	end repeat
	error "PowerPoint did not change black screen after the b key" number 1003
end setBlack

on focus()
	tell application id "com.microsoft.Powerpoint" to activate
	return state()
end focus

-- Save the presentation at posixPath as a PDF at pdfPath, which must be
-- inside PowerPoint's sandbox container (~/Library/Containers/
-- com.microsoft.Powerpoint/Data/...). Returns the number of slides. The
-- presentation is closed again unless it was open already.
-- ("save as PNG"/"save as JPG" report success but write nothing in 16.x.)
on exportPdf(posixPath, pdfPath)
	set wasOpen to (findPresentation(posixPath) is not missing value)
	set pres to openPresentation(posixPath)
	tell application id "com.microsoft.Powerpoint"
		set n to count of slides of pres
		save pres in (POSIX file pdfPath) as save as PDF
		if not wasOpen then close pres saving no
	end tell
	return n as text
end exportPdf
