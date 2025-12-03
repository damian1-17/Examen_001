package com.example.examen_001.activities;



import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.examen_001.R;
import com.example.examen_001.database.DatabaseHelper;
import com.example.examen_001.models.Category;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
public class AddTransactionActivity extends AppCompatActivity {

    // Views
    private RadioGroup rgTransactionType;
    private RadioButton rbIncome, rbExpense;
    private EditText etAmount;
    private Spinner spinnerCategory;
    private Spinner spinnerPaymentMethod;
    private EditText etDescription;
    private TextView tvSelectedDate;
    private Button btnSelectDate;
    private Button btnSaveTransaction;
    private Button btnCancel;

    // Conversión de moneda
    private EditText etOriginalAmount;
    private Spinner spinnerOriginalCurrency;
    private Button btnConvertCurrency;
    private TextView tvExchangeRate;
    private View layoutCurrencyConversion;

    // Variables
    private DatabaseHelper dbHelper;
    private Calendar selectedDate;
    private String transactionType = DatabaseHelper.TYPE_EXPENSE;
    private List<Category> expenseCategories;
    private List<Category> incomeCategories;
    private List<String> paymentMethods;
    private String userCurrency = "USD";
    private long transactionId = -1; // Para edición
    private boolean isEditMode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Nueva Transacción");

        dbHelper = new DatabaseHelper(this);
        selectedDate = Calendar.getInstance();

        // Verificar si es modo edición
        if (getIntent().hasExtra("transaction_id")) {
            transactionId = getIntent().getLongExtra("transaction_id", -1);
            isEditMode = true;
            getSupportActionBar().setTitle("Editar Transacción");
        }

        initializeViews();
        loadUserCurrency();
        loadCategories();
        loadPaymentMethods();
        setupListeners();

