package com.eniac.optimalist;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.ShoppingList;
import com.eniac.optimalist.utils.DividerItemDecoration;
import com.eniac.optimalist.utils.RecyclerTouchListener;
import com.eniac.optimalist.view.ShoppingListAdapter;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String CHANNEL_1_ID = "Channel 1";
    NotificationManagerCompat notificationManager;
    private DBHelper db;
    private ShoppingListAdapter shoppingListAdapter;
    private List<ShoppingList> shoppingLists = new ArrayList<>();
    private DrawerLayout drawer;
    private RecyclerView recyclerView;
    private TextView noShoppingListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = DBHelper.getInstance(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        recyclerView = findViewById(R.id.recycler_view);
        noShoppingListView = findViewById(R.id.empty_shopping_lists_view);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showShoppingListDialog(false, null, -1);
            }
        });


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        addNotification();
         notificationManager = NotificationManagerCompat.from(this);

        sendOnChannel(null,"Öneri:Hafta 4","Yumurta,Balık");

        shoppingLists.addAll(db.getAllShoppingLists());
        shoppingListAdapter = new ShoppingListAdapter(this, shoppingLists);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(shoppingListAdapter);

        toggleEmptyShoppingLists();

        /*
         * On long press on RecyclerView item, open alert dialog
         * with options to choose
         * Edit and Delete
         * */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void addNotification() {
        // Builds your notification

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel 1");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }

    public void sendOnChannel(View v, String tempTitle,String tempMessage) {
        String title = tempTitle;
        String message = tempMessage;

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManager.notify(1, notification);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        CharSequence colors[] = new CharSequence[]{"Düzenle", "Sil"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bir seçenek seçiniz");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showShoppingListDialog(true, shoppingLists.get(position), position);
                } else {
                    deleteShoppingList(position);
                }
            }
        });
        builder.show();
    }

    /**
     * Shows alert dialog with EditText options to enter / edit
     * a shopping list.
     * when shouldUpdate=true, it automatically displays old shopping list and changes the
     * button text to UPDATE
     */
    private void showShoppingListDialog(final boolean shouldUpdate, final ShoppingList shoppingList, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.shopping_list_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputShoppingList = view.findViewById(R.id.shopping_list);
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
                    Toast.makeText(MainActivity.this, "Alışveriş listesi adını giriniz!", Toast.LENGTH_SHORT).show();
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
}
