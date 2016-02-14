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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.quelea.data.displayable.TextSection;
import org.quelea.data.displayable.TextSlides;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;

/**
 *
 * @author Fabian
 */


public class PlanningCenterOnlinePlanDialog extends Pane {
    
    enum PlanType {
        PlanMedia,
        PlanSong,
        PlanCustomSlides,
        PlanUnknown,
    }
    
    private final Map<TreeItem<String>, JSONObject> treeViewItemMap = new HashMap<TreeItem<String>, JSONObject>();
    private final PlanningCenterOnlineImportDialog importDialog;
    private final Long planId;
    
    private JSONObject  planJSON;
    
    @FXML private TreeView planView;
    
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
            getChildren().addAll(root);
            planView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
  
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
    
    @FXML private void onImportAllAction(ActionEvent event) {
        List<TreeItem<String> > allTreeItems = planView.getRoot().getChildren();
        importSelected(allTreeItems);
    }
    
    @FXML private void onImportSelectedAction(ActionEvent event) {
        List<TreeItem<String> > selectedTreeItems = planView.getSelectionModel().getSelectedItems();
        importSelected(selectedTreeItems);
    }
    
    @FXML private void onRefreshAction(ActionEvent event) {
        updateView();
    }
    
    private void importSelected(List<TreeItem<String> > selectedTreeItems) {
        for (TreeItem<String> treeItem : selectedTreeItems) {
            JSONObject item = treeViewItemMap.get(treeItem);
            
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
        }
    }
    
    protected void import_PlanMedia(JSONObject item, TreeItem<String> treeItem) {
    }
    
    protected void import_PlanSong(JSONObject item, TreeItem<String> treeItem) {
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
