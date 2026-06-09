package com.mindoc.ui.common;

import com.mindoc.ui.theme.MindDocTheme;
import javafx.scene.layout.VBox;

/**
 * Base class for all application panels
 * Provides consistent styling and layout across all panels
 */
public abstract class BasePanel extends VBox {
    protected static final double SPACING = 20.0;
    protected static final double PADDING = 24.0;
    
    public BasePanel() {
        setPrefWidth(Double.MAX_VALUE);
        setPrefHeight(Double.MAX_VALUE);
        setSpacing(SPACING);
        setPadding(new javafx.geometry.Insets(PADDING));
        setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");
    }
    
    public abstract void refresh();
}
