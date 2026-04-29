package com.example.memoriva.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriva.R;
import com.example.memoriva.models.CalendarDay;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }

    private final Context context;
    private List<CalendarDay> days;
    private OnDayClickListener listener;

    public CalendarAdapter(Context context, List<CalendarDay> days) {
        this.context = context;
        this.days = days;
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.listener = listener;
    }

    public void updateDays(List<CalendarDay> newDays) {
        this.days = newDays;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        CalendarDay day = days.get(position);

        if (day.isPadding()) {
            holder.tvDay.setText("");
            holder.dotIndicator.setVisibility(View.GONE);
            holder.itemView.setClickable(false);
            holder.tvDay.setBackground(null);
            return;
        }

        holder.tvDay.setText(String.valueOf(day.getDayNumber()));
        holder.itemView.setClickable(true);

        // Dot indicator for days with memories
        holder.dotIndicator.setVisibility(day.isHasMemory() ? View.VISIBLE : View.GONE);

        // Highlight today
        if (day.isToday()) {
            holder.tvDay.setTextColor(context.getResources().getColor(R.color.colorPrimary, null));
            holder.tvDay.setTypeface(null, Typeface.BOLD);
        } else {
            holder.tvDay.setTextColor(context.getResources().getColor(R.color.colorTextPrimary, null));
            holder.tvDay.setTypeface(null, Typeface.NORMAL);
        }

        // Selected state: colorPrimary circle background
        if (day.isSelected()) {
            holder.tvDay.setBackgroundResource(R.drawable.fab_background);
            holder.tvDay.setTextColor(Color.WHITE);
        } else {
            holder.tvDay.setBackground(null);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDayClick(day);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days != null ? days.size() : 0;
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        View dotIndicator;

        CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            dotIndicator = itemView.findViewById(R.id.dotIndicator);
        }
    }
}
