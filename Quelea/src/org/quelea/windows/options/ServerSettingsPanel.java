/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.windows.options;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import org.quelea.server.RCHandler;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.PropertyPanel;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.QueleaApp;

/**
 * The panel that shows the mobile lyrics and remote control options.
 * <p>
 * @author Michael and Ben
 */
public class ServerSettingsPanel extends GridPane implements PropertyPanel {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final CheckBox useMobLyricsCheckBox;
    private final TextField mlPortNumTextField;
    private String mlPrevPortNum;
    private boolean mlPrevChecked;
    private final CheckBox useRemoteControlCheckBox;
    private final TextField rcPortNumTextField;
    private String rcPrevPortNum;
    private boolean rcPrevChecked;
    private final TextField rcPasswordTextField;
    private String rcPrevPassword;
    private BufferedImage qrImage;

    /**
     * Create the server settings panel.
     */
    public ServerSettingsPanel() {
        setVgap(5);
        setHgap(10);
        setPadding(new Insets(5));

        useMobLyricsCheckBox = new CheckBox();
        mlPortNumTextField = new TextField();
        useRemoteControlCheckBox = new CheckBox();
        rcPortNumTextField = new TextField();
        rcPasswordTextField = new TextField();

        setupMobLyrics();
        Label blank = new Label("");
        GridPane.setConstraints(blank, 2, 1);
        getChildren().add(blank);
        setupRemoteControl();
        readProperties();
    }

