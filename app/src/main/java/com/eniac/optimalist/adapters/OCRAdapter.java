package com.eniac.optimalist.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;

import com.eniac.optimalist.R;
import com.eniac.optimalist.utils.OCRParsedItem;

import java.util.List;

public class OCRAdapter extends BaseAdapter {

    private Context context;
    public static List<OCRParsedItem> ocrParsedItemList;

    public OCRAdapter(Context context, List<OCRParsedItem> ocrParsedItemList) {
        this.context = context;
        this.ocrParsedItemList = ocrParsedItemList;
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }
    @Override
    public int getItemViewType(int position) {

        return position;
    }

    @Override
    public int getCount() {
        return ocrParsedItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return ocrParsedItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.ocr_result_row, null, true);

            holder.nameText = (EditText) convertView.findViewById(R.id.ocrProductName);
            holder.priceText = (EditText) convertView.findViewById(R.id.ocrProductPrice);
            convertView.setTag(holder);
        }else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder)convertView.getTag();
        }

        holder.nameText.setText(ocrParsedItemList.get(position).getName());
        holder.priceText.setText(ocrParsedItemList.get(position).getPrice());

        holder.nameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ocrParsedItemList.get(position).setName(holder.nameText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        holder.priceText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ocrParsedItemList.get(position).setPrice(holder.priceText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return convertView;
    }

    private class ViewHolder {
        protected EditText nameText;
        protected EditText priceText;
    }

}
