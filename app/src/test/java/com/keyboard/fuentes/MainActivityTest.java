package com.keyboard.fuentes;

import android.app.Application;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class MainActivityTest {
    private MainActivity activity;

    @Before public void setUp() {
        Application application = RuntimeEnvironment.getApplication();
        application.getSharedPreferences(FontKeyboardService.PREFS_NAME, Context.MODE_PRIVATE)
                .edit().clear().commit();
        activity = Robolectric.buildActivity(MainActivity.class).setup().get();
    }

    @Test public void activationOpensSystemSettings() {
        assertTrue(activity.findViewById(R.id.enableButton).performClick());
        Intent intent = Shadows.shadowOf(activity).getNextStartedActivity();
        assertEquals(Settings.ACTION_INPUT_METHOD_SETTINGS, intent.getAction());
    }

    @Test public void disabledKeyboardGuidesUserToActivationInsteadOfFailingSilently() {
        assertTrue(activity.findViewById(R.id.chooseButton).performClick());
        assertEquals(Settings.ACTION_INPUT_METHOD_SETTINGS,
                Shadows.shadowOf(activity).getNextStartedActivity().getAction());
        assertTrue(activity.findViewById(R.id.testButton).performClick());
        assertEquals(Settings.ACTION_INPUT_METHOD_SETTINGS,
                Shadows.shadowOf(activity).getNextStartedActivity().getAction());
    }

    @Test public void clearOnlyEnablesWhenThereIsText() {
        EditText input = activity.findViewById(R.id.testInput);
        Button clear = activity.findViewById(R.id.clearButton);
        assertFalse(clear.isEnabled());
        input.setText("Hola");
        assertTrue(clear.isEnabled());
        assertTrue(clear.performClick());
        assertEquals("", input.getText().toString());
        assertFalse(clear.isEnabled());
    }

    @Test public void copyExampleUsesSelectedFont() {
        activity.getSharedPreferences(FontKeyboardService.PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putInt(FontKeyboardService.KEY_FONT_STYLE, FontStyle.BOLD.ordinal()).commit();
        activity.onResume();
        assertTrue(activity.findViewById(R.id.copyButton).performClick());
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        assertEquals(FontStyle.BOLD.transform(activity.getString(R.string.preview_sample)),
                clipboard.getPrimaryClip().getItemAt(0).getText().toString());
    }

    @Test public void hapticsPreferencePersists() {
        Switch toggle = activity.findViewById(R.id.hapticsSwitch);
        assertTrue(toggle.isChecked());
        toggle.setChecked(false);
        assertFalse(activity.getSharedPreferences(FontKeyboardService.PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(FontKeyboardService.KEY_HAPTICS, true));
        activity.finish();
        MainActivity reopened = Robolectric.buildActivity(MainActivity.class).setup().get();
        assertFalse(((Switch) reopened.findViewById(R.id.hapticsSwitch)).isChecked());
        reopened.finish();
    }
}
