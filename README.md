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
| 🔍 **Eureka Server** | `ms-eureka` | 8761 | Service discovery y registro de microservicios |
| 🌐 **API Gateway** | `api-gateway` | 9093 | Punto de entrada único, enrutamiento y balanceo de carga |
| 👤 **Usuarios** | `ms-users` | 9090 | Gestión de cuentas y autenticación JWT |
| 🌱 **Productos** | `ms-product` | 9000 | Catálogo de plantas y productos |
| 🛒 **Carrito** | `ms-cart` | 9092 | Gestión del carrito de compras |
| 📋 **Órdenes** | `ms-order` | 9003 | Ciclo de vida de los pedidos |
| 💳 **Pagos** | `ms-payment` | 9094 | Procesamiento de pagos |
| 📦 **Inventario** | `ms-inventory` | 9002 | Control de stock y disponibilidad |
| 🚚 **Envíos** | `ms-shipping` | 9096 | Gestión de despacho y seguimiento |
| ⭐ **Reseñas** | `ms-review` | 9097 | Valoraciones y comentarios de productos |
| 🏷️ **Descuentos** | `ms-discount` | 9098 | Cupones y descuentos |
| 🔔 **Notificaciones** | `ms-notification` | 9099 | Envío de alertas y correos |

### Flujo de comunicación

```
Cliente (Postman / Frontend)
        ↓
   API Gateway :9093
        ↓
┌─────────────────────────────────────────────────────┐
│  ms-users  │  ms-product  │  ms-order  │    ...     │
│   :9090    │    :9000     │   :9003    │            │
└─────────────────────────────────────────────────────┘
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
- **JUnit 5 + Mockito** — Testing unitario
- **JaCoCo** — Cobertura de código

---

## 🌐 API Gateway

El API Gateway actúa como **punto de entrada único** para todos los microservicios. Todas las peticiones pasan por el gateway, que las enruta al microservicio correspondiente usando Eureka para resolución de nombres (`lb://`).

### Rutas configuradas

| Ruta en Gateway | Microservicio destino | Puerto directo |
|---|---|---|
| `/api/v1/auth/**` | ms-users | 9090 |
| `/api/v1/users/**` | ms-users | 9090 |
| `/api/v1/admin/users/**` | ms-users | 9090 |
| `/api/v1/products/**` | ms-product | 9000 |
| `/api/v1/inventory/**` | ms-inventory | 9002 |
| `/api/v1/cart/**` | ms-cart | 9092 |
| `/api/v1/discounts/**` | ms-discount | 9098 |
| `/api/v1/payments/**` | ms-payment | 9094 |
| `/api/v1/shipping/**` | ms-shipping | 9096 |
| `/api/v1/orders/**` | ms-order | 9003 |
| `/api/v1/reviews/**` | ms-review | 9097 |
| `/api/v1/notifications/**` | ms-notification | 9099 |

### Documentación Swagger por microservicio

Accede al Swagger de cada microservicio directamente por su puerto:

