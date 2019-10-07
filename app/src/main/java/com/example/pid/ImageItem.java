package com.example.pid;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.io.File;

public class ImageItem {

    private File file;
    private ImageView imageView;
    private Bitmap bitmap;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
