package com.example.pid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ProvaDoAluno extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prova_do_aluno);

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

    /**
     * @param view
     */
    public void linearizaProvaAluno(View view) {
        Intent intent = new Intent(this, LinearizaProvaAluno.class);
        startActivity(intent);
    }
}
