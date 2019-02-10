package com.eniac.optimalist.services;

import android.content.ClipData;
import android.content.Context;

import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.ItemList;
import com.eniac.optimalist.database.model.ShoppingList;

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
    private DBHelper db;

    public RecommendationService(Context p){
        db = DBHelper.getInstance(p.getApplicationContext());
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
        List<ItemList> newList=new ArrayList<>();
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
        }
        return newList;
    }
    public void createReminderFromRecom(){
        //createReminder(getRecommendedList());
    }
}
