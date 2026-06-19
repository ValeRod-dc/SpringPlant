# 🌿 SpringPlant

> Plataforma de e-commerce para plantas construida con arquitectura de microservicios en Spring Boot.

Proyecto de Desarrollo Full-Stack — Caso Semestral

---

## 📋 Descripción

**SpringPlant** es una aplicación de comercio electrónico orientada a la venta de plantas, desarrollada como caso semestral de desarrollo full-stack. El sistema implementa una **arquitectura de microservicios** usando el ecosistema de Spring, donde cada dominio del negocio es gestionado de forma independiente y se comunica a través de un registro centralizado con Eureka.

---

## 🏗️ Arquitectura

El proyecto está compuesto por los siguientes microservicios:

| Microservicio | Carpeta | Responsabilidad |
|---|---|---|
| 🔍 **Eureka Server** | `ms_eureka` | Service discovery y registro de microservicios |
| 👤 **Usuarios** | `ms_users` | Gestión de cuentas y autenticación |
| 🌱 **Productos** | `product` | Catálogo de plantas y productos |
| 📦 **Inventario** | `inventory` | Control de stock y disponibilidad |
| 🛒 **Carrito** | `ms_cart` | Gestión del carrito de compras |
| 🏷️ **Descuentos** | `ms_discount` | Cupones y descuentos |
| 💳 **Pagos** | `ms_payment` | Procesamiento de pagos |
| 🚚 **Envíos** | `ms_shipping` | Gestión de despacho y seguimiento |
| 📋 **Órdenes** | `order` | Ciclo de vida de los pedidos |
| ⭐ **Reseñas** | `review` | Valoraciones y comentarios de productos |
| 🔔 **Notificaciones** | `notification` | Envío de alertas y correos |

---

## 🛠️ Tecnologías

- **Java** — Lenguaje principal (100%)
- **Spring Boot** — Framework base de cada microservicio
- **Spring Cloud Netflix Eureka** — Service discovery
- **Spring Cloud** — Comunicación entre microservicios
- **Maven** — Gestión de dependencias

---

## 🚀 Cómo ejecutar el proyecto

### Prerrequisitos

- Java 17 o superior
- Maven 3.8+

### Pasos

1. **Clona el repositorio:**
   ```bash
   git clone https://github.com/ValeRod-dc/SpringPlant.git
   cd SpringPlant
   ```

2. **Inicia el Eureka Server primero** (es necesario para el registro de los demás servicios):
   ```bash
   cd ms_eureka
   mvn spring-boot:run
   ```

3. **Levanta cada microservicio** en terminales separadas:
   ```bash
   cd ms_users && mvn spring-boot:run
   cd product && mvn spring-boot:run
   cd inventory && mvn spring-boot:run
   cd ms_cart && mvn spring-boot:run
   cd ms_discount && mvn spring-boot:run
   cd ms_payment && mvn spring-boot:run
   cd ms_shipping && mvn spring-boot:run
   cd order && mvn spring-boot:run
   cd review && mvn spring-boot:run
   cd notification && mvn spring-boot:run
   ```

4. **Accede al dashboard de Eureka** para verificar que todos los servicios estén registrados:
   ```
   http://localhost:8761
   ```

---

## 📁 Estructura del repositorio

```
SpringPlant/
├── ms_eureka/        # Service discovery
├── ms_users/         # Microservicio de usuarios
├── product/          # Microservicio de productos
├── inventory/        # Microservicio de inventario
├── ms_cart/          # Microservicio de carrito
├── ms_discount/      # Microservicio de descuentos
├── ms_payment/       # Microservicio de pagos
├── ms_shipping/      # Microservicio de envíos
├── order/            # Microservicio de órdenes
├── review/           # Microservicio de reseñas
└── notification/     # Microservicio de notificaciones
```

---

## 👩‍💻 Autora

Desarrollado por [ValeRod-dc](https://github.com/ValeRod-dc) como proyecto semestral de desarrollo full-stack.
