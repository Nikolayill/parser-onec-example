package com.github.nikolayill.dto;

import java.util.ArrayList;
import java.util.List;

public class DocumentSection {
    private String sectionName;
    private List<Item> items = new ArrayList<>();

    // Getters and setters    
    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    // Alias methods for compatibility
    public String getName() {
        return sectionName;
    }

    public void setName(String name) {
        this.sectionName = name;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
