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

import java.util.HashMap;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.quelea.services.languages.LabelGrabber;

/**
 *
 * @author Bronson
 */


public class PlanningCenterOnlineImportDialog extends Stage{

    private final Map<TreeItem<String>, PlanningCenterOnlinePlanDialog> treeViewItemPlanDialogMap = new HashMap<TreeItem<String>, PlanningCenterOnlinePlanDialog>();
    private final PlanningCenterOnlineParser parser;
    private final PlanningCenterOnlineLoginDialog loginDialog;
    
    @FXML private TreeView serviceView;
    @FXML private Pane planPane;
    @FXML private Button okButton;
    
    @SuppressWarnings("unchecked")
    public PlanningCenterOnlineImportDialog() {
        parser = new PlanningCenterOnlineParser();
        loginDialog = new PlanningCenterOnlineLoginDialog(this);
        
        initModality(Modality.APPLICATION_MODAL);
        setTitle(LabelGrabber.INSTANCE.getLabel("pco.import.heading"));

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setController(this);
            loader.setResources(LabelGrabber.INSTANCE);
            Parent root = loader.load(getClass().getResourceAsStream("PlanningCenterOnlineImportDialog.fxml"));
            setScene(new Scene(root));
                
            serviceView.getSelectionModel().selectedItemProperty()
            .addListener(new ChangeListener<TreeItem<String>>() {

                @Override
                public void changed(
                        ObservableValue<? extends TreeItem<String>> observable,
                        TreeItem<String> old_val, TreeItem<String> new_val) {
                    onServiceViewSelectedItem(observable, old_val, new_val);
                }

            });
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        centerOnScreen();
        getIcons().add(new Image("file:icons/planningcenteronline.png"));        
    }
    
    public PlanningCenterOnlineParser getParser() {
        return parser;
    }
    
    public void start() {
        show();
        loginDialog.start();
    }
    
    // Disable/enable appropriate widgets while a import task is in operation
    public void enablePlanProgressBars(boolean enable) {        
        // stop user being able to try to change to another plan and do bad!
        serviceView.setDisable(enable);     
        okButton.setDisable(enable);
    }
    
    protected void onServiceViewSelectedItem(ObservableValue<? extends TreeItem<String>> observable,
                        TreeItem<String> old_val, TreeItem<String> new_val) {
        TreeItem<String> selectedItem = new_val;        
        planPane.getChildren().clear();
        PlanningCenterOnlinePlanDialog planDialog = treeViewItemPlanDialogMap.get(selectedItem);
        if (planDialog != null) {
            planPane.getChildren().clear();
            planPane.getChildren().add(planDialog);
        }
    }
    
    protected void onLogin() {
        // update ui to retreive plans and populate the list
        updatePlans();
    }
    
    @FXML public void onAcceptAction(ActionEvent event) {
        event.consume();
        hide();
    }
    
    
    class UpdatePlanTask extends Task<Void> {

        UpdatePlanTask() {
        }

        @SuppressWarnings("unchecked")
        @Override 
        protected Void call() throws Exception {
            
            JSONObject jsonMap = parser.organisation();
            JSONArray serviceTypes = (JSONArray)jsonMap.get("service_types");
            for (Object serviceTypeObj : serviceTypes) {
                JSONObject serviceType = (JSONObject)serviceTypeObj;
                Long serviceTypeId = (Long)serviceType.get("id");
                JSONObject serviceTypePlans = parser.serviceTypePlans(serviceTypeId);
                String serviceTypeName = (String)serviceType.get("name");
                JSONArray serviceTypePlansArray = (JSONArray)serviceTypePlans.get("array");

                System.out.println("Service type: " + serviceTypeName);

                TreeItem<String> serviceItem = new TreeItem<String>(serviceTypeName);
                for (Object planObj : serviceTypePlansArray) {
                    JSONObject plan = (JSONObject)planObj;
                    String date = (String)plan.get("dates");
                    if (date.isEmpty() || date.equals("No dates")) {
                        continue;
                    }

                    Long id = (Long)plan.get("id");
                    System.out.println("\tPlan: date:" + date + " id:" + id);

                    TreeItem<String> planItem = new TreeItem<String>(date);
                    serviceItem.getChildren().add(planItem);
                    PlanningCenterOnlinePlanDialog planDialog = new PlanningCenterOnlinePlanDialog(PlanningCenterOnlineImportDialog.this, id);
                    treeViewItemPlanDialogMap.put(planItem, planDialog);
                }

                if (!serviceItem.getChildren().isEmpty()) {
                    serviceItem.setExpanded(true);
                    serviceView.getRoot().getChildren().add(serviceItem);
                }
            }
          
            return null;
        }
    };
        
    @SuppressWarnings("unchecked")
    protected void updatePlans() {
        
        serviceView.setShowRoot(false);
        TreeItem<String> rootItem = new TreeItem<String>();
        serviceView.setRoot(rootItem);
        
        UpdatePlanTask task = new UpdatePlanTask();
        new Thread(task).start();
    }
}
