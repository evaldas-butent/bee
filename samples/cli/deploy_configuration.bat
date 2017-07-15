REM Copy config files from war directory
REM Before start run script
REM Menu Run >External tools > External tools configurations
REM Select Program of left field and press New
REM Enter Name: copy b-novo config
REM In Main tab: 
REM Enter Location: ${workspace_loc:/Bee/samples/cli/deploy_configuration.bat}
REM Enter working directory: ${workspace_loc:/Bee}
REM Enter arguments: ${workspace_loc:/Bee/war}
REM Press Apply button
REM You can run this script in External tools toolbar.


SET WAR_FOLDER=%1
SET APP_FOLDER=C:\glassfish4\glassfish\domains\domain1\applications\Bee


 xcopy %WAR_FOLDER%\css\*  %APP_FOLDER%\css /c /d /e /h /i /k /q /r /s /x /y
 xcopy %WAR_FOLDER%\images\*  %APP_FOLDER%\images /c /d /e /h /i /k /q /r /s /x /y
 xcopy %WAR_FOLDER%\js\*  %APP_FOLDER%\js /c /d /e /h /i /k /q /r /s /x /y
 xcopy %WAR_FOLDER%\WEB-INF\config\*  %APP_FOLDER%\WEB-INF\config /c /d /e /h /i /k /q /r /s /x /y
 xcopy %WAR_FOLDER%\WEB-INF\web.xml  %APP_FOLDER%\WEB-INF\web.xml /c /d /e /h /i /k /q /r /s /x /y | echo f