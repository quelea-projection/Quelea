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

    BETA("icons/splash-beta-new.png"), CI("icons/splash-ci-new.png"), RELEASE("icons/splash-new.png");

    VersionType(String splashPath) {
        this.splashPath = splashPath;
    }

    private final String splashPath;

    public String getSplashPath() {
        return splashPath;
    }

}
