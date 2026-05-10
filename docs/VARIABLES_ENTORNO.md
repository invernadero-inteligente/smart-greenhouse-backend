# 🌿 Guía: Variables de Entorno y Configuración

**Versión:** 1.0  
**Última actualización:** 29 de Abril de 2026  
**Equipo IS**

---

## 📖 Descripción

Las variables de entorno permiten que la aplicación Spring Boot lea configuraciones sin exponerlas en el código. Esto es especialmente importante para:

- 🔐 **Credenciales** (contraseñas de BD, tokens JWT)
- 🌐 **URLs y dominios** (base de datos, SMTP, MQTT)
- 🔑 **Claves secretas** (JWT, API keys)
- 🚀 **Configuración por entorno** (desarrollo, staging, producción)

---

## 📋 Archivos Importantes

```
backend/
├── application.properties          ← Importa from .env
├── .env.example                   ← Plantilla (en Git) ✅
├── template.env                   ← Completa con docs (en Git) ✅
└── .env                          ← Tu configuración local (NO en Git) ❌
```

---

## 🚀 Pasos para Comenzar (5 minutos)

### 1️⃣ Crear tu archivo `.env` local

```bash
# Desde la carpeta backend/
cp .env.example .env
```

### 2️⃣ Editar según tu entorno local

```ini
# Ejemplo: variables mínimas para desarrollo local

DB_HOST=localhost
DB_PORT=5432
DB_NAME=invernadero_db
DB_USERNAME=postgres
DB_PASSWORD=tu_contraseña_postgres

JWT_SECRET=una_clave_secreta_minimo_32_caracteres_aqui

CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

MAIL_USERNAME=tu_email@gmail.com
MAIL_PASSWORD=contraseña_de_aplicacion_gmail
```

### 3️⃣ Compilar y ejecutar

```bash
# Compilar
mvn clean compile

# Ejecutar
mvn spring-boot:run

# O usando Maven Wrapper
./mvnw spring-boot:run
```

### 4️⃣ Verificar que funciona

```bash
# En otra terminal
curl http://localhost:8080/api/v1/invernaderos
```

---

## 🔐 Cómo Spring Boot Carga las Variables

```
application.properties contiene:
spring.config.import=optional:file:.env[.properties]
         ↓
Spring Boot busca .env en la raíz del proyecto
         ↓
Lee cada línea como variable de entorno
         ↓
Las propiedades usan ${VARIABLE_NAME:valor_defecto}
```

**Ejemplo:**
```properties
# En application.properties
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:invernadero_db}

# Si .env tiene:
DB_HOST=myserver.com
DB_PORT=5434

# Resultado final:
# spring.datasource.url=jdbc:postgresql://myserver.com:5434/invernadero_db
```

---

## 📚 Variables Disponibles

### 🗄️ Base de Datos
| Variable | Defecto | Ejemplo |
|----------|---------|---------|
| `DB_HOST` | localhost | localhost, mydb.example.com |
| `DB_PORT` | 5432 | 5432 |
| `DB_NAME` | invernadero_db | invernadero_db_dev |
| `DB_USERNAME` | postgres | miusuario |
| `DB_PASSWORD` | postgres123 | 🔐 **Cambiar en producción** |

