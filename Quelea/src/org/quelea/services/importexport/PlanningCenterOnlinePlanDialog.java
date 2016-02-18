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
package org.quelea.services.importexport;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.concurrent.Task;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.quelea.data.YoutubeInfo;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.data.displayable.PresentationDisplayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.data.displayable.TextSlides;
import org.quelea.data.displayable.VideoDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;

/**
 *
 * @author Fabian
 */


public class PlanningCenterOnlinePlanDialog extends BorderPane {
    
    enum PlanType {
        PlanMedia,
        PlanSong,
        PlanCustomSlides,
        PlanUnknown,
    }
    
    enum MediaType {
        MediaPresentation,
        MediaVideo,
        MediaImage,
        MediaUnknown,
    }
    
    private final Map<TreeItem<String>, JSONObject> treeViewItemMap = new HashMap<TreeItem<String>, JSONObject>();
    private final PlanningCenterOnlineImportDialog importDialog;
    private final Long planId;
    
    private JSONObject  planJSON;
    
    @FXML private TreeView planView;
    @FXML private ProgressBar totalProgress;
    @FXML private ProgressBar itemProgress;
    @FXML private VBox buttonBox;
    @FXML private VBox progressBox;
    
    public PlanningCenterOnlinePlanDialog() {   
        importDialog = null;
        planId = 0L;
    }
    
