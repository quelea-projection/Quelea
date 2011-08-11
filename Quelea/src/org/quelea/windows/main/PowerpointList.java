package org.quelea.windows.main;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;
import org.quelea.powerpoint.PresentationSlide;
import org.quelea.utils.QueleaProperties;

/**
 * A JList for specifically displaying powerpoint slides.
 * @author Michael
 */
public class PowerpointList extends JList<PresentationSlide> {

    private Color originalSelectionColour;

    public PowerpointList() {
        setModel(new DefaultListModel<PresentationSlide>());
        setCellRenderer(new CustomCellRenderer());
        originalSelectionColour = getSelectionBackground();
        addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                if (getModel().getSize() > 0) {
                    setSelectionBackground(QueleaProperties.get().getActiveSelectionColor());
                }
            }

            public void focusLost(FocusEvent e) {
                setSelectionBackground(originalSelectionColour);
            }
        });
    }

    public void setSlides(PresentationSlide[] slides) {
        DefaultListModel<PresentationSlide> model = (DefaultListModel<PresentationSlide>)getModel();
        model.clear();
        for (PresentationSlide slide : slides) {
            model.addElement(slide);
        }
    }

    public BufferedImage getCurrentImage(int width, int height) {
        if (getSelectedValue() == null) {
            return new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        }
        return getModel().getElementAt(getSelectedIndex()).getImage(width, height);
    }

    /**
     * The custom cell renderer for the JList behind the panel.
     */
    private static class CustomCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            CustomCellRenderer ret = (CustomCellRenderer) super.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);
            ret.setBorder(new EmptyBorder(10, 5, 10, 5));
            ret.setIcon(new ImageIcon(((PresentationSlide) value).getImage( list.getWidth() > 400 ? 390 : list.getWidth() - 10, 200)));
            return ret;
        }
    }
}
