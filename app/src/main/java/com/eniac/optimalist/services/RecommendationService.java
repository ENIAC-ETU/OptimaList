package com.eniac.optimalist.services;

import android.app.Service;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.ItemList;
import com.eniac.optimalist.database.model.ShoppingList;
import com.eniac.optimalist.fragments.ReminderFragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class RecommendationService {
    private static DBHelper db;
    private long reminderId;
    private static HashMap<Integer,List<ItemList>> calendar;
    private static HashMap<ItemList,Integer> itemDate; //eski item tarihini guncellemek icin

    private static RecommendationService rs;

    public static synchronized RecommendationService getInstance(Context p) {
        if (rs == null) {
            rs = new RecommendationService(p);
        }
        return rs;
    }
    private RecommendationService(Context p){
        db = DBHelper.getInstance(p.getApplicationContext());
        calendar=db.getHashMap("key");
        if (calendar==null){
            calendar=new HashMap<Integer,List<ItemList>>(365);
            db.saveHashMap("key",calendar);
        }
        items=db.getAllItemLists();
        itemScore=new HashMap<ItemList, Integer>();
    }
    private List<ItemList> items;
    private Map<ItemList,Integer> itemScore;
    private int threshold=0;

    public void initializeShoppingListToMap(){
        for (ItemList e:items){
            int numberOfItem=getNumberOfItemsInShoppingList(e);
            itemScore.put(e,calculateScore(e.getId(),e.getCreatedAt(),numberOfItem));
        }
    }
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

        /*List<ItemList> newList=new ArrayList<>();
        List<String> nameList=new ArrayList<>();
        Iterator it = itemScore.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if ((Integer)pair.getValue()>threshold){
                ItemList p=(ItemList)pair.getKey();
                if (!nameList.contains(p.getTitle())){
                    newList.add( p);
                    nameList.add(p.getTitle());
                }
            }
        }*/
        return calendar.get(a);
    }
    public List<Integer> getDateList(ItemList item) throws ParseException {
        List<Integer> p=db.queryDatesOfItem(item);

        return p;
    }
    public void setRecommendationDate(ItemList e,int day){
        Calendar c=Calendar.getInstance();
        int a=c.get(Calendar.DAY_OF_YEAR);
        List<ItemList> p=calendar.get(a+day);
        if (p==null){
            p=new ArrayList<ItemList>() ;
        }
        p.add(e);
        calendar.put(a+day,p);
        db.saveHashMap("key",calendar);
    }
    public void createReminderFromRecom(){
        initializeShoppingListToMap();
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
        reminderId=db.insertShoppingList("Recommended");
        if (getRecommendedList()!=null)
        for (ItemList e:getRecommendedList()){
            db.insertItemList(e.getTitle(),1,1.8f,"Kategori Yok(recomm)",reminderId);
        }
    }

    public void updateRecommendationFromNewList(long id) throws ParseException {
        List<ItemList> temp=db.getCurrentItems(id);
        HashMap<String,List<Integer>> p=new HashMap<>();
        for (ItemList e:temp){
            p.put(e.getTitle(),getDateList(e));
            //setRecommendationDate(e,59);
        }
        Log.d("MyLocation:e",""+p.toString());
        //Log.d("MyLocation:e",""+calendar.get(117).get(0).getTitle());
    }
}
