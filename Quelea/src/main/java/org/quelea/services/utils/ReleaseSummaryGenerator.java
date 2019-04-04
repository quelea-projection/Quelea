/* 
 * This file is part of Quelea, free projection software for churches.
 * 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.services.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Generates release summary information in markdown.
 * @author Michael
 */
public class ReleaseSummaryGenerator {
    
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        Map<String, String> hashes = new HashMap<>();
        for(File releaseFile : new File("dist/standalone").listFiles()) {
            if(releaseFile.getName().toLowerCase().contains("windows") && releaseFile.getName().toLowerCase().contains("x64")) {
                hashes.put("Windows x64", sha256(releaseFile.getAbsolutePath()));
            }
            else if(releaseFile.getName().toLowerCase().contains("crossplatform")) {
                hashes.put("Crossplatform", sha256(releaseFile.getAbsolutePath()));
            }
            else if(releaseFile.getName().toLowerCase().contains("mac")) {
                hashes.put("Mac", sha256(releaseFile.getAbsolutePath()));
            }
        }
        
        String snapFlag = "";
        switch (QueleaProperties.VERSION.getUnstableName()) {
            case BETA:
                snapFlag = "--beta ";
                break;
            case CI:
                snapFlag = "--edge ";
                break;
        }
        
        String release = "Quelea is also distributed as a Linux snap package. To install it, make sure snap is installed then run:"
                + "<pre>sudo snap install " + snapFlag + "quelea</pre>"
                + "<h3>SHA256 hashes:</h3>"
                + "<table>";
        
        for(Entry<String, String> hash : hashes.entrySet()) {
            release += "<tr><th>" + hash.getKey() + "</th><td>" + hash.getValue() + "</td></tr>";
        }
        release += "</table>";
        
        String changelogFile = "changelogs/changelog-" + QueleaProperties.VERSION.getVersionString()+ ".txt";
        if(new File(changelogFile).exists() && QueleaProperties.VERSION.getUnstableName()!=VersionType.CI) {
            release += "\n\nFull changelog:\n";
            release += getVersionFileText(changelogFile);
        }
        
        System.out.println("Release documentation (to paste into Markdown):\n");
        System.out.println(release);
    }
    
    private static String getVersionFileText(String str) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(str));
        String ret = new String(encoded);
        ret = ret.substring(ret.indexOf('\n')+1);
        return ret;
    }
    
    private static String sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
    
}
