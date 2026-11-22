
@echo off
REM Arranca Punch It en Windows (escritorio). Uso: scripts\run.bat
REM No requiere configurar JAVA_HOME: busca un JDK 8-15 valido por su cuenta.
setlocal
cd /d "%~dp0.."

set "JAVA_HOME_FOUND="
set "JAVA_VER="
set "JAVA_MAJOR="

REM 1) JAVA_HOME actual, solo si apunta a un JDK soportado (8-15).
if defined JAVA_HOME call :try_jdk "%JAVA_HOME%"
if defined JAVA_HOME_FOUND goto :run

REM 2) Temurin / Zulu 11 (o 8 / 15) en las ubicaciones tipicas de instalacion.
for %%R in ("%ProgramFiles%" "%LOCALAPPDATA%\Programs") do (
  if exist "%%~R" (
    for %%V in ("%%~R\Eclipse Adoptium" "%%~R\Java" "%%~R\Zulu") do (
      if exist "%%~V" (
        for /d %%J in ("%%~V\jdk-11*" "%%~V\jdk-8*" "%%~V\jdk-15*") do (
          if not defined JAVA_HOME_FOUND call :try_jdk "%%~J"
        )
      )
    )
  )
)
if defined JAVA_HOME_FOUND goto :run

REM 3) El 'java' que haya en el PATH (por si esta instalado en otro sitio).
for /f "delims=" %%P in ('where java 2^>nul') do (
  if not defined JAVA_HOME_FOUND (
    for %%Q in ("%%~dpP..") do call :try_jdk "%%~fQ"
  )
)
if defined JAVA_HOME_FOUND goto :run

echo.
echo [ERROR] No encontre un JDK compatible (Java 8-15).
echo   Instala Temurin 11 ^(https://adoptium.net^) y vuelve a intentarlo.
echo.
exit /b 1

:run
set "JAVA_HOME=%JAVA_HOME_FOUND%"
if not "%JAVA_MAJOR%"=="11" echo   [AVISO] Java %JAVA_VER% detectado. La combinacion probada es Temurin 11.
echo   Iniciando Punch It con Java %JAVA_VER%...
call gradlew.bat desktop:run
exit /b %errorlevel%

REM --- Subrutina: valida que %1 sea un JDK 8-15 y lo registra ------------------
:try_jdk
set "CAND=%~1"
set "V="
set "MAJOR="
if "%CAND%"=="" goto :eof
if "%CAND:~-1%"=="\" set "CAND=%CAND:~0,-1%"
if not exist "%CAND%\bin\java.exe" goto :eof

for /f "tokens=3" %%g in ('call "%CAND%\bin\java.exe" -version 2^>^&1 ^| findstr /i "version"') do set "V=%%g"
if not defined V goto :eof
set "V=%V:"=%"
for /f "delims=." %%a in ("%V%") do set "MAJOR=%%a"
if not defined MAJOR goto :eof
if "%MAJOR%"=="1" for /f "tokens=2 delims=." %%a in ("%V%") do set "MAJOR=%%a"
if %MAJOR% LSS 8 goto :eof
if %MAJOR% GTR 15 goto :eof

set "JAVA_HOME_FOUND=%CAND%"
set "JAVA_VER=%V%"
set "JAVA_MAJOR=%MAJOR%"
goto :eof
