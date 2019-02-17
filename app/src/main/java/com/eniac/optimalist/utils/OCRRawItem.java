package com.eniac.optimalist.utils;

public class OCRRawItem {
    private String text;
    private int coordX, coordY;
    private boolean isUsed = false;

    public OCRRawItem(String text, int coordX, int coordY) {
        this.text = text;
        this.coordX = coordX;
        this.coordY = coordY;
    }

    public String getText() {
        return text;
    }

    public int getCoordX() {
        return coordX;
    }

    public int getCoordY() {
        return coordY;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    public boolean isUsed() {
        return isUsed;
    }
}
