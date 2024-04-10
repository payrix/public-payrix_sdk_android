package com.payrix.payrixsdkdemo;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.payrix.payrixsdk.paycore.paycoremobile.PayCoreTxn;
import com.payrix.payrixsdk.paycore.payrixcore.Txns;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TxnListingAdapter extends RecyclerView.Adapter<TxnListingAdapter.TransactionListingViewHolder> {
    private final List<Txns> txnsList;
    private final SharedUtilities sharedUtils = SharedUtilities.getInstance();
    private final TxnListingOnClick mListener;

    public TxnListingAdapter(List<Txns> txnData, TxnListingOnClick txnListingOnClick) {
        txnsList = txnData;
        mListener = txnListingOnClick;
    }

    @NonNull
    @Override
    public TransactionListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TransactionListingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_listing_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionListingViewHolder holder, int position) {
        Txns txn = txnsList.get(position);
        if (txn != null) {
            if (TextUtils.isEmpty(txn.getCreated())) {
                if (TextUtils.isEmpty(txn.getFirst())) {
                    // No txn
                } else {
                    // Header
                }
            } else {
                String aCardHolder = sharedUtils.bldFullName(txn);

                holder.cardHolderView.setText(aCardHolder);

                Integer txnStatus = txn.getStatus();
                if (txnStatus == PayCoreTxn.PayCoreTxnStatus.failed.getValue()) {
                    holder.successFailImgView.setImageResource(R.drawable.paymentfailed);
                } else {
                    holder.successFailImgView.setImageResource(R.drawable.paymentsuccess);
                }


                String txnDateTime = txn.getCreated();
                String strTime = txnDateTime.substring(11,16);

                String histItemTime = "";
                Date theTime;

                try {
                    theTime = new SimpleDateFormat("hh:mm", Locale.getDefault()).parse(strTime);
                    SimpleDateFormat histTimeFmt = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    if (theTime != null) {
                        histItemTime = histTimeFmt.format(theTime);
                        holder.timeView.setText(histItemTime);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                int useTotalAmt = txn.getTotal();
                Double dblTotalAmt = (double) useTotalAmt;
                dblTotalAmt = dblTotalAmt / 100.00;

                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                String theTotalAmt = currencyFormat.format(dblTotalAmt);

                if (txn.getType() == 5) {
                    theTotalAmt = String.format("-( %s )", theTotalAmt);
                }
                holder.totalView.setText(theTotalAmt);

                holder.cardLayout.setOnClickListener(view -> {
                    mListener.onClickItem(txn);
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return txnsList.size();
    }

    public static class TransactionListingViewHolder extends RecyclerView.ViewHolder {
        private final TextView timeView;
        private final TextView totalView;
        private final ImageView successFailImgView;
        private final TextView cardHolderView;
        private final ConstraintLayout cardLayout;

        public TransactionListingViewHolder(@NonNull View itemView) {
            super(itemView);
            timeView = itemView.findViewById(R.id.time_lbl);
            totalView = itemView.findViewById(R.id.total_lbl);
            cardHolderView = itemView.findViewById(R.id.card_holder_lbl);
            successFailImgView = itemView.findViewById(R.id.success_fail_img);
            cardLayout = itemView.findViewById(R.id.card);
        }
    }

    interface TxnListingOnClick {
        void onClickItem(Txns txns);
    }
}
