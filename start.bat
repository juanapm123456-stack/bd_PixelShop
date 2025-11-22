@echo off
echo ===============================================
echo   Iniciando PixelShop con todas las variables
echo ===============================================

set RESEND_API_KEY=re_bwbKzPEJ_Joc6W4xS2jdBCyZN5S3MPN8g
set CLOUDINARY_CLOUD_NAME=pixelshop
set CLOUDINARY_API_KEY=742135482932556
set CLOUDINARY_API_SECRET=k71G2RrGPrW09sSzmVRQ2PHt0K4
set GOOGLE_CLIENT_ID=18168021278-s2kgou2l49vp2850ujup870qvpedv0em.apps.googleusercontent.com
set GOOGLE_SECRET=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
set PAYPAL_CLIENT_ID=AUJoWELN6zFo6AcAlcpHDjl2r-uXpjuIqLOK-5jCKJsYcIwRQwvx2WRzXS66p_7yFIyQUixHn0ZvNNEv
set PAYPAL_CLIENT_SECRET=EHDn8X6Wmy0zPkwXpTzHlJ5SN0_jWmQiM1qFgTjvnCv7dJdOPxEPOBRXfaU-7NZpYoCKb4E06mPBvV-i

echo.
echo ✅ Variables de entorno cargadas
echo 🔑 RESEND_API_KEY: %RESEND_API_KEY%
echo 🔑 GOOGLE_CLIENT_ID: %GOOGLE_CLIENT_ID%
echo.
echo 🚀 Iniciando aplicación...
echo.

mvn spring-boot:run
