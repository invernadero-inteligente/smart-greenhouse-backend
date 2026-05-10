# 📚 GUÍA RÁPIDA - Estructura Modular Backend

Guía paso a paso para crear nuevas funcionalidades siguiendo la arquitectura modular.

---

## 🎯 Flujo de Desarrollo

Cuando necesites crear una nueva entidad (ej: **Sensor**), sigue este orden:

```
1. MODELO → 2. REPOSITORY → 3. SERVICE → 4. MAPPER → 5. DTO → 6. CONTROLLER
```

---

## 📋 Checklist para Nueva Funcionalidad

### Paso 1: Crear Entidad (Model)

**Archivo:** `models/entities/MiEntidad.java`

```java
@Entity
@Table(name = "mi_entidad")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiEntidad extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    // ... más propiedades
    
    @PrePersist
    private void prePersist() {
        super.prePersist();
    }
    
    @PreUpdate
    private void preUpdate() {
        super.preUpdate();
    }
}
```

**Considerations:**
- Extender `AuditableEntity` para tener timestamps automáticos
- Usar `@Entity` y `@Table` con nombre en minúsculas
- Usar `@Data` de Lombok para getters/setters
- Agregar índices si es necesario con `@Index`

---

### Paso 2: Crear Repository

**Archivo:** `repositories/MiEntidadRepository.java`

```java
@Repository
public interface MiEntidadRepository extends JpaRepository<MiEntidad, Long> {
    
    // Consultas personalizadas
    Optional<MiEntidad> findByNombre(String nombre);
    
    List<MiEntidad> findByActivo(Boolean activo);
    
    @Query("SELECT m FROM MiEntidad m WHERE m.nombre LIKE %:nombre%")
    Page<MiEntidad> searchByNombre(@Param("nombre") String nombre, Pageable pageable);
}
```

**Considerations:**
- Usar `JpaRepository` como base
- Métodos queries automáticos: `findBy...`, `countBy...`
- Consultas personalizadas con `@Query` cuando sea necesario
- Siempre incluir paginación en listados

---

### Paso 3: Crear Service (Interfaz + Implementación)

**Archivo:** `services/MiEntidadService.java`

```java
public interface MiEntidadService extends BaseService<MiEntidad, Long> {
    
    // Operaciones específicas del dominio
    List<MiEntidadResponseDTO> getByActivo(Boolean activo);
    
    Page<MiEntidadResponseDTO> getAllPaginated(
        String nombre,
        Boolean activo,
        Pageable pageable
    );
    
    MiEntidadResponseDTO crearMiEntidad(CrearMiEntidadRequestDTO dto);
}
```

**Archivo:** `services/impl/MiEntidadServiceImpl.java`

```java
@Slf4j
@Service
@Transactional
public class MiEntidadServiceImpl implements MiEntidadService {
    
    @Autowired
    private MiEntidadRepository miEntidadRepository;
    
    @Autowired
    private MiEntidadMapper miEntidadMapper;
    
    @Override
    public MiEntidad create(MiEntidad entity) {
        log.info("Creando nueva entidad: {}", entity.getNombre());
        ValidationUtil.validateNotNull(entity, "entidad");
        
        entity.prePersist();
        return miEntidadRepository.save(entity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<MiEntidad> getById(Long id) {
        return miEntidadRepository.findById(id);
    }
    
    // ... implementar otros métodos
}
```

**Considerations:**
- Usar `@Transactional` en la clase
- Usar `@Transactional(readOnly = true)` para consultas
- Usar `Slf4j` para logging
- Inyectar `@Autowired` el repository y mapper
- Validar inputs con `ValidationUtil`
- Lanzar `ResourceNotFoundException` si no existe

---

### Paso 4: Crear Mappers

**Archivo:** `mappers/MiEntidadMapper.java`

```java
@Component
public class MiEntidadMapper implements IMapper<MiEntidad, MiEntidadResponseDTO> {
    
    @Autowired
    private ModelMapper modelMapper;
    
    @Override
    public MiEntidadResponseDTO toDTO(MiEntidad entity) {
        if (entity == null) return null;
        return modelMapper.map(entity, MiEntidadResponseDTO.class);
    }
    
    @Override
    public MiEntidad toEntity(MiEntidadResponseDTO dto) {
        if (dto == null) return null;
        return modelMapper.map(dto, MiEntidad.class);
    }
}
```

---

### Paso 5: Crear DTOs

**Archivo:** `dtos/request/CrearMiEntidadRequestDTO.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearMiEntidadRequestDTO {
    
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 3, max = 100)
    private String nombre;
    
    @Size(max = 500)
    private String descripcion;
    
    @NotNull(message = "El estado es requerido")
    private Boolean activo;
}
```

