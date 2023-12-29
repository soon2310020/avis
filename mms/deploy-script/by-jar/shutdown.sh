#!/bin/sh

kill `cat mms.pid`

#logrotate
#mv nohup.out ./mms-$(date '+%Y%m%d').log
touch nohup.out
