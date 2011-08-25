package org.quelea.notice;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.quelea.windows.main.LyricCanvas;

/**
 * The dialog used to manage the notices.
 * @author Michael
 */
public class NoticeDialog extends JDialog {
    
    private JButton newNoticeButton;
    private JButton removeNoticeButton;
    private JButton editNoticeButton;
    private JList<Notice> noticeList;
    private List<NoticeManager> noticeManagers;
    
    public NoticeDialog(JFrame owner) {
        super(owner, true);
        noticeManagers = new ArrayList<>();
        setTitle("Notices");
        setLayout(new BorderLayout());
        JPanel leftPanel = new JPanel();
        newNoticeButton = new JButton("New notice");
        newNoticeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Notice notice = NoticeEntry.getNotice();
                ((DefaultListModel<Notice>)noticeList.getModel()).addElement(notice);
                for(NoticeManager manager : noticeManagers) {
                    manager.addNotice(notice);
                }
            }
        });
        editNoticeButton = new JButton("Edit notice");
        removeNoticeButton = new JButton("Remove notice");
        leftPanel.setLayout(new GridLayout(3, 1));
        leftPanel.add(newNoticeButton);
        leftPanel.add(editNoticeButton);
        leftPanel.add(removeNoticeButton);
        JPanel leftPanelBorder = new JPanel();
        leftPanelBorder.setLayout(new BorderLayout());
        leftPanelBorder.add(leftPanel, BorderLayout.NORTH);
        add(leftPanelBorder, BorderLayout.WEST);
        
        noticeList = new JList<>(new DefaultListModel<Notice>());
        noticeList.setPreferredSize(new Dimension((int)noticeList.getPreferredSize().getHeight(), 50));
        add(new JScrollPane(noticeList), BorderLayout.CENTER);
        pack();
    }
    
    @Override
    public void setVisible(boolean visible) {
        setLocationRelativeTo(getOwner());
        super.setVisible(visible);
    }
    
    public void registerCanvas(LyricCanvas canvas) {
        noticeManagers.add(canvas.getNoticeManager());
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
