# GameTrack Solitario

# GameTrack - Gestor de Colección de Videojuegos
GameTrack es una aplicación móvil completa desarrollada en Kotlin con Jetpack Compose que permite a los usuarios gestionar su colección personal de videojuegos. La aplicación se conecta a un backend propio desarrollado con Spring Boot y MySQL, e integra una API externa para obtener automáticamente imágenes de juegos.

# Integrantes del Equipo
[Martín Villarroel]

# Funcionalidades Implementadas

# Autenticación y Usuarios

1.-Registro de usuarios (username, email, password) con validaciones
2.-Inicio de sesión con sistema híbrido (local y remoto)
3.-Recuperación de contraseña con token de 6 dígitos
4.-Restablecimiento de contraseña con validaciones
5.-Perfil de usuario con foto personalizable (cámara y galería)
6.-Cierre de sesión

# Gestión de Colección de Juegos

1.-Añadir juegos con campos: título, plataforma, estado, calificación, notas, horas jugadas
2.-Búsqueda automática de imágenes mediante API de IGDB
3.-Listado de juegos organizado por usuario
4.-Eliminación de juegos con confirmación
5.-Visualización de imágenes en pantalla completa
6.-Estadísticas básicas en perfil

# Características Técnicas

1.-Persistencia local con Room Database
2.-Conexión a backend propio con Spring Boot
3.-Consumo de API externa (IGDB)
4.-Acceso a recursos nativos: Cámara y Galería
5.-Validaciones de formularios en tiempo real
6.-Navegación fluida entre 7 pantallas

# Tecnologías Utilizadas

# Frontend (Android App)
Lenguaje: Kotlin
UI: Jetpack Compose
Arquitectura: MVVM
Base de datos local: Room
Networking: Retrofit 2 + OkHttp3
Manejo de imágenes: Coil Compose
Navegación: Navigation Compose
Cifrado: BCrypt para passwords
Corrutinas: Para operaciones asíncronas

# Backend (Spring Boot)
Framework: Spring Boot 4.0.0
Base de datos: MySQL
ORM: Spring Data JPA
Seguridad: BCrypt Password Encoding
API: RESTful endpoints

# Herramientas
Control de versiones: GitHub
IDE: Android Studio
Base de datos: MySQL (Laragon)
Testing: Pruebas manuales

# Endpoints Utilizados

# Backend Propio (Spring Boot)
URL Base: http://10.116.67.176:8080/api/auth/

Método	Endpoint	Descripción	Implementado
POST	/register	Registro de usuario	
POST	/login	Inicio de sesión	
POST	/forgot-password	Solicitar token	
POST	/reset-password	Restablecer contraseña	
GET	  /check-username/{username}	Verificar username	
GET	  /test	Prueba de conexión	

# API Externa Consumida
IGDB (Internet Game Database)
Endpoint: https://api.igdb.com/v4/games
Propósito: Obtener imágenes de carátulas de juegos
Método: POST con autenticación Bearer token
Integración: En AddGameScreen.kt y ApiGame.kt

#  Recursos Nativos Implementados

# 1. Cámara
Archivo: ProfileScreen.kt
Uso: Tomar foto para perfil de usuario
Permiso: Manifest.permission.CAMERA
Código clave: createImageFileUri(), cameraLauncher
Estado:  Completamente funcional

# 2. Galería de Fotos
Archivo: ProfileScreen.kt
Uso: Seleccionar imagen desde galería
Implementación: ActivityResultContracts.GetContent()
Código clave: galleryLauncher
Estado:  Completamente funcional

# 3. Almacenamiento Local
Tecnología: Room Database
Entidades: User, Game
Archivos: GameDatabase.kt, UserDao.kt, GameDao.kt
Estado:  Persistencia completa

# 4. Vibrador
Archivo: LoginScreen.kt
Uso: Retroalimentación háptica en errores
Código: vibrateError() función
Estado:  Implementado

#  Estructura del Proyecto

