@echo off
echo Building Personalized Voice Responder...
echo Note: You need Java 17 and Android SDK Command-line Tools installed.
echo.
call gradlew.bat assembleDebug
if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build Successful! 
    echo APK location: app\build\outputs\apk\debug\app-debug.apk
) else (
    echo.
    echo Build Failed. Check the errors above.
)
pause
