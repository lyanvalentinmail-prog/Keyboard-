package com.keyboard.fuentes;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.Toast;

/** Servicio de método de entrada y puente con el campo de texto activo. */
public class FontKeyboardService extends InputMethodService {
    public static final String PREFS_NAME = "keyboard_fuentes_prefs";
    public static final String KEY_FONT_STYLE = "font_style";

    private static final int COLOR_WINDOW_BG = Color.parseColor("#111111");

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
        inputWrapper.setBackgroundColor(COLOR_WINDOW_BG);
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
            root.resetForNewInput();
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
