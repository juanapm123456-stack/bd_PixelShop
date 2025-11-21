# 🔐 EXPLICACIÓN TÉCNICA: Spring Profiles

## ¿Qué son los Spring Profiles?

Los **Spring Profiles** permiten tener diferentes configuraciones según el entorno (desarrollo, producción, testing).

---

## 📁 Estructura de Archivos

```
src/main/resources/
├── application.properties          ← Configuración BASE (siempre se carga)
├── application-dev.properties      ← Solo se carga si profile = dev
└── application-prod.properties     ← Solo se carga si profile = prod
```

### Orden de carga:

1. **application.properties** (base)
2. **application-{profile}.properties** (específico)

Las propiedades específicas **sobrescriben** las generales.

---

## 🎯 Ejemplo Práctico

### application.properties (base)
```properties
spring.application.name=PixelShop
spring.profiles.active=dev

# Configuración compartida
spring.datasource.url=jdbc:mysql://...
spring.datasource.username=adminuser
```

### application-dev.properties
```properties
# SOLO para desarrollo
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update
logging.level.com.example=DEBUG
```

### application-prod.properties
```properties
# SOLO para producción
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=validate
logging.level.com.example=INFO
```

### Resultado:

**Con `spring.profiles.active=dev`:**
- Se carga: `application.properties` + `application-dev.properties`
- `show-sql` = `true`
- `ddl-auto` = `update`

**Con `spring.profiles.active=prod`:**
- Se carga: `application.properties` + `application-prod.properties`
- `show-sql` = `false`
- `ddl-auto` = `validate`

---

## 🏗️ @Profile en Clases Java

### Sintaxis:

```java
@Component
@Profile("dev")  // Solo se carga si profile = dev
public class DataSeeder implements CommandLineRunner {
    // ...
}
```

```java
@Component
@Profile("prod")  // Solo se carga si profile = prod
public class AdminInitializer implements CommandLineRunner {
    // ...
}
```

### ¿Cómo funciona?

Spring escanea las clases al iniciar:
- Si `@Profile("dev")` y el perfil activo es `dev` → **Crea el bean**
- Si `@Profile("dev")` y el perfil activo es `prod` → **NO crea el bean**

**Resultado:** La clase simplemente no existe en el contexto de Spring.

---

## 🔄 Flujo de Ejecución

### Modo Desarrollo (`spring.profiles.active=dev`)

```
1. Spring Boot inicia
   └─ Lee application.properties
      └─ Encuentra: spring.profiles.active=dev
      
2. Spring carga configuraciones:
   ├─ application.properties (base)
   └─ application-dev.properties (sobrescribe)
   
3. Spring escanea componentes:
   ├─ DataSeeder → @Profile("dev") → ✅ ACTIVADO
   └─ AdminInitializer → @Profile("prod") → ❌ IGNORADO
   
4. DataSeeder.run() se ejecuta
   └─ Carga usuarios de prueba
   └─ Carga juegos de ejemplo
   └─ Crea compras ficticias
```

### Modo Producción (`spring.profiles.active=prod`)

```
1. Spring Boot inicia
   └─ Lee application.properties
      └─ Encuentra: spring.profiles.active=prod
      
2. Spring carga configuraciones:
   ├─ application.properties (base)
   └─ application-prod.properties (sobrescribe)
   
3. Spring escanea componentes:
   ├─ DataSeeder → @Profile("dev") → ❌ IGNORADO
   └─ AdminInitializer → @Profile("prod") → ✅ ACTIVADO
   
4. AdminInitializer.run() se ejecuta
   └─ Verifica si existe admin
   └─ Si no existe, crea uno
```

---

## 🎛️ Formas de Activar un Profile

### 1. En `application.properties`
```properties
spring.profiles.active=dev
```

✅ **Ventaja:** Simple  
❌ **Desventaja:** Hay que cambiarlo manualmente antes de desplegar

---

### 2. Variable de entorno
```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar
```

✅ **Ventaja:** No modifica código  
✅ **Ideal para:** Azure, Docker, Kubernetes

---

### 3. Argumento al ejecutar
```bash
java -jar app.jar --spring.profiles.active=prod
```

✅ **Ventaja:** Flexible  
❌ **Desventaja:** Fácil de olvidar

---

### 4. En Maven
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

✅ **Ventaja:** Útil en desarrollo  

---

### 5. En IntelliJ IDEA
```
Run → Edit Configurations
├─ Active profiles: dev
└─ Environment variables: SPRING_PROFILES_ACTIVE=dev
```

✅ **Ventaja:** Persistente en el IDE

---

### 6. En Azure App Service
```
Configuration → Application settings
├─ Name: SPRING_PROFILES_ACTIVE
└─ Value: prod
```

✅ **Ventaja:** Configuración centralizada  
✅ **Recomendado para producción**

---

## 🔍 Verificar Profile Activo

### En logs al iniciar:
```
The following 1 profile is active: "dev"
```

### Desde código:
```java
@Component
public class ProfileChecker {
    
    @Value("${spring.profiles.active}")
    private String activeProfile;
    
    @PostConstruct
    public void init() {
        System.out.println("🎯 Perfil activo: " + activeProfile);
    }
}
```

