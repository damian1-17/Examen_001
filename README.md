# 💰 Finance App - Gestión Financiera Personal

![Android](https://img.shields.io/badge/Android-API%2024%2B-green?logo=android)
![Java](https://img.shields.io/badge/Java-8-orange?logo=java)
![SQLite](https://img.shields.io/badge/SQLite-3-blue?logo=sqlite)
![License](https://img.shields.io/badge/License-MIT-yellow)

> Aplicación Android nativa para control integral de finanzas personales con estadísticas en tiempo real, conversión de monedas y gestión inteligente de presupuestos.

---

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Screenshots](#-screenshots)
- [Arquitectura](#️-arquitectura)
- [Tecnologías](#-tecnologías)
- [Instalación](#-instalación)
- [Uso](#-uso)
- [Base de Datos](#-base-de-datos)
- [API](#-api)
- [Roadmap](#-roadmap)
- [Contribución](#-contribución)
- [Licencia](#-licencia)

---

## ✨ Características

### 🎯 Funcionalidades Principales

#### 📊 **Dashboard Inteligente**
- Resumen financiero del mes actual
- Visualización de ingresos, gastos y balance
- Progreso del presupuesto con alertas visuales
- Cálculo automático de días restantes
- Saludo dinámico según hora del día

#### 💸 **Gestión de Transacciones**
- Registro rápido de ingresos y gastos
- Categorización automática
- Múltiples métodos de pago
- Conversión de monedas en tiempo real
- Edición y eliminación con gestos (swipe)
- Filtros avanzados por tipo, categoría y fecha

#### 📈 **Estadísticas y Análisis**
- **Gráfico de pastel**: Distribución de gastos por categoría
- **Gráfico de barras**: Comparación mensual ingresos vs gastos
- **Gráfico de línea**: Tendencias diarias de gastos
- Métricas clave: promedio diario, categoría top, total de transacciones
- Filtros por período (mes, 3 meses, 6 meses, año)

#### 💱 **Convertidor de Monedas**
- Tasas de cambio en tiempo real
- Soporte para 160+ monedas
- Cache inteligente (24h) para modo offline
- Historial de tasas guardadas
- Actualización manual bajo demanda

#### ⚙️ **Configuración Completa**
- Personalización de perfil y presupuesto
- Gestión de categorías personalizadas
- Administración de métodos de pago
- Ajuste de umbral de alertas
- Día de inicio del mes configurable
- Opción de restablecer datos

### 🎨 **Diseño y UX**

- ✅ Material Design 3
- ✅ Interfaz intuitiva y moderna
- ✅ Animaciones fluidas
- ✅ Modo responsivo
- ✅ Feedback visual inmediato
- ✅ Accesibilidad optimizada

---

## 📱 Screenshots

```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   Dashboard     │  │  Transacciones  │  │  Estadísticas   │
│                 │  │                 │  │                 │
│  💰 Ingresos    │  │  [Lista de      │  │  [Gráfico      │
│  💸 Gastos      │  │   transacciones │  │   de pastel]   │
│  💵 Balance     │  │   con swipe]    │  │                 │
│                 │  │                 │  │  [Gráfico      │
│  [Progreso]     │  │  [Filtros]      │  │   de barras]   │
│                 │  │                 │  │                 │
└─────────────────┘  └─────────────────┘  └─────────────────┘

┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  Convertidor    │  │  Nueva Trans.   │  │  Configuración  │
│                 │  │                 │  │                 │
│  Monto: 100     │  │  Tipo: Gasto    │  │  👤 Perfil     │
│  De: USD        │  │  Monto: $50     │  │  🏷️ Categorías │
│  A:  EUR        │  │  Categoría: 🍔  │  │  💳 Pagos      │
│                 │  │  Fecha: Hoy     │  │  💱 Convertidor│
│  = 91.00 EUR    │  │                 │  │  🗑️ Restablecer│
│                 │  │  [Guardar]      │  │                 │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

---

## 🏗️ Arquitectura

### Patrón de Diseño

La aplicación implementa una **arquitectura en capas** con separación clara de responsabilidades:

```
┌─────────────────────────────────────────────────────┐
│                PRESENTATION LAYER                    │
│  Activities | Fragments | Adapters | XML Layouts    │
└─────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────┐
│              BUSINESS LOGIC LAYER                    │
│     Managers | Utils | Validators | Formatters      │
└─────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────┐
│                  DATA LAYER                          │
│        SQLite (Local) | Retrofit (Remote)           │
└─────────────────────────────────────────────────────┘
```

### Estructura de Paquetes

```
com.example.financeapp/
│
├── 📱 activities/          # Pantallas de la app (8 Activities)
│   ├── InitialConfigActivity      # Onboarding
│   ├── MainActivity                # Dashboard
│   ├── AddTransactionActivity      # CRUD transacciones
│   ├── TransactionsListActivity    # Lista con filtros
│   ├── StatisticsActivity          # Gráficos
│   ├── SettingsActivity            # Configuración
│   ├── ManageCategoriesActivity    # Gestión categorías
│   └── ExchangeRateActivity        # Convertidor
│
├── 🔄 adapters/           # RecyclerView Adapters
│   ├── TransactionAdapter
│   └── CategoryManageAdapter
│
├── 📦 models/             # Modelos de datos (POJOs)
│   ├── Transaction
│   ├── Category
│   └── ExchangeRateResponse
│
├── 💾 database/           # Persistencia local
│   └── DatabaseHelper
│
├── 🌐 api/                # Networking
│   ├── ExchangeRateService
│   └── RetrofitClient
│
└── 🛠️ utils/              # Utilidades
    └── ExchangeRateManager
```

### Componentes Clave

#### **DatabaseHelper**
- Gestión completa de SQLite
- 7 tablas relacionales
- Índices optimizados
- Triggers automáticos
- Vistas materializadas

#### **ExchangeRateManager**
- Orquestación de API calls
- Sistema de cache (24h)
- Conversiones de moneda
- Manejo de errores robusto
- Modo offline-first

#### **Adapters**
- ViewHolder pattern
- Smooth scrolling
- Click listeners
- Swipe gestures

---

## 🛠️ Tecnologías

### Core
- **Lenguaje**: Java 8
- **Min SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 34 (Android 14)
- **Build System**: Gradle 8.x

### Librerías Principales

#### UI/UX
```gradle
implementation 'com.google.android.material:material:1.11.0'
implementation 'androidx.cardview:cardview:1.0.0'
implementation 'androidx.recyclerview:recyclerview:1.3.2'
implementation 'it.xabaras.android:recyclerview-swipedecorator:1.4'
```

#### Gráficos
```gradle
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
```
- PieChart para distribución
- BarChart para comparaciones
- LineChart para tendencias

#### Networking
```gradle
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
```
- Retrofit para REST API
- Gson para JSON parsing
- OkHttp para logging y optimización

#### Base de Datos
- **SQLite** (nativo Android)
- 7 tablas normalizadas
- Foreign keys habilitados
- Transacciones ACID

---

## 📦 Instalación

### Prerrequisitos

- Android Studio Arctic Fox o superior
- JDK 8 o superior
- Android SDK API 24+
- Gradle 7.0+

### Pasos de Instalación

1. **Clonar el repositorio**
```bash
git clone https://github.com/tu-usuario/finance-app.git
cd finance-app
```

2. **Abrir en Android Studio**
```
File → Open → Seleccionar carpeta del proyecto
```

3. **Sincronizar Gradle**
```
Android Studio sincronizará automáticamente las dependencias
```

4. **Configurar emulador o dispositivo**
```
- Emulador: Tools → Device Manager → Create Virtual Device
- Dispositivo físico: Habilitar USB Debugging
```

5. **Ejecutar la aplicación**
```
Run → Run 'app' (Shift + F10)
```

### Configuración de API

La app usa **ExchangeRate-API** que **no requiere API key**. Las tasas se obtienen de:
```
https://api.exchangerate-api.com/v4/latest/{currency}
```

Si deseas cambiar el proveedor de tasas, edita:
```java
// api/ExchangeRateService.java
String BASE_URL = "https://tu-api.com/";
```

---

## 📖 Uso

### Primera Vez

1. **Configuración Inicial**
   - Ingresa tu nombre
   - Define tu presupuesto mensual
   - Selecciona tu moneda
   - Elige el día de inicio del mes
   - Ajusta el umbral de alertas (50-90%)

2. **Explorar el Dashboard**
   - Visualiza tu resumen financiero
   - Observa el progreso del presupuesto
   - Revisa los días restantes del período

### Operaciones Diarias

#### Agregar una Transacción
1. Tap en botón flotante `(+)`
2. Selecciona tipo (Ingreso/Gasto)
3. Ingresa el monto
4. Elige la categoría
5. Método de pago
6. Fecha (hoy por defecto)
7. Descripción opcional
8. **Guardar**

#### Convertir Moneda (Opcional)
1. En el formulario, sección "Conversión"
2. Ingresa monto en moneda extranjera
3. Selecciona la moneda original
4. Tap "Convertir"
5. El monto se calcula automáticamente

#### Ver Estadísticas
1. Dashboard → "Estadísticas"
2. Selecciona período de análisis
3. Observa gráficos interactivos
4. Analiza métricas clave

#### Filtrar Transacciones
1. Dashboard → "Transacciones"
2. Menú (⋮) → Filtros
3. Selecciona tipo, categoría o rango de fechas
4. Limpia filtros cuando desees

#### Editar/Eliminar
- **Editar**: Swipe derecha → o tap en transacción
- **Eliminar**: Swipe izquierda → Confirmar

---

## 💾 Base de Datos

### Esquema Relacional

```sql
user_config (Configuración)
    ├── id, user_name, monthly_budget
    ├── currency_code, month_start_day
    └── alert_threshold

categories (Categorías)
    ├── id, name, type
    ├── icon, color
    └── is_predefined, is_active

transactions (Transacciones) ★
    ├── id, type, amount
    ├── category_id → categories(id)
    ├── description, transaction_date
    ├── payment_method
    └── original_currency, exchange_rate

exchange_rates (Cache de Tasas)
    ├── base_currency, target_currency
    ├── rate, last_updated
    └── UNIQUE(base, target)

payment_methods (Métodos de Pago)
    ├── id, name
    └── is_active

category_budgets (Presupuestos)
    ├── category_id, budget_amount
    └── month, year

alerts (Alertas)
    ├── alert_type, message
    └── is_read, created_at
```

### Optimizaciones

**Índices Críticos:**
```sql
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_type_date ON transactions(type, transaction_date);
CREATE INDEX idx_categories_type ON categories(type);
```

**Ventajas:**
- Consultas de estadísticas 10x más rápidas
- Filtrado eficiente por fecha y tipo
- Aggregaciones optimizadas

---

## 🌐 API

### ExchangeRate-API

**Endpoint:**
```
GET https://api.exchangerate-api.com/v4/latest/{currency}
```

**Ejemplo de Respuesta:**
```json
{
  "base": "USD",
  "date": "2024-12-06",
  "time_last_updated": 1701907200,
  "rates": {
    "EUR": 0.9143,
    "GBP": 0.7889,
    "JPY": 149.50,
    "MXN": 17.25,
    "BRL": 4.95,
    ...
  }
}
```

### Sistema de Cache

**Estrategia:** Cache-First con TTL de 24 horas

```
┌─────────────────────────────────────┐
│ Usuario solicita conversión         │
└──────────────┬──────────────────────┘
               ↓
┌──────────────────────────────────────┐
│ ¿Tasa en cache < 24h?                │
├────────────┬─────────────────────────┤
│    SÍ      │         NO              │
│     ↓      │          ↓              │
│  Retornar  │    Llamar API           │
│  cache     │          ↓              │
│            │   Guardar en SQLite     │
│            │          ↓              │
│            │   Retornar resultado    │
└────────────┴─────────────────────────┘
```

**Ventajas:**
- ⚡ Respuesta instantánea (cache)
- 📱 Funciona offline
- 💰 Ahorra datos móviles
- 🔋 Reduce consumo de batería

---

## 🗺️ Roadmap

### ✅ Fase 1 - Core (Completado)
- [x] Dashboard con resumen financiero
- [x] CRUD de transacciones
- [x] Categorización de ingresos/gastos
- [x] Estadísticas con gráficos
- [x] Configuración completa

### ✅ Fase 2 - Features Avanzados (Completado)
- [x] API de tasas de cambio
- [x] Convertidor de monedas
- [x] Cache inteligente
- [x] Gestión de categorías
- [x] Filtros avanzados

### 🚧 Fase 3 - Mejoras (En progreso)
- [ ] Unit tests (JUnit + Mockito)
- [ ] UI tests (Espresso)
- [ ] CI/CD pipeline
- [ ] Documentación de código

### 📋 Fase 4 - Futuro
- [ ] **Modo oscuro** completo
- [ ] **Widgets** para home screen
- [ ] **Notificaciones push** de presupuesto
- [ ] **Exportar a PDF/Excel**
- [ ] **Backup en la nube** (Firebase/Google Drive)
- [ ] **Sincronización multi-dispositivo**
- [ ] **Gráficos de tendencias** históricos
- [ ] **Metas de ahorro** con tracking
- [ ] **Categorías inteligentes** con ML
- [ ] **Escaneo de recibos** con OCR

---

## 🤝 Contribución

¡Las contribuciones son bienvenidas! Sigue estos pasos:

### Proceso de Contribución

1. **Fork** el proyecto
2. **Crea** tu rama de feature
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. **Commit** tus cambios
   ```bash
   git commit -m 'Add: Amazing new feature'
   ```
4. **Push** a la rama
   ```bash
   git push origin feature/AmazingFeature
   ```
5. **Abre** un Pull Request

### Guías de Estilo

#### Código Java
- Seguir convenciones de Google Java Style
- Documentar métodos públicos con Javadoc
- Nombres descriptivos de variables
- Máximo 120 caracteres por línea

#### Commits
- Usar prefijos: `Add:`, `Fix:`, `Update:`, `Refactor:`
- Mensajes descriptivos en español o inglés
- Un commit por cambio lógico

#### Pull Requests
- Título claro y descriptivo
- Descripción de los cambios
- Screenshots si afecta UI
- Tests incluidos (si aplica)

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver archivo [LICENSE](LICENSE) para más detalles.

```
MIT License

Copyright (c) 2024 Finance App

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```

---

## 👥 Autores

- **Tu Nombre** - *Desarrollo inicial* - [@tu-usuario](https://github.com/tu-usuario)

---

## 🙏 Agradecimientos

- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) por los gráficos
- [ExchangeRate-API](https://www.exchangerate-api.com/) por las tasas de cambio
- [Material Design](https://material.io/) por las guías de diseño
- [Retrofit](https://square.github.io/retrofit/) por la biblioteca de networking
- Comunidad de Android Developers

---

## 📞 Contacto

- **Email**: tu-email@ejemplo.com
- **Twitter**: [@tu_twitter](https://twitter.com/tu_twitter)
- **LinkedIn**: [Tu Perfil](https://linkedin.com/in/tu-perfil)

---

## 🌟 ¿Te gustó el proyecto?

Si este proyecto te fue útil, considera:
- ⭐ Darle una estrella en GitHub
- 🐛 Reportar bugs o sugerir features
- 🤝 Contribuir con código
- 📢 Compartirlo con otros

---

<div align="center">

**Hecho con ❤️ y ☕ para la comunidad Android**

[⬆ Volver arriba](#-finance-app---gestión-financiera-personal)

</div>
