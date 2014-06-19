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

import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.quelea.services.languages.LabelGrabber;

/**
 *
 * @author Greg
 * Needs attention--- works, but is not too pretty (I'm not sure what it will do outside of the US)
 */
public class ccliSelect extends Stage {

    private String title = "";
    private String text = "";

    private boolean canceled = true;

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
    private String ccliAddress = "https://us.songselect.com";

    public ccliSelect() {
//create gui
        componentLayout = new BorderPane();
        componentLayout.setPadding(new Insets(15,15,15,15));
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
                s.hide();
            }
        });
        cancelButton = new Button(LabelGrabber.INSTANCE.getLabel("cancel.text"), new ImageView(new Image("file:icons/cross.png")));
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                canceled = true;
                s.hide();
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
                webEngine.load(ccliAddress);
            }
        });
        
        
        buttonLayout1.getChildren().add(importButton);
        buttonLayout1.getChildren().add(cancelButton);
        
        buttonLayout2.getChildren().add(navBack);
        buttonLayout2.getChildren().add(navHome);
        buttonLayout1.setAlignment(Pos.CENTER_LEFT);
        buttonLayout1.setSpacing(6);
        buttonLayout1.setPadding(new Insets(9,9,9,9));
         buttonLayout2.setAlignment(Pos.CENTER_RIGHT);
        buttonLayout2.setSpacing(6);
        buttonLayout2.setPadding(new Insets(9,9,9,9));
        buttonLayout.setRight(buttonLayout2);
        buttonLayout.setLeft(buttonLayout1);
        componentLayout.setBottom(buttonLayout);
        componentLayout.setCenter(new Browser());
        this.initStyle(StageStyle.UTILITY);
        scene = new Scene(componentLayout, 1200, 700);
        this.setScene(scene);

        browser.requestFocus();
        //timer to check web page to see if it is valid
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

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
    
//uses JSoup to parse html into a string, then replace all html tags with the proper escape sequences, and sets the text as one massive string
    private String importString() {
        timer.cancel();
        
    

        Document doc = Jsoup.parse(pageHTML);
        Elements p = doc.select("p");
        Elements title = doc.select("title");
        this.title = title.text();
        String all = "";
        for (Element x : p) {
            if (x.toString().contains("p class")) {
            } else {
                all = all + x.toString() + "<br />";
            }

        }
        String replace = all.replace("<br />", "\n");
        String replace2 = replace.replace("<p>", "\n");
        String replace3 = replace2.replace("</p>", "");

        this.text = replace3;
        return this.text;
    }

    private static String trim(String s, int width) {
        if (s.length() > width) {
            return s.substring(0, width - 1) + ".";
        } else {
            return s;
        }
    }

    public String getSongText() {
        return text;
    }

    public String getSongTitle() {
        return title;
    }

    public boolean isCanceled() {
        return canceled;
    }

    //web browser class.. for song select
    private class Browser extends Region {

        public Browser() {
            browser = new WebView();
            webEngine = browser.getEngine();
            //apply the styles
            getStyleClass().add("browser");
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

            // load the web page
            webEngine.load(ccliAddress);
            //add the web view to the scene
            getChildren().add(browser);

        }

        private Node createSpacer() {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            return spacer;
        }

        @Override
        protected void layoutChildren() {
            double w = getWidth();
            double h = getHeight();
            layoutInArea(browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
        }

        @Override
        protected double computePrefWidth(double height) {
            return height;
        }

        @Override
        protected double computePrefHeight(double width) {
            return width;
        }
    }

}
