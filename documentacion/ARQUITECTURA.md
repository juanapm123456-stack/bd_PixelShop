# 🏗️ ARQUITECTURA DEL SISTEMA - PixelShop

**Guía Técnica del Patrón Interfaz + Implementación**

---

## 📋 Contenido

1. [Patrón Arquitectónico](#-patrón-arquitectónico)
2. [¿Por qué Interfaz + Implementación?](#-por-qué-interfaz--implementación)
3. [Capas del Sistema](#-capas-del-sistema)
4. [Flujo de Datos](#-flujo-de-datos)
5. [Principios SOLID](#-principios-solid)
6. [Ventajas del Patrón](#-ventajas-del-patrón)
7. [Ejemplos Prácticos](#-ejemplos-prácticos)

---

## 🎯 Patrón Arquitectónico

### MVC + Service Layer + Repository Pattern

```
┌──────────────────────────────────────────────────────┐
│                  NAVEGADOR WEB                       │
│            (Usuario interactúa)                      │
└───────────────────┬──────────────────────────────────┘
                    │ HTTP Request
                    ▼
┌──────────────────────────────────────────────────────┐
│               CAPA DE VISTA                          │
│              (Thymeleaf Templates)                   │
│   - login.html                                       │
│   - catalogo/index.html                              │
│   - usuario/mi-biblioteca.html                       │
│   - proveedor/publicar-juego.html                    │
│   - admin/ganancias.html                             │
└───────────────────┬──────────────────────────────────┘
                    │ Renderiza
                    ▼
┌──────────────────────────────────────────────────────┐
│            CAPA DE CONTROLADORES                     │
│               (Spring MVC)                           │
│   - AuthController                                   │
│   - CatalogoController                               │
│   - CompraController                                 │
│   - ProveedorController                              │
│   - AdminController                                  │
│   - BibliotecaController                             │
│   - PerfilController                                 │
└───────────────────┬──────────────────────────────────┘
                    │ @Autowired
                    │ Inyecta Interfaces
                    ▼
┌──────────────────────────────────────────────────────┐
│         CAPA DE SERVICIOS (INTERFACES)               │
│              Define "QUÉ" hacer                      │
│   - IServicioUsuario                                 │
│   - IServicioJuego                                   │
│   - IServicioCompra                                  │
│   - IServicioProveedor                               │
│   - IServicioAdministrador                           │
│   - IServicioBiblioteca                              │
└───────────────────┬──────────────────────────────────┘
                    │ implements
                    ▼
┌──────────────────────────────────────────────────────┐
│      CAPA DE SERVICIOS (IMPLEMENTACIONES)            │
│         @Service - Define "CÓMO" hacerlo             │
│   - ServicioUsuario                                  │
│   - ServicioJuego                                    │
│   - ServicioCompra                                   │
│   - ServicioProveedor                                │
│   - ServicioAdministrador                            │
│   - ServicioBiblioteca                               │
└───────────────────┬──────────────────────────────────┘
                    │ @Autowired
                    │ Usa Repositories
                    ▼
┌──────────────────────────────────────────────────────┐
│           CAPA DE REPOSITORIOS                       │
│         (Spring Data JPA - CRUD)                     │
│   - UsuarioRepository                                │
│   - JuegoRepository                                  │
│   - CompraRepository                                 │
│   - BibliotecaUsuarioRepository                      │
│   - MovimientoProveedorRepository                    │
│   - PublicacionJuegoRepository                       │
└───────────────────┬──────────────────────────────────┘
                    │ SQL Queries
                    ▼
┌──────────────────────────────────────────────────────┐
│              BASE DE DATOS                           │
│                 (MySQL)                              │
│   Tablas:                                            │
│   - usuarios                                         │
│   - juegos                                           │
│   - compras                                          │
│   - biblioteca_usuario                               │
│   - movimiento_proveedor                             │
│   - publicacion_juego                                │
└──────────────────────────────────────────────────────┘
```

---

## 🤔 ¿Por qué Interfaz + Implementación?

### El Problema sin Interfaces

**Antes (Mal diseño):**
```java
@Controller
public class CompraController {
    
    @Autowired
    private ServicioCompra servicioCompra;  // ❌ Dependencia directa de implementación
    
    @PostMapping("/comprar/{id}")
    public String comprar(@PathVariable Long id) {
        // Si cambia ServicioCompra, puede afectar al controlador
        servicioCompra.procesarCompra(id);
        return "redirect:/catalogo";
    }
}
```

**Problemas:**
- ❌ **Acoplamiento fuerte**: El controlador depende directamente de la clase concreta
- ❌ **Difícil de testear**: No puedes hacer mock fácilmente
- ❌ **Poco flexible**: Cambiar la implementación afecta a todos los que la usan
- ❌ **No cumple SOLID**: Viola el principio de Inversión de Dependencias

---

### La Solución con Interfaces

**Ahora (Buen diseño):**

#### 1. Interfaz (Define el contrato)
```java
public interface IServicioCompra {
    Compra crearNuevaCompra(Usuario usuario, Juego juego);
    boolean verificarJuegoYaComprado(Usuario usuario, Juego juego);
    BigDecimal calcularMontoParaProveedor(BigDecimal precio);
}
```

#### 2. Implementación (Realiza el trabajo)
```java
@Service
public class ServicioCompra implements IServicioCompra {
    
    @Autowired
    private CompraRepository compraRepository;
    
    @Override
    public Compra crearNuevaCompra(Usuario usuario, Juego juego) {
        // Lógica de negocio aquí
        Compra compra = new Compra(usuario, juego, juego.getPrecio());
        return compraRepository.save(compra);
    }
    
    @Override
    public boolean verificarJuegoYaComprado(Usuario usuario, Juego juego) {
        return compraRepository.existsByUsuarioAndJuego(usuario, juego);
    }
    
    @Override
    public BigDecimal calcularMontoParaProveedor(BigDecimal precio) {
        return precio.multiply(new BigDecimal("0.85"));
    }
}
```

#### 3. Controlador (Usa la interfaz)
```java
@Controller
public class CompraController {
    
    @Autowired
    private IServicioCompra servicioCompra;  // ✅ Depende de la abstracción
    
    @PostMapping("/comprar/{id}")
    public String comprar(@PathVariable Long id, Principal principal) {
        Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
        Juego juego = servicioJuego.buscarJuegoPorId(id);
        
        // Verificar si ya lo compró
        if (servicioCompra.verificarJuegoYaComprado(usuario, juego)) {
            return "redirect:/catalogo?error=yaComprado";
        }
        
        // Crear la compra
        servicioCompra.crearNuevaCompra(usuario, juego);
        
        return "redirect:/usuario/mi-biblioteca";
    }
}
```

**Ventajas:**
- ✅ **Desacoplamiento**: El controlador no conoce la implementación
- ✅ **Testeable**: Puedes crear mocks de IServicioCompra
- ✅ **Flexible**: Puedes cambiar ServicioCompra sin tocar el controlador
- ✅ **Cumple SOLID**: Principio de Inversión de Dependencias

---

## 📚 Capas del Sistema

### 1. Capa de Vista (Thymeleaf)

**Responsabilidad:** Presentar información al usuario y capturar entrada.

**Tecnología:** Thymeleaf + HTML + CSS + JavaScript

**Ejemplos:**
- `login.html` → Formulario de login
- `catalogo/index.html` → Listado de juegos
- `proveedor/publicar-juego.html` → Formulario para publicar juego

**Interacción:**
```html
<!-- Ejemplo: Botón de compra -->
<form th:action="@{/comprar/{id}(id=${juego.id})}" method="post">
    <button type="submit">Comprar Juego</button>
</form>
```

---

### 2. Capa de Controladores (Spring MVC)

**Responsabilidad:** Recibir peticiones HTTP, delegar lógica a servicios, devolver vistas.

**Anotaciones:** `@Controller`, `@GetMapping`, `@PostMapping`

**Ejemplo:**
```java
@Controller
public class CompraController {
    
    @Autowired
    private IServicioCompra servicioCompra;
    
    @Autowired
    private IServicioJuego servicioJuego;
    
    @Autowired
    private IServicioUsuario servicioUsuario;
    
    @PostMapping("/comprar/{id}")
    public String comprarJuego(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        
        // 1. Obtener usuario actual
        Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
        
        // 2. Obtener juego
        Juego juego = servicioJuego.buscarJuegoPorId(id);
        
        // 3. Validar si puede comprar (delega al servicio)
        if (!servicioCompra.puedeRealizarCompras(usuario)) {
            redirectAttributes.addFlashAttribute("error", "Los administradores no pueden comprar");
            return "redirect:/catalogo";
        }
        
        // 4. Verificar si ya lo compró
        if (servicioCompra.verificarJuegoYaComprado(usuario, juego)) {
            redirectAttributes.addFlashAttribute("error", "Ya tienes este juego");
            return "redirect:/catalogo";
        }
        
        // 5. Procesar compra (delega al servicio)
        servicioCompra.crearNuevaCompra(usuario, juego);
        
        // 6. Redirigir
        redirectAttributes.addFlashAttribute("success", "Compra realizada con éxito");
        return "redirect:/usuario/mi-biblioteca";
    }
}
```

**Responsabilidades del Controlador:**
- ✅ Recibir parámetros (PathVariable, RequestParam, Principal)
- ✅ Validar entrada básica
- ✅ Llamar a servicios
- ✅ Manejar respuestas y redirecciones
- ❌ **NO debe tener lógica de negocio**
- ❌ **NO debe acceder directamente a repositorios**

---

### 3. Capa de Servicios (Interfaces)

**Responsabilidad:** Definir **QUÉ** operaciones se pueden hacer.

**Convención:** Nombre con prefijo `I` (IServicioX)

**Ejemplo:**
```java
public interface IServicioCompra {
    
    // CRUD básico
    Compra crearNuevaCompra(Usuario usuario, Juego juego);
    Compra guardarCompra(Compra compra);
    
    // Consultas
    boolean verificarJuegoYaComprado(Usuario usuario, Juego juego);
    List<Compra> obtenerComprasDeUsuario(Usuario usuario);
    List<Long> obtenerIdsDeJuegosComprados(Usuario usuario);
    
    // Cálculos de negocio
    BigDecimal calcularMontoParaProveedor(BigDecimal precio);
    BigDecimal calcularComisionPlataforma(BigDecimal precio);
    BigDecimal calcularTotalVentas();
    
    // Validaciones
    boolean puedeRealizarCompras(Usuario usuario);
    List<Compra> obtenerComprasValidas();
    
    // Operaciones de estado
    Compra marcarComoPagadaAlProveedor(Compra compra);
}
```

**Características:**
- ✅ Solo declaración de métodos (sin implementación)
- ✅ Documentación Javadoc clara
- ✅ Nombres descriptivos en español
- ✅ Define el "contrato" del servicio

---

### 4. Capa de Servicios (Implementaciones)

**Responsabilidad:** Definir **CÓMO** se hacen las operaciones (lógica de negocio).

**Anotación:** `@Service`

**Convención:** Nombre sin prefijo `I` (ServicioX)

**Ejemplo:**
```java
@Service
public class ServicioCompra implements IServicioCompra {
    
    // Constantes de negocio
    private static final BigDecimal COMISION_PROVEEDOR = new BigDecimal("0.85");
    private static final BigDecimal COMISION_PLATAFORMA = new BigDecimal("0.15");
    
    // Inyección de dependencias
    @Autowired
    private CompraRepository compraRepository;
    
    @Autowired
    private IServicioBiblioteca servicioBiblioteca;
    
    @Autowired
    private IServicioProveedor servicioProveedor;
    
    @Override
    public Compra crearNuevaCompra(Usuario usuario, Juego juego) {
        // 1. Crear la compra
        Compra compra = new Compra();
        compra.setUsuario(usuario);
        compra.setJuego(juego);
        compra.setPrecio(juego.getPrecio());
        compra.setFechaCompra(LocalDateTime.now());
        compra.setPagadaAlProveedor(false);
        
        // 2. Guardar compra
        Compra compraSalvada = compraRepository.save(compra);
        
        // 3. Añadir a biblioteca del usuario
        servicioBiblioteca.agregarJuegoABiblioteca(usuario, juego);
        
        // 4. Crear movimiento para el proveedor
        BigDecimal montoProveedor = calcularMontoParaProveedor(juego.getPrecio());
        servicioProveedor.crearMovimientoProveedor(
            juego.getProveedor(), 
            compraSalvada, 
            montoProveedor
        );
        
        return compraSalvada;
    }
    
    @Override
    public BigDecimal calcularMontoParaProveedor(BigDecimal precio) {
        return precio.multiply(COMISION_PROVEEDOR).setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public boolean verificarJuegoYaComprado(Usuario usuario, Juego juego) {
        return compraRepository.existsByUsuarioAndJuego(usuario, juego);
    }
    
    @Override
    public boolean puedeRealizarCompras(Usuario usuario) {
        // Los administradores NO pueden comprar
        return !usuario.getRol().equals(Rol.ADMIN);
    }
    
    // ... resto de implementaciones
}
```

**Características:**
- ✅ Contiene toda la lógica de negocio
- ✅ Usa repositorios para acceder a datos
- ✅ Coordina con otros servicios
- ✅ Implementa validaciones y cálculos
- ✅ Maneja transacciones (implícito con @Service)

---

### 5. Capa de Repositorios (Spring Data JPA)

**Responsabilidad:** Acceso a base de datos (CRUD).

**Tecnología:** Spring Data JPA + Hibernate

**Ejemplo:**
```java
@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    
    // Métodos automáticos de JpaRepository:
    // - save(Compra)
    // - findById(Long)
    // - findAll()
    // - delete(Compra)
    // - count()
    
    // Consultas personalizadas con naming convention
    List<Compra> findByUsuarioOrderByFechaCompraDesc(Usuario usuario);
    
    boolean existsByUsuarioAndJuego(Usuario usuario, Juego juego);
    
    @Query("SELECT c.juego.id FROM Compra c WHERE c.usuario = :usuario")
    List<Long> findJuegoIdsByUsuario(@Param("usuario") Usuario usuario);
    
    @Query("SELECT SUM(c.precio) FROM Compra c")
    BigDecimal sumPrecioTotal();
    
    @Query("SELECT c FROM Compra c WHERE c.usuario.rol != 'ADMIN'")
    List<Compra> findComprasValidas();
}
```

**Características:**
- ✅ Extiende `JpaRepository<Entidad, TipoID>`
- ✅ Métodos CRUD automáticos
- ✅ Consultas por convención de nombres
- ✅ Consultas personalizadas con `@Query`
- ❌ **NO contiene lógica de negocio**

---

## 🔄 Flujo de Datos

### Ejemplo Completo: Comprar un Juego

```
1. Usuario hace clic en "Comprar" en el navegador
   ↓
2. Navegador envía POST /comprar/123
   ↓
3. CompraController recibe la petición
   ↓
4. CompraController llama a IServicioUsuario.buscarUsuarioPorEmail()
   ↓
5. ServicioUsuario llama a UsuarioRepository.findByEmail()
   ↓
6. Repository ejecuta: SELECT * FROM usuarios WHERE email = ?
   ↓
7. Usuario devuelto a CompraController
   ↓
8. CompraController llama a IServicioJuego.buscarJuegoPorId(123)
   ↓
9. ServicioJuego llama a JuegoRepository.findById(123)
   ↓
10. Repository ejecuta: SELECT * FROM juegos WHERE id = 123
   ↓
11. Juego devuelto a CompraController
   ↓
12. CompraController llama a IServicioCompra.verificarJuegoYaComprado()
   ↓
13. ServicioCompra llama a CompraRepository.existsByUsuarioAndJuego()
   ↓
14. Repository ejecuta: SELECT COUNT(*) FROM compras WHERE usuario_id = ? AND juego_id = ?
   ↓
15. Si ya existe → return "redirect:/catalogo?error=yaComprado"
   ↓
16. Si no existe → CompraController llama a IServicioCompra.crearNuevaCompra()
   ↓
17. ServicioCompra:
    - Crea objeto Compra
    - Llama a CompraRepository.save()
    - INSERT INTO compras (usuario_id, juego_id, precio, fecha_compra) VALUES (?, ?, ?, ?)
    ↓
18. ServicioCompra llama a IServicioBiblioteca.agregarJuegoABiblioteca()
    ↓
19. ServicioBiblioteca:
    - Crea objeto BibliotecaUsuario
    - Llama a BibliotecaUsuarioRepository.save()
    - INSERT INTO biblioteca_usuario (usuario_id, juego_id, fecha_adquisicion) VALUES (?, ?, ?)
    ↓
20. ServicioCompra llama a IServicioProveedor.crearMovimientoProveedor()
    ↓
21. ServicioProveedor:
    - Crea objeto MovimientoProveedor con 85% del precio
    - Llama a MovimientoProveedorRepository.save()
    - INSERT INTO movimiento_proveedor (proveedor_id, compra_id, monto, fecha) VALUES (?, ?, ?, ?)
    ↓
22. CompraController devuelve "redirect:/usuario/mi-biblioteca"
    ↓
23. Navegador redirige a /usuario/mi-biblioteca
    ↓
24. Usuario ve su juego en la biblioteca
```

**Resumen del flujo:**
```
Navegador → Controlador → Servicio → Repository → Base de Datos
                           ↓
                      (Lógica de negocio)
                           ↓
                   Otros servicios colaboran
```

---

## 🎓 Principios SOLID

### 1. **S** - Single Responsibility Principle
**Cada clase tiene una única responsabilidad.**

```java
// ✅ Correcto: Cada servicio tiene una responsabilidad clara
public class ServicioUsuario {
    // Solo gestiona usuarios
}

public class ServicioCompra {
    // Solo gestiona compras
}

// ❌ Incorrecto:
public class ServicioGeneral {
    // Gestiona usuarios, compras, juegos... (demasiadas responsabilidades)
}
```

---

### 2. **O** - Open/Closed Principle
**Abierto para extensión, cerrado para modificación.**

```java
// ✅ Correcto: Puedes crear nuevas implementaciones sin modificar la interfaz
public interface IServicioCompra {
    BigDecimal calcularComision(BigDecimal precio);
}

public class ServicioCompraBasico implements IServicioCompra {
    public BigDecimal calcularComision(BigDecimal precio) {
        return precio.multiply(new BigDecimal("0.15"));
    }
}

// Puedes añadir nuevas implementaciones
public class ServicioCompraPremium implements IServicioCompra {
    public BigDecimal calcularComision(BigDecimal precio) {
        return precio.multiply(new BigDecimal("0.10")); // 10% para premium
    }
}
```

---

### 3. **L** - Liskov Substitution Principle
**Las implementaciones deben ser sustituibles por su interfaz.**

```java
// ✅ Correcto: Cualquier implementación de IServicioCompra puede usarse
@Controller
public class CompraController {
    
    @Autowired
    private IServicioCompra servicioCompra; // Puede ser ServicioCompraBasico o ServicioCompraPremium
    
    public void procesarCompra() {
        servicioCompra.crearNuevaCompra(); // Funciona con cualquier implementación
    }
}
```

---

### 4. **I** - Interface Segregation Principle
**Muchas interfaces específicas mejor que una interfaz general.**

```java
// ✅ Correcto: Interfaces segregadas
public interface IServicioCompra {
    Compra crearNuevaCompra(Usuario usuario, Juego juego);
}

public interface IServicioProveedor {
    MovimientoProveedor crearMovimientoProveedor(Usuario proveedor, Compra compra, BigDecimal monto);
}

// ❌ Incorrecto: Interfaz demasiado grande
public interface IServicioGeneral {
    Compra crearCompra();
    Usuario crearUsuario();
    Juego crearJuego();
    // ... 50 métodos más
}
```

---

### 5. **D** - Dependency Inversion Principle
**Depende de abstracciones (interfaces), no de implementaciones.**

```java
// ✅ Correcto: Depende de la interfaz
@Controller
public class CompraController {
    
    @Autowired
    private IServicioCompra servicioCompra; // ← Interfaz
    
    // ...
}

// ❌ Incorrecto: Depende de la implementación
@Controller
public class CompraController {
    
    @Autowired
    private ServicioCompra servicioCompra; // ← Clase concreta
    
    // ...
}
```

---

## ✅ Ventajas del Patrón

### 1. Desacoplamiento
```java
// El controlador no sabe qué implementación usa
@Autowired
private IServicioCompra servicioCompra;

// Puedes cambiar la implementación sin tocar el controlador
// Spring decide automáticamente cuál inyectar
```

### 2. Testing Unitario
```java
// En tests, puedes hacer mock fácilmente
@Mock
private IServicioCompra servicioCompraMock;

@Test
void testCompra() {
    when(servicioCompraMock.calcularMontoParaProveedor(any()))
        .thenReturn(new BigDecimal("85.00"));
    
    // Test sin necesidad de base de datos real
}
```

### 3. Flexibilidad
```java
// Puedes tener múltiples implementaciones
public class ServicioCompraBasico implements IServicioCompra { }
public class ServicioCompraPremium implements IServicioCompra { }

// Y cambiar entre ellas con configuración
@Primary
@Service
public class ServicioCompraBasico implements IServicioCompra { }
```

### 4. Documentación Clara
```java
// La interfaz sirve como documentación
public interface IServicioCompra {
    /**
     * Crea una nueva compra de un juego.
     * @param usuario El usuario que compra
     * @param juego El juego a comprar
     * @return La compra creada y guardada
     */
    Compra crearNuevaCompra(Usuario usuario, Juego juego);
}
```

---

## 📖 Ejemplos Prácticos

### Ejemplo 1: Sistema de Comisiones

```java
// Interfaz
public interface IServicioCompra {
    BigDecimal calcularMontoParaProveedor(BigDecimal precio);
    BigDecimal calcularComisionPlataforma(BigDecimal precio);
}

// Implementación
@Service
public class ServicioCompra implements IServicioCompra {
    
    private static final BigDecimal COMISION_PROVEEDOR = new BigDecimal("0.85");
    private static final BigDecimal COMISION_PLATAFORMA = new BigDecimal("0.15");
    
    @Override
    public BigDecimal calcularMontoParaProveedor(BigDecimal precio) {
        // Proveedor recibe 85%
        return precio.multiply(COMISION_PROVEEDOR).setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public BigDecimal calcularComisionPlataforma(BigDecimal precio) {
        // Plataforma recibe 15%
        return precio.multiply(COMISION_PLATAFORMA).setScale(2, RoundingMode.HALF_UP);
    }
}

// Uso en controlador
@Controller
public class CompraController {
    
    @Autowired
    private IServicioCompra servicioCompra;
    
    @PostMapping("/comprar/{id}")
    public String comprar(@PathVariable Long id) {
        Juego juego = servicioJuego.buscarJuegoPorId(id);
        
        // Calcular comisiones
        BigDecimal paraProveedor = servicioCompra.calcularMontoParaProveedor(juego.getPrecio());
        BigDecimal paraPlataforma = servicioCompra.calcularComisionPlataforma(juego.getPrecio());
        
        System.out.println("Precio: " + juego.getPrecio());
        System.out.println("Para proveedor (85%): " + paraProveedor);
        System.out.println("Para plataforma (15%): " + paraPlataforma);
        
        // ... resto del código
    }
}
```

---

### Ejemplo 2: Validación de Compras

```java
// Interfaz
public interface IServicioCompra {
    boolean puedeRealizarCompras(Usuario usuario);
    boolean verificarJuegoYaComprado(Usuario usuario, Juego juego);
}

// Implementación
@Service
public class ServicioCompra implements IServicioCompra {
    
    @Autowired
    private CompraRepository compraRepository;
    
    @Override
    public boolean puedeRealizarCompras(Usuario usuario) {
        // Los administradores NO pueden comprar
        return !usuario.getRol().equals(Rol.ADMIN);
    }
    
    @Override
    public boolean verificarJuegoYaComprado(Usuario usuario, Juego juego) {
        return compraRepository.existsByUsuarioAndJuego(usuario, juego);
    }
}

// Uso en controlador
@Controller
public class CompraController {
    
    @Autowired
    private IServicioCompra servicioCompra;
    
    @PostMapping("/comprar/{id}")
    public String comprar(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
        Juego juego = servicioJuego.buscarJuegoPorId(id);
        
        // Validar si puede comprar
        if (!servicioCompra.puedeRealizarCompras(usuario)) {
            redirectAttributes.addFlashAttribute("error", "Los administradores no pueden comprar juegos");
            return "redirect:/catalogo";
        }
        
        // Verificar si ya lo compró
        if (servicioCompra.verificarJuegoYaComprado(usuario, juego)) {
            redirectAttributes.addFlashAttribute("error", "Ya tienes este juego en tu biblioteca");
            return "redirect:/catalogo";
        }
        
        // Procesar compra
        servicioCompra.crearNuevaCompra(usuario, juego);
        
        return "redirect:/usuario/mi-biblioteca";
    }
}
```

---

## 🎯 Resumen Final

### ¿Qué hemos aprendido?

1. **Patrón Interfaz + Implementación**
   - Separa **QUÉ** hacer (interfaz) de **CÓMO** hacerlo (implementación)
   - Es el estándar de Spring Framework

2. **Ventajas Clave**
   - ✅ Desacoplamiento
   - ✅ Testeable
   - ✅ Flexible
   - ✅ Cumple SOLID

3. **Arquitectura en Capas**
   - Vista → Controlador → Servicio (Interface) → Servicio (Impl) → Repository → BD

4. **Responsabilidades Claras**
   - **Controlador**: Recibir peticiones, devolver vistas
   - **Servicio**: Lógica de negocio
   - **Repository**: Acceso a datos

5. **Principios SOLID**
   - Cada clase tiene una responsabilidad
   - Dependemos de abstracciones, no de implementaciones

---

**Tu código ahora sigue las mejores prácticas de Spring Boot y está listo para presentar a tus profesores.** 🚀

Para más información, consulta:
- [SERVICIOS.md](SERVICIOS.md) - Guía de todos los servicios
- [MODELO_NEGOCIO.md](MODELO_NEGOCIO.md) - Sistema de comisiones
- [SEGURIDAD.md](SEGURIDAD.md) - Spring Security
