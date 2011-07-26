package org.quelea.windows.main;

import java.awt.BorderLayout;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.quelea.Background;
import org.quelea.Theme;
import org.quelea.displayable.PresentationDisplayable;
import org.quelea.powerpoint.PresentationSlide;
import org.quelea.utils.Utils;

/**
 *
 * @author Michael
 */
public class PowerpointPanel extends ContainedPanel {

    private PowerpointList powerpointList;

    public PowerpointPanel(final LivePreviewPanel containerPanel) {
        setLayout(new BorderLayout());
        powerpointList = new PowerpointList();
        powerpointList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!powerpointList.getValueIsAdjusting()) {
                    for (LyricCanvas lc : containerPanel.getCanvases()) {
                        lc.setText(new String[]{});
                        BufferedImage displayImage = powerpointList.getCurrentImage(lc.getWidth(), lc.getHeight());
                        lc.setTheme(new Theme(null, null, new Background(null, displayImage)));
                    }
                }
            }
        });
        JScrollPane scroll = new JScrollPane(powerpointList);
        add(scroll, BorderLayout.CENTER);
    }

    public void setDisplayable(PresentationDisplayable displayable, int index) {
        PresentationSlide[] slides = displayable.getPresentation().getSlides();
        DefaultListModel model = (DefaultListModel) (powerpointList.getModel());
        model.clear();
        for (PresentationSlide slide : slides) {
            model.addElement(slide);
        }
        powerpointList.setSelectedIndex(index);
        if(powerpointList.getSelectedIndex()==-1) {
            powerpointList.setSelectedIndex(0);
        }
        powerpointList.ensureIndexIsVisible(powerpointList.getSelectedIndex());
    }

    public int getIndex() {
        return powerpointList.getSelectedIndex();
    }
    
    @Override
    public void addKeyListener(KeyListener l) {
        super.addKeyListener(l);
        powerpointList.addKeyListener(l);
    }

    @Override
    public void focus() {
        powerpointList.requestFocus();
    }

    @Override
    public void clear() {
        //Doesn't really apply
    }

//    public static void main(String[] args) {
//        final PowerpointPanel panel = new PowerpointPanel(null);
//        final PresentationDisplayable presentation = new PresentationDisplayable(new File("C:\\java.ppt"));
//        SwingUtilities.invokeLater(new Runnable() {
//
//            public void run() {
//                JFrame frame = new JFrame();
//                frame.setLayout(new BorderLayout());
//                frame.add(panel, BorderLayout.CENTER);
//                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                frame.pack();
//                frame.setVisible(true);
//                panel.setDisplayable(presentation);
//            }
//        });
//    }
}
