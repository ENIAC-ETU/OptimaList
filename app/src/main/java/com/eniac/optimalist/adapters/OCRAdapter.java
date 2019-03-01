package com.eniac.optimalist.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.eniac.optimalist.R;
import com.eniac.optimalist.utils.OCRParsedItem;

import java.util.ArrayList;
import java.util.List;

public class OCRAdapter extends BaseAdapter {

    private Context context;
    public static List<OCRParsedItem> ocrParsedItemList;
    public String[] categories={"Kategori Seçiniz...",
            "Meyve, Sebze",
            "Et, Balık",
            "Süt, Kahvaltılık",
            "Gıda, Şekerleme",
            "İçecek",
            "Deterjan, Temizlik",
            "Kağıt, Kozmetik",
            "Bebek, Oyuncak",
            "Ev, Pet"};

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
            holder.category = (Spinner) convertView.findViewById(R.id.ocrCategory);
            holder.delete_item_OCR = (Button) convertView.findViewById(R.id.delete_item_OCR);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categories);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.category.setAdapter(dataAdapter);
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
        holder.category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position1, long id) {
                if(((String)holder.category.getSelectedItem()).equals("Kategori Seçiniz..."))
                    ocrParsedItemList.get(position).setCategory("Seçilmedi");
                else
                    ocrParsedItemList.get(position).setCategory((String)holder.category.getSelectedItem());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ocrParsedItemList.get(position).setCategory("Seçilmedi");
            }
        });


        holder.delete_item_OCR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                ocrParsedItemList.remove(position);

                notifyDataSetChanged();

            }
        });





        return convertView;
    }

    private class ViewHolder {
        protected EditText nameText;
        protected EditText priceText;
        protected Spinner category;
        protected Button delete_item_OCR;
    }

}
