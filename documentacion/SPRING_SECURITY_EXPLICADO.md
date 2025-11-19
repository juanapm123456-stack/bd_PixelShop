# 🔒 GUÍA SIMPLE: SPRING SECURITY EN PIXELSHOP

## 📋 ÍNDICE
1. [¿Qué es Spring Security?](#qué-es-spring-security)
2. [¿Por qué se implementó?](#por-qué-se-implementó)
3. [¿Qué problemas resuelve?](#qué-problemas-resuelve)
4. [Cómo funciona paso a paso](#cómo-funciona-paso-a-paso)
5. [Archivos y configuración](#archivos-y-configuración)
6. [Encriptación de contraseñas](#encriptación-de-contraseñas)
7. [Control de acceso por roles](#control-de-acceso-por-roles)
8. [Resumen visual](#resumen-visual)

---

## 🛡️ ¿QUÉ ES SPRING SECURITY?

Spring Security es como un **guardia de seguridad** para tu aplicación web. Se encarga de:

1. ✅ Verificar quién eres (autenticación)
2. ✅ Verificar qué puedes hacer (autorización)
3. ✅ Proteger las contraseñas
4. ✅ Bloquear accesos no autorizados
5. ✅ Gestionar sesiones de usuario

**Analogía simple:** Es como el portero de un edificio que:
- Te pide identificación antes de entrar
- Verifica si puedes subir a ciertos pisos
- No deja pasar a desconocidos

---

## 🎯 ¿POR QUÉ SE IMPLEMENTÓ?

### Objetivos principales:

1. **Proteger información sensible**
   - Que nadie vea las compras de otros usuarios
   - Que solo los proveedores puedan publicar juegos
   - Que solo los admins puedan ver estadísticas

2. **Cifrar las contraseñas**
   - Las contraseñas NO se guardan en texto plano
   - Se usa encriptación BCrypt (irreversible)

3. **Gestionar roles de usuario**
   - ADMIN: Acceso completo
   - PROVEEDOR: Puede publicar y vender juegos
   - CLIENTE: Puede comprar juegos

4. **Evitar accesos no autorizados**
   - Si no has iniciado sesión → te redirige al login
   - Si intentas acceder a algo que no te corresponde → te bloquea

---

## 🔧 ¿QUÉ PROBLEMAS RESUELVE?

### Problema 1: Contraseñas expuestas
❌ **Sin Spring Security:**
```
Usuario: juan@email.com
Contraseña en BD: miPassword123  ← Se ve tal cual
```

✅ **Con Spring Security:**
```
Usuario: juan@email.com
Contraseña en BD: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```
☝️ Nadie puede descifrar esto, ni siquiera el administrador de la BD

### Problema 2: Acceso sin control
❌ **Sin Spring Security:**
- Cualquiera puede ver `/admin/ganancias` escribiendo la URL
- Un cliente podría acceder a `/proveedor/publicar`

✅ **Con Spring Security:**
- Te redirige al login si no estás autenticado
- Te bloquea si tu rol no tiene permiso

### Problema 3: No saber quién está conectado
❌ **Sin Spring Security:**
- No sabes quién hizo una compra
- No puedes personalizar la experiencia

✅ **Con Spring Security:**
- Siempre sabes quién está logueado: `@AuthenticationPrincipal UserDetails userDetails`
- Puedes mostrar "Hola, Juan" en la interfaz

---

## 🔄 CÓMO FUNCIONA PASO A PASO

### PASO 1: Usuario se registra

**Archivo:** `src/main/java/com/example/controller/AuthController.java`  
**Método:** `registrar()`  
**Líneas:** 37-56

```java
@PostMapping("/register")
public String registrar(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
    // Verificar si email ya existe
    if (usuarioRepository.existsByEmail(usuario.getEmail())) {
        redirectAttributes.addFlashAttribute("error", "El email ya está registrado");
        return "redirect:/register";
    }
    
    // 🔐 CIFRAR LA CONTRASEÑA (línea 45)
    usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
    
    // Por defecto: rol CLIENTE
    usuario.setRol(Rol.CLIENTE);
    usuario.setFechaRegistro(LocalDateTime.now());
    usuario.setActivo(true);
    
    usuarioRepository.save(usuario);
    
    redirectAttributes.addFlashAttribute("success", "Registro exitoso. Inicia sesión");
    return "redirect:/login";
}
```

#### ¿Qué hace la línea 45?

```java
usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
```

**Antes de esta línea:**
```
Contraseña del usuario: "miPassword123"
```

**Después de esta línea:**
```
Contraseña cifrada: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
```

**¿Por qué?** Para que nadie (ni tú como desarrollador) pueda ver las contraseñas reales en la base de datos.

---

### PASO 2: Usuario inicia sesión

**Archivo:** `src/main/java/com/example/service/UserDetailsServiceImpl.java`  
**Método:** `loadUserByUsername()`  
**Líneas:** 24-39

```java
@Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    // 1. Buscar usuario en la base de datos por email (línea 25)
    Usuario usuario = usuarioRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    
    // 2. Verificar si está activo (línea 28)
    if (!usuario.getActivo()) {
        throw new UsernameNotFoundException("Usuario inactivo");
    }
    
    // 3. Asignar su ROL (líneas 32-33)
    List<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    
    // 4. Crear objeto User de Spring Security (líneas 35-39)
    return new User(
        usuario.getEmail(),           // Username (en tu caso es el email)
        usuario.getPassword(),         // Contraseña CIFRADA de la BD
        authorities                    // ROLE_ADMIN, ROLE_CLIENTE, ROLE_PROVEEDOR
    );
}
```

#### ¿Qué pasa cuando escribes tu contraseña en el login?

1. Escribes: `miPassword123`
2. Spring Security la cifra automáticamente con BCrypt
3. Compara el resultado con lo que está en la BD
4. Si coincide → Acceso permitido ✅
5. Si NO coincide → "Contraseña incorrecta" ❌

**Importante:** La contraseña NUNCA se descifra. Se vuelve a cifrar tu input y se compara.

---

### PASO 3: Spring Security verifica los permisos

**Archivo:** `src/main/java/com/example/config/SecurityConfig.java`  
**Método:** `securityFilterChain()`  
**Líneas:** 18-45

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            // RUTAS PÚBLICAS (línea 21) - No necesitan login
            .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/static/**").permitAll()
            
            // RUTAS QUE NECESITAN LOGIN (línea 22)
            .requestMatchers("/", "/juego/**", "/buscar").authenticated()
            
            // RUTAS SOLO PARA CLIENTE Y PROVEEDOR (líneas 24-25)
            .requestMatchers("/compra/**").hasAnyRole("CLIENTE", "PROVEEDOR")
            .requestMatchers("/mi-biblioteca").hasAnyRole("CLIENTE", "PROVEEDOR")
            
            // RUTAS SOLO PARA USUARIOS LOGUEADOS (línea 26)
            .requestMatchers("/perfil").authenticated()
            
            // RUTAS SOLO PARA PROVEEDOR Y ADMIN (línea 27)
            .requestMatchers("/proveedor/**").hasAnyRole("PROVEEDOR", "ADMIN")
            
            // RUTAS SOLO PARA ADMIN (línea 28)
            .requestMatchers("/admin/**").hasRole("ADMIN")
            
            // CUALQUIER OTRA RUTA NECESITA LOGIN (línea 29)
            .anyRequest().authenticated()
        )
        // CONFIGURACIÓN DEL LOGIN (líneas 31-36)
        .formLogin(form -> form
            .loginPage("/login")                    // Página personalizada de login
            .defaultSuccessUrl("/", true)           // A dónde ir después del login
            .failureUrl("/login?error=true")        // Si falla el login
            .permitAll()
        )
        // CONFIGURACIÓN DEL LOGOUT (líneas 37-43)
        .logout(logout -> logout
            .logoutUrl("/logout")                   // URL para cerrar sesión
            .logoutSuccessUrl("/login?logout=true") // A dónde ir después del logout
            .invalidateHttpSession(true)            // Destruir la sesión
            .deleteCookies("JSESSIONID")            // Borrar la cookie de sesión
            .permitAll()
        )
        .csrf(csrf -> csrf.disable()); // Solo para desarrollo
    
    return http.build();
}
```

---

## 📂 ARCHIVOS Y CONFIGURACIÓN

### 0. pom.xml - CONFIGURACIÓN INICIAL (¡MUY IMPORTANTE!)

**Ubicación:** `pom.xml` (raíz del proyecto)

**Propósito:** Agregar las dependencias necesarias para que Spring Security funcione.

#### Dependencias clave para Spring Security:

##### Líneas 46-49: Spring Security (la más importante)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```
**¿Qué hace?** Esta dependencia instala Spring Security en tu proyecto. Sin esto, NADA de seguridad funcionaría.

##### Líneas 50-53: Thymeleaf + Spring Security
```xml
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```
**¿Qué hace?** Permite usar Spring Security directamente en tus plantillas HTML de Thymeleaf.

**Ejemplo de uso en HTML:**
```html
<!-- Mostrar nombre del usuario logueado -->
<span sec:authentication="name"></span>

<!-- Mostrar contenido solo si tiene rol ADMIN -->
<div sec:authorize="hasRole('ADMIN')">
    <a href="/admin/panel">Panel Admin</a>
</div>

<!-- Mostrar si está autenticado -->
<div sec:authorize="isAuthenticated()">
    <a href="/logout">Cerrar sesión</a>
</div>
```

#### Otras dependencias importantes:

##### Líneas 32-35: Spring Data JPA
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```
**¿Para qué?** Gestionar la base de datos (guardar usuarios, contraseñas cifradas, etc.)

##### Líneas 60-63: MySQL Connector
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```
**¿Para qué?** Conectar con la base de datos MySQL donde se guardan los usuarios.

### ⚙️ application.properties

**Ubicación:** `src/main/resources/application.properties`

Este archivo NO necesita configuración especial para Spring Security, pero SÍ para la base de datos:

```properties
# Conexión a MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/pixelshop_db
spring.datasource.username=root
spring.datasource.password=tu_password

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Puerto del servidor
server.port=8080
```

**Importante:** Spring Security funciona automáticamente una vez añadida la dependencia. No necesitas configurar nada en `application.properties` para que la seguridad funcione.

---

### 1. SecurityConfig.java
**Ubicación:** `src/main/java/com/example/config/SecurityConfig.java`

**Propósito:** Configurar las reglas de seguridad de toda la aplicación.

**Funciones principales:**
- Define qué URLs son públicas y cuáles protegidas
- Configura el sistema de login
- Define el encriptador de contraseñas (BCrypt)

**Anotaciones importantes:**

#### Línea 12:
```java
@Configuration
```
Le dice a Spring que esta clase contiene configuración.

#### Línea 13:
```java
@EnableWebSecurity
```
Activa Spring Security en el proyecto.

#### Línea 14:
```java
@EnableMethodSecurity(prePostEnabled = true)
```
Permite usar anotaciones de seguridad en los métodos (como `@PreAuthorize`).

---

### 2. UserDetailsServiceImpl.java
**Ubicación:** `src/main/java/com/example/service/UserDetailsServiceImpl.java`

**Propósito:** Cargar los datos del usuario desde la base de datos cuando intenta iniciar sesión.

**Función principal:**
- Buscar el usuario por email
- Verificar si está activo
- Asignar sus roles
- Devolver un objeto `UserDetails` que Spring Security entiende

**Línea 24:** Método principal
```java
public UserDetails loadUserByUsername(String email)
```
Este método se ejecuta automáticamente cuando alguien intenta hacer login.

---

### 3. AuthController.java
**Ubicación:** `src/main/java/com/example/controller/AuthController.java`

**Propósito:** Gestionar el registro de nuevos usuarios.

**Funciones:**
- Mostrar el formulario de registro
- Verificar que el email no esté duplicado
- **CIFRAR la contraseña antes de guardarla** (línea 45)
- Asignar el rol por defecto (CLIENTE)

---

### 4. Usuario.java (modelo)
**Ubicación:** `src/main/java/com/example/model/Usuario.java`

**Campo de contraseña (línea 22):**
```java
@Column(nullable = false)
private String password;
```

Aquí se guarda la contraseña **CIFRADA**. Nunca la contraseña en texto plano.

**Campo de rol (línea 24-26):**
```java
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private Rol rol;
```

Guarda el rol del usuario: ADMIN, PROVEEDOR o CLIENTE.

---

## 🚀 PASOS PARA IMPLEMENTAR SPRING SECURITY DESDE CERO

Si estás empezando un proyecto nuevo, estos son los pasos en orden:

### PASO 1: Añadir dependencias en pom.xml

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Integración con Thymeleaf (opcional pero recomendado) -->
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

Después de añadir esto, ejecuta:
```bash
mvn clean install
```

### PASO 2: Crear SecurityConfig.java

Crea el archivo `src/main/java/com/example/config/SecurityConfig.java` con la configuración básica.

### PASO 3: Crear UserDetailsServiceImpl.java

Crea el servicio que cargará los usuarios desde tu base de datos.

### PASO 4: Modificar tu controlador de registro

Añade el `PasswordEncoder` para cifrar contraseñas al registrar usuarios.

### PASO 5: Actualizar tus plantillas HTML (opcional)

Usa las etiquetas de Thymeleaf Security para mostrar/ocultar contenido según roles:

```html
<!-- En header.html -->
<div sec:authorize="isAuthenticated()">
    <span sec:authentication="name"></span>
    <a href="/logout">Cerrar sesión</a>
</div>

<div sec:authorize="hasRole('ADMIN')">
    <a href="/admin/panel">Panel Administrador</a>
</div>
```

---

## 🔐 ENCRIPTACIÓN DE CONTRASEÑAS

### ¿Cómo funciona BCrypt?

**Archivo:** `SecurityConfig.java`  
**Líneas:** 48-51

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

#### ¿Qué es BCrypt?

Es un algoritmo de cifrado **irreversible**. Significa:
- ✅ Puedes cifrar: `"miPassword123"` → `"$2a$10$xyz..."`
- ❌ NO puedes descifrar: `"$2a$10$xyz..."` → `???`

#### ¿Cómo verifica Spring Security si la contraseña es correcta?

```
1. Usuario escribe: "miPassword123"
2. Spring Security la cifra con BCrypt: "$2a$10$abc..."
3. Compara con la BD: "$2a$10$xyz..."
4. Si coinciden → Login exitoso
```

**Nota importante:** Aunque dos personas tengan la misma contraseña, el resultado cifrado será diferente gracias a un "salt" aleatorio que BCrypt añade.

### Ejemplo real:

```java
// En el registro (AuthController.java, línea 45)
usuario.setPassword(passwordEncoder.encode("miPassword123"));

// Lo que se guarda en la BD:
// $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

Si miras la tabla `usuarios` en MySQL, verás algo así:

| id | email | password | rol |
|----|-------|----------|-----|
| 1 | admin@admin.com | $2a$10$N9qo8uLOickgx2ZMRZoMye... | ADMIN |
| 2 | juan@email.com | $2a$10$Xtz9p2LDickgx3AMRZoQwe... | CLIENTE |

---

## 👥 CONTROL DE ACCESO POR ROLES

### Los 3 roles del sistema:

**Archivo:** `src/main/java/com/example/model/Rol.java`

```java
public enum Rol {
    ADMIN,
    PROVEEDOR,
    CLIENTE
}
```

### Matriz de permisos:

| Ruta | ADMIN | PROVEEDOR | CLIENTE |
|------|-------|-----------|---------|
| `/login`, `/register` | ✅ | ✅ | ✅ |
| `/`, `/juego/**` | ✅ | ✅ | ✅ |
| `/compra/**` | ❌ | ✅ | ✅ |
| `/mi-biblioteca` | ❌ | ✅ | ✅ |
| `/proveedor/**` | ✅ | ✅ | ❌ |
| `/admin/**` | ✅ | ❌ | ❌ |

### ¿Cómo se implementa?

#### En SecurityConfig.java (líneas 21-29):

```java
// Rutas públicas
.requestMatchers("/login", "/register", "/css/**").permitAll()

// Solo CLIENTE y PROVEEDOR pueden comprar
.requestMatchers("/compra/**").hasAnyRole("CLIENTE", "PROVEEDOR")

// Solo PROVEEDOR y ADMIN pueden publicar juegos
.requestMatchers("/proveedor/**").hasAnyRole("PROVEEDOR", "ADMIN")

// Solo ADMIN puede ver estadísticas
.requestMatchers("/admin/**").hasRole("ADMIN")
```

#### En los controladores (con anotaciones):

**Ejemplo 1:** `CompraController.java`
```java
// Cualquier usuario autenticado puede acceder
@Controller
@RequestMapping("/compra")
public class CompraController { ... }
```

**Ejemplo 2:** `ProveedorController.java`
```java
// Solo PROVEEDOR y ADMIN
@Controller
@RequestMapping("/proveedor")
@PreAuthorize("hasAnyRole('PROVEEDOR', 'ADMIN')")
public class ProveedorController { ... }
```

**Ejemplo 3:** `AdminController.java`
```java
// Solo ADMIN
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController { ... }
```

---

## 🔍 VALIDACIÓN ADICIONAL: Evitar que ADMIN compre juegos

**Archivo:** `CompraController.java`  
**Líneas:** 36-40

```java
// ✅ VALIDACIÓN CRÍTICA: ADMIN NO PUEDE COMPRAR
if (usuario.getRol() == Rol.ADMIN) {
    redirectAttributes.addFlashAttribute("error", "Los administradores no pueden comprar juegos");
    return "redirect:/juego/" + juegoId;
}
```

**¿Por qué?**
- Los admins no deben aparecer en las estadísticas de compras
- Evita conflictos de interés
- Mantiene las métricas limpias

---

## 🖥️ CÓMO OBTENER EL USUARIO ACTUAL EN UN CONTROLADOR

En cualquier método de controlador puedes obtener quién está logueado:

```java
@GetMapping("/mi-biblioteca")
public String miBiblioteca(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    // userDetails.getUsername() contiene el email del usuario logueado
    Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    
    // Ahora tienes acceso a todos los datos del usuario
    String nombre = usuario.getNombre();
    Rol rol = usuario.getRol();
    
    return "usuario/mi-biblioteca";
}
```

**Ejemplo en tu código:** `CompraController.java`, líneas 30-31

```java
Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
```

---

## 🔄 FLUJO COMPLETO DE SEGURIDAD

```
┌─────────────────────────────────────────────────────────────┐
│                   CICLO DE VIDA DE UNA PETICIÓN             │
└─────────────────────────────────────────────────────────────┘

1. Usuario escribe URL: http://localhost:8080/compra/mis-compras
                              │
                              v
2. Spring Security intercepta la petición
                              │
                              v
3. ¿Está autenticado? (¿Hay sesión activa?)
                              │
                ┌─────────────┴─────────────┐
                │                           │
               NO                          SÍ
                │                           │
                v                           v
    Redirige a /login          4. ¿Tiene el rol necesario?
                                            │
                              ┌─────────────┴─────────────┐
                              │                           │
                             NO                          SÍ
                              │                           │
                              v                           v
                   403 Forbidden              5. Permitir acceso
                   "Acceso denegado"             Ejecutar controlador
                                                          │
                                                          v
                                              6. Mostrar página
```

---

## 📊 RESUMEN EJECUTIVO

### ¿Qué hace Spring Security en PixelShop?

| Función | ¿Cómo? | Archivo / Línea |
|---------|--------|-----------------|
| **Cifrar contraseñas** | BCrypt (irreversible) | `AuthController.java`, línea 45 |
| **Verificar login** | Buscar usuario en BD y comparar contraseñas | `UserDetailsServiceImpl.java`, líneas 24-39 |
| **Bloquear URLs** | Configuración de permisos | `SecurityConfig.java`, líneas 21-29 |
| **Asignar roles** | ADMIN, PROVEEDOR, CLIENTE | `UserDetailsServiceImpl.java`, línea 33 |
| **Gestionar sesiones** | Cookies y JSESSIONID | `SecurityConfig.java`, líneas 37-43 |
| **Evitar compras de ADMIN** | Validación manual | `CompraController.java`, línea 36 |

### Archivos clave:

1. **pom.xml** → Dependencias necesarias (Spring Security, Thymeleaf Security)
2. **SecurityConfig.java** → Configuración general de seguridad
3. **UserDetailsServiceImpl.java** → Carga datos del usuario al hacer login
4. **AuthController.java** → Registro de usuarios (cifrado de contraseña)
5. **CompraController.java** → Validación adicional (admin no puede comprar)

### Conceptos importantes:

✅ **Autenticación**: Verificar quién eres (email + contraseña)  
✅ **Autorización**: Verificar qué puedes hacer (roles)  
✅ **Encriptación**: BCrypt para contraseñas (irreversible)  
✅ **Sesión**: Cookie JSESSIONID que identifica al usuario logueado  
✅ **Roles**: ADMIN, PROVEEDOR, CLIENTE (cada uno con permisos diferentes)

---

## ❓ PREGUNTAS FRECUENTES

### ¿Por qué no puedo ver la contraseña en la base de datos?
Porque está cifrada con BCrypt. Es una medida de seguridad. Ni tú como desarrollador puedes verla.

### ¿Se puede descifrar la contraseña?
No, BCrypt es irreversible. Si un usuario olvida su contraseña, hay que crear una nueva, no recuperar la antigua.

### ¿Qué pasa si alguien copia la contraseña cifrada de la BD?
No sirve de nada. BCrypt compara el hash generado, no la contraseña directa. Copiar el hash no da acceso.

### ¿Por qué algunos métodos tienen `@PreAuthorize`?
Es una capa extra de seguridad. Valida el rol antes de ejecutar el método, incluso si Spring Security ya validó la URL.

### ¿Qué es JSESSIONID?
Es una cookie que guarda el ID de tu sesión. Así Spring Security sabe que ya iniciaste sesión y no te pide la contraseña en cada página.

### ¿Por qué CSRF está desactivado?
```java
.csrf(csrf -> csrf.disable()); // Solo para desarrollo
```
Para facilitar el desarrollo. **En producción debe estar ACTIVO** para evitar ataques de falsificación de peticiones.

---

## 🎯 CONCLUSIÓN

Spring Security en PixelShop hace 3 cosas principales:

1. **Protege las contraseñas** con cifrado BCrypt (nadie puede verlas)
2. **Controla quién puede acceder** a cada parte de la aplicación según su rol
3. **Gestiona las sesiones** para saber quién está logueado

Sin Spring Security:
- ❌ Contraseñas en texto plano
- ❌ Cualquiera puede acceder a cualquier URL
- ❌ No hay forma de saber quién está logueado

Con Spring Security:
- ✅ Contraseñas cifradas e imposibles de descifrar
- ✅ Acceso controlado por roles
- ✅ Sesiones gestionadas automáticamente
- ✅ Redirección automática al login si no estás autenticado

---

**Fecha de documentación:** 19 de noviembre de 2025  
**Versión del proyecto:** Spring CRUD Thymeleaf v21  
**Tecnologías:** Spring Security 6, BCrypt, Spring Boot 3
