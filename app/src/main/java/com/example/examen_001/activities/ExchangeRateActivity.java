package com.example.examen_001.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.examen_001.R;
import com.example.examen_001.utils.ExchangeRateManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity para convertir monedas y actualizar tasas
 */
public class ExchangeRateActivity extends AppCompatActivity {

    private EditText etAmount;
    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private Button btnConvert;
    private Button btnSwap;
    private Button btnRefresh;
    private TextView tvResult;
    private TextView tvRate;
    private TextView tvLastUpdate;
    private ProgressBar progressBar;

    private ExchangeRateManager rateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_rate);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Convertidor de Monedas");

        rateManager = new ExchangeRateManager(this);

        initializeViews();
        setupSpinners();
        setupListeners();
        updateLastUpdateTime();
    }

    private void initializeViews() {
        etAmount = findViewById(R.id.etAmount);
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        btnConvert = findViewById(R.id.btnConvert);
        btnSwap = findViewById(R.id.btnSwap);
        btnRefresh = findViewById(R.id.btnRefresh);
        tvResult = findViewById(R.id.tvResult);
        tvRate = findViewById(R.id.tvRate);
        tvLastUpdate = findViewById(R.id.tvLastUpdate);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSpinners() {
        String[] currencies = {
                "USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF",
                "CNY", "MXN", "BRL", "ARS", "COP", "CLP", "PEN",
                "INR", "KRW", "SGD", "HKD", "NZD", "SEK", "NOK", "DKK"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        // USD por defecto en From, EUR en To
        spinnerFrom.setSelection(0);
        spinnerTo.setSelection(1);
    }

    private void setupListeners() {
        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertCurrency();
            }
        });

        btnSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapCurrencies();
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshRates();
            }
        });
    }

    private void convertCurrency() {
        String amountStr = etAmount.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Ingresa un monto");
            return;
        }

        final double amount = Double.parseDouble(amountStr);
        final String fromCurrency = spinnerFrom.getSelectedItem().toString();
        final String toCurrency = spinnerTo.getSelectedItem().toString();

        showLoading(true);
        tvResult.setText("---");
        tvRate.setText("---");

        rateManager.convertAmount(amount, fromCurrency, toCurrency,
                new ExchangeRateManager.ConversionCallback() {
                    @Override
                    public void onSuccess(double convertedAmount, double rate) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showLoading(false);

                                tvResult.setText(String.format(Locale.getDefault(),
                                        "%.2f %s", convertedAmount, toCurrency));

                                tvRate.setText(String.format(Locale.getDefault(),
                                        "1 %s = %.4f %s", fromCurrency, rate, toCurrency));

                                updateLastUpdateTime();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showLoading(false);
                                Toast.makeText(ExchangeRateActivity.this,
                                        "Error: " + error, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
    }

    private void swapCurrencies() {
        int fromPos = spinnerFrom.getSelectedItemPosition();
        int toPos = spinnerTo.getSelectedItemPosition();

        spinnerFrom.setSelection(toPos);
        spinnerTo.setSelection(fromPos);

        // Si ya hay un resultado, reconvertir automáticamente
        if (!tvResult.getText().toString().equals("---")) {
            convertCurrency();
        }
    }

    private void refreshRates() {
        final String baseCurrency = spinnerFrom.getSelectedItem().toString();

        showLoading(true);
        Toast.makeText(this, "Actualizando tasas...", Toast.LENGTH_SHORT).show();

        rateManager.forceRefresh(baseCurrency, new ExchangeRateManager.RefreshCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoading(false);
                        Toast.makeText(ExchangeRateActivity.this,
                                message, Toast.LENGTH_SHORT).show();
                        updateLastUpdateTime();

                        // Si hay monto ingresado, reconvertir
                        if (!TextUtils.isEmpty(etAmount.getText())) {
                            convertCurrency();
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoading(false);
                        Toast.makeText(ExchangeRateActivity.this,
                                "Error al actualizar: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void updateLastUpdateTime() {
        String baseCurrency = spinnerFrom.getSelectedItem().toString();
        long lastUpdate = rateManager.getLastUpdateTime(baseCurrency);

        if (lastUpdate > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String dateStr = sdf.format(new Date(lastUpdate));
            tvLastUpdate.setText("Última actualización: " + dateStr);
        } else {
            tvLastUpdate.setText("Sin datos en cache");
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnConvert.setEnabled(!show);
        btnRefresh.setEnabled(!show);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}