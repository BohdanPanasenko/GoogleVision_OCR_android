package com.example.googlevision_api;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OCRAdapter extends RecyclerView.Adapter<OCRAdapter.OCRViewHolder> {
    private final List<OCRResult> list;
    private final OCRDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public OCRAdapter(List<OCRResult> list, OCRDatabase db) {
        this.list = list;
        this.db = db;
    }

    @NonNull
    @Override
    public OCRViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ocr, parent, false);
        return new OCRViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OCRViewHolder holder, int position) {
        OCRResult item = list.get(position);
        holder.tvText.setText(item.textContent);
        holder.tvDate.setText(item.timestamp);
        holder.btnDelete.setOnClickListener(v -> {
            deleteItem(item, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class OCRViewHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvDate;
        Button btnDelete;
        OCRViewHolder(View v) {
            super(v);
            tvText = v.findViewById(R.id.tv_item_text);
            tvDate = v.findViewById(R.id.tv_item_date);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }

    private void deleteItem(final OCRResult item, final int position) {
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        executorService.execute(() -> {
            db.ocrDao().delete(item);
            handler.post(() -> {
                list.remove(position);
                notifyItemRemoved(position);
            });
        });
    }
}
