package com.example.javastripeapp.ui.workorder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javastripeapp.data.Address;
import com.example.javastripeapp.data.WorkOrder;
import com.example.javastripeapp.databinding.ItemWorkOrderBinding;
import com.example.javastripeapp.utils.DateUtils;

import java.util.List;

public class WorkOrderAdapter extends RecyclerView.Adapter<WorkOrderAdapter.ViewHolder> {
    private static final String TAG = "WorkOrderAdapter";
    private final List<WorkOrder> workOrderList;

    /**
     * @param workOrderList containing the data to populate views to be used by
     *                      RecyclerView
     *                      Initialize the dataset of the Adapter
     */
    public WorkOrderAdapter(List<WorkOrder> workOrderList) {
        this.workOrderList = workOrderList;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemWorkOrderBinding binding;
        private final Context context;

        public ViewHolder(ItemWorkOrderBinding binding, Context context) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = context;
        }

        public void bind(WorkOrder workOrder) {
            Long timestamp = (Long) workOrder.getCreatedAt();
            String readableDate = DateUtils.formatCurrentDateTime(timestamp);

            Address address = workOrder.getJobAddress();
            String addressString = address.getFormattedAddress();

            double total = workOrder.getTotalAmount();
            String totalString = String.valueOf(total);

            String status = workOrder.getWorkOrderStatus();

            binding.tvCreatedAt.setText(readableDate);
            binding.tvLocation.setText(addressString);
            binding.tvTotal.setText("$" + totalString);
            binding.tvStatus.setText(status);

            int currentPosition = getBindingAdapterPosition();

            if (currentPosition != RecyclerView.NO_POSITION) {
                binding.btnView.setOnClickListener(v -> {
                    String workOrderId = workOrder.getWorkOrderId();
                    Intent intent = new Intent(context, ViewWorkOrderActivity.class);
                    intent.putExtra("WO_ID", workOrderId);
                    context.startActivity(intent);
                });
            }
        }
    }

    @NonNull
    @Override
    public WorkOrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWorkOrderBinding binding = ItemWorkOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkOrder workOrder = workOrderList.get(position);
        holder.bind(workOrder);
    }

    @Override
    public int getItemCount() {
        return workOrderList.size();
    }

}