**Archivo:** `dtos/response/MiEntidadResponseDTO.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiEntidadResponseDTO {
    
    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

### Paso 6: Crear Controlador

**Archivo:** `controllers/MiEntidadController.java`

```java
@Slf4j
@RestController
@RequestMapping(API_PREFIX + "/mi-entidad")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MiEntidadController extends BaseController {
    
    @Autowired
    private MiEntidadService miEntidadService;
    
    @GetMapping
    public ResponseEntity<ApiResponseDTO<?>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MiEntidadResponseDTO> result = 
            miEntidadService.getAllPaginated(null, true, pageable);
        
        return ResponseEntity.ok(
            ApiResponseDTO.success("Datos obtenidos", result)
        );
    }
    
    @PostMapping
    public ResponseEntity<ApiResponseDTO<?>> create(
            @Valid @RequestBody CrearMiEntidadRequestDTO dto) {
        
        MiEntidadResponseDTO created = miEntidadService.crearMiEntidad(dto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponseDTO.success("Creado exitosamente", created));
    }
    
    // ... otros métodos CRUD
}
```

---

## 🔧 Patrones Comunes

### 1. Validación en Service
```java
if (entity.getArchivo() == null || entity.getArchivo().isEmpty()) {
    throw new ValidationException("archivo", "El archivo es obligatorio");
}
```

### 2. Búsqueda por ID con manejo de excepciones
```java
Invernadero invernadero = invernaderoRepository.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Invernadero", "id", id));
```

### 3. Convertir Page<Entity> a Page<DTO>
```java
Page<MiEntidadResponseDTO> pagina = repository.findAll(pageable)
    .map(miEntidadMapper::toDTO);
```

### 4. Crear respuesta exitosa
```java
return ResponseEntity.ok(
    ApiResponseDTO.success("Mensaje", datos)
);
```

### 5. Crear respuesta de error
```java
return ResponseEntity.status(HttpStatus.BAD_REQUEST)
    .body(ApiResponseDTO.error(400, "Mensaje de error"));
```

---

## 📊 Ejemplo Completo: Crear módulo SENSOR

### 1️⃣ Entidad (Sensor.java)
```java
@Entity
@Table(name = "sensores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sensor extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String codigo;
    
    @Enumerated(EnumType.STRING)
    private TipoSensor tipo;
    
    @ManyToOne
    @JoinColumn(name = "invernadero_id")
    private Invernadero invernadero;
}
```

### 2️⃣ Repository (SensorRepository.java)
```java
@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {
    List<Sensor> findByInvernaderoId(Long invernaderoId);
    Optional<Sensor> findByCodigo(String codigo);
}
```

### 3️⃣ Service
```java
public interface SensorService extends BaseService<Sensor, Long> {
    List<SensorResponseDTO> getByInvernadero(Long invernaderoId);
}
```

### 4️⃣ Mapper, DTO, Controller
(Seguir el patrón anterior)

---

## 🎓 Principios SOLID a Seguir

| Principio | Ejemplo |
|-----------|---------|
| **S**ingle Responsibility | Cada clase tiene una sola razón para cambiar |
| **O**pen/Closed | Abierto para extensión, cerrado para modificación |
| **L**iskov Substitution | Las subclases pueden reemplazar a las clases base |
| **I**nterface Segregation | Interfaces específicas, no genéricas |
| **D**ependency Inversion | Depender de abstracciones, no de implementaciones |

---

## 🚫 Errores Comunes

❌ **NO hacer:**
```java
// Mezclar lógica en controladores
@PostMapping
public void crear(Entity entity) {
    // Validaciones, transformaciones, etc.
}
```

✅ **HACER:**
```java
// Delegar a services
@PostMapping
public ResponseEntity<ApiResponseDTO<?>> crear(
        @Valid @RequestBody CrearDTO dto) {
    ResponseDTO result = service.crear(dto);
    return ResponseEntity.ok(ApiResponseDTO.success(result));
}
```

---

## 📞 Preguntas Frecuentes

**P: ¿Dónde va la lógica de negocio compleja?**
R: En los Services, no en los Controllers ni en las entidades.

**P: ¿Cómo manejo relaciones entre entidades?**
R: Usa `@ManyToOne`, `@OneToMany`, `@ManyToMany` en las entidades y carga lazy con DTOs.

**P: ¿Cuándo usar MongoDB vs PostgreSQL?**
R: PostgreSQL para datos transaccionales (usuarios, configuración), MongoDB para series de tiempo (sensores).

**P: ¿Cómo validar datos complejos?**
R: Crea clases en `validation/` o usa `@Custom` annotations.

---

## 📌 Comandos Git

```bash
# Crear rama de feature
git checkout develop
git checkout -b feature/is/nombre-funcionalidad

# Commit siguiendo convención
git commit -m "feat(is): descripción del cambio"
git commit -m "fix(is): descripción del fix"
git commit -m "refactor(is): descripción del refactoring"

# Push y Pull Request
git push origin feature/is/nombre-funcionalidad
# Abrir PR en GitHub hacia develop
```

---

## ✅ Checklist Final

Antes de hacer commit, verifica:

- [ ] Código compila sin errores
- [ ] Tests unitarios pasan
- [ ] Sigue la estructura modular
- [ ] Usa Lombok para reducir boilerplate
- [ ] Tiene logging apropiado
- [ ] Validación de inputs
- [ ] DTOs en request/response
- [ ] Manejo de excepciones
- [ ] Documentación de métodos públicos
- [ ] Commit message en convención

---

**¡Listo! Ahora estás preparado para desarrollar módulos modulares y mantenibles.** 🚀

Para dudas, consulta el `README_BACKEND.md`

