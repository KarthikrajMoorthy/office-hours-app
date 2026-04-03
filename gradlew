#!/usr/bin/env sh

DIR="$(cd "$(dirname "$0")"; pwd)"
CLASSPATH="$DIR/gradle/wrapper/gradle-wrapper.jar"

JAVA_EXEC="java"

exec "$JAVA_EXEC" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
