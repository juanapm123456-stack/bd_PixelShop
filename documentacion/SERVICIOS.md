# 🛠️ GUÍA DE SERVICIOS - PixelShop

**Documentación Completa de la Capa de Servicios**

---

## 📋 Contenido

1. [Introducción](#-introducción)
2. [Lista de Servicios](#-lista-de-servicios)
3. [ServicioUsuario](#-serviciousuario)
4. [ServicioJuego](#-serviciojuego)
5. [ServicioCompra](#-serviciocompra)
6. [ServicioProveedor](#-servicioproveedor)
7. [ServicioAdministrador](#-servicioadministrador)
8. [ServicioBiblioteca](#-serviciobiblioteca)
9. [Casos de Uso Completos](#-casos-de-uso-completos)

---

## 📖 Introducción

La **capa de servicios** es el corazón de la lógica de negocio de PixelShop. Cada servicio tiene dos partes:

1. **Interfaz** (`IServicioX`) → Define **QUÉ** operaciones se pueden hacer
2. **Implementación** (`ServicioX`) → Define **CÓMO** se hacen esas operaciones

### Ventajas de esta Arquitectura

✅ **Código limpio**: Lógica de negocio separada de controladores  
✅ **Reutilizable**: Los servicios pueden llamarse desde diferentes controladores  
✅ **Testeable**: Fácil crear mocks para pruebas unitarias  
✅ **Mantenible**: Cambios centralizados en un solo lugar  
✅ **Profesional**: Sigue estándares de Spring Framework  

---

## 📚 Lista de Servicios

| Servicio | Interfaz | Implementación | Responsabilidad |
|----------|----------|----------------|-----------------|
| **Usuario** | `IServicioUsuario` | `ServicioUsuario` | Gestión de usuarios, autenticación, roles |
| **Juego** | `IServicioJuego` | `ServicioJuego` | Catálogo de juegos, validación de imágenes |
| **Compra** | `IServicioCompra` | `ServicioCompra` | Proceso de compra, comisiones |
| **Proveedor** | `IServicioProveedor` | `ServicioProveedor` | Movimientos financieros, publicaciones |
| **Administrador** | `IServicioAdministrador` | `ServicioAdministrador` | Ganancias de la plataforma |
| **Biblioteca** | `IServicioBiblioteca` | `ServicioBiblioteca` | Biblioteca personal de juegos |

---

## 👤 ServicioUsuario

### Responsabilidad
Gestión completa de usuarios: registro, autenticación, verificación de roles.

### Métodos Principales

#### 1. `buscarUsuarioPorEmail(String email)`
**¿Qué hace?** Busca un usuario por su email.

**Parámetros:**
- `email` (String): Email del usuario

**Retorna:** `Usuario` encontrado

**Lanza:** `RuntimeException` si no existe

**Ejemplo de uso:**
```java
@Controller
public class AuthController {
    
    @Autowired
    private IServicioUsuario servicioUsuario;
    
    @PostMapping("/login")
    public String login(@RequestParam String email) {
        try {
            Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(email);
            System.out.println("Usuario encontrado: " + usuario.getNombre());
        } catch (RuntimeException e) {
            System.out.println("Usuario no existe");
        }
        return "redirect:/catalogo";
    }
}
```

---

#### 2. `registrarNuevoUsuario(Usuario usuario)`
**¿Qué hace?** Registra un nuevo usuario en el sistema.

**Lógica interna:**
1. Encripta la contraseña con BCrypt
2. Establece fecha de registro
3. Marca como activo
4. Guarda en base de datos

**Parámetros:**
- `usuario` (Usuario): Usuario con datos básicos

**Retorna:** `Usuario` registrado con ID

**Ejemplo de uso:**
```java
@PostMapping("/registro")
public String registrar(@RequestParam String nombre, 
                       @RequestParam String email,
                       @RequestParam String password,
                       @RequestParam String rol) {
    
    // Crear usuario
    Usuario nuevoUsuario = new Usuario();
    nuevoUsuario.setNombre(nombre);
    nuevoUsuario.setEmail(email);
    nuevoUsuario.setPassword(password); // Sin encriptar
    nuevoUsuario.setRol(Rol.valueOf(rol));
    
    // Registrar (el servicio encriptará la contraseña)
    Usuario usuarioRegistrado = servicioUsuario.registrarNuevoUsuario(nuevoUsuario);
    
    System.out.println("Usuario registrado con ID: " + usuarioRegistrado.getId());
    
    return "redirect:/login";
}
```

---

#### 3. `verificarEmailExistente(String email)`
**¿Qué hace?** Verifica si un email ya está registrado.

**Parámetros:**
- `email` (String): Email a verificar

**Retorna:** `true` si existe, `false` si no existe

**Ejemplo de uso:**
```java
@PostMapping("/registro")
public String registrar(@RequestParam String email, RedirectAttributes redirectAttributes) {
    
    // Verificar si el email ya existe
    if (servicioUsuario.verificarEmailExistente(email)) {
        redirectAttributes.addFlashAttribute("error", "Este email ya está registrado");
        return "redirect:/registro";
    }
    
    // Continuar con el registro...
    return "redirect:/login";
}
```

---

#### 4. `encriptarContrasena(String contrasenaPlana)`
**¿Qué hace?** Encripta una contraseña usando BCrypt.

**Parámetros:**
- `contrasenaPlana` (String): Contraseña sin encriptar

**Retorna:** `String` contraseña encriptada

**Ejemplo:**
```java
String passwordOriginal = "miPassword123";
String passwordEncriptada = servicioUsuario.encriptarContrasena(passwordOriginal);

System.out.println("Original: " + passwordOriginal);
System.out.println("Encriptada: " + passwordEncriptada);

// Output:
// Original: miPassword123
// Encriptada: $2a$10$N9qo8uLOickgx2ZMRZoMye...
```

---

#### 5. `esAdministrador(Usuario usuario)`
**¿Qué hace?** Verifica si un usuario tiene rol de administrador.

**Parámetros:**
- `usuario` (Usuario): Usuario a verificar

**Retorna:** `true` si es ADMIN, `false` si no lo es

**Ejemplo de uso:**
```java
@GetMapping("/admin/usuarios")
public String listarUsuarios(Principal principal, Model model) {
    
    Usuario usuarioActual = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    
    // Verificar si es administrador
    if (!servicioUsuario.esAdministrador(usuarioActual)) {
        return "redirect:/catalogo?error=noAutorizado";
    }
    
    // Continuar si es admin...
    List<Usuario> usuarios = servicioUsuario.listarTodosLosUsuarios();
    model.addAttribute("usuarios", usuarios);
    
    return "admin/usuarios";
}
```

---

#### 6. `listarTodosLosUsuarios()`
**¿Qué hace?** Obtiene la lista completa de usuarios registrados.

**Retorna:** `List<Usuario>` con todos los usuarios

**Ejemplo de uso:**
```java
@GetMapping("/admin/usuarios")
public String listarUsuarios(Model model) {
    
    List<Usuario> usuarios = servicioUsuario.listarTodosLosUsuarios();
    
    model.addAttribute("usuarios", usuarios);
    model.addAttribute("totalUsuarios", usuarios.size());
    
    return "admin/usuarios";
}
```

---

#### 7. `cambiarEstadoActivacion(Usuario usuario, boolean activo)`
**¿Qué hace?** Activa o desactiva un usuario.

**Parámetros:**
- `usuario` (Usuario): Usuario a modificar
- `activo` (boolean): `true` para activar, `false` para desactivar

**Retorna:** `Usuario` actualizado

**Ejemplo de uso:**
```java
@PostMapping("/admin/usuarios/desactivar/{id}")
public String desactivarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    
    Usuario usuario = servicioUsuario.obtenerUsuarioPorId(id);
    
    // Desactivar usuario
    servicioUsuario.cambiarEstadoActivacion(usuario, false);
    
    redirectAttributes.addFlashAttribute("success", "Usuario desactivado");
    return "redirect:/admin/usuarios";
}

@PostMapping("/admin/usuarios/activar/{id}")
public String activarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    
    Usuario usuario = servicioUsuario.obtenerUsuarioPorId(id);
    
    // Activar usuario
    servicioUsuario.cambiarEstadoActivacion(usuario, true);
    
    redirectAttributes.addFlashAttribute("success", "Usuario activado");
    return "redirect:/admin/usuarios";
}
```

---

### Métodos de Verificación de Roles

#### `esProveedor(Usuario usuario)`
Retorna `true` si el usuario tiene rol `PROVEEDOR`.

#### `esCliente(Usuario usuario)`
Retorna `true` si el usuario tiene rol `CLIENTE`.

**Ejemplo combinado:**
```java
Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(email);

if (servicioUsuario.esAdministrador(usuario)) {
    System.out.println("Es ADMIN");
} else if (servicioUsuario.esProveedor(usuario)) {
    System.out.println("Es PROVEEDOR");
} else if (servicioUsuario.esCliente(usuario)) {
    System.out.println("Es CLIENTE");
}
```

---

## 🎮 ServicioJuego

### Responsabilidad
Gestión del catálogo de juegos, validación de imágenes, búsqueda.

### Métodos Principales

#### 1. `buscarJuegoPorId(Long id)`
**¿Qué hace?** Busca un juego por su ID.

**Parámetros:**
- `id` (Long): ID del juego

**Retorna:** `Juego` encontrado

**Ejemplo de uso:**
```java
@GetMapping("/juego/{id}")
public String verDetalleJuego(@PathVariable Long id, Model model) {
    
    Juego juego = servicioJuego.buscarJuegoPorId(id);
    
    model.addAttribute("juego", juego);
    model.addAttribute("proveedor", juego.getProveedor());
    
    return "catalogo/juego-detalle";
}
```

---

#### 2. `obtenerJuegosActivos()`
**¿Qué hace?** Obtiene todos los juegos activos del catálogo.

**Retorna:** `List<Juego>` con juegos activos

**Ejemplo de uso:**
```java
@GetMapping("/catalogo")
public String verCatalogo(Model model) {
    
    List<Juego> juegos = servicioJuego.obtenerJuegosActivos();
    
    model.addAttribute("juegos", juegos);
    model.addAttribute("totalJuegos", juegos.size());
    
    return "catalogo/index";
}
```

---

#### 3. `obtenerJuegosDeProveedor(Usuario proveedor)`
**¿Qué hace?** Obtiene los juegos publicados por un proveedor específico.

**Parámetros:**
- `proveedor` (Usuario): Proveedor dueño de los juegos

**Retorna:** `List<Juego>` del proveedor

**Ejemplo de uso:**
```java
@GetMapping("/proveedor/mis-juegos")
public String verMisJuegos(Principal principal, Model model) {
    
    Usuario proveedor = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    
    List<Juego> misJuegos = servicioJuego.obtenerJuegosDeProveedor(proveedor);
    
    model.addAttribute("juegos", misJuegos);
    model.addAttribute("totalJuegos", misJuegos.size());
    
    return "proveedor/mis-juegos";
}
```

---

#### 4. `validarImagen(MultipartFile archivo, String nombreImagen)`
**¿Qué hace?** Valida que una imagen cumpla con los requisitos.

**Requisitos:**
- Tamaño máximo: 10 MB
- Dimensiones mínimas: 200x200 px
- Dimensiones máximas: 3000x3000 px

**Parámetros:**
- `archivo` (MultipartFile): Imagen a validar
- `nombreImagen` (String): Nombre descriptivo para errores

**Retorna:** 
- `null` si es válida
- `String` con mensaje de error si no es válida

**Ejemplo de uso:**
```java
@PostMapping("/proveedor/publicar")
public String publicarJuego(@RequestParam MultipartFile imagen1,
                           @RequestParam MultipartFile imagen2,
                           RedirectAttributes redirectAttributes) {
    
    // Validar imagen 1
    String errorImagen1 = servicioJuego.validarImagen(imagen1, "Imagen 1");
    if (errorImagen1 != null) {
        redirectAttributes.addFlashAttribute("error", errorImagen1);
        return "redirect:/proveedor/publicar";
    }
    
    // Validar imagen 2
    String errorImagen2 = servicioJuego.validarImagen(imagen2, "Imagen 2");
    if (errorImagen2 != null) {
        redirectAttributes.addFlashAttribute("error", errorImagen2);
        return "redirect:/proveedor/publicar";
    }
    
    // Continuar con publicación...
    return "redirect:/proveedor/mis-juegos";
}
```

---

#### 5. `guardarImagenJuego(MultipartFile archivo, int numeroImagen)`
**¿Qué hace?** Guarda una imagen en el servidor.

**Parámetros:**
- `archivo` (MultipartFile): Imagen a guardar
- `numeroImagen` (int): Número de imagen (1, 2, 3 o 4)

**Retorna:** `String` con URL relativa de la imagen

**Lanza:** `IOException` si hay error al guardar

**Ejemplo de uso:**
```java
@PostMapping("/proveedor/publicar")
public String publicarJuego(@RequestParam MultipartFile imagen1,
                           @RequestParam MultipartFile imagen2) throws IOException {
    
    // Guardar imágenes
    String urlImagen1 = servicioJuego.guardarImagenJuego(imagen1, 1);
    String urlImagen2 = servicioJuego.guardarImagenJuego(imagen2, 2);
    
    System.out.println("Imagen 1 guardada en: " + urlImagen1);
    System.out.println("Imagen 2 guardada en: " + urlImagen2);
    
    // Output:
    // Imagen 1 guardada en: /images/juegos/juego_123_imagen1.jpg
    // Imagen 2 guardada en: /images/juegos/juego_123_imagen2.jpg
    
    return "redirect:/proveedor/mis-juegos";
}
```

---

#### 6. `crearNuevoJuego(...)`
**¿Qué hace?** Crea un nuevo juego con toda su información básica.

**Parámetros:**
- `titulo` (String): Título del juego
- `descripcion` (String): Descripción del juego
- `precio` (BigDecimal): Precio del juego
- `genero` (String): Género del juego
- `proveedor` (Usuario): Proveedor que publica

**Retorna:** `Juego` creado (sin guardar en BD)

**Ejemplo de uso:**
```java
@PostMapping("/proveedor/publicar")
public String publicarJuego(@RequestParam String titulo,
                           @RequestParam String descripcion,
                           @RequestParam BigDecimal precio,
                           @RequestParam String genero,
                           Principal principal) {
    
    Usuario proveedor = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    
    // Crear juego
    Juego nuevoJuego = servicioJuego.crearNuevoJuego(
        titulo, 
        descripcion, 
        precio, 
        genero, 
        proveedor
    );
    
    // Guardar juego
    Juego juegoGuardado = servicioJuego.guardarJuego(nuevoJuego);
    
    System.out.println("Juego creado con ID: " + juegoGuardado.getId());
    
    return "redirect:/proveedor/mis-juegos";
}
```

---

#### 7. `esProveedorDelJuego(Juego juego, Usuario usuario)`
**¿Qué hace?** Verifica si un usuario es el propietario de un juego.

**Parámetros:**
- `juego` (Juego): Juego a verificar
- `usuario` (Usuario): Usuario a verificar

**Retorna:** `true` si es el propietario, `false` si no lo es

**Ejemplo de uso:**
```java
@GetMapping("/proveedor/editar/{id}")
public String editarJuego(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
    
    Usuario proveedor = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    Juego juego = servicioJuego.buscarJuegoPorId(id);
    
    // Verificar si es el propietario
    if (!servicioJuego.esProveedorDelJuego(juego, proveedor)) {
        redirectAttributes.addFlashAttribute("error", "No puedes editar este juego");
        return "redirect:/proveedor/mis-juegos";
    }
    
    // Continuar con edición...
    return "proveedor/editar-juego";
}
```

---

## 💳 ServicioCompra

### Responsabilidad
Gestión de compras, cálculo de comisiones, validaciones.

### Métodos Principales

#### 1. `crearNuevaCompra(Usuario usuario, Juego juego)`
**¿Qué hace?** Crea una nueva compra completa.

**Lógica interna:**
1. Crea el registro de compra
2. Añade el juego a la biblioteca del usuario
3. Crea movimiento financiero para el proveedor (85%)

**Parámetros:**
- `usuario` (Usuario): Usuario que compra
- `juego` (Juego): Juego a comprar

**Retorna:** `Compra` creada y guardada

**Ejemplo de uso:**
```java
@PostMapping("/comprar/{id}")
public String comprarJuego(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
    
    Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    Juego juego = servicioJuego.buscarJuegoPorId(id);
    
    // Validaciones
    if (!servicioCompra.puedeRealizarCompras(usuario)) {
        redirectAttributes.addFlashAttribute("error", "No puedes comprar juegos");
        return "redirect:/catalogo";
    }
    
    if (servicioCompra.verificarJuegoYaComprado(usuario, juego)) {
        redirectAttributes.addFlashAttribute("error", "Ya tienes este juego");
        return "redirect:/catalogo";
    }
    
    // Crear compra
    Compra compra = servicioCompra.crearNuevaCompra(usuario, juego);
    
    redirectAttributes.addFlashAttribute("success", 
        "¡Compra realizada! El juego está en tu biblioteca");
    
    return "redirect:/usuario/mi-biblioteca";
}
```

---

#### 2. `calcularMontoParaProveedor(BigDecimal precio)`
**¿Qué hace?** Calcula el 85% del precio (lo que recibe el proveedor).

**Parámetros:**
- `precio` (BigDecimal): Precio del juego

**Retorna:** `BigDecimal` con 85% del precio

**Ejemplo de uso:**
```java
BigDecimal precioJuego = new BigDecimal("100.00");

BigDecimal paraProveedor = servicioCompra.calcularMontoParaProveedor(precioJuego);
BigDecimal paraPlataforma = servicioCompra.calcularComisionPlataforma(precioJuego);

System.out.println("Precio juego: €" + precioJuego);
System.out.println("Para proveedor (85%): €" + paraProveedor);  // €85.00
System.out.println("Para plataforma (15%): €" + paraPlataforma); // €15.00
```

---

#### 3. `verificarJuegoYaComprado(Usuario usuario, Juego juego)`
**¿Qué hace?** Verifica si un usuario ya compró un juego específico.

**Parámetros:**
- `usuario` (Usuario): Usuario a verificar
- `juego` (Juego): Juego a verificar

**Retorna:** `true` si ya lo compró, `false` si no

**Ejemplo de uso:**
```java
@PostMapping("/comprar/{id}")
public String comprar(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
    
    Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    Juego juego = servicioJuego.buscarJuegoPorId(id);
    
    // Verificar si ya lo compró
    if (servicioCompra.verificarJuegoYaComprado(usuario, juego)) {
        redirectAttributes.addFlashAttribute("error", 
            "Ya tienes este juego en tu biblioteca");
        return "redirect:/catalogo";
    }
    
    // Continuar con compra...
    return "redirect:/usuario/mi-biblioteca";
}
```

---

#### 4. `puedeRealizarCompras(Usuario usuario)`
**¿Qué hace?** Valida si un usuario puede realizar compras.

**Regla:** Los administradores NO pueden comprar.

**Parámetros:**
- `usuario` (Usuario): Usuario a validar

**Retorna:** `true` si puede comprar, `false` si no puede

**Ejemplo de uso:**
```java
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
```

---

#### 5. `obtenerComprasDeUsuario(Usuario usuario)`
**¿Qué hace?** Obtiene el historial de compras de un usuario.

**Parámetros:**
- `usuario` (Usuario): Usuario del que obtener compras

**Retorna:** `List<Compra>` ordenadas por fecha (más recientes primero)

**Ejemplo de uso:**
```java
@GetMapping("/usuario/mis-compras")
public String verMisCompras(Principal principal, Model model) {
    
    Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    
    List<Compra> compras = servicioCompra.obtenerComprasDeUsuario(usuario);
    
    // Calcular total gastado
    BigDecimal totalGastado = compras.stream()
        .map(Compra::getPrecio)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    model.addAttribute("compras", compras);
    model.addAttribute("totalCompras", compras.size());
    model.addAttribute("totalGastado", totalGastado);
    
    return "usuario/mis-compras";
}
```

---

## 💰 ServicioProveedor

### Responsabilidad
Movimientos financieros de proveedores, publicaciones de juegos.

### Métodos Principales

#### 1. `crearMovimientoProveedor(Usuario proveedor, Compra compra, BigDecimal montoNeto)`
**¿Qué hace?** Crea un movimiento financiero para un proveedor.

**Cuándo se usa:** Automáticamente al crear una compra.

**Parámetros:**
- `proveedor` (Usuario): Proveedor que recibe el dinero
- `compra` (Compra): Compra asociada
- `montoNeto` (BigDecimal): Monto neto (85% del precio)

**Retorna:** `MovimientoProveedor` creado

**Ejemplo interno (usado por ServicioCompra):**
```java
// Dentro de ServicioCompra.crearNuevaCompra()
BigDecimal montoProveedor = calcularMontoParaProveedor(juego.getPrecio());
servicioProveedor.crearMovimientoProveedor(
    juego.getProveedor(), 
    compraSalvada, 
    montoProveedor
);
```

---

#### 2. `obtenerMovimientosDeProveedor(Usuario proveedor)`
**¿Qué hace?** Obtiene todos los movimientos financieros de un proveedor.

**Parámetros:**
- `proveedor` (Usuario): Proveedor del que obtener movimientos

**Retorna:** `List<MovimientoProveedor>` ordenados por fecha

**Ejemplo de uso:**
```java
@GetMapping("/proveedor/ventas")
public String verVentas(Principal principal, Model model) {
    
    Usuario proveedor = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    
    List<MovimientoProveedor> movimientos = servicioProveedor.obtenerMovimientosDeProveedor(proveedor);
    
    // Calcular ingresos pendientes
    BigDecimal pendiente = servicioProveedor.calcularIngresosPendientes(proveedor);
    
    // Calcular total cobrado
    BigDecimal cobrado = movimientos.stream()
        .filter(MovimientoProveedor::getCobrado)
        .map(MovimientoProveedor::getMonto)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    model.addAttribute("movimientos", movimientos);
    model.addAttribute("pendiente", pendiente);
    model.addAttribute("cobrado", cobrado);
    
    return "proveedor/ventas";
}
```

---

#### 3. `calcularIngresosPendientes(Usuario proveedor)`
**¿Qué hace?** Calcula el total de dinero pendiente de cobro.

**Parámetros:**
- `proveedor` (Usuario): Proveedor del que calcular ingresos

**Retorna:** `BigDecimal` con total pendiente

**Ejemplo de uso:**
```java
@GetMapping("/proveedor/ventas")
public String verVentas(Principal principal, Model model) {
    
    Usuario proveedor = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    
    BigDecimal pendiente = servicioProveedor.calcularIngresosPendientes(proveedor);
    
    model.addAttribute("ingresosPendientes", pendiente);
    
    if (pendiente.compareTo(BigDecimal.ZERO) > 0) {
        model.addAttribute("mensaje", 
            "Tienes €" + pendiente + " pendientes de cobro");
    }
    
    return "proveedor/ventas";
}
```

---

#### 4. `registrarPublicacionJuego(Juego juego, Usuario proveedor)`
**¿Qué hace?** Registra la publicación de un juego (costo: 25€).

**Parámetros:**
- `juego` (Juego): Juego publicado
- `proveedor` (Usuario): Proveedor que publica

**Retorna:** `PublicacionJuego` registrada

**Ejemplo de uso:**
```java
@PostMapping("/proveedor/publicar")
public String publicarJuego(@RequestParam String titulo,
                           @RequestParam BigDecimal precio,
                           Principal principal,
                           RedirectAttributes redirectAttributes) throws IOException {
    
    Usuario proveedor = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    
    // Crear juego
    Juego nuevoJuego = servicioJuego.crearNuevoJuego(titulo, descripcion, precio, genero, proveedor);
    Juego juegoGuardado = servicioJuego.guardarJuego(nuevoJuego);
    
    // Registrar publicación (costo 25€)
    PublicacionJuego publicacion = servicioProveedor.registrarPublicacionJuego(juegoGuardado, proveedor);
    
    BigDecimal costoPublicacion = servicioProveedor.obtenerCostoPublicacion();
    
    redirectAttributes.addFlashAttribute("success", 
        "Juego publicado. Costo de publicación: €" + costoPublicacion);
    
    return "redirect:/proveedor/mis-juegos";
}
```

---

## 👨‍💼 ServicioAdministrador

### Responsabilidad
Cálculo de ganancias de la plataforma.

### Métodos Principales

#### 1. `calcularGananciasTotalesPlataforma()`
**¿Qué hace?** Calcula las ganancias totales de la plataforma.

**Fuentes de ingresos:**
1. 15% de cada venta
2. 25€ por cada publicación

**Retorna:** `ResumenGanancias` con:
- `gananciasPorVentas` (BigDecimal)
- `gananciasPorPublicaciones` (BigDecimal)
- `totalGanancias` (BigDecimal)
- `numeroVentas` (Long)
- `numeroPublicaciones` (Long)

**Ejemplo de uso:**
```java
@GetMapping("/admin/ganancias")
public String verGanancias(Model model) {
    
    ServicioAdministrador.ResumenGanancias resumen = 
        servicioAdministrador.calcularGananciasTotalesPlataforma();
    
    model.addAttribute("gananciasPorVentas", resumen.getGananciasPorVentas());
    model.addAttribute("gananciasPorPublicaciones", resumen.getGananciasPorPublicaciones());
    model.addAttribute("totalGanancias", resumen.getTotalGanancias());
    model.addAttribute("numeroVentas", resumen.getNumeroVentas());
    model.addAttribute("numeroPublicaciones", resumen.getNumeroPublicaciones());
    
    return "admin/ganancias";
}
```

**Ejemplo de salida:**
```
Ganancias por ventas (15%): €1,500.00
Ganancias por publicaciones (25€ × 40): €1,000.00
─────────────────────────────────────────
Total ganancias: €2,500.00

Número de ventas: 100
Número de publicaciones: 40
```

---

## 📚 ServicioBiblioteca

### Responsabilidad
Gestión de la biblioteca personal de juegos de cada usuario.

### Métodos Principales

#### 1. `agregarJuegoABiblioteca(Usuario usuario, Juego juego)`
**¿Qué hace?** Añade un juego a la biblioteca de un usuario.

**Cuándo se usa:** Automáticamente al crear una compra.

**Parámetros:**
- `usuario` (Usuario): Usuario que adquiere el juego
- `juego` (Juego): Juego adquirido

**Retorna:** `BibliotecaUsuario` creada

**Ejemplo interno (usado por ServicioCompra):**
```java
// Dentro de ServicioCompra.crearNuevaCompra()
servicioBiblioteca.agregarJuegoABiblioteca(usuario, juego);
```

---

#### 2. `obtenerBibliotecaDeUsuario(Usuario usuario)`
**¿Qué hace?** Obtiene todos los juegos en la biblioteca de un usuario.

**Parámetros:**
- `usuario` (Usuario): Usuario del que obtener biblioteca

**Retorna:** `List<BibliotecaUsuario>` ordenada por fecha de adquisición

**Ejemplo de uso:**
```java
@GetMapping("/usuario/mi-biblioteca")
public String verBiblioteca(Principal principal, Model model) {
    
    Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    
    List<BibliotecaUsuario> biblioteca = servicioBiblioteca.obtenerBibliotecaDeUsuario(usuario);
    
    int totalJuegos = servicioBiblioteca.contarJuegosEnBiblioteca(usuario);
    
    model.addAttribute("biblioteca", biblioteca);
    model.addAttribute("totalJuegos", totalJuegos);
    
    return "usuario/mi-biblioteca";
}
```

---

#### 3. `usuarioTieneJuegoEnBiblioteca(Usuario usuario, Juego juego)`
**¿Qué hace?** Verifica si un usuario tiene un juego en su biblioteca.

**Parámetros:**
- `usuario` (Usuario): Usuario a verificar
- `juego` (Juego): Juego a buscar

**Retorna:** `true` si lo tiene, `false` si no

**Ejemplo de uso:**
```java
@GetMapping("/catalogo")
public String verCatalogo(Principal principal, Model model) {
    
    Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    List<Juego> juegos = servicioJuego.obtenerJuegosActivos();
    
    // Marcar cuáles juegos ya tiene el usuario
    for (Juego juego : juegos) {
        boolean loTiene = servicioBiblioteca.usuarioTieneJuegoEnBiblioteca(usuario, juego);
        // Usar en la vista para mostrar "Ya lo tienes" o "Comprar"
    }
    
    model.addAttribute("juegos", juegos);
    
    return "catalogo/index";
}
```

---

## 🎯 Casos de Uso Completos

### Caso 1: Registro de Usuario

**Flujo completo:**
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
    nuevoUsuario.setPassword(password); // Sin encriptar
    nuevoUsuario.setRol(Rol.valueOf(rol));
    
    // 3. Registrar (el servicio encripta la contraseña)
    Usuario usuarioRegistrado = servicioUsuario.registrarNuevoUsuario(nuevoUsuario);
    
    // 4. Redirigir al login
    redirectAttributes.addFlashAttribute("success", 
        "Usuario registrado con éxito. Ya puedes iniciar sesión.");
    
    return "redirect:/login";
}
```

---

### Caso 2: Compra de un Juego

**Flujo completo:**
```java
@PostMapping("/comprar/{id}")
public String comprarJuego(@PathVariable Long id, 
                          Principal principal, 
                          RedirectAttributes redirectAttributes) {
    
    // 1. Obtener usuario actual
    Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    
    // 2. Obtener juego
    Juego juego = servicioJuego.buscarJuegoPorId(id);
    
    // 3. Validar si puede comprar (no es ADMIN)
    if (!servicioCompra.puedeRealizarCompras(usuario)) {
        redirectAttributes.addFlashAttribute("error", 
            "Los administradores no pueden comprar juegos");
        return "redirect:/catalogo";
    }
    
    // 4. Verificar si ya lo compró
    if (servicioCompra.verificarJuegoYaComprado(usuario, juego)) {
        redirectAttributes.addFlashAttribute("error", 
            "Ya tienes este juego en tu biblioteca");
        return "redirect:/catalogo";
    }
    
    // 5. Crear compra (esto también añade a biblioteca y crea movimiento proveedor)
    Compra compra = servicioCompra.crearNuevaCompra(usuario, juego);
    
    // 6. Mostrar resumen
    BigDecimal montoProveedor = servicioCompra.calcularMontoParaProveedor(juego.getPrecio());
    BigDecimal comisionPlataforma = servicioCompra.calcularComisionPlataforma(juego.getPrecio());
    
    System.out.println("Compra realizada:");
    System.out.println("- Usuario: " + usuario.getNombre());
    System.out.println("- Juego: " + juego.getTitulo());
    System.out.println("- Precio: €" + juego.getPrecio());
    System.out.println("- Para proveedor: €" + montoProveedor);
    System.out.println("- Para plataforma: €" + comisionPlataforma);
    
    redirectAttributes.addFlashAttribute("success", 
        "¡Compra realizada con éxito! El juego está en tu biblioteca.");
    
    return "redirect:/usuario/mi-biblioteca";
}
```

---

### Caso 3: Publicar un Juego

**Flujo completo:**
```java
@PostMapping("/proveedor/publicar")
public String publicarJuego(@RequestParam String titulo,
                           @RequestParam String descripcion,
                           @RequestParam BigDecimal precio,
                           @RequestParam String genero,
                           @RequestParam MultipartFile imagen1,
                           @RequestParam MultipartFile imagen2,
                           Principal principal,
                           RedirectAttributes redirectAttributes) throws IOException {
    
    // 1. Obtener proveedor
    Usuario proveedor = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    
    // 2. Validar imágenes
    String errorImagen1 = servicioJuego.validarImagen(imagen1, "Imagen 1");
    if (errorImagen1 != null) {
        redirectAttributes.addFlashAttribute("error", errorImagen1);
        return "redirect:/proveedor/publicar";
    }
    
    String errorImagen2 = servicioJuego.validarImagen(imagen2, "Imagen 2");
    if (errorImagen2 != null) {
        redirectAttributes.addFlashAttribute("error", errorImagen2);
        return "redirect:/proveedor/publicar";
    }
    
    // 3. Crear juego
    Juego nuevoJuego = servicioJuego.crearNuevoJuego(
        titulo, 
        descripcion, 
        precio, 
        genero, 
        proveedor
    );
    
    // 4. Guardar imágenes
    String urlImagen1 = servicioJuego.guardarImagenJuego(imagen1, 1);
    String urlImagen2 = servicioJuego.guardarImagenJuego(imagen2, 2);
    
    nuevoJuego.setImagen1(urlImagen1);
    nuevoJuego.setImagen2(urlImagen2);
    
    // 5. Guardar juego
    Juego juegoGuardado = servicioJuego.guardarJuego(nuevoJuego);
    
    // 6. Registrar publicación (costo 25€)
    PublicacionJuego publicacion = servicioProveedor.registrarPublicacionJuego(
        juegoGuardado, 
        proveedor
    );
    
    // 7. Informar al proveedor
    BigDecimal costoPublicacion = servicioProveedor.obtenerCostoPublicacion();
    
    redirectAttributes.addFlashAttribute("success", 
        "Juego publicado con éxito. Costo de publicación: €" + costoPublicacion);
    
    return "redirect:/proveedor/mis-juegos";
}
```

---

### Caso 4: Ver Ganancias (Admin)

**Flujo completo:**
```java
@GetMapping("/admin/ganancias")
public String verGanancias(Principal principal, Model model, RedirectAttributes redirectAttributes) {
    
    // 1. Verificar que es administrador
    Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    
    if (!servicioUsuario.esAdministrador(usuario)) {
        redirectAttributes.addFlashAttribute("error", "Acceso denegado");
        return "redirect:/catalogo";
    }
    
    // 2. Calcular ganancias totales
    ServicioAdministrador.ResumenGanancias resumen = 
        servicioAdministrador.calcularGananciasTotalesPlataforma();
    
    // 3. Obtener estadísticas adicionales
    List<Usuario> todosUsuarios = servicioUsuario.listarTodosLosUsuarios();
    List<Compra> todasCompras = servicioCompra.obtenerTodasLasCompras();
    
    // 4. Añadir al modelo
    model.addAttribute("gananciasPorVentas", resumen.getGananciasPorVentas());
    model.addAttribute("gananciasPorPublicaciones", resumen.getGananciasPorPublicaciones());
    model.addAttribute("totalGanancias", resumen.getTotalGanancias());
    model.addAttribute("numeroVentas", resumen.getNumeroVentas());
    model.addAttribute("numeroPublicaciones", resumen.getNumeroPublicaciones());
    model.addAttribute("totalUsuarios", todosUsuarios.size());
    model.addAttribute("totalCompras", todasCompras.size());
    
    return "admin/ganancias";
}
```

---

## 📝 Resumen

### Servicios Creados

1. **ServicioUsuario** - 13 métodos
   - Gestión de usuarios
   - Autenticación y roles
   - Encriptación de contraseñas

2. **ServicioJuego** - 12 métodos
   - Catálogo de juegos
   - Validación de imágenes
   - Búsqueda y filtrado

3. **ServicioCompra** - 13 métodos
   - Proceso de compra
   - Cálculo de comisiones
   - Validaciones de negocio

4. **ServicioProveedor** - 12 métodos
   - Movimientos financieros
   - Publicaciones de juegos
   - Ingresos pendientes

5. **ServicioAdministrador** - 3 métodos
   - Ganancias de la plataforma
   - Estadísticas generales

6. **ServicioBiblioteca** - 5 métodos
   - Biblioteca personal
   - Gestión de juegos adquiridos

**Total: 58 métodos de servicio**

---

**Tu código ahora tiene una capa de servicios profesional y bien documentada.** 🚀

Para más información, consulta:
- [ARQUITECTURA.md](ARQUITECTURA.md) - Arquitectura del sistema
- [MODELO_NEGOCIO.md](MODELO_NEGOCIO.md) - Sistema de comisiones
- [SEGURIDAD.md](SEGURIDAD.md) - Spring Security
