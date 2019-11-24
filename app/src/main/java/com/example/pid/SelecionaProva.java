package com.example.pid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SelecionaProva extends AppCompatActivity {
    // variaveis globais
    Button btnCamera;
    Button btnGaleria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleciona_prova);

        btnCamera = (Button) findViewById(R.id.btnFotoProvaEmBranco);
        btnGaleria = (Button) findViewById(R.id.btnGaleriaProvaEmBranco);

        Intent intent = getIntent();
    }

    public void telaInicial(View view) {
        Intent intent = new Intent(this, TelaInicial.class);
        startActivity(intent);
    }
}
