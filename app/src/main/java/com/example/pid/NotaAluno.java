package com.example.pid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class NotaAluno extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nota);

        Intent intent = getIntent();
    }

    /**
     * Volta para tela inicial.
     * @param view
     */
    public void telaInicial(View view) {
        Intent intent = new Intent(this, TelaInicial.class);
        startActivity(intent);
    }

    public void proximaProva(View view) {
        Intent intent = new Intent(this, ProvaDoAluno.class);
        startActivity(intent);
    }
}
