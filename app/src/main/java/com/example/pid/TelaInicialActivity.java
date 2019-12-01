package com.example.pid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.opencv.android.OpenCVLoader;

import java.io.File;

import static com.example.pid.SelecionaProvaActivity.printFiles;

public class TelaInicialActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_inicial);
        OpenCVLoader.initDebug();
    }

    public void provaEmBranco(View view) {
        Intent intent = new Intent(this, SelecionaProvaActivity.class);
        intent.putExtra("TITLE", "Prova em Branco");
        intent.putExtra("TIPO_PROVA", TipoProva.PROVA_EM_BRANCO);
        File[] files = new File[3];
        intent.putExtra("FILE", files);

        System.out.println(this.getClass().getCanonicalName());
        System.out.println(TipoProva.PROVA_EM_BRANCO.name());
        printFiles(files);
        System.out.println();

        startActivity(intent);
    }
}
