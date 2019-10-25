package com.xcheko51x.exportar_sqlite_csv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView rvUsuarios;
    Button btnExportar;

    AdaptadorUsuarios adaptador;

    List<Usuario> listaUsuarios = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvUsuarios = findViewById(R.id.rvUsuarios);
        btnExportar = findViewById(R.id.btnExportar);

        rvUsuarios.setLayoutManager(new GridLayoutManager(this, 1));

        pedirPermisos();
        obtenerUsuarios();

        btnExportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportarCSV();
            }
        });
    }

    public void pedirPermisos() {
        // PERMISOS PARA ANDROID 6 O SUPERIOR
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0
            );

        }
    }

    public void obtenerUsuarios() {
        listaUsuarios.clear();

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(MainActivity.this, "dbSistema", null, 1);
        SQLiteDatabase db = admin.getWritableDatabase();

        Cursor fila = db.rawQuery("select * from usuarios", null);

        if(fila != null && fila.getCount() != 0) {
            fila.moveToFirst();
            do {
                listaUsuarios.add(
                        new Usuario(
                                fila.getString(1),
                                fila.getString(2)
                        )
                );
            } while(fila.moveToNext());
        } else {
            Toast.makeText(MainActivity.this, "No hay registros.", Toast.LENGTH_LONG).show();
        }

        db.close();

        adaptador = new AdaptadorUsuarios(MainActivity.this, listaUsuarios);
        rvUsuarios.setAdapter(adaptador);
    }

    public void exportarCSV() {
        File carpeta = new File(Environment.getExternalStorageDirectory() + "/ExportarSQLiteCSV");
        String archivoAgenda = carpeta.toString() + "/" + "Usuarios.csv";

        boolean isCreate = false;
        if(!carpeta.exists()) {
            isCreate = carpeta.mkdir();
        }

        try {
            FileWriter fileWriter = new FileWriter(archivoAgenda);

            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(MainActivity.this, "dbSistema", null, 1);

            SQLiteDatabase db = admin.getWritableDatabase();

            Cursor fila = db.rawQuery("select * from usuarios", null);

            if(fila != null && fila.getCount() != 0) {
                fila.moveToFirst();
                do {

                    fileWriter.append(fila.getString(0));
                    fileWriter.append(",");
                    fileWriter.append(fila.getString(1));
                    fileWriter.append(",");
                    fileWriter.append(fila.getString(2));
                    fileWriter.append("\n");

                } while(fila.moveToNext());
            } else {
                Toast.makeText(MainActivity.this, "No hay registros.", Toast.LENGTH_LONG).show();
            }

            db.close();
            fileWriter.close();
            Toast.makeText(MainActivity.this, "SE CREO EL ARCHIVO CSV EXITOSAMENTE", Toast.LENGTH_LONG).show();

        } catch (Exception e) { }
    }
}
