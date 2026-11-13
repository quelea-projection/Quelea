/*
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea.windows.presentation.powerpoint;

/**
 * A PowerPoint command that failed, with the reason classified so the UI can
 * react to it (for example by explaining which permission is missing).
 */
public class PowerPointException extends Exception {

    public enum Kind {
        /** PowerPoint is not running (or quit while we talked to it). */
        NOT_RUNNING,
        /** No slide show is running, or the slide does not exist. */
        NO_SLIDESHOW,
        /** macOS Automation permission to control PowerPoint is missing. */
        NOT_AUTHORIZED,
        /** macOS Automation permission to control System Events is missing. */
        NOT_AUTHORIZED_SYSTEM_EVENTS,
        /** macOS Accessibility permission (needed to type into PowerPoint) is missing. */
        NOT_ACCESSIBLE,
        /** Windows: a presentation is already open and must be closed first. */
        ALREADY_RUNNING,
        /** PowerPoint did not answer in time. */
        TIMEOUT,
        /** PowerPoint could not be found or started. */
        NOT_AVAILABLE,
        /** Anything else. */
        FAILED
    }

    /** Raised by powerpoint.applescript when it may not send keys to PowerPoint (Accessibility). */
    static final int CODE_NOT_ACCESSIBLE = 1002;
    /** Raised by powerpoint.applescript when System Events may not be controlled. */
    static final int CODE_SYSTEM_EVENTS_NOT_AUTHORIZED = 1743;
    /** Raised by powerpoint.applescript when keys were sent but the show did not follow. */
    static final int CODE_TYPING_IGNORED = 1003;
    /** The show is not on Quelea's projector screen and cannot be moved there without Accessibility. */
    static final int CODE_SHOW_OFF_SCREEN = 1004;

    private final Kind kind;
    private final int code;

    public PowerPointException(Kind kind, String message) {
        this(kind, 0, message, null);
    }

    public PowerPointException(Kind kind, int code, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
        this.code = code;
    }

    /**
     * Classify an AppleScript error number.
     *
     * @param code the AppleScript / Apple event error number.
     * @param message the error message.
     * @return the exception.
     */
    public static PowerPointException fromAppleScript(int code, String message) {
        return new PowerPointException(kindOf(code), code, message + " (" + code + ")", null);
    }

    static Kind kindOf(int code) {
        switch (code) {
            case -1743: // errAEEventNotPermitted
            case -1744: // errAEEventWouldRequireUserConsent
                return Kind.NOT_AUTHORIZED;
            case CODE_SYSTEM_EVENTS_NOT_AUTHORIZED:
                return Kind.NOT_AUTHORIZED_SYSTEM_EVENTS;
            case CODE_NOT_ACCESSIBLE:
            case -25211: // kAXErrorAPIDisabled
                return Kind.NOT_ACCESSIBLE;
            case -1728: // errAENoSuchObject
            case -1719: // errAEIllegalIndex
                return Kind.NO_SLIDESHOW;
            case -1712: // errAETimeout
                return Kind.TIMEOUT;
            case -600:  // procNotFound
            case -609:  // connectionInvalid
            case -903:  // noPortErr
                return Kind.NOT_RUNNING;
            case -2700: // not found in the dictionary / app missing
            case -10814: // kLSApplicationNotFoundErr
                return Kind.NOT_AVAILABLE;
            default:
                return Kind.FAILED;
        }
    }

    public Kind getKind() {
        return kind;
    }

    /**
     * @return the AppleScript error number, or 0 if there is none.
     */
    public int getCode() {
        return code;
    }

    /**
     * @return true if this is about a missing macOS permission.
     */
    public boolean isPermissionProblem() {
        return kind == Kind.NOT_AUTHORIZED || kind == Kind.NOT_AUTHORIZED_SYSTEM_EVENTS
                || kind == Kind.NOT_ACCESSIBLE;
    }
}
