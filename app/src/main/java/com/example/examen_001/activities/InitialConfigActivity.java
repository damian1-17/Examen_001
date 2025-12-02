package com.example.examen_001.activities;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.examen_001.database.DatabaseHelper;
import com.example.examen_001.R;

public class InitialConfigActivity extends AppCompatActivity {

    private EditText etUserName;
    private EditText etMonthlyBudget;
    private Spinner spinnerCurrency;
    private Spinner spinnerStartDay;
    private Button btnSaveConfig;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si ya existe configuración
        dbHelper = new DatabaseHelper(this);
        if (isConfigurationExists()) {
            // Si ya existe configuración, ir al Dashboard
            navigateToDashboard();
            return;
        }

        setContentView(R.layout.activity_initial_config);

        initializeViews();
        setupCurrencySpinner();
        setupStartDaySpinner();
        setupSaveButton();
    }

    /**
     * Inicializa las vistas
     */
    private void initializeViews() {
        etUserName = findViewById(R.id.etUserName);
        etMonthlyBudget = findViewById(R.id.etMonthlyBudget);
        spinnerCurrency = findViewById(R.id.spinnerCurrency);
        spinnerStartDay = findViewById(R.id.spinnerStartDay);
        btnSaveConfig = findViewById(R.id.btnSaveConfig);
    }

    /**
     * Configura el Spinner de monedas
     */
    private void setupCurrencySpinner() {
        String[] currencies = {
                "USD - Dólar Estadounidense",
                "EUR - Euro",
                "GBP - Libra Esterlina",
                "JPY - Yen Japonés",
                "CAD - Dólar Canadiense",
                "AUD - Dólar Australiano",
                "CHF - Franco Suizo",
                "CNY - Yuan Chino",
                "MXN - Peso Mexicano",
                "BRL - Real Brasileño",
                "ARS - Peso Argentino",
                "COP - Peso Colombiano",
                "CLP - Peso Chileno",
                "PEN - Sol Peruano"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                currencies
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapter);
    }

    /**
     * Configura el Spinner de día de inicio del mes
     */
    private void setupStartDaySpinner() {
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) {
            days[i] = String.valueOf(i + 1);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                days
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStartDay.setAdapter(adapter);
        spinnerStartDay.setSelection(0); // Día 1 por defecto
    }

    /**
     * Configura el botón de guardar
     */
    private void setupSaveButton() {
        btnSaveConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfiguration();
            }
        });
    }

    /**
     * Valida y guarda la configuración
     */
    private void saveConfiguration() {
        // Obtener valores
        String userName = etUserName.getText().toString().trim();
        String budgetStr = etMonthlyBudget.getText().toString().trim();
        String currencySelected = spinnerCurrency.getSelectedItem().toString();
        String currencyCode = currencySelected.substring(0, 3); // Obtener código (USD, EUR, etc.)
        int startDay = spinnerStartDay.getSelectedItemPosition() + 1;

        // Validaciones
        if (TextUtils.isEmpty(userName)) {
            etUserName.setError("Por favor ingresa tu nombre");
            etUserName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(budgetStr)) {
            etMonthlyBudget.setError("Por favor ingresa tu presupuesto mensual");
            etMonthlyBudget.requestFocus();
            return;
        }

        double monthlyBudget;
        try {
            monthlyBudget = Double.parseDouble(budgetStr);
            if (monthlyBudget <= 0) {
                etMonthlyBudget.setError("El presupuesto debe ser mayor a 0");
                etMonthlyBudget.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etMonthlyBudget.setError("Por favor ingresa un número válido");
            etMonthlyBudget.requestFocus();
            return;
        }

        // Guardar en la base de datos
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_NAME, userName);
        values.put(DatabaseHelper.COLUMN_MONTHLY_BUDGET, monthlyBudget);
        values.put(DatabaseHelper.COLUMN_CURRENCY_CODE, currencyCode);
        values.put(DatabaseHelper.COLUMN_MONTH_START_DAY, startDay);
        values.put(DatabaseHelper.COLUMN_ALERT_THRESHOLD, 80); // 80% por defecto

        long result = db.insert(DatabaseHelper.TABLE_USER_CONFIG, null, values);

        if (result != -1) {
            Toast.makeText(this, "¡Configuración guardada exitosamente!", Toast.LENGTH_SHORT).show();
            navigateToDashboard();
        } else {
            Toast.makeText(this, "Error al guardar la configuración", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Verifica si ya existe una configuración guardada
     */
    private boolean isConfigurationExists() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USER_CONFIG,
                new String[]{DatabaseHelper.COLUMN_ID},
                null, null, null, null, null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Navega al Dashboard (MainActivity)
     */
    private void navigateToDashboard() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }


}
