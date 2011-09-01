package org.quelea.windows.main;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import org.quelea.Application;
import org.quelea.Theme;
import org.quelea.utils.Utils;
import org.quelea.windows.newsong.ThemePanel;

/**
 *
 * @author Michael
 */
public class ThemePreviewPanel extends JPanel {

    private Theme theme;
    private LyricCanvas canvas;
    private JRadioButton selectButton;
    private JButton removeButton;

    public ThemePreviewPanel(Theme theme) {
        this.theme = theme;
        if (theme != null) {
            canvas = new LyricCanvas(false);
            canvas.setTheme(theme);
            canvas.setText(ThemePanel.SAMPLE_LYRICS, new String[0]);
            canvas.setPreferredSize(new Dimension(100, 100));
        }
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        String name;
        if (theme == null) {
            name = "<html><i>Default themes</i></html>";
        }
        else {
            name = theme.getThemeName();
        }
        selectButton = new JRadioButton(name);
        if (theme != null) {
            removeButton = new JButton(Utils.getImageIcon("icons/delete.png", 16, 16));
            removeButton.setMargin(new Insets(0, 0, 0, 0));
            removeButton.setBorder(new EmptyBorder(0, 0, 0, 0));
            removeButton.setToolTipText("Remove this theme");
            removeButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    int result = JOptionPane.showConfirmDialog(Application.get().getMainWindow(), "Delete this theme?", "Confirm delete", JOptionPane.YES_NO_OPTION);
                    if (result != JOptionPane.NO_OPTION) {
                        ThemePreviewPanel.this.theme.getFile().delete();
                    }
                }
            });
        }
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        if (canvas != null) {
            canvas.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    selectButton.doClick();
                }
            });
        }
        else {
            selectButton.doClick();
        }
        buttonPanel.add(selectButton);
        if (theme != null) {
            buttonPanel.add(Box.createHorizontalGlue());
            buttonPanel.add(removeButton);
        }
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel canvasPanel = new JPanel();
        if (theme == null) {
            JLabel label = new JLabel("<html><h1>DEFAULT</h1></html>");
            canvasPanel.add(label);
            canvasPanel.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    selectButton.doClick();
                }
            });
            label.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    selectButton.doClick();
                }
            });
        }
        else {
            canvasPanel.setLayout(new GridLayout(1, 1, 0, 0));
            canvasPanel.add(canvas);
        }
        canvasPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(canvasPanel);
        add(buttonPanel);
    }

    public JRadioButton getSelectButton() {
        return selectButton;
    }

    public Theme getTheme() {
        return theme;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ThemePreviewPanel(Theme.DEFAULT_THEME));
        frame.pack();
        frame.setVisible(true);
    }
}
