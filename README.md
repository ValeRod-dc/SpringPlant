# 🌿 SpringPlant
> Plataforma de e-commerce para plantas construida con arquitectura de microservicios en Spring Boot.

Proyecto de Desarrollo Full-Stack — Caso Semestral

---

## 📋 Descripción

**SpringPlant** es una aplicación de comercio electrónico orientada a la venta de plantas, desarrollada como caso semestral de desarrollo full-stack. El sistema implementa una **arquitectura de microservicios** usando el ecosistema de Spring, donde cada dominio del negocio es gestionado de forma independiente y se comunica a través de un registro centralizado con Eureka.

---

## 🏗️ Arquitectura

El proyecto está compuesto por los siguientes microservicios:

| Microservicio | Carpeta | Puerto | Responsabilidad |
|---|---|---|---|
| 🔍 **Eureka Server** | `ms_eureka` | 8761 | Service discovery y registro de microservicios |
| 🌐 **API Gateway** | `api-gateway` | 9093 | Punto de entrada único, enrutamiento y seguridad |
| 👤 **Usuarios** | `ms_users` | 9090 | Gestión de cuentas y autenticación |
| 🌱 **Productos** | `product` | 9000 | Catálogo de plantas y productos |
| 🛒 **Carrito** | `ms_cart` | 9092 | Gestión del carrito de compras |
| 📋 **Órdenes** | `order` | 9001 | Ciclo de vida de los pedidos |
| 💳 **Pagos** | `ms_payment` | 9094 | Procesamiento de pagos |
| 📦 **Inventario** | `inventory` | 9002 | Control de stock y disponibilidad |
| 🚚 **Envíos** | `ms_shipping` | 9096 | Gestión de despacho y seguimiento |
| ⭐ **Reseñas** | `review` | 9097 | Valoraciones y comentarios de productos |
| 🏷️ **Descuentos** | `ms_discount` | 9098 | Cupones y descuentos |
| 🔔 **Notificaciones** | `notification` | 9099 | Envío de alertas y correos |

### Flujo de comunicación

```
Cliente (Postman / Frontend)
        ↓
   API Gateway :9093
        ↓
┌──────────────────────────────────────────┐
│  ms_users  │  product  │  order  │  ...  │
│   :9090    │   :9000   │  :9001  │       │
└──────────────────────────────────────────┘
        ↑
  Eureka Server :8761
  (registro y descubrimiento)
```

---

## 🛠️ Tecnologías

- **Java 21** — Lenguaje principal
- **Spring Boot** — Framework base de cada microservicio
- **Spring Cloud Netflix Eureka** — Service discovery
- **Spring Cloud Gateway** — API Gateway y enrutamiento
- **Spring Security + JWT** — Autenticación y autorización
- **Spring Data JPA** — Persistencia de datos
- **MySQL (XAMPP)** — Base de datos relacional
- **Maven** — Gestión de dependencias
- **Docker & Docker Compose** — Contenedorización y orquestación
- **springdoc-openapi** — Documentación automática de APIs (Swagger UI)
- **JUnit 5 + Mockito** — Testing unitario e integración

---

## 🌐 API Gateway

El API Gateway actúa como **punto de entrada único** para todos los microservicios. Todas las peticiones del cliente pasan primero por el gateway, que se encarga de enrutarlas al microservicio correspondiente.

### Endpoints principales

| Ruta en Gateway | Microservicio destino |
|---|---|
| `/api/users/**` | ms_users |
| `/api/products/**` | product |
| `/api/inventory/**` | inventory |
| `/api/cart/**` | ms_cart |
| `/api/discounts/**` | ms_discount |
| `/api/payments/**` | ms_payment |
| `/api/shipping/**` | ms_shipping |
| `/api/orders/**` | order |
| `/api/reviews/**` | review |
| `/api/notifications/**` | notification |

### Documentación Swagger centralizada

Con el gateway corriendo, puedes ver la documentación de todos los microservicios desde una sola URL:

```
http://localhost:9093/swagger-ui/index.html
```

Usa el dropdown en la esquina superior derecha para cambiar entre microservicios.

También puedes acceder al Swagger de cada microservicio individualmente:

```
http://localhost:9090/swagger-ui/index.html   ← ms_users
http://localhost:9000/swagger-ui/index.html   ← product
http://localhost:9001/swagger-ui/index.html   ← order
```

---

## 🐳 Ejecución con Docker

### Prerrequisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado
- XAMPP corriendo con MySQL activo
- WSL instalado (Windows): `wsl --install` desde PowerShell como administrador

### Pasos

**1. Compila cada microservicio** (con XAMPP encendido):

```powershell
cd ms_eureka   && .\mvnw.cmd clean package -DskipTests
cd api-gateway && .\mvnw.cmd clean package -DskipTests
cd ms_users    && .\mvnw.cmd clean package -DskipTests
cd product     && .\mvnw.cmd clean package -DskipTests
# ... repetir para cada microservicio
```

> ⚠️ Si el comando falla con error de borrado, detén IntelliJ y pausa OneDrive antes de compilar.

**2. Construye las imágenes Docker:**

```bash
docker compose build
```

**3. Levanta todos los servicios:**

```bash
docker compose up -d
```

**4. Verifica que todos estén corriendo:**

```bash
docker ps
```

**5. Detener todos los servicios:**

```bash
docker compose down
```

### Perfiles de configuración

Cada microservicio tiene dos archivos de configuración:

| Archivo | Uso |
|---|---|
| `application.properties` | Ejecución local desde IntelliJ |
| `application-docker.properties` | Ejecución dentro de Docker |

Cuando se ejecuta con Docker Compose, se activa automáticamente el perfil `docker` mediante la variable de entorno `SPRING_PROFILES_ACTIVE: docker`.

### Acceso a los servicios

| Servicio | URL |
|---|---|
| Eureka Dashboard | http://localhost:8761 |
| API Gateway / Swagger | http://localhost:9090/swagger-ui/index.html |

---

## 🧪 Testing

El proyecto incluye tests unitarios y de integración usando **JUnit 5** y **Mockito**.

### Tipos de tests

| Tipo | Anotación | Qué prueba |
|---|---|---|
| Test de servicio | `@ExtendWith(MockitoExtension.class)` | Lógica de negocio aislada |
| Test de controlador | `@WebMvcTest` | Endpoints HTTP, códigos de respuesta, JSON |

### Ejecutar los tests

Desde la carpeta de cada microservicio:

```bash
# Ejecutar todos los tests
.\mvnw.cmd test

# Ejecutar un test específico
.\mvnw.cmd test -Dtest=PacienteServiceTest

# Compilar sin ejecutar tests
.\mvnw.cmd clean package -DskipTests
```



---

## 🚀 Ejecución local (sin Docker)

### Prerrequisitos

- Java 21 o superior
- Maven 3.8+
- XAMPP con MySQL activo

### Pasos

1. **Clona el repositorio:**

   ```bash
   git clone https://github.com/ValeRod-dc/SpringPlant.git
   cd SpringPlant
   ```

2. **Inicia el Eureka Server primero:**

   ```bash
   cd ms_eureka
   mvn spring-boot:run
   ```

3. **Inicia el API Gateway:**

   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```

4. **Levanta cada microservicio** en terminales separadas:

   ```bash
   cd ms_users    && mvn spring-boot:run
   cd product     && mvn spring-boot:run
   cd inventory   && mvn spring-boot:run
   cd ms_cart     && mvn spring-boot:run
   cd ms_discount && mvn spring-boot:run
   cd ms_payment  && mvn spring-boot:run
   cd ms_shipping && mvn spring-boot:run
   cd order       && mvn spring-boot:run
   cd review      && mvn spring-boot:run
   cd notification && mvn spring-boot:run
   ```

5. **Verifica en Eureka** que todos los servicios estén registrados:

   ```
   http://localhost:8761
   ```

---

## 📁 Estructura del repositorio

```
SpringPlant/
├── ms_eureka/          # Service discovery
├── api-gateway/        # API Gateway y enrutamiento
├── ms_users/           # Microservicio de usuarios
├── product/            # Microservicio de productos
├── inventory/          # Microservicio de inventario
├── ms_cart/            # Microservicio de carrito
├── ms_discount/        # Microservicio de descuentos
├── ms_payment/         # Microservicio de pagos
├── ms_shipping/        # Microservicio de envíos
├── order/              # Microservicio de órdenes
├── review/             # Microservicio de reseñas
├── notification/       # Microservicio de notificaciones
└── docker-compose.yml  # Orquestación de todos los servicios
```

---

## 👩‍💻 Autoras

Desarrollado por [ValeRod-dc](https://github.com/ValeRod-dc) y Cata como proyecto semestral de desarrollo full-stack.
