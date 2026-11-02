package com.keyboard.fuentes;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/** Activation, real preferences and a test field that explicitly requests the IME. */
public class MainActivity extends Activity {
    private TextView statusText;
    private TextView previewText;
    private EditText testInput;
    private Button clearButton;
    private Switch hapticsSwitch;
    private boolean updatingHaptics;
    private final SharedPreferences.OnSharedPreferenceChangeListener preferencesListener =
            (preferences, key) -> runOnUiThread(this::updateStatus);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusText = findViewById(R.id.statusText);
        previewText = findViewById(R.id.previewText);
        testInput = findViewById(R.id.testInput);
        clearButton = findViewById(R.id.clearButton);
        hapticsSwitch = findViewById(R.id.hapticsSwitch);

        Button enableButton = findViewById(R.id.enableButton);
        Button chooseButton = findViewById(R.id.chooseButton);
        Button testButton = findViewById(R.id.testButton);
        Button copyButton = findViewById(R.id.copyButton);
        decorate(enableButton, R.drawable.ic_settings, true);
        decorate(chooseButton, R.drawable.ic_keyboard, false);
        decorate(testButton, R.drawable.ic_edit, false);
        decorate(clearButton, R.drawable.ic_close, false);
        decorate(copyButton, R.drawable.ic_copy, false);

        enableButton.setOnClickListener(view -> openInputMethodSettings());
        chooseButton.setOnClickListener(view -> showInputMethodPicker());
        testButton.setOnClickListener(view -> showKeyboardForTest());
        clearButton.setOnClickListener(view -> {
            testInput.setText("");
            testInput.requestFocus();
            toast(R.string.test_cleared);
        });
        copyButton.setOnClickListener(view -> copyExample());
        testInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable text) {
                clearButton.setEnabled(text.length() > 0);
                clearButton.setAlpha(text.length() > 0 ? 1f : 0.45f);
            }
        });
        clearButton.setEnabled(testInput.getText().length() > 0);
        clearButton.setAlpha(testInput.getText().length() > 0 ? 1f : 0.45f);
        updatingHaptics = true;
        hapticsSwitch.setChecked(preferences().getBoolean(FontKeyboardService.KEY_HAPTICS, true));
        updatingHaptics = false;
        hapticsSwitch.setOnCheckedChangeListener((button, checked) -> {
            if (!updatingHaptics) {
                preferences().edit().putBoolean(FontKeyboardService.KEY_HAPTICS, checked).apply();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    @Override
    protected void onStart() {
        super.onStart();
        preferences().registerOnSharedPreferenceChangeListener(preferencesListener);
    }

    @Override
    protected void onStop() {
        preferences().unregisterOnSharedPreferenceChangeListener(preferencesListener);
        super.onStop();
    }

    @Override
    protected void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            updateStatus();
        }
    }

    private void decorate(Button button, int icon, boolean primary) {
        Drawable drawable = PanelUi.icon(this, icon, PanelUi.color(this,
                primary ? R.color.button_text : R.color.accent));
        int size = PanelUi.dp(this, 22);
        drawable.setBounds(0, 0, size, size);
        button.setCompoundDrawables(drawable, null, null, null);
        button.setCompoundDrawablePadding(PanelUi.dp(this, 10));
    }

    private void openInputMethodSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
        } catch (ActivityNotFoundException | SecurityException exception) {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                toast(R.string.settings_error);
            } catch (ActivityNotFoundException | SecurityException fallbackError) {
                toast(R.string.settings_error);
            }
        }
    }

    private void showInputMethodPicker() {
        if (!isImeEnabled()) {
            toast(R.string.enable_first);
            openInputMethodSettings();
            return;
        }
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (manager == null) {
            toast(R.string.picker_error);
            return;
        }
        try {
            manager.showInputMethodPicker();
        } catch (RuntimeException exception) {
            toast(R.string.picker_error);
        }
    }

    private void showKeyboardForTest() {
        if (!isImeEnabled()) {
            toast(R.string.enable_first);
            openInputMethodSettings();
            return;
        }
        testInput.requestFocus();
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (manager == null) {
            toast(R.string.picker_error);
            return;
        }
        if (!isImeActive()) {
            toast(R.string.choose_to_test);
            showInputMethodPicker();
            return;
        }
        // Let the window and EditText gain focus before requesting the keyboard.
        // Unlike SHOW_IMPLICIT, this is an explicit user-initiated request.
        testInput.post(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            try {
                boolean shown = manager.showSoftInput(testInput, 0);
                if (shown && isImeActive()) {
                    toast(R.string.test_ready);
                } else if (!shown) {
                    toast(R.string.choose_to_test);
                    manager.showInputMethodPicker();
                }
            } catch (RuntimeException exception) {
                toast(R.string.picker_error);
            }
        });
    }

    private void copyExample() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            toast(R.string.clipboard_error);
            return;
        }
        try {
            clipboard.setPrimaryClip(ClipData.newPlainText("Ejemplo de Teclado Fuentes", previewText.getText()));
            toast(R.string.example_copied);
        } catch (RuntimeException exception) {
            toast(R.string.clipboard_error);
        }
    }

    private void updateStatus() {
        boolean enabled = isImeEnabled();
        boolean active = isImeActive();
        int status = active ? R.string.status_active : enabled ? R.string.status_enabled : R.string.status_not_enabled;
        statusText.setText(status);
        statusText.setTextColor(PanelUi.color(this, active ? R.color.accent : R.color.warning));
        previewText.setText(currentStyle().transform(getString(R.string.preview_sample)));
    }

    private boolean isImeEnabled() {
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (manager == null) {
            return false;
        }
        for (InputMethodInfo info : manager.getEnabledInputMethodList()) {
            if (info.getComponent().equals(imeComponent())) {
                return true;
            }
        }
        return false;
    }

    private boolean isImeActive() {
        String active = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        ComponentName component = active == null ? null : ComponentName.unflattenFromString(active);
        return imeComponent().equals(component);
    }

    private ComponentName imeComponent() {
        return new ComponentName(this, FontKeyboardService.class);
    }

    private SharedPreferences preferences() {
        return getSharedPreferences(FontKeyboardService.PREFS_NAME, MODE_PRIVATE);
    }

    private FontStyle currentStyle() {
        return FontStyle.fromOrdinal(preferences().getInt(FontKeyboardService.KEY_FONT_STYLE,
                FontStyle.NORMAL.ordinal()));
    }

    private void toast(int resource) {
        Toast.makeText(this, resource, Toast.LENGTH_SHORT).show();
    }
}