Frontend Android - Carpetas Principales
app/src/main/java/com/example/gametrack/
├── data/
│   ├── remote/
│   │   ├── api/
│   │   │   ├── ApiClient.kt          # Configuración Retrofit
│   │   │   └── AuthApiService.kt     # Interface endpoints
│   │   └── models/                   # 10 data classes para API
│   ├── Game.kt                       # Entidad Room
│   ├── User.kt                       # Entidad Room
│   ├── GameDao.kt                    # Operaciones juegos
│   ├── UserDao.kt                    # Operaciones usuarios
│   ├── GameDatabase.kt               # Database Room v2
│   ├── GameRepository.kt             # Repositorio juegos
│   └── UserRepository.kt             # Repositorio usuarios (674 líneas)
├── screens/
│   ├── LoginScreen.kt                # Pantalla login
│   ├── SignUpScreen.kt               # Pantalla registro
│   ├── ForgotPasswordScreen.kt       # Recuperar contraseña
│   ├── ResetPasswordScreen.kt        # Restablecer contraseña
│   ├── HomeScreen.kt                 # Lista juegos
│   ├── AddGameScreen.kt              # Añadir juego
│   ├── ProfileScreen.kt              # Perfil usuario
│   └── ApiGame.kt                    # Cliente IGDB (217 líneas)
├── navigation/
│   └── NavGraph.kt                   # Navegación (7 destinos)
├── ui/theme/
│   ├── Color.kt                      # Paleta colores
│   ├── Type.kt                       # Tipografía
│   ├── Shape.kt                      # Formas
│   └── Theme.kt                      # Tema principal
├── GameViewModel.kt                  # ViewModel principal
├── GameViewModelFactory.kt           # Factory ViewModel
└── MainActivity.kt                   # Actividad principal



Backend Spring Boot - Estructura
src/main/java/com/gametrack/auth/
├── controller/
│   └── AuthController.java           # Controlador REST (400+ líneas)
├── model/
│   ├── User.java                     # Entidad JPA User
│   └── PasswordResetToken.java       # Entidad tokens reset
├── repository/
│   ├── UserRepository.java           # Repository usuarios
│   └── PasswordResetTokenRepository.java
└── AuthServiceApplication.java       # Clase main



# Instrucciones para Ejecutar el Proyecto
Prerrequisitos

Android Studio (versión reciente)
JDK 17
Dispositivo Android API 24+ o emulador
MySQL Server ejecutándose
Conexión a internet para API IGDB


# Paso 1: Configurar y Ejecutar el Backend
1.-Crear base de datos MySQL:
CREATE DATABASE gametrack_auth;

2.-Configurar application.properties:
spring.datasource.url=jdbc:mysql://localhost:3306/gametrack_auth
spring.datasource.username=root
spring.datasource.password=

3.-Ejecutar Spring Boot:
cd backend-auth-service
./mvnw spring-boot:run

Verificar en: http://localhost:8080/api/auth/test

# Paso 2: Configurar y Ejecutar la App Android
1.-Clonar repositorio y abrir en Android Studio
2.-Cambiar la IP en ApiClient.kt: private const val BASE_URL = "http://TU_IP_LOCAL:8080/api/"
Reemplazar TU_IP_LOCAL con tu IP (ej: 10.116.67.176)
3.-Sincronizar proyecto: File → Sync Project with Gradle Files
4.-Ejecutar la app: Click en Run button
Seleccionar dispositivo físico o emulador

# Paso 3: Probar la Aplicación

1.-Registrar un nuevo usuario
2.-Iniciar sesión con las credenciales
3.-Añadir un juego (ej: "Minecraft")
4.-Ver la imagen automática de IGDB
5.-Probar recuperación de contraseña
6.-Cambiar foto de perfil (cámara/galería)

# Archivos de Distribución
# APK Firmado
Ubicación en repositorio: /app/release/app-release.apk
Versión: 1.0
Firma: Configurada en build.gradle.kts con keystore

# Keystore Configuración
Archivo: keystore/gametrack.jks (NO incluido en repo por seguridad)

Configuración en build.gradle.kts:
signingConfigs {
    create("release") {
        storeFile = file("../keystore/gametrack.jks")
        storePassword = "GameTrack2025"
        keyAlias = "gametrack"
        keyPassword = "GameTrack2025"
    }
}

# Validaciones de Formularios Implementadas

# Login Screen
Username no vacío
Password no vacío
Password mínimo 6 caracteres
Visual feedback con íconos
Vibración en errores

# Register Screen
Username único (verificación contra backend)
Email con formato válido
Password mínimo 6 caracteres
Confirmación de password coincidente

