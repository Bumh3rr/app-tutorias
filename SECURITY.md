# Seguridad — Sistema de Tutorías

## Modelo de autenticación

Spring Security 6 con `BCryptPasswordEncoder`. Cada usuario tiene un registro en la tabla `usuario` con `username`, `password_hash` (BCrypt) y `rol` (enum).

---

## Roles y acceso

| Rol         | Descripción                         | Rutas permitidas            |
|-------------|-------------------------------------|-----------------------------|
| `DDA`       | Dirección de Docencia y Apoyo       | `/admin/**`, `/mi-cuenta`   |
| `CIT`       | Centro de Información y Tecnología  | `/admin/**`, `/mi-cuenta`   |
| `SUBDIRECTOR` | Panel de resumen general          | `/subdirector/**`, `/mi-cuenta` |
| `COORDINADOR` | Panel de su carrera               | `/coordinador/**`, `/mi-cuenta` |
| `TUTOR`     | Panel de sus grupos                 | `/tutor/**`, `/mi-cuenta`   |
| `TUTORADO`  | Vista de su tutoría y asistencia    | `/tutorado/**`, `/mi-cuenta` |

---

## Credenciales predeterminadas (desarrollo)

Creadas automáticamente por `DataSeeder` al iniciar la aplicación.

| Usuario                            | Contraseña       | Rol          |
|------------------------------------|------------------|--------------|
| `dda@chilpancingo.tecnm.mx`        | `dda2026`        | DDA          |
| `cit@chilpancingo.tecnm.mx`        | `cit2026`        | CIT          |
| `subdirector@chilpancingo.tecnm.mx` | `subdirector2026` | SUBDIRECTOR |

> **Importante:** Cambiar estas contraseñas antes de pasar a producción usando `/mi-cuenta/cambiar-password`.

---

## Cuentas sincronizadas automáticamente

Al arrancar, `DataSeeder` crea cuentas `usuario` para tutores, tutorados y coordinadores existentes que tengan email registrado y no tengan cuenta aún:

- **Tutores** → rol `TUTOR`, usuario = email, contraseña = número de control (BCrypt)
- **Tutorados** → rol `TUTORADO`, usuario = email, contraseña = número de control
- **Coordinadores** → rol `COORDINADOR`, usuario = email, contraseña = número de control

Si una entidad no tiene número de control, se genera una contraseña aleatoria (revisar logs de arranque).

---

## Cambio de contraseña

Todos los usuarios autenticados pueden cambiar su contraseña desde **Mi Cuenta** (`/mi-cuenta`).

Validaciones:
- La contraseña actual debe ser correcta
- La nueva contraseña debe tener al menos 6 caracteres
- La confirmación debe coincidir

---

## Logout

`POST /logout` con token CSRF. Spring Security invalida la sesión y borra la cookie `JSESSIONID`. El enlace de logout está en la barra lateral para todos los roles.

---

## Tabla `usuario`

```sql
CREATE TABLE usuario (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    rol             ENUM('DDA','CIT','SUBDIRECTOR','COORDINADOR','TUTOR','TUTORADO') NOT NULL,
    activo          TINYINT(1) NOT NULL DEFAULT 1,
    fecha_creacion  DATETIME,
    id_tutor        INT UNIQUE,
    id_tutorado     INT UNIQUE,
    id_coordinador  INT UNIQUE,
    FOREIGN KEY (id_tutor)       REFERENCES tutor(id),
    FOREIGN KEY (id_tutorado)    REFERENCES tutorado(id),
    FOREIGN KEY (id_coordinador) REFERENCES coordinador_carrera(id)
);
```

Hibernate crea/actualiza esta tabla automáticamente con `ddl-auto=update`.
