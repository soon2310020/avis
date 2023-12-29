SCRIPTS_DIR=$(cd "$(dirname "$0")" && pwd)
WORKING_DIR=$(dirname "$SCRIPTS_DIR")
export WORKING_DIR

cd "$WORKING_DIR"/avis-car-backend && mvn clean install

mkdir -p "$WORKING_DIR"/jar

cp "$WORKING_DIR"/avis-car-backend/cloud/api-gateway/target/*.jar "$WORKING_DIR"/jar/api-gateway.jar
cp "$WORKING_DIR"/avis-car-backend/run/avis-authen-center/target/*.jar "$WORKING_DIR"/jar/avis-authen-center.jar
cp "$WORKING_DIR"/avis-car-backend/run/avis-api-service/target/*.jar "$WORKING_DIR"/jar/avis-api-service.jar
cp "$WORKING_DIR"/avis-car-backend/run/avis-mobile-api-service/target/*.jar "$WORKING_DIR"/jar/avis-mobile-api-service.jar
