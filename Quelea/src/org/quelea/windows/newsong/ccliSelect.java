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
package org.quelea.windows.newsong;

import java.security.GeneralSecurityException;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.javafx.dialog.Dialog;

import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;

/**
 * A panel that is essentially a web browser opens CCLI SongSelect and will
 * return a string upon finding proper lyrics.
 *
 * @author Greg Needs attention--- works, but is not too pretty (I'm not sure
 * what it will do outside of the US)
 */
public class CCLISelect extends Stage {

    private String title = "";
    private String text = "";
    private boolean canceled = true;
    private boolean closeUponOpening = false;
    private WebView browser = new WebView();
    private WebEngine webEngine = browser.getEngine();
    private Timer timer = new Timer();
    private Scene scene;
    private BorderPane componentLayout;
    private BorderPane buttonLayout;
    private HBox buttonLayout1;
    private HBox buttonLayout2;
    private Button cancelButton;
    private Button importButton;
    private Button navBack;
    private Button navHome;
    private String pageHTML = "";
    private static final String CCLI_ADDRESS = "https://us.songselect.com";

    /**
     * Creates a new CCLI Select panel
     */
    public CCLISelect() {

        componentLayout = new BorderPane();
        componentLayout.setPadding(new Insets(15, 15, 15, 15));
        buttonLayout = new BorderPane();
        buttonLayout1 = new HBox();
        buttonLayout2 = new HBox();

        importButton = new Button(LabelGrabber.INSTANCE.getLabel("import.button"), new ImageView(new Image("file:icons/tick.png")));
        final Stage s = this;
        importButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                importString();
                canceled = false;
                s.close();
            }
        });
        cancelButton = new Button(LabelGrabber.INSTANCE.getLabel("cancel.text"), new ImageView(new Image("file:icons/cross.png")));
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                canceled = true;
                s.close();
            }
        });

        navBack = new Button();
        navBack.setText(LabelGrabber.INSTANCE.getLabel("ccli.navBack"));
        navBack.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                webEngine.getHistory().go(-1);
            }
        });

        navHome = new Button();
        navHome.setText(LabelGrabber.INSTANCE.getLabel("ccli.navHome"));
        navHome.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                webEngine.load(CCLI_ADDRESS);
            }
        });

        buttonLayout1.getChildren().add(importButton);
        buttonLayout1.getChildren().add(cancelButton);

        buttonLayout2.getChildren().add(navBack);
        buttonLayout2.getChildren().add(navHome);
        buttonLayout1.setAlignment(Pos.CENTER_LEFT);
        buttonLayout1.setSpacing(6);
        buttonLayout1.setPadding(new Insets(9, 9, 9, 9));
        buttonLayout2.setAlignment(Pos.CENTER_RIGHT);
        buttonLayout2.setSpacing(6);
        buttonLayout2.setPadding(new Insets(9, 9, 9, 9));
        buttonLayout.setRight(buttonLayout2);
        buttonLayout.setLeft(buttonLayout1);
        componentLayout.setBottom(buttonLayout);
        final Browser browserNode = new Browser();
        componentLayout.setCenter(browserNode);
        this.initStyle(StageStyle.UTILITY);
        scene = new Scene(componentLayout, 1200, 700);
        this.setScene(scene);

        browser.requestFocus();
        //determine if page loads under normal security
        browserNode.loadWebpage(false);
        pageHTML = (String) webEngine.executeScript("document.documentElement.outerHTML");
        //see if HTML is empty
        if (pageHTML.equals("<html><head></head><body></body></html>")) {
            //ask if want to continue, and continue or cancel the dialog
            Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("ccli.warning"), LabelGrabber.INSTANCE.getLabel("ccli.loadAnyways.message"))
                    .addYesButton(new EventHandler() {

                        @Override
                        public void handle(Event t) {
                            browserNode.loadWebpage(true);
                        }
                    })
                    .addNoButton(new EventHandler() {

                        @Override
                        public void handle(Event t) {
                            closeUponOpening = true;
                        }
                    })
                    .build()
                    .showAndWait();

        }

        //timer to check web page to see if it is valid
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        if (closeUponOpening) {
                            canceled = true;
                            s.close();
                        }
                    }
                });

                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        pageHTML = (String) webEngine.executeScript("document.documentElement.outerHTML");
                    }
                });

                if (pageHTML.contains("CCLI License #")) {

                    importButton.setDisable(false);
                } else {

                    importButton.setDisable(true);
                }
            }
        }, 0, 500l);

    }

    /**
     * uses JSoup to parse html into a string, then replace all html tags with
     * the proper escape sequences, and sets the text as one massive string
     *
     * @return The imported String
     */
    private String importString() {
        timer.cancel();

        Document doc = Jsoup.parse(pageHTML);
        Elements p = doc.select("p");
        Elements title = doc.select("title");
        Elements h3 = doc.select("h3");
        this.title = title.text();
        String all = "";
        int iterator = 0;
        for (Element x : p) {

            if (x.toString().contains("p class")) {
            } else {
                iterator++;
                if (!(h3.get(iterator).toString().equals("<h3>Please print this page using the print button</h3>"))) {
                    all = all + "\n" + h3.get(iterator).toString();
                }
                all = all + x.toString() + "<br />";
            }

        }
        String replace = all.replace("<br />", "\n");
        replace = replace.replace("<p>", "\n");
        replace = replace.replace("</p>", "");
        replace = replace.replace("<h3>", "");
        replace = replace.replace("</h3>", "");
        
        
        this.text = replace;
        
        return this.text;
    }

    /**
     * Get the Song Text.
     *
     * @return Song Text as imported from HTML. Will be an empty string until
     * import has been called
     */
    public String getSongText() {
        return text;
    }

    /**
     * Get the Song Title
     *
     * @return Song Title as imported from HTML. Will be an empty string until
     * import has been called
     */
    public String getSongTitle() {
        return title;
    }

    /**
     * Get whether the dialog was canceled before it was closed
     *
     * @return Will return false if the "import" button was pressed, otherwise
     * will return true.
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Private class that is only the web browser part of the song
     * selection...used for songselect
     */
    private class Browser extends Region {

        /**
         * Creates a new Browser, which implements a JavaFX WebView.
         */
        public Browser() {
            browser = new WebView();
            webEngine = browser.getEngine();
            //apply the styles
            getStyleClass().add("browser");

            //add the web view to the scene
            getChildren().add(browser);

        }

        /**
         * Loads the SongSelect website.
         *
         * @param notSecure determines whether to load regardless of potential
         * certificate problems
         */
        public void loadWebpage(boolean notSecure) {
            if (notSecure) {
                //NOT THE BEST WAY TO DO THIS>>> INSECURE   
                //song select has some security certificate problems, so:
                // Create a trust manager that does not validate certificate chains
                TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
                };

                // Install the all-trusting trust manager
                try {
                    SSLContext sc = SSLContext.getInstance("SSL");
                    sc.init(null, trustAllCerts, new java.security.SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                } catch (GeneralSecurityException e) {
                }
            }
            // load the web page
            webEngine.load(CCLI_ADDRESS);
        }

        /**
         * Lays out the WebView with proper width and height
         */
        @Override
        protected void layoutChildren() {
            double w = getWidth();
            double h = getHeight();
            layoutInArea(browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
        }

        /**
         * Computes preferred height. Currently doesn't do anything but return
         * its passed value
         *
         * @param height The preferred height of the browser
         * @return The preferred height of the browser
         */
        @Override
        protected double computePrefWidth(double height) {
            return height;
        }

        /**
         * Computes preferred width. Currently doesn't do anything but return
         * its passed value
         *
         * @param width The preferred width of the browser
         * @return The preferred width of the browser
         */
        @Override
        protected double computePrefHeight(double width) {
            return width;
        }
    }

}
