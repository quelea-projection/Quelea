package org.quelea.windows.newsong;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import org.quelea.Theme;
import org.quelea.utils.Utils;
import org.quelea.display.Song;

/**
 * A new song window that users use for inserting the text content of a new
 * song.
 * @author Michael
 */
public class SongEntryWindow extends JDialog {

    private BasicSongPanel basicSongPanel;
    private DetailedSongPanel detailedSongPanel;
    private ThemePanel themePanel;
    private final JTabbedPane tabbedPane;
    private final JButton confirmButton;
    private final JButton cancelButton;
    private Song song;

    /**
     * Create and initialise the new song window.
     * @param owner the owner of this window.
     */
    public SongEntryWindow(JFrame owner) {
        super(owner, "Song entry");
        setResizable(false);
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        setupBasicSongPanel();
        tabbedPane.add(basicSongPanel);
        setupDetailedSongPanel();
        tabbedPane.add(detailedSongPanel);
        tabbedPane.setEnabledAt(1, false);
        setupThemePanel();
        tabbedPane.add(themePanel);
        add(tabbedPane, BorderLayout.CENTER);

        confirmButton = new JButton("Add Song", Utils.getImageIcon("icons/tick.png"));
        cancelButton = new JButton("Cancel", Utils.getImageIcon("icons/cross.png"));
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(confirmButton);
        bottomPanel.add(cancelButton);
        add(bottomPanel, BorderLayout.SOUTH);
        pack();
    }

    /**
     * Called by the constructor to initialise the theme panel.
     */
    private void setupThemePanel() {
        themePanel = new ThemePanel();
    }

    /**
     * Called by the constructor to initialise the detailed song panel.
     */
    private void setupDetailedSongPanel() {
        detailedSongPanel = new DetailedSongPanel();
    }

    /**
     * Called by the constructor to initialise the basic song panel.
     */
    private void setupBasicSongPanel() {
        basicSongPanel = new BasicSongPanel();
        basicSongPanel.getLyricsField().addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                checkHighlight();
                checkConfirmButton();
            }

            public void keyPressed(KeyEvent e) {
                checkHighlight();
                checkConfirmButton();
            }

            public void keyReleased(KeyEvent e) {
                checkHighlight();
                checkConfirmButton();
            }

            private void checkHighlight() {
                //TODO: Highlighting
            }
        });
        basicSongPanel.getTitleField().addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                checkConfirmButton();
            }

            public void keyPressed(KeyEvent e) {
                checkConfirmButton();
            }

            public void keyReleased(KeyEvent e) {
                checkConfirmButton();
            }
        });
    }

    /**
     * Get the confirm button on the new song window.
     * @return the confirm button.
     */
    public JButton getConfirmButton() {
        return confirmButton;
    }

    /**
     * Get the cancel button on the new song window.
     * @return the cancel button.
     */
    public JButton getCancelButton() {
        return cancelButton;
    }

    /**
     * Get the panel where the user enters the basic song information.
     * @return the basic song panel.
     */
    public BasicSongPanel getBasicSongPanel() {
        return basicSongPanel;
    }

    /**
     * Get the theme currently displayed on this window.
     * @return the current theme.
     */
    public Theme getTheme() {
        return themePanel.getTheme();
    }

    /**
     * Set this window up ready to enter a new song.
     */
    public void resetNewSong() {
        setTitle("New song");
        song = null;
        confirmButton.setText("Add new song");
        confirmButton.setEnabled(false);
        basicSongPanel.resetNewSong();
        themePanel.setTheme(Theme.DEFAULT_THEME);
        tabbedPane.setSelectedIndex(0);
    }

    /**
     * Set this window up ready to edit an existing song.
     * @param song the song to edit.
     */
    public void resetEditSong(Song song) {
        setTitle("Edit song");
        this.song = song;
        confirmButton.setText("Edit song");
        confirmButton.setEnabled(true);
        basicSongPanel.resetEditSong(song);
        if (song.getSections().length > 0) {
            themePanel.setTheme(song.getSections()[0].getTheme());
        }
        tabbedPane.setSelectedIndex(0);
    }

    /**
     * Get the song that's been edited or created by the window.
     * @return the song.
     */
    public Song getSong() {
        if (song == null) {
            song = new Song(getBasicSongPanel().getTitleField().getText(), getBasicSongPanel().getAuthorField().getText());
        }
        song.setLyrics(getBasicSongPanel().getLyricsField().getText());
        song.setTitle(getBasicSongPanel().getTitleField().getText());
        song.setAuthor(getBasicSongPanel().getAuthorField().getText());
        return song;
    }

    /**
     * Centre this window on its owner.
     */
    public void centreOnOwner() {
        setLocation((getOwner().getX() + getOwner().getWidth() / 2) - getWidth() / 2, (getOwner().getY() + getOwner().getHeight() / 2) - getHeight() / 2);
    }

    /**
     * Check whether the confirm button should be enabled or disabled and set
     * it accordingly.
     */
    private void checkConfirmButton() {
        if (getBasicSongPanel().getLyricsField().getText().trim().equals("")
                || getBasicSongPanel().getTitleField().getText().trim().equals("")) {
            confirmButton.setEnabled(false);
        }
        else {
            confirmButton.setEnabled(true);
        }
    }
}
