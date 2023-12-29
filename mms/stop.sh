#!/bin/sh

kill $(ps aus | grep 'mms-*.jar' | awk '{print $2}')

