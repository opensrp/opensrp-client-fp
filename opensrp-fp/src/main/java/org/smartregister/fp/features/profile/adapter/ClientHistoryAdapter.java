package org.smartregister.fp.features.profile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.fp.R;
import org.smartregister.fp.common.util.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("ConstantConditions")
public class ClientHistoryAdapter extends RecyclerView.Adapter<ClientHistoryAdapter.ViewHolder> {

    private final List<HashMap<String, String>> data;
    private final HistoryItemClickListener itemClickListener;

    public ClientHistoryAdapter(List<HashMap<String, String>> data, HistoryItemClickListener itemClickListener) {
        this.data = data;
        this.itemClickListener = itemClickListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.client_visit_history_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, String> record = data.get(position);

        holder.visitData.setText(getFormattedData(record.get("visit_date")));
        holder.methodExit.setText(Utils.getMethodName(record.get("method_exit")));

        holder.container.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClicked(record, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private String getFormattedData(String currentDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Date date = formatter.parse(currentDate);
            currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return currentDate;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private View container;
        private TextView visitData;
        private TextView methodExit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.container);
            visitData = itemView.findViewById(R.id.visit_date);
            methodExit = itemView.findViewById(R.id.method_exit);
        }
    }

    public interface HistoryItemClickListener {
        void onItemClicked(HashMap<String, String> item, int position);
    }
}
