#!/bin/sh

iscc="${WINEPREFIX:-$HOME/.wine}/drive_c/Program Files/Inno Setup 7/ISCC.exe"
if [ ! -f "$iscc" ]; then
    echo "Error: Inno Setup 7 compiler not found at $iscc." >&2
    echo "Install Inno Setup 7 in the Wine prefix and try again." >&2
    exit 1
fi
if ! command -v wine >/dev/null 2>&1; then
    echo "Error: Wine is required to run the Inno Setup 7 compiler." >&2
    exit 1
fi

wine "$iscc" "/DMyAppVersion=$1" /Q quelea64.iss
