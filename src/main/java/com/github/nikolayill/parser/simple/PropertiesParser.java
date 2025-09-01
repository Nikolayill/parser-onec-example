package com.github.nikolayill.parser.simple;

import com.github.nikolayill.dto.simple.SimpleProperties;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.SkipNode;
import org.parboiled.annotations.SuppressSubnodes;
import org.parboiled.support.Var;

import java.util.HashMap;
import java.util.Map;

@BuildParseTree
public class PropertiesParser extends BaseParser<Object> {
    public Rule Document(){
        Var<SimpleProperties> propertiesVar = new Var<>();

        return Sequence(
                ACTION(setSimpleProperties(propertiesVar)),
                RootPropertiesOrSection(propertiesVar),
                EOI,
                ACTION(push(propertiesVar.get())));
    }

    public boolean setSimpleProperties(Var<SimpleProperties> propertiesVar) {
        propertiesVar.set(new SimpleProperties());
        return true;
    }

    public Rule RootPropertiesOrSection(Var<SimpleProperties> propertiesVar){
        return OneOrMore(
                SectionOrProperties(propertiesVar),
                OptNewLine()
        );
    }

    @SkipNode
    public Rule SectionOrProperties(Var<SimpleProperties> propertiesVar) {
        Var<Map<String, String>> properties = new Var<>();
        return FirstOf(
                Sequence(
                    Section(),
                    ACTION(addSection(propertiesVar, (SimpleProperties.SimplePropertiesSection) pop()))
                ),
                Sequence(
                    ACTION(properties.set(new HashMap<>())),
                    PropertiesLines(properties),
                    ACTION(addRootProperties(propertiesVar, properties))
                )
        );
    }

    public boolean addRootProperties(Var<SimpleProperties> propertiesVar, Var<Map<String, String>> properties) {
        Map<String, String> properties1 = propertiesVar.get().getProperties();
        properties1.putAll(properties.get());
        return true;
    }

    public boolean addSection(Var<SimpleProperties> propertiesVar, SimpleProperties.SimplePropertiesSection section) {
        propertiesVar.get().getSections().add(section);
        return true;
    }

    public boolean setSection(Var<SimpleProperties.SimplePropertiesSection> section) {
        return section.set(new SimpleProperties.SimplePropertiesSection());
    }

    public boolean setSectionName(Var<SimpleProperties.SimplePropertiesSection> section, String name) {
        section.get().setName(name);
        return true;
    }

    public Rule Section() {
        Var<String> sectionName = new Var<>();
        Var<SimpleProperties.SimplePropertiesSection> section = new Var<>();
        Var<Map<String, String>> properties = new Var<>();
        return Sequence(
                ACTION(setSection(section)),
                SectionDef(sectionName),
                ACTION(setSectionName(section, sectionName.get())),
                Newlines(),
                Sequence(
                    ACTION(properties.set(new HashMap<>())),
                    PropertiesLines(properties)
                ),
                EndSectionDef(sectionName),
                OptNewLine(),
                ACTION(addSectionProperties(section, properties.get())),
                ACTION(push(section.get()))
        );
    }

    public boolean addSectionProperties(Var<SimpleProperties.SimplePropertiesSection> section,
                                         Map<String, String> stringStringMap) {
        section.get().setProperties(stringStringMap);
        return true;
    }

    @SuppressSubnodes
    public Rule SectionDef(Var<String> sectionName) {
        return Sequence(
            Ch('['), 
            SectionName(), 
            ACTION(captureSectionName(sectionName)),
            Ch(']')
        );
    }

    @SuppressSubnodes
    public Rule EndSectionDef(Var<String> sectionName){
        return Sequence(
            Ch('['), 
            Ch('-'), 
            SectionName(),
            ACTION(validateSectionName(sectionName)),
            Ch(']')
        );
    }

    public Rule SectionName() {
        return OneOrMore(
                FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'))
        );
    }

    // Helper method to capture section name from match
    public boolean captureSectionName(Var<String> sectionName) {
        String matched = match();
        sectionName.set(matched);
        return true;
    }

    // Helper method to validate the section name matches
    public boolean validateSectionName(Var<String> sectionName) {
        String expectedName = sectionName.get();
        String actualName = match();
        return expectedName != null && expectedName.equals(actualName);
    }

    @SkipNode
    public Rule PropertiesLines(Var<Map<String, String>> properties){
        return OneOrMore(
                KeyValuePair(), // label replaces node name created from rule method name
                ACTION(addPropertyKeyValue(properties)),
                OptNewLine()
        );
    }

    public boolean addPropertyKeyValue(Var<Map<String, String>> properties) {
        String value = (String) pop();
        String key = (String) pop();
        properties.get().put(key, value);
        return true;
    }

    public Rule KeyValuePair(){
        return Sequence(
                Sequence(
                        KeyName(),
                        ACTION(push(match()))
                ),
                Ch('='),
                Sequence(
                        Value(),
                        ACTION(push(match()))
                ));
    }

    @SuppressSubnodes
    public Rule Value() {
        return ZeroOrMore(TestNot(Newline()), ANY);
    }

    @SuppressSubnodes
    public Rule KeyName() {
        return OneOrMore(
                FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'))
        );
    }

    @SkipNode
    public Rule OptNewLine(){
        return Optional(Newlines());
    }

    @SuppressSubnodes
    public Rule Newlines() {
        return OneOrMore(Newline());
    }

    @SuppressSubnodes
    public Rule Newline() {
        return FirstOf('\n', Sequence('\r', Optional('\n')));
    }
}
