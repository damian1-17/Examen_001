# Examen_001 - Aplicación de Finanzas Personales

Esta es una aplicación para Android diseñada para ayudar a los usuarios a administrar sus finanzas personales de manera sencilla y efectiva. Permite realizar un seguimiento de los ingresos y gastos, establecer un presupuesto mensual y visualizar la actividad financiera para tomar mejores decisiones económicas.

## ✨ Características Principales

*   **Configuración Inicial**: Permite al usuario configurar su nombre, presupuesto mensual, moneda preferida y el día de inicio de su período mensual.
*   **Dashboard Principal**:
    *   Muestra un saludo personalizado y un resumen del período actual.
    *   Visualización clara de **Ingresos**, **Gastos** y **Balance** del mes.
    *   Seguimiento del **presupuesto mensual** con una barra de progreso y porcentaje de uso.
    *   Cálculo y visualización de los **días restantes** del período.
    *   **Alerta visual** cuando los gastos superan un umbral predefinido (ej. 80% del presupuesto).
*   **Gestión de Transacciones**:
    *   Añadir nuevas transacciones (ingresos o gastos) a través de un botón flotante.
    *   Edición de transacciones existentes.
    *   Clasificación por categorías (Alimentación, Transporte, Salario, etc.).
    *   Registro del método de pago.
    *   Listado histórico de todas las transacciones.
*   **Conversión de Divisas**:
    *   Herramienta integrada para registrar gastos en monedas extranjeras.
    *   Conversión automática a la moneda principal del usuario utilizando tasas de cambio actualizadas.
*   **Navegación Intuitiva**: Acceso rápido a las secciones de Transacciones, Estadísticas y Configuración desde el dashboard.
*   **Soporte Multi-moneda**: Formatea las cantidades monetarias según la divisa seleccionada por el usuario.
*   **Persistencia de Datos**: Utiliza una base de datos local SQLite para almacenar toda la información de forma segura en el dispositivo.

## 🛠️ Tecnologías y Librerías

*   **Lenguaje**: Java
*   **Arquitectura**: AppCompat, Activity-based.
*   **Base de Datos**: SQLite con `DatabaseHelper` personalizado para gestión de tablas y actualizaciones.
*   **UI Components**:
    *   `Material Components for Android` para un diseño moderno (Cards, FloatingActionButton, TextInputs, etc.).
    *   `ConstraintLayout` para layouts responsivos.
    *   `CardView` y `RecyclerView` para listas y contenedores.
    *   `Toolbar` personalizada con soporte de navegación.
*   **Red y APIs**:
    *   `Retrofit` para consumo de APIs REST (tasas de cambio).
    *   `Gson` para el parseo de respuestas JSON.
*   **Gráficos**: [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) para visualización de estadísticas.

## 🚀 Cómo Empezar

1.  Clona este repositorio:
    ```bash
    git clone https://github.com/tu-usuario/Examen_001.git
    ```
2.  Abre el proyecto en Android Studio.
3.  Sincroniza las dependencias de Gradle.
4.  Ejecuta la aplicación en un emulador o en un dispositivo físico.

## 📋 Estructura de la Base de Datos

La aplicación utiliza varias tablas relacionales en SQLite:

1.  **`user_config`**: Configuración global del usuario.
2.  **`transactions`**: Registro histórico de ingresos y gastos.
3.  **`categories`**: Categorías predefinidas para clasificar transacciones.
4.  **`payment_methods`**: Métodos de pago disponibles.

## 📱 Flujo de Usuario

1.  **Inicio**: Si es la primera vez, el usuario completa la `InitialConfigActivity`.
2.  **Dashboard**: Acceso a `MainActivity` donde se ve el resumen financiero.
3.  **Operaciones**:
    -   Agregar/Editar transacción (`AddTransactionActivity`).
    -   Ver historial (`TransactionsListActivity`).
    -   Ver gráficos (`StatisticsActivity`).
    -   Ajustar preferencias (`SettingsActivity`).

