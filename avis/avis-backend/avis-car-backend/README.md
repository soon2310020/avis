Profile Local:

To build: run command : mvn clean install
To start: Start application step by step
1. Start cloud/config-app(Profile is local)
2. Start cloud/service-discovery-app(Profile Local)
3. Start cloud/api-gateway-app(Profile local)
4. Start run/authen-author-service(Profile local)
5. Start run/avis-mobile-api-service(Profile local, jdbc_security)
To check service discovery is correct and all of service to register -> Go to: http://localhost:8761/
To call API via API-Gateway: API-Gateway start on port 8890
Need to map with service(see api-service.yml tag zuul/routes): http://localhost:8890/api/auth/signup