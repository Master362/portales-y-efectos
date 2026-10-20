@echo off
set APP_HOME=%~dp0
set JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
if not exist "%JAR%" (
  echo Falta %JAR%. Ejecuta una vez: gradle wrapper --gradle-version 8.1.1
  exit /b 1
)
if defined JAVA_HOME ( set JAVACMD=%JAVA_HOME%\bin\java.exe ) else ( set JAVACMD=java.exe )
"%JAVACMD%" -Dorg.gradle.appname=gradlew -classpath "%JAR%" org.gradle.wrapper.GradleWrapperMain %*
