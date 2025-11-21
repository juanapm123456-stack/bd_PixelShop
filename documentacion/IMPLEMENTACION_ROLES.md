# 🎭 IMPLEMENTACIÓN DEL SISTEMA DE ROLES

**Documentación Completa - PixelShop**

---

## 📋 Contenido

1. [¿Qué es un Rol?](#-qué-es-un-rol)
2. [Los 3 Roles del Sistema](#-los-3-roles-del-sistema)
3. [Implementación Técnica](#-implementación-técnica)
4. [Flujo Completo del Registro](#-flujo-completo-del-registro)
5. [Autenticación con Spring Security](#-autenticación-con-spring-security)
6. [Autorización (Control de Acceso)](#-autorización-control-de-acceso)
7. [Validación en el Backend](#-validación-en-el-backend)
8. [Validación en el Frontend](#-validación-en-el-frontend)
9. [Casos de Uso Prácticos](#-casos-de-uso-prácticos)
10. [Resumen Visual](#-resumen-visual)

---

## 🎯 ¿Qué es un Rol?

Un **rol** es una etiqueta que define **qué puede hacer un usuario** en el sistema.

**Ejemplo en la vida real:**
- En un hospital: Doctor, Enfermero, Paciente
- En PixelShop: CLIENTE, PROVEEDOR, ADMIN

Cada rol tiene **permisos específicos** que determinan:
- ✅ A qué páginas puede acceder
- ✅ Qué acciones puede realizar
- ❌ Qué funcionalidades están restringidas

---

## 👥 Los 3 Roles del Sistema

### 1. **CLIENTE** 🛒

**¿Qué puede hacer?**
- ✅ Comprar juegos
- ✅ Ver su biblioteca de juegos
- ✅ Ver sus compras
- ✅ Ver el catálogo
- ❌ NO puede publicar juegos
- ❌ NO puede acceder al panel de administración

**Páginas permitidas:**
- `/` (Catálogo)
- `/juego/{id}` (Detalle de juego)
- `/comprar/{id}` (Comprar juego)
- `/mi-biblioteca` (Biblioteca)
- `/compra/mis-compras` (Compras)
- `/perfil` (Perfil)

---

### 2. **PROVEEDOR** 🎮

**¿Qué puede hacer?**
- ✅ Publicar juegos (paga 25€)
- ✅ Editar sus juegos
- ✅ Ver sus ventas e ingresos
- ✅ Ver sus juegos publicados
- ✅ **TAMBIÉN puede comprar juegos** (como un cliente)
- ✅ Ver su biblioteca de juegos
- ❌ NO puede acceder al panel de administración

**Páginas permitidas:**
- Todo lo que puede hacer un CLIENTE, MÁS:
- `/proveedor/publicar` (Publicar juego)
- `/proveedor/mis-juegos` (Mis juegos)
- `/proveedor/editar/{id}` (Editar juego)
- `/proveedor/ventas` (Ventas)

---

### 3. **ADMIN** 👔

**¿Qué puede hacer?**
- ✅ Ver todos los usuarios
- ✅ Eliminar usuarios
- ✅ Ver ganancias de la plataforma
- ✅ Ver todos los movimientos financieros
- ✅ Ver el catálogo
- ✅ Acceder a todo el panel de administración
- ❌ **NO puede comprar juegos** (solo supervisar)
- ❌ NO tiene biblioteca de juegos

**Páginas permitidas:**
- `/admin/usuarios` (Usuarios)
- `/admin/ganancias` (Ganancias)
- `/admin/movimientos` (Movimientos)
- `/` (Catálogo - solo ver)
- `/perfil` (Perfil)

**Restricción especial:**
```java
// ADMIN NO puede comprar juegos
if (usuario.getRol() == Rol.ADMIN) {
    // Denegar compra
}
```

---

## 💻 Implementación Técnica

### Paso 1: El Enum Rol

**Archivo:** `src/main/java/com/example/model/Rol.java`

```java
package com.example.model;

public enum Rol {
    CLIENTE, PROVEEDOR, ADMIN
}
```

**¿Qué es un Enum?**
- Es una lista cerrada de valores posibles
- Solo puede ser: `CLIENTE`, `PROVEEDOR` o `ADMIN`
- No se puede crear ningún otro rol

**Ventajas:**
- ✅ Evita errores de escritura ("CLENTE" no compilaría)
- ✅ El IDE te sugiere los valores automáticamente
- ✅ Más seguro que usar Strings

---

### Paso 2: Asociar el Rol al Usuario

**Archivo:** `src/main/java/com/example/model/Usuario.java`

```java
@Entity
@Table(name = "usuarios")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private String email;
    private String password;
    
    // ========== AQUÍ SE DEFINE EL ROL ==========
    @Enumerated(EnumType.STRING)  // 👈 Guarda el texto "CLIENTE" en BD
    @Column(nullable = false)      // 👈 Obligatorio
    private Rol rol;
    
    // ... resto de campos
}
```

**¿Qué hace cada anotación?**

#### `@Enumerated(EnumType.STRING)`
Indica cómo se guarda el enum en la base de datos.

**Opciones:**
1. **EnumType.STRING** ← (Usamos este)
   - Guarda: `"CLIENTE"`, `"PROVEEDOR"`, `"ADMIN"`
   - Ventaja: Se entiende fácilmente en la BD
   
2. **EnumType.ORDINAL** ❌ (NO usar)
   - Guarda: `0`, `1`, `2`
   - Problema: Si cambias el orden del enum, se rompe todo

**Tabla en MySQL:**
```sql
CREATE TABLE usuarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255),
    email VARCHAR(255),
    password VARCHAR(255),
    rol VARCHAR(20) NOT NULL,  -- ← Aquí se guarda "CLIENTE", etc.
    ...
);
```

**Ejemplo de registro en BD:**
```
id | nombre | email           | rol       | password (encrypted)
---+--------+-----------------+-----------+----------------------
1  | Juan   | juan@email.com  | CLIENTE   | $2a$10$abc...
2  | María  | maria@email.com | PROVEEDOR | $2a$10$def...
3  | Admin  | admin@email.com | ADMIN     | $2a$10$ghi...
```

---

## 🔐 Flujo Completo del Registro

### Paso 1: Usuario llena el formulario

**Archivo:** `src/main/resources/templates/auth/register.html`

```html
<form th:action="@{/register}" th:object="${usuario}" method="post">
    
    <input type="text" th:field="*{nombre}" placeholder="Tu nombre" required>
    
    <input type="email" th:field="*{email}" placeholder="tu@email.com" required>
    
    <input type="password" th:field="*{password}" placeholder="Contraseña" required>
    
    <!-- SELECTOR DE ROL -->
    <select th:field="*{rol}" class="form-select">
        <option value="CLIENTE">Cliente - Solo comprar juegos</option>
        <option value="PROVEEDOR">Proveedor - Publicar y vender juegos</option>
    </select>
    
    <button type="submit">Crear Cuenta</button>
</form>
```

**¿Cómo funciona `th:field="*{rol}"`?**
1. Thymeleaf vincula el `<select>` con el campo `rol` del objeto `usuario`
2. El usuario elige: `CLIENTE` o `PROVEEDOR`
3. Al enviar el formulario, el valor seleccionado se asigna a `usuario.setRol(Rol.CLIENTE)`

**Nota:** No se permite crear ADMIN desde el formulario (solo por BD).

---

### Paso 2: El controlador recibe los datos

**Archivo:** `src/main/java/com/example/controller/AuthController.java`

```java
@Controller
public class AuthController {
    
    @Autowired
    private ServicioUsuario servicioUsuario;
    
    @PostMapping("/register")
    public String registrar(@ModelAttribute Usuario usuario, 
                           RedirectAttributes redirectAttributes) {
        try {
            // Delega el registro al servicio
            servicioUsuario.registrarNuevoUsuario(usuario);
            
            redirectAttributes.addFlashAttribute("success", "Registro exitoso");
            return "redirect:/login";
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
}
```

**¿Qué es `@ModelAttribute Usuario usuario`?**
- Spring toma los datos del formulario y crea un objeto `Usuario`
- Automáticamente asigna:
  - `usuario.nombre` = "Juan"
  - `usuario.email` = "juan@email.com"
  - `usuario.password` = "123456"
  - `usuario.rol` = `Rol.CLIENTE` (el que eligió en el select)

---

### Paso 3: El servicio guarda el usuario

**Archivo:** `src/main/java/com/example/service/ServicioUsuario.java`

```java
@Service
public class ServicioUsuario {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public Usuario registrarNuevoUsuario(Usuario usuario) {
        
        // 1. Validar que el email no exista
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        // 2. Encriptar la contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        // 3. Establecer valores por defecto
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setActivo(true);
        
        // 4. Guardar en BD (el ROL ya viene asignado del formulario)
        return usuarioRepository.save(usuario);
    }
}
```

**Resultado en BD:**
```sql
INSERT INTO usuarios (nombre, email, password, rol, fecha_registro, activo)
VALUES ('Juan', 'juan@email.com', '$2a$10$abc...', 'CLIENTE', NOW(), true);
```

**El rol queda guardado:** ✅ `rol = 'CLIENTE'`

---

## 🔒 Autenticación con Spring Security

### Paso 1: Usuario intenta iniciar sesión

**Vista:** `login.html`
```html
<form th:action="@{/login}" method="post">
    <input type="email" name="username" placeholder="Email" required>
    <input type="password" name="password" placeholder="Contraseña" required>
    <button type="submit">Iniciar Sesión</button>
</form>
```

**Nota:** Spring Security usa `username` aunque sea un email.

---

### Paso 2: Spring Security busca al usuario

**Archivo:** `src/main/java/com/example/service/UserDetailsServiceImpl.java`

```java
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) 
            throws UsernameNotFoundException {
        
        // 1. Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        
        // 2. Validar que esté activo
        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo");
        }
        
        // 3. CREAR LA AUTORIDAD (ROL) PARA SPRING SECURITY
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
        
        // 4. Retornar UserDetails con el rol incluido
        return new User(
            usuario.getEmail(),
            usuario.getPassword(),
            authorities  // ← Aquí va el rol: ["ROLE_CLIENTE"]
        );
    }
}
```

**¿Qué pasa en el paso 3?**

```java
// Si el usuario tiene rol: CLIENTE
usuario.getRol().name() → "CLIENTE"

// Se crea: "ROLE_CLIENTE"
"ROLE_" + "CLIENTE" = "ROLE_CLIENTE"

// Spring Security requiere el prefijo "ROLE_"
authorities.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));
```

**Resultado:**
```
UserDetails {
  username: "juan@email.com",
  password: "$2a$10$abc...",
  authorities: ["ROLE_CLIENTE"]  ← Aquí está el rol
}
```

**Spring Security guarda esto en la sesión del usuario.**

---

## 🛡️ Autorización (Control de Acceso)

### Configuración Global

**Archivo:** `src/main/java/com/example/config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // 👈 Habilita @PreAuthorize
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                
                // ========== RUTAS PÚBLICAS (SIN LOGIN) ==========
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**")
                    .permitAll()
                
                // ========== RUTAS AUTENTICADAS (CON LOGIN) ==========
                .requestMatchers("/", "/juego/**", "/buscar")
                    .authenticated()  // Cualquier usuario logueado
                
                // ========== COMPRAS: SOLO CLIENTE Y PROVEEDOR ==========
                .requestMatchers("/compra/**")
                    .hasAnyRole("CLIENTE", "PROVEEDOR")  // ← ADMIN NO
                
                .requestMatchers("/mi-biblioteca")
                    .hasAnyRole("CLIENTE", "PROVEEDOR")  // ← ADMIN NO
                
                // ========== PERFIL: CUALQUIERA ==========
                .requestMatchers("/perfil")
                    .authenticated()
                
                // ========== PROVEEDOR: PROVEEDOR Y ADMIN ==========
                .requestMatchers("/proveedor/**")
                    .hasAnyRole("PROVEEDOR", "ADMIN")  // ← ADMIN puede ver
                
                // ========== ADMIN: SOLO ADMIN ==========
                .requestMatchers("/admin/**")
                    .hasRole("ADMIN")  // ← Solo ADMIN
                
                // ========== TODO LO DEMÁS REQUIERE LOGIN ==========
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
            );
        
        return http.build();
    }
}
```

---

### ¿Cómo funciona `hasRole()` y `hasAnyRole()`?

#### **`hasRole("ADMIN")`**
Solo permite acceso si el usuario tiene rol `ADMIN`.

**Ejemplo:**
```java
.requestMatchers("/admin/**").hasRole("ADMIN")
```

**Equivalencia:**
- Usuario: `ROLE_ADMIN` → ✅ Permitido
- Usuario: `ROLE_CLIENTE` → ❌ Denegado (403 Forbidden)
- Usuario: `ROLE_PROVEEDOR` → ❌ Denegado

**Nota:** Spring Security automáticamente añade el prefijo `ROLE_`.

---

#### **`hasAnyRole("CLIENTE", "PROVEEDOR")`**
Permite acceso si el usuario tiene **cualquiera** de esos roles.

**Ejemplo:**
```java
.requestMatchers("/compra/**").hasAnyRole("CLIENTE", "PROVEEDOR")
```

**Equivalencia:**
- Usuario: `ROLE_CLIENTE` → ✅ Permitido
- Usuario: `ROLE_PROVEEDOR` → ✅ Permitido
- Usuario: `ROLE_ADMIN` → ❌ Denegado

---

### Tabla de Permisos

| Ruta                 | CLIENTE | PROVEEDOR | ADMIN |
|----------------------|---------|-----------|-------|
| `/login`             | ✅       | ✅         | ✅     |
| `/register`          | ✅       | ✅         | ✅     |
| `/` (Catálogo)       | ✅       | ✅         | ✅     |
| `/juego/{id}`        | ✅       | ✅         | ✅     |
| `/compra/**`         | ✅       | ✅         | ❌     |
| `/mi-biblioteca`     | ✅       | ✅         | ❌     |
| `/proveedor/**`      | ❌       | ✅         | ✅     |
| `/admin/**`          | ❌       | ❌         | ✅     |
| `/perfil`            | ✅       | ✅         | ✅     |

---

### Protección a Nivel de Controlador

**Opción 1: Proteger toda la clase**

**Archivo:** `AdminController.java`

```java
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")  // ← TODO el controlador requiere ADMIN
public class AdminController {
    
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        // Solo accesible por ADMIN
        return "admin/usuarios";
    }
    
    @GetMapping("/ganancias")
    public String verGanancias(Model model) {
        // Solo accesible por ADMIN
        return "admin/ganancias";
    }
}
```

**Resultado:**
- Si un CLIENTE intenta acceder a `/admin/usuarios` → ❌ 403 Forbidden
- Si un ADMIN accede → ✅ Permitido

---

**Opción 2: Proteger método específico**

```java
@Controller
public class CompraController {
    
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR')")  // ← Solo este método
    @PostMapping("/comprar/{id}")
    public String comprarJuego(@PathVariable Long id, Principal principal) {
        // Solo CLIENTE y PROVEEDOR pueden ejecutar esto
        return "redirect:/mi-biblioteca";
    }
}
```

---

## ✅ Validación en el Backend

### Validación Manual en el Servicio

**Archivo:** `src/main/java/com/example/service/ServicioCompra.java`

```java
@Service
public class ServicioCompra {
    
    /**
     * Verifica si un usuario puede realizar compras.
     * Los ADMIN NO pueden comprar (solo supervisar).
     */
    public boolean puedeRealizarCompras(Usuario usuario) {
        return usuario.getRol() != Rol.ADMIN;
    }
}
```

**Uso en el controlador:**

**Archivo:** `CompraController.java`

```java
@PostMapping("/comprar/{id}")
public String comprarJuego(@PathVariable Long id, Principal principal,
                          RedirectAttributes redirectAttributes) {
    
    Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    
    // ========== VALIDACIÓN: ¿PUEDE COMPRAR? ==========
    if (!servicioCompra.puedeRealizarCompras(usuario)) {
        redirectAttributes.addFlashAttribute("error", 
            "Los administradores no pueden comprar juegos");
        return "redirect:/catalogo";
    }
    
    // Continuar con la compra...
}
```

**¿Por qué esta validación extra?**

Aunque Spring Security ya bloquea `/compra/**` para ADMIN, esta validación:
1. ✅ Proporciona un mensaje de error personalizado
2. ✅ Es una capa de seguridad adicional
3. ✅ Útil si se llama desde otro lugar del código

---

### Ejemplo Completo de Validación

**Archivo:** `AdminController.java`

```java
@GetMapping("/movimientos")
public String verMovimientos(Model model) {
    
    // Solo obtener compras de CLIENTE y PROVEEDOR (no ADMIN)
    List<Compra> compras = servicioCompra.obtenerComprasValidas();
    
    model.addAttribute("compras", compras);
    return "admin/movimientos";
}
```

**Archivo:** `ServicioCompra.java`

```java
public List<Compra> obtenerComprasValidas() {
    return compraRepository.findAll().stream()
        .filter(compra -> puedeRealizarCompras(compra.getUsuario()))
        .collect(Collectors.toList());
}
```

**Resultado:**
- Filtra compras donde el usuario **NO sea ADMIN**
- Solo muestra compras de CLIENTE y PROVEEDOR

---

## 🎨 Validación en el Frontend

### Mostrar/Ocultar según el Rol

**Thymeleaf Security Dialect**

Spring Security proporciona expresiones para Thymeleaf:
- `sec:authorize="isAuthenticated()"` - Usuario logueado
- `sec:authorize="hasRole('ADMIN')"` - Usuario es ADMIN
- `sec:authorize="hasAnyRole('CLIENTE', 'PROVEEDOR')"` - Cliente o proveedor

---

### Ejemplo 1: Header con menú dinámico

**Archivo:** `src/main/resources/templates/fragments/header.html`

```html
<nav class="navbar">
    <ul class="nav">
        
        <!-- ========== SIN LOGIN ========== -->
        <li sec:authorize="!isAuthenticated()">
            <a th:href="@{/login}">Iniciar Sesión</a>
        </li>
        <li sec:authorize="!isAuthenticated()">
            <a th:href="@{/register}">Registrarse</a>
        </li>
        
        <!-- ========== CLIENTE ========== -->
        <li sec:authorize="hasRole('CLIENTE')">
            <a th:href="@{/mi-biblioteca}">
                <i class="fas fa-book"></i> Mi Biblioteca
            </a>
        </li>
        <li sec:authorize="hasRole('CLIENTE')">
            <a th:href="@{/compra/mis-compras}">
                <i class="fas fa-shopping-bag"></i> Mis Compras
            </a>
        </li>
        
        <!-- ========== PROVEEDOR ========== -->
        <li sec:authorize="hasRole('PROVEEDOR')">
            <a th:href="@{/mi-biblioteca}">
                <i class="fas fa-book"></i> Mi Biblioteca
            </a>
        </li>
        <li sec:authorize="hasRole('PROVEEDOR')">
            <a th:href="@{/proveedor/publicar}">
                <i class="fas fa-upload"></i> Publicar Juego
            </a>
        </li>
        <li sec:authorize="hasRole('PROVEEDOR')">
            <a th:href="@{/proveedor/mis-juegos}">
                <i class="fas fa-gamepad"></i> Mis Juegos
            </a>
        </li>
        <li sec:authorize="hasRole('PROVEEDOR')">
            <a th:href="@{/proveedor/ventas}">
                <i class="fas fa-chart-line"></i> Ventas
            </a>
        </li>
        
        <!-- ========== ADMIN ========== -->
        <li sec:authorize="hasRole('ADMIN')">
            <a th:href="@{/admin/usuarios}">
                <i class="fas fa-users"></i> Usuarios
            </a>
        </li>
        <li sec:authorize="hasRole('ADMIN')">
            <a th:href="@{/admin/ganancias}">
                <i class="fas fa-dollar-sign"></i> Ganancias
            </a>
        </li>
        <li sec:authorize="hasRole('ADMIN')">
            <a th:href="@{/admin/movimientos}">
                <i class="fas fa-exchange-alt"></i> Movimientos
            </a>
        </li>
        
        <!-- ========== PERFIL (TODOS) ========== -->
        <li sec:authorize="isAuthenticated()">
            <a th:href="@{/perfil}">
                <i class="fas fa-user"></i> Mi Perfil
            </a>
        </li>
        <li sec:authorize="isAuthenticated()">
            <a th:href="@{/logout}">
                <i class="fas fa-sign-out-alt"></i> Cerrar Sesión
            </a>
        </li>
        
    </ul>
</nav>
```

**Resultado:**
- **CLIENTE** ve: Mi Biblioteca, Mis Compras, Perfil
- **PROVEEDOR** ve: Mi Biblioteca, Publicar Juego, Mis Juegos, Ventas, Perfil
- **ADMIN** ve: Usuarios, Ganancias, Movimientos, Perfil

---

### Ejemplo 2: Botón de compra solo para CLIENTE y PROVEEDOR

**Archivo:** `catalogo/juego-detalle.html`

```html
<!-- ========== SOLO CLIENTE Y PROVEEDOR PUEDEN COMPRAR ========== -->
<div sec:authorize="hasAnyRole('CLIENTE', 'PROVEEDOR')">
    <form th:action="@{/comprar/{id}(id=${juego.id})}" method="post">
        <button type="submit" class="btn btn-success btn-lg">
            <i class="fas fa-shopping-cart"></i>
            Comprar - €[[${juego.precio}]]
        </button>
    </form>
</div>

<!-- ========== ADMIN NO PUEDE COMPRAR ========== -->
<div sec:authorize="hasRole('ADMIN')">
    <div class="alert alert-info">
        <i class="fas fa-info-circle"></i>
        Los administradores no pueden comprar juegos
    </div>
</div>
```

**Resultado:**
- **CLIENTE/PROVEEDOR:** Ve el botón "Comprar"
- **ADMIN:** Ve el mensaje informativo

---

### Ejemplo 3: Mostrar el rol en el perfil

**Archivo:** `usuario/perfil.html`

```html
<div class="form-group">
    <label>Rol</label>
    <input type="text" 
           th:value="${usuario.rol}" 
           class="form-control" 
           disabled readonly>
</div>

<!-- Solo mostrar datos fiscales si es PROVEEDOR -->
<div class="form-group" th:if="${usuario.rol.name() == 'PROVEEDOR'}">
    <label>Datos Fiscales</label>
    <textarea th:field="*{datosFiscales}" 
              class="form-control" 
              rows="3"></textarea>
    <small class="text-muted">
        Información fiscal para recibir pagos
    </small>
</div>
```

**Resultado:**
- **CLIENTE:** Solo ve su rol "CLIENTE"
- **PROVEEDOR:** Ve su rol "PROVEEDOR" y el campo "Datos Fiscales"
- **ADMIN:** Solo ve su rol "ADMIN"

---

## 📚 Casos de Uso Prácticos

### Caso 1: Cliente intenta comprar un juego

**Flujo:**
1. Usuario logueado como CLIENTE
2. Hace clic en "Comprar"
3. Spring Security verifica: ¿Tiene rol CLIENTE o PROVEEDOR? → ✅ SÍ
4. Permite acceso a `/comprar/{id}`
5. `CompraController.comprarJuego()` ejecuta
6. Valida: `puedeRealizarCompras(usuario)` → ✅ true
7. Crea la compra → Éxito

---

### Caso 2: Admin intenta comprar un juego

**Flujo:**
1. Usuario logueado como ADMIN
2. Hace clic en "Comprar" (si el botón estuviera visible)
3. Spring Security verifica: ¿Tiene rol CLIENTE o PROVEEDOR? → ❌ NO
4. **Bloquea la petición → 403 Forbidden**
5. El método `comprarJuego()` ni siquiera se ejecuta

**Seguridad adicional en backend:**
```java
if (!servicioCompra.puedeRealizarCompras(usuario)) {
    return "redirect:/catalogo";  // Redirige al catálogo
}
```

---

### Caso 3: Proveedor publica un juego

**Flujo:**
1. Usuario logueado como PROVEEDOR
2. Accede a `/proveedor/publicar`
3. Spring Security verifica: ¿Tiene rol PROVEEDOR o ADMIN? → ✅ SÍ
4. Muestra el formulario
5. Llena el formulario → POST a `/proveedor/publicar`
6. `@PreAuthorize("hasAnyRole('PROVEEDOR', 'ADMIN')")` en el controlador → ✅ Pasa
7. Crea el juego y cobra 25€ → Éxito

---

### Caso 4: Cliente intenta acceder al panel de admin

**Flujo:**
1. Usuario logueado como CLIENTE
2. Intenta acceder manualmente a `/admin/usuarios`
3. Spring Security verifica: ¿Tiene rol ADMIN? → ❌ NO
4. **Bloquea la petición → 403 Forbidden**
5. Muestra página de error "Acceso Denegado"

---

### Caso 5: Proveedor también puede comprar

**Flujo:**
1. Usuario logueado como PROVEEDOR
2. Ve el catálogo → puede ver botón "Comprar" (porque es PROVEEDOR)
3. Hace clic en "Comprar"
4. Spring Security verifica: ¿Tiene rol CLIENTE o PROVEEDOR? → ✅ SÍ (PROVEEDOR)
5. Permite acceso a `/comprar/{id}`
6. Crea la compra → Éxito
7. El juego aparece en **su biblioteca**

**Nota:** Un proveedor puede:
- ✅ Publicar juegos
- ✅ Comprar juegos de otros proveedores
- ✅ Tener biblioteca de juegos

---

## 📊 Resumen Visual

### Diagrama de Flujo: Registro con Rol

```
┌─────────────────────────┐
│ Usuario llena registro  │
│ - Nombre                │
│ - Email                 │
│ - Contraseña            │
│ - Rol: CLIENTE          │ ← Elige rol
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ AuthController          │
│ @PostMapping("/register")│
│ recibe objeto Usuario   │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ ServicioUsuario         │
│ registrarNuevoUsuario() │
│ - Valida email único    │
│ - Encripta contraseña   │
│ - Guarda en BD          │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Base de Datos           │
│ INSERT INTO usuarios    │
│ (nombre, email, rol...) │
│ VALUES (..., 'CLIENTE') │ ← Rol guardado
└─────────────────────────┘
```

---

### Diagrama de Flujo: Login y Autorización

```
┌─────────────────────────┐
│ Usuario hace login      │
│ - Email                 │
│ - Contraseña            │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Spring Security         │
│ llama a                 │
│ UserDetailsServiceImpl  │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ loadUserByUsername()    │
│ - Busca usuario en BD   │
│ - Lee el rol            │
│ - Crea autoridad:       │
│   "ROLE_CLIENTE"        │ ← Añade prefijo
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Spring Security         │
│ guarda en sesión:       │
│ UserDetails {           │
│   email: "...",         │
│   authorities: [        │
│     "ROLE_CLIENTE"      │ ← Aquí está el rol
│   ]                     │
│ }                       │
└─────────────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Usuario intenta acceder │
│ a /compra/123           │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ SecurityFilterChain     │
│ verifica regla:         │
│ .requestMatchers(       │
│   "/compra/**")         │
│ .hasAnyRole(            │
│   "CLIENTE",            │
│   "PROVEEDOR")          │
└────────┬────────────────┘
         │
    ┌────┴────┐
    │ ¿Tiene? │
    └────┬────┘
         │
    ┌────┴────────┐
   SÍ            NO
    │              │
    ▼              ▼
┌────────┐    ┌──────────┐
│ ✅ OK  │    │ ❌ 403   │
│ Pasa   │    │ Denegado │
└────────┘    └──────────┘
```

---

### Tabla Resumen de Implementación

| Componente | Archivo | Responsabilidad |
|------------|---------|-----------------|
| **Enum Rol** | `Rol.java` | Define los 3 roles posibles |
| **Modelo Usuario** | `Usuario.java` | Asocia el rol al usuario |
| **Formulario** | `register.html` | Permite elegir rol en registro |
| **Servicio Usuario** | `ServicioUsuario.java` | Guarda usuario con rol en BD |
| **UserDetailsService** | `UserDetailsServiceImpl.java` | Carga rol en autenticación |
| **SecurityConfig** | `SecurityConfig.java` | Define qué roles acceden a qué rutas |
| **@PreAuthorize** | Controladores | Protege métodos específicos |
| **Validación Backend** | `ServicioCompra.java` | `puedeRealizarCompras()` |
| **Frontend** | Templates | `sec:authorize="hasRole()"` |

---

## 🔑 Puntos Clave a Recordar

1. **Enum Rol:** Define CLIENTE, PROVEEDOR, ADMIN
2. **@Enumerated(EnumType.STRING):** Guarda el texto en BD
3. **UserDetailsService:** Convierte rol a `"ROLE_CLIENTE"`
4. **SecurityConfig:** Define qué roles acceden a qué rutas
5. **hasRole() vs hasAnyRole():** Uno o varios roles
6. **@PreAuthorize:** Protege controladores/métodos
7. **ADMIN no puede comprar:** Validación extra en backend
8. **PROVEEDOR sí puede comprar:** Tiene ambos permisos
9. **sec:authorize:** Oculta/muestra elementos en frontend
10. **Sesión:** Spring Security guarda el rol en la sesión del usuario

---

## 🎓 Preguntas Frecuentes

### ¿Por qué usar Enum en lugar de String?

**String:**
```java
private String rol = "CLENTE";  // ❌ Error de escritura, compila bien
```

**Enum:**
```java
private Rol rol = Rol.CLENTE;  // ❌ No compila, el IDE lo detecta
```

**Ventajas del Enum:**
- ✅ Evita errores de escritura
- ✅ El IDE sugiere valores válidos
- ✅ Más seguro y mantenible

---

### ¿Por qué Spring Security añade "ROLE_"?

Spring Security usa el prefijo `"ROLE_"` por convención.

**En BD:** `"CLIENTE"`
**En Spring Security:** `"ROLE_CLIENTE"`

**Por eso:**
```java
// En UserDetailsServiceImpl
authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
```

**Luego en SecurityConfig:**
```java
.hasRole("CLIENTE")  // Spring añade automáticamente "ROLE_"
```

**Equivale a:**
```java
.hasAuthority("ROLE_CLIENTE")  // Forma explícita
```

---

### ¿Puedo cambiar el rol de un usuario después del registro?

**Sí**, pero necesitas:

1. **Crear un método en el servicio:**
```java
public Usuario cambiarRol(Long usuarioId, Rol nuevoRol) {
    Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    
    usuario.setRol(nuevoRol);
    return usuarioRepository.save(usuario);
}
```

2. **Crear endpoint en AdminController:**
```java
@PostMapping("/admin/usuario/{id}/cambiar-rol")
@PreAuthorize("hasRole('ADMIN')")
public String cambiarRol(@PathVariable Long id, @RequestParam Rol nuevoRol) {
    servicioUsuario.cambiarRol(id, nuevoRol);
    return "redirect:/admin/usuarios";
}
```

3. **El usuario debe cerrar sesión y volver a entrar** para que se actualice el rol en Spring Security.

---

### ¿Cómo creo un usuario ADMIN?

**Opción 1: Directamente en la base de datos**
```sql
INSERT INTO usuarios (nombre, email, password, rol, fecha_registro, activo)
VALUES (
    'Admin', 
    'admin@pixelshop.com', 
    '$2a$10$abcd...',  -- Contraseña encriptada
    'ADMIN', 
    NOW(), 
    true
);
```

**Opción 2: Script de inicialización en Spring Boot**

Crear: `src/main/resources/data.sql`
```sql
-- Solo se ejecuta si la tabla está vacía
INSERT INTO usuarios (nombre, email, password, rol, fecha_registro, activo)
SELECT 'Admin', 'admin@pixelshop.com', '$2a$10$...', 'ADMIN', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'admin@pixelshop.com');
```

**Opción 3: Clase de inicialización**

```java
@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        if (usuarioRepository.findByEmail("admin@pixelshop.com").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNombre("Admin");
            admin.setEmail("admin@pixelshop.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol(Rol.ADMIN);
            admin.setFechaRegistro(LocalDateTime.now());
            admin.setActivo(true);
            
            usuarioRepository.save(admin);
            System.out.println("✅ Usuario ADMIN creado");
        }
    }
}
```

---

## 🏁 Conclusión

El sistema de roles en PixelShop está implementado con:

1. **Enum Rol** → Define los 3 roles
2. **@Enumerated** → Guarda el rol en BD como texto
3. **UserDetailsService** → Carga el rol en autenticación
4. **SecurityConfig** → Define permisos por ruta
5. **@PreAuthorize** → Protege controladores
6. **Validaciones Backend** → Capa extra de seguridad
7. **sec:authorize** → Oculta/muestra elementos en frontend

**Resultado:** Sistema robusto, seguro y fácil de mantener. ✅

---

**¿Tienes dudas?** Revisa los ejemplos de código y los casos de uso prácticos. 🚀
