#!/bin/sh

[ ! -d "./build" ] && mkdir ./build

[ ! -d "./build/generated-snippets" ] && mkdir ./build/generated-snippets

./gradlew  bootjar  --exclude-task test
