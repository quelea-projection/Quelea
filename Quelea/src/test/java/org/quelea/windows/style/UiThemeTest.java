package org.quelea.windows.style;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Interface themes")
public class UiThemeTest {
    @Test
    public void storedIdsResolveToThemesAndUnknownIdsUseLight() {
        for (UiTheme theme : UiTheme.values()) {
            Assertions.assertSame(theme, UiTheme.fromId(theme.getId()));
        }
        Assertions.assertSame(UiTheme.LIGHT, UiTheme.fromId(null));
        Assertions.assertSame(UiTheme.LIGHT, UiTheme.fromId("unknown"));
    }

    @Test
    public void explicitThemesHavePackagedStylesheets() {
        for (UiTheme theme : UiTheme.values()) {
            if (theme != UiTheme.SYSTEM) {
                Assertions.assertNotNull(getClass().getClassLoader().getResource(theme.getStylesheet()), theme.getId());
            }
        }
        Assertions.assertTrue(UiTheme.DARK.isDark());
        Assertions.assertFalse(UiTheme.LIGHT.isDark());
    }
}
