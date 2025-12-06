package com.example.examen_001.activities;



import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.examen_001.R;
import com.example.examen_001.database.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * MainActivity - Dashboard Principal
 * Muestra resumen financiero del mes actual
 */
public class MainActivity extends AppCompatActivity {

    // Views principales
    private TextView tvUserGreeting;
    private TextView tvCurrentMonth;
    private TextView tvIncome;
    private TextView tvExpenses;
    private TextView tvBalance;
    private TextView tvBudget;
    private TextView tvBudgetRemaining;
    private TextView tvDaysRemaining;
    private ProgressBar progressBudget;
    private TextView tvProgressPercentage;
    private CardView cardAlert;
    private TextView tvAlertMessage;
    private FloatingActionButton fabAddTransaction;

    // Cards de navegación
    private CardView cardTransactions;
    private CardView cardStatistics;
    private CardView cardSettings;

    private DatabaseHelper dbHelper;
    private String currencyCode = "USD";
    private double monthlyBudget = 0;
    private int monthStartDay = 1;
    private int alertThreshold = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        initializeViews();
        loadUserConfiguration();
        loadDashboardData();
        setupClickListeners();
    }

    /**
     * Inicializa todas las vistas
     */
    private void initializeViews() {
        tvUserGreeting = findViewById(R.id.tvUserGreeting);
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpenses = findViewById(R.id.tvExpenses);
        tvBalance = findViewById(R.id.tvBalance);
        tvBudget = findViewById(R.id.tvBudget);
        tvBudgetRemaining = findViewById(R.id.tvBudgetRemaining);
        tvDaysRemaining = findViewById(R.id.tvDaysRemaining);
        progressBudget = findViewById(R.id.progressBudget);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        cardAlert = findViewById(R.id.cardAlert);
        tvAlertMessage = findViewById(R.id.tvAlertMessage);
        fabAddTransaction = findViewById(R.id.fabAddTransaction);

        cardTransactions = findViewById(R.id.cardTransactions);
        cardStatistics = findViewById(R.id.cardStatistics);
        cardSettings = findViewById(R.id.cardSettings);
    }

    /**
     * Carga la configuración del usuario
     */
    private void loadUserConfiguration() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USER_CONFIG,
                null, null, null, null, null, null
        );

        if (cursor.moveToFirst()) {
            String userName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME));
            monthlyBudget = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MONTHLY_BUDGET));
            currencyCode = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CURRENCY_CODE));
            monthStartDay = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MONTH_START_DAY));
            alertThreshold = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALERT_THRESHOLD));

            // Establecer saludo
            String greeting = getGreeting() + ", " + userName + "!";
            tvUserGreeting.setText(greeting);
        }
        cursor.close();
    }

    /**
     * Carga los datos del dashboard
     */
    private void loadDashboardData() {
        long[] dateRange = getCurrentPeriodRange();
        long startDate = dateRange[0];
        long endDate = dateRange[1];

        // Establecer mes actual
        Calendar cal = Calendar.getInstance();
        String[] months = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        String currentMonth = months[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.YEAR);
        tvCurrentMonth.setText(currentMonth);

        // Calcular ingresos y gastos del período
        double totalIncome = getTransactionSum(DatabaseHelper.TYPE_INCOME, startDate, endDate);
        double totalExpenses = getTransactionSum(DatabaseHelper.TYPE_EXPENSE, startDate, endDate);
        double balance = totalIncome - totalExpenses;

        // Formatear y mostrar valores
        NumberFormat currencyFormat = getCurrencyFormat();
        tvIncome.setText(currencyFormat.format(totalIncome));
        tvExpenses.setText(currencyFormat.format(totalExpenses));
        tvBalance.setText(currencyFormat.format(balance));

        // Balance en color
        if (balance >= 0) {
            tvBalance.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        // Progreso del presupuesto
        tvBudget.setText(currencyFormat.format(monthlyBudget));
        double budgetRemaining = monthlyBudget - totalExpenses;
        tvBudgetRemaining.setText(currencyFormat.format(budgetRemaining));

        // Calcular porcentaje usado
        int percentage = (int) ((totalExpenses / monthlyBudget) * 100);
        percentage = Math.min(percentage, 100); // Máximo 100%
        progressBudget.setProgress(percentage);
        tvProgressPercentage.setText(percentage + "%");

        // Cambiar color del progreso según el porcentaje
        if (percentage >= alertThreshold) {
            progressBudget.setProgressTintList(
                    getResources().getColorStateList(android.R.color.holo_red_dark)
            );
        } else if (percentage >= 60) {
            progressBudget.setProgressTintList(
                    getResources().getColorStateList(android.R.color.holo_orange_dark)
            );
        }

        // Días restantes
        int daysRemaining = getDaysRemainingInPeriod();
        tvDaysRemaining.setText(daysRemaining + " días restantes");

        // Mostrar alerta si supera el umbral
        if (percentage >= alertThreshold) {
            cardAlert.setVisibility(View.VISIBLE);
            String alertMsg = "⚠️ Has usado el " + percentage + "% de tu presupuesto mensual";
            tvAlertMessage.setText(alertMsg);
        } else {
            cardAlert.setVisibility(View.GONE);
        }
    }

    /**
     * Obtiene la suma de transacciones por tipo en un rango de fechas
     */
    private double getTransactionSum(String type, long startDate, long endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") FROM " +
                DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_TYPE + " = ?" +
                " AND " + DatabaseHelper.COLUMN_TRANSACTION_DATE + " >= ?" +
                " AND " + DatabaseHelper.COLUMN_TRANSACTION_DATE + " <= ?";

        Cursor cursor = db.rawQuery(query, new String[]{
                type,
                String.valueOf(startDate),
                String.valueOf(endDate)
        });

        double sum = 0;
        if (cursor.moveToFirst()) {
            sum = cursor.getDouble(0);
        }
        cursor.close();
        return sum;
    }

    /**
     * Calcula el rango de fechas del período actual
     */
    private long[] getCurrentPeriodRange() {
        Calendar cal = Calendar.getInstance();
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);

        // Si el día actual es anterior al día de inicio, retroceder un mes
        if (currentDay < monthStartDay) {
            cal.add(Calendar.MONTH, -1);
        }

        // Fecha de inicio del período
        cal.set(Calendar.DAY_OF_MONTH, monthStartDay);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startDate = cal.getTimeInMillis() / 1000;

        // Fecha de fin del período (día anterior al inicio del próximo período)
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        long endDate = cal.getTimeInMillis() / 1000;

        return new long[]{startDate, endDate};
    }

    /**
     * Calcula los días restantes en el período actual
     */
    private int getDaysRemainingInPeriod() {
        long[] dateRange = getCurrentPeriodRange();
        long endDate = dateRange[1];
        long currentDate = System.currentTimeMillis() / 1000;
        long diffInSeconds = endDate - currentDate;
        return (int) (diffInSeconds / (60 * 60 * 24)) + 1;
    }

    /**
     * Obtiene el saludo según la hora del día
     */
    private String getGreeting() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour >= 0 && hour < 12) {
            return "Buenos días";
        } else if (hour >= 12 && hour < 18) {
            return "Buenas tardes";
        } else {
            return "Buenas noches";
        }
    }

    /**
     * Obtiene el formato de moneda según el código
     */
    private NumberFormat getCurrencyFormat() {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        try {
            java.util.Currency currency = java.util.Currency.getInstance(currencyCode);
            format.setCurrency(currency);
        } catch (Exception e) {
            // Si falla, usar formato por defecto
        }
        return format;
    }

    /**
     * Configura los listeners de clicks
     */
    private void setupClickListeners() {
        // Botón flotante para agregar transacción
        fabAddTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        // Card de Transacciones
        cardTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TransactionsListActivity.class);
                startActivity(intent);
            }
        });

        // Card de Estadísticas
        cardStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(intent);
            }
        });

        // Card de Configuración
        cardSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Abrir configuración
                // Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                // startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos al volver a la actividad
        loadDashboardData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Recargar datos después de agregar/editar transacción
            loadDashboardData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}