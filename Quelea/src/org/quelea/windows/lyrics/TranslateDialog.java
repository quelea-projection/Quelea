/*
 * This file is part of Quelea, free projection software for churches.
 * 
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.lyrics;

import com.memetix.mst.detect.Detect;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.javafx.dialog.Dialog;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LanguageNameMap;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;

/**
 * The translation dialog that manages all the translations - translations can
 * be added or removed from here.
 *
 * @author Michael
 */
public class TranslateDialog extends Stage {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final SplitPane splitPane;
    private final TabPane tabPane;
    private final TextArea defaultLyricsArea;
    private final Button addTranslationButton;
    private final Button closeButton;

    /**
     * Create the translation dialog.
     */
    public TranslateDialog() {
        initModality(Modality.APPLICATION_MODAL);
        Utils.addIconsToStage(this);
        setTitle(LabelGrabber.INSTANCE.getLabel("translate.dialog.title"));
        splitPane = new SplitPane();
        defaultLyricsArea = new TextArea();
        defaultLyricsArea.setEditable(false);
        StackPane translationPane = new StackPane();
        tabPane = new TabPane();
        translationPane.getChildren().add(tabPane);
        addTranslationButton = new Button("", new ImageView(new Image("file:icons/newstar.png", 16, 16, false, true)));
        Utils.setToolbarButtonStyle(addTranslationButton);
        addTranslationButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                final String name = NewTranslationDialog.getTranslationName(getExistingNames());
                if (name != null) {
                    final TranslateTab tab = new TranslateTab(name, "");
                    tabPane.getTabs().add(tab);
                    new Thread() {
                        @Override
                        public void run() {
                            final String lyrics = getTranslatedLyrics(name);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (isShowing() && lyrics != null && !lyrics.isEmpty()) {
                                        if (tab.getLyrics().length() < 10) {
                                            tab.setLyrics(lyrics);
                                        } else {
                                            Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("overwrite.lyrics.title"), LabelGrabber.INSTANCE.getLabel("overwrite.lyrics.text"))
                                                    .addYesButton(new EventHandler<ActionEvent>() {

                                                        @Override
                                                        public void handle(ActionEvent t) {
                                                            tab.setLyrics(lyrics);
                                                        }
                                                    }).addNoButton(new EventHandler<ActionEvent>() {

                                                        @Override
                                                        public void handle(ActionEvent t) {
                                                            //Nothing needed here.
                                                        }
                                                    }).build().showAndWait();
                                        }
                                    }
                                }
                            });
                        }
                    }.start();
                }
            }
        });
        addTranslationButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("add.translation.button")));
        StackPane.setAlignment(addTranslationButton, Pos.TOP_RIGHT);
        translationPane.getChildren().add(addTranslationButton);
        StackPane.setMargin(addTranslationButton, new Insets(5));
        BorderPane leftArea = new BorderPane();
        StackPane topRegion = new StackPane();
        Text defaultTranslationLabel = new Text(LabelGrabber.INSTANCE.getLabel("default.translation.label"));
        defaultTranslationLabel.setFill(Color.WHITE);
        StackPane.setAlignment(defaultTranslationLabel, Pos.CENTER_LEFT);
        StackPane.setMargin(defaultTranslationLabel, new Insets(5));
        topRegion.getChildren().add(defaultTranslationLabel);
        topRegion.setPrefHeight(31);
        topRegion.setStyle("-fx-background-color:rgb(166,166,166);"); //Match the tabpane header background.
        topRegion.setMaxWidth(Double.MAX_VALUE);
        leftArea.setTop(topRegion);
        leftArea.setCenter(defaultLyricsArea);
        splitPane.getItems().add(leftArea);
        splitPane.getItems().add(translationPane);

        BorderPane root = new BorderPane();
        root.setCenter(splitPane);

        StackPane bottomPane = new StackPane();
        closeButton = new Button(LabelGrabber.INSTANCE.getLabel("close.button"));
        StackPane.setMargin(closeButton, new Insets(10));
        closeButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                hide();
            }
        });
        bottomPane.getChildren().add(closeButton);
        root.setBottom(bottomPane);
        setScene(new Scene(root));
        setWidth(800);
        setHeight(600);
    }

    /**
     * Set (update) the default lyrics on this dialog.
     *
     * @param lyrics the lyrics to show in the default panel.
     */
    public void setDefaultLyrics(String lyrics) {
        defaultLyricsArea.setText(lyrics);
    }

    /**
     * Clear any lyrics from this dialog (default and translations.)
     */
    public void clearSong() {
        tabPane.getTabs().clear();
        defaultLyricsArea.clear();
    }

    /**
     * Set the song to display in this panel - updates the translations and the
     * default lyrics view.
     *
     * @param song the song to display.
     */
    public void setSong(SongDisplayable song) {
        tabPane.getTabs().clear();
        defaultLyricsArea.setText(song.getLyrics(false, false));
        if (song.getTranslations() != null) {
            for (Entry<String, String> translation : song.getTranslations().entrySet()) {
                tabPane.getTabs().add(new TranslateTab(translation.getKey(), translation.getValue()));
            }
        }
    }

    /**
     * Get the translations as a hashmap from this dialog.
     *
     * @return the translations, with the name as the key and the lyrics as the
     * value.
     */
    public HashMap<String, String> getTranslations() {
        HashMap<String, String> ret = new HashMap<>();
        for (Tab tab : tabPane.getTabs()) {
            if (tab instanceof TranslateTab) {
                TranslateTab ttab = (TranslateTab) tab;
                ret.put(ttab.getName(), ttab.getLyrics());
            } else {
                LOGGER.log(Level.WARNING, "Non-translate tab!");
            }
        }
        return ret;
    }

    /**
     * Attempt to get automatically translated lyrics from the translation
     * service. May fail for various reasons, in which case it will return an
     * empty string. This method will take a while to execute so must not be
     * called on the platform thread.
     *
     * @param langName the language name to translate to.
     * @return the translated text, or an empty string if translation failed for
     * some reason.
     */
    private String getTranslatedLyrics(String langName) {
        if(!QueleaProperties.get().getAutoTranslate()) {
            return "";
        }
        try {
            Language newLang = LanguageNameMap.INSTANCE.getLanguage(langName);
            if (newLang != null) {
                Language origLang = Detect.execute(defaultLyricsArea.getText());
                if (origLang != null) {
                    String[] origArr = defaultLyricsArea.getText().split("\n");
                    ArrayList<String> translatedList = new ArrayList<>();
                    for (String str : Translate.execute(origArr, origLang, newLang)) {
                        translatedList.add(str);
                    }
                    for (int i = 0; i < origArr.length; i++) {
                        if (origArr[i].trim().isEmpty()) {
                            translatedList.add(i, "\n");
                        }
                    }
                    StringBuilder ret = new StringBuilder();
                    for (String translatedLine : translatedList) {
                        ret.append(translatedLine).append("\n");
                    }
                    Pattern p = Pattern.compile("\n\n+");
                    LOGGER.log(Level.INFO, "Translated successfully");
                    return p.matcher(ret.toString().trim()).replaceAll("\n\n").trim();
                }
            }
            return "";
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Error translating", ex);
            return "";
        }
    }
    
    /**
     * Get a list of the existing names used for translations on tabs.
     * @return a list of the existing names used for translations on tabs.
     */
    private List<String> getExistingNames() {
        List<String> ret = new ArrayList<>();
        for(Tab tab : tabPane.getTabs()) {
            ret.add(((TranslateTab)tab).getName());
        }
        return ret;
    }
}
