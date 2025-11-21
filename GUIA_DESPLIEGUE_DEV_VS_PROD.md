# 🚀 GUÍA DE DATOS DE EJEMPLO

## 📋 Resumen

Este proyecto incluye **datos de ejemplo/demostración** que se cargan automáticamente:

- ✅ Se ejecutan **SIEMPRE** (desarrollo y producción)
- ✅ Solo se cargan si la BD está **vacía**
- ✅ Incluyen usuarios, juegos, compras de ejemplo
- ⚠️ Usan contraseñas **simples** para demostración

| Aspecto | Estado |
|---------|--------|
| **EjemplosDataSeeder** | ✅ Activo siempre |
| **Usuarios de ejemplo** | ✅ Se crean automáticamente |
| **Contraseñas** | ⚠️ Simples (admin123, cliente123) |
| **Juegos de ejemplo** | ✅ 10 juegos con imágenes placeholder |
| **Compras de ejemplo** | ✅ 9 compras entre 3 clientes |

---

## 📦 ¿Qué datos se cargan?

### 👥 Usuarios (6 en total)

**1 Administrador:**
- 📧 `admin@pixelshop.com` / 🔑 `admin123`

**2 Proveedores:**
- 📧 `epic@pixelshop.com` / 🔑 `proveedor123`
- 📧 `indie@pixelshop.com` / 🔑 `proveedor123`

**3 Clientes:**
- 📧 `maria@gmail.com` / 🔑 `cliente123`
- 📧 `carlos@gmail.com` / 🔑 `cliente123`
- 📧 `ana@gmail.com` / 🔑 `cliente123`

### 🎮 Juegos (10 en total)

1. Cyberpunk 2077 - 59.99€
2. The Last of Us Part II - 49.99€
3. Hollow Knight - 19.99€
4. FIFA 24 - 69.99€
5. Stardew Valley - 14.99€
6. Call of Duty: Modern Warfare - 59.99€
7. Civilization VI - 39.99€
8. Phasmophobia - 12.99€
9. Among Us - 4.99€
10. Red Dead Redemption 2 - 59.99€

### 💰 Compras (9 en total)

- María: 3 juegos (Cyberpunk, Hollow Knight, Stardew Valley)
- Carlos: 2 juegos (The Last of Us, Call of Duty)
- Ana: 4 juegos (FIFA, Civilization, Among Us, Red Dead)

### 📊 Movimientos Financieros

- 10 publicaciones × 25€ = **250€** para la plataforma
- Total ventas: **391.92€**
  - 15% plataforma: **58.79€**
  - 85% proveedores: **333.13€**

**Total ganancias plataforma:** 250€ + 58.79€ = **308.79€**

---

## 🎯 ¿Cuándo se cargan los datos?

### ✅ Se cargan SI:

- La tabla `usuarios` está **vacía** (`count = 0`)
- Es la **primera vez** que ejecutas la aplicación
- Has **borrado** todos los datos de la BD

### ❌ NO se cargan SI:

- Ya existen usuarios en la BD
- Has ejecutado la aplicación anteriormente
- La BD ya tiene datos

---

## 🔄 ¿Cómo desactivar los datos de ejemplo?

Si NO quieres que se carguen datos de ejemplo:

### Opción 1: Comentar @Component

En `EjemplosDataSeeder.java`:

```java
// @Component  ← Comentar esta línea
@Order(1)
public class EjemplosDataSeeder implements CommandLineRunner {
```

### Opción 2: Usar @Profile

En `EjemplosDataSeeder.java`:

```java
@Component
@Profile("never")  // ← Nunca se ejecutará
@Order(1)
public class EjemplosDataSeeder implements CommandLineRunner {
```

### Opción 3: Borrar la clase

Simplemente elimina el archivo:
```
src/main/java/com/example/config/EjemplosDataSeeder.java
```

---

## 🔄 ¿Cómo recargar los datos de ejemplo?

Si ya tienes datos y quieres **recargar los ejemplos**:

