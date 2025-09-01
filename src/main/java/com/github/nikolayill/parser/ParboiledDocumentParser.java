package com.github.nikolayill.parser;

import com.github.nikolayill.dto.*;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.support.Var;

import java.util.ArrayList;
import java.util.List;

@BuildParseTree
public class ParboiledDocumentParser extends BaseParser<Object> {

    // Helper methods for Parboiled ACTION blocks
    public boolean setDocumentVar(Var<Document> document) {
        document.set(new Document());
        return true;
    }

    public boolean setAccountSectionVar(Var<AccountSection> accountSection) {
        accountSection.set(new AccountSection());
        return true;
    }

    public boolean setAccountSectionFromStack(Var<AccountSection> accountSection, AccountSection value) {
        accountSection.set(value);
        return true;
    }

    public boolean setDocumentSectionVar(Var<DocumentSection> docSection) {
        docSection.set(new DocumentSection());
        return true;
    }

    public boolean setStringVar(Var<String> var, String value) {
        var.set(value);
        return true;
    }

    public boolean addItemToList(List<Item> list, Item item) {
        list.add(item);
        return true;
    }

    public boolean addSectionToList(List<DocumentSection> list, DocumentSection section) {
        list.add(section);
        return true;
    }

    public boolean setHeading(Document doc, List<Item> heading) {
        doc.setHeading(heading);
        return true;
    }

    public boolean setAccount(Document doc, AccountSection account) {
        doc.setAccount(account);
        return true;
    }

    public boolean setSections(Document doc, List<DocumentSection> sections) {
        doc.setSections(sections);
        return true;
    }

    public boolean setAccountItems(AccountSection accountSection, List<Item> items) {
        accountSection.setItems(items);
        return true;
    }

    public boolean setSectionName(DocumentSection section, String name) {
        section.setName(name);
        return true;
    }

    public boolean setSectionItems(DocumentSection section, List<Item> items) {
        section.setItems(items);
        return true;
    }

    public boolean setItemKey(Item item, String key) {
        item.setKey(key);
        return true;
    }

    public boolean setItemValue(Item item, String value) {
        item.setValue(value);
        return true;
    }

    public boolean setItemVar(Var<Item> item) {
        item.set(new Item());
        return true;
    }

    public boolean pushObj(Object obj) {
        push(obj);
        return true;
    }

    // Main document rule
    public Rule Document() {
        Var<Document> document = new Var<>();
        Var<List<Item>> heading = new Var<>(new ArrayList<>());
        Var<AccountSection> account = new Var<>();
        Var<List<DocumentSection>> sections = new Var<>(new ArrayList<>());

        return Sequence(
                ACTION(setDocumentVar(document)),
                IgnoreCase("1CClientBankExchange"),
                Whitespace(),

                // Parse heading items
                ZeroOrMore(
                        Sequence(
                                TestNot(IgnoreCase("СекцияРасчСчет")),
                                Item(),
                                ACTION(addItemToList(heading.get(), (Item) pop())),
                                OptionalWhitespace()
                        )
                ),
                ACTION(setHeading(document.get(), heading.get())),            // Parse account section
                AccountSection(),
                ACTION(setAccountSectionFromStack(account, (AccountSection) pop())),
                ACTION(setAccount(document.get(), account.get())),
                Whitespace(),

                // Parse document sections
                ZeroOrMore(
                        Sequence(
                                DocumentSection(),
                                ACTION(addSectionToList(sections.get(), (DocumentSection) pop())),
                                OptionalWhitespace()
                        )
                ),
                ACTION(setSections(document.get(), sections.get())),

                IgnoreCase("КонецФайла"),
                OptionalWhitespace(),
                EOI,
                ACTION(pushObj(document.get()))
        );
    }

    public Rule AccountSection() {
        Var<AccountSection> accountSection = new Var<>();
        Var<List<Item>> items = new Var<>(new ArrayList<>());

        return Sequence(
                ACTION(setAccountSectionVar(accountSection)),
                IgnoreCase("СекцияРасчСчет"),
                Whitespace(),

                ZeroOrMore(
                        Sequence(
                                TestNot(IgnoreCase("КонецРасчСчет")),
                                Item(),
                                ACTION(addItemToList(items.get(), (Item) pop())),
                                OptionalWhitespace()
                        )
                ),
                ACTION(setAccountItems(accountSection.get(), items.get())),

                IgnoreCase("КонецРасчСчет"),
                ACTION(pushObj(accountSection.get()))
        );
    }

    public Rule DocumentSection() {
        Var<DocumentSection> docSection = new Var<>();
        Var<List<Item>> items = new Var<>(new ArrayList<>());
        Var<String> sectionName = new Var<>();

        return Sequence(
                ACTION(setDocumentSectionVar(docSection)),
                IgnoreCase("СекцияДокумент="),
                SectionName(),
                ACTION(setStringVar(sectionName, (String) pop())),
                ACTION(setSectionName(docSection.get(), sectionName.get())),
                Whitespace(),

                ZeroOrMore(
                        Sequence(
                                TestNot(IgnoreCase("КонецДокумента")),
                                Item(),
                                ACTION(addItemToList(items.get(), (Item) pop())),
                                OptionalWhitespace()
                        )
                ),
                ACTION(setSectionItems(docSection.get(), items.get())),

                IgnoreCase("КонецДокумента"),
                ACTION(pushObj(docSection.get()))
        );
    }

    public Rule Item() {
        Var<Item> item = new Var<>();

        return Sequence(
                ACTION(setItemVar(item)),
                Key(),
                ACTION(setItemKey(item.get(), (String) pop())),
                OptionalWhitespace(),
                '=',
                OptionalWhitespace(),
                Value(),
                ACTION(setItemValue(item.get(), (String) pop())),
                ACTION(pushObj(item.get()))
        );
    }

    public Rule Key() {
        return Sequence(
                OneOrMore(
                        Sequence(
                                TestNot(AnyOf("=\r\n")),
                                ANY
                        )
                ),
                ACTION(pushObj(match().trim()))
        );
    }

    public Rule Value() {
        return Sequence(
                ZeroOrMore(
                        Sequence(
                                TestNot(AnyOf("\r\n")),
                                ANY
                        )
                ),
                ACTION(pushObj(match().trim()))
        );
    }

    public Rule SectionName() {
        return Sequence(
                OneOrMore(
                        Sequence(
                                TestNot(AnyOf("\r\n")),
                                ANY
                        )
                ),
                ACTION(pushObj(match().trim()))
        );
    }

    public Rule Whitespace() {
        return OneOrMore(AnyOf(" \t\r\n"));
    }

    public Rule OptionalWhitespace() {
        return ZeroOrMore(AnyOf(" \t\r\n"));
    }
}
