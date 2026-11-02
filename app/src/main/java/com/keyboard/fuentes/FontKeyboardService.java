package com.keyboard.fuentes;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.text.TextUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

/** Servicio de método de entrada y puente con el campo de texto activo. */
public class FontKeyboardService extends InputMethodService {
    public static final String PREFS_NAME = "keyboard_fuentes_prefs";
    public static final String KEY_FONT_STYLE = "font_style";
    public static final String KEY_HAPTICS = "haptics";

    private FontStyle style = FontStyle.NORMAL;
    private KeyboardRootView root;
    private FrameLayout inputWrapper;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        style = FontStyle.fromOrdinal(preferences.getInt(KEY_FONT_STYLE, FontStyle.NORMAL.ordinal()));
    }

    @Override
    public View onCreateInputView() {
        inputWrapper = new FrameLayout(this);
        inputWrapper.setBackgroundColor(PanelUi.color(this, R.color.window_bg));
        inputWrapper.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        root = new KeyboardRootView(this, this);
        inputWrapper.addView(root);
        applyCompactBounds();
        return inputWrapper;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        applyCompactBounds();
        updateFullscreenMode();
        if (root != null) {
            root.resetForNewInput(info);
        }
    }

    @Override
    public void onConfigureWindow(Window win, boolean isFullscreen, boolean isCandidatesOnly) {
        super.onConfigureWindow(win, isFullscreen, isCandidatesOnly);
        configureWindowSize(win);
        constrainInputArea(win);
        applyCompactBounds();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyCompactBounds();
        updateFullscreenMode();
    }

    @Override
    public void onComputeInsets(Insets outInsets) {
        super.onComputeInsets(outInsets);
        if (!isFullscreenMode()) {
            int top = visibleKeyboardTop();
            outInsets.visibleTopInsets = top;
            outInsets.contentTopInsets = top;
            outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_VISIBLE;
            outInsets.touchableRegion.setEmpty();
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
    public void onFinishInputView(boolean finishingInput) {
        if (root != null) {
            root.stopRepeating();
        }
        super.onFinishInputView(finishingInput);
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

    private void configureWindowSize(Window win) {
        win.setGravity(Gravity.BOTTOM);
        win.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void constrainInputArea(Window win) {
        int inputAreaId = getResources().getIdentifier("inputArea", "id", "android");
        View inputArea = inputAreaId == 0 ? null : win.findViewById(inputAreaId);
        if (inputArea != null) {
            ViewGroup.LayoutParams params = inputArea.getLayoutParams();
            if (params != null) {
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                if (params instanceof FrameLayout.LayoutParams) {
                    ((FrameLayout.LayoutParams) params).gravity = Gravity.BOTTOM;
                }
                inputArea.setLayoutParams(params);
            }
            inputArea.requestLayout();
        }

        int inputViewId = getResources().getIdentifier("inputView", "id", "android");
        View inputView = inputViewId == 0 ? null : win.findViewById(inputViewId);
        if (inputView != null) {
            ViewGroup.LayoutParams params = inputView.getLayoutParams();
            if (params != null) {
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                inputView.setLayoutParams(params);
            }
        }

        if (inputWrapper != null) {
            ViewGroup.LayoutParams params = inputWrapper.getLayoutParams();
            if (params != null) {
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                if (params instanceof FrameLayout.LayoutParams) {
                    ((FrameLayout.LayoutParams) params).gravity = Gravity.BOTTOM;
                }
                inputWrapper.setLayoutParams(params);
            }
            inputWrapper.requestLayout();
        }
    }

    private void applyCompactBounds() {
        if (root == null) {
            return;
        }
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int availableWidth = getMaxWidth();
        if (availableWidth <= 0) {
            availableWidth = metrics.widthPixels;
        }
        int width = KeyboardBounds.width(availableWidth, metrics.density);
        int height = KeyboardBounds.height(metrics.heightPixels, metrics.density);
        int toolbar = KeyboardBounds.toolbarHeight(height, metrics.density);
        int gap = KeyboardBounds.keyGap(height, metrics.density);
        root.applyCompactBounds(width, height, toolbar, gap);
    }

    private int visibleKeyboardTop() {
        if (root == null) {
            return 0;
        }
        int[] location = new int[2];
        root.getLocationInWindow(location);
        int top = location[1];
        if (top <= 0 && inputWrapper != null) {
            top = inputWrapper.getTop() + root.getTop();
        }
        if (top <= 0) {
            Window win = getWindow() == null ? null : getWindow().getWindow();
            View decor = win == null ? null : win.getDecorView();
            if (decor != null && decor.getHeight() > 0) {
                ViewGroup.LayoutParams params = root.getLayoutParams();
                int rootHeight = params == null ? 0 : params.height;
                top = Math.max(0, decor.getHeight() - rootHeight);
            }
        }
        return Math.max(0, top);
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
        connection.beginBatchEdit();
        try {
            CharSequence selected = connection.getSelectedText(0);
            if (selected != null && selected.length() > 0) {
                connection.commitText("", 1);
                return;
            }
            if (selected == null) {
                ExtractedText extracted = connection.getExtractedText(new ExtractedTextRequest(), 0);
                if (extracted != null && extracted.selectionStart >= 0
                        && extracted.selectionEnd > extracted.selectionStart) {
                    connection.commitText("", 1);
                    return;
                }
            }
            CharSequence before = connection.getTextBeforeCursor(128, 0);
            int count = TextEditing.safeLength(before, graphemeLength(before));
            if (count > 0) {
                connection.deleteSurroundingText(count, 0);
            }
        } finally {
            connection.endBatchEdit();
        }
    }

    private int graphemeLength(CharSequence before) {
        if (before == null || before.length() == 0) {
            return 0;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return IcuText.previousGraphemeLength(before);
        }
        return TextEditing.previousGraphemeLength(before);
    }

    /** Isolated so Android 6 never loads the API 24 ICU class. */
    private static final class IcuText {
        static int previousGraphemeLength(CharSequence text) {
            android.icu.text.BreakIterator iterator = android.icu.text.BreakIterator.getCharacterInstance();
            iterator.setText(text.toString());
            int boundary = iterator.preceding(text.length());
            return boundary < 0 ? text.length() : text.length() - boundary;
        }
    }

    EditorAction editorAction() {
        EditorInfo info = getCurrentInputEditorInfo();
        return EditorAction.fromImeOptions(info == null ? EditorInfo.IME_ACTION_UNSPECIFIED : info.imeOptions);
    }

    void sendEnter() {
        InputConnection connection = getCurrentInputConnection();
        if (connection == null) {
            return;
        }
        EditorAction action = editorAction();
        if (action == EditorAction.ENTER) {
            connection.commitText("\n", 1);
        } else if (!connection.performEditorAction(action.id)) {
            // Do not lose the key press in editors that do not implement the action.
            connection.commitText("\n", 1);
        }
    }

    String readClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            return null;
        }
        try {
            ClipData clip = clipboard.getPrimaryClip();
            if (clip == null || clip.getItemCount() == 0) {
                return null;
            }
            CharSequence text = clip.getItemAt(0).coerceToText(this);
            return text == null ? null : text.toString();
        } catch (RuntimeException exception) {
            return null;
        }
    }

    boolean pasteClipboard(boolean styled) {
        String text = readClipboard();
        if (TextUtils.isEmpty(text)) {
            toast("No hay texto accesible en el portapapeles");
            return false;
        }
        if (styled) {
            commitTyped(text);
        } else {
            commitText(text);
        }
        toast(styled ? "Pegado con fuente " + style.getLabel() : "Pegado sin cambiar el texto");
        return true;
    }

    void copySelection() {
        InputConnection connection = getCurrentInputConnection();
        CharSequence selection = connection == null ? null : connection.getSelectedText(0);
        if (selection == null || selection.length() == 0) {
            toast("Selecciona texto en el campo antes de copiar");
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            toast("Portapapeles no disponible");
            return;
        }
        try {
            clipboard.setPrimaryClip(ClipData.newPlainText("Texto seleccionado", selection));
            toast("Texto copiado");
        } catch (RuntimeException exception) {
            toast("No se pudo copiar el texto");
        }
    }

    void hideKeyboard() {
        if (root != null) {
            root.stopRepeating();
        }
        requestHideSelf(0);
    }

    void tap(View view) {
        if (getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(KEY_HAPTICS, true)) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
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
