package com.github.nikolayill.builder;


import com.github.nikolayill.dto.*;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.Test;


public class DocumentBuilderTest {

    @Test
    public void buildPlain() {
        // Пример создания документа
        Document document = new Document();

        // Пример добавления элементов заголовка
        Item item1 = new Item();
        item1.setKey("Ключ1");
        item1.setValue("Значение1");
        document.getHeading().add(item1);

        Item item2 = new Item();
        item2.setKey("Ключ2");
        item2.setValue("Значение2");
        document.getHeading().add(item2);

        // Пример создания и добавления секции счета
        AccountSection accountSection = new AccountSection();

        Item item3 = new Item();
        item3.setKey("Ключ3");
        item3.setValue("Значение3");
        accountSection.getItems().add(item3);

        Item item4 = new Item();
        item4.setKey("Ключ4");
        item4.setValue("Значение4");
        accountSection.getItems().add(item4);

        document.setAccount(accountSection);

        // Пример создания и добавления раздела документа
        DocumentSection documentSection = new DocumentSection();
        documentSection.setSectionName("ИмяСекции");

        Item item5 = new Item();
        item5.setKey("Ключ5");
        item5.setValue("Значение5");
        documentSection.getItems().add(item5);

        Item item6 = new Item();
        item6.setKey("Ключ6");
        item6.setValue("Значение6");
        documentSection.getItems().add(item6);

        document.getSections().add(documentSection);        // Пример вывода данных
        printDocument(document);

        Assertions.assertNotNull(document);
    }

    @Test
    public void buildBuilder(){
        // Использование улучшенного Builder для создания экземпляра Document
        Document document = new DocumentBuilder()
                .withHeading("Ключ1", "Значение1")
                .withHeading("Ключ2", "Значение2")
                .withAccount("Ключ3", "Значение3")
                .withAccount("Ключ4", "Значение4")
                .withDocumentSection("ИмяСекции")
                    .withDataItem("Ключ5", "Значение5")
                    .withDataItem("Ключ6", "Значение6")
                .endSection()
                .withDocumentSection("ИмяСекции2")
                    .withDataItem("Ключ7", "Значение7")
                    .withDataItem("Ключ8", "Значение8")
                .endSection()
                .build();

        // Вывод данных
        printDocument(document);

        Assertions.assertNotNull(document);
    }

    public static void printDocument(Document document) {
        System.out.println("Heading:");
        for (Item item : document.getHeading()) {
            System.out.println(item.getKey() + "=" + item.getValue());
        }

        System.out.println("\nAccount Section:");
        for (Item item : document.getAccount().getItems()) {
            System.out.println(item.getKey() + "=" + item.getValue());
        }

        System.out.println("\nDocument Sections:");
        for (DocumentSection section : document.getSections()) {
            System.out.println("Section Name: " + section.getSectionName());
            for (Item item : section.getItems()) {
                System.out.println(item.getKey() + "=" + item.getValue());
            }
        }
    }
}