```
http://localhost:9090/swagger-ui/index.html   ← ms-users
http://localhost:9000/swagger-ui/index.html   ← ms-product
http://localhost:9002/swagger-ui/index.html   ← ms-inventory
http://localhost:9092/swagger-ui/index.html   ← ms-cart
http://localhost:9098/swagger-ui/index.html   ← ms-discount
http://localhost:9094/swagger-ui/index.html   ← ms-payment
http://localhost:9096/swagger-ui/index.html   ← ms-shipping
http://localhost:9003/swagger-ui/index.html   ← ms-order
http://localhost:9097/swagger-ui/index.html   ← ms-review
http://localhost:9099/swagger-ui/index.html   ← ms-notification
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
cd ms-eureka      ; .\mvnw.cmd clean package -DskipTests ; cd ..
cd api-gateway    ; .\mvnw.cmd clean package -DskipTests ; cd ..
cd ms-users       ; .\mvnw.cmd clean package -DskipTests ; cd ..
cd ms-product     ; .\mvnw.cmd clean package -DskipTests ; cd ..
cd ms-inventory   ; .\mvnw.cmd clean package -DskipTests ; cd ..
cd ms-cart        ; .\mvnw.cmd clean package -DskipTests ; cd ..
cd ms-discount    ; .\mvnw.cmd clean package -DskipTests ; cd ..
cd ms-payment     ; .\mvnw.cmd clean package -DskipTests ; cd ..
cd ms-shipping    ; .\mvnw.cmd clean package -DskipTests ; cd ..
cd ms-order       ; .\mvnw.cmd clean package -DskipTests ; cd ..
cd ms-review      ; .\mvnw.cmd clean package -DskipTests ; cd ..
cd ms-notification; .\mvnw.cmd clean package -DskipTests ; cd ..
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
| API Gateway | http://localhost:9093 |
| Swagger ms-users | http://localhost:9090/swagger-ui/index.html |
| Swagger ms-product | http://localhost:9000/swagger-ui/index.html |

---

## 🧪 Testing

El proyecto incluye tests unitarios usando **JUnit 5** y **Mockito**, con cobertura de código medida mediante **JaCoCo**.

### Tipos de tests

| Tipo | Anotación | Qué prueba |
|---|---|---|
| Test de servicio | `@ExtendWith(MockitoExtension.class)` | Lógica de negocio aislada con patrón Given-When-Then |
| Test de controlador | `@WebMvcTest` | Endpoints HTTP, códigos de respuesta, JSON |

### Patrón Given-When-Then

Los tests de servicio siguen el patrón Given-When-Then para mayor claridad:

```java
@Test
void shouldProcessPaymentSuccessfully() {
    // Given
    when(userClient.userExists(username)).thenReturn(true);
    when(orderClient.getOrderById(2L)).thenReturn(validOrder);

    // When
    PaymentResponseDTO result = paymentService.processPayment(username, validRequest);

    // Then
    assertNotNull(result);
    assertEquals(PaymentStatus.COMPLETED, result.getStatus());
}
```

### Cobertura con JaCoCo

JaCoCo está configurado en todos los microservicios de negocio. Para generar el reporte:

```bash
.\mvnw.cmd test
```

El reporte HTML se genera en `target/site/jacoco/index.html`.

### Ejecutar los tests

Desde la carpeta de cada microservicio:

```bash
# Ejecutar todos los tests
.\mvnw.cmd test

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
   cd ms-eureka
   mvn spring-boot:run
   ```

3. **Inicia el API Gateway:**

   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```

4. **Levanta cada microservicio** en terminales separadas:

   ```bash
   cd ms-users       && mvn spring-boot:run
   cd ms-product     && mvn spring-boot:run
   cd ms-inventory   && mvn spring-boot:run
   cd ms-cart        && mvn spring-boot:run
   cd ms-discount    && mvn spring-boot:run
   cd ms-payment     && mvn spring-boot:run
   cd ms-shipping    && mvn spring-boot:run
   cd ms-order       && mvn spring-boot:run
   cd ms-review      && mvn spring-boot:run
   cd ms-notification && mvn spring-boot:run
   ```

5. **Verifica en Eureka** que todos los servicios estén registrados:

   ```
   http://localhost:8761
   ```

---

## 📁 Estructura del repositorio

```
SpringPlant/
├── ms-eureka/          # Service discovery
├── api-gateway/        # API Gateway y enrutamiento
├── ms-users/           # Microservicio de usuarios y autenticación
├── ms-product/         # Microservicio de productos
├── ms-inventory/       # Microservicio de inventario
├── ms-cart/            # Microservicio de carrito
├── ms-discount/        # Microservicio de descuentos
├── ms-payment/         # Microservicio de pagos
├── ms-shipping/        # Microservicio de envíos
├── ms-order/           # Microservicio de órdenes
├── ms-review/          # Microservicio de reseñas
├── ms-notification/    # Microservicio de notificaciones
└── docker-compose.yml  # Orquestación de todos los servicios
```

---

## 👩‍💻 Autoras

Desarrollado por [Valeria Rodriguez](https://github.com/ValeRod-dc) y [Catalina Campos](https://github.com/miucat05) como proyecto semestral de Desarrollo Full-Stack.