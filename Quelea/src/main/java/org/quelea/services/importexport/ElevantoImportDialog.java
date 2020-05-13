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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;

/**
 *
 * @author Fabian Mathews
 */
public class ElevantoImportDialog extends Stage {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final Map<TreeItem<String>, ElevantoPlanDialog> treeViewItemPlanDialogMap = new HashMap<TreeItem<String>, ElevantoPlanDialog>();
    private final ElevantoParser parser;
    private final ElevantoLoginDialog loginDialog;
    
    @FXML private TreeView serviceView;
    @FXML private Pane planPane;
    @FXML private Button okButton;
    
    @SuppressWarnings("unchecked")
    public ElevantoImportDialog() {
        parser = new ElevantoParser();
        loginDialog = new ElevantoLoginDialog(this);
        
        initModality(Modality.APPLICATION_MODAL);
        setTitle(LabelGrabber.INSTANCE.getLabel("elevanto.import.heading"));

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setController(this);
            loader.setResources(LabelGrabber.INSTANCE);
            Parent root = loader.load(getClass().getResourceAsStream("PlanningCenterOnlineImportDialog.fxml"));
            Scene scene = new Scene(root);
            if (QueleaProperties.get().getUseDarkTheme()) {
                scene.getStylesheets().add("org/modena_dark.css");
            }
            setScene(scene);
                
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
            LOGGER.log(Level.WARNING, "Couldn't create planning import dialog", e);
        }
        
        centerOnScreen();
        getIcons().add(new Image("file:icons/logo-elevanto.png"));
    }
    
    public ElevantoParser getParser() {
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
        ElevantoPlanDialog planDialog = treeViewItemPlanDialogMap.get(selectedItem);
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
            JSONObject response = parser.getServices();
            processServices(response, serviceView.getRoot());
            return null;
        }
               
        public String convertToCurrentTimeZone(String p_date) {
            String convertedDate = "";
            try {
                DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date date = utcFormat.parse(p_date);

                DateFormat currentTFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                currentTFormat.setTimeZone(TimeZone.getTimeZone(getCurrentTimeZone()));

                convertedDate = currentTFormat.format(date);
            }catch (Exception e){
                // error
                LOGGER.log(Level.WARNING, "Error", e);
            }

            return convertedDate;
        }

        //get the current time zone
        public String getCurrentTimeZone(){
            TimeZone tz = Calendar.getInstance().getTimeZone();
            return tz.getID();
        }
    
        @SuppressWarnings("unchecked")
        protected void processServices(JSONObject response, TreeItem<String> parentItem)
        {
            JSONObject services = (JSONObject)response.get("services");
            JSONArray serviceJsonArray = (JSONArray)services.get("service");
            for (Object serviceTypeObj : serviceJsonArray) {
                JSONObject service = (JSONObject)serviceTypeObj;
                String serviceTypeId = (String)service.get("id");
                String serviceName = (String)service.get("name");
                
                String date = convertToCurrentTimeZone((String)service.get("date"));
                if (date != null) {
                    // grab the date, ignore the time
                    String[] dateParts = date.split(" ");
                    if (dateParts.length >= 1) {
                        date = dateParts[0];
                    }
                }
                
                JSONObject plans;
                try {
                    plans = (JSONObject)service.get("plans");
                }
                catch (Exception e) {
                    // no plans
                    continue;
                }
                
                if (plans == null) {
                    continue;
                }
                JSONArray servicePlanArray = (JSONArray)plans.get("plan");

                for (Object planObj : servicePlanArray) {
                    JSONObject plan = (JSONObject)planObj;

                    TreeItem<String> planItem = new TreeItem<String>(date + " " + serviceName);
                    parentItem.getChildren().add(planItem);
                    ElevantoPlanDialog planDialog = new ElevantoPlanDialog(ElevantoImportDialog.this, plan);
                    treeViewItemPlanDialogMap.put(planItem, planDialog);
                }
            }
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
