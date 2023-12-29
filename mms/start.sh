#!/bin/sh

# java -jar  ./build/libs/mms-1.0.0.jar --stacktrace --exclude-task test

java -jar  -Dspring.profiles.active=developer ./build/libs/mms-1.0.0.jar --exclude-task test
