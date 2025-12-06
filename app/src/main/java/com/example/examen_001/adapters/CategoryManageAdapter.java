package com.example.examen_001.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.examen_001.R;
import com.example.examen_001.models.Category;
import java.util.List;

public class CategoryManageAdapter extends RecyclerView.Adapter<CategoryManageAdapter.ViewHolder> {

    private List<Category> categories;
    private Context context;
    private OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onEditCategory(Category category);
        void onToggleCategory(Category category);
        void onDeleteCategory(Category category);
    }

    public CategoryManageAdapter(List<Category> categories, Context context,
                                 OnCategoryActionListener listener) {
        this.categories = categories;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);

        holder.tvName.setText(category.getName());
        holder.tvType.setText(category.getType().equals("EXPENSE") ? "Gasto" : "Ingreso");

        try {
            holder.cardView.setCardBackgroundColor(Color.parseColor(category.getColor() + "30"));
        } catch (Exception e) {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }

        holder.btnToggle.setText(category.isActive() ? "Desactivar" : "Activar");
        holder.btnDelete.setEnabled(!category.isPredefined());
        holder.btnEdit.setEnabled(!category.isPredefined());

        holder.btnEdit.setOnClickListener(v -> listener.onEditCategory(category));
        holder.btnToggle.setOnClickListener(v -> listener.onToggleCategory(category));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteCategory(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvName, tvType;
        Button btnEdit, btnToggle, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvType = itemView.findViewById(R.id.tvCategoryType);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnToggle = itemView.findViewById(R.id.btnToggle);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
