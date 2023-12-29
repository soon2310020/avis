#!/bin/sh

echo "============================="
echo "= MMS Deploy  - new terminal          "
echo "============================="

TARGET=/home/emoldino/mms


${TARGET}/shutdown.sh
sudo cp /home/ubuntu/mms-1.0.0.jar ${TARGET}/mms-1.0.0.jar
${TARGET}/startup.sh