    private void setupMobLyrics() {
        Label useMobLyricsLabel = new Label(LabelGrabber.INSTANCE.getLabel("use.mobile.lyrics.label") + " ");
        GridPane.setConstraints(useMobLyricsLabel, 1, 1);
        getChildren().add(useMobLyricsLabel);
        GridPane.setConstraints(useMobLyricsCheckBox, 2, 1);
        getChildren().add(useMobLyricsCheckBox);

        Label portNumberLabel = new Label(LabelGrabber.INSTANCE.getLabel("port.number.label") + " ");
        GridPane.setConstraints(portNumberLabel, 1, 2);
        getChildren().add(portNumberLabel);
        mlPortNumTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String t, String t1) {
                checkDifferent();
            }
        });
        mlPortNumTextField.addEventFilter(KeyEvent.KEY_TYPED, (KeyEvent t) -> {
            String text = t.getCharacter();
            char arr[] = text.toCharArray();
            char ch = arr[text.toCharArray().length - 1];
            if (!(ch >= '0' && ch <= '9')) {
                t.consume();
            }
            try {
                String newText = mlPortNumTextField.getText() + ch;
                int num = Integer.parseInt(newText);
                if (num > 65535 || num <= 0) {
                    t.consume();
                }
            } catch (NumberFormatException ex) {
                t.consume();
            }
            checkDifferent();
        });
        mlPortNumTextField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(mlPortNumTextField, Priority.ALWAYS);
        portNumberLabel.setLabelFor(mlPortNumTextField);
        GridPane.setConstraints(mlPortNumTextField, 2, 2);
        getChildren().add(mlPortNumTextField);
        HBox mobBox = new HBox(5);
        mobBox.getChildren().add(new Text(LabelGrabber.INSTANCE.getLabel("navigate.mob.url.label") + ": "));
        Text mobUrlLabel = new Text(getMLURL());
        if (Desktop.isDesktopSupported() && getMLURL().startsWith("http")) {
            mobUrlLabel.setCursor(Cursor.HAND);
            mobUrlLabel.setFill(Color.BLUE);
            mobUrlLabel.setStyle("-fx-underline: true;");
            mobUrlLabel.setOnMouseClicked((MouseEvent t) -> {
                try {
                    Desktop.getDesktop().browse(new URI(getMLURL()));
                } catch (IOException | URISyntaxException ex) {
                    LOGGER.log(Level.WARNING, "Couldn't browse to mobile lyrics URL: {0}", getMLURL());
                }
            });
        }
        mobBox.getChildren().add(mobUrlLabel);
        GridPane.setConstraints(mobBox, 1, 3);
        getChildren().add(mobBox);

        StackPane qrStack = new StackPane();
        qrStack.setAlignment(Pos.CENTER_LEFT);

        if (!getMLURL().contains(LabelGrabber.INSTANCE.getLabel("not.started.label"))) {
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
            GridPane.setConstraints(qrStack, 2, 3);
            getChildren().add(qrStack);
        }
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

    private void setupRemoteControl() {
        Label useRemoteControlLabel = new Label(LabelGrabber.INSTANCE.getLabel("use.remote.control.label") + " ");
        GridPane.setConstraints(useRemoteControlLabel, 1, 4);
        getChildren().add(useRemoteControlLabel);
        GridPane.setConstraints(useRemoteControlCheckBox, 2, 4);
        getChildren().add(useRemoteControlCheckBox);

        Label portNumberLabel = new Label(LabelGrabber.INSTANCE.getLabel("port.number.label") + " ");
        GridPane.setConstraints(portNumberLabel, 1, 5);
        getChildren().add(portNumberLabel);
        rcPortNumTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String t, String t1) {
                checkDifferent();
            }
        });
        rcPortNumTextField.addEventFilter(KeyEvent.KEY_TYPED, (KeyEvent t) -> {
            String text = t.getCharacter();
            char arr[] = text.toCharArray();
            char ch = arr[text.toCharArray().length - 1];
            if (!(ch >= '0' && ch <= '9')) {
                t.consume();
            }
            try {
                String newText = rcPortNumTextField.getText() + ch;
                int num = Integer.parseInt(newText);
                if (num > 65535 || num <= 0) {
                    t.consume();
                }
            } catch (NumberFormatException ex) {
                t.consume();
            }
            checkDifferent();
        });
        rcPortNumTextField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(rcPortNumTextField, Priority.ALWAYS);
        portNumberLabel.setLabelFor(rcPortNumTextField);
        GridPane.setConstraints(rcPortNumTextField, 2, 5);
        getChildren().add(rcPortNumTextField);
        HBox rcBox = new HBox(5);
        rcBox.getChildren().add(new Text(LabelGrabber.INSTANCE.getLabel("navigate.remote.control.label") + ": "));
        Text rcUrlLabel = new Text(getRCURL());
        if (Desktop.isDesktopSupported() && getRCURL().startsWith("http")) {
            rcUrlLabel.setCursor(Cursor.HAND);
            rcUrlLabel.setFill(Color.BLUE);
            rcUrlLabel.setStyle("-fx-underline: true;");
            rcUrlLabel.setOnMouseClicked((MouseEvent t) -> {
                try {
                    Desktop.getDesktop().browse(new URI(getRCURL()));
                } catch (IOException | URISyntaxException ex) {
                    LOGGER.log(Level.WARNING, "Couldn't browse to remote control URL: {0}", getRCURL());
                }
            });
        }
        rcBox.getChildren().add(rcUrlLabel);
        GridPane.setConstraints(rcBox, 1, 6);
        getChildren().add(rcBox);

        Label passwordLabel = new Label(LabelGrabber.INSTANCE.getLabel("remote.control.password") + " ");
        GridPane.setConstraints(passwordLabel, 1, 7);
        getChildren().add(passwordLabel);
        rcPasswordTextField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(rcPasswordTextField, Priority.ALWAYS);
        passwordLabel.setLabelFor(rcPasswordTextField);
        GridPane.setConstraints(rcPasswordTextField, 2, 7);
        getChildren().add(rcPasswordTextField);
    }

    private String urlMLCache;
    private String urlRCCache;

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

    public void resetChanged() {
        mlPrevChecked = useMobLyricsCheckBox.isSelected();
        mlPrevPortNum = mlPortNumTextField.getText();
        rcPrevChecked = useRemoteControlCheckBox.isSelected();
        rcPrevPortNum = rcPortNumTextField.getText();
        rcPrevPassword = rcPasswordTextField.getText();
    }

    /**
     * Determine if the user has changed any settings since resetChanged() was
     * called.
     *
     * @return true if the user has changed any settings since resetChanged()
     * was called, false otherwise.
     */
    public boolean hasChanged() {
        return mlPrevChecked != useMobLyricsCheckBox.isSelected() || !(mlPrevPortNum.equals(mlPortNumTextField.getText())) || rcPrevChecked != useRemoteControlCheckBox.isSelected() || !(rcPrevPortNum.equals(rcPortNumTextField.getText()));
    }

    // If they have the same port, uncheck the remote control checkbox
    private void checkDifferent() {
        if (mlPortNumTextField.getText().equals(rcPortNumTextField.getText())) {
            useRemoteControlCheckBox.setSelected(false);
        }
    }

    /**
     * Set the properties based on the values in this frame.
     */
    @Override
    public void setProperties() {
        QueleaProperties.get().setUseMobLyrics(useMobLyricsCheckBox.isSelected());
        QueleaProperties.get().setUseRemoteControl(useRemoteControlCheckBox.isSelected());
        if (mlPortNumTextField.getText().trim().isEmpty()) {
            mlPortNumTextField.setText(Integer.toString(QueleaProperties.get().getMobLyricsPort()));
        } else {
            QueleaProperties.get().setMobLyricsPort(Integer.parseInt(mlPortNumTextField.getText()));
        }
        if (rcPortNumTextField.getText().trim().isEmpty()) {
            rcPortNumTextField.setText(Integer.toString(QueleaProperties.get().getRemoteControlPort()));
        } else {
            QueleaProperties.get().setRemoteControlPort(Integer.parseInt(rcPortNumTextField.getText()));
        }
        if (rcPasswordTextField.getText().trim().isEmpty()) {
            rcPasswordTextField.setText(QueleaProperties.get().getRemoteControlPassword());
        } else {
            QueleaProperties.get().setRemoteControlPassword(rcPasswordTextField.getText());
            if (!rcPasswordTextField.getText().equals(rcPrevPassword)) {
                RCHandler.logAllOut();
            }
        }
    }

    /**
     * Read the properties into this frame.
     */
    @Override
    public final void readProperties() {
        useMobLyricsCheckBox.setSelected(QueleaProperties.get().getUseMobLyrics());
        mlPortNumTextField.setText(Integer.toString(QueleaProperties.get().getMobLyricsPort()));
        useRemoteControlCheckBox.setSelected(QueleaProperties.get().getUseRemoteControl());
        rcPortNumTextField.setText(Integer.toString(QueleaProperties.get().getRemoteControlPort()));
        rcPasswordTextField.setText(QueleaProperties.get().getRemoteControlPassword());
    }

}