### 1. Borrar todos los datos

```sql
-- Borrar en orden (por claves foráneas)
DELETE FROM movimiento_proveedor;
DELETE FROM biblioteca_usuario;
DELETE FROM compras;
DELETE FROM publicacion_juego;
DELETE FROM juegos;
DELETE FROM usuarios;
```

### 2. Reiniciar la aplicación

Los datos de ejemplo se cargarán automáticamente.

---

## 🔒 Seguridad en Producción

### ⚠️ PROBLEMA: Contraseñas débiles

Los datos de ejemplo usan contraseñas **muy simples**:
- `admin123`
- `proveedor123`
- `cliente123`

### ✅ SOLUCIÓN: Cambiar contraseñas después de cargar

```sql
-- Generar hash BCrypt para tu contraseña segura
-- Usar: https://bcrypt-generator.com/

UPDATE usuarios 
SET password = '$2a$10$TU_HASH_BCRYPT_AQUI' 
WHERE email = 'admin@pixelshop.com';
```

---

## ✅ RESUMEN EJECUTIVO

**Situación actual:**
- ✅ `EjemplosDataSeeder` carga datos **siempre** (dev y prod)
- ✅ Solo si la BD está vacía
- ✅ Incluye 6 usuarios, 10 juegos, 9 compras
- ⚠️ Usa contraseñas simples para demostración

**Para producción real:**
- ⚠️ **Cambiar contraseñas** por seguras
- ⚠️ O desactivar con `@Profile("never")`

---

**Fecha:** 21 Noviembre 2025  
**Versión:** 2.0 (Actualizado con EjemplosDataSeeder siempre activo)

---

## 🔒 CHECKLIST DE SEGURIDAD PARA PRODUCCIÓN

Antes de desplegar en producción, verifica:

### 1. ✅ Perfil activo = `prod`
```properties
spring.profiles.active=prod
```

### 2. ✅ Activar CSRF en `SecurityConfig.java`
```java
// ❌ EN DESARROLLO:
.csrf(csrf -> csrf.disable());

// ✅ EN PRODUCCIÓN:
.csrf(Customizer.withDefaults());
```

### 3. ✅ Contraseñas seguras
- ❌ NO usar: `admin123`, `cliente123`
- ✅ Usar: Contraseñas fuertes con números, símbolos, mayúsculas

### 4. ✅ Credenciales de BD seguras
```properties
# ❌ NO:
spring.datasource.password=admin

# ✅ SÍ:
spring.datasource.password=${DB_PASSWORD}  # Variable de entorno
```

### 5. ✅ Hibernate DDL = `validate` o `none`
```properties
# ❌ NUNCA en producción:
spring.jpa.hibernate.ddl-auto=update  # Puede borrar datos!

# ✅ Usar:
spring.jpa.hibernate.ddl-auto=validate  # Solo valida
```

### 6. ✅ Ocultar mensajes de error detallados
```properties
server.error.include-message=never
server.error.include-stacktrace=never
```

### 7. ✅ SSL/TLS en MySQL
Ya lo tienes:
```properties
spring.datasource.url=...?sslMode=REQUIRED
```

### 8. ✅ Session timeout configurado
```properties
server.servlet.session.timeout=30m
```

---

## 🔄 CAMBIAR ENTRE MODOS

### Desarrollo → Producción

1. Cambiar en `application.properties`:
   ```properties
   spring.profiles.active=prod
   ```

2. Reiniciar la aplicación

3. Verificar en los logs:
   - ✅ NO debe aparecer el banner de DataSeeder
   - ✅ NO debe verse SQL en consola
   - ✅ Logs deben ser mínimos

### Producción → Desarrollo

1. Cambiar en `application.properties`:
   ```properties
   spring.profiles.active=dev
   ```

2. Reiniciar la aplicación

3. Verificar en los logs:
   - ✅ Debe aparecer banner de DataSeeder (si BD vacía)
   - ✅ SQL visible en consola

