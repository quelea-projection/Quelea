package org.quelea.windows.newsong;

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import org.quelea.SpringUtilities;
import org.quelea.Utils;

/**
 * A new song window that users use for inserting the text content of a new
 * song.
 * @author Michael
 */
public class NewSongWindow extends JDialog {

    private JTextArea textArea;
    private JTextField titleField;
    private JTextField authorField;
    private JTextField[] attributes;
    private JButton confirmButton;

    /**
     * Create and initialise the new song window.
     * @param owner the owner of this window.
     */
    public NewSongWindow(JFrame owner) {
        super(owner, "Add new song");
        setLayout(new BorderLayout());
        JPanel centrePanel = new JPanel();
        centrePanel.setLayout(new BoxLayout(centrePanel, BoxLayout.Y_AXIS));

        JPanel titleAuthorPanel = new JPanel();
        titleAuthorPanel.setLayout(new BorderLayout());

        attributes = new JTextField[] {
            (titleField=new JTextField() {
                {
                    setName("Title");
                    addKeyListener(new KeyListener() {

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
            }),
            (authorField=new JTextField() {
                {
                    setName("Author");
                }
            })
        };

        JPanel topPanel = new JPanel(new SpringLayout());
        for(int i = 0; i < attributes.length; i++) {
            JLabel label = new JLabel(attributes[i].getName(), JLabel.TRAILING);
            topPanel.add(label);
            label.setLabelFor(attributes[i]);
            topPanel.add(attributes[i]);
        }
        SpringUtilities.makeCompactGrid(topPanel, attributes.length, 2, 6, 6, 6, 6);

        centrePanel.add(topPanel);
        textArea = new JTextArea(25, 50);
        textArea.addKeyListener(new KeyListener() {

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
        JScrollPane textAreaScroll = new JScrollPane(textArea);
        centrePanel.add(textAreaScroll);
        add(centrePanel, BorderLayout.CENTER);

        confirmButton = new JButton("Add Song", Utils.getImageIcon("icons/tick.png"));
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(confirmButton);
        add(bottomPanel, BorderLayout.SOUTH);

        resetContents();
        pack();
    }

    /**
     * Reset the contents of this window.
     */
    public final void resetContents() {
        for(JTextField textField : attributes) {
            textField.setText("");
        }
        textArea.setText("<Type lyrics here>");
        textArea.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        textArea.setText("");
                    }
                });
                textArea.removeFocusListener(this);
            }

            public void focusLost(FocusEvent e) {}
        });
        confirmButton.setEnabled(false);
        titleField.requestFocus();
    }

    /**
     * Check whether the confirm button should be enabled or disabled and set
     * it accordingly.
     */
    private void checkConfirmButton() {
        if(textArea.getText().trim().equals("") ||
                titleField.getText().trim().equals("")) {
            confirmButton.setEnabled(false);
        }
        else {
            confirmButton.setEnabled(true);
        }
    }

    /**
     * Centre this window on its owner.
     */
    public void centreOnOwner() {
        setLocation((getOwner().getX()+getOwner().getWidth()/2)-getWidth()/2, (getOwner().getY()+getOwner().getHeight()/2)-getHeight()/2);
    }

    /**
     * Get the confirm button on the new song window.
     * @return the confirm button.
     */
    public JButton getConfirmButton() {
        return confirmButton;
    }

    /**
     * Get the lyrics that have been typed into this window.
     * @return the lyrics the user has typed.
     */
    public String getLyrics() {
        return textArea.getText();
    }

    /**
     * Get the contents of the title field.
     * @return the contents of the title field.
     */
    public String getTitleField() {
        return titleField.getText();
    }

    /**
     * Get the contents of the author field.
     * @return the contents of the author field.
     */
    public String getAuthorField() {
        return authorField.getText();
    }

}
