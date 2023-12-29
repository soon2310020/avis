sudo su
set -a
export ENV_FILE=env/.env.development
. $ENV_FILE
docker-compose up -d --build
