package com.github.nikolayill.parser;

import com.github.nikolayill.dto.Document;
import org.junit.jupiter.api.Test;

public class SimpleParserTest {
      @Test
    public void testSimpleParse() throws Exception {
        String content = """
            1CClientBankExchange
            HeaderKey1=HeaderValue1
            HeaderKey2=HeaderValue2
            СекцияРасчСчет
            AccountKey1=AccountValue1
            AccountKey2=AccountValue2
            КонецРасчСчет
            СекцияДокумент=TestSection
            DocKey1=DocValue1
            DocKey2=DocValue2
            КонецДокумента
            КонецФайла
            """;
        
        ParboiledDocumentParserService parser = new ParboiledDocumentParserService();
        Document document = parser.parseFromString(content);
        
        System.out.println("Document parsed successfully!");
        System.out.println("Heading items: " + document.getHeading().size());
        System.out.println("Account items: " + document.getAccount().getItems().size());
        System.out.println("Document sections: " + document.getSections().size());
        
        if (document.getHeading().size() > 0) {
            for (int i = 0; i < document.getHeading().size(); i++) {
                System.out.println("Heading item " + i + ": " + document.getHeading().get(i).getKey() + "=" + document.getHeading().get(i).getValue());
            }
        }
        if (document.getAccount().getItems().size() > 0) {
            for (int i = 0; i < document.getAccount().getItems().size(); i++) {
                System.out.println("Account item " + i + ": " + document.getAccount().getItems().get(i).getKey() + "=" + document.getAccount().getItems().get(i).getValue());
            }
        }
    }
}
