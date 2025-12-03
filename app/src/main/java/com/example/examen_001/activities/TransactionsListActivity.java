package com.example.examen_001.activities;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examen_001.R;
import com.example.examen_001.adapters.TransactionAdapter;
import com.example.examen_001.database.DatabaseHelper;
import com.example.examen_001.models.Transaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class TransactionsListActivity extends AppCompatActivity
        implements TransactionAdapter.OnTransactionClickListener{

private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private TextView tvEmptyState;
    private TextView tvTotalIncome;
    private TextView tvTotalExpense;
    private TextView tvBalance;
    private FloatingActionButton fabAddTransaction;
    private View summaryCard;

    private DatabaseHelper dbHelper;
    private List<Transaction> transactions;
    private String currentFilter = "ALL"; // ALL, INCOME, EXPENSE
    private long startDateFilter = 0;
    private long endDateFilter = 0;
    private String userCurrency = "USD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions_list);

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Transacciones");

        dbHelper = new DatabaseHelper(this);
        transactions = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        loadUserCurrency();
        loadTransactions();
        setupFab();
    }

    /**
     * Inicializa las vistas
     */
    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewTransactions);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        summaryCard = findViewById(R.id.summaryCard);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvBalance = findViewById(R.id.tvBalance);
        fabAddTransaction = findViewById(R.id.fabAddTransaction);
    }

    /**
     * Configura el RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new TransactionAdapter(transactions, this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Configurar swipe to delete
        setupSwipeToDelete();
    }

    /**
     * Configura el swipe para eliminar
     */
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Transaction deletedTransaction = transactions.get(position);

                if (direction == ItemTouchHelper.LEFT) {
                    // Swipe izquierda: Eliminar
                    showDeleteConfirmation(deletedTransaction, position);
                } else {
                    // Swipe derecha: Editar
                    editTransaction(deletedTransaction);
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY,
                        actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(TransactionsListActivity.this,
                                android.R.color.holo_red_dark))
                        .addSwipeLeftActionIcon(android.R.drawable.ic_menu_delete)
                        .addSwipeLeftLabel("Eliminar")
                        .setSwipeLeftLabelColor(ContextCompat.getColor(TransactionsListActivity.this,
                                android.R.color.white))
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(TransactionsListActivity.this,
                                android.R.color.holo_blue_dark))
                        .addSwipeRightActionIcon(android.R.drawable.ic_menu_edit)
                        .addSwipeRightLabel("Editar")
                        .setSwipeRightLabelColor(ContextCompat.getColor(TransactionsListActivity.this,
                                android.R.color.white))
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
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
     * Carga las transacciones desde la base de datos
     */
    private void loadTransactions() {
        transactions.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Construir query con filtros
        StringBuilder query = new StringBuilder(
                "SELECT t.*, c." + DatabaseHelper.COLUMN_NAME + " AS category_name, " +
                        "c." + DatabaseHelper.COLUMN_ICON + " AS category_icon, " +
                        "c." + DatabaseHelper.COLUMN_COLOR + " AS category_color " +
                        "FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " t " +
                        "INNER JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c " +
                        "ON t." + DatabaseHelper.COLUMN_CATEGORY_ID + " = c." + DatabaseHelper.COLUMN_ID
        );

        List<String> selectionArgs = new ArrayList<>();
        boolean hasWhere = false;

        // Filtro por tipo
        if (!currentFilter.equals("ALL")) {
            query.append(" WHERE t." + DatabaseHelper.COLUMN_TYPE + " = ?");
            selectionArgs.add(currentFilter);
            hasWhere = true;
        }

        // Filtro por rango de fechas
        if (startDateFilter > 0 && endDateFilter > 0) {
            if (hasWhere) {
                query.append(" AND ");
            } else {
                query.append(" WHERE ");
                hasWhere = true;
            }
            query.append("t." + DatabaseHelper.COLUMN_TRANSACTION_DATE + " >= ? AND " +
                    "t." + DatabaseHelper.COLUMN_TRANSACTION_DATE + " <= ?");
            selectionArgs.add(String.valueOf(startDateFilter));
            selectionArgs.add(String.valueOf(endDateFilter));
        }

        // Ordenar por fecha descendente
        query.append(" ORDER BY t." + DatabaseHelper.COLUMN_TRANSACTION_DATE + " DESC");

        Cursor cursor = db.rawQuery(query.toString(),
                selectionArgs.toArray(new String[0]));

        while (cursor.moveToNext()) {
            Transaction transaction = new Transaction();
            transaction.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
            transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE)));
            transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT)));
            transaction.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID)));
            transaction.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow("category_name")));
            transaction.setCategoryIcon(cursor.getString(cursor.getColumnIndexOrThrow("category_icon")));
            transaction.setCategoryColor(cursor.getString(cursor.getColumnIndexOrThrow("category_color")));
            transaction.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)));
            transaction.setTransactionDate(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_DATE)));
            transaction.setPaymentMethod(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PAYMENT_METHOD)));

            transactions.add(transaction);
        }
        cursor.close();

        // Actualizar UI
        updateUI();
    }

    /**
     * Actualiza la interfaz según los datos
     */
    private void updateUI() {
        if (transactions.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            summaryCard.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            summaryCard.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            updateSummary();
        }
    }

    /**
     * Actualiza el resumen financiero
     */
    private void updateSummary() {
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction transaction : transactions) {
            if (transaction.getType().equals(DatabaseHelper.TYPE_INCOME)) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpense += transaction.getAmount();
            }
        }

        double balance = totalIncome - totalExpense;

        NumberFormat currencyFormat = getCurrencyFormat();
        tvTotalIncome.setText(currencyFormat.format(totalIncome));
        tvTotalExpense.setText(currencyFormat.format(totalExpense));
        tvBalance.setText(currencyFormat.format(balance));

        // Color del balance
        if (balance >= 0) {
            tvBalance.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
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

    /**
     * Configura el FAB
     */
    private void setupFab() {
        fabAddTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TransactionsListActivity.this, AddTransactionActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        editTransaction(transaction);
    }

    /**
     * Edita una transacción
     */
    private void editTransaction(Transaction transaction) {
        Intent intent = new Intent(this, AddTransactionActivity.class);
        intent.putExtra("transaction_id", transaction.getId());
        startActivityForResult(intent, 1);
    }

    /**
     * Muestra confirmación para eliminar
     */
    private void showDeleteConfirmation(final Transaction transaction, final int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Transacción")
                .setMessage("¿Estás seguro de que deseas eliminar esta transacción?")
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTransaction(transaction, position);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.notifyItemChanged(position);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        adapter.notifyItemChanged(position);
                    }
                })
                .show();
    }

    /**
     * Elimina una transacción
     */
    private void deleteTransaction(final Transaction transaction, final int position) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deleted = db.delete(DatabaseHelper.TABLE_TRANSACTIONS,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(transaction.getId())});

        if (deleted > 0) {
            transactions.remove(position);
            adapter.notifyItemRemoved(position);
            updateUI();

            // Mostrar Snackbar con opción de deshacer
            Snackbar.make(recyclerView, "Transacción eliminada", Snackbar.LENGTH_LONG)
                    .setAction("Deshacer", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            undoDelete(transaction, position);
                        }
                    })
                    .show();
        } else {
            Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
            adapter.notifyItemChanged(position);
        }
    }

    /**
     * Deshace la eliminación
     */
    private void undoDelete(Transaction transaction, int position) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(DatabaseHelper.COLUMN_TYPE, transaction.getType());
        values.put(DatabaseHelper.COLUMN_AMOUNT, transaction.getAmount());
        values.put(DatabaseHelper.COLUMN_CATEGORY_ID, transaction.getCategoryId());
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, transaction.getDescription());
        values.put(DatabaseHelper.COLUMN_TRANSACTION_DATE, transaction.getTransactionDate());
        values.put(DatabaseHelper.COLUMN_PAYMENT_METHOD, transaction.getPaymentMethod());

        long newId = db.insert(DatabaseHelper.TABLE_TRANSACTIONS, null, values);
        if (newId != -1) {
            transaction.setId(newId);
            transactions.add(position, transaction);
            adapter.notifyItemInserted(position);
            updateUI();
            Toast.makeText(this, "Transacción restaurada", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_transactions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_filter_all) {
            currentFilter = "ALL";
            loadTransactions();
            return true;
        } else if (id == R.id.action_filter_income) {
            currentFilter = DatabaseHelper.TYPE_INCOME;
            loadTransactions();
            return true;
        } else if (id == R.id.action_filter_expense) {
            currentFilter = DatabaseHelper.TYPE_EXPENSE;
            loadTransactions();
            return true;
        } else if (id == R.id.action_filter_date) {
            showDateRangeDialog();
            return true;
        } else if (id == R.id.action_clear_filters) {
            clearFilters();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Muestra diálogo para filtrar por rango de fechas
     */
    private void showDateRangeDialog() {
        final Calendar startCal = Calendar.getInstance();
        final Calendar endCal = Calendar.getInstance();

        // Selector de fecha inicial
        DatePickerDialog startPicker = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        startCal.set(year, month, dayOfMonth, 0, 0, 0);
                        startDateFilter = startCal.getTimeInMillis() / 1000;

                        // Mostrar selector de fecha final
                        DatePickerDialog endPicker = new DatePickerDialog(TransactionsListActivity.this,
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                        endCal.set(year, month, dayOfMonth, 23, 59, 59);
                                        endDateFilter = endCal.getTimeInMillis() / 1000;
                                        loadTransactions();

                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                        Toast.makeText(TransactionsListActivity.this,
                                                "Filtrado: " + sdf.format(startCal.getTime()) + " - " +
                                                        sdf.format(endCal.getTime()), Toast.LENGTH_LONG).show();
                                    }
                                },
                                endCal.get(Calendar.YEAR),
                                endCal.get(Calendar.MONTH),
                                endCal.get(Calendar.DAY_OF_MONTH)
                        );
                        endPicker.setTitle("Fecha Final");
                        endPicker.show();
                    }
                },
                startCal.get(Calendar.YEAR),
                startCal.get(Calendar.MONTH),
                startCal.get(Calendar.DAY_OF_MONTH)
        );
        startPicker.setTitle("Fecha Inicial");
        startPicker.show();
    }

    /**
     * Limpia todos los filtros
     */
    private void clearFilters() {
        currentFilter = "ALL";
        startDateFilter = 0;
        endDateFilter = 0;
        loadTransactions();
        Toast.makeText(this, "Filtros eliminados", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadTransactions();
        }
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
