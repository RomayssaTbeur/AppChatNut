package com.example.appchatnutritien.activities;

import com.squareup.picasso.Transformation;

import java.util.List;

import android.graphics.Bitmap;
import com.squareup.picasso.Transformation;

public class CircleTransformation implements Transformation {

    @Override
    public Bitmap transform(Bitmap source) {
        // Convertir l'image en cercle
        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        return output;
    }

    @Override
    public String key() {
        return "circle";
    }
}
