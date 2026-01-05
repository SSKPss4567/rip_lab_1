# Script for creating PostgreSQL database for Movie Catalog

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Movie Catalog - Database Setup Script" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Default parameters
$DB_NAME = "movieCatalog"
$DB_USER = "admin"
$DB_HOST = "localhost"
$DB_PORT = "5432"
$DB_PASSWORD = "admin"

Write-Host "Database Configuration:"
Write-Host "  Database Name: $DB_NAME"
Write-Host "  User: $DB_USER"
Write-Host "  Host: $DB_HOST"
Write-Host "  Port: $DB_PORT"
Write-Host ""

# Choose PostgreSQL setup method
Write-Host "Choose PostgreSQL setup method:"
Write-Host "1) Local PostgreSQL installation"
Write-Host "2) Docker"
$dockerChoice = Read-Host "Enter number (1 or 2)"

$useDocker = $false
if ($dockerChoice -eq "2") {
    $useDocker = $true
    Write-Host ""
    Write-Host "Using Docker for PostgreSQL..." -ForegroundColor Green
    
    $dockerExists = Get-Command docker -ErrorAction SilentlyContinue
    if (-not $dockerExists) {
        Write-Host "Docker is not installed. Install Docker and try again." -ForegroundColor Red
        exit 1
    }
    
    $existingContainer = docker ps -a --filter "name=postgres-moviecatalog" --format "{{.Names}}"
    if ($existingContainer -eq "postgres-moviecatalog") {
        Write-Host "Container postgres-moviecatalog already exists." -ForegroundColor Yellow
        $removeContainer = Read-Host "Remove existing container? (y/n)"
        if ($removeContainer -eq "y" -or $removeContainer -eq "Y") {
            docker stop postgres-moviecatalog 2>$null
            docker rm postgres-moviecatalog 2>$null
            Write-Host "Existing container removed." -ForegroundColor Green
        }
    }
    
    Write-Host ""
    Write-Host "Starting PostgreSQL in Docker..." -ForegroundColor Green
    $null = docker run -d `
        --name postgres-moviecatalog `
        -e POSTGRES_DB=$DB_NAME `
        -e POSTGRES_USER=$DB_USER `
        -e POSTGRES_PASSWORD=$DB_PASSWORD `
        -p "${DB_PORT}:5432" `
        postgres:16
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error starting PostgreSQL container!" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "PostgreSQL container started successfully!" -ForegroundColor Green
    Write-Host "Waiting for database to be ready..." -ForegroundColor Yellow
    Start-Sleep -Seconds 5
} else {
    Write-Host ""
    Write-Host "Using local PostgreSQL installation..." -ForegroundColor Green
    
    $psqlExists = Get-Command psql -ErrorAction SilentlyContinue
    if (-not $psqlExists) {
        Write-Host "PostgreSQL is not installed. Install PostgreSQL and try again." -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
$confirm = Read-Host "Continue with these settings? (y/n)"

if ($confirm -ne "y" -and $confirm -ne "Y") {
    Write-Host "Operation cancelled." -ForegroundColor Yellow
    if ($useDocker) {
        $stopDocker = Read-Host "Stop Docker container? (y/n)"
        if ($stopDocker -eq "y" -or $stopDocker -eq "Y") {
            docker stop postgres-moviecatalog
        }
    }
    exit 0
}

Write-Host ""
Write-Host "Creating database..." -ForegroundColor Yellow

if ($useDocker) {
    # Create database via Docker
    $sqlScript = @"
-- Drop database if it already exists
DROP DATABASE IF EXISTS $DB_NAME;

-- Create database
CREATE DATABASE $DB_NAME;
"@
    
    $sqlScript | docker exec -i postgres-moviecatalog psql -U $DB_USER -d postgres
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "Database '$DB_NAME' created successfully!" -ForegroundColor Green
        
        # Create database schema
        if (Test-Path "database_schema.sql") {
            Write-Host "Creating database schema..." -ForegroundColor Yellow
            Get-Content database_schema.sql | docker exec -i postgres-moviecatalog psql -U $DB_USER -d $DB_NAME
            if ($LASTEXITCODE -eq 0) {
                Write-Host "Database schema created successfully!" -ForegroundColor Green
            } else {
                Write-Host "Warning: Error creating database schema" -ForegroundColor Yellow
            }
        } else {
            Write-Host "Warning: database_schema.sql not found, skipping schema creation" -ForegroundColor Yellow
        }
        
        exit 0
    } else {
        Write-Host ""
        Write-Host "Error creating database!" -ForegroundColor Red
        Write-Host "Check PostgreSQL installation and restart the script." -ForegroundColor Yellow
        exit 1
    }
} else {
    # Create database via local PostgreSQL
    $psqlPath = (Get-Command psql).Source
    $env:PGPASSWORD = "postgres"
    
    $sqlScript = @"
-- Drop database if it already exists
DROP DATABASE IF EXISTS $DB_NAME;

-- Create database
CREATE DATABASE $DB_NAME;

-- Create user if not exists
DO `$\$`
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = '$DB_USER') THEN
        CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';
    END IF;
END
`$\$;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;
"@
    
    Write-Host "Attempting to connect to PostgreSQL..." -ForegroundColor Yellow
    Write-Host "Note: Password may be required for postgres user" -ForegroundColor Yellow
    
    $sqlScript | & $psqlPath -U postgres -h $DB_HOST -p $DB_PORT
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "Database '$DB_NAME' created successfully!" -ForegroundColor Green
        exit 0
    } else {
        Write-Host ""
        Write-Host "Error creating database!" -ForegroundColor Red
        Write-Host "Check PostgreSQL installation and restart the script." -ForegroundColor Yellow
        exit 1
    } finally {
        Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
    }
}
