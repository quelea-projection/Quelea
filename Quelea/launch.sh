#!/bin/sh
cd $SNAP/jar
desktop-launch java -Xms1200m -Duser.dir=$SNAP/jar -Dfile.encoding=UTF-8 -Djavafx.embed.singleThread=true -Dprism.dirtyopts=false -jar $SNAP/jar/Quelea.jar
