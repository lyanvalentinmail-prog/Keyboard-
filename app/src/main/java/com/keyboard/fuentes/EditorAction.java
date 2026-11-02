package com.keyboard.fuentes;

/** Pure editor-action resolution, including IME_FLAG_NO_ENTER_ACTION. */
enum EditorAction {
    ENTER(1), GO(2), SEARCH(3), SEND(4), NEXT(5), DONE(6), PREVIOUS(7);

    static final int MASK_ACTION = 0xff;
    static final int FLAG_NO_ENTER_ACTION = 0x10000000;

    final int id;

    EditorAction(int id) {
        this.id = id;
    }

    static EditorAction fromImeOptions(int options) {
        if ((options & FLAG_NO_ENTER_ACTION) != 0) {
            return ENTER;
        }
        switch (options & MASK_ACTION) {
            case 2: return GO;
            case 3: return SEARCH;
            case 4: return SEND;
            case 5: return NEXT;
            case 6: return DONE;
            case 7: return PREVIOUS;
            default: return ENTER;
        }
    }

    KeyView.Icon icon() {
        switch (this) {
            case GO: return KeyView.Icon.GO;
            case SEARCH: return KeyView.Icon.SEARCH;
            case SEND: return KeyView.Icon.SEND;
            case NEXT: return KeyView.Icon.NEXT;
            case DONE: return KeyView.Icon.DONE;
            case PREVIOUS: return KeyView.Icon.PREVIOUS;
            default: return KeyView.Icon.ENTER;
        }
    }

    String description() {
        switch (this) {
            case GO: return "Ir. Mantén pulsado para insertar un salto de línea";
            case SEARCH: return "Buscar. Mantén pulsado para insertar un salto de línea";
            case SEND: return "Enviar. Mantén pulsado para insertar un salto de línea";
            case NEXT: return "Siguiente campo. Mantén pulsado para insertar un salto de línea";
            case DONE: return "Hecho. Mantén pulsado para insertar un salto de línea";
            case PREVIOUS: return "Campo anterior. Mantén pulsado para insertar un salto de línea";
            default: return "Intro. Insertar un salto de línea";
        }
    }
}
