@echo off
REM ====================================================================
REM  Compila e roda o projeto Supermercado BD.
REM  Pre-requisitos:
REM    - Java 17+ instalado (javac e java no PATH)
REM    - MySQL rodando com o banco "supermercado" populado
REM    - Pasta lib/ contem mysql-connector-j, jfreechart e jcommon
REM ====================================================================

cd /d "%~dp0"

echo [1/2] Compilando...
if not exist out mkdir out
dir /S /B src\main\java\*.java > .arquivos.txt
javac -d out -cp "lib\*" @.arquivos.txt
del .arquivos.txt

if errorlevel 1 (
    echo.
    echo  ERRO na compilacao. Verifique a saida acima.
    pause
    exit /b 1
)

echo [2/2] Iniciando aplicacao...
java -cp "out;lib\*" Principal

pause
