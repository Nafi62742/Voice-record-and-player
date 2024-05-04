package com.ataraxia.pawahara.adapter;

import static com.ataraxia.pawahara.utils.Popuputils.showPlanDetailsPopup;

import android.app.Activity;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.ataraxia.pawahara.MainActivity;
import com.ataraxia.pawahara.R;
import com.ataraxia.pawahara.model.Plan_pojos;
import com.ataraxia.pawahara.subscription.Subs;

import java.util.List;

public class PlanRecyclerViewAdapter extends RecyclerView.Adapter<PlanRecyclerViewAdapter.PlanViewHolder> {
    private List<Plan_pojos> planslist;
    private Context context;
    private  Activity activity;
    private View.OnClickListener onPlanDetailsClick;
    public PlanRecyclerViewAdapter(Context context, Activity activity, List<Plan_pojos> planslist, View.OnClickListener onPlanDetailsClick) {
        this.context = context;
        this.activity = activity;
        this.planslist = planslist;
        this.onPlanDetailsClick = onPlanDetailsClick;
    }
    @Override
    public PlanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate your list item layout and create a view holder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plan_details_item, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlanViewHolder holder, int position) {
        Plan_pojos plans = planslist.get(position);
        String planExtension=context.getString(R.string.min);
        String time= String.valueOf(plans.getminuates());
        String suffixTime= context.getString(R.string.way_back_time_approx);
        String titleSuffix = context.getString(R.string.monthly);
        String planAmount = "";
        String title = "";
        String planName=plans.getName() ;
        String planTime = suffixTime+time+" "+planExtension;


        int planTimes =plans.getminuates();
        if(planTimes<60){
            planAmount = plans.getName();
            title = titleSuffix + "/" + planAmount;
        }else{
            planAmount = plans.getPrice()+context.getResources().getString(R.string.yen);
            title = titleSuffix + "/" + planAmount;
        }

// Concatenate the parts of the string

// Create a SpannableString
        SpannableString spannableString = new SpannableString(title);
// Set the color for the getPlanAmount part
        int start = title.indexOf(planAmount);
        int end = start + planAmount.length();
        spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.Red)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

// Set the styled text in the TextView
        holder.nameTextView.setText(spannableString);
        holder.titleTextView.setText(planName);
        holder.priceTextView.setText(planTime);



        holder.plan_details_btn.setOnClickListener(new View.OnClickListener() {
            long DEBOUNCE_INTERVAL = 1000; // Set the debounce interval in milliseconds
            long lastClickTime = 0;
            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime > DEBOUNCE_INTERVAL) {
                    showPlanDetailsPopup(context, onPlanDetailsClick, plans ,activity);
                    lastClickTime = currentTime;

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return planslist.size(); // You may need to change this depending on the list
    }
    public void setPlanslist(List<Plan_pojos> planslist) {
        this.planslist = planslist;
        notifyDataSetChanged();
    }

    public class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView priceTextView;

        TextView titleTextView;
        CardView plan_details_btn;

        public PlanViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.plan_name_TV);
            titleTextView = itemView.findViewById(R.id.plan_title_TV);
            priceTextView = itemView.findViewById(R.id.plan_price_TV);
            plan_details_btn = itemView.findViewById(R.id.plan_details_BTN);
            // Initialize views here
        }
    }
}
