# Versification data

Quelea uses these files to tell which book is which in a bible file, and to
pair verses between bibles that number them differently.

## Copenhagen Alliance data

`merged_rules.json` (the sniffing rules) and `org.json`, `eng.json`,
`lxx.json`, `vul.json`, `rsc.json` and `rso.json` (the standard
versifications) are byte-for-byte copies from the Copenhagen Alliance
versification specification:

- Upstream: https://github.com/Copenhagen-Alliance/versification-specification
- Copied from the snapshot kept in BibleOrgSys
  (https://github.com/Freely-Given-org/BibleOrgSys, commit
  `6ed731f1179a3288df5768d47a27369295ddbbf2`,
  `BibleOrgSys/DataFiles/VersificationMappings/Copenhagen`), which is
  snapshot commit `519af776ab65d89a51e37f750779250f940459ef` of
  https://github.com/Freely-Given-org/versification-specification.
- The sniffing rules are derived from STEPBible's TVTMS
  (https://github.com/STEPBible/STEPBible-Data, CC BY 4.0).

The data is licensed under the Creative Commons Attribution-ShareAlike 4.0
International licence (https://creativecommons.org/licenses/by-sa/4.0/); see
`COPENHAGEN-LICENSE.md`. The files are unmodified, so any changes to them must
be shared under the same licence.

## Book names

`book-names.tsv` lists the names and abbreviations of each book by its USFM
code. It's built by `tools/versification/build_book_names.py` from
`BibleBooksCodes.xml` and the `BookNames` files in BibleOrgSys (same commit as
above), which BibleOrgSys publishes as public domain.

## Updating

Copy the files from a newer BibleOrgSys commit, rebuild `book-names.tsv`, update
the commits above and run the tests in `org.quelea.data.bible`.
