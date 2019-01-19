package com.eniac.optimalist.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.eniac.optimalist.R;
import com.eniac.optimalist.adapters.ShoppingListAdapter;
import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.ShoppingList;
import com.eniac.optimalist.utils.DividerItemDecoration;
import com.eniac.optimalist.utils.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListFragment extends Fragment {

    private DBHelper db;
    private ShoppingListAdapter shoppingListAdapter;
    private List<ShoppingList> shoppingLists = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView noShoppingListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shopping_lists, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Alışveriş Listelerim");

        db = DBHelper.getInstance(getContext());
        recyclerView = view.findViewById(R.id.recycler_view);
        noShoppingListView = view.findViewById(R.id.empty_shopping_lists_view);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showShoppingListDialog(false, null, -1);
            }
        });

        FloatingActionButton fab2 = (FloatingActionButton) view.findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddItemDialog();
            }
        });

        shoppingLists.addAll(db.getAllShoppingLists());

        shoppingListAdapter = new ShoppingListAdapter(getActivity(), shoppingLists);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(shoppingListAdapter);

        toggleEmptyShoppingLists();

        /*
         * On long press on RecyclerView item, open alert dialog
         * with options to choose
         * Edit and Delete
         * */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(),
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));
    }

    /**
     * Inserting new shopping list in db
     * and refreshing the list
     */
    private void createShoppingList(String title) {
        // inserting shopping list in db and getting
        // newly inserted shopping list id
        long id = db.insertShoppingList(title);

        // get the newly inserted shopping list from db
        ShoppingList l = db.getShoppingList(id);

        if (l != null) {
            // adding new shopping list to array list at 0 position
            shoppingLists.add(0, l);

            // refreshing the list
            shoppingListAdapter.notifyDataSetChanged();

            toggleEmptyShoppingLists();
        }
    }

    /**
     * Updating shopping list in db and updating
     * item in the list by its position
     */
    private void updateShoppingList(String title, int position) {
        ShoppingList l = shoppingLists.get(position);
        // updating shopping list title
        l.setTitle(title);

        // updating note in db
        db.updateShoppingList(l);

        // refreshing the list
        shoppingLists.set(position, l);
        shoppingListAdapter.notifyItemChanged(position);

        toggleEmptyShoppingLists();
    }

    /**
     * Deleting shopping list from SQLite and removing the
     * item from the list by its position
     */
    private void deleteShoppingList(int position) {
        // deleting the shopping list from db
        db.deleteShoppingList(shoppingLists.get(position));

        // removing the shopping list from the list
        shoppingLists.remove(position);
        shoppingListAdapter.notifyItemRemoved(position);

        toggleEmptyShoppingLists();
    }

    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 0
     */
    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Düzenle", "Hatırlatıcı ekle", "Sil"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Bir seçenek seçiniz");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showShoppingListDialog(true, shoppingLists.get(position), position);
                }
                else if (which == 1){
// Bir sonraki hafta yapılacaktır.
                }
                else {
                    deleteShoppingList(position);
                }
            }
        });
        builder.show();
    }

    private void showShoppingListDialog(final boolean shouldUpdate, final ShoppingList shoppingList, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
        View view = layoutInflaterAndroid.inflate(R.layout.add_shopping_list_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(getContext());
        alertDialogBuilderUserInput.setView(view);

        final EditText inputShoppingList = view.findViewById(R.id.shopping_list);
        //final EditText inputMarketId = view.findViewById(R.id.market_id);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_shopping_list_title) : getString(R.string.lbl_edit_shopping_list_title));

        if (shouldUpdate && shoppingList != null) {
            inputShoppingList.setText(shoppingList.getTitle());
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "güncelle" : "kaydet", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("iptal",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show toast message when no text is entered
                if (TextUtils.isEmpty(inputShoppingList.getText().toString())) {
                    Toast.makeText(getContext(), "Alışveriş listesi adını giriniz!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                // check if user updating note
                if (shouldUpdate && shoppingList != null) {
                    // update note by it's id
                    updateShoppingList(inputShoppingList.getText().toString(), position);
                } else {
                    // create new note
                    createShoppingList(inputShoppingList.getText().toString());
                }
            }
        });
    }

    /**
     * Toggling list and empty shopping lists view
     */
    private void toggleEmptyShoppingLists() {
        if (db.getShoppingListsCount() > 0) {
            noShoppingListView.setVisibility(View.GONE);
        } else {
            noShoppingListView.setVisibility(View.VISIBLE);
        }
    }

    private void addItemToShoppingList(String item){}

    /**
     * Shows alert dialog with EditText options to enter / edit
     * a shopping list.
     * when shouldUpdate=true, it automatically displays old shopping list and changes the
     * button text to UPDATE
     */
    private void showAddItemDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
        View view = layoutInflaterAndroid.inflate(R.layout.add_item_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(getContext());
        alertDialogBuilderUserInput.setView(view);

        final EditText inputItemName = view.findViewById(R.id.add_item);
        TextView dialogTitle = view.findViewById(R.id.add_item_dialog_title);
        dialogTitle.setText(getString(R.string.new_item));

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("kaydet", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("iptal",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();

                addItemToShoppingList(inputItemName.getText().toString());
            }
        });
    }
}