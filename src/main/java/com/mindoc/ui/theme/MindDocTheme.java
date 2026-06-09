package com.mindoc.ui.theme;

/**
 * MindDoc Theme - Clean modern green palette
 */
public class MindDocTheme {

    // Primary Colors
    public static final String PRIMARY = "#10B981";
    public static final String SECONDARY = "#34D399";
    public static final String PRIMARY_DARK = "#0D8659";
    public static final String ACCENT = "#34D399";

    // Background & Surfaces
    public static final String BACKGROUND = "#F8FAFC";
    public static final String SURFACE = "#FFFFFF";
    public static final String SURFACE_HOVER = "#F1F5F9";

    // Text
    public static final String TEXT_PRIMARY = "#1F2937";
    public static final String TEXT_SECONDARY = "#6b7280";
    public static final String TEXT_MUTED = "#9ca3af";

    // Borders
    public static final String BORDER = "#e5e7eb";
    public static final String BORDER_FOCUS = "#10B981";

    // UI States
    public static final String SUCCESS = "#10B981";
    public static final String WARNING = "#f59e0b";
    public static final String DANGER = "#ef4444";
    public static final String INFO = "#3b82f6";

    // Mood Colors
    public static final String MOOD_EXCELLENT = "#10B981";
    public static final String MOOD_GOOD = "#3b82f6";
    public static final String MOOD_NEUTRAL = "#f59e0b";
    public static final String MOOD_BAD = "#f97316";
    public static final String MOOD_TERRIBLE = "#ef4444";

    // Dark Mode
    public static final String DARK_BACKGROUND = "#0f172a";
    public static final String DARK_SURFACE = "#1e293b";
    public static final String DARK_TEXT_PRIMARY = "#f8fafc";
    public static final String DARK_TEXT_SECONDARY = "#cbd5e1";
    public static final String DARK_BORDER = "#334155";

