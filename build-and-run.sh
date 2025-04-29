#!/bin/bash

# 为脚本设置错误处理
set -e

echo "===== 贴吧签到助手 Docker版构建脚本 ====="

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "错误: 未安装Docker，请先安装Docker和Docker Compose"
    exit 1
fi

# 检查Docker Compose是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "错误: 未安装Docker Compose，请先安装"
    exit 1
fi

# 检查Maven是否安装
if command -v mvn &> /dev/null; then
    # 使用Maven编译
    echo "使用Maven编译项目..."
    mvn clean package -DskipTests
elif command -v ./mvnw &> /dev/null; then
    # 使用Maven Wrapper编译
    echo "使用Maven Wrapper编译项目..."
    ./mvnw clean package -DskipTests
else
    echo "警告: 未找到Maven或Maven Wrapper，跳过编译步骤..."
    echo "如果这是第一次运行，可能会失败，请先安装Maven"
fi

# 创建必要的目录
mkdir -p data logs

# 构建Docker镜像并启动容器
echo "构建Docker镜像并启动容器..."
docker-compose up --build -d

# 检查容器是否正常运行
if [ $(docker ps -q -f name=tieba-sign) ]; then
    echo "===== 部署成功! ====="
    echo "访问 http://localhost:11111 进行配置"
else
    echo "===== 部署失败! ====="
    echo "请检查日志:"
    docker-compose logs
fi 