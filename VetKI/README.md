# 🐾 VetKI - Sistema de Gestión de Clínica Veterinaria

API REST robusta, segura y escalable desarrollada con **Spring Boot 3**, **Spring Security**, **JPA/Hibernate** y **PostgreSQL** para la administración integral de pacientes, citas médicas y expedientes clínicos veterinarios.

---

## 📋 Información del Proyecto

* **Desarrollador / Alumno:** Javier Adrían Cajchún Zúñiga
* **Carné:** 2023512
* **Paquete Base:** `com.jcajchun_2023512.VetKI`
* **Puerto Predeterminado:** `8081`

---

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java 21 / 17
* **Framework:** Spring Boot 3.2.5
  * **Spring Web**: Construcción de API RESTful.
  * **Spring Security 6**: Seguridad, filtros HTTP y RBAC (Role-Based Access Control).
  * **Spring Data JPA**: Persistencia relacional con Hibernate.
  * **Spring Validation**: Validación declarativa con Bean Validation (Jakarta).
* **Seguridad & Token:** JJWT (`io.jsonwebtoken` 0.12.6) con HMAC-SHA256 y hashing de contraseñas con **BCrypt**.
* **Base de Datos:** PostgreSQL 15+
* **Utilidades:** Project Lombok
* **Testing:** JUnit 5, Mockito, Spring Security Test, MockMvc

---

## 🏛️ Arquitectura del Sistema

El proyecto sigue una arquitectura en capas desacoplada y orientada al dominio:

```
src/main/java/com/jcajchun_2023512/VetKI/
├── controller/         # Endpoints REST expuestos al cliente
│   ├── AuthController.java
│   ├── MascotaController.java
│   ├── CitaMedicaController.java
│   └── ExpedienteClinicoController.java
├── service/            # Interfaces de servicios del dominio
│   ├── impl/           # Implementación de lógica de negocio y reglas
├── repository/         # Interfaces Spring Data JPA
├── entity/             # Entidades mapeadas con JPA
│   └── enums/          # Enumeradores (Rol, Especie, EstadoCita)
├── dto/                # Objetos de transferencia de datos (Request / Response)
├── security/           # Filtro JWT, UserDetailsService, JwtService y SecurityConfig
└── exception/          # Manejador global de excepciones (@RestControllerAdvice)
```

---

## ⚙️ Configuración y Ejecución

### 1. Requisitos Previos
* JDK 17 o superior instalado.
* PostgreSQL corriendo localmente en el puerto `5432`.
* Base de datos creada: `vetki_db`.

