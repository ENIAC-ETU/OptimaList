package com.eniac.optimalist.services;

import com.eniac.optimalist.database.model.ItemList;
import com.eniac.optimalist.database.model.ShoppingList;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class RecommendationService {
    private Map<ItemList,Integer> itemScore;
    private int threshold=0;

    public void initializeShoppingListToMap(){

    }
    private int calculateScore(int itemNumber, Date lastItemDate,int frequency){
        return 0;
    }
    public List<ItemList> getRecommendedList(List<ShoppingList> temp){
        return null;
    }

}
