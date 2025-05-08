package com.mycompany.taskflow.controller.Admin;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

public class ReportsController {

    @FXML
    private ImageView reportImageView;

    public void initialize() {
        loadImage(); // Zakładając, że photo_logo.png jest w resources
        // Jeśli jest w podkatalogu np. 'images':
        // loadImage("images/photo_logo.png");
    }

    private void loadImage() {
        InputStream inputStream = getClass().getResourceAsStream("/" + "photo_logo.png");

        if (inputStream == null) {
            System.err.println("Nie można załadować zasobu: " + "photo_logo.png");
            return;
        }

        try {
            Image image = new Image(inputStream, 584, 403, true, true);
            reportImageView.setImage(image);
            reportImageView.setVisible(true);
            reportImageView.setManaged(true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}