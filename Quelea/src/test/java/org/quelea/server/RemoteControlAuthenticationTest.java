package org.quelea.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Remote-control device sessions")
public class RemoteControlAuthenticationTest {
    @AfterEach
    public void clearSessions() {
        RCHandler.logAllOut();
    }

    @Test
    public void loggingOutOneDeviceLeavesOtherDevicesSignedIn() {
        RCHandler.addDevice("test-device-one");
        RCHandler.addDevice("test-device-two");
        RCHandler.logout("test-device-one");
        Assertions.assertFalse(RCHandler.isLoggedOn("test-device-one"));
        Assertions.assertTrue(RCHandler.isLoggedOn("test-device-two"));
        RCHandler.logAllOut();
        Assertions.assertFalse(RCHandler.isLoggedOn("test-device-two"));
    }

    @Test
    public void logoutRevokesRepeatedLoginsFromTheSameDevice() {
        RCHandler.addDevice("test-device");
        RCHandler.addDevice("test-device");
        RCHandler.logout("test-device");
        Assertions.assertFalse(RCHandler.isLoggedOn("test-device"));
    }
}
