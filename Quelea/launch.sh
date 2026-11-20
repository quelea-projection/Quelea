#!/bin/sh

# Glycin's packaged loader configurations contain host-absolute executable
# paths. Inside the snap, use the helpers from this revision instead.
configure_snap_glycin() {
    [ -n "${SNAP_USER_DATA:-}" ] || return 0
    [ -z "${GLYCIN_DATA_DIR+x}" ] || return 0

    source_root="$SNAP/usr/share/glycin-loaders"
    [ -d "$source_root" ] || return 0
    data_root="$SNAP_USER_DATA/quelea-glycin"
    escaped_snap=$(printf '%s\n' "$SNAP" | sed 's/[\\&|]/\\&/g') || return 1
    found=false
    for conf in "$source_root"/*+/conf.d/*.conf; do
        [ -f "$conf" ] || continue
        version_dir=${conf%/conf.d/*}
        version=${version_dir##*/}
        target_dir="$data_root/glycin-loaders/$version/conf.d"
        mkdir -p "$target_dir" || return 1
        name=${conf##*/}
        temporary="$target_dir/.$name.$$"
        sed "s|^\([[:space:]]*Exec[[:space:]]*=[[:space:]]*\)/usr/libexec/glycin-loaders/|\1$escaped_snap/usr/libexec/glycin-loaders/|" "$conf" > "$temporary" || return 1
        mv -f "$temporary" "$target_dir/$name" || return 1
        found=true
    done
    if [ "$found" = true ]; then
        export GLYCIN_DATA_DIR="$data_root"
    fi
}

configure_snap_glycin || echo 'Unable to configure packaged Glycin image loaders.' >&2
cd "$SNAP/app" || exit 1
export GST_PLUGIN_PATH="$SNAP/usr/lib/x86_64-linux-gnu/gstreamer-1.0"
export GST_PLUGIN_SCANNER="$SNAP/usr/lib/x86_64-linux-gnu/gstreamer-1.0/gst-plugin-scanner"
export G_FILENAME_ENCODING=UTF-8
jvm/bin/java --enable-native-access=javafx.graphics,javafx.web --add-opens java.base/java.nio=ALL-UNNAMED --add-exports=javafx.graphics/com.sun.javafx.scene.traversal=ALL-UNNAMED --add-exports=javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED --add-exports=javafx.graphics/com.sun.javafx.tk.quantum=ALL-UNNAMED --add-exports=javafx.graphics/com.sun.javafx.css=ALL-UNNAMED --add-exports=javafx.base/com.sun.javafx.runtime=ALL-UNNAMED --add-exports=javafx.base/com.sun.javafx.event=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED -Djdk.gtk.verbose=true -DVLCJ_INITX=no "-Duser.dir=$SNAP/app" -Dfile.encoding=UTF-8 -Dprism.dirtyopts=false "-Djavafx.cachedir=$SNAP_USER_COMMON" -jar "$SNAP/app/Quelea.jar" "--userhome=$SNAP_USER_COMMON"
