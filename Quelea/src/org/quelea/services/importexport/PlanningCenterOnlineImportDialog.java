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

import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Bronson
 */


public class PlanningCenterOnlineImportDialog extends Stage{
    
    
    private final PlanningCenterOnlineParser parser;
    private final PlanningCenterOnlineLoginDialog loginDialog;
    
    public PlanningCenterOnlineImportDialog() {
        parser = new PlanningCenterOnlineParser();
        loginDialog = new PlanningCenterOnlineLoginDialog(this, parser);
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
        JSONObject jsonMap = parser.organisation();
        JSONArray serviceTypes = (JSONArray)jsonMap.get("service_types");
        for (Object serviceTypeObj : serviceTypes) {
            JSONObject serviceType = (JSONObject)serviceTypeObj;
            Long serviceTypeId = (Long)serviceType.get("id");
            JSONObject serviceTypePlans = parser.serviceTypePlans(serviceTypeId);
            String serviceTypeName = (String)serviceType.get("name");
            JSONArray serviceTypePlansArray = (JSONArray)serviceTypePlans.get("array");
            
            System.out.println("Service type: " + serviceTypeName);
            for (Object planObj : serviceTypePlansArray) {
                JSONObject plan = (JSONObject)planObj;
                String date = (String)plan.get("dates");
                Long id = (Long)plan.get("id");
                
                System.out.println("\tPlan: date:" + date + " id:" + id);
            }
        }
    }
}
