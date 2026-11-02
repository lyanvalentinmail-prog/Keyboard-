package com.keyboard.fuentes;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/** Pantalla de activación, prueba e instrucciones del teclado. */
public class MainActivity extends Activity {
    private TextView statusText;
    private TextView previewText;
    private EditText testInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        previewText = findViewById(R.id.previewText);
        testInput = findViewById(R.id.testInput);

        Button enableButton = findViewById(R.id.enableButton);
        Button chooseButton = findViewById(R.id.chooseButton);
        Button testButton = findViewById(R.id.testButton);

        enableButton.setOnClickListener(view -> openInputMethodSettings());
        chooseButton.setOnClickListener(view -> showInputMethodPicker());
        testButton.setOnClickListener(view -> showKeyboardForTest());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void openInputMethodSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.settings_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void showInputMethodPicker() {
        InputMethodManager manager =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.showInputMethodPicker();
        }
    }

    private void showKeyboardForTest() {
        testInput.requestFocus();
        InputMethodManager manager =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.showSoftInput(testInput, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void updateStatus() {
        boolean enabled = isImeEnabled();
        boolean active = isImeActive();

        if (active) {
            statusText.setText(R.string.status_active);
            statusText.setTextColor(getResources().getColor(R.color.accent, getTheme()));
        } else if (enabled) {
            statusText.setText(R.string.status_enabled);
            statusText.setTextColor(getResources().getColor(R.color.warning, getTheme()));
        } else {
            statusText.setText(R.string.status_not_enabled);
            statusText.setTextColor(getResources().getColor(R.color.warning, getTheme()));
        }

        FontStyle style = currentStyle();
        previewText.setText(style.transform(getString(R.string.preview_sample)));
    }

    private boolean isImeEnabled() {
        InputMethodManager manager =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (manager == null) {
            return false;
        }
        for (InputMethodInfo info : manager.getEnabledInputMethodList()) {
            if (info.getComponent().getPackageName().equals(getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isImeActive() {
        String active = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD);
        return active != null && active.contains(getPackageName());
    }

    private FontStyle currentStyle() {
        SharedPreferences preferences =
                getSharedPreferences(FontKeyboardService.PREFS_NAME, MODE_PRIVATE);
        return FontStyle.fromOrdinal(preferences.getInt(
                FontKeyboardService.KEY_FONT_STYLE, FontStyle.NORMAL.ordinal()));
    }
}
