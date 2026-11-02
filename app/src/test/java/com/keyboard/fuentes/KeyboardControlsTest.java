package com.keyboard.fuentes;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class KeyboardControlsTest {
    private TestKeyboardService service;
    private KeyboardRootView root;
    private EditText editor;
    private RecordingConnection connection;

    public static class TestKeyboardService extends FontKeyboardService {
        InputConnection connection;
        EditorInfo info = new EditorInfo();
        int hidden;

        @Override public InputConnection getCurrentInputConnection() { return connection; }
        @Override public EditorInfo getCurrentInputEditorInfo() { return info; }
        @Override void hideKeyboard() { hidden++; }
    }

    private static class RecordingConnection extends BaseInputConnection {
        int action = -1;
        boolean handled = true;
        int batches;
        private final EditText editor;
        RecordingConnection(EditText editor) {
            super(editor, true);
            this.editor = editor;
        }
        @Override public boolean commitText(CharSequence text, int newCursorPosition) {
            android.text.Editable editable = editor.getText();
            int start = Math.max(0, Math.min(android.text.Selection.getSelectionStart(editable), editable.length()));
            int end = Math.max(0, Math.min(android.text.Selection.getSelectionEnd(editable), editable.length()));
            editable.replace(Math.min(start, end), Math.max(start, end), text);
            return true;
        }
        @Override public CharSequence getTextBeforeCursor(int length, int flags) {
            android.text.Editable editable = editor.getText();
            int end = Math.max(0, android.text.Selection.getSelectionEnd(editable));
            return editable.subSequence(Math.max(0, end - length), end);
        }
        @Override public CharSequence getSelectedText(int flags) {
            android.text.Editable editable = editor.getText();
            int start = android.text.Selection.getSelectionStart(editable);
            int end = android.text.Selection.getSelectionEnd(editable);
            return start >= 0 && end > start ? editable.subSequence(start, end) : null;
        }
        @Override public boolean deleteSurroundingText(int before, int after) {
            android.text.Editable editable = editor.getText();
            int cursor = android.text.Selection.getSelectionEnd(editable);
            editable.delete(Math.max(0, cursor - before), cursor);
            return true;
        }
        @Override public boolean performEditorAction(int editorAction) {
            action = editorAction;
            return handled;
        }
        @Override public boolean beginBatchEdit() { batches++; return true; }
        @Override public boolean endBatchEdit() { batches--; return true; }
    }

    @Before public void setUp() {
        Application application = RuntimeEnvironment.getApplication();
        application.setTheme(R.style.AppTheme);
        org.robolectric.android.controller.ServiceController<TestKeyboardService> controller =
                Robolectric.buildService(TestKeyboardService.class);
        service = controller.get();
        service.setTheme(R.style.AppTheme);
        controller.create();
        editor = new EditText(service);
        editor.setSelection(0);
        connection = new RecordingConnection(editor);
        service.connection = connection;
        service.getSharedPreferences(FontKeyboardService.PREFS_NAME, Context.MODE_PRIVATE)
                .edit().clear().commit();
        service.setFontStyle(FontStyle.NORMAL);
        root = new KeyboardRootView(service, service);
    }

    @Test public void toolbarOpensAndClosesEveryPanel() {
        int[] tools = {0, 1, 2, 4};
        String[] panels = {"Elige la fuente", "Emojis", "Portapapeles", "Más opciones"};
        for (int i = 0; i < tools.length; i++) {
            clickTool(tools[i]);
            assertTrue("Panel did not open: " + panels[i], visibleText(root, panels[i]));
            clickTool(tools[i]);
            assertFalse(visibleText(root, panels[i]));
        }
        click(root, "Ajustes del teclado");
        Intent intent = org.robolectric.Shadows.shadowOf(service).getNextStartedActivity();
        assertEquals(MainActivity.class.getName(), intent.getComponent().getClassName());
    }

    @Test public void fontSelectionWritesStyledTextAndPersists() {
        clickTool(0);
        click(root, "Usar fuente Negrita");
        click(root, "Tecla a");
        assertEquals("𝐚", editor.getText().toString());
        assertEquals(FontStyle.BOLD, service.getFontStyle());
        assertEquals(FontStyle.BOLD.ordinal(), service.getSharedPreferences(
                FontKeyboardService.PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(FontKeyboardService.KEY_FONT_STYLE, -1));
    }

    @Test public void shiftLongPressAndSymbolsWork() {
        View shift = find(root, "Mayúsculas. Mantén pulsado para bloquear");
        shift.performClick();
        click(root, "Tecla Q");
        assertEquals("Q", editor.getText().toString());
        click(root, "Tecla w");
        assertEquals("Qw", editor.getText().toString());
        find(root, "Mayúsculas. Mantén pulsado para bloquear").performLongClick();
        click(root, "Tecla A");
        click(root, "Tecla B");
        assertEquals("QwAB", editor.getText().toString());
        click(root, "Bloqueo de mayúsculas activado. Pulsa para desactivar");
        click(root, "Abrir símbolos");
        click(root, "Tecla @");
        assertEquals("QwAB@", editor.getText().toString());
        click(root, "Volver a las letras");
        click(root, "Tecla z");
        assertEquals("QwAB@z", editor.getText().toString());
    }

    @Test public void secondarySymbolLongPressDoesNotInsertPrimaryKey() {
        assertTrue(find(root, "Tecla a, mantén pulsada para !").performLongClick());
        assertEquals("!", editor.getText().toString());
    }

    @Test public void emojiInsertDeleteAndCloseWork() {
        click(root, "Abrir emojis");
        click(root, "Insertar emoji 😀");
        assertEquals("😀", editor.getText().toString());
        click(root, "Borrar último carácter");
        assertEquals("", editor.getText().toString());
        click(root, "Volver al teclado");
        assertFalse(visibleText(root, "Toca un emoji para insertarlo. El panel permanece abierto."));
    }

    @Test public void clipboardSupportsStyledAndOriginalPasteAndCopy() {
        ClipboardManager clipboard = (ClipboardManager) service.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("test", "Aa"));
        service.setFontStyle(FontStyle.BOLD);
        click(root, "Abrir portapapeles");
        click(root, "Pegar texto original");
        assertEquals("Aa", editor.getText().toString());
        click(root, "Abrir portapapeles");
        click(root, "Pegar con la fuente elegida");
        assertEquals("Aa𝐀𝐚", editor.getText().toString());
        editor.setText("copiar");
        editor.setSelection(1, 4);
        click(root, "Abrir portapapeles");
        click(root, "Copiar texto seleccionado");
        assertEquals("opi", clipboard.getPrimaryClip().getItemAt(0).getText().toString());
        click(root, "Actualizar portapapeles");
        click(root, "Cerrar portapapeles");
        assertFalse(visibleText(root, "Pegar texto original"));
    }

    @Test public void emptyClipboardDisablesPaste() {
        ClipboardManager clipboard = (ClipboardManager) service.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.clearPrimaryClip();
        click(root, "Abrir portapapeles");
        assertFalse(find(root, "Pegar texto original").isEnabled());
        assertFalse(find(root, "Pegar con la fuente elegida").isEnabled());
    }

    @Test public void moreShortcutsAndHideWork() {
        click(root, "Más opciones");
        click(root, "Ocultar teclado");
        assertEquals(1, service.hidden);
        click(root, "Más opciones");
        click(root, "Portapapeles");
        assertTrue(visibleText(root, "Pegar texto original"));
        click(root, "Más opciones");
        click(root, "Elegir fuente");
        assertTrue(visibleText(root, "Elige la fuente"));
        root.handleBack();
        click(root, "Más opciones");
        click(root, "Emojis");
        assertTrue(visibleDescription(root, "Insertar emoji 😀"));
    }

    @Test public void actionKeyUsesCurrentEditorAndMultilineFlag() {
        service.info.imeOptions = EditorInfo.IME_ACTION_SEARCH;
        root.resetForNewInput(service.info);
        click(root, EditorAction.SEARCH.description());
        assertEquals(EditorInfo.IME_ACTION_SEARCH, connection.action);
        service.info.imeOptions = EditorInfo.IME_ACTION_SEND | EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        root.resetForNewInput(service.info);
        click(root, EditorAction.ENTER.description());
        assertEquals("\n", editor.getText().toString());
    }

    @Test public void deleteHandlesSelectionAndUnicodeInBatches() {
        editor.getText().replace(0, editor.length(), "a😀");
        editor.setSelection(2);
        service.deleteBackward();
        assertEquals("a", editor.getText().toString());
        editor.getText().replace(0, editor.length(), "👨‍👩‍👧‍👦");
        editor.setSelection(editor.length());
        service.deleteBackward();
        assertEquals("", editor.getText().toString());
        editor.setText("hola");
        editor.setSelection(1, 3);
        service.deleteBackward();
        assertEquals("ha", editor.getText().toString());
        assertEquals(0, connection.batches);
    }

    private void clickTool(int index) {
        java.util.List<ToolButtonView> buttons = new java.util.ArrayList<>();
        collectTools(root, buttons);
        assertTrue(buttons.get(index).performClick());
    }

    private static void collectTools(View view, java.util.List<ToolButtonView> buttons) {
        if (view instanceof ToolButtonView) {
            buttons.add((ToolButtonView) view);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                collectTools(((ViewGroup) view).getChildAt(i), buttons);
            }
        }
    }

    private static void click(View root, String description) {
        View view = find(root, description);
        if (view == null && description.startsWith("Tecla ") && description.length() == 7) {
            view = find(root, description + ", mantén pulsada para ", true);
        }
        assertNotNull("Missing control: " + description, view);
        assertTrue("Disabled control: " + description, view.performClick());
    }

    private static View find(View view, String description) {
        return find(view, description, false);
    }

    private static View find(View view, String description, boolean prefix) {
        CharSequence content = view.getContentDescription();
        if (content != null && (prefix ? content.toString().startsWith(description)
                : description.contentEquals(content.toString()))) {
            return view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View found = find(group.getChildAt(i), description, prefix);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static boolean visibleDescription(View view, String description) {
        if (view.getVisibility() != View.VISIBLE) {
            return false;
        }
        if (view.getContentDescription() != null
                && description.contentEquals(view.getContentDescription().toString())) {
            return true;
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                if (visibleDescription(((ViewGroup) view).getChildAt(i), description)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean visibleText(View view, String text) {
        if (view.getVisibility() != View.VISIBLE) {
            return false;
        }
        if (view instanceof android.widget.TextView && text.equals(((android.widget.TextView) view).getText().toString())) {
            return true;
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                if (visibleText(((ViewGroup) view).getChildAt(i), text)) {
                    return true;
                }
            }
        }
        return false;
    }
}
