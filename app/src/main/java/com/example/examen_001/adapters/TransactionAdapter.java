package com.example.examen_001.adapters;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examen_001.R;
import com.example.examen_001.database.DatabaseHelper;
import com.example.examen_001.models.Transaction;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {


    private List<Transaction> transactions;
    private Context context;
    private OnTransactionClickListener listener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    public TransactionAdapter(List<Transaction> transactions, Context context,
                              OnTransactionClickListener listener) {
        this.transactions = transactions;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        // Categoría
        holder.tvCategory.setText(transaction.getCategoryName());

        // Descripción
        if (transaction.getDescription() != null && !transaction.getDescription().isEmpty()) {
            holder.tvDescription.setText(transaction.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Monto
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        String amountText = currencyFormat.format(transaction.getAmount());
        holder.tvAmount.setText(amountText);

        // Color según tipo
        if (transaction.getType().equals(DatabaseHelper.TYPE_INCOME)) {
            holder.tvAmount.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            holder.tvType.setText("💰");
        } else {
            holder.tvAmount.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            holder.tvType.setText("💸");
        }

        // Fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        Date date = new Date(transaction.getTransactionDate() * 1000);
        holder.tvDate.setText(sdf.format(date));

        // Método de pago
        if (transaction.getPaymentMethod() != null && !transaction.getPaymentMethod().isEmpty()) {
            holder.tvPaymentMethod.setText(transaction.getPaymentMethod());
            holder.tvPaymentMethod.setVisibility(View.VISIBLE);
        } else {
            holder.tvPaymentMethod.setVisibility(View.GONE);
        }

        // Color de categoría
        try {
            if (transaction.getCategoryColor() != null) {
                holder.cardView.setCardBackgroundColor(Color.parseColor(transaction.getCategoryColor() + "20")); // 20 = transparencia
            }
        } catch (Exception e) {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }

        // Click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTransactionClick(transaction);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvType;
        TextView tvCategory;
        TextView tvDescription;
        TextView tvAmount;
        TextView tvDate;
        TextView tvPaymentMethod;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvType = itemView.findViewById(R.id.tvType);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
        }
    }
}
