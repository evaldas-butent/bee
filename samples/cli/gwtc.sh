#!/bin/bash
cd /media/bnovo/Bee
echo start gwt compile `date` $COMCOUNTLEFT
java -cp ./lib/*:./war/WEB-INF/lib/guava-18.0.jar:/opt/gwt-2.6.1/*:./src:./war/WEB-INF/classes -Xmx720m com.google.gwt.dev.Compiler com.butent.bee.Bee

if [ -z "${COMCOUNTLEFT}" ]; then
export COMCOUNTLEFT="####"

fi
export DEPCOUNTLEFT=${DEPCOUNTLEFT%?}
echo end deploy `date` $DEPCOUNTLEFT

free -h
cd