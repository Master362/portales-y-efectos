#!/bin/sh
# Lanzador minimo del Gradle Wrapper. Requiere gradle/wrapper/gradle-wrapper.jar
# (ver README: se genera una vez con `gradle wrapper --gradle-version 8.1.1`).
APP_HOME=$(cd "$(dirname "$0")" && pwd -P)
JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$JAR" ]; then
  echo "Falta $JAR. Ejecuta una vez: gradle wrapper --gradle-version 8.1.1" >&2
  exit 1
fi
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then JAVACMD="$JAVA_HOME/bin/java"; else JAVACMD=java; fi
exec "$JAVACMD" -Dorg.gradle.appname=gradlew -classpath "$JAR" org.gradle.wrapper.GradleWrapperMain "$@"
