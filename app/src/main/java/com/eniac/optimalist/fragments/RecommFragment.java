package com.eniac.optimalist.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.eniac.optimalist.R;
import com.eniac.optimalist.adapters.RecommAdapter;
import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.ItemList;
import com.eniac.optimalist.services.RecommendationService;
import com.eniac.optimalist.utils.DividerItemDecoration;
import com.eniac.optimalist.utils.RecyclerTouchListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class RecommFragment extends android.support.v4.app.Fragment {
    private DBHelper db;
    private RecommendationService recomm;
    private RecommAdapter recommAdapter;
    private List<ItemList> ritems = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView a;
    private HashMap<Long,Integer> mItems;
    public RecommFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUserVisibleHint(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recomm, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Öneri Ürünleri Tarihleri");
        recyclerView = view.findViewById(R.id.recomm_recycler_view);
        a= view.findViewById(R.id.empty_recomm_view);
        db = DBHelper.getInstance(getContext());
        mItems=(HashMap<Long, Integer>) db.getHashMap("key1");
        Log.d("MyLocation",""+mItems.keySet().size());
        recommAdapter= new RecommAdapter(getContext(), mItems);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(recommAdapter);
        if (mItems.keySet().size()>0){
            a.setVisibility(view.GONE);
        }
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(),
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
            }

            @Override
            public void onLongClick(View view,final int position) {
                CharSequence colors[] = new CharSequence[]{"Düzenle"};

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Bir seçenek seçiniz");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showRecommDialog(position);


                    }
                });
                builder.show();
            }
        }));
    }
    private void showRecommDialog(final int position){
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
        View view = layoutInflaterAndroid.inflate(R.layout.edit_recomm_date, null);
        final List<Long> p= new ArrayList<Long>(mItems.keySet());
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(getContext());
        alertDialogBuilderUserInput.setView(view);

        final TextView itemName=view.findViewById(R.id.name_item);
        itemName.setText(db.getItemList(p.get(position)).getTitle());
        final NumberPicker inputAmount = view.findViewById(R.id.day_picker);
        inputAmount.setMaxValue(365);
        inputAmount.setMinValue(1);
        final Button editButton=view.findViewById(R.id.save_button);
        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int b=inputAmount.getValue();
                RecommendationService a=RecommendationService.getInstance(getContext());
                Calendar c=Calendar.getInstance();
                int d=c.get(Calendar.DAY_OF_YEAR);
                a.checkPreviousDates(db.getItemList(p.get(position)),d+b);
                a.setRecommendationDate(db.getItemList(p.get(position)),d+b);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(RecommFragment.this).attach(RecommFragment.this).commit();
                a.createReminderFromRecom();
                alertDialog.dismiss();

            }
        });
        alertDialog.show();
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            Log.d("MyLocation","Yes");

            // Refresh your fragment here
        }
    }
}
