@echo off
echo ===============================================
echo   Iniciando PixelShop con todas las variables
echo ===============================================

set EMAIL_USERNAME=juanapmparkersolution@gmail.com
set EMAIL_PASSWORD=dbhd urmn vpoq vncs
set CLOUDINARY_CLOUD_NAME=pixelshop
set CLOUDINARY_API_KEY=742135482932556
set CLOUDINARY_API_SECRET=k71G2RrGPrW09sSzmVRQ2PHt0K4
set GOOGLE_CLIENT_ID=18168021278-s2kgou2l49vp2850ujup870qvpedv0em.apps.googleusercontent.com
set GOOGLE_SECRET=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
set PAYPAL_CLIENT_ID=AfUAGvDCxWhgSUKFnRGu7pCh2si2_IS6MEe8xC1FEKD1288Yom4uZNbwB7UMba-7eafCDY7xSat8fmo2
set PAYPAL_CLIENT_SECRET=EO5zFEnFMpEveATnGn5tS2-kzyH4JUjHctqzCdWUWsBAaQZ9_D7Jl4nxFyLgMUNTEceHbklMINhE0vip

echo.
echo ✅ Variables de entorno cargadas
echo 🔑 RESEND_API_KEY: %RESEND_API_KEY%
echo 🔑 GOOGLE_CLIENT_ID: %GOOGLE_CLIENT_ID%
echo.
echo 🚀 Iniciando aplicación...
echo.

mvn spring-boot:run
