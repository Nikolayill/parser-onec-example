package com.github.nikolayill.dto;

import java.util.List;
import java.util.ArrayList;

public class Document {
    private List<Item> heading = new ArrayList<>();
    private AccountSection account;
    private List<DocumentSection> sections = new ArrayList<>();

    // Getters and setters

    public List<Item> getHeading() {
        return heading;
    }

    public void setHeading(List<Item> heading) {
        this.heading = heading;
    }

    public AccountSection getAccount() {
        return account;
    }

    public void setAccount(AccountSection account) {
        this.account = account;
    }

    public List<DocumentSection> getSections() {
        return sections;
    }

    public void setSections(List<DocumentSection> sections) {
        this.sections = sections;
    }
}
