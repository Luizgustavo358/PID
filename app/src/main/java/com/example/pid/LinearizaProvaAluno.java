package com.example.pid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LinearizaProvaAluno extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lineariza_prova_aluno);

        Intent intent = getIntent();
    }

    public void tiraFotoProvaAluno(View view) {
        Intent intent = new Intent(this, ProvaDoAluno.class);
        startActivity(intent);
    }

    public void calculaNota(View view) {
        Intent intent = new Intent(this, NotaAluno.class);
        startActivity(intent);
    }
}
