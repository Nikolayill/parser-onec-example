package com.github.nikolayill.parser;

import com.github.nikolayill.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class ParboiledDocumentParserTest {
    
    private ParboiledDocumentParserService parserService;
    
    @BeforeEach
    public void setUp() {
        parserService = new ParboiledDocumentParserService();
    }
    
    @Test
    public void testParseSimpleDocument() throws Exception {
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
        
        Document document = parserService.parseFromString(content);
        
        assertNotNull(document);
        
        // Check heading
        assertNotNull(document.getHeading());
        assertEquals(2, document.getHeading().size());
        assertEquals("HeaderKey1", document.getHeading().get(0).getKey());
        assertEquals("HeaderValue1", document.getHeading().get(0).getValue());
        assertEquals("HeaderKey2", document.getHeading().get(1).getKey());
        assertEquals("HeaderValue2", document.getHeading().get(1).getValue());
        
        // Check account section
        assertNotNull(document.getAccount());
        assertEquals(2, document.getAccount().getItems().size());
        assertEquals("AccountKey1", document.getAccount().getItems().get(0).getKey());
        assertEquals("AccountValue1", document.getAccount().getItems().get(0).getValue());
        
        // Check document sections
        assertNotNull(document.getSections());
        assertEquals(1, document.getSections().size());
        DocumentSection section = document.getSections().get(0);
        assertEquals("TestSection", section.getName());
        assertEquals(2, section.getItems().size());
        assertEquals("DocKey1", section.getItems().get(0).getKey());
        assertEquals("DocValue1", section.getItems().get(0).getValue());
    }
    
    @Test
    public void testParseEmptyHeading() throws Exception {
        String content = """
            1CClientBankExchange
            СекцияРасчСчет
            AccountKey1=AccountValue1
            КонецРасчСчет
            СекцияДокумент=TestSection
            DocKey1=DocValue1
            КонецДокумента
            КонецФайла
            """;
        
        Document document = parserService.parseFromString(content);
        
        assertNotNull(document);
        assertEquals(0, document.getHeading().size());
        assertNotNull(document.getAccount());
        assertEquals(1, document.getAccount().getItems().size());
    }
    
    @Test
    public void testParseMultipleDocumentSections() throws Exception {
        String content = """
            1CClientBankExchange
            HeaderKey=HeaderValue
            СекцияРасчСчет
            AccountKey=AccountValue
            КонецРасчСчет
            СекцияДокумент=Section1
            Key1=Value1
            КонецДокумента
            СекцияДокумент=Section2
            Key2=Value2
            КонецДокумента
            КонецФайла
            """;
        
        Document document = parserService.parseFromString(content);
        
        assertNotNull(document);
        assertEquals(2, document.getSections().size());
        assertEquals("Section1", document.getSections().get(0).getName());
        assertEquals("Section2", document.getSections().get(1).getName());
    }
    
    @Test
    public void testParseExampleFile() throws Exception {
        // Test parsing the example file
        Path examplePath = Paths.get("example.kl_to_1c");
        if (Files.exists(examplePath)) {
            Document document = parserService.parseFromFile(examplePath);
            assertNotNull(document);
            
            // Verify basic structure
            assertNotNull(document.getHeading());
            assertNotNull(document.getAccount());
            assertNotNull(document.getSections());
        }
    }
      @Test
    public void testParseInvalidDocument() {
        String invalidContent = """
            InvalidHeader
            Key=Value
            КонецФайла
            """;
        
        assertThrows(ParboiledDocumentParserService.ParseException.class, () -> {
            parserService.parseFromString(invalidContent);
        });
    }
      @Test
    public void testParseMissingEnd() {
        String invalidContent = """
            1CClientBankExchange
            Key=Value
            СекцияРасчСчет
            AccountKey=AccountValue
            КонецРасчСчет
            """;
        // Missing КонецФайла
        
        assertThrows(ParboiledDocumentParserService.ParseException.class, () -> {
            parserService.parseFromString(invalidContent);
        });
    }
    
    @Test
    public void testParseWithSpacesAndTabs() throws Exception {
        String content = """
            1CClientBankExchange
              HeaderKey  =  HeaderValue  
            	TabKey	=	TabValue	
            СекцияРасчСчет
              AccountKey  =  AccountValue  
            КонецРасчСчет
            СекцияДокумент=TestSection
              DocKey  =  DocValue  
            КонецДокумента
            КонецФайла
            """;
        
        Document document = parserService.parseFromString(content);
        
        assertNotNull(document);
        assertEquals(2, document.getHeading().size());
        assertEquals("HeaderKey", document.getHeading().get(0).getKey());
        assertEquals("HeaderValue", document.getHeading().get(0).getValue());
        assertEquals("TabKey", document.getHeading().get(1).getKey());
        assertEquals("TabValue", document.getHeading().get(1).getValue());
    }
}
