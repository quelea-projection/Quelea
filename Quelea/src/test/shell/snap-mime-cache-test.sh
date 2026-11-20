#!/bin/sh
set -eu

project=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
test_root=$(mktemp -d "${TMPDIR:-/tmp}/quelea-snap-mime.XXXXXX")
trap 'rm -rf "$test_root"' EXIT HUP INT TERM
mkdir -p "$test_root/bin" "$test_root/prime"

# Run the actual prime commands, substituting only Snapcraft's driver.
awk '
    { sub(/\r$/, "") }
    /^    override-prime: \|/ { in_prime = 1; next }
    in_prime && /^      / { sub(/^      /, ""); print; next }
    in_prime { exit }
' "$project/../snap/snapcraft.yaml" > "$test_root/prime.sh"
test -s "$test_root/prime.sh"
cat > "$test_root/bin/craftctl" <<'EOF'
#!/bin/sh
test "$1" = default
EOF
chmod +x "$test_root/bin/craftctl"
if PATH="$test_root/bin:$PATH" CRAFT_PRIME="$test_root/prime" sh "$test_root/prime.sh" > "$test_root/missing.log" 2>&1; then
    echo 'Prime unexpectedly accepted missing MIME definitions.' >&2
    exit 1
fi
grep -F 'packaged MIME type definitions are missing' "$test_root/missing.log" >/dev/null

mkdir -p "$test_root/prime/usr/share/mime/packages"
cat > "$test_root/prime/usr/share/mime/packages/test.xml" <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<mime-info xmlns="http://www.freedesktop.org/standards/shared-mime-info">
  <mime-type type="image/svg+xml"><glob pattern="*.svg"/></mime-type>
  <mime-type type="image/png"><glob pattern="*.png"/></mime-type>
</mime-info>
EOF
PATH="$test_root/bin:$PATH" CRAFT_PRIME="$test_root/prime" sh "$test_root/prime.sh"
test -s "$test_root/prime/usr/share/mime/mime.cache"
test -s "$test_root/prime/usr/share/mime/image/svg+xml.xml"
test -s "$test_root/prime/usr/share/mime/image/png.xml"
printf 'Snap MIME cache build test passed.\n'
