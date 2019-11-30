package com.example.pid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;

import java.io.File;

public class TelaInicial extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_inicial);
        OpenCVLoader.initDebug();
    }

    public void provaEmBranco(View view) {
        Intent intent = new Intent(this, SelecionaProva.class);
        startActivity(intent);
    }
}
