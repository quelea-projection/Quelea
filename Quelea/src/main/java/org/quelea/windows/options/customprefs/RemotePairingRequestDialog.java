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
package org.quelea.windows.options.customprefs;

import java.time.Duration;
import java.time.Instant;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.quelea.server.v2.PairingService;
import org.quelea.server.v2.Scope;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.style.StyleManager;

/**
 * Asks whether a device that wants to pair may do so. Closes by itself when
 * the request expires; closing it without answering denies the request.
 */
public class RemotePairingRequestDialog extends Stage {

    private final PairingService pairing;
    private final PairingService.Request request;
    private final Label countdownLabel = new Label();
    private final CheckBox viewOnly = new CheckBox(LabelGrabber.INSTANCE.getLabel("remote.v2.pair.view.only"));
    private final Timeline countdown;
    private boolean answered;

    /**
     * Show the question for a request. Call on the FX thread.
     *
     * @param owner the window to show it over, may be null.
     * @param pairing the pairing service.
     * @param request the request.
     */
    public static void ask(Window owner, PairingService pairing, PairingService.Request request) {
        RemotePairingRequestDialog dialog = new RemotePairingRequestDialog(owner, pairing, request);
        dialog.show();
        dialog.toFront();
    }

    private RemotePairingRequestDialog(Window owner, PairingService pairing, PairingService.Request request) {
        this.pairing = pairing;
        this.request = request;
        if (owner != null) {
            initOwner(owner);
        }
        // Not modal: the service must go on while nobody answers.
        setAlwaysOnTop(true);
        setTitle(LabelGrabber.INSTANCE.getLabel("remote.v2.request.title"));
        setResizable(false);

        Label message = new Label(LabelGrabber.INSTANCE.getLabel("remote.v2.request.message")
                .replace("$1", request.name()));
        message.setFont(Font.font(null, FontWeight.BOLD, 15));
        message.setWrapText(true);
        StringBuilder details = new StringBuilder();
        if (request.platform() != null && !request.platform().isBlank()) {
            details.append(platformName(request.platform()));
        }
        if (request.address() != null) {
            if (details.length() > 0) {
                details.append(" · ");
            }
            details.append(request.address());
        }
        Label detailsLabel = new Label(details.toString());
        detailsLabel.setOpacity(0.7);
        Label hint = new Label(LabelGrabber.INSTANCE.getLabel("remote.v2.request.hint"));
        hint.setWrapText(true);

        Button deny = new Button(LabelGrabber.INSTANCE.getLabel("remote.v2.request.deny"));
        deny.setCancelButton(true);
        deny.setOnAction(e -> answer(false));
        Button allow = new Button(LabelGrabber.INSTANCE.getLabel("remote.v2.request.allow"));
        allow.setDefaultButton(true);
        allow.setOnAction(e -> answer(true));
        HBox buttons = new HBox(10, deny, allow);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(10, message, detailsLabel, hint, viewOnly, countdownLabel, buttons);
        root.setPadding(new Insets(20));
        root.setPrefWidth(380);
        Scene scene = new Scene(root);
        StyleManager.apply(scene);
        setScene(scene);

        countdown = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> tick()));
        countdown.setCycleCount(Animation.INDEFINITE);
        tick();
        countdown.play();
        setOnHidden(e -> {
            countdown.stop();
            if (!answered) {
                pairing.deny(request.id());
            }
        });
    }

    private void answer(boolean allow) {
        answered = true;
        if (allow) {
            pairing.approve(request.id(), viewOnly.isSelected() ? Scope.VIEW : Scope.CONTROL);
        } else {
            pairing.deny(request.id());
        }
        close();
    }

    private void tick() {
        long seconds = Duration.between(Instant.now(), request.expires()).toSeconds();
        PairingService.Request pending = pairing.pendingRequest();
        if (seconds <= 0 || pending == null || !pending.id().equals(request.id())) {
            // Expired, or the device stopped waiting.
            answered = true;
            close();
            return;
        }
        countdownLabel.setText(LabelGrabber.INSTANCE.getLabel("remote.v2.request.expires")
                .replace("$1", Long.toString(seconds)));
    }

    private static String platformName(String platform) {
        return switch (platform.toLowerCase(java.util.Locale.ROOT)) {
            case "ios" -> "iOS";
            case "android" -> "Android";
            case "web" -> LabelGrabber.INSTANCE.getLabel("remote.v2.request.web");
            default -> platform;
        };
    }
}