### 🔐 Autenticación (JWT)
| Variable | Tiempo | Notas |
|----------|--------|-------|
| `JWT_SECRET` | - | **Mínimo 32 caracteres.** Generar con openssl o [random.org](https://random.org) |
| `JWT_EXPIRATION_MS` | 86400000 | Token expira en 24h (en ms) |
| `JWT_REFRESH_EXPIRATION_MS` | 604800000 | Refresh token expira en 7 días |

### 🌐 CORS (Frontend)
```ini
# Orígenes permitidos (separados por comas)
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# En producción, añadir tu dominio
CORS_ALLOWED_ORIGINS=https://miapp.com,https://admin.miapp.com
```

### 📧 Email (Notificaciones)
```ini
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu_email@gmail.com
MAIL_PASSWORD=contraseña_de_aplicacion  # NO la contraseña de Gmail
```

**Nota:** Para Gmail, genera una [Contraseña de Aplicación](https://myaccount.google.com/apppasswords).

### 🔌 IoT / MQTT
```ini
IOT_MQTT_BROKER_URL=mqtt://localhost:1883  # O 'mqtt://broker.example.com'
IOT_MQTT_CLIENT_ID=invernadero-backend
IOT_MQTT_USERNAME=usuario_mqtt
IOT_MQTT_PASSWORD=contraseña_mqtt
IOT_MQTT_TOPIC_PREFIX=invernadero/
```

---

## ⚠️ Seguridad

### ✅ Checklist de Seguridad

- [ ] **`.env` está en `.gitignore`** (nunca commitear valores reales)
- [ ] **Contraseña BD cambiad**a en producción (NO usar `postgres123`)
- [ ] **JWT_SECRET es único y seguro** (mínimo 32 caracteres)
- [ ] **MAIL_PASSWORD usa contraseña de aplicación**, no la contraseña de Gmail
- [ ] **CORS_ALLOWED_ORIGINS** lista solo dominios necesarios
- [ ] **No grabes secretos en logs** (ver `LOGGING_LEVEL_ROOT`)

### 🔑 Generar JWT_SECRET Seguro

**Linux/Mac:**
```bash
openssl rand -base64 32
```

**Windows PowerShell:**
```powershell
[Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

**Resultado ejemplo:**
```
a3F7kL9mP2q8sT4lL6vN9bY1xQ5wR8jH2uI0oP3mK7nL9vQ2sT6uV8wX0yZ
```

---

## 🛑 Errores Comunes

### ❌ "No configuration properties found"
**Causa:** `.env` no existe o está mal ubicado  
**Solución:**
```bash
# Verificar ubicación
ls -la .env  # En Linux/Mac

# En Windows PowerShell
Get-ChildItem -Name .env
```

### ❌ "Could not resolve placeholder 'VARIABLE_NAME'"
**Causa:** Variable referenciada en `application.properties` no existe en `.env`  
**Solución:** Añadir la variable a `.env` o usar valor por defecto en `application.properties`:
```properties
app.jwt.secret=${JWT_SECRET:valor_por_defecto_aqui}
```

### ❌ Variables no se actualizan al cambiar `.env`
**Causa:** Aplicación en memoria  
**Solución:** Reiniciar la aplicación
```bash
# Mata el proceso y vuelve a ejecutar
mvn spring-boot:run
```

---

## 🚀 Configuración por Entornos

Para desarrollo, staging y producción separados:

```bash
# Crear archivos
.env.development
.env.staging
.env.production
```

**Opción 1:** Cambiar nombre antes de ejecutar
```bash
# Desarrollo
cp .env.development .env
mvn spring-boot:run

# Producción
cp .env.production .env
java -jar target/invernadero-0.0.1.jar
```

**Opción 2:** Usar Spring Profiles (avanzado)
```properties
# application.properties
spring.config.import=optional:file:.env.${spring.profiles.active}[.properties]
```

```bash
# Ejecutar con perfil específico
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=production"
```

---

## 📞 Soporte

Si tienes problemas con variables de entorno:

1. Revisa `docs/ENV_CONFIG.md` (detallado) en la raíz
2. Consulta `ENV_CONFIG.md` en logs: `tail -n 50 spring.log`
3. Pregunta al equipo IS en Taiga o GitHub Discussions

---

## ✅ Checklist: Antes de Hacer Commit

- [ ] `.env` actual NO está commiteado
- [ ] `.env.example` está actualizado con placeholders
- [ ] `template.env` está sincronizado
- [ ] `.gitignore` incluye `.env`
- [ ] Documentación está actualizada en este archivo

---

**🌿 ¡Ya estás listo para desarrollar! Cualquier duda, consulta el equipo IS.**

