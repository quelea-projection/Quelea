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
package org.quelea.windows.options;

import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.notice.NoticeDrawer.NoticePosition;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.PropertyPanel;
import org.quelea.services.utils.QueleaProperties;

/**
 * The panel that shows the notice options
 * <p/>
 * @author Michael
 */
public class OptionsNoticePanel extends GridPane implements PropertyPanel {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final ComboBox<String> noticePositionComboBox;
    private final ColorPicker noticeBackgroundColourPicker;
    private final Slider noticeSpeedSlider;
    private final Slider noticeSizeSlider;

    /**
     * Create the options bible panel.
     */
    public OptionsNoticePanel() {
        setVgap(5);
        setHgap(10);
        setPadding(new Insets(5));

        Label positionLabel = new Label(LabelGrabber.INSTANCE.getLabel("notice.position.text"));
        GridPane.setConstraints(positionLabel, 1, 1);
        getChildren().add(positionLabel);
        noticePositionComboBox = new ComboBox<>();
        noticePositionComboBox.setEditable(false);
        noticePositionComboBox.getItems().add(LabelGrabber.INSTANCE.getLabel("top.text.position"));
        noticePositionComboBox.getItems().add(LabelGrabber.INSTANCE.getLabel("bottom.text.position"));
        positionLabel.setLabelFor(noticePositionComboBox);
        GridPane.setConstraints(noticePositionComboBox, 2, 1);
        getChildren().add(noticePositionComboBox);

        Label colourLabel = new Label(LabelGrabber.INSTANCE.getLabel("notice.background.colour.text"));
        GridPane.setConstraints(colourLabel, 1, 2);
        getChildren().add(colourLabel);
        noticeBackgroundColourPicker = new ColorPicker();
        noticeBackgroundColourPicker.setStyle("-fx-color-label-visible: false ;");
        colourLabel.setLabelFor(noticeBackgroundColourPicker);
        GridPane.setConstraints(noticeBackgroundColourPicker, 2, 2);
        getChildren().add(noticeBackgroundColourPicker);

        Label speedLabel = new Label(LabelGrabber.INSTANCE.getLabel("notice.speed.text"));
        GridPane.setConstraints(speedLabel, 1, 3);
        getChildren().add(speedLabel);
        noticeSpeedSlider = new Slider(2, 20, 10);
        speedLabel.setLabelFor(noticeSpeedSlider);
        GridPane.setConstraints(noticeSpeedSlider, 2, 3);
        getChildren().add(noticeSpeedSlider);

        Label fontSize = new Label(LabelGrabber.INSTANCE.getLabel("notice.font.size"));
        GridPane.setConstraints(fontSize, 1, 4);
        getChildren().add(fontSize);
        noticeSizeSlider = new Slider(20, 100, 50);
        fontSize.setLabelFor(noticeSizeSlider);
        GridPane.setConstraints(noticeSizeSlider, 2, 4);
        getChildren().add(noticeSizeSlider);

        readProperties();
    }

    /**
     * @inheritDoc
     */
    @Override
    public final void readProperties() {
        QueleaProperties props = QueleaProperties.get();
        if (props.getNoticePosition() == NoticePosition.BOTTOM) {
            noticePositionComboBox.getSelectionModel().select(0);
        } else {
            noticePositionComboBox.getSelectionModel().select(1);
        }
        noticeBackgroundColourPicker.setValue(props.getNoticeBackgroundColour());
        noticeBackgroundColourPicker.fireEvent(new ActionEvent());
        noticeSpeedSlider.setValue(noticeSpeedSlider.getMax() - noticeSpeedSlider.getMin() - props.getNoticeSpeed());
        noticeSizeSlider.setValue(props.getNoticeFontSize());
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setProperties() {
        QueleaProperties props = QueleaProperties.get();
        if (noticePositionComboBox.getSelectionModel().getSelectedIndex() == 0) {
            props.setNoticePosition(NoticePosition.BOTTOM);
        } else {
            props.setNoticePosition(NoticePosition.TOP);
        }
        props.setNoticeBackgroundColour(noticeBackgroundColourPicker.getValue());
        props.setNoticeSpeed(noticeSpeedSlider.getMax() - (noticeSpeedSlider.getValue() + noticeSpeedSlider.getMin()));
        props.setNoticeFontSize(noticeSizeSlider.getValue());
    }

}
