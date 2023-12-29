To run project in local: 
1. Open run config
2. Fill spring profile is: local
3. Run Application class with spring boot


Call api to create user:
API: http://localhost:8890/api/auth/signup
Method: POST
{"username":"trungnt","email":"trungnt@twendie.vn","password":"avis2020","code":"Avis","name":"Nguyen Chip Chip","idCard":"13999823444","mobile":"09099999999","address":"Ha noi - Viet nam","department":"Thank god is friday","userRoleId":1,"userTypeId":1}

Call API to sign in:
API: http://localhost:8890/api/auth/signin