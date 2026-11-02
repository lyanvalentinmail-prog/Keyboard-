# Teclado Fuentes

App de teclado (IME) para Android inspirada en la captura de One UI guardada en la carpeta **Keyboard** de Google Drive. Incluye el diseño oscuro, la fila numérica, la distribución QWERTY española con `ñ`, los símbolos secundarios, la barra espaciadora «Español (US)» y un botón **Aa** para cambiar la fuente con la que se escribe.

## Descargar el APK desde el Workflow

El workflow [`Build Android APK`](.github/workflows/build-apk.yml) compila, prueba y firma un APK en cada `push`, `pull_request` y ejecución manual (`workflow_dispatch`).

1. Abre <https://github.com/lyanvalentinmail-prog/Keyboard-/actions/workflows/build-apk.yml>.
2. Entra en la ejecución más reciente de la rama `arena/a1cea532-keyboard`. También puedes pulsar **Run workflow** para generarla de nuevo.
3. Descarga el artefacto **KeyboardFuentes-APK**. Dentro están `KeyboardFuentes.apk` y su `SHA-256`.
4. En el teléfono, permite la instalación desde orígenes desconocidos y abre el APK.

Si se crea una etiqueta `v*` (por ejemplo `v1.0.0`), el mismo workflow publica además una **GitHub Release** con el APK como descarga directa y notas generadas automáticamente. También puedes ejecutarlo desde la terminal con `gh workflow run build-apk.yml --ref arena/a1cea532-keyboard`.

> El APK se firma con la clave de depuración que genera Gradle para poder instalarlo sin configurar secretos. El workflow la conserva en la caché de Actions para mantener la misma firma entre ejecuciones. Para publicar en Google Play, configura una clave de lanzamiento propia. Si la caché se pierde y Android rechaza una actualización por firma distinta, desinstala la versión anterior antes de instalar la nueva.

## Activar el teclado

1. Instala el APK y abre **Teclado Fuentes**.
2. Pulsa **Activar en Ajustes** y habilita *Teclado Fuentes*.
3. Pulsa **Elegir teclado** y selecciónalo como método activo.
4. Usa **Probar el teclado** para escribir en el campo de prueba.

## Cambiar la fuente

Pulsa el botón circular **Aa** situado a la izquierda de la barra superior. Se abre un selector con ocho estilos: Normal, Negrita, Cursiva, Negrita cursiva, Manuscrita, Monoespacio, Círculos y Ancho completo. La elección se guarda y se aplica al texto que escribas a partir de ese momento, incluido lo que pegues desde el portapapeles.

Un teclado de Android no puede imponer una tipografía dentro de otra aplicación, porque solo envía texto al campo activo. Por eso estos estilos usan caracteres Unicode compatibles (matemáticos, circulares o de ancho completo) que parecen otra fuente y funcionan sin permisos especiales. La mayoría de apps modernas los muestra; algunas apps o fuentes pueden mostrar cuadros para determinados glifos. Los signos de puntuación y los emojis se conservan tal cual.

## Diseño y controles

- Barra superior oscura con cinco botones circulares: **Aa**, emojis, portapapeles, ajustes y más opciones.
- Fila numérica `1–0` y cuatro filas QWERTY con teclas redondeadas grises.
- Mantén pulsada una tecla para insertar el símbolo pequeño de su esquina superior derecha (`+`, `@`, `#`, `$`, etc.).
- `⇧` alterna mayúsculas; una pulsación larga activa o desactiva bloqueo de mayúsculas.
- `⌫` borra y, al mantenerlo, borra repetidamente. También elimina correctamente caracteres Unicode de dos unidades.
- `!#1` abre la página de símbolos; `ABC` vuelve a las letras.
- La lupa ejecuta la acción del editor (buscar, enviar, etc.) o inserta una nueva línea.
- El botón de portapapeles pega el primer elemento; el engranaje abre la pantalla de configuración; `⋯` abre accesos a fuentes, emojis, símbolos y ajustes.

## Compilar en local

Necesitas JDK 17 y Android SDK 34.

```bash
./gradlew testDebugUnitTest assembleRelease
```

El APK queda en `app/build/outputs/apk/release/`. En CI se usa `./gradlew testDebugUnitTest assembleRelease` y se copia a `dist/KeyboardFuentes.apk`.

## Estructura

```text
app/src/main/java/com/keyboard/fuentes/
├── FontKeyboardService.java   # Servicio InputMethodService y comunicación con el campo de texto
├── KeyboardRootView.java      # Barra, filas de teclas y paneles
├── KeyView.java               # Tecla dibujada a medida, iconos y símbolos secundarios
├── ToolButtonView.java        # Botones circulares de la barra superior
├── FontPickerView.java        # Selector de estilos
├── EmojiPanelView.java        # Panel de emojis
├── MorePanelView.java         # Panel de opciones adicionales
├── FontStyle.java             # Transformación de texto a estilos Unicode
├── FontAlphabets.java         # Alfabetos y dígitos Unicode
└── MainActivity.java          # Activación, prueba e instrucciones
```

No se usan permisos de internet ni dependencias de AndroidX. Todo el teclado se dibuja con vistas nativas de Android.
