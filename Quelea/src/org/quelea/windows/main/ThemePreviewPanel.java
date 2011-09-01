package org.quelea.windows.main;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
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
        removeButton = new JButton(Utils.getImageIcon("icons/remove.png", 16, 16));
        removeButton.setMargin(new Insets(0, 0, 0, 0));
        removeButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        removeButton.setToolTipText("Remove this theme");
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(selectButton);
        buttonPanel.add(removeButton);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel canvasPanel = new JPanel();
        if (theme == null) {
            canvasPanel.add(new JLabel("<html><h1>DEFAULT</h1></html>"));
        }
        else {
            canvasPanel.setLayout(new GridLayout(1, 1, 0, 0));
            canvasPanel.add(canvas);
        }
        canvasPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(canvasPanel);
        add(buttonPanel);
    }

    public JButton getRemoveButton() {
        return removeButton;
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
