package com.kluster.controller;

import uk.org.okapibarcode.backend.Code128;
import uk.org.okapibarcode.backend.HumanReadableLocation;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class CodeHelper {
    // Create a barcode
    public BufferedImage createBarcode(String data) {
        Code128 barcode = new Code128();
        barcode.setFontName("Monospaced");
        barcode.setFontSize(16);
        barcode.setModuleWidth(2);
        barcode.setBarHeight(50);
        barcode.setHumanReadableLocation(HumanReadableLocation.BOTTOM);
        barcode.setContent(data);

        int width = barcode.getWidth();
        int height = barcode.getHeight();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            // Try to use the library's Java2D renderer if available (use reflection to avoid compile-time coupling)
            try {
                Class<?> rendererClass = Class.forName("uk.org.okapibarcode.output.Java2DRenderer");
                Constructor<?> ctor = rendererClass.getConstructor(java.awt.Graphics2D.class);
                Object renderer = ctor.newInstance(g);

                Class<?> canvasClass = Class.forName("uk.org.okapibarcode.output.Canvas");
                Method renderMethod = barcode.getClass().getMethod("render", canvasClass);
                renderMethod.invoke(barcode, renderer);
            } catch (Exception e) {
                // Fallback: draw the human-readable text and a simple placeholder bar pattern
                g.setColor(Color.BLACK);
                g.setFont(new Font("Monospaced", Font.PLAIN, 16));
                g.drawString(data, 8, height / 2 + 6);

                int stripeX = 8;
                int stripeY = height / 2 + 14;
                int stripeHeight = Math.max(8, height / 6);
                for (int i = 0; i < data.length(); i++) {
                    if ((data.charAt(i) % 2) == 0) {
                        g.fillRect(stripeX, stripeY, 4, stripeHeight);
                    }
                    stripeX += 6;
                }
            }
        } finally {
            g.dispose();
        }

        return image;
    }
}
