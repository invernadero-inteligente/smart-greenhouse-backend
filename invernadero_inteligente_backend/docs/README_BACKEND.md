# 🌿 Backend - Invernadero Inteligente

Sistema API REST para la gestión y monitoreo de invernaderos inteligentes, desarrollado con **Java 21 + Spring Boot 4.0.6**

## 📋 Descripción del Proyecto

Motor backend que integra datos de sensores IoT, procesa información agroclimática y proporciona APIs para el monitoreo, control y análisis de variables ambientales en invernaderos.

**Stack Tecnológico:**
- ☕ **Java 21**
- 🌱 **Spring Boot 4.0.6**
- 🔐 **Spring Security + JWT**
- 🗄️ **PostgreSQL + MongoDB**
- 🗂️ **Spring Data JPA / MongoDB**
- 🔄 **ModelMapper para DTOs**
- 📝 **Lombok**
- 🧪 **JUnit 5 + Mockito**

---

## 📁 Estructura de Carpetas (Arquitectura Modular)

```
src/main/java/com/invernadero/invernadero_inteligente_backend/
│
├── 📄 InvernaderoInteligenteBackendApplication.java
│   └── Punto de entrada de la aplicación
│
├── 🔌 controllers/
│   ├── BaseController.java           → Clase base para todos los controladores
│   ├── InvernaderoController.java    → Endpoints de invernaderos
│   ├── SensorController.java         → Endpoints de sensores
│   ├── UsuarioController.java        → Endpoints de usuarios
│   ├── LecturaController.java        → Endpoints de lecturas de sensores
│   └── ReporteController.java        → Endpoints de reportes
│
├── 📦 dtos/
│   ├── PageResponseDTO.java          → DTO para respuestas paginadas
│   │
│   ├── request/
│   │   ├── PageRequestDTO.java       → DTO para solicitudes paginadas
│   │   ├── CrearInvernaderoDTO.java  → Request para crear invernadero
│   │   ├── CrearSensorDTO.java       → Request para crear sensor
│   │   ├── LoginRequestDTO.java      → Request de login
│   │   └── CrearLecturaDTO.java      → Request para crear lectura
│   │
│   └── response/
│       ├── ApiResponseDTO.java       → DTO estándar para respuestas API
│       ├── InvernaderoResponseDTO.java
│       ├── SensorResponseDTO.java
│       ├── UsuarioResponseDTO.java
│       └── TokenResponseDTO.java
│
├── 🏢 models/
│   │
│   ├── entities/
│   │   ├── AuditableEntity.java      → Clase base con timestamps
│   │   ├── Invernadero.java          → Entidad invernadero (@Entity JPA)
│   │   ├── Sensor.java               → Entidad sensor
│   │   ├── Lectura.java              → Entidad lectura de sensores
│   │   ├── Usuario.java              → Entidad usuario
│   │   ├── Variable.java             → Entidad variable agroclimática
│   │   └── Alerta.java               → Entidad alertas
│   │
│   └── enums/
│       ├── EstadoInvernadero.java    → ACTIVO, INACTIVO, MANTENIMIENTO, ALERTA, CRÍTICO
│       ├── RolUsuario.java           → ADMINISTRADOR, ENCARGADO, TÉCNICO, CONSULTOR
│       └── TipoSensor.java           → TEMPERATURA, HUMEDAD, PH, etc.
│
├── 🔌 repositories/
│   ├── InvernaderoRepository.java    → JPA Repository para Invernadero
│   ├── SensorRepository.java         → JPA Repository para Sensor
│   ├── LecturaRepository.java        → JPA Repository para Lectura
│   ├── UsuarioRepository.java        → JPA Repository para Usuario
│   ├── VariableRepository.java       → JPA Repository para Variable
│   └── AlertaRepository.java         → JPA Repository para Alerta
│
├── 🔧 services/
│   │
│   ├── BaseService.java              → Interfaz base para servicios CRUD
│   │
│   ├── InvernaderoService.java       → Interfaz de servicios de invernadero
│   ├── SensorService.java            → Interfaz de servicios de sensores
│   ├── LecturaService.java           → Interfaz de servicios de lecturas
│   ├── UsuarioService.java           → Interfaz de servicios de usuarios
│   ├── VariableService.java          → Interfaz de servicios de variables
│   ├── AlertaService.java            → Interfaz de servicios de alertas
│   └── AuthService.java              → Interfaz de servicios de autenticación
│
│   └── impl/
│       ├── InvernaderoServiceImpl.java
│       ├── SensorServiceImpl.java
│       ├── LecturaServiceImpl.java
│       ├── UsuarioServiceImpl.java
│       ├── VariableServiceImpl.java
│       ├── AlertaServiceImpl.java
│       └── AuthServiceImpl.java
│
├── 🗺️ mappers/
│   ├── IMapper.java                  → Interfaz base para mappers
│   ├── InvernaderoMapper.java        → Mapper Invernadero ↔ InvernaderoDTO
│   ├── SensorMapper.java             → Mapper Sensor ↔ SensorDTO
│   ├── LecturaMapper.java            → Mapper Lectura ↔ LecturaDTO
│   ├── UsuarioMapper.java            → Mapper Usuario ↔ UsuarioDTO
│   ├── VariableMapper.java           → Mapper Variable ↔ VariableDTO
│   └── AlertaMapper.java             → Mapper Alerta ↔ AlertaDTO
│
├── 🔐 security/
│   ├── JwtTokenProvider.java         → Generador y validador de JWT
│   ├── JwtAuthenticationFilter.java  → Filtro de autenticación JWT
│   ├── CustomUserDetailsService.java → Servicio de detalles de usuario
│   └── SecurityConstants.java        → Constantes de seguridad
│
├── ⚠️ exceptions/
│   ├── ResourceNotFoundException.java → Recurso no encontrado
│   ├── ValidationException.java      → Errores de validación
│   ├── UnauthorizedException.java    → No autorizado
│   └── GlobalExceptionHandler.java   → Manejador global de excepciones
│
├── 🎨 filters/
│   ├── RequestLoggingFilter.java     → Filtro para loguear solicitudes
│   └── CorsFilter.java               → Filtro CORS personalizado
│
├── ⚙️ config/
│   ├── CorsConfig.java               → Configuración CORS
│   ├── SecurityConfig.java           → Configuración de seguridad
│   ├── ModelMapperConfig.java        → Configuración de ModelMapper
│   └── JpaConfig.java                → Configuración JPA
│
├── 🔧 validation/
│   ├── InvernaderoValidator.java     → Validaciones de invernadero
│   ├── SensorValidator.java          → Validaciones de sensor
│   ├── UsuarioValidator.java         → Validaciones de usuario
│   └── LecturaValidator.java         → Validaciones de lectura
│
├── 🔨 utils/
│   ├── ResponseUtil.java             → Utilidad para respuestas estandarizadas
│   ├── ValidationUtil.java           → Utilidad para validaciones comunes
│   ├── DateUtil.java                 → Utilidades de fechas
│   └── JsonUtil.java                 → Utilidades JSON
│
└── 📌 constants/
    └── AppConstants.java             → Constantes globales de la aplicación
```

