package com.keyboard.fuentes;

import android.view.inputmethod.EditorInfo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class EditorActionTest {
    @Test public void mapsEverySupportedEditorAction() {
        assertEquals(EditorAction.GO, EditorAction.fromImeOptions(EditorInfo.IME_ACTION_GO));
        assertEquals(EditorAction.SEARCH, EditorAction.fromImeOptions(EditorInfo.IME_ACTION_SEARCH));
        assertEquals(EditorAction.SEND, EditorAction.fromImeOptions(EditorInfo.IME_ACTION_SEND));
        assertEquals(EditorAction.NEXT, EditorAction.fromImeOptions(EditorInfo.IME_ACTION_NEXT));
        assertEquals(EditorAction.DONE, EditorAction.fromImeOptions(EditorInfo.IME_ACTION_DONE));
        assertEquals(EditorAction.PREVIOUS, EditorAction.fromImeOptions(EditorInfo.IME_ACTION_PREVIOUS));
    }

    @Test public void unspecifiedAndNoneUseEnter() {
        assertEquals(EditorAction.ENTER, EditorAction.fromImeOptions(EditorInfo.IME_ACTION_NONE));
        assertEquals(EditorAction.ENTER, EditorAction.fromImeOptions(EditorInfo.IME_ACTION_UNSPECIFIED));
        assertEquals(EditorAction.ENTER, EditorAction.fromImeOptions(0));
    }

    @Test public void multilineFlagOverridesActionBits() {
        for (EditorAction action : EditorAction.values()) {
            assertEquals(EditorAction.ENTER, EditorAction.fromImeOptions(
                    action.id | EditorInfo.IME_FLAG_NO_ENTER_ACTION));
        }
    }

    @Test public void navigationAndFullscreenFlagsDoNotChangeAction() {
        assertEquals(EditorAction.NEXT, EditorAction.fromImeOptions(EditorInfo.IME_ACTION_NEXT
                | EditorInfo.IME_FLAG_NAVIGATE_NEXT | EditorInfo.IME_FLAG_NO_FULLSCREEN));
    }

    @Test public void everyActionHasAnIconAndAccessibleDescription() {
        for (EditorAction action : EditorAction.values()) {
            assertNotEquals(KeyView.Icon.NONE, action.icon());
            assertNotEquals("", action.description());
        }
    }
}
