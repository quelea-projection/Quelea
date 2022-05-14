package org.quelea.utils;

import org.quelea.services.utils.LoggerUtils;
import uk.co.caprica.vlcj.Info;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.discovery.StandardNativeDiscoveryStrategy;
import uk.co.caprica.vlcj.discovery.mac.DefaultMacNativeDiscoveryStrategy;
import uk.co.caprica.vlcj.discovery.windows.DefaultWindowsNativeDiscoveryStrategy;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Custom discovery for VLC args. Includes folder where libvlc is located on
 * newer linux distros.
 *
 * @author michael
 */
public class VLCDiscovery {

    private static Logger LOGGER = LoggerUtils.getLogger();

    public NativeDiscovery getNativeDiscovery() {
        return new NativeDiscovery(new DefaultWindowsNativeDiscoveryStrategy(), new DefaultMacNativeDiscoveryStrategy(), new LinuxDiscoveryStrategy());
    }

    public static class LinuxDiscoveryStrategy extends StandardNativeDiscoveryStrategy {

        @Override
        public Pattern[] getFilenamePatterns() {
            return new Pattern[]{Pattern.compile("libvlc\\.so(?:\\.\\d)*"), Pattern.compile("libvlccore\\.so(?:\\.\\d)*")};
        }

        @Override
        public final boolean supported() {
            return RuntimeUtil.isNix();
        }

        @Override
        public void onGetDirectoryNames(List<String> directoryNames) {
            LOGGER.info("Adding search dirs with root dir " + new File(".").getAbsolutePath());

            Stream.of("/usr/lib", "/usr/lib64", "/usr/local/lib", "/usr/local/lib64", "/usr/lib/x86_64-linux-gnu", "/usr/lib/i386-linux-gnu", Paths.get("/usr/lib/x86_64-linux-gnu").toAbsolutePath().normalize().toString()).filter(this::candidate).forEach(directoryNames::add);

            LOGGER.info("Found candidate dirs: " + directoryNames);

//            directoryNames.add("/usr/lib");
//            directoryNames.add("/usr/lib64");
//            directoryNames.add("/usr/local/lib");
//            directoryNames.add("/usr/local/lib64");
//            directoryNames.add("/usr/lib/x86_64-linux-gnu");
//            directoryNames.add("/usr/lib/i386-linux-gnu");
//            directoryNames.add(Paths.get("../usr/lib").toAbsolutePath().normalize().toString());
//            directoryNames.add(Paths.get("../usr/lib64").toAbsolutePath().normalize().toString());
//            directoryNames.add(Paths.get("../usr/lib/x86_64-linux-gnu").toAbsolutePath().normalize().toString());
//            directoryNames.add(Paths.get("../usr/lib/i386-linux-gnu").toAbsolutePath().normalize().toString());
        }

        public boolean candidate(String path) {
            if(true) return true;
            try {
                var list = Files.list(Paths.get(path)).filter(Files::isRegularFile).map(p -> p.getFileName().toString()).collect(Collectors.toList());
                boolean v = false;
                boolean core = false;
                for (String s : list) {
                    if (s.startsWith("libvlc.")) {
                        v = true;
                    }
                    if (s.startsWith("libvlccore.")) {
                        core = true;
                    }
                }
                boolean res = v && core;
                LOGGER.info(path + " candidate: " + res);
                return res;
            } catch (IOException ex) {
                LOGGER.warning(path + " doesn't exist");
                return false;
            }
        }

    }
}
