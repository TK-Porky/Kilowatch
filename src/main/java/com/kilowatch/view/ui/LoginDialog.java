package com.kilowatch.view.ui;

import com.kilowatch.view.component.AppDialog;
import com.kilowatch.view.theme.AppColors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.BiFunction;

public class LoginDialog extends AppDialog {

    private boolean isSuccess = false;

    // Le callback : prend (User, Password) et retourne un Boolean
    public LoginDialog(BiFunction<String, String, Boolean> onAuthRequest) {
        super((Frame) null, "Kilowatch - Connexion", true); // true = bloque l'application

        setSize(350, 250);
        setResizable(false);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        panel.setBackground(AppColors.BG_SURFACE);

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();

        panel.add(createFieldPanel("Nom d'utilisateur :", userField));
        panel.add(createFieldPanel("Mot de passe :", passField));

        JButton loginBtn = new JButton("Se connecter");
        loginBtn.setBackground(AppColors.ACCENT_AMBER);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);

        // --- LA VUE DUMP EN ACTION ---
        loginBtn.addActionListener(e -> {
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());

            // On délègue la logique au Main via le callback
            boolean isValid = onAuthRequest.apply(user, pass);

            if (isValid) {
                isSuccess = true;
                dispose(); // Ferme la fenêtre
            } else {
                JOptionPane.showMessageDialog(this, "Identifiants incorrects !", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(loginBtn);
        add(panel);

        // On indique à AppDialog que ce bouton doit réagir à la touche Entrée
        setSubmitButton(loginBtn);
    }

    private JPanel createFieldPanel(String labelText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setForeground(AppColors.TEXT_PRIMARY);
        p.add(label, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}