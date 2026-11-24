#!/usr/bin/env python3
"""Build book-names.tsv for Quelea from a BibleOrgSys checkout.

Usage: build_book_names.py <BibleOrgSys checkout> <output tsv>

Each line of the output is a USFM book code followed by the names and
abbreviations BibleOrgSys knows for that book, separated by tabs. The names
come from BibleBooksCodes.xml and the BookNames files, which BibleOrgSys
publishes as public domain.
"""
import glob
import os
import sys
import xml.etree.ElementTree as ET


def main(root, out):
    data = os.path.join(root, 'BibleOrgSys', 'DataFiles')
    usfm = {}
    names = {}

    def add(code, name):
        if code and name and name.strip():
            names.setdefault(code, [])
            if name.strip() not in names[code]:
                names[code].append(name.strip())

    for entry in ET.parse(os.path.join(data, 'BibleBooksCodes.xml')).getroot().iter('BibleBookCodes'):
        bos = entry.findtext('referenceAbbreviation')
        code = (entry.findtext('USFMAbbreviation') or '').upper()
        if not code:
            continue
        usfm[bos] = code
        for name in (entry.findtext('bookNameEnglishGuide') or '').split(' / '):
            add(code, name)
        for field in ('SBLAbbreviation', 'OSISAbbreviation', 'SwordAbbreviation', 'NETBibleAbbreviation'):
            add(code, entry.findtext(field))
        for name in (entry.findtext('possibleAlternativeAbbreviations') or '').split(','):
            add(code, name)
    for path in sorted(glob.glob(os.path.join(data, 'BookNames', 'BibleBooksNames_*.xml'))):
        for entry in ET.parse(path).getroot().iter('BibleBookNames'):
            code = usfm.get(entry.get('referenceAbbreviation'))
            for field in ('defaultName', 'defaultAbbreviation'):
                add(code, entry.findtext(field))
            for name in entry.findall('inputAbbreviation'):
                add(code, name.text)
    with open(out, 'w', encoding='utf-8', newline='\n') as f:
        f.write('# Book names from BibleOrgSys (public domain), built by tools/versification/build_book_names.py\n')
        for code in sorted(names):
            f.write(code + '\t' + '\t'.join(n.replace('\t', ' ') for n in names[code]) + '\n')


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
