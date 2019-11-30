package com.example.pid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class LinearizarProvaEmBranco extends AppCompatActivity {

    public ImageView provaEmBranco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linearizar_prova_em_branco);

        Intent intent = getIntent();

        initializeViews();

        colocaImagem(intent);
    }

    /**
     * Initializes the views of the activity
     */
    private void initializeViews() {
        provaEmBranco = (ImageView) findViewById(R.id.provaEmBranco);
    }

    private void colocaImagem(Intent intent) {
        File file = (File) getIntent().getExtras().get("prova");

        String filePath = file.getPath();

        Bitmap bitmap = BitmapFactory.decodeFile(filePath);

        provaEmBranco.setImageBitmap(bitmap);
    }

    public void selecionarProvaEmBranco(View view) {
        Intent intent = new Intent(this, SelecionaProva.class);
        startActivity(intent);
    }

    public void selecionaGabarito(View view) {
        Intent intent = new Intent(this, SelecionaGabarito.class);
        startActivity(intent);
    }
}
