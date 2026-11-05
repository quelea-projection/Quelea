# Quelea UI styling

All styling of the operator-facing user interface lives in this folder.
(The projection and stage screens are *not* styled by these files.)

| File               | Purpose                                                         |
|--------------------|-----------------------------------------------------------------|
| `base.css`         | Component styling shared by every theme (buttons, lists, tabs…) |
| `icons.css`        | Vector icons, one `-fx-shape` per `.icon-<name>` class          |
| `theme-light.css`  | Colour palette for the default (light) theme                    |
| `theme-dark.css`   | Colour palette for the dark theme                               |

`org.quelea.windows.style.StyleManager` adds these stylesheets to every
window's scene, in the order above, followed by an optional user stylesheet.

## How the theming works

Theme files only define *named colours* on `.root`, for example:

```css
.root {
    -q-accent: #2563eb;
    -q-surface: #ffffff;
}
```

`base.css` never uses literal colours, only these names (`-q-accent`,
`-q-surface`, `-q-text`, …). Changing a theme therefore means changing a
handful of values in one file.

## Typography

The UI uses **Inter** (SIL Open Font License, `licenses/inter-ofl.txt`), which
is bundled in `icons/bundledfonts` and loaded at start-up. Body text uses the
`Inter` family; headings, panel titles and primary buttons use the separate
`Inter SemiBold` family. The lyrics editor uses the bundled Roboto Mono.

## Making your own tweaks without rebuilding Quelea

Create a file called `ui-style.css` in the Quelea user directory (the folder
that holds `quelea.properties`). It is loaded last, after the theme, so it can
override anything. Some examples:

```css
/* A green accent colour instead of blue */
.root {
    -q-accent: #16a34a;
    -q-accent-hover: #15803d;
    -q-accent-soft: #dcfce7;
}

/* Larger text everywhere, or a different UI font */
.root {
    -fx-font-size: 14px;
    -fx-font-family: "Noto Sans";
}

/* Swap an icon for a different SVG path. The two tiny squares at the end
   pin the 24x24 grid so the icon is not stretched. */
.icon-go-live {
    -fx-shape: "M8 5v14l11-7z M0 0h.01v.01h-.01z M23.99 23.99h.01v.01h-.01z";
}
```

Restart Quelea after editing the file.

## Icons

Icons are created in code with `QueleaIcon.SAVE.create()` (or
`create(size)`), which returns a `Region` with the style classes `icon` and
`icon-save`. The shape comes from `icons.css` and the colour from `-q-icon`,
so one icon works in both light and dark mode.

To add an icon: add a constant to `QueleaIcon` and a matching
`.icon-<name> { -fx-shape: "…"; }` rule in `icons.css`. The current icons are
[Material Symbols](https://github.com/google/material-design-icons)
(Rounded, weight 400, Apache License 2.0). Each path ends with two tiny
squares in opposite corners of the 960×960 viewBox so that all icons scale
to the same size.

Larger buttons with an icon, such as play / pause, use `MediaButton`, and
`UiEffects` holds the small animations (e.g. fading in a tab's content).