---

## 📦 DESPLIEGUE EN AZURE

### Configurar perfil de producción en Azure

**App Service → Configuration → Application settings**

Añadir variable:
- **Name:** `SPRING_PROFILES_ACTIVE`
- **Value:** `prod`

Esto asegura que Azure **siempre use producción**.

### Build para producción

```bash
# Limpiar y construir
mvn clean package -DskipTests

# El JAR estará en:
target/spring_crud_thymeleaf_v21-0.0.1-SNAPSHOT.jar
```

### Variables de entorno en Azure

```bash
SPRING_PROFILES_ACTIVE=prod
DB_HOST=mysqldemojuan.mysql.database.azure.com
DB_NAME=prueba_bd
DB_USERNAME=adminuser
DB_PASSWORD=TuPasswordSeguro
```

---

## 🧪 TESTING

### Probar modo desarrollo

```bash
# 1. Borrar datos de prueba (opcional)
DROP DATABASE IF EXISTS prueba_bd;
CREATE DATABASE prueba_bd;

# 2. Iniciar con perfil dev
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 3. Verificar datos de prueba:
# - Ir a http://localhost:8080/login
# - Login: maria@gmail.com / cliente123
# - Debe haber 10 juegos en el catálogo
```

### Probar modo producción

```bash
# 1. Iniciar con perfil prod
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# 2. Verificar:
# - NO debe haber datos de prueba
# - Catálogo debe estar vacío
# - NO debe verse SQL en logs
```

---

## 📝 NOTAS IMPORTANTES

### ¿Por qué DataSeeder es peligroso en producción?

1. ❌ **Credenciales débiles** (`admin123`) conocidas públicamente
2. ❌ **Datos falsos** que afectan métricas reales
3. ❌ **Puede sobrescribir** datos reales si BD se vacía
4. ❌ **Exposición de seguridad** (alguien puede entrar con las credenciales de prueba)

### ¿Cuándo usar cada modo?

| Escenario | Modo |
|-----------|------|
| Desarrollo local | `dev` |
| Demostración/Pruebas | `dev` |
| Testing con datos reales | `dev` (con precaución) |
| Producción (Azure) | `prod` |
| Servidor en producción | `prod` |

### ¿Cómo sé qué modo está activo?

Revisar logs al iniciar:
```
The following 1 profile is active: "dev"
```

O verificar con código:
```java
@Value("${spring.profiles.active}")
private String activeProfile;

System.out.println("Perfil activo: " + activeProfile);
```

---

## 🆘 PROBLEMAS COMUNES

### 1. DataSeeder se ejecuta en producción

**Causa:** Perfil activo no es `prod`

**Solución:**
```properties
spring.profiles.active=prod
```

### 2. No hay datos de prueba en desarrollo

**Causa:** Perfil activo no es `dev` O la BD ya tiene datos

**Solución:**
```properties
spring.profiles.active=dev
```

Y borrar datos existentes:
```sql
DELETE FROM compras;
DELETE FROM biblioteca_usuario;
DELETE FROM movimiento_proveedor;
DELETE FROM publicacion_juego;
DELETE FROM juegos;
DELETE FROM usuarios;
```

### 3. Hibernate borra datos en producción

**Causa:** `ddl-auto=create` o `create-drop`

**Solución:** En `application-prod.properties`:
```properties
spring.jpa.hibernate.ddl-auto=validate
```

---

## ✅ RESUMEN EJECUTIVO

**Para desarrollo local:**
```properties
spring.profiles.active=dev
```
→ DataSeeder carga datos de prueba automáticamente

**Para producción (Azure):**
```properties
spring.profiles.active=prod
```
→ DataSeeder NO se ejecuta, debes crear admin manualmente

**Siempre revisar** que el perfil activo sea el correcto antes de desplegar.

---

**Fecha de creación:** 21 Noviembre 2025  
**Versión:** 1.0  
**Autor:** PixelShop Development Team
