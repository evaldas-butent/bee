#!/bin/bash
# UNIX DEPLPOY
## orka server configuration
WAR_FOLDER=/media/bnovo/Bee/war
WAR_FILE=/tmp/Bee.war
ZIP_FILE=/tmp/Bee
echo start create war `date` $DEPCOUNTLEFT
## Creating zip file due read-only /media/bnovo/ file system
cd $WAR_FOLDER
zip -r -0 -q $ZIP_FILE .

## Rename zip to war
mv $ZIP_FILE.zip $WAR_FILE
echo start deploy `date`
/opt/glassfish4/glassfish/bin/asadmin deploy --force=true --name Bee --contextroot Bee $WAR_FILE


if [ -z "${DEPCOUNTLEFT}" ]; then
export DEPCOUNTLEFT="#####"
echo restart server due deploy count `date`
/opt/glassfish4/glassfish/bin/asadmin restart-domain domain1
fi
export DEPCOUNTLEFT=${DEPCOUNTLEFT%?}
echo end deploy `date` $DEPCOUNTLEFT