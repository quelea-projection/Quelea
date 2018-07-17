#!/bin/bash
wine "/home/travis/.wine/drive_c/Program Files (x86)/Inno Setup 5/iscc.exe" /dMyAppVersion=$1 quelea.iss /O /F /q"Quelea"
wine "/home/travis/.wine/drive_c/Program Files (x86)/Inno Setup 5/iscc.exe" /dMyAppVersion=$1 quelea64.iss /O /F /q"Quelea"