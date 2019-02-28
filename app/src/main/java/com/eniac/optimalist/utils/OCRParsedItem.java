package com.eniac.optimalist.utils;

public class OCRParsedItem {
    private String name;
    private String price;
    private String category;

    public OCRParsedItem(String name, String price, String category) {
        this.name = name;
        this.price = price;
        this.category=category;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getCategory() { return category; }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setCategory(String category) {this.category = category; }

    @Override
    public String toString() {
        return "Name: " + name + " - Price: " + price;
    }
}