---

## 🚀 Patrones de Arquitectura Implementados

### 1. **Arquitectura en Capas (Layered Architecture)**
- **Controller Layer:** Recibe y responde solicitudes HTTP
- **Service Layer:** Lógica de negocio
- **Repository Layer:** Acceso a datos
- **Model Layer:** Entidades y enumeraciones

### 2. **Inyección de Dependencias**
```java
@Service
public class InvernaderoServiceImpl implements InvernaderoService {
    
    @Autowired
    private InvernaderoRepository invernaderoRepository;
    
    @Autowired
    private InvernaderoMapper invernaderoMapper;
}
```

### 3. **Data Transfer Objects (DTOs)**
Separación entre modelos de base de datos y respuestas API

### 4. **Mapper Pattern**
Conversión automática entre entidades y DTOs usando ModelMapper

### 5. **Global Exception Handling**
Manejo centralizado de excepciones con respuestas estandarizadas

### 6. **RESTful API Design**
Endpoints siguiendo estándares REST con versionado (`/api/v1/`)

---

## 📌 Convenciones de Código

### Naming Conventions
- **Paquetes:** `com.invernadero.invernadero_inteligente_backend.{capa}`
- **Clases:** PascalCase (ej: `InvernaderoService`)
- **Métodos:** camelCase (ej: `obtenerInvernaderosPorUsuario()`)
- **Constantes:** UPPER_SNAKE_CASE
- **Variables:** camelCase

### Estructura de DTOs
```java
// Request DTOs terminan en ...RequestDTO
public class CrearInvernaderoDTO { }

// Response DTOs terminan en ...ResponseDTO
public class InvernaderoResponseDTO { }
```