### 2. Configuración en `application.properties`
El archivo [`src/main/resources/application.properties`](file:///c:/Users/Informatica/Desktop/Examen_2023512_VetKI/Examen_VetKI/VetKI/src/main/resources/application.properties) contiene los parámetros de conexión:

```properties
server.port=8081
spring.datasource.url=jdbc:postgresql://localhost:5432/vetki_db
spring.datasource.username=postgres
spring.datasource.password=admin
spring.jpa.hibernate.ddl-auto=update
spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=always
```

### 3. Ejecutar la Aplicación
Abre una terminal en el directorio raíz del proyecto y ejecuta:

```powershell
./mvnw spring-boot:run
```
O con Maven instalado globalmente:
```powershell
mvn spring-boot:run
```

---

## 👥 Usuarios Iniciales y Roles (Semilla)

El script [`src/main/resources/data.sql`](file:///c:/Users/Informatica/Desktop/Examen_2023512_VetKI/Examen_VetKI/VetKI/src/main/resources/data.sql) inicializa automáticamente usuarios para cada rol con contraseñas codificadas en **BCrypt**:

| Rol | Nombre | Correo Electrónico | Contraseña | ID |
| :--- | :--- | :--- | :--- | :---: |
| **ADMIN** | Administrador VetKI | `admin@vetki.com` | `Admin1234!` | `1` |
| **VET** | Dr. Carlos Ramírez | `vet@vetki.com` | `Vet1234!` | `2` |
| **CLIENTE** | María López | `cliente@vetki.com` | `Cliente1234!` | `3` |

---

## 🔒 Matriz de Seguridad y Endpoints

Todas las rutas (excepto `/api/v1/auth/**`) requieren la cabecera:  
`Authorization: Bearer <TOKEN_JWT>`

| Endpoint | Método | Roles Permitidos | Descripción |
| :--- | :---: | :---: | :--- |
| `/api/v1/auth/register` | `POST` | Público | Registrar un nuevo usuario (rol CLIENTE por defecto) |
| `/api/v1/auth/login` | `POST` | Público | Autenticar credenciales y obtener JWT |
| `/api/v1/mascotas` | `POST` | **CLIENTE, ADMIN** | Registrar una nueva mascota |
| `/api/v1/mascotas/mis-mascotas` | `GET` | **CLIENTE** | Listar mascotas del cliente autenticado |
| `/api/v1/mascotas/{id}` | `GET` | **VET, ADMIN** | Consultar mascota por ID |
| `/api/v1/citas` | `POST` | **CLIENTE, ADMIN** | Agendar una cita médica |
| `/api/v1/citas/mis-citas` | `GET` | **CLIENTE** | Consultar historial de citas del cliente |
| `/api/v1/citas/agenda` | `GET` | **VET, ADMIN** | Consultar agenda de citas del veterinario |
| `/api/v1/citas/{id}` | `GET` | **CLIENTE, VET, ADMIN** | Consultar cita por ID |
| `/api/v1/citas/{id}/cancelar` | `PATCH` | **CLIENTE, ADMIN** | Cancelar una cita médica |
| `/api/v1/expedientes` | `POST` | **VET, ADMIN** | Registrar diagnóstico y tratamiento (completa la cita) |
| `/api/v1/expedientes/mascota/{id}` | `GET` | **CLIENTE, VET, ADMIN** | Consultar historial clínico de una mascota |
| `/api/v1/expedientes/{id}` | `GET` | **CLIENTE, VET, ADMIN** | Consultar expediente específico por ID |

---

## 📖 Catálogo de Peticiones 

### 1. Autenticación (`/api/v1/auth`)

#### Iniciar Sesión (Login)
* **POST** `http://localhost:8081/api/v1/auth/login`
```json
{
  "email": "cliente@vetki.com",
  "password": "Cliente1234!"
}
```

#### Registro de Usuario
* **POST** `http://localhost:8081/api/v1/auth/register`
```json
{
  "nombre": "Ana Morales",
  "email": "ana.morales@test.com",
  "password": "Password123!",
  "telefono": "50244445555"
}
```

---

### 2. Mascotas (`/api/v1/mascotas`)

#### Registrar Mascota
* **POST** `http://localhost:8081/api/v1/mascotas` *(Bearer Token: CLIENTE o ADMIN)*
```json
{
  "nombre": "Bobby",
  "especie": "PERRO",
  "raza": "Labrador",
  "edad": 3
}
```
*(Especies permitidas: `PERRO`, `GATO`, `AVE`, `OTRO`).*

#### Listar Mascotas del Cliente
* **GET** `http://localhost:8081/api/v1/mascotas/mis-mascotas` *(Bearer Token: CLIENTE)*

---

### 3. Citas Médicas (`/api/v1/citas`)

#### Agendar Cita
* **POST** `http://localhost:8081/api/v1/citas` *(Bearer Token: CLIENTE o ADMIN)*
```json
{
  "mascotaId": 2,
  "veterinarioId": 2,
  "fechaHora": "2026-10-15 10:00:00",
  "motivo": "Revisión general y vacunación antirrábica"
}
```

#### Consultar Agenda del Veterinario
* **GET** `http://localhost:8081/api/v1/citas/agenda` *(Bearer Token: VET o ADMIN)*

#### Cancelar Cita
* **PATCH** `http://localhost:8081/api/v1/citas/{id}/cancelar` *(Bearer Token: CLIENTE o ADMIN)*

---

### 4. Expedientes Clínicos (`/api/v1/expedientes`)

#### Registrar Expediente (Atención Médica)
* **POST** `http://localhost:8081/api/v1/expedientes` *(Bearer Token: VET asignado o ADMIN)*
```json
{
  "citaId": 2,
  "diagnostico": "Paciente canino en óptimas condiciones de nutrición y salud",
  "tratamiento": "Aplicación de vacuna séxtuple y vitaminas vía oral",
  "pesoKg": 24.50
}
```
> Al registrar el expediente, la cita médica asociada cambia automáticamente de estado `PENDIENTE` a **`COMPLETADA`**.

#### Consultar Historial Médico de una Mascota
* **GET** `http://localhost:8081/api/v1/expedientes/mascota/2` *(Bearer Token: CLIENTE propietario, VET o ADMIN)*

---

## 🧠 Reglas de Negocio Implementadas

1. **Disponibilidad del Veterinario (Sin Traslapes):**
   * Cada cita tiene una duración estimada de **30 minutos**.
   * No se permite programar citas para un veterinario si se traslapan dentro del rango `[fechaHora - 30m, fechaHora + 30m]`.
2. **Límite Diario por Cliente:**
   * Un cliente tiene un límite de **máximo 2 citas en estado `PENDIENTE` para el mismo día**.
3. **Pertenencia de Mascota:**
   * Los clientes solo pueden agendar citas o consultar historiales de sus **propias mascotas**.
4. **Política de Cancelación:**
   * Solo se pueden cancelar citas en estado `PENDIENTE`.
   * La cancelación debe realizarse con **más de 2 horas de anticipación** a la hora programada.
5. **Cierre de Ciclo de Atención:**
   * Solo el veterinario asignado a la cita (o un `ADMIN`) puede registrar su expediente clínico.
   * La cita queda sellada como `COMPLETADA` y no puede ser modificada ni cancelada.

---

## 🧪 Pruebas Unitarias Automatizadas

El proyecto incluye tests unitarios con **Mockito** y **MockMvc** para verificar lógica de negocio y seguridad sin depender de la base de datos:

Para ejecutar todas las pruebas unitarias:
```powershell
mvn test -Dtest="ExpedienteClinicoControllerTest,ExpedienteClinicoServiceTest,CitaMedicaServiceTest,CitaMedicaControllerTest,MascotaControllerTest" -pl VetKI
```
