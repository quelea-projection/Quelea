package org.quelea.notice;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.quelea.windows.main.LyricCanvas;

/**
 * The dialog used to manage the notices.
 * @author Michael
 */
public class NoticeDialog extends JDialog implements NoticesChangedListener {

    private JButton newNoticeButton;
    private JButton removeNoticeButton;
    private JButton editNoticeButton;
    private JButton doneButton;
    private JList<Notice> noticeList;
    private List<NoticeDrawer> noticeDrawers;

    public NoticeDialog(JFrame owner) {
        super(owner, true);
        noticeDrawers = new ArrayList<>();
        setTitle("Notices");
        setLayout(new BorderLayout());
        JPanel leftPanel = new JPanel();
        newNoticeButton = new JButton("New notice");
        newNoticeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Notice notice = NoticeEntryDialog.getNotice(NoticeDialog.this, null);
                ((DefaultListModel<Notice>) noticeList.getModel()).addElement(notice);
                for (NoticeDrawer drawer : noticeDrawers) {
                    drawer.addNotice(notice);
                }
            }
        });
        editNoticeButton = new JButton("Edit notice");
        editNoticeButton.setEnabled(false);
        editNoticeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                NoticeEntryDialog.getNotice(NoticeDialog.this, noticeList.getSelectedValue());
                validate();
            }
        });
        removeNoticeButton = new JButton("Remove notice");
        removeNoticeButton.setEnabled(false);
        removeNoticeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Notice notice = noticeList.getSelectedValue();
                ((DefaultListModel)noticeList.getModel()).remove(noticeList.getSelectedIndex());
                for (NoticeDrawer drawer : noticeDrawers) {
                    drawer.removeNotice(notice);
                }
                noticeList.validate();
            }
        });
        leftPanel.setLayout(new GridLayout(3, 1));
        leftPanel.add(newNoticeButton);
        leftPanel.add(editNoticeButton);
        leftPanel.add(removeNoticeButton);
        JPanel leftPanelBorder = new JPanel();
        leftPanelBorder.setLayout(new BorderLayout());
        leftPanelBorder.add(leftPanel, BorderLayout.NORTH);
        add(leftPanelBorder, BorderLayout.WEST);

        noticeList = new JList<>(new DefaultListModel<Notice>());
        noticeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        noticeList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                editNoticeButton.setEnabled(noticeList.getSelectedValue() != null);
                removeNoticeButton.setEnabled(noticeList.getSelectedValue() != null);
            }
        });
        noticeList.setPreferredSize(new Dimension((int) noticeList.getPreferredSize().getHeight(), 50));
        add(new JScrollPane(noticeList), BorderLayout.CENTER);
        
        doneButton = new JButton("Done");
        doneButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        JPanel southPanel = new JPanel();
        southPanel.add(doneButton);
        add(southPanel, BorderLayout.SOUTH);
        pack();
    }

    /**
     * Called when the notice status has updated, i.e. it's removed or the 
     * counter is decremented.
     * @param notices the list of notices currently in possession by the calling 
     * canvas.
     */
    @Override
    public void noticesUpdated(List<Notice> notices) {
        ((DefaultListModel<Notice>)noticeList.getModel()).removeAllElements();
        Set<Notice> noticesSet = new HashSet<>();
        for(NoticeDrawer drawer : noticeDrawers) {
            noticesSet.addAll(drawer.getNotices());
        }
        for(Notice notice : noticesSet) {
            ((DefaultListModel<Notice>)noticeList.getModel()).addElement(notice);
        }
        validate();
    }

    @Override
    public void setVisible(boolean visible) {
        setLocationRelativeTo(getOwner());
        super.setVisible(visible);
    }

    public void registerCanvas(LyricCanvas canvas) {
        noticeDrawers.add(canvas.getNoticeDrawer());
        canvas.getNoticeDrawer().addNoticeChangedListener(this);
    }

    public static void main(String[] args) {
        NoticeDialog dialog = new NoticeDialog(null);
//        ((DefaultListModel<Notice>)dialog.noticeList.getModel()).addElement(new Notice("Hello there", 2));
//        ((DefaultListModel<Notice>)dialog.noticeList.getModel()).addElement(new Notice("Hello there 535 ", 2));
//        ((DefaultListModel<Notice>)dialog.noticeList.getModel()).addElement(new Notice("Hello there 2", 2));
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        dialog.setVisible(true);
    }
}
