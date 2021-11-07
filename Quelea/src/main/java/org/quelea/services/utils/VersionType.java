/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.services.utils;

/**
 *
 * @author Michael
 */
public enum VersionType {

    BETA("icons/splash-beta.png"), CI("icons/splash-bare-nightly.png"), RELEASE("icons/splash-bare.png");

    VersionType(String splashPath) {
        this.splashPath = splashPath;
    }

    private String splashPath;

    public String getSplashPath() {
        return splashPath;
    }

}
