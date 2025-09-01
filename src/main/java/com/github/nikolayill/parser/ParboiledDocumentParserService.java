package com.github.nikolayill.parser;

import com.github.nikolayill.dto.Document;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ParboiledDocumentParserService {
    
    private final ParboiledDocumentParser parser;
    private final ReportingParseRunner<Object> runner;
    
    public ParboiledDocumentParserService() {
        this.parser = Parboiled.createParser(ParboiledDocumentParser.class);
        this.runner = new ReportingParseRunner<>(parser.Document());
    }
    
    /**
     * Parse document from string content
     * @param content The document content to parse
     * @return Parsed Document object
     * @throws ParseException if parsing fails
     */
    public Document parseFromString(String content) throws ParseException {
        ParsingResult<Object> result = runner.run(content);
        
        if (!result.matched) {
            throw new ParseException("Failed to parse document: " + result.parseErrors);
        }
        
        return (Document) result.resultValue;
    }
    
    /**
     * Parse document from file
     * @param filePath Path to the file to parse
     * @return Parsed Document object
     * @throws ParseException if parsing fails
     * @throws IOException if file reading fails
     */
    public Document parseFromFile(Path filePath) throws ParseException, IOException {
        String content = Files.readString(filePath);
        return parseFromString(content);
    }
    
    /**
     * Parse document from file
     * @param fileName Name of the file to parse
     * @return Parsed Document object
     * @throws ParseException if parsing fails
     * @throws IOException if file reading fails
     */
    public Document parseFromFile(String fileName) throws ParseException, IOException {
        return parseFromFile(Path.of(fileName));
    }
    
    /**
     * Custom exception for parsing errors
     */
    public static class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }
        
        public ParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
