package utils;

import java.util.List;
import java.util.regex.Pattern;
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

    public NativeDiscovery getNativeDiscovery() {
        return new NativeDiscovery(
                new DefaultWindowsNativeDiscoveryStrategy(),
                new DefaultMacNativeDiscoveryStrategy(),
                new LinuxDiscoveryStrategy()
        );
    }

    static class LinuxDiscoveryStrategy extends StandardNativeDiscoveryStrategy {

        @Override
        protected Pattern[] getFilenamePatterns() {
            return new Pattern[]{
                Pattern.compile("libvlc\\.so(?:\\.\\d)*"),
                Pattern.compile("libvlccore\\.so(?:\\.\\d)*")
            };
        }

        @Override
        public final boolean supported() {
            return RuntimeUtil.isNix();
        }

        @Override
        protected void onGetDirectoryNames(List<String> directoryNames) {
            directoryNames.add("/usr/lib");
            directoryNames.add("/usr/local/lib");
            directoryNames.add("/usr/lib/x86_64-linux-gnu");
        }

    }
}
