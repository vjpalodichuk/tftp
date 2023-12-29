@ECHO OFF
@ECHO Running FileReceiver...
.\gradlew sender --args %1
pause
exit
@ECHO done.
