#!/bin/sh
set -eu

project=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
test_root=$(mktemp -d "${TMPDIR:-/tmp}/quelea-glycin-test.XXXXXX")
trap 'rm -rf "$test_root"' EXIT HUP INT TERM
snap="$test_root/snap & revision"
user_data="$test_root/user data"
common="$test_root/common data"
capture="$test_root/launch.env"
mkdir -p "$snap/app/jvm/bin" "$snap/usr/share/glycin-loaders/2+/conf.d" "$snap/usr/share/glycin-loaders/1+/conf.d" "$user_data" "$common"
cat > "$snap/app/jvm/bin/java" <<'EOF'
#!/bin/sh
printf '%s\n' "${GLYCIN_DATA_DIR-unset}" "$GST_PLUGIN_PATH" "$GST_PLUGIN_SCANNER" "$*" > "$CAPTURE"
EOF
chmod +x "$snap/app/jvm/bin/java"
cat > "$snap/usr/share/glycin-loaders/2+/conf.d/glycin-svg.conf" <<'EOF'
[loader:image/svg+xml]
Exec = /usr/libexec/glycin-loaders/2+/glycin-svg
ExposeBaseDir = true
EOF
cat > "$snap/usr/share/glycin-loaders/2+/conf.d/glycin-image-rs.conf" <<'EOF'
[loader:image/png]
Exec=/usr/libexec/glycin-loaders/2+/glycin-image-rs
EOF
cat > "$snap/usr/share/glycin-loaders/1+/conf.d/legacy.conf" <<'EOF'
[loader:image/jpeg]
Exec = /usr/libexec/glycin-loaders/1+/glycin-image-rs
EOF
env -i PATH="$PATH" SNAP="$snap" SNAP_USER_DATA="$user_data" SNAP_USER_COMMON="$common" CAPTURE="$capture" sh "$project/launch.sh"
data="$user_data/quelea-glycin"
test "$(sed -n '1p' "$capture")" = "$data"
test "$(sed -n '2p' "$capture")" = "$snap/usr/lib/x86_64-linux-gnu/gstreamer-1.0"
test "$(sed -n '3p' "$capture")" = "$snap/usr/lib/x86_64-linux-gnu/gstreamer1.0/gstreamer-1.0/gst-plugin-scanner"
grep -Fx "Exec = $snap/usr/libexec/glycin-loaders/2+/glycin-svg" "$data/glycin-loaders/2+/conf.d/glycin-svg.conf" >/dev/null
grep -Fx 'ExposeBaseDir = true' "$data/glycin-loaders/2+/conf.d/glycin-svg.conf" >/dev/null
grep -Fx "Exec=$snap/usr/libexec/glycin-loaders/2+/glycin-image-rs" "$data/glycin-loaders/2+/conf.d/glycin-image-rs.conf" >/dev/null
grep -Fx "Exec = $snap/usr/libexec/glycin-loaders/1+/glycin-image-rs" "$data/glycin-loaders/1+/conf.d/legacy.conf" >/dev/null
grep -Fx 'Exec = /usr/libexec/glycin-loaders/2+/glycin-svg' "$snap/usr/share/glycin-loaders/2+/conf.d/glycin-svg.conf" >/dev/null
grep -F -- "--userhome=$common" "$capture" >/dev/null

env -i PATH="$PATH" SNAP="$snap" SNAP_USER_DATA="$user_data" SNAP_USER_COMMON="$common" CAPTURE="$capture" GLYCIN_DATA_DIR=/explicit/override sh "$project/launch.sh"
test "$(sed -n '1p' "$capture")" = /explicit/override

empty_snap="$test_root/empty"
mkdir -p "$empty_snap/app/jvm/bin" "$empty_snap/usr/share/glycin-loaders/2+/conf.d"
cp "$snap/app/jvm/bin/java" "$empty_snap/app/jvm/bin/java"
env -i PATH="$PATH" SNAP="$empty_snap" SNAP_USER_DATA="$user_data" SNAP_USER_COMMON="$common" CAPTURE="$capture" sh "$project/launch.sh"
test "$(sed -n '1p' "$capture")" = unset
printf 'Snap Glycin launcher tests passed.\n'
