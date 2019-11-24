package com.example.pid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class TelaInicial extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_inicial);
    }

    public void provaEmBranco(View view) {
        Intent intent = new Intent(this, SelecionaProva.class);
        startActivity(intent);
    }
}