### Estructura de Servicios
```java
// Interfaz
public interface InvernaderoService extends BaseService<Invernadero, Long> { }

// Implementación
@Service
@Transactional
public class InvernaderoServiceImpl implements InvernaderoService { }
```

---

## 🔌 Endpoints Principales (Ejemplos)

```
INVERNADEROS
GET    /api/v1/invernaderos              → Listar todos (paginado)
POST   /api/v1/invernaderos              → Crear nuevo
GET    /api/v1/invernaderos/{id}         → Obtener por ID
PUT    /api/v1/invernaderos/{id}         → Actualizar
DELETE /api/v1/invernaderos/{id}         → Eliminar

SENSORES
GET    /api/v1/sensores                  → Listar todos
POST   /api/v1/sensores                  → Crear nuevo
GET    /api/v1/sensores/{id}             → Obtener por ID
PUT    /api/v1/sensores/{id}             → Actualizar
DELETE /api/v1/sensores/{id}             → Eliminar

LECTURAS DE SENSORES
GET    /api/v1/lecturas                  → Listar todas
POST   /api/v1/lecturas                  → Crear nueva lectura
GET    /api/v1/lecturas/sensor/{sensorId} → Lecturas por sensor

USUARIOS
POST   /api/v1/auth/registro             → Registrar usuario
POST   /api/v1/auth/login                → Login y obtener JWT
GET    /api/v1/usuarios/{id}             → Obtener perfil
PUT    /api/v1/usuarios/{id}             → Actualizar perfil

ALERTAS
GET    /api/v1/alertas                   → Listar alertas activas
POST   /api/v1/alertas                   → Crear alerta
PUT    /api/v1/alertas/{id}              → Actualizar alerta
DELETE /api/v1/alertas/{id}              → Eliminar alerta

REPORTES
GET    /api/v1/reportes/invernadero/{id} → Reporte por invernadero
GET    /api/v1/reportes/sensor/{id}      → Reporte por sensor
```

---

## 🔐 Seguridad

### Autenticación JWT
- Tokens con expiración configurable
- Refresh tokens para sesiones prolongadas
- Validación en filtro personalizado

### Autorización Basada en Roles
- **ADMINISTRADOR:** Acceso total
- **ENCARGADO:** Gestión de invernaderos asignados
- **TÉCNICO:** Lectura y configuración de sensores
- **CONSULTOR:** Solo lectura
- **USUARIO:** Lectura limitada

---

## 📊 Base de Datos

### PostgreSQL (Datos transaccionales)
- Usuarios
- Invernaderos
- Sensores
- Variables configurables
- Alertas
- Configuraciones del sistema

### MongoDB (Series de tiempo)
- Lecturas de sensores (alto volumen)
- Logs de eventos
- Datos históricos

---

## 🧪 Testing

Estructura de directorios para tests:

```
src/test/java/com/invernadero/invernadero_inteligente_backend/
├── controllers/
├── services/
├── repositories/
└── utils/
```

**Tipos de Tests:**
- Unit Tests (Servicios, Utilidades)
- Integration Tests (Controladores, Repositorios)
- E2E Tests (Flujos completos)

---

## 🏃 Cómo Ejecutar

```bash
# Compilar proyecto
mvn clean compile

# Ejecutar aplicación
mvn spring-boot:run

# Ejecutar tests
mvn test

# Generar JAR
mvn clean package

# Ejecutar con profile específico
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

---

## 📝 Propiedades de Configuración

Archivo: `src/main/resources/application.properties`

```properties
# Server
server.port=8080
server.servlet.context-path=/

# Database - PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/invernadero_db
spring.datasource.username=postgres
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update

# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/invernadero_sensores

# JWT
jwt.secret=your-secret-key-here
jwt.expiration=18000000

# Logging
logging.level.root=INFO
logging.level.com.invernadero=DEBUG
```

---

## 🤝 Contribución

1. Crear rama desde `develop`: `git checkout -b feature/is/nombre-funcionalidad`
2. Hacer cambios manteniendo la estructura modular
3. Escribir tests para nuevas funcionalidades
4. Cumplir con el estándar de código
5. Crear Pull Request hacia `develop`

---

## 📚 Recursos Útiles

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Security](https://spring.io/projects/spring-security)
- [JWT.io](https://jwt.io/)
- [ModelMapper](http://modelmapper.org/)

---

## ✉️ Contacto

**Equipo IS (Ingeniería de Software)**
- Líder: Jorel Vargas
- Backend Lead: [Tu nombre]

---

**Última actualización:** abril 2026  
**Versión:** 1.0.0

