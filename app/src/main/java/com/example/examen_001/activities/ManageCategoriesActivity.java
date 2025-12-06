package com.example.examen_001.activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examen_001.R;
import com.example.examen_001.adapters.CategoryManageAdapter;
import com.example.examen_001.database.DatabaseHelper;
import com.example.examen_001.models.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity para gestionar categorías
 */
public class ManageCategoriesActivity extends AppCompatActivity
        implements CategoryManageAdapter.OnCategoryActionListener {

    private RecyclerView recyclerView;
    private CategoryManageAdapter adapter;
    private FloatingActionButton fabAdd;
    private TextView tvEmpty;

    private DatabaseHelper dbHelper;
    private List<Category> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Gestionar Categorías");

        dbHelper = new DatabaseHelper(this);
        categories = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        loadCategories();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewCategories);
        fabAdd = findViewById(R.id.fabAddCategory);
        tvEmpty = findViewById(R.id.tvEmpty);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCategoryDialog();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new CategoryManageAdapter(categories, this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadCategories() {
        categories.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                null, null, null, null, null,
                DatabaseHelper.COLUMN_TYPE + " ASC, " + DatabaseHelper.COLUMN_NAME + " ASC");

        while (cursor.moveToNext()) {
            Category category = new Category();
            category.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
            category.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)));
            category.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE)));
            category.setIcon(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ICON)));
            category.setColor(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COLOR)));
            category.setPredefined(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_PREDEFINED)) == 1);
            category.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_ACTIVE)) == 1);

            categories.add(category);
        }
        cursor.close();

        if (categories.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);

        final EditText etName = dialogView.findViewById(R.id.etCategoryName);
        final Spinner spinnerType = dialogView.findViewById(R.id.spinnerType);
        final Spinner spinnerColor = dialogView.findViewById(R.id.spinnerColor);

        // Configurar spinner de tipo
        String[] types = {"Gasto", "Ingreso"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Configurar spinner de colores
        final String[] colorNames = {"Rojo", "Verde", "Azul", "Naranja", "Morado",
                "Amarillo", "Rosa", "Cyan", "Gris"};
        final String[] colorValues = {"#F44336", "#4CAF50", "#2196F3", "#FF9800",
                "#9C27B0", "#FFEB3B", "#E91E63", "#00BCD4", "#607D8B"};

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, colorNames);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(colorAdapter);

        builder.setView(dialogView)
                .setTitle("Nueva Categoría")
                .setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = etName.getText().toString().trim();
                        if (TextUtils.isEmpty(name)) {
                            Toast.makeText(ManageCategoriesActivity.this,
                                    "Ingresa un nombre", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String type = spinnerType.getSelectedItemPosition() == 0
                                ? DatabaseHelper.TYPE_EXPENSE : DatabaseHelper.TYPE_INCOME;
                        String color = colorValues[spinnerColor.getSelectedItemPosition()];

                        addCategory(name, type, color);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void addCategory(String name, String type, String color) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_TYPE, type);
        values.put(DatabaseHelper.COLUMN_ICON, "ic_custom");
        values.put(DatabaseHelper.COLUMN_COLOR, color);
        values.put(DatabaseHelper.COLUMN_IS_PREDEFINED, 0);
        values.put(DatabaseHelper.COLUMN_IS_ACTIVE, 1);

        long result = db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values);
        if (result != -1) {
            Toast.makeText(this, "Categoría agregada", Toast.LENGTH_SHORT).show();
            loadCategories();
        } else {
            Toast.makeText(this, "Error al agregar categoría", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditCategory(Category category) {
        showEditCategoryDialog(category);
    }

    private void showEditCategoryDialog(final Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);

        final EditText etName = dialogView.findViewById(R.id.etCategoryName);
        final Spinner spinnerColor = dialogView.findViewById(R.id.spinnerColor);

        // Ocultar spinner de tipo (no se puede cambiar)
        dialogView.findViewById(R.id.spinnerType).setVisibility(View.GONE);

        etName.setText(category.getName());

        // Configurar colores
        final String[] colorNames = {"Rojo", "Verde", "Azul", "Naranja", "Morado",
                "Amarillo", "Rosa", "Cyan", "Gris"};
        final String[] colorValues = {"#F44336", "#4CAF50", "#2196F3", "#FF9800",
                "#9C27B0", "#FFEB3B", "#E91E63", "#00BCD4", "#607D8B"};

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, colorNames);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(colorAdapter);

        // Seleccionar color actual
        for (int i = 0; i < colorValues.length; i++) {
            if (colorValues[i].equals(category.getColor())) {
                spinnerColor.setSelection(i);
                break;
            }
        }

        builder.setView(dialogView)
                .setTitle("Editar Categoría")
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = etName.getText().toString().trim();
                        if (TextUtils.isEmpty(name)) {
                            Toast.makeText(ManageCategoriesActivity.this,
                                    "Ingresa un nombre", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String color = colorValues[spinnerColor.getSelectedItemPosition()];
                        updateCategory(category.getId(), name, color);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void updateCategory(int id, String name, String color) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_COLOR, color);

        int updated = db.update(DatabaseHelper.TABLE_CATEGORIES, values,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});

        if (updated > 0) {
            Toast.makeText(this, "Categoría actualizada", Toast.LENGTH_SHORT).show();
            loadCategories();
        }
    }

    @Override
    public void onToggleCategory(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_IS_ACTIVE, category.isActive() ? 0 : 1);

        db.update(DatabaseHelper.TABLE_CATEGORIES, values,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(category.getId())});

        loadCategories();
    }

    @Override
    public void onDeleteCategory(final Category category) {
        if (category.isPredefined()) {
            Toast.makeText(this, "No se pueden eliminar categorías predefinidas",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar Categoría")
                .setMessage("¿Eliminar '" + category.getName() + "'?")
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        int deleted = db.delete(DatabaseHelper.TABLE_CATEGORIES,
                                DatabaseHelper.COLUMN_ID + " = ?",
                                new String[]{String.valueOf(category.getId())});

                        if (deleted > 0) {
                            Toast.makeText(ManageCategoriesActivity.this,
                                    "Categoría eliminada", Toast.LENGTH_SHORT).show();
                            loadCategories();
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}