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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.quelea.server.v2.DeviceRegistry;
import org.quelea.server.v2.LanAddress;
import org.quelea.server.v2.PairingService;
import org.quelea.server.v2.PairingUri;
import org.quelea.server.v2.Scope;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.style.StyleManager;

/**
 * Shows a pairing code and its QR code until a device pairs, the code
 * expires or the dialog is closed. Closing the dialog closes pairing.
 */
public class RemotePairingDialog extends Stage {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final int QR_SIZE = 220;

    private final PairingService pairing;
    private final int port;
    private final String host;
    private final ImageView qrView = new ImageView();
    private final Label codeLabel = new Label();
    private final Label countdownLabel = new Label();
    private final Label doneLabel = new Label();
    private final CheckBox viewOnly = new CheckBox(LabelGrabber.INSTANCE.getLabel("remote.v2.pair.view.only"));
    private final Timeline countdown;
    private final Consumer<DeviceRegistry.Paired> pairedListener;
    private PairingService.OpenCode code;
    private boolean showing;

    /**
     * @param owner the window to show the dialog over.
     * @param pairing the pairing service.
     * @param port the v2 port.
     */
    public RemotePairingDialog(Window owner, PairingService pairing, int port) {
        this.pairing = pairing;
        this.port = port;
        this.host = LanAddress.find().map(InetAddress::getHostAddress).orElse(null);
        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        setTitle(LabelGrabber.INSTANCE.getLabel("remote.v2.pair.title"));
        setResizable(false);

        qrView.setFitWidth(QR_SIZE);
        qrView.setFitHeight(QR_SIZE);
        codeLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 34));
        codeLabel.setStyle("-fx-letter-spacing: 4;");
        doneLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
        doneLabel.setVisible(false);
        doneLabel.setManaged(false);

        Label instructions = new Label(LabelGrabber.INSTANCE.getLabel("remote.v2.pair.instructions"));
        instructions.setWrapText(true);
        Label address = new Label(LabelGrabber.INSTANCE.getLabel("remote.v2.pair.address")
                .replace("$1", (host == null ? "?" : host) + ":" + port));

        Button newCode = new Button(LabelGrabber.INSTANCE.getLabel("remote.v2.pair.new.code"));
        newCode.setOnAction(e -> openCode());
        viewOnly.setOnAction(e -> openCode());
        Button close = new Button(LabelGrabber.INSTANCE.getLabel("close.button"));
        close.setDefaultButton(true);
        close.setOnAction(e -> close());
        HBox buttons = new HBox(10, newCode, close);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12, instructions, qrView, codeLabel, countdownLabel, address, viewOnly, doneLabel, buttons);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setPrefWidth(340);
        Scene scene = new Scene(root);
        StyleManager.apply(scene);
        setScene(scene);

        countdown = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> tick()));
        countdown.setCycleCount(Animation.INDEFINITE);

        pairedListener = paired -> Platform.runLater(() -> devicePaired(paired));
        pairing.addPairedListener(pairedListener);
        setOnShown(e -> showing = true);
        setOnHidden(e -> {
            showing = false;
            countdown.stop();
            pairing.close();
            pairing.removePairedListener(pairedListener);
        });
        openCode();
    }

    private void openCode() {
        code = pairing.open(viewOnly.isSelected() ? Scope.VIEW : Scope.CONTROL);
        codeLabel.setText(code.code());
        codeLabel.setOpacity(1);
        qrView.setOpacity(1);
        qrView.setImage(qrImage(PairingUri.build(host == null ? "" : host, port, code.code(), hostName())));
        doneLabel.setVisible(false);
        doneLabel.setManaged(false);
        tick();
        countdown.playFromStart();
    }

    private void tick() {
        long seconds = Math.max(0, Duration.between(Instant.now(), code.expires()).toSeconds());
        if (seconds == 0 || !pairing.isOpen()) {
            countdown.stop();
            countdownLabel.setText(LabelGrabber.INSTANCE.getLabel("remote.v2.pair.expired"));
            codeLabel.setOpacity(0.3);
            qrView.setOpacity(0.15);
        } else {
            countdownLabel.setText(LabelGrabber.INSTANCE.getLabel("remote.v2.pair.expires")
                    .replace("$1", Long.toString(seconds)));
        }
    }

    private void devicePaired(DeviceRegistry.Paired paired) {
        if (!showing) {
            return;
        }
        countdown.stop();
        codeLabel.setOpacity(0.3);
        qrView.setOpacity(0.15);
        countdownLabel.setText("");
        doneLabel.setText(LabelGrabber.INSTANCE.getLabel("remote.v2.pair.done").replace("$1", paired.name()));
        doneLabel.setVisible(true);
        doneLabel.setManaged(true);
    }

    private static String hostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return null;
        }
    }

    private static javafx.scene.image.Image qrImage(String text) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, QR_SIZE * 2, QR_SIZE * 2);
            return SwingFXUtils.toFXImage(MatrixToImageWriter.toBufferedImage(matrix), null);
        } catch (WriterException ex) {
            LOGGER.log(Level.WARNING, "Could not create pairing QR code", ex);
            return null;
        }
    }
}
