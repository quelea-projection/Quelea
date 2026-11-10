package org.quelea.windows.presentation.powerpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Stands in for pptbridge.js in tests: same line protocol, scripted answers.
 * <ul>
 * <li>echo a b -> ok, "a b"</li>
 * <li>fail N msg -> error with code N</li>
 * <li>hang -> never answers</li>
 * <li>exit -> quits without answering</li>
 * </ul>
 * Started with "fatal" as its first argument it reports a start failure.
 */
public final class FakeBridge {

    private FakeBridge() {
    }

    public static void main(String[] argv) throws Exception {
        if (argv.length > 0 && "fatal".equals(argv[0])) {
            System.out.println("{\"event\":\"fatal\",\"code\":-2741,\"message\":\"syntax error\"}");
            System.out.flush();
            return;
        }
        System.out.println("{\"event\":\"ready\"}");
        System.out.flush();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String line;
        while ((line = in.readLine()) != null) {
            JsonObject req = JsonParser.parseString(line).getAsJsonObject();
            long id = req.get("id").getAsLong();
            JsonArray args = req.getAsJsonArray("args");
            String cmd = args.get(0).getAsString();
            JsonObject res = new JsonObject();
            res.addProperty("id", id);
            switch (cmd) {
                case "echo":
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < args.size(); i++) {
                        if (i > 1) {
                            sb.append(' ');
                        }
                        sb.append(args.get(i).getAsString());
                    }
                    res.addProperty("ok", true);
                    res.addProperty("result", sb.toString());
                    break;
                case "fail":
                    res.addProperty("ok", false);
                    res.addProperty("code", Integer.parseInt(args.get(1).getAsString()));
                    res.addProperty("message", args.get(2).getAsString());
                    break;
                case "hang":
                    Thread.sleep(60_000);
                    continue;
                case "exit":
                    return;
                default:
                    res.addProperty("ok", false);
                    res.addProperty("code", -50);
                    res.addProperty("message", "Unknown command: " + cmd);
            }
            System.out.println(res);
            System.out.flush();
        }
    }
}
