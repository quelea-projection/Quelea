package org.quelea.windows.main;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.LineBorder;
import org.quelea.Theme;
import org.quelea.displayable.Displayable;
import org.quelea.displayable.TextDisplayable;
import org.quelea.displayable.TextSection;
import org.quelea.utils.FadeWindow;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;

/**
 *
 * @author Michael
 */
public class ScheduleThemePopupWindow extends FadeWindow {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private JPanel contentPanel;
    private Theme tempTheme;
    private ScheduleList schedule;

    public ScheduleThemePopupWindow(final ScheduleList schedule) {
        setSpeed(0.06f);
        this.schedule = schedule;
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(1, 1, 0, 0));
        contentPanel.setBorder(new LineBorder(Color.BLACK, 1));
        refresh();
        add(contentPanel);
    }

    public Theme getTempTheme() {
        return tempTheme;
    }

    public void updateTheme() {
        setTheme(tempTheme);
    }

    public final void refresh() {
        List<Theme> themes = getThemes();
        themes.add(null);
        ButtonGroup group = new ButtonGroup();
        contentPanel.removeAll();
        contentPanel.setLayout(new GridLayout(1, themes.size(), 5, 5));
        for (final Theme theme : themes) {
            ThemePreviewPanel panel = new ThemePreviewPanel(theme);
            panel.getSelectButton().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    tempTheme = theme;
                    setTheme(theme);
                }
            });
            group.add(panel.getSelectButton());
            contentPanel.add(panel);
        }
    }

    private List<Theme> getThemes() {
        List<Theme> themesList = new ArrayList<>();
        File themeDir = new File(QueleaProperties.getQueleaUserHome(), "themes");
        if (!themeDir.exists()) {
            themeDir.mkdir();
        }
        for (File file : themeDir.listFiles()) {
            if (file.getName().endsWith(".th")) {
                final Theme theme = Theme.parseDBString(Utils.getTextFromFile(file.getAbsolutePath(), ""));
                if (theme.equals(Theme.DEFAULT_THEME)) {
                    LOGGER.log(Level.WARNING, "Error parsing theme file: {0}", file.getAbsolutePath());
                    continue;  //error
                }
                themesList.add(theme);
            }
        }
        return themesList;
    }

    private void setTheme(Theme theme) {
        if (schedule == null) {
            LOGGER.log(Level.WARNING, "Null schedule, not setting theme");
            return;
        }
        for (int i = 0; i < schedule.getModel().getSize(); i++) {
            Displayable displayable = schedule.getModel().get(i);
            if (displayable instanceof TextDisplayable) {
                TextDisplayable textDisplayable = (TextDisplayable) displayable;
                for (TextSection section : textDisplayable.getSections()) {
                    section.setTempTheme(theme);
                }
            }
        }
    }

    public static void main(String[] args) {
        JWindow window = new ScheduleThemePopupWindow(null);
        window.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        window.pack();
        window.setVisible(true);
    }
}
