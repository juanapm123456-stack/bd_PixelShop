# Script para arrancar PixelShop con variables de entorno

Write-Host "🚀 Cargando variables de entorno..." -ForegroundColor Cyan

$env:CLOUDINARY_CLOUD_NAME = "pixelshop"
$env:CLOUDINARY_API_KEY = "258925342165751"
$env:CLOUDINARY_API_SECRET = "UaTSS3321b-7rDlMVFuWNfQO4DM"
$env:RESEND_API_KEY = "re_bwbKzPEJ_Joc6W4xS2jdBCyZN5S3MPN8g"
$env:GOOGLE_CLIENT_ID = "18168021278-s2kgou2l49vp2850ujup870qvpedv0em.apps.googleusercontent.com"
$env:GOOGLE_SECRET = "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"
$env:PAYPAL_CLIENT_ID = "AfUAGvDCxWhgSUKFnRGu7pCh2si2_IS6MEe8xC1FEKD1288Yom4uZNbwB7UMba-7eafCDY7xSat8fmo2"
$env:PAYPAL_CLIENT_SECRET = "EO5zFEnFMpEveATnGn5tS2-kzyH4JUjHctqzCdWUWsBAaQZ9_D7Jl4nxFyLgMUNTEceHbklMINhE0vip"

Write-Host "✅ Variables cargadas" -ForegroundColor Green
Write-Host "🎮 Arrancando PixelShop..." -ForegroundColor Cyan
Write-Host ""

mvn spring-boot:run
