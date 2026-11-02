package com.keyboard.fuentes;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

/** Servicio de método de entrada y puente con el campo de texto activo. */
public class FontKeyboardService extends InputMethodService {
    public static final String PREFS_NAME = "keyboard_fuentes_prefs";
    public static final String KEY_FONT_STYLE = "font_style";

    private FontStyle style = FontStyle.NORMAL;
    private KeyboardRootView root;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        style = FontStyle.fromOrdinal(preferences.getInt(KEY_FONT_STYLE, FontStyle.NORMAL.ordinal()));
    }

    @Override
    public View onCreateInputView() {
        root = new KeyboardRootView(this, this);
        return root;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        if (root != null) {
            root.resetForNewInput();
        }
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && root != null && root.handleBack()) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onWindowHidden() {
        if (root != null) {
            root.stopRepeating();
        }
        super.onWindowHidden();
    }

    @Override
    public void onDestroy() {
        if (root != null) {
            root.stopRepeating();
        }
        super.onDestroy();
    }

    FontStyle getFontStyle() {
        return style;
    }

    void setFontStyle(FontStyle value) {
        style = value == null ? FontStyle.NORMAL : value;
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putInt(KEY_FONT_STYLE, style.ordinal())
                .apply();
    }

    void commitText(String text) {
        InputConnection connection = getCurrentInputConnection();
        if (connection != null && text != null) {
            connection.commitText(text, 1);
        }
    }

    void commitTyped(String text) {
        commitText(style.transform(text));
    }

    void deleteBackward() {
        InputConnection connection = getCurrentInputConnection();
        if (connection == null) {
            return;
        }

        CharSequence selected = connection.getSelectedText(0);
        if (selected != null && selected.length() > 0) {
            connection.commitText("", 1);
            return;
        }

        int count = 1;
        CharSequence before = connection.getTextBeforeCursor(2, 0);
        if (before != null && before.length() >= 2) {
            char high = before.charAt(before.length() - 2);
            char low = before.charAt(before.length() - 1);
            if (Character.isSurrogatePair(high, low)) {
                count = 2;
            }
        }
        connection.deleteSurroundingText(count, 0);
    }

    void sendEnter() {
        InputConnection connection = getCurrentInputConnection();
        if (connection == null) {
            return;
        }
        EditorInfo info = getCurrentInputEditorInfo();
        int action = info == null ? EditorInfo.IME_ACTION_NONE
                : info.imeOptions & EditorInfo.IME_MASK_ACTION;
        if (action == EditorInfo.IME_ACTION_NONE) {
            connection.commitText("\n", 1);
        } else {
            connection.performEditorAction(action);
        }
    }

    void pasteClipboard() {
        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            toast("Portapapeles no disponible");
            return;
        }
        try {
            if (!clipboard.hasPrimaryClip()) {
                toast("Portapapeles vacío");
                return;
            }
            ClipData clip = clipboard.getPrimaryClip();
            if (clip == null || clip.getItemCount() == 0) {
                toast("Portapapeles vacío");
                return;
            }
            CharSequence text = clip.getItemAt(0).coerceToText(this);
            if (text == null || text.length() == 0) {
                toast("Portapapeles vacío");
                return;
            }
            commitTyped(text.toString());
            toast("Pegado con fuente " + style.getLabel());
        } catch (SecurityException exception) {
            toast("Sin acceso al portapapeles");
        }
    }

    void openSettings() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            toast(getString(R.string.settings_error));
        }
    }

    void toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
