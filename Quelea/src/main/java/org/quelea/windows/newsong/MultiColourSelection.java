/*
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea.windows.newsong;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;
import javafx.scene.control.skin.ColorPickerSkin;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import org.quelea.services.languages.LabelGrabber;

/**
 * Lets a colour picker choose several colours, one after the other: a click
 * in the picker's palette chooses one colour as usual, and a click with
 * Ctrl (Cmd on a Mac) held down adds that colour, or takes it away again.
 * <p>
 * The picker's own value is the first colour. With several colours, the
 * swatch on the picker is split into one part per colour. While the palette
 * is open, the chosen colours are numbered with the bible version they are
 * for (the first colour is for the second version), and with Ctrl/Cmd held
 * down the square under the mouse shows the number it would get.
 * <p>
 * This reaches into the palette that JavaFX shows (its squares have the
 * style class "color-square", in grids, plus an enlarged copy of the square
 * under the mouse). If a later JavaFX builds the palette differently, the
 * picker still works for one colour.
 */
public class MultiColourSelection {

    private static final String BADGE_CLASS = "colour-order-badge";
    private final ColorPicker picker;
    private final Runnable onChange;
    private final List<Color> more = new ArrayList<>();
    private boolean updating;
    private Parent palette;
    private Rectangle swatch;
    private boolean paintingSwatch;
    /** The colour a Ctrl/Cmd-click started on, until the button is released. */
    private Color pressedColour;
    /** Whether the click event after a Ctrl/Cmd-click should be swallowed. */
    private boolean swallowClick;

    /**
     * Let the given picker choose several colours.
     *
     * @param picker the colour picker.
     * @param onChange called when the colours change.
     */
    public MultiColourSelection(ColorPicker picker, Runnable onChange) {
        this.picker = picker;
        this.onChange = onChange;
        // An ordinary choice in the palette (or the custom colour dialog)
        // means just that one colour
        picker.valueProperty().addListener((ov, oldColour, colour) -> {
            if (!updating && !more.isEmpty()) {
                more.clear();
                refresh();
                onChange.run();
            }
        });
        // The skin makes the palette when it's first needed: take it from
        // there, so it's ready before the first click
        picker.setSkin(new ColorPickerSkin(picker) {
            @Override
            protected Node getPopupContent() {
                Node content = super.getPopupContent();
                if (content instanceof Parent && content != palette) {
                    preparePalette((Parent) content);
                }
                return content;
            }

            @Override
            public Node getDisplayNode() {
                Node display = super.getDisplayNode();
                if (swatch == null) {
                    findSwatch(display);
                }
                return display;
            }
        });
        Tooltip tooltip = picker.getTooltip();
        String hint = LabelGrabber.INSTANCE.getLabel("theme.multi.colour.hint");
        picker.setTooltip(new Tooltip(tooltip == null ? hint : tooltip.getText() + "\n" + hint));
    }

