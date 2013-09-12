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
package org.quelea.windows.options;

import java.util.List;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.javafx.dialog.Dialog;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.PropertyPanel;
import org.quelea.windows.main.QueleaApp;

/**
 * The dialog that holds all the options the user can set.
 * @author Michael
 */
public class OptionsDialog extends Stage {

    private final BorderPane mainPane;
    private final Button okButton;
    private final TabPane tabbedPane;
    private final OptionsDisplaySetupPanel displayPanel;
    private final OptionsGeneralPanel generalPanel;
    private OptionsBiblePanel biblePanel;
    private OptionsStageViewPanel stageViewPanel;

    /**
     * Create a new options dialog.
     * @param owner the owner of the dialog - should be the main window.
     */
    public OptionsDialog() {
        setTitle(LabelGrabber.INSTANCE.getLabel("options.title"));
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setResizable(false);
        
        getIcons().add(new Image("file:icons/options.png", 16, 16, false, true));
        mainPane = new BorderPane();
        tabbedPane = new TabPane();
        
        generalPanel = new OptionsGeneralPanel();
        Tab generalTab = new Tab();
        generalTab.setClosable(false);
        generalTab.setText(LabelGrabber.INSTANCE.getLabel("general.options.heading"));
        generalTab.setContent(generalPanel);
        tabbedPane.getTabs().add(generalTab);
        
        displayPanel = new OptionsDisplaySetupPanel();
        Tab displayTab = new Tab();
        displayTab.setClosable(false);
        displayTab.setText(LabelGrabber.INSTANCE.getLabel("display.options.heading"));
        displayTab.setContent(displayPanel);
        tabbedPane.getTabs().add(displayTab);
        
        stageViewPanel = new OptionsStageViewPanel();
        Tab stageViewTab = new Tab();
        stageViewTab.setClosable(false);
        stageViewTab.setText(LabelGrabber.INSTANCE.getLabel("stage.options.heading"));
        stageViewTab.setContent(stageViewPanel);
        tabbedPane.getTabs().add(stageViewTab);
//        
        biblePanel = new OptionsBiblePanel();
        Tab bibleTab = new Tab();
        bibleTab.setClosable(false);
        bibleTab.setText(LabelGrabber.INSTANCE.getLabel("bible.options.heading"));
        bibleTab.setContent(biblePanel);
        tabbedPane.getTabs().add(bibleTab);
        
        mainPane.setCenter(tabbedPane);
        okButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), new ImageView(new Image("file:icons/tick.png")));
        BorderPane.setMargin(okButton, new Insets(5));
        okButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                List<Tab> tabs = tabbedPane.getTabs();
                for(int i = 0; i < tabs.size(); i++) {
                    if(tabs.get(i).getContent() instanceof PropertyPanel) {
                        ((PropertyPanel) tabs.get(i).getContent()).setProperties();
                    }
                }
                callBeforeHiding();
                hide();
            }
        });
        BorderPane.setAlignment(okButton, Pos.CENTER);
        mainPane.setBottom(okButton);
        setScene(new Scene(mainPane));
    }
    
    /**
     * Call this method before showing this dialog to set it up properly.
     */
    public void callBeforeShowing() {
        getGeneralPanel().resetLanguageChanged();
    }
    
    /**
     * Call this method before hiding this dialog to tear it down properly.
     */
    private void callBeforeHiding() {
        if(getGeneralPanel().hasLanguageChanged()) {
            Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("language.changed"), LabelGrabber.INSTANCE.getLabel("language.changed.message"), QueleaApp.get().getMainWindow());
        }
    }

    /**
     * Get the general panel used in this options dialog.
     * @return the general panel.
     */
    public OptionsGeneralPanel getGeneralPanel() {
        return generalPanel;
    }

    /**
     * Get the display panel used in this options dialog.
     * @return the display panel.
     */
    public OptionsDisplaySetupPanel getDisplayPanel() {
        return displayPanel;
    }

    /**
     * Get the bible panel used in this options dialog.
     * @return the bible panel.
     */
    public OptionsBiblePanel getBiblePanel() {
        return biblePanel;
    }

    /**
     * Get the stage view panel used in this options dialog.
     * @return the stage view panel.
     */
    public OptionsStageViewPanel getStageViewPanel() {
        return stageViewPanel;
    }

    /**
     * Get the OK button used to affirm the change in options.
     * @return the OK button.
     */
    public Button getOKButton() {
        return okButton;
    }

}
