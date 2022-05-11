package org.quelea.utils;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.quelea.services.utils.LoggerUtils;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.discovery.StandardNativeDiscoveryStrategy;
import uk.co.caprica.vlcj.discovery.mac.DefaultMacNativeDiscoveryStrategy;
import uk.co.caprica.vlcj.discovery.windows.DefaultWindowsNativeDiscoveryStrategy;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

/**
 * Custom discovery for VLC args. Includes folder where libvlc is located on
 * newer linux distros.
 *
 * @author michael
 */
public class VLCDiscovery {

    private static Logger LOGGER = LoggerUtils.getLogger();

    public NativeDiscovery getNativeDiscovery() {
        return new NativeDiscovery(
                new DefaultWindowsNativeDiscoveryStrategy(),
                new DefaultMacNativeDiscoveryStrategy(),
                new LinuxDiscoveryStrategy()
        );
    }

    public static class LinuxDiscoveryStrategy extends StandardNativeDiscoveryStrategy {

        @Override
        protected Pattern[] getFilenamePatterns() {
            return new Pattern[]{
                    Pattern.compile("libvlc\\.so(?:\\.\\d)*"),
                    Pattern.compile("libvlccore\\.so(?:\\.\\d)*")
            };
        }

        @Override
        public final boolean supported() {
            boolean supported = RuntimeUtil.isNix();
            LOGGER.log(Level.INFO, System.getProperty("os.name") + " linux discovery strategy: " + supported);
            return supported;
        }

        @Override
        protected void onGetDirectoryNames(List<String> directoryNames) {
            directoryNames.add("/usr/lib");
            directoryNames.add("/usr/lib64");
            directoryNames.add("/usr/local/lib");
            directoryNames.add("/usr/local/lib64");
            directoryNames.add("/usr/lib/x86_64-linux-gnu");
            directoryNames.add("/usr/lib/i386-linux-gnu");
            directoryNames.add(new File("../usr/lib").getAbsolutePath());
        }

    }
}
