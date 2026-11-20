# Theme equality and its callers

`ThemeDTO.equals` compares the theme name and the display and saved properties:
both font families and sizes, colours and style flags, background, shadow,
text position and alignment. It deliberately excludes the `.th` file location:
two saved files can contain the same theme. `equalsIgnoreNameAndFontSize`
compares the same properties while ignoring the name and both font sizes.
Font style is represented by the separate bold and italic flags used by the
editor and lyric renderer. Shadow values compare by their contents.

Video backgrounds compare the stored location and stretch setting. Image
backgrounds compare the stored location; the soft-reference image cache is not
part of their value. Locations are compared as stored, so an absolute path and
a library-relative name are not necessarily equal even if they resolve to the
same file.

Themes are mutable. In particular, changing their name or layout changes
equality. Their hash deliberately omits those mutable fields, which is valid
because equal themes still have equal hashes and avoids changing the hash of a
song merely because its existing theme was renamed or repositioned. Unequal
themes can therefore share a hash. Do not use a hash alone to detect edits, and
avoid changing equality-relevant properties of objects used as map keys.

The main callers need different notions of identity:

* The song editor compares a saved theme snapshot with the current theme to
  detect unsaved changes, including alignment, shadow and stretch changes.
* `GlobalThemeStore` recognises the built-in song and Bible item defaults
  separately, as they have different layouts and some different style flags.
  It ignores theme names and font sizes for that comparison. When item themes
  are allowed to override the global theme, other differences are treated as
  custom and the item's theme wins.
* The schedule theme chooser restores a saved selection by normalized file
  path when both themes have files. A theme can be edited without losing its
  selection, and two equal themes in different files remain distinct choices.
  Selection of a supplied theme elsewhere in the chooser still uses its name.
* `SongDisplayable.equals` and `hashCode` include its theme, so songs that
  differ only by a theme property may now compare unequal. Schedule entries
  use their indices to distinguish occurrences. The song library's sorted
  duplicate handling uses `compareTo` (title, author and lyrics), so it still
  lists such songs once while storing both database rows.

The `stretch` setting is persisted and affects video background equality. The
timer renderer uses it, but the lyric video renderer currently does not; value
equality does not itself change rendering behavior.
