/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.windows.options;

import java.awt.Desktop;
import java.io.IOException;
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.PropertyPanel;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.QueleaApp;

/**
 * The panel that shows the mobile lyrics options.
 * <p>
 * @author Michael
 */
public class OptionsMobLyricsPanel extends GridPane implements PropertyPanel {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private CheckBox useMobLyricsCheckBox;
    private final TextField portNumTextField;
    private boolean changeMade;

    /**
     * Create the mobile lyrics panel.
     */
    public OptionsMobLyricsPanel() {
        setVgap(5);
        setPadding(new Insets(5));

        Label useMobLydicsLabel = new Label(LabelGrabber.INSTANCE.getLabel("use.mobile.lyrics.label") + " ");
        GridPane.setConstraints(useMobLydicsLabel, 1, 1);
        getChildren().add(useMobLydicsLabel);
        useMobLyricsCheckBox = new CheckBox();
        useMobLyricsCheckBox.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                changeMade = true;
            }
        });
        GridPane.setConstraints(useMobLyricsCheckBox, 2, 1);
        getChildren().add(useMobLyricsCheckBox);

        Label portNumberLabel = new Label(LabelGrabber.INSTANCE.getLabel("port.number.label") + " ");
        GridPane.setConstraints(portNumberLabel, 1, 2);
        getChildren().add(portNumberLabel);
        portNumTextField = new TextField();
        portNumTextField.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                changeMade = true;
            }
        });
        portNumTextField.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                String text = t.getCharacter();
                char arr[] = text.toCharArray();
                char ch = arr[text.toCharArray().length - 1];
                if(!(ch >= '0' && ch <= '9')) {
                    t.consume();
                }
                try {
                    String newText = portNumTextField.getText() + ch;
                    int num = Integer.parseInt(newText);
                    if(num > 65535 || num <= 0) {
                        t.consume();
                    }
                }
                catch(NumberFormatException ex) {
                    t.consume();
                }
            }
        });
        portNumTextField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(portNumTextField, Priority.ALWAYS);
        portNumberLabel.setLabelFor(portNumTextField);
        GridPane.setConstraints(portNumTextField, 2, 2);
        getChildren().add(portNumTextField);
        HBox mobBox = new HBox(5);
        mobBox.getChildren().add(new Text(LabelGrabber.INSTANCE.getLabel("navigate.mob.url.label") + ": "));
        Text mobUrlLabel = new Text(getURL());
        if(Desktop.isDesktopSupported() && getURL().startsWith("http")) {
            mobUrlLabel.setCursor(Cursor.HAND);
            mobUrlLabel.setFill(Color.BLUE);
            mobUrlLabel.setStyle("-fx-underline: true;");
            mobUrlLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent t) {
                    try {
                        Desktop.getDesktop().browse(new URI(getURL()));
                    }
                    catch(IOException | URISyntaxException ex) {
                        LOGGER.log(Level.WARNING, "Couldn't browse to mobile lyrics URL: {0}", getURL());
                    }
                }
            });
        }
        mobBox.getChildren().add(mobUrlLabel);
        GridPane.setConstraints(mobBox, 1, 3);
        getChildren().add(mobBox);

        readProperties();
    }

    private String urlCache;

    private String getURL() {
        if(urlCache == null) {
            if(QueleaProperties.get().getUseMobLyrics() && QueleaApp.get().getMobileLyricsServer() != null) {
                String ip = getIP();
                if(ip!=null) {
                    StringBuilder ret = new StringBuilder("http://");
                    ret.append(ip);
                    int port = QueleaProperties.get().getMobLyricsPort();
                    if(port != 80) {
                        ret.append(":");
                        ret.append(port);
                    }
                    urlCache = ret.toString();
                }
                else {
                    urlCache = "[Not started]";
                }
            }
            else {
                urlCache = "[Not started]";
            }
        }
        return urlCache;
    }

    private static String getIP() {
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        }
        catch(SocketException ex) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            }
            catch(UnknownHostException ex2) {
                return null;
            }
        }
        while(interfaces.hasMoreElements()) {
            NetworkInterface current = interfaces.nextElement();
            try {
                if(!current.isUp() || current.isLoopback() || current.isVirtual() || current.getDisplayName().toLowerCase().contains("virtual")) {
                    continue;
                }
            }
            catch(SocketException ex) {
                continue;
            }
            Enumeration<InetAddress> addresses = current.getInetAddresses();
            while(addresses.hasMoreElements()) {
                InetAddress current_addr = addresses.nextElement();
                if(current_addr.isLoopbackAddress()) {
                    continue;
                }
                return current_addr.getHostAddress();
            }
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch(UnknownHostException ex) {
            return null;
        }
    }
    
    public void resetChanged() {
        changeMade = false;
    }

    /**
     * Determine if the user has changed any settings since resetChanged() was
     * called.
     */
    public boolean hasChanged() {
        return changeMade;
    }

    /**
     * Set the properties based on the values in this frame.
     */
    @Override
    public void setProperties() {
        QueleaProperties.get().setUseMobLyrics(useMobLyricsCheckBox.isSelected());
        QueleaProperties.get().setMobLyricsPort(Integer.parseInt(portNumTextField.getText()));
    }

    /**
     * Read the properties into this frame.
     */
    @Override
    public final void readProperties() {
        useMobLyricsCheckBox.setSelected(QueleaProperties.get().getUseMobLyrics());
        portNumTextField.setText(Integer.toString(QueleaProperties.get().getMobLyricsPort()));
    }

}
