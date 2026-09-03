@echo off
setlocal
cd /d "%~dp0"

where javac >nul 2>&1
if errorlevel 1 (
  echo Java nao encontrado. Instale o JDK 17 ou superior e tente novamente.
  pause
  exit /b 1
)

if not exist out mkdir out
javac -encoding UTF-8 -d out src\mostra\*.java
if errorlevel 1 (
  echo.
  echo Erro ao compilar o projeto.
  pause
  exit /b 1
)

java -cp out mostra.Main
