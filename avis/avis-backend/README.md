# AVIS Car

## Hướng dẫn deployment

### Build project

- Chạy file scripts/build.sh, các file .jar sẽ được build và copy vào thư mục jar

### Môi trường dev

- SSH vào server: ssh ubuntu@54.179.24.29 (file pem do anh Hưng cung cấp)

#### Deploy backend
- Copy các file .jar được build ở trên lên thư mục /home/ubuntu/avis/avis-backend/jar/ trên server
- Tại thư mục /home/ubuntu/avis/avis-backend, chạy lần lượt các lệnh trong file deploy.sh

#### Deploy frontend
- Copy thư mục build (do đội FE gửi) lên thư mục /home/ubuntu/avis/avis-frontend trên server (overwrite thư mục cũ nếu có)

### Môi trường production

- Thực hiện VPN để SSH được vào các server production (thông tin account VPN, SSH được cung cấp trong file riêng)
- Thông tin các service trên từng server production như sau:

| **Tên server** | **IP nội bộ** | **Các service**                         |
|:---------------|:--------------|:----------------------------------------|
| app1           | 10.10.10.11   | api-service (BE)                        |
| app2           | 10.10.10.12   | mobile-api-service, authen-service (BE) |
| apigateway     | 10.10.10.30   | api-gateway (BE) + FE + NGINX           |
| db1            | 10.10.10.21   | database                                |
| db2            | 10.10.10.22   | database (slave, hiện chưa cài)         |
| elasticsearch  | 10.10.10.40   | Cài Elasticsearch (hiện chưa dùng)      |
| fileserver     | 10.10.10.50   | FTP Server + NGINX                      |
| redisserver    | 10.10.10.60   | Redis + RabbitMQ                        |
| srs            | 10.10.10.70   | Service nhận diện ODO                   |

#### Deploy backend
- Các thông tin kết nối dến database, redis, rabbitmq,...  có trong file /opt/avis/avis-backend/env/.env.production
trên các server app1, app2, apigateway, dùng các thông tin này để cập nhật database nếu cần thiết
- Copy file .jar của mỗi service lên thư mục /opt/avis/avis-backend/jar/ trên từng server tương ứng (3 server chạy app backend)
- Tại thư mục /opt/avis/avis-backend, chạy lần lượt các lệnh trong file deploy.sh

#### Deploy frontend
- Copy thư mục build (do đội FE gửi) lên thư mục /opt/avis/avis-frontend trên server apigateway (overwrite thư mục cũ nếu có)