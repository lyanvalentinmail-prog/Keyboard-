# Teclado Fuentes

App de teclado (IME) para Android inspirada en la captura de One UI guardada en la carpeta **Keyboard** de Google Drive. Incluye iconos SVG renovados, el diseño oscuro, la fila numérica, la distribución QWERTY española con `ñ`, los símbolos secundarios, la barra espaciadora «Español (US)» y un botón **Aa** para cambiar la fuente con la que se escribe.

## Descargar el APK desde el Workflow

El workflow [`Build Android APK`](.github/workflows/build-apk.yml) compila, prueba y firma un APK en cada `push`, `pull_request` y ejecución manual (`workflow_dispatch`).

1. Abre <https://github.com/lyanvalentinmail-prog/Keyboard-/actions/workflows/build-apk.yml>.
2. Entra en la ejecución más reciente de la rama `arena/22267e0d-keyboard`. También puedes pulsar **Run workflow** para generarla de nuevo.
3. Descarga el artefacto **KeyboardFuentes-APK**. Dentro están `KeyboardFuentes.apk` y su `SHA-256`.
4. En el teléfono, permite la instalación desde orígenes desconocidos y abre el APK.

Si se crea una etiqueta `v*` (por ejemplo `v1.0.0`), el mismo workflow publica además una **GitHub Release** con el APK como descarga directa y notas generadas automáticamente. También puedes ejecutarlo desde la terminal con `gh workflow run build-apk.yml --ref arena/22267e0d-keyboard`.

> El APK se firma con una clave de depuración de CI para poder instalarlo sin configurar secretos. El workflow la genera una sola vez en `.ci/keyboard-debug.keystore` y la conserva en la caché de Actions, de modo que la firma se mantiene entre ejecuciones. Esa carpeta está ignorada por Git. Para publicar en Google Play, configura una clave de lanzamiento propia. Si la caché se pierde y Android rechaza una actualización por firma distinta, desinstala la versión anterior antes de instalar la nueva.

## Activar el teclado

1. Instala el APK y abre **Teclado Fuentes**.
2. Pulsa **Activar en Ajustes** y habilita *Teclado Fuentes*.
3. Pulsa **Elegir teclado** y selecciónalo como método activo.
4. Usa **Probar el teclado** para escribir en el campo de prueba.

## Cambiar la fuente

Pulsa el botón circular **Aa** situado a la izquierda de la barra superior. Se abre un selector con ocho estilos: Normal, Negrita, Cursiva, Negrita cursiva, Manuscrita, Monoespacio, Círculos y Ancho completo. La elección se guarda y se aplica al texto que escribas a partir de ese momento, incluido lo que pegues desde el portapapeles.

Un teclado de Android no puede imponer una tipografía dentro de otra aplicación, porque solo envía texto al campo activo. Por eso estos estilos usan caracteres Unicode compatibles (matemáticos, circulares o de ancho completo) que parecen otra fuente y funcionan sin permisos especiales. La mayoría de apps modernas los muestra; algunas apps o fuentes pueden mostrar cuadros para determinados glifos. Los signos de puntuación y los emojis se conservan tal cual.

## Diseño y controles

- Barra superior oscura con cinco botones: **Aa**, emojis, portapapeles, ajustes y más opciones. Los iconos son vectores SVG de 24 unidades, con estados pulsado, activo y enfocado.
- Los cierres, accesos rápidos y botones de la pantalla principal también usan los mismos SVG. El icono de la app tiene versión adaptable para Android 8+.
- **Vibración al tocar** se puede activar o desactivar en la pantalla principal; se guarda y respeta el ajuste del sistema.
- Vista compacta anclada abajo: ancho máximo de 560dp, alto máximo del 58% de la pantalla y 292dp en móviles normales. No entra en modo pantalla completa; las filas se reparten solo ese alto y los paneles se desplazan si hace falta.
- Fila numérica `1–0` y cuatro filas QWERTY con teclas redondeadas grises.
- Mantén pulsada una tecla para insertar el símbolo pequeño de su esquina superior derecha (`+`, `@`, `#`, `$`, etc.).
- Mayúsculas alterna la primera letra; una pulsación larga activa o desactiva el bloqueo, con un icono específico y estado accesible.
- Borrar elimina la selección o el carácter anterior y repite al mantener pulsado. Usa ICU en Android 7+ y una alternativa compatible en Android 6 para acentos, emojis combinados, tonos de piel y banderas; nunca divide un par de sustitutos Unicode.
- `!#1` abre la página de símbolos; `ABC` vuelve a las letras.
- La tecla de acción cambia su icono y función según el campo: intro, buscar, enviar, siguiente, anterior, ir o hecho. Respeta los campos multilínea. Una pulsación larga inserta un salto de línea.
- Portapapeles abre un panel con vista previa, pegado original o con la fuente elegida, copia de la selección y actualización. Solo lee el portapapeles tras una acción del usuario y no guarda historial. Mantener pulsado su botón superior pega directamente con la fuente activa.
- El engranaje abre activación y preferencias; Más opciones permite cambiar de panel, abrir símbolos, ocultar el teclado o volver a las teclas.
- La pantalla principal incluye un campo de prueba, borrar prueba, copiar el ejemplo y una guía si el teclado aún no está activado.

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
├── ToolButtonView.java        # Botones de la barra con SVG y estados accesibles
├── PanelUi.java               # Controles, iconos y respuesta táctil compartidos
├── ClipboardPanelView.java    # Pegado, copia y vista previa del portapapeles
├── EditorAction.java          # Acción e icono adecuados para cada campo
├── TextEditing.java           # Borrado Unicode seguro y compatibilidad con Android 6
├── FontPickerView.java        # Selector de estilos
├── EmojiPanelView.java        # Panel de emojis
├── MorePanelView.java         # Panel de opciones adicionales
├── FontStyle.java             # Transformación de texto a estilos Unicode
├── FontAlphabets.java         # Alfabetos y dígitos Unicode
├── KeyboardBounds.java        # Límites compactos de ancho y alto
└── MainActivity.java          # Activación, prueba e instrucciones
```

No se usan permisos de internet ni dependencias de AndroidX en la aplicación instalada. Todo el teclado se dibuja con vistas nativas de Android. Robolectric y AndroidX se utilizan únicamente en las pruebas locales/CI.

## Iconos SVG

Los 26 SVG editables están en `design/icons/`. Android no carga SVG directamente en las vistas del IME: cada SVG tiene un VectorDrawable equivalente en `app/src/main/res/drawable/`. Ambos se generan desde `tools/sync_icons.py` para mantener el mismo trazo, geometría y color:

```bash
python3 tools/sync_icons.py
python3 tools/sync_icons.py --check
```

La comprobación también se ejecuta en CI. No se necesita ninguna fuente de iconos ni conexión a internet para usar el teclado.

## Verificación

La versión 1.1.0 incluye 40 pruebas: estilos Unicode, límites del teclado, acciones del editor, borrado de grafemas e inserción de texto, botones y paneles, portapapeles, activación, copia, borrado del campo de prueba y preferencias. El workflow ejecuta las pruebas antes de firmar y publicar el APK; también adjunta el informe de pruebas y verifica la firma y alineación del paquete.

Las pruebas automatizadas no sustituyen una prueba en un teléfono físico: la disponibilidad del selector y del portapapeles depende de la versión de Android y de la aplicación que tenga el foco.
