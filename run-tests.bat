@echo off
setlocal
cd /d "%~dp0"

where javac >nul 2>&1
if errorlevel 1 (
  echo Java nao encontrado. Instale o JDK 17 ou superior e tente novamente.
  exit /b 1
)

if not exist out-tests mkdir out-tests
javac -encoding UTF-8 -d out-tests src\mostra\*.java tests\mostra\*.java
if errorlevel 1 exit /b 1

java -ea -cp out-tests mostra.TestesJogo
