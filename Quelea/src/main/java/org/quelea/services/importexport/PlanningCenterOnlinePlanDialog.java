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
import java.io.IOException;
import java.time.LocalDateTime;
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
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.data.displayable.PresentationDisplayable;
import org.quelea.data.displayable.PdfDisplayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.VideoDisplayable;
import org.quelea.planningcenter.model.services.Arrangement;
import org.quelea.planningcenter.model.services.Attachment;
import org.quelea.planningcenter.model.services.CustomSlide;
import org.quelea.planningcenter.model.services.Item;
import org.quelea.planningcenter.model.services.Media;
import org.quelea.planningcenter.model.services.Plan;
import org.quelea.planningcenter.model.services.Song;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
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

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final Map<TreeItem<String>, Item> treeViewItemMap = new HashMap<>();
    private final PlanningCenterOnlineImportDialog importDialog;
    private final Plan plan;
    private final List<Item> planItems;

    @FXML
    private TreeView<String> planView;
    @FXML
    private ProgressBar totalProgress;
    @FXML
    private ProgressBar itemProgress;
    @FXML
    private VBox buttonBox;
    @FXML
    private VBox progressBox;

    public PlanningCenterOnlinePlanDialog() {
        importDialog = null;
        this.plan = null;
        this.planItems = new ArrayList<>();
    }

    public PlanningCenterOnlinePlanDialog(PlanningCenterOnlineImportDialog importDlg, Plan plan, List<Item> planItems) {
        importDialog = importDlg;
        this.plan = plan;
        this.planItems = planItems;

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

    protected PlanType getItemPlanType(Item item) {
        String itemType = item.getItemType();
        if ("media".equals(itemType)) {
            return PlanType.MEDIA;
        } else if ("song".equals(itemType)) {
            return PlanType.SONG;
        }
//        else if (itemType.equals("item") && (boolean) item.get("using_custom_slides") == true) {
//            return PlanType.CUSTOM_SLIDES;
//        } else if (itemType.equals("item") && ((JSONArray) item.get("plan_item_medias")).size() > 0) {
//            return PlanType.MEDIA;
//        }
        return PlanType.UNKNOWN;
    }

    @SuppressWarnings("unchecked")
    protected void updateView() {
        LOGGER.log(Level.INFO, "Updating view with id {0}", plan.getId());

        planView.setShowRoot(false);
        TreeItem<String> rootTreeItem = new TreeItem<>();
        planView.setRoot(rootTreeItem);

        for (Item item : planItems) {

            PlanType planType = getItemPlanType(item);
            switch (planType) {
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

    protected void addToView_PlanMedia(Item item, TreeItem<String> parentTreeItem) {
        String title = "Media: " + item.getTitle();
        TreeItem<String> treeItem = new TreeItem<>(title);
        parentTreeItem.getChildren().add(treeItem);
        treeViewItemMap.put(treeItem, item);
    }

    protected void addToView_PlanSong(Item item, TreeItem<String> parentTreeItem) {
        String title = "Song: " + item.getTitle();
        TreeItem<String> treeItem = new TreeItem<>(title);
        parentTreeItem.getChildren().add(treeItem);
        treeViewItemMap.put(treeItem, item);
    }

    protected void addToView_CustomSlides(Item item, TreeItem<String> parentTreeItem) {
        String title = "Custom Slides: " + item.getTitle();
        TreeItem<String> treeItem = new TreeItem<>(title);
        parentTreeItem.getChildren().add(treeItem);
        treeViewItemMap.put(treeItem, item);
    }

    @FXML
    private void onImportAllAction(ActionEvent event) {
        List<TreeItem<String>> allTreeItems = planView.getRoot().getChildren();
        importSelected(allTreeItems);
    }

    @FXML
    private void onImportSelectedAction(ActionEvent event) {
        List<TreeItem<String>> selectedTreeItems = planView.getSelectionModel().getSelectedItems();
        importSelected(selectedTreeItems);
    }

    @FXML
    private void onRefreshAction(ActionEvent event) {
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

        List<TreeItem<String>> selectedTreeItems;
        List<Displayable> importItems = new ArrayList<>();

        ImportTask(List<TreeItem<String>> selectedTreeItems) {
            this.selectedTreeItems = selectedTreeItems;
        }

        @Override
        protected Void call() throws Exception {
            try {
                enablePlanProgressBars(true);
                totalProgress.setProgress(0);

                int index = 0;
                for (TreeItem<String> treeItem : selectedTreeItems) {
                    Item item = treeViewItemMap.get(treeItem);

                    itemProgress.setProgress(0);

                    PlanType planType = getItemPlanType(item);
                    switch (planType) {
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
                    totalProgress.setProgress((double) index / (double) selectedTreeItems.size());
                }

                enablePlanProgressBars(false);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return null;

        }

        @Override
        protected void succeeded() {
            importTaskSucceeded(this);
            super.succeeded();
        }

        protected void prepare_PlanMedia(Item item, TreeItem<String> treeItem) {
            try {
                List<Media> customMedia = importDialog.getParser().getPlanningCenterClient().services().serviceType(plan.getServiceType().getId()).plan(plan.getId()).item(item.getId()).media().api().get().execute().body().get();

                for (Media media : customMedia) {
                    prepare_PlanMedia_fromMediaJSON(media);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Error preparing media", ex);
            }
        }

        protected void prepare_PlanMedia_fromMediaJSON(Media media) {
            List<Attachment> attachments = media.getAttachments();
            Attachment firstAttachment = attachments.get(0);

            if (firstAttachment.isDownloadable()) {
                String fileName = firstAttachment.getFilename();

                // work out when file was last updated in PCO
                LocalDateTime updatedAt = firstAttachment.getUpdatedAt();

                fileName = importDialog.getParser().downloadFile(media, firstAttachment, fileName, itemProgress, updatedAt);

                
                Displayable displayable = null;
                MediaType mediaType = classifyMedia(fileName);
                switch (mediaType) {
                    case PRESENTATION:
                        try {
                            displayable = new PresentationDisplayable(new File(fileName));
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Error", e);
                        }
                        break;

                    case PDF:
                        try {
                            displayable = new PdfDisplayable(new File(fileName));
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Error", e);
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

        protected void prepare_PlanSong(Item item, TreeItem<String> treeItem) {
            Song song = item.getSong();
            Arrangement arrangement = item.getArrangement();

            String lyrics = arrangement.getChordChart();
            if (lyrics == null) {
                lyrics = "";
            } else {
                lyrics = cleanLyrics(lyrics);
            }

            SongDisplayable songDisplayable = new SongDisplayable(song.getTitle(), song.getAuthor());
            songDisplayable.setTheme(ThemeDTO.DEFAULT_THEME);
            songDisplayable.setLyrics(lyrics);
            songDisplayable.setCopyright(song.getCopyright());
            songDisplayable.setCcli(Integer.toString(song.getCcliNumber()));

            Utils.updateSongInBackground(songDisplayable, true, false);
            importItems.add(songDisplayable);
        }

        protected void prepare_CustomSlides(Item item, TreeItem<String> treeItem) {

            try {
                String title = item.getTitle();
                List<String> slideTextArray = new ArrayList<>();
                List<CustomSlide> customSlides = importDialog.getParser().getPlanningCenterClient().services().serviceType(plan.getServiceType().getId()).plan(plan.getId()).item(item.getId()).customSlides().api().get()
                        .execute().body().get();

                for (CustomSlide slide : customSlides) {
                    String body = slide.getBody();

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
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Can't prepare custom slides", ex);
            }
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

    private void importSelected(List<TreeItem<String>> selectedTreeItems) {
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

        class TitleTextBlock {

            public String title;
            public String text;

            public TitleTextBlock(String title, String text) {
                this.title = title;
                this.text = text;
            }
        }
        List<TitleTextBlock> titleTextBlockList = new ArrayList<>();

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

                String number = (match.group(2) != null) ? match.group(2) : "";
                number = (match.group(4) != null) ? match.group(4) : number;

                title = title + " " + number;
                title = title.trim();

                int matchStart = match.start();
                if (lastMatchEnd != -1) {
                    String text = lyrics.substring(lastMatchEnd, matchStart).trim();
                    titleTextBlockList.add(new TitleTextBlock(lastTitle, text));
                } else {
                    // if the first title is malformed, at least this will pull down the text for the user to be able to fix it up
                    if (matchStart != 0) {
                        String text = lyrics.substring(0, matchStart).trim();
                        titleTextBlockList.add(new TitleTextBlock("Unknown", text));
                    }
                }

                lastTitle = title;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error", e);
            }

            lastMatchEnd = match.end();
        }

        if (lastMatchEnd != -1) {
            String text = lyrics.substring(lastMatchEnd).trim();
            titleTextBlockList.add(new TitleTextBlock(lastTitle, text));
        } else {
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
