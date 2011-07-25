package org.quelea.windows.main;

import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;
import org.quelea.powerpoint.PresentationSlide;
import org.quelea.utils.Utils;

/**
 *
 * @author Michael
 */
public class PowerpointList extends JList {

    public PowerpointList() {
        setBackground(Color.BLACK);
        setModel(new DefaultListModel());
        setCellRenderer(new CustomCellRenderer());
    }

    public void setSlides(PresentationSlide[] slides) {
        ((DefaultListModel) getModel()).clear();
        for (PresentationSlide slide : slides) {
            ((DefaultListModel) getModel()).addElement(slide);
        }
    }

    public BufferedImage getCurrentImage() {
        if(getSelectedValue()==null) {
            return new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        }
        return ((PresentationSlide) getSelectedValue()).getImage();
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
            ret.setIcon(new ImageIcon(Utils.resizeImage(((PresentationSlide) value).getImage(), list.getWidth() > 400 ? 390 : list.getWidth() - 10, 200)));
            return ret;
        }
    }
}