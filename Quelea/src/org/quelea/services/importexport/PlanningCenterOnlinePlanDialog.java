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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.quelea.services.languages.LabelGrabber;

/**
 *
 * @author Fabian
 */


public class PlanningCenterOnlinePlanDialog extends Pane {
    
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
            
            planJSON = importDialog.getParser().plan(planId);
            update();
        
        } catch (Exception e) {
            e.printStackTrace();
        }       
    }
    
    
    protected void update() {
        planView.setShowRoot(false);
        TreeItem<String> rootTreeItem = new TreeItem<String>();
        planView.setRoot(rootTreeItem);
        
        int itemIndex = 0;
        JSONArray items = (JSONArray)planJSON.get("items");
        for (Object itemObj : items) {
            JSONObject item = (JSONObject)itemObj;
            ++itemIndex;
            
            String itemType = (String)item.get("type");
            if (itemType.equals("PlanMedia")) {
                processPlanMedia(item, rootTreeItem);
            }
            else if (itemType.equals("PlanSong")) {
                processPlanSong(item, rootTreeItem);
            }
            else if (itemType.equals("PlanItem") && (boolean)item.get("using_custom_slides") == true) {
                processCustomSlides(item, rootTreeItem);
            }
        }
    }
    
    protected void processPlanMedia(JSONObject item, TreeItem<String> parentTreeItem) {
        String title = "Media: " + (String)item.get("title");
        TreeItem<String> treeItem = new TreeItem<String>(title);
        parentTreeItem.getChildren().add(treeItem);
    }
    
    protected void processPlanSong(JSONObject item, TreeItem<String> parentTreeItem) {
        String title = "Song: " + (String)item.get("title");
        TreeItem<String> treeItem = new TreeItem<String>(title);
        parentTreeItem.getChildren().add(treeItem);
    }
    
    protected void processCustomSlides(JSONObject item, TreeItem<String> parentTreeItem) {
        String title = "Custom Slides: " + (String)item.get("title") + " - " + (String)item.get("dates");
        TreeItem<String> treeItem = new TreeItem<String>(title);
        parentTreeItem.getChildren().add(treeItem);
    }
    
    @FXML private void onImportAction(ActionEvent event) {
        System.out.println("Do import");
    }
}