        if (isEditMode) {
            loadTransactionData();
        }
    }

    /**
     * Inicializa las vistas
     */
    private void initializeViews() {
        rgTransactionType = findViewById(R.id.rgTransactionType);
        rbIncome = findViewById(R.id.rbIncome);
        rbExpense = findViewById(R.id.rbExpense);
        etAmount = findViewById(R.id.etAmount);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        etDescription = findViewById(R.id.etDescription);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSaveTransaction = findViewById(R.id.btnSaveTransaction);
        btnCancel = findViewById(R.id.btnCancel);

        // Conversión de moneda
        layoutCurrencyConversion = findViewById(R.id.layoutCurrencyConversion);
        etOriginalAmount = findViewById(R.id.etOriginalAmount);
        spinnerOriginalCurrency = findViewById(R.id.spinnerOriginalCurrency);
        btnConvertCurrency = findViewById(R.id.btnConvertCurrency);
        tvExchangeRate = findViewById(R.id.tvExchangeRate);

        // Establecer fecha actual
        updateDateDisplay();

        // Configurar spinner de monedas
        setupCurrencySpinner();
    }

    /**
     * Configura el spinner de monedas
     */
    private void setupCurrencySpinner() {
        String[] currencies = {"USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF",
                "CNY", "MXN", "BRL", "ARS", "COP", "CLP", "PEN"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOriginalCurrency.setAdapter(adapter);
    }

    /**
     * Carga la moneda del usuario
     */
    private void loadUserCurrency() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_CONFIG,
                new String[]{DatabaseHelper.COLUMN_CURRENCY_CODE},
                null, null, null, null, null);

        if (cursor.moveToFirst()) {
            userCurrency = cursor.getString(0);
        }
        cursor.close();
    }

    /**
     * Carga las categorías desde la base de datos
     */
    private void loadCategories() {
        expenseCategories = new ArrayList<>();
        incomeCategories = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                null,
                DatabaseHelper.COLUMN_IS_ACTIVE + " = 1",
                null, null, null,
                DatabaseHelper.COLUMN_NAME + " ASC");

        while (cursor.moveToNext()) {
            Category category = new Category();
            category.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
            category.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)));
            category.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE)));
            category.setIcon(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ICON)));
            category.setColor(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COLOR)));

            if (category.getType().equals(DatabaseHelper.TYPE_EXPENSE)) {
                expenseCategories.add(category);
            } else {
                incomeCategories.add(category);
            }
        }
        cursor.close();

        // Actualizar spinner con categorías de gastos por defecto
        updateCategorySpinner();
    }

    /**
     * Actualiza el spinner de categorías según el tipo seleccionado
     */
    private void updateCategorySpinner() {
        List<Category> categories = transactionType.equals(DatabaseHelper.TYPE_EXPENSE)
                ? expenseCategories : incomeCategories;

        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    /**
     * Carga los métodos de pago
     */
    private void loadPaymentMethods() {
        paymentMethods = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PAYMENT_METHODS,
                new String[]{DatabaseHelper.COLUMN_NAME},
                DatabaseHelper.COLUMN_IS_ACTIVE + " = 1",
                null, null, null,
                DatabaseHelper.COLUMN_NAME + " ASC");

        while (cursor.moveToNext()) {
            paymentMethods.add(cursor.getString(0));
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, paymentMethods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentMethod.setAdapter(adapter);
    }

    /**
     * Configura los listeners
     */
    private void setupListeners() {
        // Radio group para tipo de transacción
        rgTransactionType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbIncome) {
                    transactionType = DatabaseHelper.TYPE_INCOME;
                } else {
                    transactionType = DatabaseHelper.TYPE_EXPENSE;
                }
                updateCategorySpinner();
            }
        });

        // Botón de fecha
        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Botón de conversión de moneda
        btnConvertCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertCurrency();
            }
        });

        // Botón guardar
        btnSaveTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTransaction();
            }
        });

        // Botón cancelar
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Muestra el selector de fecha
     */
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate.set(year, month, dayOfMonth);
                        updateDateDisplay();
                    }
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /**
     * Actualiza la visualización de la fecha
     */
    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvSelectedDate.setText(sdf.format(selectedDate.getTime()));
    }

    /**
     * Convierte la moneda usando API
     * TODO: Implementar llamada a API real
     */
    private void convertCurrency() {
        String originalAmountStr = etOriginalAmount.getText().toString().trim();

        if (TextUtils.isEmpty(originalAmountStr)) {
            etOriginalAmount.setError("Ingresa el monto");
            return;
        }

        double originalAmount = Double.parseDouble(originalAmountStr);
        String originalCurrency = spinnerOriginalCurrency.getSelectedItem().toString();

        // Por ahora usar tasa simulada (TODO: implementar API real)
        double exchangeRate = getExchangeRate(originalCurrency, userCurrency);
        double convertedAmount = originalAmount * exchangeRate;

        etAmount.setText(String.format(Locale.getDefault(), "%.2f", convertedAmount));
        tvExchangeRate.setText(String.format(Locale.getDefault(),
                "Tasa: 1 %s = %.4f %s", originalCurrency, exchangeRate, userCurrency));
        tvExchangeRate.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Conversión realizada", Toast.LENGTH_SHORT).show();
    }

    /**
     * Obtiene la tasa de cambio (simulada por ahora)
     * TODO: Implementar llamada real a API
     */
    private double getExchangeRate(String from, String to) {
        if (from.equals(to)) return 1.0;

        // Tasas simuladas (reemplazar con API real)
        if (from.equals("USD") && to.equals("MXN")) return 17.5;
        if (from.equals("EUR") && to.equals("USD")) return 1.1;
        if (from.equals("USD") && to.equals("EUR")) return 0.91;

        return 1.0; // Por defecto
    }

    /**
     * Guarda la transacción
     */
    private void saveTransaction() {
        // Validar campos
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Ingresa el monto");
            etAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etAmount.setError("El monto debe ser mayor a 0");
                etAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etAmount.setError("Monto inválido");
            etAmount.requestFocus();
            return;
        }

        if (spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(this, "Selecciona una categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener categoría seleccionada
        String categoryName = spinnerCategory.getSelectedItem().toString();
        List<Category> categories = transactionType.equals(DatabaseHelper.TYPE_EXPENSE)
                ? expenseCategories : incomeCategories;

        int categoryId = -1;
        for (Category category : categories) {
            if (category.getName().equals(categoryName)) {
                categoryId = category.getId();
                break;
            }
        }

        // Preparar valores
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TYPE, transactionType);
        values.put(DatabaseHelper.COLUMN_AMOUNT, amount);
        values.put(DatabaseHelper.COLUMN_CATEGORY_ID, categoryId);
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, description);
        values.put(DatabaseHelper.COLUMN_TRANSACTION_DATE, selectedDate.getTimeInMillis() / 1000);
        values.put(DatabaseHelper.COLUMN_PAYMENT_METHOD,
                spinnerPaymentMethod.getSelectedItem().toString());

        // Guardar datos de conversión si existen
        String originalAmountStr = etOriginalAmount.getText().toString().trim();
        if (!TextUtils.isEmpty(originalAmountStr)) {
            values.put(DatabaseHelper.COLUMN_ORIGINAL_CURRENCY,
                    spinnerOriginalCurrency.getSelectedItem().toString());
            values.put(DatabaseHelper.COLUMN_ORIGINAL_AMOUNT,
                    Double.parseDouble(originalAmountStr));
            // Calcular tasa de cambio
            double originalAmount = Double.parseDouble(originalAmountStr);
            double exchangeRate = amount / originalAmount;
            values.put(DatabaseHelper.COLUMN_EXCHANGE_RATE, exchangeRate);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result;

        if (isEditMode) {
            // Actualizar transacción existente
            result = db.update(DatabaseHelper.TABLE_TRANSACTIONS, values,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(transactionId)});

            if (result > 0) {
                Toast.makeText(this, "Transacción actualizada", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Insertar nueva transacción
            result = db.insert(DatabaseHelper.TABLE_TRANSACTIONS, null, values);

            if (result != -1) {
                Toast.makeText(this, "Transacción guardada", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Carga los datos de una transacción existente (modo edición)
     */
    private void loadTransactionData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_TRANSACTIONS,
                null,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(transactionId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            // Tipo
            String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE));
            if (type.equals(DatabaseHelper.TYPE_INCOME)) {
                rbIncome.setChecked(true);
                transactionType = DatabaseHelper.TYPE_INCOME;
            } else {
                rbExpense.setChecked(true);
                transactionType = DatabaseHelper.TYPE_EXPENSE;
            }
            updateCategorySpinner();

            // Monto
            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT));
            etAmount.setText(String.valueOf(amount));

            // Categoría
            int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID));
            String categoryName = getCategoryName(categoryId);
            int categoryPosition = ((ArrayAdapter<String>)spinnerCategory.getAdapter()).getPosition(categoryName);
            spinnerCategory.setSelection(categoryPosition);

            // Descripción
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
            if (description != null) {
                etDescription.setText(description);
            }

            // Fecha
            long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_DATE));
            selectedDate.setTimeInMillis(timestamp * 1000);
            updateDateDisplay();

            // Método de pago
            String paymentMethod = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PAYMENT_METHOD));
            if (paymentMethod != null) {
                int paymentPosition = ((ArrayAdapter<String>)spinnerPaymentMethod.getAdapter()).getPosition(paymentMethod);
                spinnerPaymentMethod.setSelection(paymentPosition);
            }
        }
        cursor.close();
    }

    /**
     * Obtiene el nombre de una categoría por su ID
     */
    private String getCategoryName(int categoryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                new String[]{DatabaseHelper.COLUMN_NAME},
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(categoryId)},
                null, null, null);

        String name = "";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
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
