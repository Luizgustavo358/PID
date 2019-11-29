package com.example.pid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LinearizarProvaEmBranco extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linearizar_prova_em_branco);

        Intent intent = getIntent();
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
