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

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
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
import org.quelea.planningcenter.PlanningCenterClient;
import org.quelea.planningcenter.auth.AuthToken;
import org.quelea.planningcenter.model.services.Folder;
import org.quelea.planningcenter.model.services.Item;
import org.quelea.planningcenter.model.services.Plan;
import org.quelea.planningcenter.model.services.ServiceType;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.QueleaApp;

/**
 * @author Bronson
 */
public class PlanningCenterOnlineImportDialog extends Stage {

    public static final DateTimeFormatter STANDARD_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final Map<TreeItem<String>, PlanningCenterOnlinePlanDialog> treeViewItemPlanDialogMap = new HashMap<TreeItem<String>, PlanningCenterOnlinePlanDialog>();
    private final PlanningCenterOnlineParser parser;
    private final PlanningCenterAuthenticator authenticator;

    @FXML
    private TreeView serviceView;
    @FXML
    private Pane planPane;
    @FXML
    private Button okButton;

    @SuppressWarnings("unchecked")
    public PlanningCenterOnlineImportDialog() {
        parser = new PlanningCenterOnlineParser();
        authenticator = new PlanningCenterAuthenticator();

        initModality(Modality.APPLICATION_MODAL);
        initOwner(QueleaApp.get().getMainWindow());
        setTitle(LabelGrabber.INSTANCE.getLabel("pco.import.heading"));

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
                    .addListener((ChangeListener<TreeItem<String>>) this::onServiceViewSelectedItem);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Couldn't create planning import dialog", e);
        }

        centerOnScreen();
        getIcons().add(new Image("file:icons/planningcenteronline.png"));
    }

    public PlanningCenterOnlineParser getParser() {
        return parser;
    }

    public void start() {
        String currentToken = QueleaProperties.get().getPlanningCenterRefreshToken();
        PlanningCenterClient existingClient = new PlanningCenterClient(
                new AuthToken(PlanningCenterAuthenticator.getClientDetails(), currentToken)
                        .withRefreshTokenUpdater(t -> QueleaProperties.get().setPlanningCenterRefreshToken(t))
        );
        if (currentToken == null || !existingClient.isConnected()) {
            authenticator.authenticate(token -> {
                if (token.isPresent()) {
                    QueleaProperties.get().setPlanningCenterRefreshToken(token.get().getCurrentRefreshToken());
                    parser.setClient(new PlanningCenterClient(token.get()));
                    show();
                    updatePlans();
                } else {
                    hide();
                }
            });
        } else {
            parser.setClient(existingClient);
            show();
            updatePlans();
        }
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

    @FXML
    public void onAcceptAction(ActionEvent event) {
        event.consume();
        hide();
    }

    @SuppressWarnings("unchecked")
    protected void updatePlans() {
        serviceView.setShowRoot(false);
        TreeItem<String> rootItem = new TreeItem<>();
        serviceView.setRoot(rootItem);

        UpdatePlanTask task = new UpdatePlanTask();
        new Thread(task).start();
    }

    class UpdatePlanTask extends Task<Void> {

        UpdatePlanTask() {
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Void call() throws Exception {
            try {
                processServiceTypeFolder(serviceView.getRoot());
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "Exception with parser", t);
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        protected void processServiceTypeFolder(TreeItem<String> parentItem) throws IOException {
            try {
                String orgName = parser.getPlanningCenterClient().services().api().get().execute().body().get().getName();
                List<Folder> folders = parser.getPlanningCenterClient().services().folders().api().get().execute().body().get();
                Map<String, TreeItem<String>> treeItemMap = folders.stream()
                        .collect(Collectors.toMap(Folder::getId, f -> new TreeItem<>(f.getName() == null ? orgName : f.getName())));


                for (Folder folder : folders) {
                    if (folder.getParent().isPresent()) {
                        Folder parent = folder.getParent().get();
                        treeItemMap.get(parent.getId()).getChildren().add(treeItemMap.get(folder.getId()));
                    } else {
                        parentItem.getChildren().add(treeItemMap.get(folder.getId()));
                    }
                    treeItemMap.get(folder.getId()).setExpanded(true);
                }

                List<ServiceType> serviceTypes = parser.getPlanningCenterClient().services().serviceTypes().api().get().execute().body().get();

                for (ServiceType serviceType : serviceTypes) {
                    TreeItem<String> serviceTypeItem = new TreeItem<>(serviceType.getName());
                    serviceTypeItem.setExpanded(true);

                    TreeItem<String> serviceTypeParentItem;
                    if (serviceType.getParent() == null) {
                        serviceTypeParentItem = parentItem;

                    } else {
                        serviceTypeParentItem = treeItemMap.get(serviceType.getParent().getId());
                    }
                    Platform.runLater(() -> serviceTypeParentItem.getChildren().add(serviceTypeItem));

                    int pastDays = QueleaProperties.get().getPlanningCentrePrevDays();
                    Map<String, String> planQueryMap = new HashMap<>();
                    planQueryMap.put("include", "contributors,my_schedules,plan_times,series");
                    if(pastDays>=0) {
                        planQueryMap.put("filter", "after");
                        planQueryMap.put("after", STANDARD_DATE_FORMAT.format(LocalDate.now().minusDays(QueleaProperties.get().getPlanningCentrePrevDays())));
                    }
                    List<Plan> serviceTypePlans = parser.getPlanningCenterClient().services().serviceType(serviceType.getId()).plans().api().get(planQueryMap).execute().body().get();
                    for (Plan plan : serviceTypePlans) {
                        String date = plan.getDates();
                        if (date.isEmpty() || date.equals("No dates")) {
                            continue;
                        }

                        List<Item> planItems = parser.getPlanningCenterClient().services().serviceType(plan.getServiceType().getId()).plan(plan.getId()).items().api().get().execute().body().get();

                        TreeItem<String> planItem = new TreeItem<>(date);
                        serviceTypeItem.getChildren().add(planItem);
                        PlanningCenterOnlinePlanDialog planDialog = new PlanningCenterOnlinePlanDialog(PlanningCenterOnlineImportDialog.this, plan, planItems);
                        treeViewItemPlanDialogMap.put(planItem, planDialog);
                    }

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }
}
