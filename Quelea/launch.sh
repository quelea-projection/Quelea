#!/bin/sh
cd $SNAP/jar
desktop-launch $SNAP/jar/linjre64/bin/java -DVLCJ_INITX=no -Xms1200m -Duser.dir=$SNAP/jar -Dfile.encoding=UTF-8 -Dprism.dirtyopts=false -jar $SNAP/jar/Quelea.jar --userhome=$SNAP_USER_COMMON
