# Green Portals (estilo DEDsafio) — Forge 1.20.1 / Java 17

**Estado: codigo fuente completo, NO compilado ni probado.** Se escribio en un entorno sin acceso
a red ni JDK/Gradle, por lo que no se pudo resolver Forge ni ejecutar `gradle build`.
No hay `.jar` incluido. Es posible que haya errores de compilacion o de ejecucion que
solo aparecen al compilar y probar en el juego.

## Compilar
Requisitos: JDK 17, acceso a internet (Forge/Gradle).
```
gradle wrapper --gradle-version 8.1.1   # una sola vez (genera gradle-wrapper.jar)
./gradlew build                          # jar en build/libs/dedportals-1.20.1-1.0.0.jar
./gradlew runClient                      # probar en cliente de desarrollo
./gradlew runServer
```
Copia el jar a `mods/` de un cliente y/o servidor Forge 1.20.1 (47.x). El mod es necesario en ambos lados.

## Comandos (permiso OP 2) — `/dedportal ...`
| Comando | Efecto |
|---|---|
| `create <nombre> [pos]` | Crea marco 4x5 (interior 2x3) delante de ti y lo abre con animacion |
| `remove <nombre>` | Elimina portal y marco |
| `enable/disable <nombre>` | Abre/cierra (animado) |
| `setdest <nombre> here` | Destino = tu posicion/dimension/orientacion |
| `setdest <nombre> to <dimension> <x y z>` | Destino explicito |
| `link <a> <b> [oneway]` | Enlaza portales (bidireccional por defecto) |
| `unlink <nombre>` | Quita destino |
| `set <nombre> effects <true/false>` / `set <nombre> delay <ticks>` | Config por portal |
| `info`, `list`, `tp <nombre>` | Consulta / ir al portal |

Los portales se guardan en el mundo (`SavedData`) y sobreviven a reinicios.

## Configuracion
`dedportals-common.toml` (retardo, enfriamiento, radio de busqueda segura) y
`dedportals-client.toml` (efectos, shader on/off, intensidad, duracion de recuperacion, camara).

## Notas de diseno
- Servidor: deteccion (`PortalBlock#entityInside`), retardo, busqueda de lugar seguro, teletransporte y cooldown.
- Cliente: shader `portal_warp` (copia el framebuffer, torbellino, absorcion, ondas, aberracion cromatica,
  rayos); si falla o se desactiva, se usa un velo verde alternativo. FOV/giro de camara configurables.
- El efecto nunca bloquea el movimiento; tiene un tiempo maximo de seguridad de 4 s.
- Marcos y portales son indestructibles en supervivencia; se eliminan con `/dedportal remove`.
- `create` sobrescribe los bloques del area 4x5.

## Compilar en GitHub (Codespaces / Actions)
1. Crea un repositorio y sube el contenido de este zip (los ficheros en la raiz, no dentro de una carpeta).
2. **Actions:** cada push ejecuta `.github/workflows/build.yml`; el jar queda en *Actions > ejecucion > Artifacts > dedportals-jar*.
3. **Codespaces:** abre el repo en un Codespace (usa `.devcontainer`, con JDK 17 y Gradle 8.1.1). Ejecuta `./gradlew build`; el jar sale en `build/libs/`.
