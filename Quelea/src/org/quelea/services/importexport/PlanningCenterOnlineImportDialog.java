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

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
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
    
    private final PlanningCenterOnlineParser parser;
    private final PlanningCenterOnlineLoginDialog loginDialog;
    
    @FXML private TreeView serviceView;
    @FXML private TreeTableView planView;
    
    public PlanningCenterOnlineImportDialog() {
        parser = new PlanningCenterOnlineParser();
        loginDialog = new PlanningCenterOnlineLoginDialog(this, parser);
        
        initModality(Modality.APPLICATION_MODAL);
        setTitle(LabelGrabber.INSTANCE.getLabel("pco.login.import.heading"));

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setController(this);
            Parent root = loader.load(getClass().getResourceAsStream("PlanningCenterOnlineImportDialog.fxml"));
            setScene(new Scene(root));
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void start() {
        loginDialog.start();
    }
    
    protected void onLogin() {
        // update ui to retreive plans and populate the list
        updatePlans();
        show();
    }
    
    protected void updatePlans() {
        
        serviceView.setShowRoot(false);
        TreeItem<String> rootItem = new TreeItem<String>();
        serviceView.setRoot(rootItem);
        
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
            rootItem.getChildren().add(serviceItem);
            
            for (Object planObj : serviceTypePlansArray) {
                JSONObject plan = (JSONObject)planObj;
                String date = (String)plan.get("dates");
                Long id = (Long)plan.get("id");
                
                System.out.println("\tPlan: date:" + date + " id:" + id);
                
                TreeItem<String> planItem = new TreeItem<String>(date);
                serviceItem.getChildren().add(planItem);                
            }
        }
    }
}