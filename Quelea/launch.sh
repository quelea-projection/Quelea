#!/bin/sh
java -Xms1024m -Xmx1024m -jar Quelea.jar || (
echo Default Java failed, locating Java 7
/usr/bin/update-alternatives --query java | while read line
do
  if [ ! -z "$(echo $line | awk '/Alternative:/')" ]; then
    if [ ! -z "$(echo $line | awk '/java-7-openjdk')" ]; then
      line=$(echo $line | cut -d ":" -f2)
      $line -Xms1024m -Xmx1024m -jar Quelea.jar
      exit
    fi
  fi
done
)
