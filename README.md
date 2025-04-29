# 贴吧签到助手 Docker版

本项目是基于[TiebaSignIn](https://github.com/LuoSue/TiebaSignIn-1)的重构版本，采用本地部署方式，支持在Docker中运行。

## 使用方法

### 方法一：直接运行（需要Java环境）

1. 确保已安装JDK 8或更高版本
2. 克隆本仓库到本地
3. 使用Maven构建项目：`mvn clean package`
4. 运行生成的JAR文件：`java -jar target/TiebaSignIn-Docker-1.0-SNAPSHOT.jar`
5. 访问 `http://localhost:11111` 进行配置

### 方法二：Docker部署（推荐）

1. 确保已安装Docker和Docker Compose
2. 克隆本仓库到本地
3. 在项目根目录下运行：`docker-compose up -d`
4. 访问 `http://localhost:11111` 进行配置

## 配置说明

### 1. BDUSS获取方法

1. 在浏览器中登录百度贴吧
2. 按F12打开开发者工具，切换到"Network"选项卡
3. 刷新页面，在请求中找到任意一个百度域名的请求
4. 查看Cookie，找到名为"BDUSS"的值并复制

### 2. Bark推送配置

1. 在手机上安装Bark应用
2. 获取你的推送URL，格式为：`https://api.day.app/你的KEY`
3. 在Web界面中设置Bark服务器和Key
4. 开启推送通知开关

## 数据存储

- 所有配置和签到记录存储在`./data`目录下的H2数据库中
- 日志文件保存在`./logs`目录下

## 定时任务

- 默认每天凌晨2点自动执行签到
- 也可以在Web界面手动触发签到

## 贡献

欢迎提交Issue和Pull Request来帮助完善本项目。 