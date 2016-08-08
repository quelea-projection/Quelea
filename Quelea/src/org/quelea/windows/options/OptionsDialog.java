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
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
    private final OptionsNoticePanel noticePanel;
    private final OptionsPresentationPanel presentationPanel;
    private final OptionsBiblePanel biblePanel;
    private final OptionsStageViewPanel stageViewPanel;
    private final ServerSettingsPanel serverSettingsPanel;
    private final OptionsRecordingPanel recordingPanel;
    private final OptionsImportExportPanel importExportPanel;

    /**
     * Create a new options dialog.
     */
    public OptionsDialog() {
        setTitle(LabelGrabber.INSTANCE.getLabel("options.title"));
        initModality(Modality.APPLICATION_MODAL);
        initOwner(QueleaApp.get().getMainWindow());
        setResizable(false);
        
        getIcons().add(new Image("file:icons/options.png", 16, 16, false, true));
        mainPane = new BorderPane();
        tabbedPane = new TabPane();
        
        generalPanel = new OptionsGeneralPanel();
        Tab generalTab = new Tab();
        generalTab.setClosable(false);
        generalTab.setText(LabelGrabber.INSTANCE.getLabel("general.options.heading"));
        generalTab.setGraphic(new ImageView(new Image("file:icons/generalsettingsicon.png")));
        generalTab.setContent(generalPanel);
        tabbedPane.getTabs().add(generalTab);
        
        displayPanel = new OptionsDisplaySetupPanel();
        Tab displayTab = new Tab();
        displayTab.setClosable(false);
        displayTab.setText(LabelGrabber.INSTANCE.getLabel("display.options.heading"));
        displayTab.setGraphic(new ImageView(new Image("file:icons/monitorsettingsicon.png")));
        displayTab.setContent(displayPanel);
        tabbedPane.getTabs().add(displayTab);
        
        stageViewPanel = new OptionsStageViewPanel();
        Tab stageViewTab = new Tab();
        stageViewTab.setClosable(false);
        stageViewTab.setText(LabelGrabber.INSTANCE.getLabel("stage.options.heading"));
        stageViewTab.setGraphic(new ImageView(new Image("file:icons/stageviewsettingsicon.png")));
        stageViewTab.setContent(stageViewPanel);
        tabbedPane.getTabs().add(stageViewTab);
        
        noticePanel = new OptionsNoticePanel();
        Tab noticeTab = new Tab();
        noticeTab.setClosable(false);
        noticeTab.setText(LabelGrabber.INSTANCE.getLabel("notice.options.heading"));
        noticeTab.setGraphic(new ImageView(new Image("file:icons/noticessettingsicon.png")));
        noticeTab.setContent(noticePanel);
        tabbedPane.getTabs().add(noticeTab);
        
        presentationPanel = new OptionsPresentationPanel();
        Tab presentationTab = new Tab();
        presentationTab.setClosable(false);
        presentationTab.setText(LabelGrabber.INSTANCE.getLabel("presentation.options.heading"));
        presentationTab.setGraphic(new ImageView(new Image("file:icons/presentationssettingsicon.png")));
        presentationTab.setContent(presentationPanel);
        tabbedPane.getTabs().add(presentationTab);
        
        biblePanel = new OptionsBiblePanel();
        Tab bibleTab = new Tab();
        bibleTab.setClosable(false);
        bibleTab.setText(LabelGrabber.INSTANCE.getLabel("bible.options.heading"));
        bibleTab.setContent(biblePanel);
        bibleTab.setGraphic(new ImageView(new Image("file:icons/biblesettingsicon.png")));
        tabbedPane.getTabs().add(bibleTab);
        
        serverSettingsPanel = new ServerSettingsPanel();
        Tab serverSettingsTab = new Tab();
        serverSettingsTab.setClosable(false);
        serverSettingsTab.setText(LabelGrabber.INSTANCE.getLabel("server.settings.heading"));
        serverSettingsTab.setGraphic(new ImageView(new Image("file:icons/serversettingsicon.png")));
        serverSettingsTab.setContent(serverSettingsPanel);
        tabbedPane.getTabs().add(serverSettingsTab);
        
        recordingPanel = new OptionsRecordingPanel();
        Tab optionsRecordingTab = new Tab();
        optionsRecordingTab.setClosable(false);
        optionsRecordingTab.setText(LabelGrabber.INSTANCE.getLabel("recordings.options.heading"));
        optionsRecordingTab.setGraphic(new ImageView(new Image("file:icons/recordingssettingsicon.png")));
        optionsRecordingTab.setContent(recordingPanel);
        tabbedPane.getTabs().add(optionsRecordingTab);
        
        importExportPanel = new OptionsImportExportPanel();
        Tab optionsImportExportTab = new Tab();
        optionsImportExportTab.setClosable(false);
        optionsImportExportTab.setText(LabelGrabber.INSTANCE.getLabel("importexport.options.heading"));
        optionsImportExportTab.setGraphic(new ImageView(new Image("file:icons/text.png")));
        optionsImportExportTab.setContent(importExportPanel);
        tabbedPane.getTabs().add(optionsImportExportTab);
        
        mainPane.setCenter(tabbedPane);
        okButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), new ImageView(new Image("file:icons/tick.png")));
        BorderPane.setMargin(okButton, new Insets(5));
        okButton.setOnAction((ActionEvent t) -> {
            List<Tab> tabs = tabbedPane.getTabs();
            tabs.stream().filter((tab) -> (tab.getContent() instanceof PropertyPanel)).forEach((tab) -> {
                ((PropertyPanel) tab.getContent()).setProperties();
            });
            callBeforeHiding();
            hide();
        });
        BorderPane.setAlignment(okButton, Pos.CENTER);
        mainPane.setBottom(okButton);
        setScene(new Scene(mainPane));
    }
    
    /**
     * Call this method before showing this dialog to set it up properly.
     */
    public void callBeforeShowing() {
        generalPanel.resetLanguageChanged();
        serverSettingsPanel.resetChanged();
        presentationPanel.resetPresentationChanged();
    }
    
    /**
     * Call this method before hiding this dialog to tear it down properly.
     */
    private void callBeforeHiding() {
        if(generalPanel.hasLanguageChanged()) {
            Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("language.changed"), LabelGrabber.INSTANCE.getLabel("language.changed.message"), QueleaApp.get().getMainWindow());
        }
        if(serverSettingsPanel.hasChanged()) {
            Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("server.changed.label"), LabelGrabber.INSTANCE.getLabel("server.changed.message"), QueleaApp.get().getMainWindow());
        }
        if(presentationPanel.hasPPChanged()) {
            Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("presentation.changed.label"), LabelGrabber.INSTANCE.getLabel("presentation.changed.message"), QueleaApp.get().getMainWindow());
        }
    }

    /**
     * Get the OK button used to affirm the change in options.
     * @return the OK button.
     */
    public Button getOKButton() {
        return okButton;
    }

    public ServerSettingsPanel getServerSettingsPanel() {
        return serverSettingsPanel;
    }
    
    public OptionsGeneralPanel getOptionsGeneralPanel() {
        return generalPanel;
    }
    
    public OptionsDisplaySetupPanel getDisplaySetupPanel() {
        return displayPanel;
    }
    
    public OptionsRecordingPanel getRecordingSettingsPanel() {
        return recordingPanel;
    }
    
    public OptionsImportExportPanel getImportExportSettingsPanel() {
        return importExportPanel;
    }
    
    public void setCurrentPanel(GridPane pane) {
        for (Tab tab : tabbedPane.getTabs()) {
            if (tab.getContent() == pane) {
                tabbedPane.getSelectionModel().select(tab);
                break;
            }
        }
    }
}
