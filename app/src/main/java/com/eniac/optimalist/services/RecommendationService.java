package com.eniac.optimalist.services;

import android.content.Context;
import android.util.Log;

import com.eniac.optimalist.activities.RecommendationActivity;
import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.ItemList;
import com.eniac.optimalist.database.model.ShoppingList;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.HashMap;

public class RecommendationService {
    private static DBHelper db;
    private long reminderId;
    private static HashMap<Integer,List<ItemList>> calendar;
    private static HashMap<Long,Integer> itemDate;
    private List<ItemList> items;

    private static RecommendationService rs;
    private static RecommendationActivity ra;
    public static synchronized RecommendationService getInstance(Context p) {
        if (rs == null) {
            rs = new RecommendationService(p);
        }
        return rs;
    }
    private RecommendationService(Context p){
        db = DBHelper.getInstance(p.getApplicationContext());
        calendar=(HashMap<Integer,List<ItemList>>)db.getHashMap("key");
        itemDate=(HashMap<Long,Integer>)db.getHashMap("key1");
        if (calendar==null){
            calendar=new HashMap<Integer,List<ItemList>>(365);
            db.saveHashMap("key",calendar);
        }
        if(itemDate==null){
            itemDate=new HashMap<Long,Integer>();
            db.saveHashMap("key1",itemDate);

        }
        items=db.getAllItemLists();

    }

    public List<ItemList> getRecommendedList(){
        Calendar c=Calendar.getInstance();
        int a=c.get(Calendar.DAY_OF_YEAR);
        if (calendar.get(a)!=null)
        Log.d("MyLocationab",""+ Arrays.toString(calendar.get(a).toArray()));
        return calendar.get(a);
    }
    public List<Integer> getDateList(ItemList item) throws ParseException {
        List<Integer> p=db.queryDatesOfItem(item);

        return p;
    }
    public void setRecommendationDate(ItemList e,int day){
        Calendar c=Calendar.getInstance();
        int a=c.get(Calendar.DAY_OF_YEAR);
        List<ItemList> p=calendar.get(day);
        if (p==null){
            p=new ArrayList<ItemList>() ;
        }
        p.add(e);
        calendar.put(day,p);
        db.saveHashMap("key",calendar);
    }

    public void createReminderFromRecom(){
        List<ShoppingList> temp=db.getAllShoppingLists();
        reminderId=-1;
        for (ShoppingList e:temp){
            if(e.getTitle().equals("Recommended")){
                reminderId=e.getId();
            }
        }

        Log.d("MyLocation:","ReminderID:"+reminderId+"");
        if(reminderId!=-1)
            db.deleteShoppingList(db.getShoppingList(reminderId));
        reminderId=db.insertShoppingList("Recommended",0);
        if (getRecommendedList()!=null){
            for (ItemList e:getRecommendedList()){
                db.insertItemList(e.getTitle(),e.getAmount(),e.getPrice(),"Öneri:"+e.getCategory(),reminderId);
            }
        }
    }
    private HashMap<String,List<Integer>> p;
    public void updateRecommendationFromNewList(long id) throws Exception {
        List<ItemList> temp=db.getCurrentItems(id);
        p=new HashMap<>();
        for (ItemList e:temp){
            p.put(e.getTitle(),getDateList(e));
            //setRecommendationDate(e,59);
        }
        Log.d("MyLocation:e",""+p.toString());
        new Thread(new Runnable() {
            public void run(){
                HashMap<String, Integer> myMap= null;
                try {
                    ra=new RecommendationActivity();
                    myMap = ra.execute(p).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (myMap!=null) {
                    for (String e : myMap.keySet()) {
                        ItemList item = db.findItemByTitle(e);
                        if (item != null) {
                            checkPreviousDates(item, myMap.get(e));
                            setRecommendationDate(item, myMap.get(e));
                        }
                    }
                }
                else{
                    Log.d("MyLocation:e","mymapnull");
                }
                //Log.d("MyLocation:e",""+calendar.get(70));
            }
        }).start();

    }




    public void checkPreviousDates(ItemList item,Integer e) {


        if (itemDate.get(item.getId())!=null){
            Log.d("MyLocation","hehe");
            int latestDay=itemDate.get(item.getId());
            if (e==latestDay){
                return;
            }
            for (ItemList a:calendar.get(latestDay)){
                Log.d("MyLocationbefore",""+a.getTitle());
            }
            Log.d("MyLocation",""+calendar.get(latestDay).toString());
            Log.d("MyLocation",""+calendar.get(latestDay));
            Log.d("MyLocation","itemdate="+latestDay);
            List<ItemList> newList=new ArrayList<>();
            for (ItemList a:calendar.get(latestDay)){
                if (a.getTitle().equals(item.getTitle())==false)
                    newList.add(a);
            }
            calendar.put(latestDay,newList);
            for (ItemList a:calendar.get(latestDay)){
                Log.d("MyLocationafter",""+a.getTitle());
            }
        }
        itemDate.put(item.getId(),e);
        db.saveHashMap("key",calendar);
        db.saveHashMap("key1",itemDate);
        Log.d("MyLocation","keysize():"+db.getHashMap("key1"));

    }

    public HashMap<Long,Integer> getItemDate() {
        return itemDate;
    }
}
