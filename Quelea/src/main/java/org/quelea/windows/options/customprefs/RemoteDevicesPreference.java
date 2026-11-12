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

import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.stage.Window;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.quelea.server.v2.DeviceRegistry;
import org.quelea.server.v2.RemoteApiServer;
import org.quelea.server.v2.Scope;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;

/**
 * The paired devices of the v2 remote control in the server settings: a
 * button to pair a new device and a list with a revoke button per device.
 * It is not a setting; its field only gives PreferencesFX something to show.
 */
public class RemoteDevicesPreference extends SimpleControl<StringField, VBox> {

    private static final DateTimeFormatter LAST_SEEN = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault());

    private final VBox deviceList = new VBox(6);
    private boolean listening;
    /**
     * Kept in a field so the derived value stays alive while it is listened to.
     */
    private ObservableValue<Boolean> windowShowing;

    @Override
    public void initializeParts() {
        super.initializeParts();
        node = new VBox(10);
        node.getStyleClass().add("simple-text-control");
    }

    @Override
    public void layoutParts() {
        render();
        // The settings dialog is built with the main window, before the v2
        // server has started: render again when the server starts, and each
        // time the settings window is shown.
        QueleaApp.get().remoteV2ServerProperty().addListener((obs, oldServer, server) -> Platform.runLater(this::render));
        windowShowing = node.sceneProperty()
                .flatMap(Scene::windowProperty)
                .flatMap(Window::showingProperty)
                .orElse(false);
        windowShowing.addListener((obs, wasShowing, showing) -> {
            if (showing) {
                render();
            }
        });
    }

    private void render() {
        RemoteApiServer server = QueleaApp.get().getRemoteV2Server();
        if (server == null || server.getDevices() == null || server.getPairing() == null) {
            Label notStarted = new Label(LabelGrabber.INSTANCE.getLabel("remote.v2.restart.needed"));
            notStarted.setWrapText(true);
            node.getChildren().setAll(notStarted);
            return;
        }
        Button pair = new Button(LabelGrabber.INSTANCE.getLabel("remote.v2.pair.button"));
        pair.setOnAction(e -> new RemotePairingDialog(node.getScene().getWindow(), server.getPairing(),
                server.getPort()).show());
        node.getChildren().setAll(pair, deviceList);
        if (!listening) {
            listening = true;
            server.getDevices().addChangeListener(() -> Platform.runLater(() -> refresh(server.getDevices())));
        }
        refresh(server.getDevices());
    }

    private void refresh(DeviceRegistry devices) {
        deviceList.getChildren().clear();
        if (devices.list().isEmpty()) {
            Label none = new Label(LabelGrabber.INSTANCE.getLabel("remote.v2.no.devices"));
            none.setOpacity(0.7);
            deviceList.getChildren().add(none);
            return;
        }
        for (DeviceRegistry.DeviceInfo device : devices.list()) {
            Label name = new Label(device.name());
            name.setStyle("-fx-font-weight: bold;");
            String scope = LabelGrabber.INSTANCE.getLabel(device.scope() == Scope.VIEW
                    ? "remote.v2.scope.view" : "remote.v2.scope.control");
            String lastSeen = LabelGrabber.INSTANCE.getLabel("remote.v2.last.seen")
                    .replace("$1", LAST_SEEN.format(Instant.ofEpochMilli(device.lastSeen())));
            Label details = new Label(device.platform() + " · " + scope + " · " + lastSeen);
            details.setOpacity(0.7);
            VBox text = new VBox(2, name, details);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button revoke = new Button(LabelGrabber.INSTANCE.getLabel("remote.v2.revoke.button"));
            revoke.setOnAction(e -> devices.revoke(device.id()));
            HBox row = new HBox(10, text, spacer, revoke);
            row.setAlignment(Pos.CENTER_LEFT);
            deviceList.getChildren().add(row);
        }
    }
}
