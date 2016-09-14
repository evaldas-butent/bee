#!/bin/bash
#LOCAL=war/WEB-INF/local
LOCAL=local
rm /opt/wildfly/standalone/deployments/Bee.war.deployed
sleep 5
rm -r -f -v /opt/wildfly/standalone/deployments/Bee.war
sleep 5
cp -r -v war /opt/wildfly/standalone/deployments/Bee.war
cp -rf -v $LOCAL /opt/wildfly/standalone/deployments/Bee.war/WEB-INF/
sleep 5
#/opt/wildfly/bin/jboss-cli.sh --connect --command="deploy --force /tmp/Bee.war"
touch /opt/wildfly/standalone/deployments/Bee.war.dodeploy
#/usr/bin/notify-send -i eclipse -t 10000 "$RANDOM JBooss" "application deploy end"
