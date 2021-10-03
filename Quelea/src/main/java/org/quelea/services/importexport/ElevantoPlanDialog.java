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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;

/**
 *
 * @author Fabian Mathews
 */
public class ElevantoPlanDialog extends BorderPane {
    
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
    
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final Map<TreeItem<String>, JSONObject> treeViewItemMap = new HashMap<>();
    private final ElevantoImportDialog importDialog;
    
    private JSONObject  planJSON;
    
    @FXML private TreeView planView;
    @FXML private ProgressBar totalProgress;
    @FXML private ProgressBar itemProgress;
    @FXML private VBox buttonBox;
    @FXML private VBox progressBox;
    
    public ElevantoPlanDialog() {   
        importDialog = null;
    }
    
    public ElevantoPlanDialog(ElevantoImportDialog importDlg, JSONObject plan) {
        importDialog = importDlg;
        planJSON = plan;
              
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setController(this);
            loader.setResources(LabelGrabber.INSTANCE);
            Parent root = loader.load(getClass().getResourceAsStream("PlanningCenterOnlinePlanDialog.fxml"));
            setCenter(root);
            planView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            enablePlanProgressBars(false);
            LOGGER.log(Level.INFO, "Initialised dialog, updating view");
            updateView();
        
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error", e);
        }       
    }
    
    protected PlanType getItemPlanType(JSONObject item) {
        // valid song object is a song
        try {
            JSONObject song = (JSONObject)item.get("song");
            return PlanType.SONG;
        }
        catch (Exception e) {
            
        }
        
        return PlanType.UNKNOWN;
    }
            
    @SuppressWarnings("unchecked")
    protected void updateView() {
        LOGGER.log(Level.INFO, "JSON is {0}", planJSON);
        
        planView.setShowRoot(false);
        TreeItem<String> rootTreeItem = new TreeItem<>();
        planView.setRoot(rootTreeItem);
        
        JSONObject itemsObj = (JSONObject)planJSON.get("items");
        JSONArray itemArray = (JSONArray)itemsObj.get("item");
        for (Object itemObj : itemArray) {
            JSONObject item = (JSONObject)itemObj;
            
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
    }
    
    protected void addToView_PlanSong(JSONObject item, TreeItem<String> parentTreeItem) {
        JSONObject song = (JSONObject)item.get("song");
        String title = "Song: " + (String)song.get("title");
        TreeItem<String> treeItem = new TreeItem<>(title);
        parentTreeItem.getChildren().add(treeItem);
        treeViewItemMap.put(treeItem, item);
    }
    
    protected void addToView_CustomSlides(JSONObject item, TreeItem<String> parentTreeItem) {
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
        List<Displayable>       importItems = new ArrayList<>();

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
        }
        
        protected void prepare_PlanMedia_fromMediaJSON(JSONObject mediaJSON) {
        }
        
        protected void prepare_PlanSong(JSONObject item, TreeItem<String> treeItem) {
            JSONObject songJSON = (JSONObject)item.get("song");
            String title = (String)songJSON.get("title");
            String author = (String)songJSON.get("artist");

            String arrangementId = (String)((JSONObject)songJSON.get("arrangement")).get("id");
            JSONObject response = importDialog.getParser().arrangement(arrangementId);
            JSONObject arrangement = (JSONObject)((JSONArray)response.get("arrangement")).get(0);
            String lyrics = cleanLyrics((String)arrangement.get("lyrics"));
            //JSONArray sequence = (JSONArray)arrangement.get("sequence");

            String ccli = (String)songJSON.get("ccli_number");
            String copyright = (String)arrangement.get("copyright");

            SongDisplayable song = new SongDisplayable(title, author);
            song.setLyrics(lyrics);
            song.setCopyright(copyright);
            song.setCcli(ccli);     

            Utils.updateSongInBackground(song, true, false);
            importItems.add(song);
        }

        protected void prepare_CustomSlides(JSONObject item, TreeItem<String> treeItem) {
        }
    }
    
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
        lyrics = m.replaceAll("").trim();
        
        // remove embedded choords (wrapped in brackets)
        Pattern removeChoordsExp = Pattern.compile("(?m)(^| |\\[|\\b)([A-G](##?|bb?)?((sus|maj|min|aug|dim)\\d?)?(\\/[A-G](##?|bb?)?)?)(\\]| (?!\\w)|$)");
        Matcher m2 = removeChoordsExp.matcher(lyrics);
        lyrics = m2.replaceAll("");
        
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

				int matchStart = match.start();
                if (lastMatchEnd != -1) {
                    String text = lyrics.substring(lastMatchEnd, matchStart).trim();
                    titleTextBlockList.add(new TitleTextBlock(lastTitle, text));
                }
				else {
					// if the first title is malformed, at least this will pull down the text for the user to be able to fix it up
					if (matchStart != 0) {
						String text = lyrics.substring(0, matchStart).trim();
						titleTextBlockList.add(new TitleTextBlock("Unknown", text));
					}
				}
                
                lastTitle = title;
            }
            catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error", e);
            }
            
            lastMatchEnd = match.end();
        }
        
        if (lastMatchEnd != -1) {
            String text = lyrics.substring(lastMatchEnd).trim();
            titleTextBlockList.add(new TitleTextBlock(lastTitle, text));
        }
		else {
			// the whole song is malformed, at least this will pull down the text for the user to be able to fix it up
			String text = lyrics;
			titleTextBlockList.add(new TitleTextBlock("Unknown", text));
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
