package com.example.examen_001.activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.examen_001.R;
import com.example.examen_001.database.DatabaseHelper;

/**
 * Activity de Configuración
 * Permite editar preferencias y gestionar categorías/métodos de pago
 */
public class SettingsActivity extends AppCompatActivity {

    // Views de Perfil
    private TextView tvUserName;
    private TextView tvMonthlyBudget;
    private TextView tvCurrency;
    private TextView tvStartDay;
    private TextView tvAlertThreshold;
    private CardView cardEditProfile;

    // Views de Gestión
    private CardView cardManageCategories;
    private CardView cardManagePaymentMethods;
    private CardView cardResetData;
    private CardView cardAbout;

    private DatabaseHelper dbHelper;
    private long configId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Configuración");

        dbHelper = new DatabaseHelper(this);

        initializeViews();
        loadConfiguration();
        setupClickListeners();
    }

    /**
     * Inicializa las vistas
     */
    private void initializeViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvMonthlyBudget = findViewById(R.id.tvMonthlyBudget);
        tvCurrency = findViewById(R.id.tvCurrency);
        tvStartDay = findViewById(R.id.tvStartDay);
        tvAlertThreshold = findViewById(R.id.tvAlertThreshold);

        cardEditProfile = findViewById(R.id.cardEditProfile);
        cardManageCategories = findViewById(R.id.cardManageCategories);
        cardManagePaymentMethods = findViewById(R.id.cardManagePaymentMethods);
        cardResetData = findViewById(R.id.cardResetData);
        cardAbout = findViewById(R.id.cardAbout);
    }

    /**
     * Carga la configuración actual
     */
    private void loadConfiguration() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_CONFIG,
                null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            configId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));

            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME));
            double budget = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MONTHLY_BUDGET));
            String currency = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CURRENCY_CODE));
            int startDay = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MONTH_START_DAY));
            int threshold = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALERT_THRESHOLD));

            tvUserName.setText(name);
            tvMonthlyBudget.setText(String.format("$%.2f", budget));
            tvCurrency.setText(currency);
            tvStartDay.setText("Día " + startDay);
            tvAlertThreshold.setText(threshold + "%");
        }
        cursor.close();
    }

    /**
     * Configura los listeners
     */
    private void setupClickListeners() {
        // Editar perfil
        cardEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        // Gestionar categorías
        cardManageCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ManageCategoriesActivity.class);
                startActivity(intent);
            }
        });

        // Gestionar métodos de pago
        cardManagePaymentMethods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showManagePaymentMethodsDialog();
            }
        });

        // Restablecer datos
        cardResetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetDataDialog();
            }
        });

        // Acerca de
        cardAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
            }
        });
    }

    /**
     * Muestra diálogo para editar perfil
     */
    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);

        final EditText etName = dialogView.findViewById(R.id.etName);
        final EditText etBudget = dialogView.findViewById(R.id.etBudget);
        final Spinner spinnerCurrency = dialogView.findViewById(R.id.spinnerCurrency);
        final Spinner spinnerStartDay = dialogView.findViewById(R.id.spinnerStartDay);
        final Spinner spinnerThreshold = dialogView.findViewById(R.id.spinnerThreshold);

        // Cargar valores actuales
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_CONFIG,
                null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME));
            double budget = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MONTHLY_BUDGET));
            String currency = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CURRENCY_CODE));
            int startDay = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MONTH_START_DAY));
            int threshold = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALERT_THRESHOLD));

            etName.setText(name);
            etBudget.setText(String.valueOf(budget));

            // Configurar spinners
            setupCurrencySpinner(spinnerCurrency, currency);
            setupStartDaySpinner(spinnerStartDay, startDay);
            setupThresholdSpinner(spinnerThreshold, threshold);
        }
        cursor.close();

        builder.setView(dialogView)
                .setTitle("Editar Perfil")
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveProfileChanges(etName, etBudget, spinnerCurrency,
                                spinnerStartDay, spinnerThreshold);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Configura el spinner de monedas
     */
    private void setupCurrencySpinner(Spinner spinner, String currentCurrency) {
        String[] currencies = {"USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF",
                "CNY", "MXN", "BRL", "ARS", "COP", "CLP", "PEN"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Seleccionar actual
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].equals(currentCurrency)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    /**
     * Configura el spinner de día de inicio
     */
    private void setupStartDaySpinner(Spinner spinner, int currentDay) {
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) {
            days[i] = String.valueOf(i + 1);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, days);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(currentDay - 1);
    }

    /**
     * Configura el spinner de umbral de alerta
     */
    private void setupThresholdSpinner(Spinner spinner, int currentThreshold) {
        String[] thresholds = {"50%", "60%", "70%", "80%", "90%"};
        int[] values = {50, 60, 70, 80, 90};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, thresholds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Seleccionar actual
        for (int i = 0; i < values.length; i++) {
            if (values[i] == currentThreshold) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    /**
     * Guarda los cambios del perfil
     */
    private void saveProfileChanges(EditText etName, EditText etBudget,
                                    Spinner spinnerCurrency, Spinner spinnerStartDay,
                                    Spinner spinnerThreshold) {
        String name = etName.getText().toString().trim();
        String budgetStr = etBudget.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Ingresa tu nombre", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(budgetStr)) {
            Toast.makeText(this, "Ingresa el presupuesto", Toast.LENGTH_SHORT).show();
            return;
        }

        double budget = Double.parseDouble(budgetStr);
        String currency = spinnerCurrency.getSelectedItem().toString();
        int startDay = spinnerStartDay.getSelectedItemPosition() + 1;
        String thresholdStr = spinnerThreshold.getSelectedItem().toString().replace("%", "");
        int threshold = Integer.parseInt(thresholdStr);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_NAME, name);
        values.put(DatabaseHelper.COLUMN_MONTHLY_BUDGET, budget);
        values.put(DatabaseHelper.COLUMN_CURRENCY_CODE, currency);
        values.put(DatabaseHelper.COLUMN_MONTH_START_DAY, startDay);
        values.put(DatabaseHelper.COLUMN_ALERT_THRESHOLD, threshold);

        int updated = db.update(DatabaseHelper.TABLE_USER_CONFIG, values,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(configId)});

        if (updated > 0) {
            Toast.makeText(this, "Configuración actualizada", Toast.LENGTH_SHORT).show();
            loadConfiguration();
        } else {
            Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Muestra diálogo para gestionar métodos de pago
     */
    private void showManagePaymentMethodsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_payment_methods, null);

        final LinearLayout layoutMethods = dialogView.findViewById(R.id.layoutMethods);
        final Button btnAddMethod = dialogView.findViewById(R.id.btnAddMethod);

        // Cargar métodos actuales
        loadPaymentMethodsIntoLayout(layoutMethods);

        btnAddMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPaymentMethodDialog(layoutMethods);
            }
        });

        builder.setView(dialogView)
                .setTitle("Métodos de Pago")
                .setPositiveButton("Cerrar", null)
                .show();
    }

    /**
     * Carga los métodos de pago en el layout
     */
    private void loadPaymentMethodsIntoLayout(LinearLayout layout) {
        layout.removeAllViews();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PAYMENT_METHODS,
                null, null, null, null, null,
                DatabaseHelper.COLUMN_NAME + " ASC");

        while (cursor.moveToNext()) {
            final long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            final String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
            boolean isActive = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_ACTIVE)) == 1;

            View itemView = LayoutInflater.from(this).inflate(R.layout.item_payment_method, layout, false);
            TextView tvName = itemView.findViewById(R.id.tvMethodName);
            Button btnToggle = itemView.findViewById(R.id.btnToggle);
            Button btnDelete = itemView.findViewById(R.id.btnDelete);

            tvName.setText(name);
            btnToggle.setText(isActive ? "Desactivar" : "Activar");

            btnToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    togglePaymentMethod(id, name, layout);
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deletePaymentMethod(id, name, layout);
                }
            });

            layout.addView(itemView);
        }
        cursor.close();
    }

    /**
     * Muestra diálogo para agregar método de pago
     */
    private void showAddPaymentMethodDialog(final LinearLayout layout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setHint("Nombre del método");

        builder.setTitle("Agregar Método de Pago")
                .setView(input)
                .setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString().trim();
                        if (!TextUtils.isEmpty(name)) {
                            addPaymentMethod(name, layout);
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Agrega un método de pago
     */
    private void addPaymentMethod(String name, LinearLayout layout) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_IS_ACTIVE, 1);

        long result = db.insert(DatabaseHelper.TABLE_PAYMENT_METHODS, null, values);
        if (result != -1) {
            Toast.makeText(this, "Método agregado", Toast.LENGTH_SHORT).show();
            loadPaymentMethodsIntoLayout(layout);
        }
    }

    /**
     * Activa/desactiva un método de pago
     */
    private void togglePaymentMethod(long id, String name, LinearLayout layout) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_PAYMENT_METHODS,
                new String[]{DatabaseHelper.COLUMN_IS_ACTIVE},
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor.moveToFirst()) {
            int currentStatus = cursor.getInt(0);
            int newStatus = currentStatus == 1 ? 0 : 1;

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IS_ACTIVE, newStatus);

            db.update(DatabaseHelper.TABLE_PAYMENT_METHODS, values,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)});

            loadPaymentMethodsIntoLayout(layout);
        }
        cursor.close();
    }

    /**
     * Elimina un método de pago
     */
    private void deletePaymentMethod(final long id, String name, final LinearLayout layout) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Método")
                .setMessage("¿Eliminar '" + name + "'?")
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete(DatabaseHelper.TABLE_PAYMENT_METHODS,
                                DatabaseHelper.COLUMN_ID + " = ?",
                                new String[]{String.valueOf(id)});

                        Toast.makeText(SettingsActivity.this, "Método eliminado", Toast.LENGTH_SHORT).show();
                        loadPaymentMethodsIntoLayout(layout);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Muestra diálogo para restablecer datos
     */
    private void showResetDataDialog() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Restablecer Datos")
                .setMessage("Esta acción eliminará TODAS las transacciones.\n\n" +
                        "La configuración y categorías se mantendrán.\n\n" +
                        "¿Deseas continuar?")
                .setPositiveButton("Restablecer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetTransactionData();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Restablece los datos de transacciones
     */
    private void resetTransactionData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_TRANSACTIONS, null, null);
        db.delete(DatabaseHelper.TABLE_ALERTS, null, null);

        Toast.makeText(this, "Datos restablecidos correctamente", Toast.LENGTH_LONG).show();
    }

    /**
     * Muestra diálogo "Acerca de"
     */
    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Acerca de Finance App")
                .setMessage("Finance App v1.0\n\n" +
                        "Aplicación de gestión financiera personal\n\n" +
                        "Características:\n" +
                        "• Control de ingresos y gastos\n" +
                        "• Estadísticas y gráficos\n" +
                        "• Conversión de monedas\n" +
                        "• Alertas de presupuesto\n\n" +
                        "Desarrollado con ❤️")
                .setPositiveButton("Cerrar", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConfiguration();
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