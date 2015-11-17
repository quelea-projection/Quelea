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
import org.quelea.services.importexport.EasySlidesImportDialog;
import org.quelea.services.importexport.EasyWorshipImportDialog;
import org.quelea.services.importexport.EpicWorshipImportDialog;
import org.quelea.services.importexport.ImportDialog;
import org.quelea.services.importexport.KingswayImportDialog;
import org.quelea.services.importexport.MediaShoutImportDialog;
import org.quelea.services.importexport.OpenLPImportDialog;
import org.quelea.services.importexport.OpenLyricsImportDialog;
import org.quelea.services.importexport.OpenSongImportDialog;
import org.quelea.services.importexport.ParadoxJDBCChecker;
import org.quelea.services.importexport.PlainTextSongsImportDialog;
import org.quelea.services.importexport.QSPImportDialog;
import org.quelea.services.importexport.SoFImportDialog;
import org.quelea.services.importexport.SongBeamerImportDialog;
import org.quelea.services.importexport.SongProImportDialog;
import org.quelea.services.importexport.SongSelectImportDialog;
import org.quelea.services.importexport.SourceImportDialog;
import org.quelea.services.importexport.SundayPlusImportDialog;
import org.quelea.services.importexport.SurvivorImportDialog;
import org.quelea.services.importexport.ZWTurboDBChecker;
import org.quelea.services.importexport.ZionWorxImportDialog;
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
    private final ImportDialog openLPImportDialog;
    private final ImportDialog openLyricsImportDialog;
    private final ImportDialog zionWorxImportDialog;
    private final ImportDialog sourceImportDialog;
    private final ImportDialog kingswayImportDialog;
    private final ImportDialog plainTextImportDialog;
    private final ImportDialog easySlidesImportDialog;
    private final ImportDialog easyWorshipImportDialog;
    private final ImportDialog sundayPlusImportDialog;
    private final ImportDialog songSelectImportDialog;
    private final ImportDialog songproImportDialog;
    private final ImportDialog mediaShoutImportDialog;
    private final ImportDialog songBeamerImportDialog;
    private final ImportDialog epicWorshipImportDialog;
    private final ImportDialog sofImportDialog;
    private final MenuItem qspItem;
    private final MenuItem osItem;
    private final MenuItem spItem;
    private final MenuItem olItem;
    private final MenuItem olpItem;
    private final MenuItem zwItem;
    private final MenuItem ssItem;
    private final MenuItem sbItem;
    private final MenuItem sofItem;
    private final MenuItem sourceItem;
    private final MenuItem mediaShoutItem;
    private final MenuItem plainTextItem;
    private final MenuItem easySlidesItem;
    private final MenuItem easyWorshipItem;
    private final MenuItem songproItem;
    private final MenuItem songSelectItem;
    private final MenuItem epicWorshipItem;
    private final Menu kingswayItem;

    /**
     * Create the import menu.
     */
    public ImportMenu() {
        super(LabelGrabber.INSTANCE.getLabel("import.heading"), new ImageView(new Image("file:icons/left.png", 16, 16, false, true)));

        qspImportDialog = new QSPImportDialog();
        openSongImportDialog = new OpenSongImportDialog();
        openLPImportDialog = new OpenLPImportDialog();
        openLyricsImportDialog = new OpenLyricsImportDialog();
        zionWorxImportDialog = new ZionWorxImportDialog();
        sImportDialog = new SurvivorImportDialog();
        sourceImportDialog = new SourceImportDialog();
        kingswayImportDialog = new KingswayImportDialog(null);
        plainTextImportDialog = new PlainTextSongsImportDialog();
        easySlidesImportDialog = new EasySlidesImportDialog();
        easyWorshipImportDialog = new EasyWorshipImportDialog();
        songproImportDialog = new SongProImportDialog();
        sundayPlusImportDialog = new SundayPlusImportDialog();
        songSelectImportDialog = new SongSelectImportDialog();
        mediaShoutImportDialog = new MediaShoutImportDialog();
        songBeamerImportDialog = new SongBeamerImportDialog();
        epicWorshipImportDialog = new EpicWorshipImportDialog();
        sofImportDialog = new SoFImportDialog();

        qspItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("qsp.button"), new ImageView(new Image("file:icons/logo16.png", 16, 16, false, true)));
        qspItem.setOnAction((ActionEvent t) -> {
            qspImportDialog.show();
        });
        getItems().add(qspItem);

        osItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("os.button"), new ImageView(new Image("file:icons/opensong.png", 16, 16, false, true)));
        osItem.setOnAction((ActionEvent t) -> {
            openSongImportDialog.show();
        });
        getItems().add(osItem);

        olpItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("olp.button"), new ImageView(new Image("file:icons/openlp.png", 16, 16, false, true)));
        olpItem.setOnAction((ActionEvent t) -> {
            openLPImportDialog.show();
        });
        getItems().add(olpItem);

        olItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("olyrics.button"));
        olItem.setOnAction((ActionEvent t) -> {
            openLyricsImportDialog.show();
        });
        getItems().add(olItem);

        zwItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("zw.button"), new ImageView(new Image("file:icons/zionworx.png", 16, 16, false, true)));
        zwItem.setOnAction((ActionEvent t) -> {
            boolean ok = new ZWTurboDBChecker().runChecks();
            if (ok) {
                zionWorxImportDialog.show();
            }
        });
        if (Utils.isWindows()) {
            getItems().add(zwItem);
        }

        spItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("sp.button"), new ImageView(new Image("file:icons/sundayplus.png", 16, 16, false, true)));
        spItem.setOnAction((ActionEvent t) -> {
            sundayPlusImportDialog.show();
        });
        getItems().add(spItem);

        ssItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("ss.button"), new ImageView(new Image("file:icons/survivor.jpg", 16, 16, false, true)));
        ssItem.setOnAction((ActionEvent t) -> {
            sImportDialog.show();
        });
        getItems().add(ssItem);

        songSelectItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("songselect.button"), new ImageView(new Image("file:icons/songselect.png", 16, 16, false, true)));
        songSelectItem.setOnAction((ActionEvent t) -> {
            songSelectImportDialog.show();
        });
        getItems().add(songSelectItem);

        mediaShoutItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("mediashout.button"), new ImageView(new Image("file:icons/mediashout.png", 16, 16, false, true)));
        mediaShoutItem.setOnAction((ActionEvent t) -> {
            mediaShoutImportDialog.show();
        });
        getItems().add(mediaShoutItem);

        sbItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("songbeamer.button"), new ImageView(new Image("file:icons/songbeamer.png", 16, 16, false, true)));
        sbItem.setOnAction((ActionEvent t) -> {
            songBeamerImportDialog.show();
        });
        getItems().add(sbItem);

        sourceItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("source.button"), new ImageView(new Image("file:icons/source.jpg", 16, 16, false, true)));
        sourceItem.setOnAction((ActionEvent t) -> {
            sourceImportDialog.show();
        });
        getItems().add(sourceItem);

        easySlidesItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("easyslides.button"), new ImageView(new Image("file:icons/easyslides.png", 16, 16, false, true)));
        easySlidesItem.setOnAction((ActionEvent t) -> {
            easySlidesImportDialog.show();
        });
        getItems().add(easySlidesItem);

        easyWorshipItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("easyworship.button"), new ImageView(new Image("file:icons/easyworship.png", 16, 16, false, true)));
        easyWorshipItem.setOnAction((ActionEvent t) -> {
            new ParadoxJDBCChecker().runChecks();
            easyWorshipImportDialog.show();
        });
        getItems().add(easyWorshipItem);

        songproItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("songpro.button"), new ImageView(new Image("file:icons/songpro.png", 16, 16, false, true)));
        songproItem.setOnAction((ActionEvent t) -> {
            songproImportDialog.show();
        });
        getItems().add(songproItem);
        
        epicWorshipItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("epicworship.button"), new ImageView(new Image("file:icons/epicworship.png", 16, 16, false, true)));
        epicWorshipItem.setOnAction((ActionEvent t) -> {
            epicWorshipImportDialog.show();
        });
        getItems().add(epicWorshipItem);
        
        sofItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("sof.button"), new ImageView(new Image("file:icons/sof.png", 16, 16, false, true)));
        sofItem.setOnAction((ActionEvent t) -> {
            sofImportDialog.show();
        });
        getItems().add(sofItem);

        MenuItem kingswayAll, kingswayRange, kingswayOne;

        kingswayItem = new Menu(LabelGrabber.INSTANCE.getLabel("kingsway.button"), new ImageView(new Image("file:icons/kingsway.png", 16, 16, false, true)));
        kingswayAll = new MenuItem(LabelGrabber.INSTANCE.getLabel("kingsway.button.all"), new ImageView(new Image("file:icons/kingsway.png", 16, 16, false, true)));
        kingswayAll.setOnAction((ActionEvent t) -> {
            kingswayImportDialog.setAll(true);
            kingswayImportDialog.show();
        });
        
        kingswayRange = new MenuItem(LabelGrabber.INSTANCE.getLabel("kingsway.button.range"), new ImageView(new Image("file:icons/kingsway.png", 16, 16, false, true)));
        kingswayRange.setOnAction((ActionEvent t) -> {
            kingswayImportDialog.setRange(true);
            kingswayImportDialog.show();
        });
      
        kingswayOne = new MenuItem(LabelGrabber.INSTANCE.getLabel("kingsway.button.one"), new ImageView(new Image("file:icons/kingsway.png", 16, 16, false, true)));
        kingswayOne.setOnAction((ActionEvent t) -> {
            kingswayImportDialog.setAll(false);
            kingswayImportDialog.setRange(false);
            kingswayImportDialog.show();
        });

        getItems().add(kingswayItem);
        kingswayItem.getItems().addAll(kingswayAll, kingswayRange, kingswayOne);

        plainTextItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("plainText.button"), new ImageView(new Image("file:icons/text.png", 16, 16, false, true)));
        plainTextItem.setOnAction((ActionEvent t) -> {
            plainTextImportDialog.show();
        });
        getItems().add(plainTextItem);
    }
}
