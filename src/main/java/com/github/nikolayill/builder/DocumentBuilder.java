package com.github.nikolayill.builder;

import com.github.nikolayill.dto.*;

import java.util.ArrayList;
import java.util.List;

public class DocumentBuilder {
    private List<Item> heading = new ArrayList<>();
    private AccountSection account;
    private List<DocumentSection> sections = new ArrayList<>();
    private DocumentSection currentSection;

    public DocumentBuilder withHeading(String key, String value) {
        Item item = new Item();
        item.setKey(key);
        item.setValue(value);
        this.heading.add(item);
        return this;
    }

    public DocumentBuilder withAccount(String key, String value) {
        if (this.account == null) {
            this.account = new AccountSection();
        }
        Item item = new Item();
        item.setKey(key);
        item.setValue(value);
        this.account.getItems().add(item);
        return this;
    }

    public DocumentSectionBuilder withDocumentSection(String sectionName) {
        endCurrentSection(); // завершаем текущий раздел, если есть
        currentSection = new DocumentSection();
        currentSection.setSectionName(sectionName);
        sections.add(currentSection);
        return new DocumentSectionBuilderImpl();
    }

    public Document build() {
        Document document = new Document();
        document.setHeading(heading);
        document.setAccount(account);
        document.setSections(sections);
        return document;
    }

    private void endCurrentSection() {
        currentSection = null;
    }

    // Вложенный интерфейс для DocumentSectionBuilder
    public interface DocumentSectionBuilder {
        DocumentSectionBuilder withDataItem(String key, String value);
        DocumentBuilder endSection();
    }

    // Реализация интерфейса DocumentSectionBuilder
    private class DocumentSectionBuilderImpl implements DocumentSectionBuilder {
        @Override
        public DocumentSectionBuilder withDataItem(String key, String value) {
            if (currentSection != null) {
                Item item = new Item();
                item.setKey(key);
                item.setValue(value);
                currentSection.getItems().add(item);
            }
            return this;
        }

        @Override
        public DocumentBuilder endSection() {
            endCurrentSection();
            return DocumentBuilder.this;
        }
    }
}

