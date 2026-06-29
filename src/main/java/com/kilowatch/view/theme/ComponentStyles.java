package com.kilowatch.view.theme;

/*
how use ex.
JButton validateBtn = new JButton("Valider le relevé");
validateBtn.putClientProperty("FlatLaf.style", ComponentStyles.BTN_PRIMARY);
*/
public class ComponentStyles {

    // Equivalent to .primary-btn
    public static final String BTN_PRIMARY = "background: #e8a23d; " +
            "foreground: #1a1305; " +
            "borderWidth: 0; " +
            "hoverBackground: darken($background, 10%); " +
            "font: bold; " +
            "arc: 10";

    // Equivalent to .ghost-btn
    public static final String BTN_GHOST = "background: #212a37; " +
            "foreground: #e8ecf1; " +
            "borderColor: #2b3544; " +
            "hoverBackground: #242e3c; " +
            "font: bold; " +
            "arc: 10";

    // Equivalent to .pill.success
    public static final String PILL_SUCCESS = "background: rgba(59, 167, 118, 0.15); " +
            "foreground: #3ba776; " +
            "font: bold -2; " + // font-size: 11px
            "borderWidth: 0; " +
            "arc: 999"; // fully rounded (border-radius: 100px)
}