package com.keyboard.fuentes;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Vista completa del IME: barra de herramientas, teclas y paneles superpuestos. */
class KeyboardRootView extends LinearLayout implements
        FontPickerView.Listener, EmojiPanelView.Listener, MorePanelView.Listener,
        ClipboardPanelView.Listener {

    private enum Mode {
        KEYS, FONTS, EMOJI, CLIPBOARD, MORE
    }

    private static final Locale SPANISH = Locale.forLanguageTag("es");

    private static final String[] NUMBERS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
    private static final String[] Q_LETTERS = {"q", "w", "e", "r", "t", "y", "u", "i", "o", "p"};
    private static final String[] Q_SYMBOLS = {"+", "×", "÷", "=", "/", "-", "<", ">", "[", "]"};
    private static final String[] A_LETTERS = {"a", "s", "d", "f", "g", "h", "j", "k", "l", "ñ"};
    private static final String[] A_SYMBOLS = {"!", "@", "#", "$", "%", "^", "&", "*", "(", ")"};
    private static final String[] Z_LETTERS = {"z", "x", "c", "v", "b", "n", "m"};
    private static final String[] Z_SYMBOLS = {"-", "'", "\"", ":", ";", ",", "?"};
    private static final String[] SYMBOL_ROW_2 = {"!", "@", "#", "$", "%", "^", "&", "*", "(", ")"};
    private static final String[] SYMBOL_ROW_3 = {"¡", "¿", "-", "_", "=", "+", "[", "]", "{", "}"};
    private static final String[] SYMBOL_ROW_4 = {"\\", "|", ";", ":", "'", "\"", ",", ".", "/", "?"};

    private final FontKeyboardService service;
    private final FrameLayout body;
    private final LinearLayout keyArea;
    private final FontPickerView fontPicker;
    private final EmojiPanelView emojiPanel;
    private final MorePanelView morePanel;
    private final ClipboardPanelView clipboardPanel;
    private final View fontPanelWrapper;
    private final View emojiPanelWrapper;
    private final View morePanelWrapper;
    private final View clipboardPanelWrapper;
    private final List<ToolButtonView> toolButtons = new ArrayList<>();

    private LinearLayout toolbar;
    private ToolButtonView fontButton;
    private ToolButtonView emojiButton;
    private ToolButtonView clipboardButton;
    private ToolButtonView settingsButton;
    private ToolButtonView moreButton;

    private int toolbarHeightPx;
    private int rowGapPx;
    private int toolButtonSizePx;

    private FontStyle style;
    private boolean shift = false;
    private boolean capsLock = false;
    private boolean symbols = false;
    private Mode mode = Mode.KEYS;
    private EditorAction editorAction = EditorAction.ENTER;
    private Runnable backspaceRepeat;

    KeyboardRootView(Context context, FontKeyboardService service) {
        super(context);
        this.service = service;
        this.style = service.getFontStyle();

        toolbarHeightPx = dp(48);
        rowGapPx = dp(3);
        toolButtonSizePx = dp(38);

        setOrientation(VERTICAL);
        setBackgroundColor(PanelUi.color(context, R.color.window_bg));

        addView(createToolbar(), new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, toolbarHeightPx));

        View separator = new View(context);
        separator.setBackgroundColor(PanelUi.color(getContext(), R.color.divider));
        addView(separator, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(1)));

        body = new FrameLayout(context);
        body.setBackgroundColor(PanelUi.color(context, R.color.window_bg));
        addView(body, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));

        keyArea = new LinearLayout(context);
        keyArea.setOrientation(VERTICAL);
        keyArea.setPadding(0, rowGapPx, 0, rowGapPx + dp(2));
        body.addView(keyArea, matchParent());

        fontPicker = new FontPickerView(context, this);
        fontPanelWrapper = scrollablePanel(fontPicker);
        fontPanelWrapper.setVisibility(GONE);
        body.addView(fontPanelWrapper, matchParent());

        emojiPanel = new EmojiPanelView(context, this);
        emojiPanelWrapper = scrollablePanel(emojiPanel);
        emojiPanelWrapper.setVisibility(GONE);
        body.addView(emojiPanelWrapper, matchParent());

        morePanel = new MorePanelView(context, this);
        morePanelWrapper = scrollablePanel(morePanel);
        morePanelWrapper.setVisibility(GONE);
        body.addView(morePanelWrapper, matchParent());

        clipboardPanel = new ClipboardPanelView(context, this);
        clipboardPanelWrapper = scrollablePanel(clipboardPanel);
        clipboardPanelWrapper.setVisibility(GONE);
        body.addView(clipboardPanelWrapper, matchParent());

        buildKeyboard();
        setMode(Mode.KEYS);
    }

    /** Fija un tamaño compacto y centrado abajo; las filas solo se reparten ese alto. */
    void applyCompactBounds(int widthPx, int heightPx, int toolbarPx, int gapPx) {
        toolbarHeightPx = Math.max(dp(30), toolbarPx);
        rowGapPx = Math.max(1, gapPx);
        toolButtonSizePx = Math.max(dp(24), toolbarHeightPx - dp(6));

        ViewGroup.LayoutParams params = getLayoutParams();
        if (params instanceof FrameLayout.LayoutParams) {
            params.width = widthPx;
            params.height = heightPx;
            ((FrameLayout.LayoutParams) params).gravity =
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            setLayoutParams(params);
        } else {
            setLayoutParams(new FrameLayout.LayoutParams(widthPx, heightPx,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL));
        }

        if (toolbar != null) {
            ViewGroup.LayoutParams toolbarParams = toolbar.getLayoutParams();
            if (toolbarParams != null) {
                toolbarParams.height = toolbarHeightPx;
                toolbar.setLayoutParams(toolbarParams);
            }
        }
        applyToolButtonSizes();
        keyArea.setPadding(0, rowGapPx, 0, rowGapPx + dp(2));
        buildKeyboard();
        requestLayout();
    }

    private void applyToolButtonSizes() {
        for (ToolButtonView button : toolButtons) {
            ViewGroup.LayoutParams params = button.getLayoutParams();
            if (params != null) {
                params.width = toolButtonSizePx;
                params.height = toolButtonSizePx;
                button.setLayoutParams(params);
            }
        }
    }

    private View scrollablePanel(View panel) {
        ScrollView scroll = new ScrollView(getContext());
        scroll.setBackgroundColor(PanelUi.color(getContext(), R.color.panel));
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.addView(panel, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        return scroll;
    }

    private LinearLayout createToolbar() {
        toolbar = new LinearLayout(getContext());
        toolbar.setOrientation(HORIZONTAL);
        toolbar.setBackgroundColor(PanelUi.color(getContext(), R.color.window_bg));
        toolbar.setPadding(dp(6), 0, dp(6), 0);

        fontButton = addToolButton(toolbar, ToolButtonView.Icon.FONT, "Cambiar fuente");
        emojiButton = addToolButton(toolbar, ToolButtonView.Icon.EMOJI, "Abrir emojis");
        clipboardButton = addToolButton(toolbar, ToolButtonView.Icon.CLIPBOARD, "Abrir portapapeles");
        settingsButton = addToolButton(toolbar, ToolButtonView.Icon.SETTINGS, "Ajustes del teclado");
        moreButton = addToolButton(toolbar, ToolButtonView.Icon.MORE, "Más opciones");

        fontButton.setOnClickListener(view -> {
            tap(view);
            toggleMode(Mode.FONTS);
        });
        emojiButton.setOnClickListener(view -> {
            tap(view);
            toggleMode(Mode.EMOJI);
        });
        clipboardButton.setOnClickListener(view -> {
            tap(view);
            toggleMode(Mode.CLIPBOARD);
        });
        settingsButton.setOnClickListener(view -> {
            tap(view);
            service.openSettings();
        });
        clipboardButton.setOnLongClickListener(view -> {
            tap(view);
            service.pasteClipboard(true);
            return true;
        });
        moreButton.setOnClickListener(view -> {
            tap(view);
            toggleMode(Mode.MORE);
        });
        return toolbar;
    }

    private ToolButtonView addToolButton(LinearLayout toolbar, ToolButtonView.Icon icon,
                                         String description) {
        FrameLayout cell = new FrameLayout(getContext());
        ToolButtonView button = new ToolButtonView(getContext(), icon);
        button.configure(icon, description);
        toolButtons.add(button);
        cell.addView(button, new FrameLayout.LayoutParams(
                toolButtonSizePx, toolButtonSizePx, Gravity.CENTER));
        toolbar.addView(cell, new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
        return button;
    }

    private void setMode(Mode nextMode) {
        stopBackspaceRepeat();
        mode = nextMode;
        keyArea.setVisibility(mode == Mode.KEYS ? VISIBLE : GONE);
        fontPanelWrapper.setVisibility(mode == Mode.FONTS ? VISIBLE : GONE);
        emojiPanelWrapper.setVisibility(mode == Mode.EMOJI ? VISIBLE : GONE);
        morePanelWrapper.setVisibility(mode == Mode.MORE ? VISIBLE : GONE);
        clipboardPanelWrapper.setVisibility(mode == Mode.CLIPBOARD ? VISIBLE : GONE);

        if (mode == Mode.FONTS) {
            fontPicker.setCurrentStyle(style);
        } else if (mode == Mode.CLIPBOARD) {
            clipboardPanel.refresh(service.readClipboard());
            clipboardPanelWrapper.scrollTo(0, 0);
        } else if (mode == Mode.MORE) {
            morePanel.refresh(style, symbols);
        }
        updateToolbar();
    }

    private void toggleMode(Mode target) {
        setMode(mode == target ? Mode.KEYS : target);
    }

    private void updateToolbar() {
        fontButton.setActive(mode == Mode.FONTS || style != FontStyle.NORMAL);
        fontButton.setContentDescription("Cambiar fuente. Actual: " + style.getLabel());
        emojiButton.setActive(mode == Mode.EMOJI);
        clipboardButton.setActive(mode == Mode.CLIPBOARD);
        moreButton.setActive(mode == Mode.MORE);
    }

    void resetForNewInput(android.view.inputmethod.EditorInfo info) {
        editorAction = EditorAction.fromImeOptions(info == null ? 0 : info.imeOptions);
        style = service.getFontStyle();
        shift = false;
        capsLock = false;
        symbols = false;
        buildKeyboard();
        setMode(Mode.KEYS);
    }

    boolean handleBack() {
        stopBackspaceRepeat();
        if (mode != Mode.KEYS) {
            setMode(Mode.KEYS);
            return true;
        }
        if (symbols || shift || capsLock) {
            symbols = false;
            shift = false;
            capsLock = false;
            buildKeyboard();
            return true;
        }
        return false;
    }

    void stopRepeating() {
        stopBackspaceRepeat();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopBackspaceRepeat();
        super.onDetachedFromWindow();
    }

    private void buildKeyboard() {
        stopBackspaceRepeat();
        keyArea.removeAllViews();
        if (symbols) {
            buildSymbolsLayout();
        } else {
            buildLettersLayout();
        }
    }

    private void buildLettersLayout() {
        LinearLayout numberRow = newRow();
        for (String number : NUMBERS) {
            addKey(numberRow, characterKey(number, "", 1f, KeyView.Appearance.NORMAL), 1f);
        }

        LinearLayout qRow = newRow();
        for (int i = 0; i < Q_LETTERS.length; i++) {
            addKey(qRow, letterKey(Q_LETTERS[i], Q_SYMBOLS[i]), 1f);
        }

        LinearLayout aRow = newRow();
        for (int i = 0; i < A_LETTERS.length; i++) {
            addKey(aRow, letterKey(A_LETTERS[i], A_SYMBOLS[i]), 1f);
        }

        LinearLayout zRow = newRow();
        addKey(zRow, shiftKey(), 1.35f);
        for (int i = 0; i < Z_LETTERS.length; i++) {
            addKey(zRow, letterKey(Z_LETTERS[i], Z_SYMBOLS[i]), 1f);
        }
        addKey(zRow, backspaceKey(), 1.35f);

        LinearLayout bottomRow = newRow();
        addKey(bottomRow, symbolsToggleKey(), 1.35f);
        addKey(bottomRow, characterKey(",", "", 1f, KeyView.Appearance.NORMAL), 1f);
        addKey(bottomRow, spaceKey(), 5.2f);
        addKey(bottomRow, characterKey(".", "", 1f, KeyView.Appearance.NORMAL), 1f);
        addKey(bottomRow, editorActionKey(), 1.35f);
    }

    private void buildSymbolsLayout() {
        LinearLayout numberRow = newRow();
        for (String number : NUMBERS) {
            addKey(numberRow, characterKey(number, "", 1f, KeyView.Appearance.NORMAL), 1f);
        }

        LinearLayout row2 = newRow();
        for (String value : SYMBOL_ROW_2) {
            addKey(row2, characterKey(value, "", 1f, KeyView.Appearance.NORMAL), 1f);
        }

        LinearLayout row3 = newRow();
        for (String value : SYMBOL_ROW_3) {
            addKey(row3, characterKey(value, "", 1f, KeyView.Appearance.NORMAL), 1f);
        }

        LinearLayout row4 = newRow();
        for (int i = 0; i < SYMBOL_ROW_4.length; i++) {
            String secondary = "";
            if (SYMBOL_ROW_4[i].equals(",")) {
                secondary = "<";
            } else if (SYMBOL_ROW_4[i].equals(".")) {
                secondary = ">";
            }
            addKey(row4, characterKey(SYMBOL_ROW_4[i], secondary, 1f, KeyView.Appearance.NORMAL), 1f);
        }

        LinearLayout bottomRow = newRow();
        addKey(bottomRow, symbolsToggleKey(), 1.35f);
        addKey(bottomRow, characterKey("<", "", 1f, KeyView.Appearance.NORMAL), 1f);
        addKey(bottomRow, spaceKey(), 5.2f);
        addKey(bottomRow, backspaceKey(), 1.35f);
        addKey(bottomRow, editorActionKey(), 1.35f);
    }

    private LinearLayout newRow() {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setPadding(rowGapPx, rowGapPx, rowGapPx, rowGapPx);
        keyArea.addView(row, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));
        return row;
    }

    private void addKey(LinearLayout row, View key, float weight) {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, weight);
        params.setMargins(rowGapPx, 0, rowGapPx, 0);
        row.addView(key, params);
    }

    private KeyView characterKey(String value, String secondary, float weight,
                                 KeyView.Appearance appearance) {
        KeyView key = new KeyView(getContext());
        key.configure(value, secondary, KeyView.Icon.NONE, appearance, false, value);
        key.setOnClickListener(view -> {
            tap(view);
            service.commitTyped(value);
        });
        key.setOnLongClickListener(view -> {
            if (!secondary.isEmpty()) {
                tap(view);
                service.commitText(secondary);
                return true;
            }
            return false;
        });
        key.setContentDescription("Tecla " + value
                + (secondary.isEmpty() ? "" : ", mantén pulsada para " + secondary));
        return key;
    }

    private KeyView letterKey(String lower, String secondary) {
        String display = isUppercase() ? lower.toUpperCase(SPANISH) : lower;
        KeyView key = characterKey(display, secondary, 1f, KeyView.Appearance.NORMAL);
        key.setOnClickListener(view -> {
            tap(view);
            service.commitTyped(display);
            if (shift && !capsLock) {
                shift = false;
                buildKeyboard();
            }
        });
        return key;
    }

    private KeyView shiftKey() {
        KeyView key = new KeyView(getContext());
        key.configure("", "", capsLock ? KeyView.Icon.SHIFT_LOCK : KeyView.Icon.SHIFT, KeyView.Appearance.SPECIAL,
                shift || capsLock, capsLock ? "Bloqueo de mayúsculas activado. Pulsa para desactivar"
                        : "Mayúsculas. Mantén pulsado para bloquear");
        key.setActive(shift || capsLock);
        key.setOnClickListener(view -> {
            tap(view);
            if (capsLock) {
                capsLock = false;
                shift = false;
            } else if (shift) {
                capsLock = true;
                shift = true;
            } else {
                shift = true;
            }
            buildKeyboard();
        });
        key.setOnLongClickListener(view -> {
            tap(view);
            capsLock = !capsLock;
            shift = capsLock;
            buildKeyboard();
            return true;
        });
        return key;
    }

    private KeyView backspaceKey() {
        KeyView key = new KeyView(getContext());
        key.configure("", "", KeyView.Icon.BACKSPACE, KeyView.Appearance.SPECIAL,
                false, "Borrar. Mantén pulsada para borrar repetidamente");
        key.setOnClickListener(view -> {
            tap(view);
            service.deleteBackward();
        });
        key.setOnLongClickListener(view -> {
            tap(view);
            startBackspaceRepeat();
            return true;
        });
        key.setOnTouchListener((view, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                stopBackspaceRepeat();
            }
            return false;
        });
        return key;
    }

    private void startBackspaceRepeat() {
        stopBackspaceRepeat();
        backspaceRepeat = new Runnable() {
            @Override
            public void run() {
                service.deleteBackward();
                postDelayed(this, 55);
            }
        };
        backspaceRepeat.run();
    }

    private void stopBackspaceRepeat() {
        if (backspaceRepeat != null) {
            removeCallbacks(backspaceRepeat);
            backspaceRepeat = null;
        }
    }

    private KeyView spaceKey() {
        KeyView key = new KeyView(getContext());
        key.configure("Español (US)", "", KeyView.Icon.NONE, KeyView.Appearance.SPACE,
                false, "Barra espaciadora. Idioma Español (US)");
        key.setOnClickListener(view -> {
            tap(view);
            service.commitText(" ");
        });
        return key;
    }

    private KeyView symbolsToggleKey() {
        String label = symbols ? "ABC" : "!#1";
        KeyView key = characterKey(label, "", 1.35f, KeyView.Appearance.SPECIAL);
        key.setContentDescription(symbols ? "Volver a las letras" : "Abrir símbolos");
        key.setOnClickListener(view -> {
            tap(view);
            symbols = !symbols;
            buildKeyboard();
        });
        return key;
    }

    private KeyView editorActionKey() {
        KeyView key = new KeyView(getContext());
        key.configure("", "", editorAction.icon(), KeyView.Appearance.SPECIAL,
                false, editorAction.description());
        key.setOnClickListener(view -> {
            tap(view);
            service.sendEnter();
        });
        key.setOnLongClickListener(view -> {
            tap(view);
            service.commitText("\n");
            return true;
        });
        return key;
    }

    private boolean isUppercase() {
        return shift || capsLock;
    }

    private void tap(View view) {
        service.tap(view);
    }

    private FrameLayout.LayoutParams matchParent() {
        return new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onStyleSelected(FontStyle style) {
        this.style = style == null ? FontStyle.NORMAL : style;
        service.setFontStyle(this.style);
        fontPicker.setCurrentStyle(this.style);
        updateToolbar();
        service.toast("Fuente: " + this.style.getLabel());
        setMode(Mode.KEYS);
    }

    @Override
    public void onEmoji(String emoji) {
        tap(emojiPanel);
        service.commitText(emoji);
    }

    @Override
    public void onOpenFonts() {
        setMode(Mode.FONTS);
    }

    @Override
    public void onOpenEmojis() {
        setMode(Mode.EMOJI);
    }

    @Override
    public void onOpenClipboard() {
        setMode(Mode.CLIPBOARD);
    }

    @Override
    public void onPasteClipboard(boolean styled) {
        if (service.pasteClipboard(styled)) {
            setMode(Mode.KEYS);
        } else {
            clipboardPanel.refresh(null);
        }
    }

    @Override
    public void onCopySelection() {
        service.copySelection();
    }

    @Override
    public void onRefreshClipboard() {
        clipboardPanel.refresh(service.readClipboard());
    }

    @Override
    public void onDelete() {
        tap(emojiPanel);
        service.deleteBackward();
    }

    @Override
    public void onHideKeyboard() {
        setMode(Mode.KEYS);
        service.hideKeyboard();
    }

    @Override
    public void onToggleSymbols() {
        symbols = !symbols;
        buildKeyboard();
        setMode(Mode.KEYS);
    }

    @Override
    public void onOpenSettings() {
        service.openSettings();
    }

    @Override
    public void onClose() {
        setMode(Mode.KEYS);
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
