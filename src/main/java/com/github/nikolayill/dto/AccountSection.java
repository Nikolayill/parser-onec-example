package com.github.nikolayill.dto;

import java.util.ArrayList;
import java.util.List;

public class AccountSection {
    private List<Item> items = new ArrayList<>();

    // Getters and setters

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
