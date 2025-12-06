package com.example.examen_001.activities;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.examen_001.R;
import com.example.examen_001.database.DatabaseHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity para mostrar estadísticas y gráficos
 */
public class StatisticsActivity extends AppCompatActivity {

    // Views
    private Spinner spinnerPeriod;
    private PieChart pieChartExpenses;
    private BarChart barChartComparison;
    private LineChart lineChartTrends;
    private TextView tvTotalExpenses;
    private TextView tvTotalIncome;
    private TextView tvAverageDaily;
    private TextView tvTopCategory;
    private TextView tvTransactionCount;
    private CardView cardEmptyState;

    private DatabaseHelper dbHelper;
    private String userCurrency = "USD";
    private int selectedPeriod = 0; // 0=Este mes, 1=Últimos 3 meses, 2=Últimos 6 meses, 3=Este año

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statics);

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Estadísticas");

        dbHelper = new DatabaseHelper(this);

        initializeViews();
        setupPeriodSpinner();
        loadUserCurrency();
        loadStatistics();
    }

    /**
     * Inicializa las vistas
     */
    private void initializeViews() {
        spinnerPeriod = findViewById(R.id.spinnerPeriod);
        pieChartExpenses = findViewById(R.id.pieChartExpenses);
        barChartComparison = findViewById(R.id.barChartComparison);
        lineChartTrends = findViewById(R.id.lineChartTrends);
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvAverageDaily = findViewById(R.id.tvAverageDaily);
        tvTopCategory = findViewById(R.id.tvTopCategory);
        tvTransactionCount = findViewById(R.id.tvTransactionCount);
        cardEmptyState = findViewById(R.id.cardEmptyState);
    }

    /**
     * Configura el spinner de períodos
     */
    private void setupPeriodSpinner() {
        String[] periods = {"Este mes", "Últimos 3 meses", "Últimos 6 meses", "Este año"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, periods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(adapter);

        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPeriod = position;
                loadStatistics();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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
     * Carga todas las estadísticas
     */
    private void loadStatistics() {
        long[] dateRange = getDateRange();
        long startDate = dateRange[0];
        long endDate = dateRange[1];

        // Verificar si hay datos
        if (!hasTransactions(startDate, endDate)) {
            showEmptyState();
            return;
        }

        hideEmptyState();
        loadSummaryStats(startDate, endDate);
        loadPieChart(startDate, endDate);
        loadBarChart(startDate, endDate);
        loadLineChart(startDate, endDate);
    }

    /**
     * Verifica si hay transacciones en el período
     */
    private boolean hasTransactions(long startDate, long endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_TRANSACTION_DATE + " >= ? " +
                "AND " + DatabaseHelper.COLUMN_TRANSACTION_DATE + " <= ?";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(startDate), String.valueOf(endDate)
        });

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count > 0;
    }

    /**
     * Muestra estado vacío
     */
    private void showEmptyState() {
        cardEmptyState.setVisibility(View.VISIBLE);
        pieChartExpenses.setVisibility(View.GONE);
        barChartComparison.setVisibility(View.GONE);
        lineChartTrends.setVisibility(View.GONE);
    }

    /**
     * Oculta estado vacío
     */
    private void hideEmptyState() {
        cardEmptyState.setVisibility(View.GONE);
        pieChartExpenses.setVisibility(View.VISIBLE);
        barChartComparison.setVisibility(View.VISIBLE);
        lineChartTrends.setVisibility(View.VISIBLE);
    }

    /**
     * Obtiene el rango de fechas según el período seleccionado
     */
    private long[] getDateRange() {
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

        switch (selectedPeriod) {
            case 0: // Este mes
                startCal.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case 1: // Últimos 3 meses
                startCal.add(Calendar.MONTH, -3);
                break;
            case 2: // Últimos 6 meses
                startCal.add(Calendar.MONTH, -6);
                break;
            case 3: // Este año
                startCal.set(Calendar.DAY_OF_YEAR, 1);
                break;
        }

        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);

        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);

        return new long[]{startCal.getTimeInMillis() / 1000, endCal.getTimeInMillis() / 1000};
    }

    /**
     * Carga estadísticas resumidas
     */
    private void loadSummaryStats(long startDate, long endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        NumberFormat currencyFormat = getCurrencyFormat();

        // Total de ingresos
        double totalIncome = getTransactionSum(DatabaseHelper.TYPE_INCOME, startDate, endDate);
        tvTotalIncome.setText(currencyFormat.format(totalIncome));

        // Total de gastos
        double totalExpenses = getTransactionSum(DatabaseHelper.TYPE_EXPENSE, startDate, endDate);
        tvTotalExpenses.setText(currencyFormat.format(totalExpenses));

        // Número de transacciones
        int transactionCount = getTransactionCount(startDate, endDate);
        tvTransactionCount.setText(String.valueOf(transactionCount));

        // Promedio diario
        long days = (endDate - startDate) / (60 * 60 * 24);
        if (days > 0) {
            double averageDaily = totalExpenses / days;
            tvAverageDaily.setText(currencyFormat.format(averageDaily));
        } else {
            tvAverageDaily.setText(currencyFormat.format(0));
        }

        // Categoría con más gastos
        String topCategory = getTopExpenseCategory(startDate, endDate);
        tvTopCategory.setText(topCategory);
    }

    /**
     * Obtiene la suma de transacciones por tipo
     */
    private double getTransactionSum(String type, long startDate, long endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") FROM " +
                DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_TYPE + " = ?" +
                " AND " + DatabaseHelper.COLUMN_TRANSACTION_DATE + " >= ?" +
                " AND " + DatabaseHelper.COLUMN_TRANSACTION_DATE + " <= ?";

        Cursor cursor = db.rawQuery(query, new String[]{type,
                String.valueOf(startDate), String.valueOf(endDate)});

        double sum = 0;
        if (cursor.moveToFirst()) {
            sum = cursor.getDouble(0);
        }
        cursor.close();
        return sum;
    }

    /**
     * Obtiene el número de transacciones
     */
    private int getTransactionCount(long startDate, long endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_TRANSACTION_DATE + " >= ?" +
                " AND " + DatabaseHelper.COLUMN_TRANSACTION_DATE + " <= ?";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(startDate), String.valueOf(endDate)});

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * Obtiene la categoría con más gastos
     */
    private String getTopExpenseCategory(long startDate, long endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT c." + DatabaseHelper.COLUMN_NAME +
                ", SUM(t." + DatabaseHelper.COLUMN_AMOUNT + ") as total " +
                "FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " t " +
                "INNER JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c " +
                "ON t." + DatabaseHelper.COLUMN_CATEGORY_ID + " = c." + DatabaseHelper.COLUMN_ID +
                " WHERE t." + DatabaseHelper.COLUMN_TYPE + " = ?" +
                " AND t." + DatabaseHelper.COLUMN_TRANSACTION_DATE + " >= ?" +
                " AND t." + DatabaseHelper.COLUMN_TRANSACTION_DATE + " <= ?" +
                " GROUP BY c." + DatabaseHelper.COLUMN_NAME +
                " ORDER BY total DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, new String[]{DatabaseHelper.TYPE_EXPENSE,
                String.valueOf(startDate), String.valueOf(endDate)});

        String category = "N/A";
        if (cursor.moveToFirst()) {
            category = cursor.getString(0);
        }
        cursor.close();
        return category;
    }

    /**
     * Carga el gráfico de pastel (gastos por categoría)
     */
    private void loadPieChart(long startDate, long endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT c." + DatabaseHelper.COLUMN_NAME +
                ", c." + DatabaseHelper.COLUMN_COLOR +
                ", SUM(t." + DatabaseHelper.COLUMN_AMOUNT + ") as total " +
                "FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " t " +
                "INNER JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c " +
                "ON t." + DatabaseHelper.COLUMN_CATEGORY_ID + " = c." + DatabaseHelper.COLUMN_ID +
                " WHERE t." + DatabaseHelper.COLUMN_TYPE + " = ?" +
                " AND t." + DatabaseHelper.COLUMN_TRANSACTION_DATE + " >= ?" +
                " AND t." + DatabaseHelper.COLUMN_TRANSACTION_DATE + " <= ?" +
                " GROUP BY c." + DatabaseHelper.COLUMN_NAME + ", c." + DatabaseHelper.COLUMN_COLOR +
                " ORDER BY total DESC";

        Cursor cursor = db.rawQuery(query, new String[]{DatabaseHelper.TYPE_EXPENSE,
                String.valueOf(startDate), String.valueOf(endDate)});

        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        while (cursor.moveToNext()) {
            String category = cursor.getString(0);
            String colorHex = cursor.getString(1);
            float amount = cursor.getFloat(2);

            entries.add(new PieEntry(amount, category));

            try {
                colors.add(Color.parseColor(colorHex));
            } catch (Exception e) {
                colors.add(Color.GRAY);
            }
        }
        cursor.close();

        if (entries.isEmpty()) {
            pieChartExpenses.setVisibility(View.GONE);
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(pieChartExpenses));
        dataSet.setSliceSpace(3f);

        PieData data = new PieData(dataSet);

        pieChartExpenses.setData(data);
        pieChartExpenses.setUsePercentValues(true);
        pieChartExpenses.getDescription().setEnabled(false);
        pieChartExpenses.setDrawHoleEnabled(true);
        pieChartExpenses.setHoleColor(Color.WHITE);
        pieChartExpenses.setTransparentCircleRadius(58f);
        pieChartExpenses.setDrawEntryLabels(false);
        pieChartExpenses.setCenterText("Gastos por\nCategoría");
        pieChartExpenses.setCenterTextSize(14f);
        pieChartExpenses.setRotationEnabled(true);

        Legend legend = pieChartExpenses.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setWordWrapEnabled(true);

        pieChartExpenses.animateY(1000);
        pieChartExpenses.invalidate();
    }

    /**
     * Carga el gráfico de barras (comparación ingresos vs gastos)
     */
    private void loadBarChart(long startDate, long endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Obtener datos por mes
        String query = "SELECT strftime('%m', datetime(" + DatabaseHelper.COLUMN_TRANSACTION_DATE + ", 'unixepoch')) as month, " +
                DatabaseHelper.COLUMN_TYPE + ", " +
                "SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") as total " +
                "FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_TRANSACTION_DATE + " >= ?" +
                " AND " + DatabaseHelper.COLUMN_TRANSACTION_DATE + " <= ?" +
                " GROUP BY month, " + DatabaseHelper.COLUMN_TYPE +
                " ORDER BY month";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(startDate), String.valueOf(endDate)});

        Map<String, Float> incomeData = new HashMap<>();
        Map<String, Float> expenseData = new HashMap<>();
        List<String> months = new ArrayList<>();

        while (cursor.moveToNext()) {
            String month = cursor.getString(0);
            String type = cursor.getString(1);
            float amount = cursor.getFloat(2);

            if (!months.contains(month)) {
                months.add(month);
            }

            if (type.equals(DatabaseHelper.TYPE_INCOME)) {
                incomeData.put(month, amount);
            } else {
                expenseData.put(month, amount);
            }
        }
        cursor.close();

        if (months.isEmpty()) {
            barChartComparison.setVisibility(View.GONE);
            return;
        }

        ArrayList<BarEntry> incomeEntries = new ArrayList<>();
        ArrayList<BarEntry> expenseEntries = new ArrayList<>();

        for (int i = 0; i < months.size(); i++) {
            String month = months.get(i);
            incomeEntries.add(new BarEntry(i, incomeData.getOrDefault(month, 0f)));
            expenseEntries.add(new BarEntry(i, expenseData.getOrDefault(month, 0f)));
        }

        BarDataSet incomeSet = new BarDataSet(incomeEntries, "Ingresos");
        incomeSet.setColor(Color.parseColor("#4CAF50"));

        BarDataSet expenseSet = new BarDataSet(expenseEntries, "Gastos");
        expenseSet.setColor(Color.parseColor("#F44336"));

        BarData data = new BarData(incomeSet, expenseSet);
        data.setBarWidth(0.4f);

        barChartComparison.setData(data);
        barChartComparison.getDescription().setEnabled(false);
        barChartComparison.setFitBars(true);
        barChartComparison.groupBars(0, 0.3f, 0.05f);

        XAxis xAxis = barChartComparison.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getMonthLabels(months)));

        barChartComparison.getAxisLeft().setAxisMinimum(0f);
        barChartComparison.getAxisRight().setEnabled(false);
        barChartComparison.getLegend().setEnabled(true);

        barChartComparison.animateY(1000);
        barChartComparison.invalidate();
    }

    /**
     * Carga el gráfico de línea (tendencias)
     */
    private void loadLineChart(long startDate, long endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT strftime('%d', datetime(" + DatabaseHelper.COLUMN_TRANSACTION_DATE + ", 'unixepoch')) as day, " +
                "SUM(CASE WHEN " + DatabaseHelper.COLUMN_TYPE + " = '" + DatabaseHelper.TYPE_EXPENSE + "' THEN " +
                DatabaseHelper.COLUMN_AMOUNT + " ELSE 0 END) as expenses " +
                "FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COLUMN_TRANSACTION_DATE + " >= ?" +
                " AND " + DatabaseHelper.COLUMN_TRANSACTION_DATE + " <= ?" +
                " GROUP BY day " +
                "ORDER BY day";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(startDate), String.valueOf(endDate)});

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int index = 0;
        while (cursor.moveToNext()) {
            String day = cursor.getString(0);
            float amount = cursor.getFloat(1);
            entries.add(new Entry(index, amount));
            labels.add(day);
            index++;
        }
        cursor.close();

        if (entries.isEmpty()) {
            lineChartTrends.setVisibility(View.GONE);
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Gastos Diarios");
        dataSet.setColor(Color.parseColor("#FF5722"));
        dataSet.setCircleColor(Color.parseColor("#FF5722"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#FFCCBC"));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData data = new LineData(dataSet);

        lineChartTrends.setData(data);
        lineChartTrends.getDescription().setEnabled(false);
        lineChartTrends.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChartTrends.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChartTrends.getAxisRight().setEnabled(false);
        lineChartTrends.getLegend().setEnabled(true);

        lineChartTrends.animateX(1000);
        lineChartTrends.invalidate();
    }

    /**
     * Convierte números de mes a nombres
     */
    private List<String> getMonthLabels(List<String> months) {
        String[] monthNames = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        List<String> labels = new ArrayList<>();
        for (String month : months) {
            int monthNum = Integer.parseInt(month) - 1;
            labels.add(monthNames[monthNum]);
        }
        return labels;
    }

    /**
     * Obtiene el formato de moneda
     */
    private NumberFormat getCurrencyFormat() {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        try {
            java.util.Currency currency = java.util.Currency.getInstance(userCurrency);
            format.setCurrency(currency);
        } catch (Exception e) {
            // Usar formato por defecto
        }
        return format;
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