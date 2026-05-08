package com.mindoc.ui.common;

import javafx.scene.layout.VBox;

/**
 * Base class for all application panels
 */
public abstract class BasePanel extends VBox {
    protected static final double SPACING = 15.0;
    protected static final double PADDING = 20.0;
    
    public BasePanel() {
        setPrefWidth(Double.MAX_VALUE);
        setPrefHeight(Double.MAX_VALUE);
        setSpacing(SPACING);
        setPadding(new javafx.geometry.Insets(PADDING));
        setStyle("-fx-background-color: #f7fafc;");
    }
    
    public abstract void refresh();
}
