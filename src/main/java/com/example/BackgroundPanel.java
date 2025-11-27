package com.example;

import javax.swing.*;
import java.awt.*;

class BackgroundPanel extends JPanel {
    private Image background;

    public BackgroundPanel(String imagePath) {
        background = new ImageIcon(imagePath).getImage();
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
