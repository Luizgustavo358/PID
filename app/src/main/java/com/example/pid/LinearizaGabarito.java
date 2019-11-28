package com.example.pid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LinearizaGabarito extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lineariza_gabarito);

        Intent intent = getIntent();
    }

    public void tiraFotoGabarito(View view) {
        Intent intent = new Intent(this, SelecionaGabarito.class);
        startActivity(intent);
    }

    public void tiraFotoProvaAluno(View view) {
        Intent intent = new Intent(this, ProvaDoAluno.class);
        startActivity(intent);
    }
}
