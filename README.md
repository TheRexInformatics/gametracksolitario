# GameTrack Solitario

📱 GameTrack - Gestor de Colección de Videojuegos
GameTrack es una aplicación móvil completa desarrollada en Kotlin con Jetpack Compose que permite a los usuarios gestionar su colección personal de videojuegos. La aplicación se conecta a un backend propio desarrollado con Spring Boot y MySQL, e integra una API externa para obtener automáticamente imágenes de juegos.


👥 Integrantes del Equipo
[Martín Villarroel]

🎯 Funcionalidades Principales
🔐 Autenticación y Usuarios
Registro de usuarios (username, email, password)

Inicio de sesión con validación local y remota

Recuperación de contraseña con token de verificación

Perfil de usuario con foto personalizable

Cierre de sesión seguro

🎮 Gestión de Colección de Juegos
Añadir juegos (título, plataforma, estado, calificación, notas)

Búsqueda automática de imágenes mediante API de IGDB

Listado de juegos organizados por usuario

Edición y eliminación de juegos

Filtrado y búsqueda en la colección

📊 Estadísticas Personalizadas
Conteo total de juegos

Juegos completados vs en progreso

Calificación promedio

Plataforma favorita

🌐 Integraciones
Backend propio con microservicios de autenticación

API externa (IGDB) para imágenes de juegos

Almacenamiento local con Room Database

🛠️ Tecnologías y Arquitectura
Frontend (Android App)
Lenguaje: Kotlin

UI Framework: Jetpack Compose

Arquitectura: MVVM (Model-View-ViewModel)

Persistencia local: Room Database

Networking: Retrofit 2 + OkHttp3

Inyección de dependencias: ViewModel Factory

Imágenes: Coil Compose

Navegación: Navigation Compose

Manejo de estado: StateFlow / MutableStateFlow

Backend (Microservicio)
Framework: Spring Boot 4.0.0

Base de datos: MySQL

Persistencia: Spring Data JPA

Seguridad: BCrypt Password Encoding

API REST: Controladores con endpoints JSON

CORS: Configurado para acceso cross-origin

Herramientas de Desarrollo
Control de versiones: GitHub

Gestión de proyectos: Trello

IDE: Android Studio + IntelliJ IDEA

Base de datos: MySQL (Laragon)

API Testing: Postman / Insomnia

🔗 Endpoints y APIs
Backend Propio (Spring Boot)
URL Base: http://10.116.67.176:8080/api/

Método	Endpoint	Descripción
POST	/auth/register	Registro de nuevo usuario
POST	/auth/login	Inicio de sesión
POST	/auth/forgot-password	Solicitar recuperación de contraseña
POST	/auth/reset-password	Restablecer contraseña con token
GET	/auth/check-username/{username}	Verificar disponibilidad de username
GET	/auth/test	Prueba de conexión al servicio

API Externa Consumida
IGDB (Internet Game Database)

Propósito: Obtener imágenes de carátulas de juegos automáticamente

Método de consumo: Retrofit con headers de autenticación

Integración: Búsqueda automática al añadir juegos

📱 Recursos Nativos del Dispositivo
Cámara

Uso: Tomar fotos para perfil de usuario

Permiso: Manifest.permission.CAMERA

Integración: FileProvider para almacenamiento temporal

Galería de Fotos

Uso: Seleccionar imagen de perfil desde galería

Integración: Activity Result API

Almacenamiento Local

Uso: Persistencia de datos de usuario y juegos

Tecnología: Room Database + SharedPreferences

Vibrador

Uso: Retroalimentación háptica en errores de login

Permiso: android.permission.VIBRATE

🏗️ Estructura del Proyecto
Frontend (Android)

com.example.gametrack/
├── data/
│   ├── remote/
│   │   ├── api/           # Clientes API (Retrofit)
│   │   └── models/        # Modelos de datos API
│   ├── Game.kt           # Entidad Room
│   ├── User.kt           # Entidad Room
│   ├── GameDao.kt        # Data Access Object
│   ├── UserDao.kt
│   ├── GameDatabase.kt   # Base de datos Room
│   ├── GameRepository.kt # Repositorio
│   └── UserRepository.kt # Repositorio con lógica de autenticación
├── screens/
│   ├── LoginScreen.kt
│   ├── SignUpScreen.kt
│   ├── ForgotPasswordScreen.kt
│   ├── ResetPasswordScreen.kt
│   ├── HomeScreen.kt
│   ├── AddGameScreen.kt
│   ├── ProfileScreen.kt
│   └── ApiGame.kt        # Cliente IGDB
├── navigation/
│   └── NavGraph.kt       # Navegación Compose
├── ui/theme/
│   ├── Color.kt
│   ├── Type.kt
│   ├── Shape.kt
│   └── Theme.kt
├── GameViewModel.kt      # ViewModel principal
├── GameViewModelFactory.kt
└── MainActivity.kt

