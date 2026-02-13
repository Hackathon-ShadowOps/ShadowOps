package com.kluster.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HTMLServe {
    public final static String baseFolderPath = "kluster/src/main/resources/html";

    public static String getPage(String folderPath) {
        if (folderPath == null || folderPath.isEmpty()) {
            return null;
        }

        String basePath = baseFolderPath + "/" + folderPath;
        String html = "";
        String css = "";
        String js = "";

        try {
            html = Files.readString(Paths.get(basePath + "/index.html"));
            css = Files.readString(Paths.get(basePath + "/style.css"));
            js = Files.readString(Paths.get(basePath + "/script.js"));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error reading landing page files: " + basePath);
            return null;
        }

        String fullPage = html
                .replace("<!-- INLINE_CSS -->", "<style>" + css + "</style>")
                .replace("<!-- INLINE_JS -->", "<script>" + js + "</script>");

        return fullPage;
    }
}
