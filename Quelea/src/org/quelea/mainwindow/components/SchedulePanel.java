package org.quelea.mainwindow.components;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.quelea.Background;
import org.quelea.Utils;
import org.quelea.display.Song;
import org.quelea.display.SongSection;

/**
 * The panel displaying the schedule / order of service. Items from here are
 * loaded into the preview panel where they are viewed and then projected live.
 * Items can be added here from the library.
 * @author Michael
 */
public class SchedulePanel extends JPanel {

    private ScheduleList scheduleList;
    private JButton removeButton;
    private JButton upButton;
    private JButton downButton;
    private JToolBar toolbar;
    private JToolBar header;

    /**
     * Create and initialise the schedule panel.
     */
    public SchedulePanel() {
        setLayout(new BorderLayout());
        DefaultListModel model = new DefaultListModel();
        model.addElement(new Song("Great is thy faithfaulness", "Traditional", new Background(Utils.getImage("img/watercross.jpg"))) {
            {
                addSection(new SongSection("Verse", new String[] {"Great is thy faithfulness oh God my father", "There is no shadow of turning with thee"}));
                addSection(new SongSection("Verse", new String[] {"Thou changest not, thy compassion it fails not", "Great is thy faithfulness Lord unto me"}));
            }
        });
        model.addElement(new Song("God of Gods", "Mark") {
            {
                addSection(new SongSection("Verse", new String[] {"You are God of Gods", "King of Kings", "Ruler over the earth"}));
                addSection(new SongSection("Chorus", new String[] {"Bring to me", "Bring to me", "Bring to me you love oh Lord"}));
            }
        });
        model.addElement(new Song("Lion of Judah", "Ben") {
            {
                addSection(new SongSection("Title", new String[] {"Lyrics", "Line2"}));
            }
        });
        scheduleList = new ScheduleList(model);

        toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        removeButton = new JButton(Utils.getImageIcon("icons/remove.png"));
        removeButton.setToolTipText("Remove song");
        removeButton.setEnabled(false);
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                scheduleList.removeCurrentItem();
            }
        });

        upButton = new JButton(Utils.getImageIcon("icons/up.png"));
        upButton.setToolTipText("Move selected item up");
        upButton.setEnabled(false);
        upButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                scheduleList.moveCurrentItem(ScheduleList.Direction.UP);
            }
        });

        downButton = new JButton(Utils.getImageIcon("icons/down.png"));
        downButton.setToolTipText("Move selected item down");
        downButton.setEnabled(false);
        downButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                scheduleList.moveCurrentItem(ScheduleList.Direction.DOWN);
            }
        });

        scheduleList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if(scheduleList.getModel().getSize()==0) {
                    removeButton.setEnabled(false);
                    upButton.setEnabled(false);
                    downButton.setEnabled(false);
                }
                else {
                    removeButton.setEnabled(true);
                    upButton.setEnabled(true);
                    downButton.setEnabled(true);
                }
            }
        });

        header = new JToolBar();
        header.setFloatable(false);
        header.add(new JLabel("<html><b>Order of Service</b></html>"));

        toolbar.add(removeButton);
        toolbar.add(upButton);
        toolbar.add(downButton);

        add(header, BorderLayout.NORTH);
        add(scheduleList, BorderLayout.CENTER);
        add(toolbar, BorderLayout.EAST);
    }

    /**
     * Get the schedule list backing this panel.
     * @return the schedule list.
     */
    public ScheduleList getScheduleList() {
        return scheduleList;
    }

}
