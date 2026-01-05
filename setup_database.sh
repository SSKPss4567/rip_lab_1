#!/bin/bash

# Скрипт для создания базы данных PostgreSQL для Movie Catalog

echo "========================================="
echo "Movie Catalog - Database Setup Script"
echo "========================================="
echo ""

# Параметры по умолчанию
DB_NAME="movieCatalog"
DB_USER="admin"
DB_HOST="localhost"
DB_PORT="5432"
DB_PASSWORD="admin"

echo "Database Configuration:"
echo "  Database Name: $DB_NAME"
echo "  User: $DB_USER"
echo "  Host: $DB_HOST"
echo "  Port: $DB_PORT"
echo ""

# Выбор способа запуска PostgreSQL
echo "Выберите способ запуска PostgreSQL:"
echo "1) Локальная установка PostgreSQL"
echo "2) Docker"
read -p "Введите номер (1 или 2): " docker_choice

USE_DOCKER=false
if [ "$docker_choice" = "2" ]; then
    USE_DOCKER=true
    echo ""
    echo "Использование Docker для PostgreSQL..."
    
    if ! command -v docker &> /dev/null; then
        echo "❌ Docker не установлен. Установите Docker и попробуйте снова."
        exit 1
    fi
    
    if docker ps -a | grep -q "postgres-moviecatalog"; then
        echo "Контейнер postgres-moviecatalog уже существует."
        read -p "Удалить существующий контейнер? (y/n): " remove_container
        if [ "$remove_container" = "y" ]; then
            docker stop postgres-moviecatalog 2>/dev/null
            docker rm postgres-moviecatalog 2>/dev/null
            echo "Существующий контейнер удален."
        fi
    fi
    
    echo ""
    echo "Запуск PostgreSQL в Docker..."
    docker run -d \
        --name postgres-moviecatalog \
        -e POSTGRES_DB=$DB_NAME \
        -e POSTGRES_USER=$DB_USER \
        -e POSTGRES_PASSWORD=$DB_PASSWORD \
        -p $DB_PORT:5432 \
        postgres:16
    
    if [ $? -ne 0 ]; then
        echo "❌ Ошибка запуска PostgreSQL контейнера!"
        exit 1
    fi
    
    echo "✅ PostgreSQL контейнер успешно запущен!"
    echo "Ожидание готовности базы данных..."
    sleep 5
else
    echo ""
    echo "Использование локальной установки PostgreSQL..."
    
    if ! command -v psql &> /dev/null; then
        echo "❌ PostgreSQL не установлен. Установите PostgreSQL и попробуйте снова."
        exit 1
    fi
fi

echo ""
read -p "Продолжить с этими настройками? (y/n): " confirm

if [ "$confirm" != "y" ]; then
    echo "Операция отменена."
    if [ "$USE_DOCKER" = true ]; then
        read -p "Остановить Docker контейнер? (y/n): " stop_docker
        if [ "$stop_docker" = "y" ]; then
            docker stop postgres-moviecatalog
        fi
    fi
    exit 0
fi

echo ""
echo "Создание базы данных..."

if [ "$USE_DOCKER" = true ]; then
    # Создание базы данных через Docker
    docker exec -i postgres-moviecatalog psql -U $DB_USER -d postgres << EOF
-- Удаление базы данных, если она уже существует
DROP DATABASE IF EXISTS $DB_NAME;

-- Создание базы данных
CREATE DATABASE $DB_NAME;
EOF

    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ База данных '$DB_NAME' успешно создана!"
        
        # Создание схемы БД
        if [ -f "database_schema.sql" ]; then
            echo "Creating database schema..."
            docker exec -i postgres-moviecatalog psql -U $DB_USER -d $DB_NAME < database_schema.sql
            if [ $? -eq 0 ]; then
                echo "✅ Database schema created successfully!"
            else
                echo "⚠️  Warning: Error creating database schema"
            fi
        else
            echo "⚠️  Warning: database_schema.sql not found, skipping schema creation"
        fi
        
        exit 0
    else
        echo ""
        echo "❌ Ошибка создания базы данных!"
        echo "Проверьте установку PostgreSQL и перезапустите скрипт."
        exit 1
    fi
else
    # Создание базы данных через локальный PostgreSQL
    sudo -u postgres psql << EOF
-- Удаление базы данных, если она уже существует
DROP DATABASE IF EXISTS $DB_NAME;

-- Создание базы данных
CREATE DATABASE $DB_NAME;

-- Создание пользователя, если не существует
DO \$\$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = '$DB_USER') THEN
        CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';
    END IF;
END
\$\$;

-- Предоставление привилегий
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;
EOF

    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ База данных '$DB_NAME' успешно создана!"
        exit 0
    else
        echo ""
        echo "❌ Ошибка создания базы данных!"
        echo "Проверьте установку PostgreSQL и перезапустите скрипт."
        exit 1
    fi
fi