### Con endpoint de diagnóstico:
```java
@RestController
public class DiagnosticController {
    
    @Autowired
    private Environment env;
    
    @GetMapping("/api/profile")
    public String getActiveProfile() {
        return env.getActiveProfiles()[0];
    }
}
```

---

## 🎨 Profiles Múltiples

Puedes activar varios perfiles a la vez:

```properties
spring.profiles.active=dev,mysql,debug
```

### Ejemplo:
```java
@Profile("dev")          // Solo dev
@Profile({"dev", "qa"})  // dev O qa
@Profile("!prod")        // Todos EXCEPTO prod
```

---

## 💡 Casos de Uso Reales

### 1. Bases de Datos Diferentes

**application-dev.properties:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pixelshop_dev
```

**application-prod.properties:**
```properties
spring.datasource.url=jdbc:mysql://azure.database.com:3306/pixelshop
```

### 2. APIs Externas

**application-dev.properties:**
```properties
payment.api.url=https://sandbox.stripe.com
payment.api.key=sk_test_123
```

**application-prod.properties:**
```properties
payment.api.url=https://api.stripe.com
payment.api.key=${STRIPE_KEY}  # Variable de entorno
```

### 3. Configuraciones de Email

**application-dev.properties:**
```properties
spring.mail.host=localhost
spring.mail.port=1025  # MailHog
```

**application-prod.properties:**
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USER}
spring.mail.password=${EMAIL_PASS}
```

---

## ⚠️ Errores Comunes

### 1. Profile mal escrito
```properties
# ❌ MAL:
spring.profiles.active=development

# ✅ BIEN:
spring.profiles.active=dev
```

El archivo debe llamarse `application-dev.properties`, no `application-development.properties`.

---

### 2. No existe el archivo de profile
```
Error: Cannot find profile-specific properties file
```

**Solución:** Crear `application-dev.properties` o `application-prod.properties`.

---

### 3. Profile no se activa
```java
@Component
@Profile("dev")
public class DataSeeder {
    // No se ejecuta
}
```

**Causa:** El profile activo no es `dev`.

**Solución:** Verificar `spring.profiles.active` en logs.

---

### 4. Beans duplicados
```
Bean with name 'dataSeeder' is already defined
```

**Causa:** Dos clases con el mismo nombre sin `@Profile`.

**Solución:** Añadir `@Profile` a cada una.

---

## 🏆 Mejores Prácticas

### 1. ✅ Siempre tener un profile por defecto
```properties
spring.profiles.active=dev
```

### 2. ✅ Usar variables de entorno en producción
```properties
# ❌ NO:
spring.datasource.password=mypassword

# ✅ SÍ:
spring.datasource.password=${DB_PASSWORD}
```

### 3. ✅ Nombrar profiles consistentemente
```
dev, qa, staging, prod
```

### 4. ✅ Documentar qué hace cada profile
```properties
# ========================================
# CONFIGURACIÓN DE DESARROLLO
# - Carga datos de prueba
# - Logs detallados
# - Hibernate modifica BD
# ========================================
spring.profiles.active=dev
```

### 5. ✅ Validar profile al iniciar
```java
@PostConstruct
public void validateProfile() {
    if (activeProfile.equals("prod")) {
        Assert.isTrue(csrfEnabled, "CSRF debe estar activado en producción");
    }
}
```

---

## 📊 Comparación: Con vs Sin Profiles

### SIN Profiles (❌ Malo)

**application.properties:**
```properties
# Configuración mezclada
spring.jpa.show-sql=true  # OK para dev, MAL para prod
spring.jpa.hibernate.ddl-auto=update  # PELIGROSO en prod
```

**Problemas:**
- ❌ Misma config en dev y prod
- ❌ Hay que cambiar manualmente antes de desplegar
- ❌ Riesgo de olvidar cambiar
- ❌ Logs sensibles en producción

---

### CON Profiles (✅ Bueno)

**application.properties:**
```properties
spring.profiles.active=dev
```

**application-dev.properties:**
```properties
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update
```

**application-prod.properties:**
```properties
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=validate
```

**Ventajas:**
- ✅ Configuraciones separadas
- ✅ Solo cambiar `spring.profiles.active`
- ✅ Seguro por diseño
- ✅ Fácil de mantener

---

## 🎓 Resumen Ejecutivo

| Aspecto | Desarrollo | Producción |
|---------|-----------|-----------|
| **Profile** | `dev` | `prod` |
| **Archivo** | `application-dev.properties` | `application-prod.properties` |
| **DataSeeder** | `@Profile("dev")` → Activo | `@Profile("dev")` → Inactivo |
| **AdminInitializer** | `@Profile("prod")` → Inactivo | `@Profile("prod")` → Activo |
| **Activación** | `spring.profiles.active=dev` | `SPRING_PROFILES_ACTIVE=prod` |

---

**Conclusión:** Los Spring Profiles son la forma correcta de gestionar configuraciones por entorno, evitando mezclar código de desarrollo con producción.

---

**Fecha:** 21 Noviembre 2025  
**Versión:** 1.0
