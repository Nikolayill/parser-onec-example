package com.github.nikolayill.parser.simple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.Var;

import java.util.Map;

public class PropertiesParserTest {
    private final PropertiesParser parser = Parboiled.createParser(PropertiesParser.class);

    @Test
    public void singleKeyValue() {
        String input = "theName=theValue";
        ParsingResult<Map<String, String>> run = new ReportingParseRunner<Map<String, String>>(
                parser.Document()).run(input);

        assertFalse(run.hasErrors());

        String nodeTree = ParseTreeUtils.printNodeTree(run);
        System.out.println(nodeTree);
    }

    @Test
    public void multipleKeyValue() {
        String input = """
                keyOne=valueOne
                keyTwo=valueTwo
                keyThree=valueThree
                """;
        ParsingResult<Map<String, String>> run = new RecoveringParseRunner<Map<String, String>>(
                parser.Document()).run(input);


        String nodeTree = ParseTreeUtils.printNodeTree(run);
        System.out.println(nodeTree);

        assertFalse(run.hasErrors());
    }

    @DisplayName("Test of single rule \"KeyName\"")
    @Test
    void ruleKeyNameTest() {
        ParsingResult<Map<String, String>> run =
                new ReportingParseRunner<Map<String, String>>(parser.KeyName()).run("testKey");

        assertFalse(run.hasErrors());
    }

    @DisplayName("Test of single rule \"SectionDef\"")
    @Test
    void ruleSectionNameTest() {
        Var<String> sectionName = new Var<>();

        ParsingResult<Map<String, String>> run =
                new ReportingParseRunner<Map<String, String>>(parser.SectionDef(sectionName)).run("[testKey]");

        assertFalse(run.hasErrors());
    }

    @DisplayName("Parse config with one section")
    @Test
    public void section() {
        String input = """
                [SectionOne]
                keyOne=valueOne
                keyTwo=valueTwo
                keyThree=valueThree
                [-SectionOne]
                """;
        ParsingResult<Map<String, String>> run = new RecoveringParseRunner<Map<String, String>>(
                parser.Document()).run(input);


        String nodeTree = ParseTreeUtils.printNodeTree(run);
        System.out.println(nodeTree);

        assertFalse(run.hasErrors());
    }

    @DisplayName("Parse config with one section")
    @Test
    public void sectionWithRootKeyValues() {
        String input = """
                keyOne=valueOne
                keyTwo=valueTwo
                [SectionOne]
                keyThree=valueThree
                keyFour=valueFour
                [-SectionOne]
                """;
        ParsingResult<Map<String, String>> run = new RecoveringParseRunner<Map<String, String>>(
                parser.Document()).run(input);


        String nodeTree = ParseTreeUtils.printNodeTree(run);
        System.out.println(nodeTree);

        assertFalse(run.hasErrors());
    }

    @DisplayName("Parse config with two sections")
    @Test
    public void multipleSectionsWithRootKeyValues() {
        String input = """
                keyOne=valueOne
                [SectionOne]
                keyTwo=valueTwo
                [-SectionOne]
                [SectionTwo]
                keyThree=valueThree
                keyFour=valueFour
                [-SectionTwo]
                """;
        ParsingResult<Map<String, String>> run = new RecoveringParseRunner<Map<String, String>>(
                parser.Document()).run(input);


        String nodeTree = ParseTreeUtils.printNodeTree(run);
        System.out.println(nodeTree);

        assertFalse(run.hasErrors());
    }

    @DisplayName("Parse config with two sections shuffled with root properties, ended with section")
    @Test
    public void multipleSectionsShuffledWithRootKeyValues1() {
        String input = """
                keyOne=valueOne
                [SectionOne]
                keyTwo=valueTwo
                [-SectionOne]
                keyThree=valueThree
                keyFour=valueFour
                [SectionTwo]
                keyFive=valueFive
                keySix=valueSix
                keySeven=valueSeven
                [-SectionTwo]
                """;
        ParsingResult<Map<String, String>> run = new RecoveringParseRunner<Map<String, String>>(
                parser.Document()).run(input);


        String nodeTree = ParseTreeUtils.printNodeTree(run);
        System.out.println(nodeTree);

        assertFalse(run.hasErrors());
    }

    @DisplayName("Parse config with two sections shuffled with root properties, ended with root property")
    @Test
    public void multipleSectionsShuffledWithRootKeyValues2() {
        String input = """
                keyOne=valueOne
                [SectionOne]
                keyTwo=valueTwo
                [-SectionOne]
                keyThree=valueThree
                keyFour=valueFour
                [SectionTwo]
                keyFive=valueFive
                keySix=valueSix
                keySeven=valueSeven
                [-SectionTwo]
                keyEight=valueEight
                keyNine=valueNine
                """;
        ParsingResult<Map<String, String>> run = new RecoveringParseRunner<Map<String, String>>(
                parser.Document()).run(input);


        String nodeTree = ParseTreeUtils.printNodeTree(run);
        System.out.println(nodeTree);

        assertFalse(run.hasErrors());
    }

    @DisplayName("Parse config with empty strings in between")
    @Test
    public void multipleSectionsShuffledWithRootKeyValues3() {
        String input = """
                keyOne=valueOne
                
                [SectionOne]
                keyTwo=valueTwo
                [-SectionOne]
                
                keyThree=valueThree
                keyFour=valueFour
                
                
                [SectionTwo]
                keyFive=valueFive
                keySix=valueSix
                keySeven=valueSeven
                [-SectionTwo]
                
                keyEight=valueEight
                keyNine=valueNine
                
                """;
        ParsingResult<Map<String, String>> run = new RecoveringParseRunner<Map<String, String>>(
                parser.Document()).run(input);


        String nodeTree = ParseTreeUtils.printNodeTree(run);
        System.out.println(nodeTree);

        assertFalse(run.hasErrors());
    }


}