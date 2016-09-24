/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.windows.main.menus;

import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.quelea.services.importexport.EasySlidesImportDialog;
import org.quelea.services.importexport.EasyWorshipImportDialog;
import org.quelea.services.importexport.EpicWorshipImportDialog;
import org.quelea.services.importexport.FreeWorshipImportDialog;
import org.quelea.services.importexport.ImportDialog;
import org.quelea.services.importexport.MediaShoutImportDialog;
import org.quelea.services.importexport.MissionPraiseImportDialog;
import org.quelea.services.importexport.OpenLPImportDialog;
import org.quelea.services.importexport.OpenLyricsImportDialog;
import org.quelea.services.importexport.OpenSongImportDialog;
import org.quelea.services.importexport.ParadoxJDBCChecker;
import org.quelea.services.importexport.PlainTextSongsImportDialog;
import org.quelea.services.importexport.PresentationManagerImportDialog;
import org.quelea.services.importexport.QSPImportDialog;
import org.quelea.services.importexport.SoFImportDialog;
import org.quelea.services.importexport.SongBeamerImportDialog;
import org.quelea.services.importexport.SongProImportDialog;
import org.quelea.services.importexport.SongSelectImportDialog;
import org.quelea.services.importexport.SourceImportDialog;
import org.quelea.services.importexport.SundayPlusImportDialog;
import org.quelea.services.importexport.SurvivorImportDialog;
import org.quelea.services.importexport.VideoPsalmImportDialog;
import org.quelea.services.importexport.ZWTurboDBChecker;
import org.quelea.services.importexport.ZionWorxImportDialog;
import org.quelea.services.importexport.PlanningCenterOnlineImportDialog;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;

/**
 * Quelea's import menu.
 * <p>
 * @author Michael
 */
public class ImportMenu extends Menu {

    private final ImportDialog sImportDialog;
    private final ImportDialog qspImportDialog;
    private final ImportDialog openSongImportDialog;
    private final ImportDialog missionPraiseImportDialog;
    private final ImportDialog openLPImportDialog;
    private final ImportDialog openLyricsImportDialog;
    private final ImportDialog zionWorxImportDialog;
    private final ImportDialog sourceImportDialog;
    private final ImportDialog plainTextImportDialog;
    private final ImportDialog easySlidesImportDialog;
    private final ImportDialog freeWorshipImportDialog;
    private final ImportDialog easyWorshipImportDialog;
    private final ImportDialog sundayPlusImportDialog;
    private final ImportDialog songSelectImportDialog;
    private final ImportDialog songproImportDialog;
    private final ImportDialog videoPsalmImportDialog;
    private final ImportDialog mediaShoutImportDialog;
    private final ImportDialog songBeamerImportDialog;
    private final ImportDialog epicWorshipImportDialog;
    private final ImportDialog presentationManagerImportDialog;
    private final ImportDialog sofImportDialog;
    private final PlanningCenterOnlineImportDialog planningCenterOnlineImportDialog;
    private final MenuItem qspItem;
    private final MenuItem osItem;
    private final MenuItem mpItem;
    private final MenuItem spItem;
    private final MenuItem olItem;
    private final MenuItem freeWorshipItem;
    private final MenuItem olpItem;
    private final MenuItem zwItem;
    private final MenuItem ssItem;
    private final MenuItem sbItem;
    private final MenuItem sofItem;
    private final MenuItem sourceItem;
    private final MenuItem pmItem;
    private final MenuItem mediaShoutItem;
    private final MenuItem plainTextItem;
    private final MenuItem easySlidesItem;
    private final MenuItem easyWorshipItem;
    private final MenuItem songproItem;
    private final MenuItem vsItem;
    private final MenuItem songSelectItem;
    private final MenuItem epicWorshipItem;
    private final MenuItem pcoItem;

    /**
     * Create the import menu.
     */
    public ImportMenu() {
        super(LabelGrabber.INSTANCE.getLabel("import.heading"), new ImageView(new Image("file:icons/left.png", 16, 16, false, true)));

        qspImportDialog = new QSPImportDialog();
        openSongImportDialog = new OpenSongImportDialog();
        missionPraiseImportDialog = new MissionPraiseImportDialog();
        openLPImportDialog = new OpenLPImportDialog();
        openLyricsImportDialog = new OpenLyricsImportDialog();
        zionWorxImportDialog = new ZionWorxImportDialog();
        sImportDialog = new SurvivorImportDialog();
        sourceImportDialog = new SourceImportDialog();
        plainTextImportDialog = new PlainTextSongsImportDialog();
        easySlidesImportDialog = new EasySlidesImportDialog();
        freeWorshipImportDialog = new FreeWorshipImportDialog();
        easyWorshipImportDialog = new EasyWorshipImportDialog();
        songproImportDialog = new SongProImportDialog();
        videoPsalmImportDialog = new VideoPsalmImportDialog();
        sundayPlusImportDialog = new SundayPlusImportDialog();
        songSelectImportDialog = new SongSelectImportDialog();
        mediaShoutImportDialog = new MediaShoutImportDialog();
        songBeamerImportDialog = new SongBeamerImportDialog();
        epicWorshipImportDialog = new EpicWorshipImportDialog();
        presentationManagerImportDialog = new PresentationManagerImportDialog();
        sofImportDialog = new SoFImportDialog();
        planningCenterOnlineImportDialog = new PlanningCenterOnlineImportDialog();

        qspItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("qsp.button"), new ImageView(new Image("file:icons/logo16.png", 16, 16, false, true)));
        qspItem.setOnAction((ActionEvent t) -> {
            qspImportDialog.show();
        });

        osItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("os.button"), new ImageView(new Image("file:icons/opensong.png", 16, 16, false, true)));
        osItem.setOnAction((ActionEvent t) -> {
            openSongImportDialog.show();
        });

        mpItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("mp.button"), new ImageView(new Image("file:icons/missionpraise.png", 16, 16, false, true)));
        mpItem.setOnAction((ActionEvent t) -> {
            missionPraiseImportDialog.show();
        });
        
        // planning center online
        pcoItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("pco.button"), new ImageView(new Image("file:icons/planningcenteronline.png", 16, 16, false, true)));
        pcoItem.setOnAction((ActionEvent t) -> {
            planningCenterOnlineImportDialog.start();
        });
        pcoItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN));

        olpItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("olp.button"), new ImageView(new Image("file:icons/openlp.png", 16, 16, false, true)));
        olpItem.setOnAction((ActionEvent t) -> {
            openLPImportDialog.show();
        });

        olItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("olyrics.button"));
        olItem.setOnAction((ActionEvent t) -> {
            openLyricsImportDialog.show();
        });

        zwItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("zw.button"), new ImageView(new Image("file:icons/zionworx.png", 16, 16, false, true)));
        zwItem.setOnAction((ActionEvent t) -> {
            boolean ok = new ZWTurboDBChecker().runChecks();
            if (ok) {
                zionWorxImportDialog.show();
            }
        });

        spItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("sp.button"), new ImageView(new Image("file:icons/sundayplus.png", 16, 16, false, true)));
        spItem.setOnAction((ActionEvent t) -> {
            sundayPlusImportDialog.show();
        });

        ssItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("ss.button"), new ImageView(new Image("file:icons/survivor.jpg", 16, 16, false, true)));
        ssItem.setOnAction((ActionEvent t) -> {
            sImportDialog.show();
        });

        songSelectItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("songselect.button"), new ImageView(new Image("file:icons/songselect.png", 16, 16, false, true)));
        songSelectItem.setOnAction((ActionEvent t) -> {
            songSelectImportDialog.show();
        });

        mediaShoutItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("mediashout.button"), new ImageView(new Image("file:icons/mediashout.png", 16, 16, false, true)));
        mediaShoutItem.setOnAction((ActionEvent t) -> {
            mediaShoutImportDialog.show();
        });

        sbItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("songbeamer.button"), new ImageView(new Image("file:icons/songbeamer.png", 16, 16, false, true)));
        sbItem.setOnAction((ActionEvent t) -> {
            songBeamerImportDialog.show();
        });

        sourceItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("source.button"), new ImageView(new Image("file:icons/source.jpg", 16, 16, false, true)));
        sourceItem.setOnAction((ActionEvent t) -> {
            sourceImportDialog.show();
        });

        easySlidesItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("easyslides.button"), new ImageView(new Image("file:icons/easyslides.png", 16, 16, false, true)));
        easySlidesItem.setOnAction((ActionEvent t) -> {
            easySlidesImportDialog.show();
        });

        freeWorshipItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("freeworship.button"), new ImageView(new Image("file:icons/freeworship.png", 16, 16, false, true)));
        freeWorshipItem.setOnAction((ActionEvent t) -> {
            freeWorshipImportDialog.show();
        });

        easyWorshipItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("easyworship.button"), new ImageView(new Image("file:icons/easyworship.png", 16, 16, false, true)));
        easyWorshipItem.setOnAction((ActionEvent t) -> {
            new ParadoxJDBCChecker().runChecks();
            easyWorshipImportDialog.show();
        });

        songproItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("songpro.button"), new ImageView(new Image("file:icons/songpro.png", 16, 16, false, true)));
        songproItem.setOnAction((ActionEvent t) -> {
            songproImportDialog.show();
        });

        vsItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("videopsalm.button"));
        vsItem.setOnAction((ActionEvent t) -> {
            videoPsalmImportDialog.show();
        });

        epicWorshipItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("epicworship.button"), new ImageView(new Image("file:icons/epicworship.png", 16, 16, false, true)));
        epicWorshipItem.setOnAction((ActionEvent t) -> {
            epicWorshipImportDialog.show();
        });

        pmItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("pm.button"), new ImageView(new Image("file:icons/pm.png", 16, 16, false, true)));
        pmItem.setOnAction((ActionEvent t) -> {
            presentationManagerImportDialog.show();
        });

        sofItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("sof.button"), new ImageView(new Image("file:icons/sof.png", 16, 16, false, true)));
        sofItem.setOnAction((ActionEvent t) -> {
            sofImportDialog.show();
        });

        plainTextItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("plainText.button"), new ImageView(new Image("file:icons/text.png", 16, 16, false, true)));
        plainTextItem.setOnAction((ActionEvent t) -> {
            plainTextImportDialog.show();
        });

        getItems().add(easySlidesItem);
        getItems().add(easyWorshipItem);
        getItems().add(epicWorshipItem);
        getItems().add(freeWorshipItem);
        getItems().add(mediaShoutItem);
        getItems().add(mpItem);
        getItems().add(olpItem);
        getItems().add(olItem);
        getItems().add(osItem);
        getItems().add(pcoItem);
        getItems().add(plainTextItem);
        getItems().add(pmItem);
        getItems().add(qspItem);
        getItems().add(sbItem);
        getItems().add(songproItem);
        getItems().add(songSelectItem);
        getItems().add(sofItem);
        getItems().add(spItem);
        getItems().add(ssItem);
        getItems().add(sourceItem);
        getItems().add(vsItem);
        if (Utils.isWindows()) {
            getItems().add(zwItem);
        }
    }
}
