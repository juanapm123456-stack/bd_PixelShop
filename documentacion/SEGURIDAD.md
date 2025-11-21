# 🔐 SPRING SECURITY - PixelShop

**Sistema de Autenticación y Autorización**

---

## 📋 Contenido

1. [Introducción](#-introducción)
2. [Configuración de Security](#-configuración-de-security)
3. [Autenticación](#-autenticación)
4. [Autorización por Roles](#-autorización-por-roles)
5. [Encriptación de Contraseñas](#-encriptación-de-contraseñas)
6. [Control de Acceso por URL](#-control-de-acceso-por-url)
7. [Sesiones y Logout](#-sesiones-y-logout)
8. [Casos de Uso de Seguridad](#-casos-de-uso-de-seguridad)

---

## 📖 Introducción

Spring Security es el framework de seguridad utilizado en PixelShop para:

✅ **Autenticación**: Verificar la identidad del usuario (login)  
✅ **Autorización**: Controlar qué puede hacer cada usuario según su rol  
✅ **Protección**: Encriptar contraseñas con BCrypt  
✅ **Sesiones**: Gestionar sesiones de usuario  

---

## ⚙️ Configuración de Security

### Archivo Principal: `SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // URLs públicas (sin login)
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                
                // URLs que requieren autenticación
                .requestMatchers("/", "/juego/**", "/buscar").authenticated()
                
                // Solo CLIENTE y PROVEEDOR pueden comprar
                .requestMatchers("/compra/**").hasAnyRole("CLIENTE", "PROVEEDOR")
                .requestMatchers("/mi-biblioteca").hasAnyRole("CLIENTE", "PROVEEDOR")
                
                // Solo PROVEEDOR y ADMIN pueden acceder a panel proveedor
                .requestMatchers("/proveedor/**").hasAnyRole("PROVEEDOR", "ADMIN")
                
                // Solo ADMIN puede acceder a panel admin
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable()); // DESACTIVADO en desarrollo
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### Anotaciones Clave

#### `@Configuration`
Indica que esta clase contiene configuración de Spring.

#### `@EnableWebSecurity`
Activa Spring Security en la aplicación.

#### `@EnableMethodSecurity(prePostEnabled = true)`
Permite usar anotaciones de seguridad en métodos:
- `@PreAuthorize("hasRole('ADMIN')")`
- `@PostAuthorize`
- `@Secured`

---

## 🔑 Autenticación

### ¿Qué es la Autenticación?

**Respuesta:** Verificar la identidad del usuario (¿quién eres?).

### Proceso de Login

```
1. Usuario visita /login
   ↓
2. Ingresa email y contraseña
   ↓
3. Spring Security busca el usuario en la BD
   ↓
4. Verifica la contraseña con BCrypt
   ↓
5. Si es correcta → Crea sesión y redirige a /
   ↓
6. Si es incorrecta → Redirige a /login?error=true
```

---

### Implementación del Login

#### Configuración en `SecurityConfig`

```java
.formLogin(form -> form
    .loginPage("/login")                  // URL del formulario de login
    .defaultSuccessUrl("/", true)         // A dónde ir si login exitoso
    .failureUrl("/login?error=true")      // A dónde ir si falla
    .permitAll()                          // Todos pueden acceder al login
)
```

---

#### Controlador de Login

```java
@Controller
public class AuthController {
    
    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(required = false) String error, 
                               @RequestParam(required = false) String logout,
                               Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Email o contraseña incorrectos");
        }
        
        if (logout != null) {
            model.addAttribute("mensaje", "Has cerrado sesión correctamente");
        }
        
        return "auth/login";
    }
}
```

---

#### Vista de Login (Thymeleaf)

```html
<form th:action="@{/login}" method="post">
    
    <!-- Spring Security espera estos nombres EXACTOS -->
    <input type="text" name="username" placeholder="Email" required />
    <input type="password" name="password" placeholder="Contraseña" required />
    
    <button type="submit">Iniciar Sesión</button>
    
    <!-- Mensajes de error -->
    <div th:if="${error}" class="alert error">
        <p th:text="${error}"></p>
    </div>
    
    <!-- Mensaje de logout -->
    <div th:if="${mensaje}" class="alert success">
        <p th:text="${mensaje}"></p>
    </div>
</form>
```

**⚠️ IMPORTANTE:**
- El input debe llamarse `username` (aunque uses email)
- El input debe llamarse `password`
- El formulario debe usar `method="post"`
- Spring Security intercepta automáticamente `/login` con POST

---

### UserDetailsService (Autenticación Personalizada)

Para que Spring Security sepa cómo buscar usuarios en tu BD:

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        // Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
        
        // Verificar si está activo
        if (!usuario.getActivo()) {
            throw new DisabledException("Usuario desactivado");
        }
        
        // Convertir a UserDetails de Spring Security
        return User.builder()
            .username(usuario.getEmail())
            .password(usuario.getPassword()) // Ya está encriptada
            .roles(usuario.getRol().name())  // ADMIN, PROVEEDOR, CLIENTE
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(!usuario.getActivo())
            .build();
    }
}
```

**¿Qué hace este código?**
1. Spring Security llama a `loadUserByUsername(email)`
2. Buscamos el usuario en la BD
3. Verificamos si está activo
4. Convertimos nuestro `Usuario` a `UserDetails` de Spring
5. Spring compara la contraseña automáticamente

---

## 🛡️ Autorización por Roles

### ¿Qué es la Autorización?

**Respuesta:** Controlar qué puede hacer un usuario autenticado (¿qué permisos tienes?).

---

### Roles en PixelShop

```java
public enum Rol {
    ADMIN,      // Administrador de la plataforma
    PROVEEDOR,  // Publicador de juegos
    CLIENTE     // Comprador de juegos
}
```

---

### Tabla de Permisos

| URL / Recurso | ADMIN | PROVEEDOR | CLIENTE | Público |
|--------------|-------|-----------|---------|---------|
| `/login` | ✅ | ✅ | ✅ | ✅ |
| `/register` | ✅ | ✅ | ✅ | ✅ |
| `/` (home) | ✅ | ✅ | ✅ | ❌ |
| `/catalogo` | ✅ | ✅ | ✅ | ❌ |
| `/juego/{id}` | ✅ | ✅ | ✅ | ❌ |
| `/comprar/{id}` | ❌ | ✅ | ✅ | ❌ |
| `/usuario/mi-biblioteca` | ❌ | ✅ | ✅ | ❌ |
| `/usuario/mis-compras` | ❌ | ✅ | ✅ | ❌ |
| `/proveedor/publicar` | ✅ | ✅ | ❌ | ❌ |
| `/proveedor/mis-juegos` | ✅ | ✅ | ❌ | ❌ |
| `/proveedor/ventas` | ✅ | ✅ | ❌ | ❌ |
| `/admin/usuarios` | ✅ | ❌ | ❌ | ❌ |
| `/admin/ganancias` | ✅ | ❌ | ❌ | ❌ |
| `/admin/movimientos` | ✅ | ❌ | ❌ | ❌ |

---

### Configuración de Autorización

#### 1. URLs Públicas (sin login)

```java
.requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
```

**Ejemplos:**
- `/login` → Página de login
- `/register` → Página de registro
- `/css/global.css` → Hojas de estilo
- `/js/login-animation.js` → Scripts JavaScript
- `/images/juegos/game1.jpg` → Imágenes

---

#### 2. URLs que Requieren Autenticación

```java
.requestMatchers("/", "/juego/**", "/buscar").authenticated()
```

**Ejemplos:**
- `/` → Home (cualquier usuario logueado)
- `/juego/123` → Detalle del juego
- `/buscar?q=mario` → Búsqueda

---

#### 3. URLs por Rol Específico

```java
// Solo CLIENTE y PROVEEDOR
.requestMatchers("/comprar/**").hasAnyRole("CLIENTE", "PROVEEDOR")
.requestMatchers("/mi-biblioteca").hasAnyRole("CLIENTE", "PROVEEDOR")

// Solo PROVEEDOR y ADMIN
.requestMatchers("/proveedor/**").hasAnyRole("PROVEEDOR", "ADMIN")

// Solo ADMIN
.requestMatchers("/admin/**").hasRole("ADMIN")
```

---

#### 4. Regla por Defecto

```java
.anyRequest().authenticated()
```

**Significa:** Cualquier otra URL requiere estar autenticado.

---

### Métodos de Autorización

#### `permitAll()`
Permite el acceso a todos (sin login).

```java
.requestMatchers("/login").permitAll()
```

---

#### `authenticated()`
Requiere que el usuario esté logueado (cualquier rol).

```java
.requestMatchers("/catalogo").authenticated()
```

---

#### `hasRole("ROLE")`
Requiere un rol específico.

```java
.requestMatchers("/admin/**").hasRole("ADMIN")
```

**⚠️ IMPORTANTE:** Spring Security añade automáticamente el prefijo `ROLE_`.
- En código: `hasRole("ADMIN")`
- En BD: `rol = ADMIN`
- Spring busca: `ROLE_ADMIN`

---

#### `hasAnyRole("ROLE1", "ROLE2")`
Requiere al menos uno de los roles especificados.

```java
.requestMatchers("/comprar/**").hasAnyRole("CLIENTE", "PROVEEDOR")
```

---

### Protección en Controladores

Además de la configuración global, puedes proteger métodos específicos:

#### Con `@PreAuthorize`

```java
@Controller
public class AdminController {
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/usuarios")
    public String listarUsuarios(Model model) {
        // Solo accesible para ADMIN
        List<Usuario> usuarios = servicioUsuario.listarTodosLosUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "admin/usuarios";
    }
}
```

---

#### Con Validación Manual

```java
@Controller
public class CompraController {
    
    @PostMapping("/comprar/{id}")
    public String comprar(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        
        Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
        
        // Validar si puede comprar
        if (!servicioCompra.puedeRealizarCompras(usuario)) {
            redirectAttributes.addFlashAttribute("error", 
                "Los administradores no pueden comprar juegos");
            return "redirect:/catalogo";
        }
        
        // Continuar con compra...
        return "redirect:/usuario/mi-biblioteca";
    }
}
```

---

## 🔒 Encriptación de Contraseñas

### BCrypt

**¿Qué es?** Algoritmo de encriptación unidireccional muy seguro.

**Características:**
- ✅ No se puede desencriptar (irreversible)
- ✅ Añade "salt" aleatorio automáticamente
- ✅ Cada encriptación genera un hash diferente
- ✅ Es muy lento (dificulta ataques de fuerza bruta)

---

### Configuración de BCrypt

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

---

### Encriptar Contraseña al Registrar

```java
@Service
public class ServicioUsuario implements IServicioUsuario {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public Usuario registrarNuevoUsuario(Usuario usuario) {
        
        // Encriptar contraseña ANTES de guardar
        String passwordEncriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(passwordEncriptada);
        
        // Establecer valores por defecto
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setActivo(true);
        
        // Guardar en BD
        return usuarioRepository.save(usuario);
    }
    
    @Override
    public String encriptarContrasena(String contrasenaPlana) {
        return passwordEncoder.encode(contrasenaPlana);
    }
}
```

---

### Ejemplo de Encriptación

```java
String passwordOriginal = "miPassword123";
String passwordEncriptada = passwordEncoder.encode(passwordOriginal);

System.out.println("Original:    " + passwordOriginal);
System.out.println("Encriptada:  " + passwordEncriptada);

// Output:
// Original:    miPassword123
// Encriptada:  $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

**Características:**
- Siempre empieza con `$2a$` (versión de BCrypt)
- `10` es el "cost factor" (número de rondas)
- El resto es el salt + hash

---

### Verificación Automática

Spring Security verifica automáticamente la contraseña:

```java
// Usuario ingresa: "miPassword123"
// BD tiene: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

// Spring Security:
boolean matches = passwordEncoder.matches("miPassword123", usuarioDeBD.getPassword());
// matches = true ✅

// Si el usuario ingresa mal:
boolean matches = passwordEncoder.matches("passwordIncorrecta", usuarioDeBD.getPassword());
// matches = false ❌
```

---

## 🌐 Control de Acceso por URL

### Flujo de Peticiones

```
1. Usuario hace petición a /admin/usuarios
   ↓
2. Spring Security intercepta
   ↓
3. ¿Está autenticado?
   ├─ NO → Redirige a /login
   └─ SÍ → Continúa
   ↓
4. ¿Tiene rol ADMIN?
   ├─ NO → Error 403 Forbidden
   └─ SÍ → Permite acceso
   ↓
5. Ejecuta el controlador
```

---

### Redirección Automática

Si un usuario intenta acceder a una URL sin permisos:

```java
// Usuario CLIENTE intenta acceder a /admin/usuarios
// Spring Security:
// 1. Verifica que está autenticado ✅
// 2. Verifica que tiene rol ADMIN ❌
// 3. Retorna error 403 Forbidden
```

---

### Personalizar Página de Error 403

```java
.exceptionHandling(ex -> ex
    .accessDeniedPage("/error/403")
)
```

---

## 🚪 Sesiones y Logout

### Gestión de Sesiones

#### Configuración de Logout

```java
.logout(logout -> logout
    .logoutUrl("/logout")                       // URL para cerrar sesión
    .logoutSuccessUrl("/login?logout=true")     // A dónde ir después
    .invalidateHttpSession(true)                // Invalidar sesión
    .deleteCookies("JSESSIONID")                // Eliminar cookie de sesión
    .permitAll()                                // Todos pueden hacer logout
)
```

---

#### Vista con Botón de Logout

```html
<nav>
    <a href="/catalogo">Catálogo</a>
    <a href="/usuario/mi-biblioteca">Mi Biblioteca</a>
    <a href="/usuario/perfil">Perfil</a>
    
    <!-- Formulario de logout -->
    <form th:action="@{/logout}" method="post" style="display: inline;">
        <button type="submit">Cerrar Sesión</button>
    </form>
</nav>
```

**⚠️ IMPORTANTE:** El logout debe ser POST, no GET.

---

### Obtener Usuario Actual

#### Método 1: Con `Principal`

```java
@GetMapping("/catalogo")
public String verCatalogo(Principal principal, Model model) {
    
    // Obtener email del usuario autenticado
    String email = principal.getName();
    
    Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(email);
    
    model.addAttribute("usuario", usuario);
    return "catalogo/index";
}
```

---

#### Método 2: Con `Authentication`

```java
@GetMapping("/catalogo")
public String verCatalogo(Authentication authentication, Model model) {
    
    // Obtener email
    String email = authentication.getName();
    
    // Obtener roles
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    
    Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(email);
    model.addAttribute("usuario", usuario);
    
    return "catalogo/index";
}
```

---

#### Método 3: Con `@AuthenticationPrincipal`

```java
@GetMapping("/catalogo")
public String verCatalogo(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    
    String email = userDetails.getUsername();
    
    Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(email);
    model.addAttribute("usuario", usuario);
    
    return "catalogo/index";
}
```

---

### En Thymeleaf (Vista)

#### Mostrar nombre del usuario

```html
<p>Bienvenido, <span sec:authentication="name"></span></p>
```

---

#### Mostrar contenido según rol

```html
<!-- Solo visible para ADMIN -->
<div sec:authorize="hasRole('ADMIN')">
    <a href="/admin/usuarios">Panel de Administración</a>
</div>

<!-- Solo visible para PROVEEDOR -->
<div sec:authorize="hasRole('PROVEEDOR')">
    <a href="/proveedor/publicar">Publicar Juego</a>
</div>

<!-- Solo visible para CLIENTE -->
<div sec:authorize="hasRole('CLIENTE')">
    <a href="/usuario/mis-compras">Mis Compras</a>
</div>
```

**⚠️ Necesitas añadir el namespace en el HTML:**
```html
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
```

---

## 🎯 Casos de Uso de Seguridad

### Caso 1: Registro de Usuario

```java
@PostMapping("/registro")
public String registrar(@RequestParam String nombre,
                       @RequestParam String email,
                       @RequestParam String password,
                       @RequestParam String rol,
                       RedirectAttributes redirectAttributes) {
    
    // 1. Verificar si el email ya existe
    if (servicioUsuario.verificarEmailExistente(email)) {
        redirectAttributes.addFlashAttribute("error", "Email ya registrado");
        return "redirect:/registro";
    }
    
    // 2. Crear usuario
    Usuario nuevoUsuario = new Usuario();
    nuevoUsuario.setNombre(nombre);
    nuevoUsuario.setEmail(email);
    nuevoUsuario.setPassword(password); // SIN ENCRIPTAR
    nuevoUsuario.setRol(Rol.valueOf(rol));
    
    // 3. Registrar (el servicio encripta la contraseña automáticamente)
    Usuario usuarioRegistrado = servicioUsuario.registrarNuevoUsuario(nuevoUsuario);
    
    redirectAttributes.addFlashAttribute("success", 
        "Usuario registrado. Ya puedes iniciar sesión.");
    
    return "redirect:/login";
}
```

---

### Caso 2: Proteger Panel de Administrador

```java
@Controller
public class AdminController {
    
    @Autowired
    private IServicioUsuario servicioUsuario;
    
    @GetMapping("/admin/usuarios")
    public String listarUsuarios(Principal principal, Model model, RedirectAttributes redirectAttributes) {
        
        // 1. Obtener usuario actual
        Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
        
        // 2. Verificar rol (doble verificación)
        if (!servicioUsuario.esAdministrador(usuario)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado");
            return "redirect:/catalogo";
        }
        
        // 3. Obtener lista de usuarios
        List<Usuario> usuarios = servicioUsuario.listarTodosLosUsuarios();
        
        model.addAttribute("usuarios", usuarios);
        
        return "admin/usuarios";
    }
}
```

---

### Caso 3: Validar Compra según Rol

```java
@Controller
public class CompraController {
    
    @PostMapping("/comprar/{id}")
    public String comprar(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        
        // 1. Obtener usuario
        Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
        
        // 2. Validar si puede comprar (ADMIN no puede)
        if (!servicioCompra.puedeRealizarCompras(usuario)) {
            redirectAttributes.addFlashAttribute("error", 
                "Los administradores no pueden comprar juegos");
            return "redirect:/catalogo";
        }
        
        // 3. Obtener juego
        Juego juego = servicioJuego.buscarJuegoPorId(id);
        
        // 4. Verificar si ya lo compró
        if (servicioCompra.verificarJuegoYaComprado(usuario, juego)) {
            redirectAttributes.addFlashAttribute("error", 
                "Ya tienes este juego");
            return "redirect:/catalogo";
        }
        
        // 5. Crear compra
        servicioCompra.crearNuevaCompra(usuario, juego);
        
        redirectAttributes.addFlashAttribute("success", 
            "¡Compra realizada con éxito!");
        
        return "redirect:/usuario/mi-biblioteca";
    }
}
```

---

## 📝 Resumen de Spring Security

### Conceptos Clave

1. **Autenticación**: Verificar identidad (login)
2. **Autorización**: Controlar permisos (roles)
3. **BCrypt**: Encriptación de contraseñas
4. **SecurityFilterChain**: Configuración de URLs protegidas
5. **UserDetailsService**: Cargar usuarios desde BD

---

### Anotaciones Importantes

- `@Configuration` - Clase de configuración
- `@EnableWebSecurity` - Activa Spring Security
- `@EnableMethodSecurity` - Permite proteger métodos
- `@PreAuthorize` - Validar rol antes de ejecutar método

---

### Métodos de Autorización

- `.permitAll()` - Acceso público
- `.authenticated()` - Requiere login
- `.hasRole("ROLE")` - Requiere rol específico
- `.hasAnyRole("ROLE1", "ROLE2")` - Requiere uno de varios roles

---

### Flujo de Seguridad

```
Usuario → Login → Spring Security → UserDetailsService → BD
                       ↓
                Verificar contraseña (BCrypt)
                       ↓
                ¿Es correcta?
                ├─ SÍ → Crear sesión → Redirigir a home
                └─ NO → Error → Redirigir a login
```

---

**Tu aplicación ahora está protegida con Spring Security, siguiendo las mejores prácticas de seguridad.** 🔐

Para más información, consulta:
- [ARQUITECTURA.md](ARQUITECTURA.md) - Arquitectura del sistema
- [SERVICIOS.md](SERVICIOS.md) - Servicios implementados
- [MODELO_NEGOCIO.md](MODELO_NEGOCIO.md) - Sistema de comisiones
