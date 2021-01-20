package com.example.stocks;

import java.util.ArrayList;

public class Section {
    private String sectionTitle;
    private ArrayList<watchShare> allItemsInSection;
    public Section(String sectionTitle, ArrayList<watchShare> allItemsInSection) {
        this.sectionTitle = sectionTitle;
        this.allItemsInSection = allItemsInSection;
    }
    public String getSectionTitle() {
        return sectionTitle;
    }
    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }
    public ArrayList<watchShare> getAllItemsInSection() {
        return allItemsInSection;
    }
    public void setAllItemsInSection(ArrayList<watchShare> allItemsInSection) {
        this.allItemsInSection = allItemsInSection;
    }
}
