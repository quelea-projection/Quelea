/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.services.phonehome;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;

/**
 * Responsible for phoning home at launch with a few pieces of anonymous
 * information about Quelea (at present just OS and version.) Just to collect a
 * bit of data to see how widely it's being used.
 *
 * If you don't want phone home enabled, simply put phonehome=false in the
 * properties file.
 *
 * @author Michael
 */
public class PhoneHome {

    public static final PhoneHome INSTANCE = new PhoneHome();
    private static final Logger LOGGER = LoggerUtils.getLogger();

    private PhoneHome() {
        //Only private.
    }

    /**
     * If phoning home is enabled, then go for it.
     */
    public void phone() {
        if(!QueleaProperties.get().getPhoneHome()) {
            LOGGER.log(Level.INFO, "Phone home disabled");
            return;
        }

        String os = System.getProperty("os.name") + " : " + System.getProperty("os.version");

        final StringBuilder urlStrBuilder = new StringBuilder("http://quelea.org/phonehome/store.php?os=");
        urlStrBuilder.append(os);
        urlStrBuilder.append("&version=");
        urlStrBuilder.append(QueleaProperties.VERSION.getVersionString());
        urlStrBuilder.append("&language=");
        urlStrBuilder.append(Locale.getDefault().getDisplayLanguage(Locale.ENGLISH));
        urlStrBuilder.append("&totalmem=");
        try {
            long physicalMemorySize = ((com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
            urlStrBuilder.append(Long.toString(physicalMemorySize / 1048576)).append(" MB");
        }
        catch(Throwable ex) {
            urlStrBuilder.append("Unknown");
        }
        urlStrBuilder.append("&osarch=");
        if(System.getProperty("os.name").contains("Windows")) {
            if((System.getenv("ProgramFiles(x86)") != null)) {
                urlStrBuilder.append("amd64");
            }
            else {
                urlStrBuilder.append("x86");
            }
        }
        else {
            urlStrBuilder.append("Unknown");
        }

        final String urlStr = urlStrBuilder.toString().replace(" ", "%20");

        //Execute in the background...
        new Thread() {
            @Override
            public void run() {
                LOGGER.log(Level.INFO, "Phoning home..");
                final StringBuilder result = new StringBuilder();
                try(BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(urlStr).openStream()));) {

                    String line;
                    while((line = reader.readLine()) != null) {
                        result.append(line).append('\n');
                    }
                    LOGGER.log(Level.INFO, "Phone home result: {0}", result.toString().trim());
                }
                catch(IOException ex) {
                    LOGGER.log(Level.WARNING, "Phone home failed", ex);
                }

            }
        }.start();
    }

    public static void main(String[] args) {
        PhoneHome.INSTANCE.phone();
    }
}
