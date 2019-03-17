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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.HashMap;

public class RecommendationService {
    private static DBHelper db;
    private long reminderId;
    private static HashMap<Integer,List<ItemList>> calendar;
    private static HashMap<Long,Integer> itemDate;

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
        ra=new RecommendationActivity();
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
    private List<ItemList> items;

    private int calculateScore(long itemNumber, String lastItemDate,int countOfItem) {
        DateFormat formatter ;
        Date date,date2;
        Date c = Calendar.getInstance().getTime();
        formatter = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = formatter.format(c);
        int sumOfScores=0;
        try {
            date = (Date)formatter.parse(lastItemDate);
            date2 = (Date)formatter.parse(formattedDate);

            int score1=getDaysDifference(date,date2);
            int score2=countOfItem;
            int score3=closestMarketScore(db.getItemList(itemNumber));
            sumOfScores=score1*2+score2*3+score3;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return sumOfScores;
    }
    private int closestMarketScore(ItemList e){
        long a=e.getShoppingListId();
        ShoppingList temp=db.getShoppingList(a);
        //long id=temp.getMarketID();
        //Market p=db.getMarket(id);
        //long distance=MyLocation-p;
        return 0;
    }
    private int getNumberOfItemsInShoppingList(ItemList e){
        int count=0;
        for (ItemList b:items){
            if (b.getTitle().equals(e.getTitle())){
                count++;
            }
        }
        return count;
    }
    public static int getDaysDifference(Date fromDate,Date toDate)
    {
        if(fromDate==null||toDate==null)
            return 0;

        return (int)( (toDate.getTime() - fromDate.getTime()) / (1000 * 60 * 60 * 24));
    }
    public List<ItemList> getRecommendedList(){
        Calendar c=Calendar.getInstance();
        int a=c.get(Calendar.DAY_OF_YEAR);
        if (calendar.get(a)!=null)
        Log.d("MyLocationab",""+calendar.get(a).toString());
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
        List<ItemList> p=db.getCurrentItems(reminderId);
        for(ItemList e:p){
            db.deleteItem(e);
        }
        if (getRecommendedList()!=null){
            for (ItemList e:getRecommendedList()){
                db.insertItemList(e.getTitle(),1,10000,"Kategori Yok(recomm)",reminderId);
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

    private void checkPreviousDates(ItemList item,Integer e) {
        Log.d("MyLocation",""+item.getId());
        Log.d("MyLocation",""+item.getTitle());
        Log.d("MyLocation",""+e);
        Log.d("MyLocation",""+itemDate.get(item.getId()));
        //Log.d("MyLocation",""+itemDate.get(4));

        if (itemDate.get(item.getId())!=null){
            Log.d("MyLocation","hehe");
            int latestDay=itemDate.get(item.getId());
            calendar.get(latestDay).remove(item);
            Log.d("MyLocation",""+calendar.get(latestDay).toString());
            Log.d("MyLocation",""+calendar.get(latestDay));
            Log.d("MyLocation","itemdate="+latestDay);

        }
        itemDate.put(item.getId(),e);
        db.saveHashMap("key",calendar);
        db.saveHashMap("key1",itemDate);
    }
}