    /**
     * Generate CSS stylesheet based on theme (light or dark)
     */
    public static String getStylesheet(String theme) {
        boolean isDark = "Dark".equalsIgnoreCase(theme);

        String bg            = isDark ? DARK_BACKGROUND    : BACKGROUND;
        String surface       = isDark ? DARK_SURFACE       : SURFACE;
        String text          = isDark ? DARK_TEXT_PRIMARY  : TEXT_PRIMARY;
        String textSecondary = isDark ? DARK_TEXT_SECONDARY : TEXT_SECONDARY;
        String border        = isDark ? DARK_BORDER        : BORDER;

        // Tab active background — slightly lighter for dark mode
        String tabActiveBg   = isDark ? "#1a9e75" : PRIMARY;

        return "* { -fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif; }\n" +

                // ── Root ─────────────────────────────────────────────────────────
                ".root { " +
                "-fx-background-color: " + bg + "; " +
                "-fx-text-fill: " + text + "; " +
                "}\n" +

                // ── Buttons ──────────────────────────────────────────────────────
                ".button { " +
                "-fx-padding: 10 20; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: 500; " +
                "-fx-cursor: hand; " +
                "-fx-background-radius: 6; " +
                "-fx-background-color: " + PRIMARY + "; " +
                "-fx-text-fill: white; " +
                "-fx-effect: dropshadow(three-pass-box, #0000001A, 4, 0, 0, 2); " +
                "-fx-border-radius: 6; " +
                "}\n" +
                ".button:hover { " +
                "-fx-background-color: " + PRIMARY_DARK + "; " +
                "-fx-effect: dropshadow(three-pass-box, #00000026, 8, 0, 0, 4); " +
                "}\n" +
                ".button:pressed { " +
                "-fx-background-color: #095C3F; " +
                "}\n" +
                ".button:focused { " +
                "-fx-border-color: transparent; " +
                "-fx-focus-color: transparent; " +
                "-fx-faint-focus-color: transparent; " +
                "-fx-effect: dropshadow(three-pass-box, #0000001A, 4, 0, 0, 2); " +
                "}\n" +
                ".button:default { " +
                "-fx-background-color: " + PRIMARY + "; " +
                "-fx-text-fill: white; " +
                "}\n" +
                ".button:default:hover { " +
                "-fx-background-color: " + PRIMARY_DARK + "; " +
                "}\n" +

                // ── Labels ───────────────────────────────────────────────────────
                ".label { " +
                "-fx-font-size: 14px; " +
                "-fx-text-fill: " + text + "; " +
                "}\n" +
                ".label:header { " +
                "-fx-font-size: 18px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: " + text + "; " +
                "}\n" +

                // ── Tab Pane — новий стиль навігації ─────────────────────────────
                // Загальний контейнер табів
                ".tab-pane { " +
                "-fx-background-color: " + bg + "; " +
                "}\n" +
                // Фон хедера табів (вся смужка зверху)
                ".tab-pane .tab-header-area { " +
                "-fx-background-color: " + tabActiveBg + "; " +
                "-fx-padding: 0; " +
                "}\n" +
                ".tab-pane .tab-header-background { " +
                "-fx-background-color: " + tabActiveBg + "; " +
                "}\n" +
                // Окремий таб — неактивний
                ".tab-pane .tab { " +
                "-fx-padding: 10 16; " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: 400; " +
                "-fx-background-color: transparent; " +
                "-fx-text-fill: rgba(255,255,255,0.72); " +
                "-fx-background-radius: 6 6 0 0; " +
                "-fx-border-color: transparent; " +
                "}\n" +
                // Hover неактивного табу
                ".tab-pane .tab:hover:not(:selected) { " +
                "-fx-background-color: #FFFFFF1E; " +
                "-fx-text-fill: white; " +
                "}\n" +
                // Активний таб — напівпрозорий білий фон
                ".tab-pane .tab:selected { " +
                "-fx-background-color: #FFFFFF38; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: 500; " +
                "}\n" +
                // Прибираємо стандартний фокус-індикатор
                ".tab-pane:focused > .tab-header-area > .headers-region > .tab:selected .focus-indicator { " +
                "-fx-border-color: transparent; " +
                "-fx-border-width: 0; " +
                "}\n" +
                ".tab-pane .tab:selected .focus-indicator { " +
                "-fx-border-color: transparent; " +
                "-fx-border-width: 0; " +
                "}\n" +
                ".tab-pane .tab .focus-indicator { " +
                "-fx-border-color: transparent; " +
                "-fx-border-width: 0; " +
                "}\n" +
                // Прибираємо нижню лінію під табами
                ".tab-pane .tab-header-area .tab-header-background { " +
                "-fx-border-width: 0; " +
                "}\n" +

                // ── Scroll Pane ──────────────────────────────────────────────────
                ".scroll-pane { " +
                "-fx-background-color: " + bg + "; " +
                "}\n" +
                ".scroll-pane:focused { " +
                "-fx-background-color: " + bg + "; " +
                "}\n" +
                ".scroll-pane .viewport { " +
                "-fx-background-color: " + bg + "; " +
                "}\n" +

                // ── Text Field ───────────────────────────────────────────────────
                ".text-field { " +
                "-fx-padding: 10 12; " +
                "-fx-border-color: " + border + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 6; " +
                "-fx-background-color: " + surface + "; " +
                "-fx-background-radius: 6; " +
                "-fx-text-fill: " + text + "; " +
                "-fx-prompt-text-fill: " + textSecondary + "; " +
                "-fx-font-size: 14px; " +
                "}\n" +
                ".text-field:focused { " +
                "-fx-border-color: " + PRIMARY + "; " +
                "-fx-border-width: 2; " +
                "}\n" +

                // ── Text Area ────────────────────────────────────────────────────
                ".text-area { " +
                "-fx-padding: 10 12; " +
                "-fx-border-color: " + border + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 6; " +
                "-fx-background-color: " + surface + "; " +
                "-fx-background-radius: 6; " +
                "-fx-text-fill: " + text + "; " +
                "-fx-control-inner-background: " + surface + "; " +
                "-fx-prompt-text-fill: " + textSecondary + "; " +
                "-fx-font-size: 14px; " +
                "}\n" +
                ".text-area:focused { " +
                "-fx-border-color: " + PRIMARY + "; " +
                "-fx-border-width: 2; " +
                "}\n" +

                // ── Combo Box ────────────────────────────────────────────────────
                ".combo-box { " +
                "-fx-border-color: " + border + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 6; " +
                "-fx-background-color: " + surface + "; " +
                "-fx-background-radius: 6; " +
                "-fx-text-fill: " + text + "; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 8 12; " +
                "}\n" +
                ".combo-box:focused { " +
                "-fx-border-color: " + PRIMARY + "; " +
                "-fx-border-width: 2; " +
                "}\n" +
                ".combo-box-popup .list-view { " +
                "-fx-background-color: " + surface + "; " +
                "-fx-text-fill: " + text + "; " +
                "}\n" +
                ".combo-box-popup .list-view .list-cell { " +
                "-fx-text-fill: " + text + "; " +
                "-fx-background-color: " + surface + "; " +
                "-fx-padding: 8 12; " +
                "}\n" +
                ".combo-box-popup .list-view .list-cell:filled:selected { " +
                "-fx-background-color: " + PRIMARY + "; " +
                "-fx-text-fill: white; " +
                "}\n" +

                // ── Progress Bar ─────────────────────────────────────────────────
                ".progress-bar { " +
                "-fx-accent: " + PRIMARY + "; " +
                "}\n" +

                // ── Menu Bar ─────────────────────────────────────────────────────
                ".menu-bar { " +
                "-fx-background-color: " + PRIMARY_DARK + "; " +
                "-fx-padding: 2 8; " +
                "}\n" +
                ".menu-bar .menu .label { " +
                "-fx-text-fill: rgba(255,255,255,0.85); " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: 400; " +
                "}\n" +
                ".menu-bar .menu:hover .label, " +
                ".menu-bar .menu:showing .label { " +
                "-fx-text-fill: white; " +
                "}\n" +
                ".menu-bar .menu:hover, " +
                ".menu-bar .menu:showing { " +
                "-fx-background-color: #FFFFFF1A; " +
                "}\n" +
                ".menu-item { " +
                "-fx-padding: 10 20; " +
                "-fx-background-color: " + surface + "; " +
                "-fx-text-fill: " + text + "; " +
                "-fx-font-size: 13px; " +
                "}\n" +
                ".context-menu { " +
                "-fx-background-color: " + surface + "; " +
                "-fx-text-fill: " + text + "; " +
                "-fx-background-radius: 6; " +
                "-fx-effect: dropshadow(three-pass-box, #00000026, 8, 0, 0, 4); " +
                "}\n" +
                ".context-menu .menu-item .label { " +
                "-fx-text-fill: " + text + "; " +
                "}\n" +
                ".menu-item:focused { " +
                "-fx-background-color: #E1F5EE; " +
                "}\n" +
                ".context-menu .menu-item:focused .label { " +
                "-fx-text-fill: " + PRIMARY_DARK + "; " +
                "}\n" +

                // ── List View ────────────────────────────────────────────────────
                ".list-view { " +
                "-fx-background-color: " + surface + "; " +
                "-fx-control-inner-background: " + surface + "; " +
                "-fx-text-fill: " + text + "; " +
                "}\n" +
                ".list-cell { " +
                "-fx-text-fill: " + text + "; " +
                "-fx-background-color: " + surface + "; " +
                "-fx-padding: 8 12; " +
                "}\n" +
                ".list-cell:filled:selected { " +
                "-fx-background-color: " + PRIMARY + "; " +
                "-fx-text-fill: white; " +
                "}\n" +

                // ── Slider ───────────────────────────────────────────────────────
                ".slider { " +
                "-fx-control-inner-background: " + surface + "; " +
                "}\n" +
                ".slider .thumb { " +
                "-fx-background-color: " + PRIMARY + "; " +
                "}\n" +
                ".slider .track { " +
                "-fx-background-color: " + border + "; " +
                "}\n" +

                // ── Checkbox ─────────────────────────────────────────────────────
                ".check-box { " +
                "-fx-text-fill: " + text + "; " +
                "-fx-font-size: 14px; " +
                "}\n" +
                ".check-box .box { " +
                "-fx-border-color: " + border + "; " +
                "-fx-background-color: " + surface + "; " +
                "-fx-border-radius: 4; " +
                "-fx-background-radius: 4; " +
                "}\n" +
                ".check-box:selected .box { " +
                "-fx-background-color: " + PRIMARY + "; " +
                "-fx-border-color: " + PRIMARY + "; " +
                "}\n" +

                // ── Alert ────────────────────────────────────────────────────────
                ".alert { " +
                "-fx-background-color: " + surface + "; " +
                "-fx-text-fill: " + text + "; " +
                "}\n" +
                ".alert .content.label { " +
                "-fx-text-fill: " + text + "; " +
                "}\n" +

                // ── Card ─────────────────────────────────────────────────────────
                ".card { " +
                "-fx-background-color: " + surface + "; " +
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 20; " +
                "-fx-effect: dropshadow(three-pass-box, #00000014, 8, 0, 0, 2); " +
                "}\n" +
                ".card:hover { " +
                "-fx-effect: dropshadow(three-pass-box, #0000001F, 12, 0, 0, 4); " +
                "}\n" +

                // ── Stat Card ────────────────────────────────────────────────────
                ".stat-card { " +
                "-fx-background-color: " + surface + "; " +
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 24; " +
                "-fx-effect: dropshadow(three-pass-box, #00000014, 8, 0, 0, 2); " +
                "}\n" +
                ".stat-card:hover { " +
                "-fx-effect: dropshadow(three-pass-box, #0000001F, 12, 0, 0, 4); " +
                "}\n" +

                // ── Welcome Section ──────────────────────────────────────────────
                ".welcome-section { " +
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, " + PRIMARY + ", " + SECONDARY + "); " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 32; " +
                "-fx-text-fill: white; " +
                "-fx-effect: dropshadow(three-pass-box, #0000001A, 8, 0, 0, 2); " +
                "}\n";
    }

    // Legacy method for backward compatibility
    public static String getStylesheet() {
        return getStylesheet("Light");
    }

    public static String toDataUri(String theme) {
        byte[] bytes = getStylesheet(theme)
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return "data:text/css;base64," +
                java.util.Base64.getEncoder().encodeToString(bytes);
    }

    public static String toDataUri() {
        return toDataUri("Light");
    }

    // Helper method to get mood color based on level
    public static String getMoodColor(int moodLevel) {
        return switch (moodLevel) {
            case 8, 9, 10 -> MOOD_EXCELLENT;
            case 6, 7     -> MOOD_GOOD;
            case 5        -> MOOD_NEUTRAL;
            case 3, 4     -> MOOD_BAD;
            default       -> MOOD_TERRIBLE;
        };
    }

    // Helper method to get severity color
    public static String getSeverityColor(int severity) {
        if (severity <= 2) return SUCCESS;
        if (severity <= 4) return WARNING;
        if (severity <= 7) return DANGER;
        return DANGER;
    }
}