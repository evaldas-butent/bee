del D:\wildfly-10.0.0.CR3\standalone\deployments\git.war.deployed

del /S /Q  D:\wildfly-10.0.0.CR3\standalone\deployments\git.war
rmdir /S /Q D:\wildfly-10.0.0.CR3\standalone\deployments\git.war

mkdir D:\wildfly-10.0.0.CR3\standalone\deployments\git.war
xcopy /s /e C:\Users\Programuotojas\git\bee\war D:\wildfly-10.0.0.CR3\standalone\deployments\git.war

copy /b NUL D:\wildfly-10.0.0.CR3\standalone\deployments\git.war.dodeploy

