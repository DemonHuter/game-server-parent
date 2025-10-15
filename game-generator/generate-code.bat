@echo off
chcp 65001 >nul

rem Check if it's a help command
setlocal enabledelayedexpansion
set "SHOW_HELP=false"
for %%a in (%*) do (
    if "%%a"=="--help" set "SHOW_HELP=true"
    if "%%a"=="-h" set "SHOW_HELP=true"
)

if "%SHOW_HELP%"=="true" (
    cd /d "%~dp0"
    echo.
    echo Showing help information...
    echo.
    java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -jar target/game-generator-1.0.0-executable.jar %*
    echo.
    echo Press any key to exit...
    pause >nul
    goto :eof
)

echo ==============================================
echo          Auto Code Generator
echo ==============================================
echo.

echo Starting code generator...
echo.

cd /d "%~dp0"

rem Check Java environment
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java environment not found, please install JDK first
    pause
    exit /b 1
)

rem Check if executable JAR file exists
if not exist "target\game-generator-1.0.0-executable.jar" (
    echo Error: Executable JAR file not found, please run "mvn package" command first
    pause
    exit /b 1
)

echo.
echo Checking configuration file...
if not exist "config.yml" (
    echo config.yml not found, creating default configuration file from example...
    copy "config-example.yml" "config.yml" >nul
    echo config.yml created, please modify database connection information as needed
    echo Note: Do not add config.yml to version control system to avoid leaking sensitive information
    echo.
)

rem Delete all files in outPut folder before generating code
echo.
echo Clearing outPut folder...
if exist "outPut\" (
    rd /s /q "outPut\" >nul 2>&1
)
mkdir "outPut" >nul 2>&1
echo outPut folder cleared.
echo.

rem Check if parameters are provided
set "TABLES_PARAM="
set "EXTRA_ARGS="
if "%~1"=="" (
    rem When double-clicked, prompt for table names directly
    echo Please enter the table names to generate code for, separate multiple names with spaces
    echo Example: player user role
    echo Press Enter directly to generate code for all tables
    echo.
    set /p table_input="Enter table names: "
    
    if defined table_input (
        set "TABLES_PARAM=--include-tables"
        set "TABLE_NAMES=!table_input!"
        echo Specified tables: !TABLE_NAMES!
        echo.
    ) else (
        echo No table names specified, will generate code for all tables...
        echo.
    )
) else (
    rem Command line parameter mode
    rem Check if the first parameter is a known command line option
    set "IS_OPTION=false"
    for %%o in (--config --db-url --db-username --db-password --author --table-prefix --exclude-tables --include-tables --no-overwrite --help -h) do (
        if "%~1"=="%%o" set "IS_OPTION=true"
    )
    
    if "!IS_OPTION!"=="false" (
        rem Treat all parameters as table names
        set "TABLES_PARAM=--include-tables"
        set "TABLE_NAMES="
        :build_table_list
        if "%~1"=="" goto :done_building_table_list
        if "!TABLE_NAMES!"=="" (
            set "TABLE_NAMES=%~1"
        ) else (
            set "TABLE_NAMES=!TABLE_NAMES!,%~1"
        )
        shift
        goto :build_table_list
        :done_building_table_list
        echo Specified tables: !TABLE_NAMES!
        echo.
    ) else (
        rem Parameters are command line options, pass directly to Java program
        echo Using command line options...
        echo.
    )
)

echo Starting code generation...
echo.

rem Run code generator - using executable JAR file
if defined TABLES_PARAM (
    java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -jar target/game-generator-1.0.0-executable.jar %TABLES_PARAM% "%TABLE_NAMES%" %*
) else (
    java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -jar target/game-generator-1.0.0-executable.jar %*
)
set RESULT=%errorlevel%

if %RESULT% equ 0 (
    echo.
    echo ==============================================
    echo          Code generation completed!
    echo ==============================================
    echo.
    echo Generated files location:
    echo - Entity classes: game-dao/src/main/java/com/game/dao/entity/
    echo - Cache classes:  game-cache/src/main/java/com/game/cache/
    echo - DAO interfaces: game-dao/src/main/java/com/game/dao/mapper/
    echo.
    echo Press any key to exit...
    pause >nul
    goto :eof
) else (
    echo.
    echo ==============================================
    echo          Code generation failed!
    echo ==============================================
    echo.
    echo Please check:
    echo 1. Is the database connection normal?
    echo 2. Is the database configuration correct?
    echo 3. Is the MySQL driver installed?
    echo 4. Run: generate-code.bat --help for help
    echo.
    echo Java program returned error code: %RESULT%
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b %RESULT%
)