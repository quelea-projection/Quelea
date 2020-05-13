/*
 * This file is part of Quelea, free projection software for churches.
 *
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.windows.options;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;

import java.util.HashMap;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.options.customprefs.MobileServerPreference;

import static org.quelea.services.utils.QueleaPropertyKeys.*;

/**
 * The panel that shows the mobile lyrics and remote control options.
 * <p>
 *
 * @author Arvid
 */
public class OptionsServerSettingsPanel {
    private HashMap<Field, ObservableValue> bindings;
    private MobileServerPreference lyricsPreference;
    private MobileServerPreference remotePreference;
    private BooleanProperty useMobileLyricsProperty;
    private StringProperty lyricsPortNumberProperty;
    private StringField mobileLyricsField;
    private BooleanProperty useMobileRemoteProperty;
    private StringProperty remotePortNumberProperty;
    private StringField remoteField;
    private StringProperty passwordProperty;

    /**
     * Create the server settings panel.
     * @param bindings HashMap of bindings to setup after the dialog has been created
     */
    OptionsServerSettingsPanel(HashMap<Field, ObservableValue> bindings) {
        this.bindings = bindings;
        lyricsPreference = new MobileServerPreference(true);
        remotePreference = new MobileServerPreference(false);

        useMobileLyricsProperty = new SimpleBooleanProperty(QueleaProperties.get().getUseMobLyrics());
        lyricsPortNumberProperty = new SimpleStringProperty(String.valueOf(QueleaProperties.get().getMobLyricsPort()));
        mobileLyricsField = Field.ofStringType(lyricsPortNumberProperty).render(lyricsPreference);
        bindings.put(mobileLyricsField, useMobileLyricsProperty.not());

        useMobileRemoteProperty = new SimpleBooleanProperty(QueleaProperties.get().getUseRemoteControl());
        remotePortNumberProperty = new SimpleStringProperty(String.valueOf(QueleaProperties.get().getRemoteControlPort()));
        remoteField = Field.ofStringType(remotePortNumberProperty).render(remotePreference);
        bindings.put(remoteField, useMobileRemoteProperty.not());

        passwordProperty = new SimpleStringProperty(QueleaProperties.get().getRemoteControlPassword());
    }

    public Category getServerTab() {
        return Category.of(LabelGrabber.INSTANCE.getLabel("server.settings.heading"),
                new ImageView(new Image("file:icons/setting-ic-server.png")),
                Group.of(LabelGrabber.INSTANCE.getLabel("mobile.lyrics.heading"),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("use.mobile.lyrics.label"), useMobileLyricsProperty)
                                .customKey(useMobLyricsKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("port.number.label"), mobileLyricsField, lyricsPortNumberProperty)
                                .customKey(mobLyricsPortKey)
                ),
                Group.of(LabelGrabber.INSTANCE.getLabel("mobile.remote.heading"),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("use.remote.control.label"), useMobileRemoteProperty)
                                .customKey(useRemoteControlKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("port.number.label"), remoteField, remotePortNumberProperty)
                                .customKey(remoteControlPortKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("remote.control.password"), passwordProperty)
                                .customKey(remoteControlPasswordKey)
                )
        );
    }

    public MobileServerPreference getLyricsPreference() {
        return lyricsPreference;
    }

    public MobileServerPreference getRemotePreference() {
        return remotePreference;
    }

}
