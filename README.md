# Cách chạy Backend ở local
## 1. Tạo các file application và .env cho Backend
### Cách 1:
- Clone repository [stride-config](https://github.com/NLNM-0-0/stride-config) xuống máy bạn
- Vào *stride-be/z-etc/bash/config-paths.env*. Sửa **CONFIG_DIR** và **SOURCE_DIR** là đường dẫn đến thư mục **stride-config** và **stride-be** của bạn

![image](https://github.com/user-attachments/assets/f8b78acf-fcbf-4d81-8900-5caf785c49f3)

- Vào thư mục *stride-be/z-etc/bash*
``` bash
cd z-etc/bash
```

- Run lệnh này trong *Git Bash* hoặc trên terminar (nếu terminal bạn hỗ trợ lệnh bash)
```
bash <<THƯ MỤC CHỨA stride-be>>/z-etc/bash/sync-config-repo.sh
```
### Cách 2:
Vào repository [stride-config](https://github.com/NLNM-0-0/stride-config) copy từng file application và .env vào từng module stride-be
- File .env đặt ở root
- File application.yml đặt ở thư mục <<TÊN MODULE>>/src/main/resources

## 2. Chạy docker-compose
```
docker-compose up
```
# Các repository liên quan
- **[stride-dto](https://github.com/NLNM-0-0/stride-dto)**: Define các DTO sử dụng cho các module
- **[stride-common](https://github.com/NLNM-0-0/stride-common)**: Define các config sử dụng nhiều giữa các module
- **[stride-config](https://github.com/NLNM-0-0/stride-config)**: Quản lý các env chạy
