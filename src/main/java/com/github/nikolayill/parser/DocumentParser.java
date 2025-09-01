/* DocumentParser.java */
package com.github.nikolayill.parser;

import com.github.nikolayill.dto.Document;
import java.util.*;

public class DocumentParser {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java DocumentParser <input-file>");
            System.exit(1);
        }
        
        try {
            ParboiledDocumentParserService parser = new ParboiledDocumentParserService();
            Document document = parser.parseFromFile(args[0]);
            
            System.out.println("Parsing completed successfully.");
            System.out.println("Document structure:");
            System.out.println("- Heading items: " + document.getHeading().size());
            System.out.println("- Account items: " + document.getAccount().getItems().size());
            System.out.println("- Document sections: " + document.getSections().size());
            
            if (!document.getSections().isEmpty()) {
                System.out.println("Sections:");
                document.getSections().forEach(section -> 
                    System.out.println("  - " + section.getName() + " (" + section.getItems().size() + " items)"));
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing document: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
