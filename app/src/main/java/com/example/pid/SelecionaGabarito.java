package com.example.pid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SelecionaGabarito extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleciona_gabarito);

        Intent intent = getIntent();
    }

    /**
     * Volta para tela inicial.
     * @param view
     */
    public void telaInicial(View view) {
        Intent intent = new Intent(this, TelaInicialActivity.class);
        startActivity(intent);
    }

    public void linearizaGabarito(View view) {
        Intent intent = new Intent(this, LinearizaGabarito.class);
        startActivity(intent);
    }
}