# Add Game Screen
Título del juego requerido
Calificación entre 1 y 10 (slider)
Plataforma seleccionable (PC, PlayStation, Xbox, Nintendo)
Estado seleccionable (Por jugar, Jugando, Completado, Abandonado)

# Forgot/Reset Password
Email válido requerido
Token de 6 dígitos
Nueva password mínimo 6 caracteres
Confirmación de password coincidente

# Sistema de Usuarios

# Rol Implementado: Usuario Registrado
Permisos: Gestión completa de su propia colección
Funcionalidades: CRUD de juegos, perfil personal, estadísticas
Persistencia: Datos locales + sincronización opcional con backend

# Mecanismo de Autenticación Híbrido
Primero intenta login local (Room Database)
Si falla, intenta con backend (Spring Boot)
Migración automática de SHA-256 a BCrypt
Persistencia de sesión con SharedPreferences

# Medidas de Seguridad

# Frontend
Passwords hasheados con BCrypt antes de enviar
No almacenamiento de credenciales en texto plano
Permisos runtime para cámara
Validación de inputs en cliente

# Backend
Passwords hasheados con BCrypt en base de datos
Validación de tokens en reset password
Manejo de errores con respuestas JSON estandarizadas
Logging detallado para debugging

# Comunicación
Headers HTTP estándar (Content-Type: application/json)
Timeouts configurados (30 segundos)
Interceptores para logging en desarrollo

# Diseño de Interfaz
# Tema Visual
Paleta principal: Verde neón (#39FF14) sobre fondo oscuro
Tipografía: Material Design 3
Formas: Esquinas redondeadas (8dp, 12dp, 16dp)
Modo: Preferencia por tema oscuro

# Componentes Reutilizables
GameCardListStyle: Tarjeta uniforme para juegos
Diálogos de confirmación para eliminaciones
Slider personalizado para calificaciones
FilterChips para plataformas y estados

# Animaciones
Transiciones entre pantallas
Indicadores de carga circulares
Animación en búsqueda de imágenes
Efectos de hover en tarjetas

# Solución de Problemas Comunes
# Error: "No hay conexión al servidor"
1.-Verificar que Spring Boot esté ejecutándose (http://localhost:8080/api/auth/test)
2.-Confirmar IP en ApiClient.kt línea 14
3.-Verificar que el dispositivo y servidor estén en misma red

# Error: "Usuario o contraseña incorrectos"
1.-Verificar credenciales en base de datos MySQL
2.-Revisar logs de Spring Boot para errores
3.-Probar registro de nuevo usuario

# Imágenes no se cargan en AddGameScreen
1.-Verificar conexión a internet
2.-Comprobar que el nombre del juego tenga al menos 3 caracteres
3.-Verificar credenciales de IGDB en ApiGame.kt

# Error al tomar foto en perfil
1.-Asegurarse de dar permiso a la cámara
2.-Verificar que el dispositivo tenga cámara
3.-Probar seleccionar de galería como alternativa

# APK no instala en dispositivo
1.-Habilitar "Orígenes desconocidos" en ajustes del dispositivo
2.-Verificar que el APK esté correctamente firmado
3.-Comprobar compatibilidad de Android version (minSdk 24)


# Licencia y Uso
Este proyecto fue desarrollado con fines educativos para la asignatura DSY1105 - Desarrollo de Aplicaciones Móviles en DUOC UC.
El código se proporciona como referencia educativa. Para uso comercial o redistribución, contactar a los autores.


# Información de Contacto
Repositorio GitHub: [https://github.com/TheRexInformatics/gametracksolitario.git]

Correo del equipo: [ma.villarroelt@duocuc.cl]

Asignatura: DSY1105 - Desarrollo de Aplicaciones Móviles

Profesor: [MARCELO EDUARDO CRISOSTOMO CARRASCO]

Fecha de entrega: Diciembre 2025


# Notas para el Evaluador
Backend: Ejecutar en puerto 8080, verificar IP en ApiClient.kt
Demo login: Usar cualquier usuario/password (sistema híbrido funciona)
Reset password: Usar cualquier token de 6 dígitos para demo
Imágenes automáticas: Escribir nombres de juegos conocidos (Minecraft, Zelda, etc.)
Persistencia: Los datos se guardan localmente incluso sin backend


# Si este proyecto te gustó, ¡no olvides darle una estrella en GitHub! #

