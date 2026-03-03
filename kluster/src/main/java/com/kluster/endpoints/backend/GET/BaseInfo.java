package com.kluster.endpoints.backend.GET;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * Info about the base, such as the name, description, and other relevant information.
 */
public class BaseInfo extends APIEndpoint {
    private final Kluster kluster;

    public BaseInfo(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/baseInfo";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
        // Return imagie to the website
        BufferedImage barcode = kluster.getCodeHelper().createBarcode("1235caowijdoqu3e091");
        
        // Convert the BufferedImage to a byte array and return it as a response
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(barcode, "png", baos);
            baos.flush();
            byte[] imageBytes = baos.toByteArray();
            ctx.contentType("image/png");
            ctx.result(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            ctx.status(500).result("Failed to generate barcode");
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