    /**
     * Find the swatch on the picker (the label's graphic holds it) and keep
     * it split into the colours.
     *
     * @param node the picker's display node, or a node in it.
     */
    private void findSwatch(Node node) {
        if (node instanceof Rectangle && node.getStyleClass().contains("picker-color-rect")) {
            swatch = (Rectangle) node;
            // The skin sets the swatch to the picker's value when it changes
            swatch.fillProperty().addListener((ov, oldFill, fill) -> paintSwatch());
            paintSwatch();
            return;
        }
        if (node instanceof Labeled && ((Labeled) node).getGraphic() != null) {
            findSwatch(((Labeled) node).getGraphic());
        }
        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                if (swatch == null) {
                    findSwatch(child);
                }
            }
        }
    }

    /**
     * Get all the chosen colours.
     *
     * @return the colours, the picker's own value first.
     */
    public List<Color> getColours() {
        List<Color> ret = new ArrayList<>();
        ret.add(picker.getValue());
        ret.addAll(more);
        return ret;
    }

    /**
     * Get the colours after the first.
     *
     * @return the colours, which may be empty.
     */
    public List<Color> getMoreColours() {
        return new ArrayList<>(more);
    }

    /**
     * Set the colours after the first (the first is the picker's value.)
     *
     * @param colours the colours, or an empty list.
     */
    public void setMoreColours(List<Color> colours) {
        more.clear();
        if (colours != null) {
            for (Color colour : colours) {
                if (colour != null && !colour.equals(picker.getValue()) && !more.contains(colour)) {
                    more.add(colour);
                }
            }
        }
        refresh();
    }

    /**
     * Add a colour to the end, or take it away if it's already chosen (unless
     * it's the only one.)
     *
     * @param colour the colour that was Ctrl/Cmd-clicked.
     */
    void toggle(Color colour) {
        if (colour.equals(picker.getValue())) {
            if (more.isEmpty()) {
                return;
            }
            updating = true;
            try {
                picker.setValue(more.remove(0));
            } finally {
                updating = false;
            }
        } else if (more.contains(colour)) {
            more.remove(colour);
        } else {
            more.add(colour);
        }
        refresh();
        onChange.run();
    }

    private void refresh() {
        paintSwatch();
        if (palette != null) {
            markSquares();
        }
    }

    /**
     * Split the swatch on the picker into one part per colour, side by side.
     */
    private void paintSwatch() {
        if (swatch == null || paintingSwatch) {
            return;
        }
        List<Color> colours = getColours();
        Paint paint = colours.get(0);
        if (colours.size() > 1) {
            List<Stop> stops = new ArrayList<>();
            double part = 1.0 / colours.size();
            for (int i = 0; i < colours.size(); i++) {
                // Two stops a hair apart make a sharp edge between colours
                stops.add(new Stop(i == 0 ? 0 : i * part + 0.0001, colours.get(i)));
                stops.add(new Stop((i + 1) * part, colours.get(i)));
            }
            paint = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
        }
        if (!paint.equals(swatch.getFill())) {
            paintingSwatch = true;
            try {
                swatch.setFill(paint);
            } finally {
                paintingSwatch = false;
            }
        }
    }

    /**
     * Listen for Ctrl/Cmd-clicks on the palette's squares (before the
     * palette itself, which would choose the colour on release, or while
     * dragging over it, and close), and number the chosen colours.
     */
    private void preparePalette(Parent content) {
        palette = content;
        palette.addEventFilter(MouseEvent.ANY, e -> {
            EventType<? extends MouseEvent> type = e.getEventType();
            if (type == MouseEvent.MOUSE_PRESSED) {
                pressedColour = e.getButton() == MouseButton.PRIMARY && e.isShortcutDown()
                        ? colourAt(e.getPickResult().getIntersectedNode()) : null;
                swallowClick = pressedColour != null;
            }
            if (pressedColour != null) {
                // Until the button is released this is a Ctrl/Cmd-click,
                // even if the mouse moves a little or Ctrl/Cmd is let go
                if (type == MouseEvent.MOUSE_RELEASED) {
                    Color colour = pressedColour;
                    pressedColour = null;
                    toggle(colour);
                }
                e.consume();
            } else if (type == MouseEvent.MOUSE_CLICKED && swallowClick) {
                swallowClick = false;
                e.consume();
            }
            boolean shortcut = e.isShortcutDown();
            // After the palette has moved its enlarged square
            Platform.runLater(() -> markHoverSquare(shortcut));
        });
        palette.addEventFilter(KeyEvent.ANY, e -> markHoverSquare(e.isShortcutDown()));
        markSquares();
    }

    /**
     * Get the colour of the palette square that a node is part of.
     *
     * @param node the node that was clicked.
     * @return the colour, or null if it isn't a square with a colour.
     */
    private static Color colourAt(Node node) {
        while (node != null && !node.getStyleClass().contains("color-square")) {
            node = node.getParent();
        }
        if (!(node instanceof StackPane)) {
            return null;
        }
        for (Node child : ((StackPane) node).getChildren()) {
            if (child instanceof Rectangle && ((Rectangle) child).getFill() instanceof Color) {
                Color colour = (Color) ((Rectangle) child).getFill();
                return colour.getOpacity() > 0 ? colour : null;
            }
        }
        return null;
    }

    /**
     * Number the chosen colours in the palette's grids (only when there are
     * several.)
     */
    private void markSquares() {
        List<Color> colours = getColours();
        for (Node node : palette.lookupAll(".color-square")) {
            if (node instanceof StackPane && node.getParent() instanceof GridPane) {
                Color colour = colourAt(node);
                int index = colour == null ? -1 : colours.indexOf(colour);
                setBadge((StackPane) node, index >= 0 && colours.size() > 1 ? index : -1);
            }
        }
        markHoverSquare(false);
    }

    /**
     * Number the enlarged square under the mouse: with Ctrl/Cmd held down,
     * the number its colour has or would get; otherwise nothing, as a click
     * would choose just that colour.
     *
     * @param shortcut whether Ctrl/Cmd is held down.
     */
    private void markHoverSquare(boolean shortcut) {
        if (palette == null) {
            return;
        }
        List<Color> colours = getColours();
        for (Node node : palette.lookupAll(".color-square")) {
            if (node instanceof StackPane && !(node.getParent() instanceof GridPane)) {
                Color colour = colourAt(node);
                int index = -1;
                if (shortcut && colour != null) {
                    index = colours.indexOf(colour);
                    if (index < 0) {
                        index = colours.size();
                    }
                }
                setBadge((StackPane) node, index);
            }
        }
    }

    /**
     * Show the version number for a colour on a square.
     *
     * @param square the square.
     * @param index the colour's place in the list (0 for the first), or -1
     * for no number.
     */
    private static void setBadge(StackPane square, int index) {
        square.getChildren().removeIf(child -> child.getStyleClass().contains(BADGE_CLASS));
        if (index >= 0) {
            // Numbered like the bible versions: the first colour is for the second version
            Label badge = new Label(Integer.toString(index + 2));
            badge.getStyleClass().add(BADGE_CLASS);
            badge.setMouseTransparent(true);
            square.getChildren().add(badge);
        }
    }
}
