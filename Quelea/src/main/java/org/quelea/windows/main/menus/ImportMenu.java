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
import org.quelea.services.importexport.PlainTextSongsImportDialog;
import org.quelea.services.importexport.PresentationManagerImportDialog;
import org.quelea.services.importexport.QSPImportDialog;
import org.quelea.services.importexport.SoFImportDialog;
import org.quelea.services.importexport.SongBeamerImportDialog;
import org.quelea.services.importexport.SongProImportDialog;
import org.quelea.services.importexport.SourceImportDialog;
import org.quelea.services.importexport.SundayPlusImportDialog;
import org.quelea.services.importexport.SurvivorImportDialog;
import org.quelea.services.importexport.VideoPsalmImportDialog;
import org.quelea.services.importexport.ZWTurboDBChecker;
import org.quelea.services.importexport.ZionWorxImportDialog;
import org.quelea.services.importexport.PlanningCenterOnlineImportDialog;
import org.quelea.services.importexport.ElevantoImportDialog;
import org.quelea.services.importexport.ProPresenterImportDialog;
import org.quelea.services.importexport.ScreenMonkeyImportDialog;
import org.quelea.services.importexport.WorshipHimImportDialog;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
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
    private final ImportDialog proPresenterImportDialog;
    private final ImportDialog freeWorshipImportDialog;
    private final ImportDialog easyWorshipImportDialog;
    private final ImportDialog sundayPlusImportDialog;
    private final ImportDialog songproImportDialog;
    private final ImportDialog videoPsalmImportDialog;
    private final ImportDialog mediaShoutImportDialog;
    private final ImportDialog worshipHimImportDialog;
    private final ImportDialog songBeamerImportDialog;
    private final ImportDialog epicWorshipImportDialog;
    private final ImportDialog presentationManagerImportDialog;
    private final ImportDialog screenMonkeyImportDialog;
    private final ImportDialog sofImportDialog;
    private final PlanningCenterOnlineImportDialog planningCenterOnlineImportDialog;
    private final ElevantoImportDialog elevantoImportDialog;
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
    private final MenuItem smItem;
    private final MenuItem mediaShoutItem;
    private final MenuItem worshipHimItem;
    private final MenuItem plainTextItem;
    private final MenuItem easySlidesItem;
    private final MenuItem proPresenterItem;
    private final MenuItem easyWorshipItem;
    private final MenuItem songproItem;
    private final MenuItem vsItem;
    private final MenuItem epicWorshipItem;
    private final MenuItem pcoItem;
    private final MenuItem elevantoItem;

    /**
     * Create the import menu.
     */
    public ImportMenu() {
        super(LabelGrabber.INSTANCE.getLabel("import.heading"), new ImageView(new Image(QueleaProperties.get().getUseDarkTheme() ? "file:icons/ic-import-light.png" : "file:icons/ic-import.png", 16, 16, false, true)));

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
        proPresenterImportDialog = new ProPresenterImportDialog();
        freeWorshipImportDialog = new FreeWorshipImportDialog();
        easyWorshipImportDialog = new EasyWorshipImportDialog();
        songproImportDialog = new SongProImportDialog();
        videoPsalmImportDialog = new VideoPsalmImportDialog();
        sundayPlusImportDialog = new SundayPlusImportDialog();
        mediaShoutImportDialog = new MediaShoutImportDialog();
        worshipHimImportDialog = new WorshipHimImportDialog();
        songBeamerImportDialog = new SongBeamerImportDialog();
        epicWorshipImportDialog = new EpicWorshipImportDialog();
        presentationManagerImportDialog = new PresentationManagerImportDialog();
        screenMonkeyImportDialog = new ScreenMonkeyImportDialog();
        sofImportDialog = new SoFImportDialog();
        planningCenterOnlineImportDialog = new PlanningCenterOnlineImportDialog();
        elevantoImportDialog = new ElevantoImportDialog();

        qspItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("qsp.button"), new ImageView(new Image("file:icons/logo16.png", 16, 16, false, true)));
        qspItem.setOnAction((ActionEvent t) -> {
            qspImportDialog.show();
        });

        osItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("os.button"), new ImageView(new Image("file:icons/logo-opensong.png", 16, 16, false, true)));
        osItem.setOnAction((ActionEvent t) -> {
            openSongImportDialog.show();
        });

        mpItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("mp.button"), new ImageView(new Image("file:icons/logo-missionpraise.png", 16, 16, false, true)));
        mpItem.setOnAction((ActionEvent t) -> {
            missionPraiseImportDialog.show();
        });
        
        // planning center online
        pcoItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("pco.button"), new ImageView(new Image("file:icons/logo-planningcenteronline.png", 16, 16, false, true)));
        pcoItem.setOnAction((ActionEvent t) -> {
            planningCenterOnlineImportDialog.start();
        });
        pcoItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN));
        
        // elevanto
        elevantoItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("elevanto.button"), new ImageView(new Image("file:icons/logo-elevanto.png", 16, 16, false, true)));
        elevantoItem.setOnAction((ActionEvent t) -> {
            elevantoImportDialog.start();
        });
        elevantoItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN));

        olpItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("olp.button"), new ImageView(new Image("file:icons/logo-openlp.png", 16, 16, false, true)));
        olpItem.setOnAction((ActionEvent t) -> {
            openLPImportDialog.show();
        });

        olItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("olyrics.button"), new ImageView(new Image("file:icons/logo-openlyrics.png", 16, 16, false, true)));
        olItem.setOnAction((ActionEvent t) -> {
            openLyricsImportDialog.show();
        });

        zwItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("zw.button"), new ImageView(new Image("file:icons/logo-zionworx.png", 16, 16, false, true)));
        zwItem.setOnAction((ActionEvent t) -> {
            boolean ok = new ZWTurboDBChecker().runChecks();
            if (ok) {
                zionWorxImportDialog.show();
            }
        });

        spItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("sp.button"), new ImageView(new Image("file:icons/logo-sundayplus.png", 16, 16, false, true)));
        spItem.setOnAction((ActionEvent t) -> {
            sundayPlusImportDialog.show();
        });

        ssItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("ss.button"), new ImageView(new Image("file:icons/logo-survivor.png", 16, 16, false, true)));
        ssItem.setOnAction((ActionEvent t) -> {
            sImportDialog.show();
        });

        mediaShoutItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("mediashout.button"), new ImageView(new Image("file:icons/logo-mediashout.png", 16, 16, false, true)));
        mediaShoutItem.setOnAction((ActionEvent t) -> {
            mediaShoutImportDialog.show();
        });

        worshipHimItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("worshiphim.button"), new ImageView(new Image("file:icons/logo-worshiphim.png", 16, 16, false, true)));
        worshipHimItem.setOnAction((ActionEvent t) -> {
            worshipHimImportDialog.show();
        });

        sbItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("songbeamer.button"), new ImageView(new Image("file:icons/logo-songbeamer.png", 16, 16, false, true)));
        sbItem.setOnAction((ActionEvent t) -> {
            songBeamerImportDialog.show();
        });

        sourceItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("source.button"), new ImageView(new Image("file:icons/logo-source.png", 16, 16, false, true)));
        sourceItem.setOnAction((ActionEvent t) -> {
            sourceImportDialog.show();
        });

        easySlidesItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("easyslides.button"), new ImageView(new Image("file:icons/logo-easyslides.png", 16, 16, false, true)));
        easySlidesItem.setOnAction((ActionEvent t) -> {
            easySlidesImportDialog.show();
        });

        proPresenterItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("propresenter.button"), new ImageView(new Image("file:icons/logo-propresenter.png", 16, 16, false, true)));
        proPresenterItem.setOnAction((ActionEvent t) -> {
            proPresenterImportDialog.show();
        });

        freeWorshipItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("freeworship.button"), new ImageView(new Image("file:icons/logo-freeworship.png", 16, 16, false, true)));
        freeWorshipItem.setOnAction((ActionEvent t) -> {
            freeWorshipImportDialog.show();
        });

        easyWorshipItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("easyworship.button"), new ImageView(new Image("file:icons/logo-easyworship.png", 16, 16, false, true)));
        easyWorshipItem.setOnAction((ActionEvent t) -> {
            easyWorshipImportDialog.show();
        });

        songproItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("songpro.button"), new ImageView(new Image("file:icons/logo-songpro.png", 16, 16, false, true)));
        songproItem.setOnAction((ActionEvent t) -> {
            songproImportDialog.show();
        });

        vsItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("videopsalm.button"), new ImageView(new Image("file:icons/logo-videopsalm.png", 16, 16, false, true)));
        vsItem.setOnAction((ActionEvent t) -> {
            videoPsalmImportDialog.show();
        });

        epicWorshipItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("epicworship.button"), new ImageView(new Image("file:icons/logo-epicworship.png", 16, 16, false, true)));
        epicWorshipItem.setOnAction((ActionEvent t) -> {
            epicWorshipImportDialog.show();
        });

        pmItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("pm.button"), new ImageView(new Image("file:icons/logo-pm.png", 16, 16, false, true)));
        pmItem.setOnAction((ActionEvent t) -> {
            presentationManagerImportDialog.show();
        });

        smItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("sm.button"), new ImageView(new Image("file:icons/logo-sm.png", 16, 16, false, true)));
        smItem.setOnAction((ActionEvent t) -> {
            screenMonkeyImportDialog.show();
        });

        sofItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("sof.button"), new ImageView(new Image("file:icons/logo-sof.png", 16, 16, false, true)));
        sofItem.setOnAction((ActionEvent t) -> {
            sofImportDialog.show();
        });

        plainTextItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("plainText.button"), new ImageView(new Image("file:icons/logo-text.png", 16, 16, false, true)));
        plainTextItem.setOnAction((ActionEvent t) -> {
            plainTextImportDialog.show();
        });

        getItems().add(easySlidesItem);
        getItems().add(easyWorshipItem);
        getItems().add(elevantoItem);
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
        getItems().add(proPresenterItem);
        getItems().add(qspItem);
        getItems().add(smItem);
        getItems().add(sbItem);
        getItems().add(songproItem);
        getItems().add(sofItem);
        getItems().add(spItem);
        getItems().add(ssItem);
        getItems().add(sourceItem);
        getItems().add(vsItem);
        getItems().add(worshipHimItem);
        if (Utils.isWindows()) {
            getItems().add(zwItem);
        }
    }
}
