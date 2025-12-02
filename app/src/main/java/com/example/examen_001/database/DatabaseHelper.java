package com.example.examen_001.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DatabaseHelper para la aplicación de Finanzas Personales
 * Maneja la creación, actualización y población inicial de la base de datos SQLite
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "finance_app.db";
    private static final int DATABASE_VERSION = 1;

    // Nombres de tablas
    public static final String TABLE_USER_CONFIG = "user_config";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_PAYMENT_METHODS = "payment_methods";
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String TABLE_EXCHANGE_RATES = "exchange_rates";
    public static final String TABLE_CATEGORY_BUDGETS = "category_budgets";
    public static final String TABLE_ALERTS = "alerts";

    // Columnas comunes
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    // Columnas user_config
    public static final String COLUMN_USER_NAME = "user_name";
    public static final String COLUMN_MONTHLY_BUDGET = "monthly_budget";
    public static final String COLUMN_CURRENCY_CODE = "currency_code";
    public static final String COLUMN_MONTH_START_DAY = "month_start_day";
    public static final String COLUMN_ALERT_THRESHOLD = "alert_threshold";

    // Columnas categories
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_ICON = "icon";
    public static final String COLUMN_COLOR = "color";
    public static final String COLUMN_IS_PREDEFINED = "is_predefined";
    public static final String COLUMN_IS_ACTIVE = "is_active";

    // Columnas transactions
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_CATEGORY_ID = "category_id";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_TRANSACTION_DATE = "transaction_date";
    public static final String COLUMN_PAYMENT_METHOD = "payment_method";
    public static final String COLUMN_ORIGINAL_CURRENCY = "original_currency";
    public static final String COLUMN_ORIGINAL_AMOUNT = "original_amount";
    public static final String COLUMN_EXCHANGE_RATE = "exchange_rate";

    // Columnas exchange_rates
    public static final String COLUMN_BASE_CURRENCY = "base_currency";
    public static final String COLUMN_TARGET_CURRENCY = "target_currency";
    public static final String COLUMN_RATE = "rate";
    public static final String COLUMN_LAST_UPDATED = "last_updated";

    // Columnas category_budgets
    public static final String COLUMN_BUDGET_AMOUNT = "budget_amount";
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_YEAR = "year";

    // Columnas alerts
    public static final String COLUMN_ALERT_TYPE = "alert_type";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_IS_READ = "is_read";

    // Tipos de transacciones
    public static final String TYPE_INCOME = "INCOME";
    public static final String TYPE_EXPENSE = "EXPENSE";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear todas las tablas
        createUserConfigTable(db);
        createCategoriesTable(db);
        createPaymentMethodsTable(db);
        createTransactionsTable(db);
        createExchangeRatesTable(db);
        createCategoryBudgetsTable(db);
        createAlertsTable(db);

        // Crear índices
        createIndexes(db);

        // Crear triggers
        createTriggers(db);

        // Crear vistas
        createViews(db);

        // Insertar datos iniciales
        insertInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Estrategia de actualización
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALERTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXCHANGE_RATES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAYMENT_METHODS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_CONFIG);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ==================== CREACIÓN DE TABLAS ====================

    private void createUserConfigTable(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USER_CONFIG + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_NAME + " TEXT NOT NULL, " +
                COLUMN_MONTHLY_BUDGET + " REAL NOT NULL, " +
                COLUMN_CURRENCY_CODE + " TEXT NOT NULL DEFAULT 'USD', " +
                COLUMN_MONTH_START_DAY + " INTEGER NOT NULL DEFAULT 1, " +
                COLUMN_ALERT_THRESHOLD + " INTEGER DEFAULT 80, " +
                COLUMN_CREATED_AT + " INTEGER DEFAULT (strftime('%s', 'now')), " +
                COLUMN_UPDATED_AT + " INTEGER DEFAULT (strftime('%s', 'now'))" +
                ")";
        db.execSQL(CREATE_TABLE);
    }

    private void createCategoriesTable(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORIES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_TYPE + " TEXT NOT NULL CHECK(" + COLUMN_TYPE + " IN ('" + TYPE_INCOME + "', '" + TYPE_EXPENSE + "')), " +
                COLUMN_ICON + " TEXT, " +
                COLUMN_COLOR + " TEXT, " +
                COLUMN_IS_PREDEFINED + " INTEGER DEFAULT 1, " +
                COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1, " +
                COLUMN_CREATED_AT + " INTEGER DEFAULT (strftime('%s', 'now')), " +
                "UNIQUE(" + COLUMN_NAME + ", " + COLUMN_TYPE + ")" +
                ")";
        db.execSQL(CREATE_TABLE);
    }

    private void createPaymentMethodsTable(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PAYMENT_METHODS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL UNIQUE, " +
                COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1, " +
                COLUMN_CREATED_AT + " INTEGER DEFAULT (strftime('%s', 'now'))" +
                ")";
        db.execSQL(CREATE_TABLE);
    }

    private void createTransactionsTable(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_TRANSACTIONS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TYPE + " TEXT NOT NULL CHECK(" + COLUMN_TYPE + " IN ('" + TYPE_INCOME + "', '" + TYPE_EXPENSE + "')), " +
                COLUMN_AMOUNT + " REAL NOT NULL, " +
                COLUMN_CATEGORY_ID + " INTEGER NOT NULL, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_TRANSACTION_DATE + " INTEGER NOT NULL, " +
                COLUMN_PAYMENT_METHOD + " TEXT, " +
                COLUMN_ORIGINAL_CURRENCY + " TEXT, " +
                COLUMN_ORIGINAL_AMOUNT + " REAL, " +
                COLUMN_EXCHANGE_RATE + " REAL, " +
                COLUMN_CREATED_AT + " INTEGER DEFAULT (strftime('%s', 'now')), " +
                COLUMN_UPDATED_AT + " INTEGER DEFAULT (strftime('%s', 'now')), " +
                "FOREIGN KEY (" + COLUMN_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_ID + ") ON DELETE RESTRICT" +
                ")";
        db.execSQL(CREATE_TABLE);
    }

    private void createExchangeRatesTable(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_EXCHANGE_RATES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_BASE_CURRENCY + " TEXT NOT NULL, " +
                COLUMN_TARGET_CURRENCY + " TEXT NOT NULL, " +
                COLUMN_RATE + " REAL NOT NULL, " +
                COLUMN_LAST_UPDATED + " INTEGER NOT NULL, " +
                "UNIQUE(" + COLUMN_BASE_CURRENCY + ", " + COLUMN_TARGET_CURRENCY + ")" +
                ")";
        db.execSQL(CREATE_TABLE);
    }

    private void createCategoryBudgetsTable(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORY_BUDGETS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CATEGORY_ID + " INTEGER NOT NULL, " +
                COLUMN_BUDGET_AMOUNT + " REAL NOT NULL, " +
                COLUMN_MONTH + " INTEGER NOT NULL CHECK(" + COLUMN_MONTH + " >= 1 AND " + COLUMN_MONTH + " <= 12), " +
                COLUMN_YEAR + " INTEGER NOT NULL, " +
                COLUMN_CREATED_AT + " INTEGER DEFAULT (strftime('%s', 'now')), " +
                "FOREIGN KEY (" + COLUMN_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_ID + ") ON DELETE CASCADE, " +
                "UNIQUE(" + COLUMN_CATEGORY_ID + ", " + COLUMN_MONTH + ", " + COLUMN_YEAR + ")" +
                ")";
        db.execSQL(CREATE_TABLE);
    }

    private void createAlertsTable(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_ALERTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ALERT_TYPE + " TEXT NOT NULL, " +
                COLUMN_MESSAGE + " TEXT NOT NULL, " +
                COLUMN_IS_READ + " INTEGER DEFAULT 0, " +
                COLUMN_CREATED_AT + " INTEGER DEFAULT (strftime('%s', 'now'))" +
                ")";
        db.execSQL(CREATE_TABLE);
    }

    // ==================== CREACIÓN DE ÍNDICES ====================

    private void createIndexes(SQLiteDatabase db) {
        // Índices para categories
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_categories_type ON " + TABLE_CATEGORIES + "(" + COLUMN_TYPE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_categories_active ON " + TABLE_CATEGORIES + "(" + COLUMN_IS_ACTIVE + ")");

        // Índices para transactions
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_date ON " + TABLE_TRANSACTIONS + "(" + COLUMN_TRANSACTION_DATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_category ON " + TABLE_TRANSACTIONS + "(" + COLUMN_CATEGORY_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_type ON " + TABLE_TRANSACTIONS + "(" + COLUMN_TYPE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_type_date ON " + TABLE_TRANSACTIONS + "(" + COLUMN_TYPE + ", " + COLUMN_TRANSACTION_DATE + ")");

        // Índices para exchange_rates
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_exchange_rates_updated ON " + TABLE_EXCHANGE_RATES + "(" + COLUMN_LAST_UPDATED + ")");

        // Índices para alerts
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_alerts_read ON " + TABLE_ALERTS + "(" + COLUMN_IS_READ + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_alerts_created ON " + TABLE_ALERTS + "(" + COLUMN_CREATED_AT + ")");
    }

    // ==================== CREACIÓN DE TRIGGERS ====================

    private void createTriggers(SQLiteDatabase db) {
        // Trigger para actualizar updated_at en user_config
        String TRIGGER_USER_CONFIG = "CREATE TRIGGER IF NOT EXISTS update_user_config_timestamp " +
                "AFTER UPDATE ON " + TABLE_USER_CONFIG + " " +
                "BEGIN " +
                "UPDATE " + TABLE_USER_CONFIG + " SET " + COLUMN_UPDATED_AT + " = strftime('%s', 'now') WHERE " + COLUMN_ID + " = NEW." + COLUMN_ID + "; " +
                "END";
        db.execSQL(TRIGGER_USER_CONFIG);

        // Trigger para actualizar updated_at en transactions
        String TRIGGER_TRANSACTIONS = "CREATE TRIGGER IF NOT EXISTS update_transactions_timestamp " +
                "AFTER UPDATE ON " + TABLE_TRANSACTIONS + " " +
                "BEGIN " +
                "UPDATE " + TABLE_TRANSACTIONS + " SET " + COLUMN_UPDATED_AT + " = strftime('%s', 'now') WHERE " + COLUMN_ID + " = NEW." + COLUMN_ID + "; " +
                "END";
        db.execSQL(TRIGGER_TRANSACTIONS);
    }

    // ==================== CREACIÓN DE VISTAS ====================

    private void createViews(SQLiteDatabase db) {
        // Vista de transacciones con detalles de categoría
        String VIEW_TRANSACTIONS_DETAIL = "CREATE VIEW IF NOT EXISTS vw_transactions_detail AS " +
                "SELECT t." + COLUMN_ID + ", " +
                "t." + COLUMN_TYPE + ", " +
                "t." + COLUMN_AMOUNT + ", " +
                "t." + COLUMN_DESCRIPTION + ", " +
                "t." + COLUMN_TRANSACTION_DATE + ", " +
                "t." + COLUMN_PAYMENT_METHOD + ", " +
                "t." + COLUMN_ORIGINAL_CURRENCY + ", " +
                "t." + COLUMN_ORIGINAL_AMOUNT + ", " +
                "t." + COLUMN_EXCHANGE_RATE + ", " +
                "c." + COLUMN_NAME + " AS category_name, " +
                "c." + COLUMN_ICON + " AS category_icon, " +
                "c." + COLUMN_COLOR + " AS category_color, " +
                "t." + COLUMN_CREATED_AT + ", " +
                "t." + COLUMN_UPDATED_AT + " " +
                "FROM " + TABLE_TRANSACTIONS + " t " +
                "INNER JOIN " + TABLE_CATEGORIES + " c ON t." + COLUMN_CATEGORY_ID + " = c." + COLUMN_ID;
        db.execSQL(VIEW_TRANSACTIONS_DETAIL);

        // Vista de resumen mensual
        String VIEW_MONTHLY_SUMMARY = "CREATE VIEW IF NOT EXISTS vw_monthly_summary AS " +
                "SELECT strftime('%Y-%m', datetime(" + COLUMN_TRANSACTION_DATE + ", 'unixepoch')) AS month, " +
                COLUMN_TYPE + ", " +
                "SUM(" + COLUMN_AMOUNT + ") AS total_amount, " +
                "COUNT(*) AS transaction_count, " +
                "AVG(" + COLUMN_AMOUNT + ") AS avg_amount " +
                "FROM " + TABLE_TRANSACTIONS + " " +
                "GROUP BY month, " + COLUMN_TYPE;
        db.execSQL(VIEW_MONTHLY_SUMMARY);

        // Vista de gastos por categoría
        String VIEW_EXPENSES_BY_CATEGORY = "CREATE VIEW IF NOT EXISTS vw_expenses_by_category AS " +
                "SELECT c." + COLUMN_ID + " AS category_id, " +
                "c." + COLUMN_NAME + " AS category_name, " +
                "c." + COLUMN_ICON + ", " +
                "c." + COLUMN_COLOR + ", " +
                "SUM(t." + COLUMN_AMOUNT + ") AS total_amount, " +
                "COUNT(t." + COLUMN_ID + ") AS transaction_count, " +
                "AVG(t." + COLUMN_AMOUNT + ") AS avg_amount " +
                "FROM " + TABLE_CATEGORIES + " c " +
                "LEFT JOIN " + TABLE_TRANSACTIONS + " t ON c." + COLUMN_ID + " = t." + COLUMN_CATEGORY_ID + " AND t." + COLUMN_TYPE + " = '" + TYPE_EXPENSE + "' " +
                "WHERE c." + COLUMN_TYPE + " = '" + TYPE_EXPENSE + "' AND c." + COLUMN_IS_ACTIVE + " = 1 " +
                "GROUP BY c." + COLUMN_ID + ", c." + COLUMN_NAME + ", c." + COLUMN_ICON + ", c." + COLUMN_COLOR;
        db.execSQL(VIEW_EXPENSES_BY_CATEGORY);
    }

    // ==================== DATOS INICIALES ====================

    private void insertInitialData(SQLiteDatabase db) {
        insertExpenseCategories(db);
        insertIncomeCategories(db);
        insertPaymentMethods(db);
    }

    private void insertExpenseCategories(SQLiteDatabase db) {
        insertCategory(db, "Alimentación", TYPE_EXPENSE, "ic_food", "#FF5722");
        insertCategory(db, "Transporte", TYPE_EXPENSE, "ic_transport", "#2196F3");
        insertCategory(db, "Educación", TYPE_EXPENSE, "ic_education", "#9C27B0");
        insertCategory(db, "Entretenimiento", TYPE_EXPENSE, "ic_entertainment", "#E91E63");
        insertCategory(db, "Salud", TYPE_EXPENSE, "ic_health", "#4CAF50");
        insertCategory(db, "Otros Gastos", TYPE_EXPENSE, "ic_other", "#607D8B");
    }

    private void insertIncomeCategories(SQLiteDatabase db) {
        insertCategory(db, "Salario", TYPE_INCOME, "ic_salary", "#4CAF50");
        insertCategory(db, "Freelance", TYPE_INCOME, "ic_freelance", "#00BCD4");
        insertCategory(db, "Beca", TYPE_INCOME, "ic_scholarship", "#FF9800");
        insertCategory(db, "Otros Ingresos", TYPE_INCOME, "ic_other", "#795548");
    }

    private void insertCategory(SQLiteDatabase db, String name, String type, String icon, String color) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_ICON, icon);
        values.put(COLUMN_COLOR, color);
        values.put(COLUMN_IS_PREDEFINED, 1);
        db.insert(TABLE_CATEGORIES, null, values);
    }

    private void insertPaymentMethods(SQLiteDatabase db) {
        String[] methods = {"Efectivo", "Tarjeta Débito", "Tarjeta Crédito", "Transferencia", "Otros"};
        for (String method : methods) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, method);
            db.insert(TABLE_PAYMENT_METHODS, null, values);
        }
    }
}