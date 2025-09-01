package com.github.nikolayill.parser.simple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SectionValidationTest {
    private final PropertiesParser parser = Parboiled.createParser(PropertiesParser.class);

    @Test
    @DisplayName("Valid matching section tags should parse successfully")
    public void validMatchingSectionTags() {
        String input = """
                [SectionOne]
                key=value
                [-SectionOne]
                """;
        ParsingResult<Map<String, String>> result = new ReportingParseRunner<Map<String, String>>(
                parser.Document()).run(input);

        assertTrue(result.matched, "Valid matching tags should parse");
        assertFalse(result.hasErrors(), "Should have no parsing errors");
    }

    @Test
    @DisplayName("Mismatched section tags should fail to parse")
    public void mismatchedSectionTags() {
        String input = """
                [SectionOne]
                key=value
                [-SectionTwo]
                """;
        ParsingResult<Map<String, String>> result = new ReportingParseRunner<Map<String, String>>(
                parser.Document()).run(input);

        assertFalse(result.matched, "Mismatched tags should fail to parse");
    }

    @Test
    @DisplayName("Multiple sections with matching tags should parse")
    public void multipleSectionsWithMatchingTags() {
        String input = """
                [SectionOne]
                keyOne=valueOne
                [-SectionOne]
                [SectionTwo]
                keyTwo=valueTwo
                [-SectionTwo]
                """;
        ParsingResult<Map<String, String>> result = new ReportingParseRunner<Map<String, String>>(
                parser.Document()).run(input);

        assertTrue(result.matched, "Multiple valid sections should parse");
        assertFalse(result.hasErrors(), "Should have no parsing errors");
    }

    @Test
    @DisplayName("Multiple sections with one mismatch should fail")
    public void multipleSectionsWithOneMismatch() {
        String input = """
                [SectionOne]
                keyOne=valueOne
                [-SectionOne]
                [SectionTwo]
                keyTwo=valueTwo
                [-SectionThree]
                """;
        ParsingResult<Map<String, String>> result = new ReportingParseRunner<Map<String, String>>(
                parser.Document()).run(input);

        assertFalse(result.matched, "Sections with mismatch should fail to parse");
    }
}
