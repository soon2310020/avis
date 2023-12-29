PORT=5000
PEFIX_LOG_NAME=nohup
#shuttdown
sudo fuser -n tcp -k $PORT || true
#save old logs
tar -czvf ${PEFIX_LOG_NAME}-`(date +%F_%H-%M-%S)`.gz ${PEFIX_LOG_NAME}.out

#start
nohup python3 main.py 1> ${PEFIX_LOG_NAME}.out 2>&1 &