    public PlanningCenterOnlinePlanDialog(PlanningCenterOnlineImportDialog importDlg, Long id) {
        importDialog = importDlg;
        planId = id;
              
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setController(this);
            loader.setResources(LabelGrabber.INSTANCE);
            Parent root = loader.load(getClass().getResourceAsStream("PlanningCenterOnlinePlanDialog.fxml"));
            setCenter(root);
            planView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            enablePlanProgressBars(false);
            
            updateView();
        
        } catch (Exception e) {
            e.printStackTrace();
        }       
    }
    
    protected PlanType getItemPlanType(JSONObject item) {
        String itemType = (String)item.get("type");
        if (itemType.equals("PlanMedia")) {
            return PlanType.PlanMedia;
        }
        else if (itemType.equals("PlanSong")) {
            return PlanType.PlanSong;
        }
        else if (itemType.equals("PlanItem") && (boolean)item.get("using_custom_slides") == true) {
            return PlanType.PlanCustomSlides;
        }
        
        return PlanType.PlanUnknown;
    }
            
    @SuppressWarnings("unchecked")
    protected void updateView() {
        planJSON = importDialog.getParser().plan(planId);  
        
        planView.setShowRoot(false);
        TreeItem<String> rootTreeItem = new TreeItem<String>();
        planView.setRoot(rootTreeItem);
        
        int itemIndex = 0;
        JSONArray items = (JSONArray)planJSON.get("items");
        for (Object itemObj : items) {
            JSONObject item = (JSONObject)itemObj;
            ++itemIndex;
            
            PlanType planType = getItemPlanType(item);
            switch (planType)
            {
                case PlanMedia:
                    addToView_PlanMedia(item, rootTreeItem);
                    break;
                    
                case PlanSong:
                    addToView_PlanSong(item, rootTreeItem);
                    break;
                    
                case PlanCustomSlides:
                    addToView_CustomSlides(item, rootTreeItem);
                    break;
                    
                default:
                    break;
            }
        }
    }
    
    protected void addToView_PlanMedia(JSONObject item, TreeItem<String> parentTreeItem) {
        String title = "Media: " + (String)item.get("title");
        TreeItem<String> treeItem = new TreeItem<String>(title);
        parentTreeItem.getChildren().add(treeItem);
        treeViewItemMap.put(treeItem, item);
    }
    
    protected void addToView_PlanSong(JSONObject item, TreeItem<String> parentTreeItem) {
        String title = "Song: " + (String)item.get("title");
        TreeItem<String> treeItem = new TreeItem<String>(title);
        parentTreeItem.getChildren().add(treeItem);
        treeViewItemMap.put(treeItem, item);
    }
    
    protected void addToView_CustomSlides(JSONObject item, TreeItem<String> parentTreeItem) {
        String title = "Custom Slides: " + (String)item.get("title");
        TreeItem<String> treeItem = new TreeItem<String>(title);
        parentTreeItem.getChildren().add(treeItem);
        treeViewItemMap.put(treeItem, item);
    }
    
    @SuppressWarnings("unchecked")
    @FXML private void onImportAllAction(ActionEvent event) {
        List<TreeItem<String> > allTreeItems = (List<TreeItem<String> >)planView.getRoot().getChildren();
        importSelected(allTreeItems);
    }
    
    @SuppressWarnings("unchecked")
    @FXML private void onImportSelectedAction(ActionEvent event) {
        List<TreeItem<String> > selectedTreeItems = (List<TreeItem<String> >)planView.getSelectionModel().getSelectedItems();
        importSelected(selectedTreeItems);
    }
    
    @FXML private void onRefreshAction(ActionEvent event) {
        updateView();
    }
    
    // Disable/enable appropriate widgets while a import task is in operation
    private void enablePlanProgressBars(boolean enable) {
        buttonBox.setDisable(enable);
        progressBox.setVisible(enable);
        planView.setDisable(enable);
        
        // stop user being able to try to change to another plan and do bad!
        importDialog.enablePlanProgressBars(enable);        
    }
    
    class ImportTask extends Task<Void> {

        List<TreeItem<String> > selectedTreeItems;

        ImportTask(List<TreeItem<String> > selectedTreeItems) {
            this.selectedTreeItems = selectedTreeItems;
        }

        @Override 
        protected Void call() throws Exception {
            enablePlanProgressBars(true);
            totalProgress.setProgress(0);

            int index = 0;
            for (TreeItem<String> treeItem : selectedTreeItems) {
                JSONObject item = treeViewItemMap.get(treeItem);

                itemProgress.setProgress(0);

                PlanType planType = getItemPlanType(item);
                switch (planType)
                {
                    case PlanMedia:
                        import_PlanMedia(item, treeItem);
                        break;

                    case PlanSong:
                        import_PlanSong(item, treeItem);
                        break;

                    case PlanCustomSlides:
                        import_CustomSlides(item, treeItem);
                        break;

                    default:
                        break;
                }

                ++index;                
                totalProgress.setProgress((double)index / (double)selectedTreeItems.size());
            }

            enablePlanProgressBars(false);  
            return null;
        }
        
        @Override 
        protected void succeeded() {
            super.succeeded();
        }
    };
    
    private void importSelected(List<TreeItem<String> > selectedTreeItems) {
        ImportTask task = new ImportTask(selectedTreeItems);
        new Thread(task).start();
    }
    
    protected void import_PlanMedia(JSONObject item, TreeItem<String> treeItem) {
        JSONArray itemMediaJSON = (JSONArray)item.get("plan_item_medias");
        if (itemMediaJSON.size() <= 0) {
            return;
        }
         
        Long mediaId = (long)((JSONObject)itemMediaJSON.get(0)).get("media_id");
        JSONObject mediaJSON = importDialog.getParser().media(mediaId);
        
        JSONArray attachmentsJSON = (JSONArray)mediaJSON.get("attachments");
        JSONObject firstAttachmentJSON = ((JSONObject)attachmentsJSON.get(0));
        
        // public URL's are youtube
        if (firstAttachmentJSON.containsKey("public_url")) {
            String url = (String)firstAttachmentJSON.get("public_url");
            YoutubeInfo youtubeInfo = new YoutubeInfo(url);
            VideoDisplayable displayable = new VideoDisplayable(youtubeInfo.getLocation(), youtubeInfo);
            QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(displayable);
        }
        else { // a file to download then put into Quela
            String url = (String)firstAttachmentJSON.get("url");
            String fileName = (String)firstAttachmentJSON.get("filename");
            
            // work out when file was last updated in PCO
            Date date = null;
            try {
                String stringDate = (String)firstAttachmentJSON.get("updated_at");
                DateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss X", Locale.ENGLISH);
                date = format.parse(stringDate);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
  
            fileName = importDialog.getParser().downloadFile(url, fileName, itemProgress, date);
            
            Displayable displayable = null;
            MediaType mediaType = classifyMedia(fileName);
            switch (mediaType)
            {
                case MediaPresentation:
                    try {
                        displayable = new PresentationDisplayable(new File(fileName));                    
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                    
                case MediaVideo:
                    displayable = new VideoDisplayable(fileName);
                    break;
                    
                case MediaImage:
                    displayable = new ImageDisplayable(new File(fileName));
                    break;
                    
                default:
                case MediaUnknown:
                    break;
            }
            
            if (displayable != null) {
                //QueleaProperties.get().setLastDirectory(file.getParentFile());
                QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(displayable);
            }
        }
    }
    
    protected MediaType classifyMedia(String fileName) {                
        String extension = "*." + FilenameUtils.getExtension(fileName);
        if (FileFilters.POWERPOINT.getExtensions().contains(extension)) {
            return MediaType.MediaPresentation;
        }
        
        if (FileFilters.VIDEOS.getExtensions().contains(extension)) {
            return MediaType.MediaVideo;
        }
        
        if (FileFilters.IMAGES.getExtensions().contains(extension)) {
            return MediaType.MediaImage;
        }
        
        return MediaType.MediaUnknown;
    }
            
    
    protected void import_PlanSong(JSONObject item, TreeItem<String> treeItem) {
        JSONObject songJSON = (JSONObject)item.get("song");
        String title = (String)songJSON.get("title");
        String author = (String)songJSON.get("author");
 
        Long arrangementId = (Long)((JSONObject)item.get("arrangement")).get("id");
        JSONObject arrangement = importDialog.getParser().arrangement(arrangementId);
        String lyrics = (String)arrangement.get("chord_chart");
        String sequence = (String)arrangement.get("sequence_to_s");

        Long ccli = (Long)songJSON.get("ccli_id");
        String copyright = (String)songJSON.get("copyright");
        
        SongDisplayable song = new SongDisplayable(title, author);
        song.setLyrics(lyrics);
        song.setCopyright(copyright);
        song.setCcli(String.valueOf(ccli));     
        
        Utils.updateSongInBackground(song, true, false);
        QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(song);
        QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
    }
    
    protected void import_CustomSlides(JSONObject item, TreeItem<String> treeItem) {
        String title = (String)item.get("title");
        List<TextSection> textSections = new ArrayList<TextSection>();
        
        JSONArray customSlides = (JSONArray)item.get("custom_slides");
        for (Object slideObj : customSlides) {
            JSONObject slide = (JSONObject)slideObj;            
            String body = (String)slide.get("body");
            String[] bodyLines = body.split("\\r?\\n");
            TextSection text = new TextSection(""/*(String)slide.get("label")*/, bodyLines, null, false);
            textSections.add(text);
        }

        TextSlides slides = new TextSlides(title, textSections);
        QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(slides);
    }
}
