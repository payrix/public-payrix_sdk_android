package com.payrix.payrixsdkdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class TxnDetailsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<HistoryDetailObj> historyDetailsData;
    private TxnDetailsRecyclerAdapter.ItemClickListener mClickListener;

    private static int TYPE_STANDARD = 0;
    private static int TYPE_SALE = 1;
    private static int TYPE_REFUND = 2;
    private static int TYPE_MISC = 3;

    private Context mContext;

    // data is passed into the constructor
    public TxnDetailsRecyclerAdapter(Context context, List<HistoryDetailObj> data) {
        this.historyDetailsData = data;
        this.mContext = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_SALE) {
            View v1 = inflater.inflate(R.layout.rv_histdetl_sale_info, parent, false);
            viewHolder = new TxnDetailsRecyclerAdapter.HistSaleHolder(v1);
        } else if (viewType == TYPE_REFUND) {
            View v2 = inflater.inflate(R.layout.rv_histdetl_refund_info, parent, false);
            viewHolder = new TxnDetailsRecyclerAdapter.HistRefundHolder(v2);
        } else if (viewType == TYPE_STANDARD) {
            View v0 = inflater.inflate(R.layout.rv_histdetl_standard_info, parent, false);
            viewHolder = new TxnDetailsRecyclerAdapter.HistStandardHolder(v0);
        } else if (viewType == TYPE_MISC) {
            View v3 = inflater.inflate(R.layout.rv_histdetl_misc_info, parent, false);
            viewHolder = new TxnDetailsRecyclerAdapter.HistMiscHolder(v3);
        }

        return viewHolder;
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case 0:  // Standard: First Section of Receipt that always appears.
                TxnDetailsRecyclerAdapter.HistStandardHolder sectStandardHolder = (TxnDetailsRecyclerAdapter.HistStandardHolder) viewHolder;
                if (historyDetailsData.get(position).getTxnDescription() != null) {
                sectStandardHolder.lblTitle.setText(historyDetailsData.get(position).getTxnDescription());
                }
                sectStandardHolder.lblSubTitle.setText(historyDetailsData.get(position).getTransactionID());
            break;
            case 1:  // Sale: Second Section of Receipt
                TxnDetailsRecyclerAdapter.HistSaleHolder sectSaleHolder = (TxnDetailsRecyclerAdapter.HistSaleHolder) viewHolder;

                sectSaleHolder.descriptorNameLabel.setText(historyDetailsData.get(position).getDescriptor());
                sectSaleHolder.lblZip.setText(historyDetailsData.get(position).getZip());

                //Former Standard
                sectSaleHolder.lblTransactionType.setText(historyDetailsData.get(position).getTransactionType());
                sectSaleHolder.lblTransactionTid.setText("");
                sectSaleHolder.lblTransactionApprCode.setText(historyDetailsData.get(position).getAuthorization());
                //sectStandardHolder.lblTransactionStatus.setText(historyDetailsData.get(position).getTransactionStatus());

                String strDate = historyDetailsData.get(position).getTransactionDate();
                try {
                    Date tmpDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(strDate);
                    SimpleDateFormat txnDateFmt = new SimpleDateFormat("MM/dd/yy, hh:mm a");
                    String useDate = txnDateFmt.format(tmpDate);
                    sectSaleHolder.lblTransactionDateTime.setText(useDate);
                } catch (ParseException e) {
                    sectSaleHolder.lblTransactionDateTime.setText(strDate);
                }

                sectSaleHolder.lblTransactionID.setText(historyDetailsData.get(position).getTransactionID());


                //Still sale
                String entryMode = historyDetailsData.get(position).getEntryMode() == null ? "" : historyDetailsData.get(position).getEntryMode();
                sectSaleHolder.lblEntryMode.setText(getEntryModeText(entryMode));

                if (historyDetailsData.get(position).getCardNumber() != null) {
                    sectSaleHolder.lblCardNo.setText(historyDetailsData.get(position).getCardNumber());
                }
                sectSaleHolder.lblExpiration.setText(historyDetailsData.get(position).getExpiration());
                Double useSaleAmt = historyDetailsData.get(position).getSaleAmt();
                String theSaleAmt = String.format(Locale.getDefault(), "%1$,.2f", useSaleAmt);
                sectSaleHolder.lblAmount.setText(theSaleAmt);


                //sectSaleHolder.lblEntryMode.setText(historyDetailsData.get(position).getEntryMode());

                Double useTaxAmt = historyDetailsData.get(position).getTaxAmt();
                String theTaxAmt = String.format(Locale.getDefault(),"%1$,.2f", useTaxAmt);
                sectSaleHolder.lblTaxAmt.setText(theTaxAmt);

                Double useTotalAmt = historyDetailsData.get(position).getTotalAmt();
                String theTotalAmt = String.format(Locale.getDefault(),"%1$,.2f", useTotalAmt);
                sectSaleHolder.lblTotal.setText(theTotalAmt);

                String useDesc = historyDetailsData.get(position).getTxnDescription();
                if ((useDesc != null) && (!useDesc.equals(""))) {
                    sectSaleHolder.lblTxnDescLabel.setText("Description:");
                    sectSaleHolder.lblTxnDesc.setText(useDesc);
                } else {
                    sectSaleHolder.lblTxnDescLabel.setText("");
                    sectSaleHolder.lblTxnDesc.setText("");
                }

            break;
            case 2:  // Refund: Third Section of Receipt
                TxnDetailsRecyclerAdapter.HistRefundHolder sectRefundHolder = (TxnDetailsRecyclerAdapter.HistRefundHolder) viewHolder;
                Double useRefundAmt = historyDetailsData.get(position).getRefundAmt();
                String theRefundAmt = String.format("%1$,.2f", useRefundAmt);
                sectRefundHolder.lblRefundAmt.setText(theRefundAmt);

                String strDate2 = historyDetailsData.get(position).getRefundDate();

                try {
                    Date tmpDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(strDate2);
                    SimpleDateFormat txnDateFmt = new SimpleDateFormat("MM/dd/yy, hh:mm a");
                    String useDate = txnDateFmt.format(tmpDate);
                    sectRefundHolder.lblRefundDate.setText(useDate);
                } catch (ParseException e) {
                    sectRefundHolder.lblRefundDate.setText(strDate2);
                }

                sectRefundHolder.lblRefundID.setText(historyDetailsData.get(position).getRefundTxnID());

            break;

            case 3:  // Misc: Fourth Section of Receipt - This handles New or Unknown Receipt Retails
                TxnDetailsRecyclerAdapter.HistMiscHolder sectMiscHolder = (TxnDetailsRecyclerAdapter.HistMiscHolder) viewHolder;

                sectMiscHolder.lblMiscLine1.setText(historyDetailsData.get(position).getMiscLine1());
                sectMiscHolder.lblMiscLine2.setText(historyDetailsData.get(position).getMiscLine2());
            break;
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return historyDetailsData.size();
    }


    @Override
    public int getItemViewType(int position) {
        // Determine Receipt Section
        return historyDetailsData.get(position).getObjectType();
    }


    // stores and recycles views as they are scrolled off screen
    public class HistStandardHolder extends RecyclerView.ViewHolder {
        TextView lblTitle;
        TextView lblSubTitle;

        HistStandardHolder(View itemView) {
            super(itemView);
            lblTitle = itemView.findViewById(R.id.txn_title);
            lblSubTitle = itemView.findViewById(R.id.txn_subtitle);
        }
    }


    // stores and recycles views as they are scrolled off screen
    public class HistSaleHolder extends RecyclerView.ViewHolder {
        TextView descriptorNameLabel;
        TextView lblZip;
        TextView lblTransactionID;
        TextView lblTransactionType;
        TextView lblTransactionApprCode;
        TextView lblTransactionDateTime;
        TextView lblTransactionTid;

        TextView lblAmount;
        TextView lblEntryMode;
        TextView lblTaxAmt;
        TextView lblTotal;
        TextView lblCardNo;
        TextView lblExpiration;
        TextView lblTxnDesc;
        TextView lblTxnDescLabel;

        HistSaleHolder(View itemView) {
            super(itemView);
            descriptorNameLabel = itemView.findViewById(R.id.descriptor_label);
            lblZip = itemView.findViewById(R.id.zip_label);
            lblTransactionID = itemView.findViewById(R.id.txnID_rvHistDetlStd);
            lblTransactionType = itemView.findViewById(R.id.txnType_rvHistDetlStd);
            lblTransactionApprCode = itemView.findViewById(R.id.txnApprCode_rvHistDetlStd);
            lblTransactionDateTime = itemView.findViewById(R.id.txnDate_rvHistDetlStd);
            lblTransactionTid = itemView.findViewById(R.id.txnTID_rvHistDetlStd);

            lblEntryMode = itemView.findViewById(R.id.entryMode_rvHistDetlSale);
            lblAmount = itemView.findViewById(R.id.amount_rvHistDetlSale);
            lblTaxAmt = itemView.findViewById(R.id.txnTax_rvHistDetlSale);
            lblCardNo = itemView.findViewById(R.id.cardNo_rvHistDetlSale);
            lblExpiration = itemView.findViewById(R.id.expiration_rvHistDetlSale);
            lblTotal = itemView.findViewById(R.id.txnTotal_rvHistDetlSale);
            lblTxnDesc = itemView.findViewById(R.id.txnDesc_rvHistDetlSale);
            lblTxnDescLabel = itemView.findViewById(R.id.txnDescLbl_rvHistDetlSale);
        }
    }


    // stores and recycles views as they are scrolled off screen
    public class HistRefundHolder extends RecyclerView.ViewHolder {
        TextView lblRefundAmt;
        TextView lblRefundDate;
        TextView lblRefundID;

        HistRefundHolder(View itemView)
        {
            super(itemView);
            lblRefundAmt = itemView.findViewById(R.id.refundAmt_rvHistDetlRefund);
            lblRefundDate = itemView.findViewById(R.id.refundDate_rvHistDetlRefund);
            lblRefundID = itemView.findViewById(R.id.refundID_rvHistDetlRefund);
        }
    }


    public class HistMiscHolder extends RecyclerView.ViewHolder {
        TextView lblMiscLine1;
        TextView lblMiscLine2;

        HistMiscHolder(View itemView)
        {
            super(itemView);
            lblMiscLine1 = itemView.findViewById(R.id.misc1_rvHistDetlMisc);
            lblMiscLine2 = itemView.findViewById(R.id.misc2_rvHistDetlMisc);
        }
    }


    public void clear() {
        historyDetailsData.clear();
        notifyDataSetChanged();
    }


    public void addAll(List<HistoryDetailObj> list) {
        historyDetailsData.addAll(list);
        notifyDataSetChanged();
    }


    // convenience method for getting data at click position
    HistoryDetailObj getItem(int id) {
        return historyDetailsData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(TxnDetailsRecyclerAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    private String getEntryModeText(String payMode) {
        String entryMode = "";
        switch (payMode) {
            case "1":
                entryMode = "Manual Entry";
                break;
                case "2":
                    entryMode = "Swipe - Track1";
                    break;
                case "3":
                    entryMode = "Swipe - Track2";
                    break;
                case "4":
                    entryMode = "Swipe - FullMagStrip";
                    break;
                case "5":
                    entryMode = "Chip - Insert";
                    break;
                case "6":
                    entryMode = "Contactless";
                    break;
                case "7":
                    entryMode = "Fallback - MagStrip";
                    break;
                case "8":
                    entryMode = "Fallback - KeyedEntry";
                    break;
                case "9":
                    entryMode = "Other - ApplePay";
                    break;
                default:
                    entryMode = String.format("%s%s", "Unknown: ", payMode);
                    break;
            }
            return entryMode;
    }


}
