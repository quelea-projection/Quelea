package org.quelea.windows.options.customprefs;

import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.QueleaApp;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.utils.DesktopApi;

public class MobileServerPreference extends SimpleControl<StringField, StackPane> {

    /**
     * - The fieldLabel is the container that displays the label property of
     * the field.
     * - The editableField allows users to modify the field's value.
     * - The readOnlyLabel displays the field's value if it is not editable.
     */
    private TextField editableField;
    private BufferedImage qrImage;
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private boolean isLyrics;
    private String urlMLCache;
    private String urlRCCache;

    public MobileServerPreference(boolean isLyrics) {
        this.isLyrics = isLyrics;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeParts() {
        super.initializeParts();

        node = new StackPane();
        node.getStyleClass().add("simple-text-control");

        editableField = new TextField(field.getValue());

        editableField.setPromptText(field.placeholderProperty().getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void layoutParts() {
        StackPane qrStack = new StackPane();
        qrStack.setAlignment(Pos.CENTER_LEFT);

        if (isLyrics && !getMLURL().contains(LabelGrabber.INSTANCE.getLabel("not.started.label"))) {
            ImageView qrView = new ImageView(getQRImage());
            StackPane.setAlignment(qrView, Pos.CENTER_LEFT);
            qrView.setFitHeight(100);
            qrView.setFitWidth(100);
            qrStack.getChildren().add(qrView);
            Button saveButton = new Button(LabelGrabber.INSTANCE.getLabel("save.qr.code.text"));
            StackPane.setAlignment(saveButton, Pos.CENTER_LEFT);
            saveButton.setOnAction((event) -> {
                FileChooser fileChooser = new FileChooser();
                if (QueleaProperties.get().getLastDirectory() != null) {
                    fileChooser.setInitialDirectory(QueleaProperties.get().getLastDirectory());
                }
                fileChooser.getExtensionFilters().add(FileFilters.PNG);
                fileChooser.setTitle(LabelGrabber.INSTANCE.getLabel("save.qr.code.text"));
                File file = fileChooser.showSaveDialog(QueleaApp.get().getMainWindow());
                if (file != null) {
                    QueleaProperties.get().setLastDirectory(file.getParentFile());
                    try {
                        ImageIO.write(qrImage, "png", file);
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Error saving QR file", ex);
                    }
                }
            });
            saveButton.setOpacity(0);
            qrStack.setOnMouseEntered((event) -> {
                saveButton.setOpacity(0.8);
            });
            qrStack.setOnMouseExited((event) -> {
                saveButton.setOpacity(0);
            });
            qrStack.getChildren().add(saveButton);
        }

        String url = isLyrics ? getMLURL() : getRCURL();
        Text mobUrlLabel = new Text(url);
        if (Desktop.isDesktopSupported() && url.startsWith("http")) {
            mobUrlLabel.setCursor(Cursor.HAND);
            mobUrlLabel.setFill(Color.BLUE);
            mobUrlLabel.setStyle("-fx-underline: true;");
            mobUrlLabel.setOnMouseClicked((MouseEvent t) -> {
                DesktopApi.browse(url);
            });
        }

        HBox hBox = new HBox();
        hBox.setSpacing(10);
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER_LEFT);
        vBox.setSpacing(10);
        vBox.getChildren().addAll(editableField, mobUrlLabel);
        hBox.getChildren().addAll(vBox, qrStack);
        HBox.setHgrow(vBox, Priority.ALWAYS);
        hBox.setAlignment(Pos.CENTER_LEFT);

        node.getChildren().addAll(hBox);

        node.setAlignment(Pos.CENTER_LEFT);
    }

    private Image getQRImage() {
        if (qrImage == null) {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            int qrWidth = 500;
            int qrHeight = 500;
            BitMatrix byteMatrix = null;
            try {
                byteMatrix = qrCodeWriter.encode(getMLURL(), BarcodeFormat.QR_CODE, qrWidth, qrHeight);
            } catch (WriterException ex) {
                LOGGER.log(Level.WARNING, "Error writing QR code", ex);
            }
            qrImage = MatrixToImageWriter.toBufferedImage(byteMatrix);
        }
        WritableImage fxImg = new WritableImage(500, 500);
        SwingFXUtils.toFXImage(qrImage, fxImg);
        return fxImg;
    }

    public String getMLURL() {
        if (urlMLCache == null) {
            if (QueleaProperties.get().getUseMobLyrics() && QueleaApp.get().getMobileLyricsServer() != null) {
                String ip = getIP();
                if (ip != null) {
                    StringBuilder ret = new StringBuilder("http://");
                    ret.append(ip);
                    int port = QueleaProperties.get().getMobLyricsPort();
                    if (port != 80) {
                        ret.append(":");
                        ret.append(port);
                    }
                    urlMLCache = ret.toString();
                } else {
                    urlMLCache = "[" + LabelGrabber.INSTANCE.getLabel("not.started.label") + "]";
                }
            } else {
                urlMLCache = "[" + LabelGrabber.INSTANCE.getLabel("not.started.label") + "]";
            }
        }
        return urlMLCache;
    }

    public String getRCURL() {
        if (urlRCCache == null) {
            if (QueleaProperties.get().getUseRemoteControl() && QueleaApp.get().getRemoteControlServer() != null) {
                String ip = getIP();
                if (ip != null) {
                    StringBuilder ret = new StringBuilder("http://");
                    ret.append(ip);
                    int port = QueleaProperties.get().getRemoteControlPort();
                    if (port != 80) {
                        ret.append(":");
                        ret.append(port);
                    }
                    urlRCCache = ret.toString();
                } else {
                    urlRCCache = "[" + LabelGrabber.INSTANCE.getLabel("not.started.label") + "]";
                }
            } else {
                urlRCCache = "[" + LabelGrabber.INSTANCE.getLabel("not.started.label") + "]";
            }
        }
        return urlRCCache;
    }

    private static String getIP() {
        String v6address = null;
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ex) {
            LOGGER.log(Level.WARNING, "Socket exception getting ip", ex);
            try {
                if (InetAddress.getLocalHost() instanceof Inet6Address) {
                    LOGGER.log(Level.WARNING, "Socket exception, but using v6 address as fallback: {0}", InetAddress.getLocalHost().getHostAddress());
                    v6address = InetAddress.getLocalHost().getHostAddress();
                } else {
                    LOGGER.log(Level.WARNING, "Socket exception, but found v4 address: {0}", InetAddress.getLocalHost().getHostAddress());
                    return InetAddress.getLocalHost().getHostAddress();
                }
            } catch (UnknownHostException ex2) {
                return null;
            }
        }
        while (interfaces != null && interfaces.hasMoreElements()) {
            NetworkInterface current = interfaces.nextElement();
            try {
                if (!current.isUp() || current.isLoopback() || current.isVirtual() || current.getDisplayName().toLowerCase().contains("virtual")) {
                    continue;
                }
            } catch (SocketException ex) {
                continue;
            }
            Enumeration<InetAddress> addresses = current.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress current_addr = addresses.nextElement();
                if (current_addr.isLoopbackAddress()) {
                    LOGGER.log(Level.INFO, "Ignoring loopback address");
                    continue;
                }
                if (current_addr instanceof Inet6Address && v6address == null) {
                    LOGGER.log(Level.INFO, "Storing v6 address, no v4 found yet: {0}", v6address);
                    v6address = current_addr.getHostAddress();
                } else if (current_addr instanceof Inet4Address) {
                    LOGGER.log(Level.INFO, "Found v4: {0}", current_addr.getHostAddress());
                    return current_addr.getHostAddress();
                }
            }
        }
        //Fallback
        try {
            String fallback = InetAddress.getLocalHost().getHostAddress();
            LOGGER.log(Level.INFO, "Using fallback: {0}", fallback);
            return fallback;
        } catch (UnknownHostException ex) {
            LOGGER.log(Level.WARNING, "Unknwon host ip", ex);
        }
        //Worst fallback, return ipv6 address
        LOGGER.log(Level.INFO, "Falling back to v6 address: {0}", v6address);
        return v6address;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setupBindings() {
        super.setupBindings();

        editableField.visibleProperty().bind(Bindings.and(field.editableProperty(),
                field.multilineProperty().not()));
        editableField.textProperty().bindBidirectional(field.userInputProperty());
        editableField.promptTextProperty().bind(field.placeholderProperty());
        editableField.managedProperty().bind(editableField.visibleProperty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupValueChangedListeners() {
        super.setupValueChangedListeners();

        field.multilineProperty().addListener((observable, oldValue, newValue) -> {
            node.setPrefHeight(newValue ? 80 : 0);
        });

        field.errorMessagesProperty().addListener((observable, oldValue, newValue) ->
                toggleTooltip(editableField)
        );

        editableField.focusedProperty().addListener(
                (observable, oldValue, newValue) -> toggleTooltip(editableField)
        );
    }

}