Backend (Spring Boot)

com.gametrack.auth/
├── controller/
│   └── AuthController.java    # Controlador REST
├── model/
│   ├── User.java              # Entidad JPA
│   └── PasswordResetToken.java
├── repository/
│   ├── UserRepository.java    # Spring Data JPA
│   └── PasswordResetTokenRepository.java
└── AuthServiceApplication.java

🚀 Instrucciones de Ejecución
Requisitos Previos
Android Studio Flamingo o superior

JDK 17

Dispositivo Android o emulador (API 24+)

MySQL Server (Laragon recomendado)

Spring Boot 4.0.0

🧪 Pruebas Unitarias
Cobertura de Pruebas
ViewModel: Pruebas de lógica de autenticación

Repository: Pruebas de acceso a datos

API Clients: Pruebas de conexión y parsing

🔒 Seguridad Implementada
Autenticación
Hash de contraseñas con BCrypt (tanto frontend como backend)

Tokens de recuperación de contraseña con expiración

Validación de entrada en ambos extremos

Protección de Datos
No almacenamiento de contraseñas en texto plano

Permisos de recursos nativos solicitados en tiempo de ejecución

Validación de inputs en formularios

Comunicación Segura
Headers HTTP estándar (Content-Type, Accept)

Manejo de errores y timeouts

Logging detallado para debug (solo en modo desarrollo)

🎨 Diseño y UX
Tema Visual
Paleta de colores: Verde neón sobre fondo oscuro

Tipografía: Material Design 3 Typography

Formas: Esquinas redondeadas consistentes

Componentes Reutilizables
GameCardListStyle: Tarjeta de juego uniforme

StatCard: Tarjetas de estadísticas

Diálogos y alertas personalizados

Animaciones y Transiciones
Transiciones suaves entre pantallas

Indicadores de carga visuales

Animaciones en botones y tarjetas

Diálogos modales con animaciones

📊 Evidencia de Trabajo Colaborativo
GitHub
Commits por integrante: [Incluir estadísticas]

Branches: main, develop, feature/*

Pull Requests: Revisión de código entre pares

Issues: Seguimiento de tareas y bugs

Trello
Tablero: [URL del tablero Trello]

Listas: To Do, In Progress, Testing, Done

Tarjetas: Asignadas por integrante con fechas límite

⚠️ Solución de Problemas Comunes
La app no se conecta al backend
Verificar que el servidor Spring Boot esté ejecutándose

Confirmar IP en ApiClient.kt

Verificar firewall/antivirus no bloqueando puerto 8080

Error en login/registro
Verificar conexión a internet

Confirmar credenciales de MySQL

Revisar logs de Spring Boot para errores

Imágenes no se cargan
Verificar conexión a internet

Confirmar API key de IGDB (si se usa)

Verificar URLs en ApiGame.kt

APK no instala
Verificar que el APK esté correctamente firmado

Habilitar "Orígenes desconocidos" en dispositivo

Verificar compatibilidad de versión Android

📈 Mejoras Futuras
Planeado para Próximas Versiones
Sistema de amigos y compartir colecciones

Sincronización en tiempo real entre dispositivos

Modo offline completo

Exportar colección a CSV/PDF

Widgets de pantalla principal

Notificaciones de logros

Integración con más APIs de juegos

📄 Licencia
Este proyecto es desarrollado con fines educativos para la asignatura DSY1105 - Desarrollo de Aplicaciones Móviles de DUOC UC.

© 2025 GameTrack Team. Todos los derechos reservados.

📞 Contacto y Soporte
Repositorio: [GitHub URL]

Correo del Equipo: [email@example.com]

Docente: [Nombre del docente]

Este README fue generado el 12 de diciembre de 2025 y corresponde a la versión 1.0 de GameTrack.

📋 Checklist de Entrega
Código fuente completo en GitHub

APK firmado incluido en releases

Microservicios funcionales con base de datos

API externa integrada (IGDB)

Pruebas unitarias implementadas

Recursos nativos (Cámara + Galería)

4 roles de usuario diferenciados

README completo con documentación

Evidencia colaborativa (commits, Trello)

Formulario individual completado en AVA

⭐ Si este proyecto te gustó, ¡no olvides darle una estrella en GitHub!
Recursos nativos del dispositivo

