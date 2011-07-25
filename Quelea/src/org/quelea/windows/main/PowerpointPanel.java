package org.quelea.windows.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.quelea.Background;
import org.quelea.Theme;
import org.quelea.displayable.PresentationDisplayable;
import org.quelea.powerpoint.PresentationSlide;

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
                for(LyricCanvas lc : containerPanel.getCanvases()) {
                    lc.setText(new String[]{});
                    lc.setTheme(new Theme(null, Color.yellow, new Background(null, powerpointList.getCurrentImage())));
                }
            }
        });
        JScrollPane scroll = new JScrollPane(powerpointList);
        add(scroll, BorderLayout.CENTER);
    }
    
    public void setDisplayable(PresentationDisplayable displayable) {
        PresentationSlide[] slides = displayable.getPresentation().getSlides();
        DefaultListModel model = (DefaultListModel) (powerpointList.getModel());
        model.clear();
        for(PresentationSlide slide : slides) {
            model.addElement(slide);
        }
    }
    
    public static void main(String[] args) {
        final PowerpointPanel panel = new PowerpointPanel(null);
        final PresentationDisplayable presentation = new PresentationDisplayable(new File("C:\\java.ppt"));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame();
                frame.setLayout(new BorderLayout());
                frame.add(panel, BorderLayout.CENTER);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
                panel.setDisplayable(presentation);
            }
        });
    }

    @Override
    public void focus() {
        //TODO: Something
    }

    @Override
    public void clear() {
        //Doesn't really apply
    }
}
