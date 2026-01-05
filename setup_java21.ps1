# Скрипт для настройки Java 21
# Проверяет наличие Java 21 и настраивает JAVA_HOME

Write-Host "Поиск установленных версий Java..." -ForegroundColor Cyan

# Типичные пути установки Java на Windows
$javaPaths = @(
    "$env:ProgramFiles\Java",
    "$env:ProgramFiles(x86)\Java",
    "$env:LOCALAPPDATA\Programs\Eclipse Adoptium",
    "$env:LOCALAPPDATA\Programs\Microsoft",
    "C:\Program Files\Eclipse Adoptium",
    "C:\Program Files\Microsoft",
    "C:\Program Files\Java"
)

$java21Found = $false
$java21Path = $null

foreach ($basePath in $javaPaths) {
    if (Test-Path $basePath) {
        $jdkFolders = Get-ChildItem -Path $basePath -Directory -ErrorAction SilentlyContinue | Where-Object { 
            $_.Name -match "jdk-?21|java-?21|jdk21|java21" 
        }
        
        foreach ($folder in $jdkFolders) {
            $javaExe = Join-Path $folder.FullName "bin\java.exe"
            if (Test-Path $javaExe) {
                $version = & $javaExe -version 2>&1 | Select-String "version"
                if ($version -match "21") {
                    $java21Path = $folder.FullName
                    $java21Found = $true
                    Write-Host "Найдена Java 21: $java21Path" -ForegroundColor Green
                    break
                }
            }
        }
        
        if ($java21Found) { break }
    }
}

if (-not $java21Found) {
    Write-Host "`nJava 21 не найдена в системе!" -ForegroundColor Red
    Write-Host "Пожалуйста, установите Java 21 одним из способов:" -ForegroundColor Yellow
    Write-Host "1. Eclipse Adoptium: https://adoptium.net/temurin/releases/?version=21" -ForegroundColor Yellow
    Write-Host "2. Microsoft Build of OpenJDK: https://learn.microsoft.com/en-us/java/openjdk/download" -ForegroundColor Yellow
    Write-Host "3. Oracle JDK: https://www.oracle.com/java/technologies/downloads/#java21" -ForegroundColor Yellow
    exit 1
}

# Установка JAVA_HOME для текущей сессии
$env:JAVA_HOME = $java21Path
$env:PATH = "$java21Path\bin;$env:PATH"

Write-Host "`nJAVA_HOME установлен на: $env:JAVA_HOME" -ForegroundColor Green

# Проверка версии
Write-Host "`nПроверка версии Java:" -ForegroundColor Cyan
& "$java21Path\bin\java.exe" -version

Write-Host "`nДля постоянной настройки JAVA_HOME выполните:" -ForegroundColor Yellow
Write-Host "[System.Environment]::SetEnvironmentVariable('JAVA_HOME', '$java21Path', 'User')" -ForegroundColor White
Write-Host "`nИли добавьте вручную в переменные окружения Windows." -ForegroundColor Yellow

Write-Host "`nДля применения в текущей сессии PowerShell выполните:" -ForegroundColor Yellow
Write-Host "`$env:JAVA_HOME = '$java21Path'" -ForegroundColor White
Write-Host "`$env:PATH = '`$env:JAVA_HOME\bin;' + `$env:PATH" -ForegroundColor White

