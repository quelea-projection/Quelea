#!/bin/sh
cd $SNAP/jar
#desktop-launch java -Djavafx.embed.singleThread=true -DVLCJ_INITX=no -Xms1200m -Duser.dir=$SNAP/jar -Dfile.encoding=UTF-8 -Dprism.dirtyopts=false -jar $SNAP/jar/Quelea.jar --userhome=$SNAP_USER_COMMON
which java
java -version
java --add-exports=javafx.graphics/com.sun.javafx.css=ALL-UNNAMED --add-exports=javafx.base/com.sun.javafx.runtime=ALL-UNNAMED --add-exports=javafx.base/com.sun.javafx.event=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED -Djdk.gtk.verbose=true -Djdk.gtk.version=2 -DVLCJ_INITX=no -Duser.dir=$SNAP/jar -Dfile.encoding=UTF-8 -Dprism.dirtyopts=false -jar $SNAP/jar/Quelea.jar --userhome=$SNAP_USER_COMMON
#Trigger