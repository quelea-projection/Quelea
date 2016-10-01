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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.quelea.data.displayable.PdfDisplayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextSection;
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
        MEDIA,
        SONG,
        CUSTOM_SLIDES,
        UNKNOWN,
    }
    
    enum MediaType {
        PRESENTATION,
        PDF,
        VIDEO,
        IMAGE,        
        UNKNOWN,
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
            return PlanType.MEDIA;
        }
        else if (itemType.equals("PlanSong")) {
            return PlanType.SONG;
        }
        else if (itemType.equals("PlanItem") && (boolean)item.get("using_custom_slides") == true) {
            return PlanType.CUSTOM_SLIDES;
        }
        else if (itemType.equals("PlanItem") && ((JSONArray)item.get("plan_item_medias")).size() > 0) {
            return PlanType.MEDIA;
        }
        
        return PlanType.UNKNOWN;
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
                case MEDIA:
                    addToView_PlanMedia(item, rootTreeItem);
                    break;
                    
                case SONG:
                    addToView_PlanSong(item, rootTreeItem);
                    break;
                    
                case CUSTOM_SLIDES:
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
        List<Displayable>       importItems = new ArrayList<Displayable>();

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
                    case MEDIA:
                        prepare_PlanMedia(item, treeItem);
                        break;

                    case SONG:
                        prepare_PlanSong(item, treeItem);
                        break;

                    case CUSTOM_SLIDES:
                        prepare_CustomSlides(item, treeItem);
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
            importTaskSucceeded(this);
            super.succeeded();
        }
        
        protected void prepare_PlanMedia(JSONObject item, TreeItem<String> treeItem) {
            JSONArray itemMediaJSON = (JSONArray)item.get("plan_item_medias");
            if (itemMediaJSON.size() <= 0) {
                return;
            }

            // process each media item in the item media
            for (int i = 0; i < itemMediaJSON.size(); ++i)
            {
                Long mediaId = (long)((JSONObject)itemMediaJSON.get(i)).get("media_id");
                JSONObject mediaJSON = importDialog.getParser().media(mediaId);
                prepare_PlanMedia_fromMediaJSON(mediaJSON);
            }
        }
        
        protected void prepare_PlanMedia_fromMediaJSON(JSONObject mediaJSON) {
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
                    case PRESENTATION:
                        try {
                            displayable = new PresentationDisplayable(new File(fileName));                    
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                        
                    case PDF:
                        try {
                            displayable = new PdfDisplayable(new File(fileName));                    
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    case VIDEO:
                        displayable = new VideoDisplayable(fileName);
                        break;

                    case IMAGE:
                        displayable = new ImageDisplayable(new File(fileName));
                        break;

                    default:
                    case UNKNOWN:
                        break;
                }

                if (displayable != null) {
                    importItems.add(displayable);
                }
            }
        }
        
        protected void prepare_PlanSong(JSONObject item, TreeItem<String> treeItem) {
            JSONObject songJSON = (JSONObject)item.get("song");
            String title = (String)songJSON.get("title");
            String author = (String)songJSON.get("author");

            Long arrangementId = (Long)((JSONObject)item.get("arrangement")).get("id");
            JSONObject arrangement = importDialog.getParser().arrangement(arrangementId);
            String lyrics = cleanLyrics((String)arrangement.get("chord_chart"));
            String sequence = (String)arrangement.get("sequence_to_s");

            Long ccli = (Long)songJSON.get("ccli_id");
            String copyright = (String)songJSON.get("copyright");

            SongDisplayable song = new SongDisplayable(title, author);
            song.setLyrics(lyrics);
            song.setCopyright(copyright);
            song.setCcli(String.valueOf(ccli));     

            Utils.updateSongInBackground(song, true, false);
            importItems.add(song);
        }

        protected void prepare_CustomSlides(JSONObject item, TreeItem<String> treeItem) {
            String title = (String)item.get("title");
            List<TextSection> textSections = new ArrayList<TextSection>();

            List<String> slideTextArray = new ArrayList<String>();
            JSONArray customSlides = (JSONArray)item.get("custom_slides");
            for (Object slideObj : customSlides) {
                JSONObject slide = (JSONObject)slideObj;            
                String body = (String)slide.get("body");

                // might need something like this in future:
                // depending on how often we use custom slides with an empty line which I think is rare
                // enough to ignore for now
                //String body = "(" + (String)slide.get("label") + ")" + System.lineSeparator() + (String)slide.get("body");
                slideTextArray.add(body);
            }

            // double line separator so SongDisplayable knows where to break the slides apart
            String joinedSlidesText = String.join(System.lineSeparator() + System.lineSeparator(), slideTextArray);

            SongDisplayable slides = new SongDisplayable(title, "Unknown");
            slides.setLyrics(joinedSlidesText);
            importItems.add(slides);
        }
    };
    
    // This MUST be run in the main thread
    // This adds the prepared displayable items into Quelea
    private void importTaskSucceeded(ImportTask importTask) {
        for (Displayable displayable : importTask.importItems) {
            QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(displayable);
        }
        
        QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
    }
    
    private void importSelected(List<TreeItem<String> > selectedTreeItems) {
        ImportTask task = new ImportTask(selectedTreeItems);
        new Thread(task).start();
    }
    
    protected MediaType classifyMedia(String fileName) {                
        String extension = "*." + FilenameUtils.getExtension(fileName);
        if (FileFilters.POWERPOINT.getExtensions().contains(extension)) {
            return MediaType.PRESENTATION;
        }
        
        if (FileFilters.PDF_GENERIC.getExtensions().contains(extension)) {
            return MediaType.PDF;
        }
                
        if (FileFilters.VIDEOS.getExtensions().contains(extension)) {
            return MediaType.VIDEO;
        }
        
        if (FileFilters.IMAGES.getExtensions().contains(extension)) {
            return MediaType.IMAGE;
        }
        
        return MediaType.UNKNOWN;
    }
            
    // clean up things like (C2) transform it to (Chorus 2)
    // so Quelea can handle it
    protected String cleanLyrics(String lyrics) {
        Pattern titleExp = Pattern.compile("^\\(?(Verse|Chorus|Pre-Chorus|Pre Chorus|Tag|Outro|Bridge|Misc|Interlude|Ending)\\)?\\s?(\\d?)|\\(?(\\S)(\\d+)\\)?$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        
        // allows us to expand abbreviations to full name (ensure Key value is all uppercase)
        Map<String, String> titleDict = new HashMap<String, String>();
        titleDict.put("C", "Chorus");
        titleDict.put("PC", "Pre-Chorus");
        titleDict.put("V", "Verse");
        titleDict.put("T", "Tag");
        titleDict.put("O", "Outro");
        titleDict.put("B", "Bridge");
        titleDict.put("M", "Misc");
        titleDict.put("E", "Ending");
        titleDict.put("I", "Interlude");
        
        class TitleTextBlock
        {
            public String title;
            public String text;
            
            public TitleTextBlock(String title, String text) {
                this.title = title;
                this.text = text;
            }
        }
        List<TitleTextBlock> titleTextBlockList = new ArrayList<TitleTextBlock>();
        
       
        // lets clean up some funky stuff we don't want the audience to know about:
        // remove line repat X time tags - (5X) 
        // and (REPEAT) tags
        Pattern removeExp = Pattern.compile("\\(\\d+X\\)|\\(REPEAT\\)", Pattern.CASE_INSENSITIVE);
        Matcher m = removeExp.matcher(lyrics);
        lyrics = m.replaceAll("");
       
        
        int lastMatchEnd = -1;
        String lastTitle = "";
        Matcher match = titleExp.matcher(lyrics);        
        while (match.find()) {            
            try {
                int groupCount = match.groupCount();
                String title = (match.group(1) != null) ? match.group(1) : "";
                title = (match.group(3) != null) ? match.group(3) : title;
                if (!title.isEmpty()) {
                    // expand abbreviations
                    if (titleDict.containsKey(title.toUpperCase())) {
                        title = titleDict.get(title);
                    }
                }

                String number =  (match.group(2) != null) ? match.group(2) : "";
                number = (match.group(4) != null) ? match.group(4) : number;

                title = title + " " + number;
                title = title.trim();

                if (lastMatchEnd != -1) {
                    String text = lyrics.substring(lastMatchEnd, match.start()).trim();
                    titleTextBlockList.add(new TitleTextBlock(lastTitle, text));
                }
                
                lastTitle = title;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            
            lastMatchEnd = match.end();
        }
        
        if (lastMatchEnd != -1) {
            String text = lyrics.substring(lastMatchEnd).trim();
            titleTextBlockList.add(new TitleTextBlock(lastTitle, text));
        }
        
        // now the song has been divided into titled text blocks, time to bring it together nicely
        // for Quelea
        String cleanedLyrics = "";
        for (int i = 0; i < titleTextBlockList.size(); ++i) {
            TitleTextBlock titleTextBlock = titleTextBlockList.get(i);
            if (titleTextBlock.text.isEmpty()) {
                continue;
            }
            
            // newlines separating previous from current
            if (i != 0) {
                cleanedLyrics += System.lineSeparator() + System.lineSeparator();
            }
            
            cleanedLyrics += "(" + titleTextBlock.title + ")" + System.lineSeparator() + titleTextBlock.text;
         }

        return cleanedLyrics;
    }
}
