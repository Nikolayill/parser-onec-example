# ParboiledDocumentParser Tutorial: Understanding 1C Client Bank Exchange Format Parser

## Table of Contents
1. [Introduction](#introduction)
2. [What is Parboiled?](#what-is-parboiled)
3. [Project Overview](#project-overview)
4. [Grammar Specification](#grammar-specification)
5. [Parser Implementation Deep Dive](#parser-implementation-deep-dive)
6. [Stack Operations](#stack-operations)
7. [ACTION Blocks and Helper Methods](#action-blocks-and-helper-methods)
8. [Parsing Flow](#parsing-flow)
9. [Error Handling and Debugging](#error-handling-and-debugging)
10. [Best Practices](#best-practices)
11. [Troubleshooting](#troubleshooting)
12. [Detailed Summary: org.parboiled.support.Var Usage](#detailed-summary-orgparboiledsupportvar-usage)

## Introduction

This tutorial provides a comprehensive guide to understanding the `ParboiledDocumentParser` implementation for parsing 1C Client Bank Exchange format files. The parser is built using the Parboiled Java library, which is a parsing expression grammar (PEG) framework that enables elegant and powerful parsing solutions.

## What is Parboiled?

### Core Concepts

**Parboiled** is a Java library for building parsers using Parsing Expression Grammars (PEGs). It offers several key advantages:

1. **Type-safe**: Grammar rules are defined as Java methods
2. **Composable**: Rules can be combined and reused
3. **Stack-based**: Built-in value stack for AST construction
4. **Action Integration**: Seamless integration of parsing actions
5. **Error Reporting**: Detailed error messages with location information

### How Parboiled Works

Parboiled uses **bytecode generation** at runtime to create optimized parsers from your grammar rules. When you define a rule like:

```java
public Rule MyRule() {
    return Sequence("keyword", WhiteSpace(), Identifier());
}
```

Parboiled generates bytecode that creates a state machine for efficient parsing.

### Key Components

- **Rules**: Define grammar patterns (Sequence, FirstOf, ZeroOrMore, etc.)
- **Matchers**: Low-level pattern matching (Ch, String, AnyOf, etc.)
- **Value Stack**: Store and manipulate parsed values
- **Actions**: Execute code during parsing
- **Context**: Access to current parsing state

## Project Overview

### File Structure
```
src/main/java/com/github/nikolayill/
├── dto/
│   ├── Document.java          # Main document structure
│   ├── Item.java             # Key-value pairs
│   ├── AccountSection.java   # Account section data
│   └── DocumentSection.java  # Document section data
└── parser/
    ├── ParboiledDocumentParser.java        # Main parser (this tutorial focus)
    ├── ParboiledDocumentParserService.java # Service wrapper
    └── DocumentParser.java                 # CLI application
```

### Input Format (1C Client Bank Exchange)

The 1C Client Bank Exchange format follows this structure:

```
1CClientBankExchange
Key1=Value1
Key2=Value2
СекцияРасчСчет
Key3=Value3
Key4=Value4
КонецРасчСчет
СекцияДокумент=ИмяСекции
Key5=Value5
Key6=Value6
КонецДокумента
КонецФайла
```

## Grammar Specification

### BNF Grammar

The parser implements this BNF grammar:

```bnf
<DOCUMENT> ::= 1CClientBankExchange <DOC_BODY> КонецФайла
<DOC_BODY> ::= <HEADING> <ACCOUNT> <SECTIONS>
<HEADING> ::= <ITEMS>
<ACCOUNT> ::= СекцияРасчСчет <ITEMS> КонецРасчСчет
<SECTIONS> ::= <SECTION_BEGIN> <ITEMS> КонецДокумента
<SECTION_BEGIN> ::= СекцияДокумент=<SECTION_NAME>
<ITEMS> ::= <KEY>"="<VALUE> | <ITEMS>
<KEY> ::= String
<VALUE> ::= String
```

### Grammar Translation to Parboiled

Each BNF rule translates to a Parboiled rule method:

| BNF Rule     | Parboiled Method | Purpose                             |
|--------------|------------------|-------------------------------------|
| `<DOCUMENT>` | `Document()`     | Root rule, matches entire document  |
| `<ITEMS>`    | `Items()`        | Matches sequence of key-value pairs |
| `<KEY>`      | `Key()`          | Matches and captures key names      |
| `<VALUE>`    | `Value()`        | Matches and captures values         |

## Parser Implementation Deep Dive

### Class Structure

```java
public class ParboiledDocumentParser extends BaseParser<Object> {
    // Grammar rules (public methods returning Rule)
    // Helper methods for ACTION blocks (public methods returning boolean)
    // Private utility methods
}
```

### Key Grammar Rules

#### 1. Document Rule (Root)
```java
public Rule Document() {
    return Sequence(
        String("1CClientBankExchange"), NewLine(),
        pushDocument(),                    // ACTION: Create new Document
        Items(),                          // Parse heading items
        
        String("СекцияРасчСчет"), NewLine(),
        pushAccountSection(),             // ACTION: Create AccountSection
        Items(),                         // Parse account items
        String("КонецРасчСчет"), NewLine(),
        setAccountSectionFromStack(),    // ACTION: Set account section
        
        String("СекцияДокумент="),
        Value(),                         // Parse section name
        NewLine(),
        pushDocumentSection(),           // ACTION: Create DocumentSection
        Items(),                        // Parse document items
        String("КонецДокумента"), NewLine(),
        setDocumentSectionFromStack(),  // ACTION: Set document section
        
        String("КонецФайла"),
        Optional(NewLine())
    );
}
```

**Flow Explanation:**
1. Match literal "1CClientBankExchange"
2. Create new Document object and push to stack
3. Parse heading key-value pairs
4. Parse account section with boundaries
5. Parse document section with name and items
6. Match end marker "КонецФайла"

#### 2. Items Rule (Repetitive Parsing)
```java
public Rule Items() {
    return ZeroOrMore(Item());
}
```

This uses `ZeroOrMore` to match zero or more `Item()` patterns, implementing the recursive nature of `<ITEMS>` in BNF.

#### 3. Item Rule (Key-Value Pairs)
```java
public Rule Item() {
    return Sequence(
        Key(),
        OptionalWhitespace(),
        Ch('='),
        OptionalWhitespace(),
        Value(),
        NewLine(),
        createAndAddItem()  // ACTION: Create Item and add to current container
    );
}
```

**Parsing Flow:**
1. Parse key and push to stack
2. Match '=' with optional whitespace
3. Parse value and push to stack
4. Create Item object from stack values
5. Add Item to current container (Document, AccountSection, or DocumentSection)

#### 4. Key and Value Rules
```java
public Rule Key() {
    return Sequence(
        OneOrMore(
            TestNot(AnyOf("=\r\n")),  // Negative lookahead
            ANY                       // Match any character except =, \r, \n
        ),
        pushKey()  // ACTION: Push matched text as key
    );
}

public Rule Value() {
    return Sequence(
        ZeroOrMore(
            TestNot(AnyOf("\r\n")),   // Negative lookahead
            ANY                       // Match any character except \r, \n
        ),
        pushValue()  // ACTION: Push matched text as value
    );
}
```

**Key Points:**
- `TestNot()` implements negative lookahead
- `ANY` matches any single character
- `match()` captures the matched text
- Values can be empty (ZeroOrMore vs OneOrMore)

## Stack Operations

### Understanding the Value Stack

Parboiled provides a **value stack** for storing intermediate parsing results. Think of it as a LIFO (Last In, First Out) data structure:

```
Stack Operations:
push(value)     # Add value to top
pop()           # Remove and return top value
peek()          # Return top value without removing
peek(index)     # Return value at index from top
```

### Stack Usage in Parser

#### Stack State During Parsing

```
Initial State: []

After Key():   [String key]
After Value(): [String key, String value]
After Item():  [] (key and value consumed to create Item)
```

#### Helper Methods for Stack Operations

```java
// Push operations
public boolean pushDocument() {
    push(new Document());
    return true;
}

public boolean pushKey() {
    push(match());  // Push matched text
    return true;
}

// Pop and use operations
public boolean createAndAddItem() {
    String value = (String) pop();    // Get value from stack
    String key = (String) pop();      // Get key from stack
    Item item = new Item(key, value);
    
    // Add to current container
    Object container = peek();
    if (container instanceof Document) {
        ((Document) container).addItem(item);
    } else if (container instanceof AccountSection) {
        ((AccountSection) container).addItem(item);
    } else if (container instanceof DocumentSection) {
        ((DocumentSection) container).addItem(item);
    }
    return true;
}
```

### Critical Stack Management

**Stack Alignment**: The stack must be properly aligned for the parser to work correctly:

```java
// WRONG: Unbalanced stack operations
public Rule BadRule() {
    return Sequence(
        SomePattern(),
        ACTION(push("value1")),
        ACTION(push("value2")),
        // Missing pop() operations!
    );
}

// CORRECT: Balanced stack operations
public Rule GoodRule() {
    return Sequence(
        SomePattern(),
        pushValue1(),
        pushValue2(),
        consumeValues()  // Pops both values
    );
}
```

## ACTION Blocks and Helper Methods

### Why Helper Methods?

Originally, the parser used inline ACTION blocks:

```java
// Original (problematic) approach
Rule SomeRule() {
    return Sequence(
        Pattern(),
        ACTION(() -> {
            // Inline code here
            return true;
        })
    );
}
```

**Problems with Inline Actions:**
1. **Compilation Issues**: Complex lambda expressions in Parboiled 1.x
2. **Code Generation**: Parboiled's bytecode generation struggles with closures
3. **Debugging**: Harder to debug inline code
4. **Reusability**: Can't reuse action logic

### Helper Method Pattern

**Solution**: Extract actions into helper methods:

```java
// Helper method approach
Rule SomeRule() {
    return Sequence(
        Pattern(),
        helperMethod()  // Simple method call
    );
}

public boolean helperMethod() {  // Must be public!
    // Action logic here
    return true;  // Must return boolean
}
```

### Key Requirements for Helper Methods

1. **Public Visibility**: Parboiled's code generation requires public access
2. **Boolean Return**: All helper methods must return boolean
3. **True Return**: Return true for successful execution
4. **Stack Operations**: Use push(), pop(), peek() for value management

### Example Helper Methods

```java
public boolean pushAccountSection() {
    push(new AccountSection());
    return true;
}

public boolean setAccountSectionFromStack() {
    AccountSection accountSection = (AccountSection) pop();
    Document document = (Document) peek();
    document.setAccountSection(accountSection);
    return true;
}

public boolean createAndAddItem() {
    String value = (String) pop();
    String key = (String) pop();
    Item item = new Item(key, value);
    
    Object container = peek();
    if (container instanceof Document) {
        ((Document) container).addItem(item);
    } else if (container instanceof AccountSection) {
        ((AccountSection) container).addItem(item);
    } else if (container instanceof DocumentSection) {
        ((DocumentSection) container).addItem(item);
    }
    return true;
}
```

## Parsing Flow

### Complete Parsing Example

Let's trace through parsing this input:

```
1CClientBankExchange
GlobalKey=GlobalValue
СекцияРасчСчет
AccountKey=AccountValue
КонецРасчСчет
СекцияДокумент=TestSection
DocKey=DocValue
КонецДокумента
КонецФайла
```

#### Step-by-Step Execution

1. **Document Rule Start**
   ```
   Stack: []
   Input: "1CClientBankExchange\nGlobalKey=GlobalValue\n..."
   ```

2. **Match Header and Push Document**
   ```
   Stack: [Document{}]
   Action: pushDocument() executed
   ```

3. **Parse Global Items**
   ```
   Key(): "GlobalKey" → Stack: [Document{}, "GlobalKey"]
   Value(): "GlobalValue" → Stack: [Document{}, "GlobalKey", "GlobalValue"]
   createAndAddItem(): Stack: [Document{items: [Item("GlobalKey", "GlobalValue")]}]
   ```

4. **Parse Account Section**
   ```
   pushAccountSection(): Stack: [Document{...}, AccountSection{}]
   Key(): "AccountKey" → Stack: [Document{...}, AccountSection{}, "AccountKey"]
   Value(): "AccountValue" → Stack: [Document{...}, AccountSection{}, "AccountKey", "AccountValue"]
   createAndAddItem(): Stack: [Document{...}, AccountSection{items: [Item("AccountKey", "AccountValue")]}]
   setAccountSectionFromStack(): Stack: [Document{accountSection: AccountSection{...}}]
   ```

5. **Parse Document Section**
   ```
   Value(): "TestSection" → Stack: [Document{...}, "TestSection"]
   pushDocumentSection(): Stack: [Document{...}, "TestSection", DocumentSection{}]
   setDocumentSectionName(): Stack: [Document{...}, DocumentSection{name: "TestSection"}]
   // Parse items...
   setDocumentSectionFromStack(): Stack: [Document{complete}]
   ```

### Parser State Transitions

```
State Machine Representation:

START → HEADER → GLOBAL_ITEMS → ACCOUNT_START → ACCOUNT_ITEMS → 
ACCOUNT_END → DOC_START → DOC_ITEMS → DOC_END → FILE_END → SUCCESS
```

## Error Handling and Debugging

### Common Error Patterns

#### 1. Stack Underflow
```java
// Symptom: EmptyStackException
// Cause: More pop() than push() operations

// Debug: Add stack size checks
public boolean safePopHelper() {
    if (getValueStack().size() == 0) {
        throw new RuntimeException("Stack underflow in safePopHelper");
    }
    String value = (String) pop();
    // ... rest of logic
    return true;
}
```

#### 2. Type Cast Exceptions
```java
// Symptom: ClassCastException
// Cause: Incorrect assumptions about stack content

// Debug: Add type checks
public boolean safeTypecastHelper() {
    Object obj = peek();
    if (!(obj instanceof ExpectedType)) {
        throw new RuntimeException("Expected ExpectedType, got " + obj.getClass());
    }
    ExpectedType typed = (ExpectedType) obj;
    // ... rest of logic
    return true;
}
```

#### 3. Grammar Ambiguity
```java
// Symptom: Unexpected parsing behavior
// Cause: Multiple rules matching same input

// Solution: Use FirstOf with specific ordering
Rule AmbiguousRule() {
    return FirstOf(
        SpecificPattern(),    // More specific first
        GeneralPattern()      // More general last
    );
}
```

### Debugging Techniques

#### 1. Add Logging Helper Methods
```java
public boolean logStackState(String location) {
    System.out.println(location + ": Stack size = " + getValueStack().size());
    for (int i = 0; i < getValueStack().size(); i++) {
        System.out.println("  [" + i + "] = " + peek(i));
    }
    return true;
}

// Use in rules:
Rule DebuggingRule() {
    return Sequence(
        SomePattern(),
        logStackState("After SomePattern"),
        SomeAction()
    );
}
```

#### 2. Incremental Testing
```java
@Test
public void testIndividualRules() {
    ParboiledDocumentParser parser = Parboiled.createParser(ParboiledDocumentParser.class);
    
    // Test individual rules
    ParsingResult<Object> result = new ReportingParseRunner<>(parser.Key())
        .run("TestKey");
    assertTrue(result.matched);
    
    result = new ReportingParseRunner<>(parser.Value())
        .run("TestValue");
    assertTrue(result.matched);
}
```

#### 3. Error Reporting
```java
@Test
public void parseWithDetailedErrors() {
    ParboiledDocumentParser parser = Parboiled.createParser(ParboiledDocumentParser.class);
    String input = "invalid input";
    
    ParsingResult<Object> result = new ReportingParseRunner<>(parser.Document())
        .run(input);
    
    if (!result.matched) {
        System.out.println("Parse errors:");
        for (ParseError error : result.parseErrors) {
            System.out.println("  " + ErrorUtils.printParseError(error));
        }
    }
}
```

## Best Practices

### 1. Rule Design

#### Keep Rules Simple
```java
// GOOD: Simple, focused rule
Rule Key() {
    return Sequence(
        OneOrMore(TestNot(AnyOf("=\r\n")), ANY),
        pushKey()
    );
}

// AVOID: Complex, multi-purpose rule
Rule ComplexRule() {
    return FirstOf(
        Sequence(/* complex pattern 1 */),
        Sequence(/* complex pattern 2 */),
        Sequence(/* complex pattern 3 */)
    );
}
```

#### Use Descriptive Names
```java
// GOOD
Rule AccountSectionHeader() { ... }
Rule DocumentSectionItem() { ... }

// AVOID
Rule Rule1() { ... }
Rule ParseStuff() { ... }
```

### 2. Stack Management

#### Follow Stack Discipline
```java
// Pattern: Push exactly what you'll pop
Rule WellBehavedRule() {
    return Sequence(
        Pattern1(), pushValue1(),    // +1 to stack
        Pattern2(), pushValue2(),    // +1 to stack
        consumeBothValues()          // -2 from stack
        // Net effect: 0 (balanced)
    );
}
```

#### Document Stack State
```java
/**
 * Parses key-value item and adds to current container.
 * Stack before: [..., Container]
 * Stack after:  [..., Container] (unchanged)
 * Consumes: key and value from parsing
 */
Rule Item() {
    return Sequence(
        Key(),      // Pushes key string
        Ch('='),
        Value(),    // Pushes value string
        createAndAddItem()  // Consumes key, value; adds Item to Container
    );
}
```

### 3. Error Recovery

#### Provide Meaningful Error Messages
```java
Rule Document() {
    return Sequence(
        String("1CClientBankExchange").label("document header"),
        NewLine(),
        // ... rest of rules
    );
}
```

#### Use Optional and Test Patterns
```java
Rule FlexiblePattern() {
    return Sequence(
        RequiredPart(),
        Optional(OptionalPart()),  // Graceful degradation
        TestNot(ErrorPattern()).label("unexpected content")
    );
}
```

### 4. Performance Considerations

#### Minimize Backtracking
```java
// GOOD: Deterministic choice
Rule EfficientChoice() {
    return FirstOf(
        Sequence(DistinctPrefix1(), Rest1()),
        Sequence(DistinctPrefix2(), Rest2())
    );
}

// AVOID: Ambiguous prefixes causing backtracking
Rule InefficientChoice() {
    return FirstOf(
        Sequence(CommonPrefix(), Variant1()),
        Sequence(CommonPrefix(), Variant2())
    );
}
```

#### Cache Parser Instances
```java
// GOOD: Reuse parser instance
private static final ParboiledDocumentParser PARSER = 
    Parboiled.createParser(ParboiledDocumentParser.class);

// AVOID: Create new parser each time
public Document parse(String input) {
    ParboiledDocumentParser parser = Parboiled.createParser(ParboiledDocumentParser.class);
    // ...
}
```

## Troubleshooting

### Common Issues and Solutions

#### Issue 1: "Method not accessible" Error
**Symptom**: Runtime error about method accessibility
**Cause**: Helper methods are private
**Solution**: Make all helper methods public

```java
// WRONG
private boolean helperMethod() { ... }

// CORRECT
public boolean helperMethod() { ... }
```

#### Issue 2: Stack Overflow
**Symptom**: StackOverflowError during parsing
**Cause**: Left-recursive grammar rules
**Solution**: Rewrite as right-recursive or iterative

```java
// WRONG: Left-recursive
Rule Items() {
    return FirstOf(
        Sequence(Items(), Item()),  // Left recursion!
        EMPTY
    );
}

// CORRECT: Iterative
Rule Items() {
    return ZeroOrMore(Item());
}
```

#### Issue 3: Incomplete Parsing
**Symptom**: Parser succeeds but doesn't consume entire input
**Solution**: Use ReportingParseRunner and check match coverage

```java
ParsingResult<Object> result = new ReportingParseRunner<>(parser.Document())
    .run(input);

if (result.matched && result.hasParseErrors()) {
    System.out.println("Partial match - input not fully consumed");
    // Handle incomplete parsing
}
```

#### Issue 4: Memory Leaks in Long Inputs
**Symptom**: OutOfMemoryError with large files
**Solution**: Implement streaming or chunked parsing

```java
// For large files, consider parsing in chunks
// or implementing a streaming parser with limited lookahead
```

### Testing Strategy

#### Unit Test Individual Rules
```java
@Test
public void testKeyRule() {
    ParboiledDocumentParser parser = Parboiled.createParser(ParboiledDocumentParser.class);
    
    // Test valid keys
    assertTrue(TestUtils.parse(parser.Key(), "ValidKey"));
    assertTrue(TestUtils.parse(parser.Key(), "Key_With_Underscores"));
    
    // Test invalid keys
    assertFalse(TestUtils.parse(parser.Key(), "Key=WithEquals"));
    assertFalse(TestUtils.parse(parser.Key(), "Key\nWithNewline"));
}
```

#### Integration Test Complete Documents
```java
@Test
public void testCompleteDocument() {
    String input = loadResourceFile("test-documents/valid-document.txt");
    Document result = parseDocument(input);
    
    assertNotNull(result);
    assertEquals("Expected title", result.getTitle());
    // ... more assertions
}
```

#### Test Error Cases
```java
@Test
public void testMalformedDocument() {
    String input = "1CClientBankExchange\nMissingEquals\n";
    
    assertThrows(ParseException.class, () -> {
        parseDocument(input);
    });
}
```

---

## Detailed Summary: org.parboiled.support.Var Usage

### What is Var?
`org.parboiled.support.Var` is a mutable container class provided by Parboiled to manage state within parsing rules. It allows you to store and update values during parsing, overcoming Java's restriction on modifying local variables inside rule expressions and ACTION blocks.

### Why Not Use Simple Fields or DTOs?
- **Initialization vs. Parsing Phase**: Simple fields or DTOs are set during the parser's initialization, not during each parse. Parboiled rules are constructed once, but parsing happens many times.
- **Thread Safety**: Parser instances may be reused concurrently. Using fields would cause data races and unpredictable results.
- **Parse State Isolation**: Each parse operation needs its own isolated state. Fields would be shared across parses, breaking isolation.
- **Reference Updates**: You often need to replace entire object references (not just mutate them), which is not possible with final or effectively final variables in Java lambdas.
- **Recursion and Rule Reuse**: The same rule may be applied multiple times in different contexts, requiring independent state for each invocation.

### How Var Works
- **Declaration**: `Var<T> var = new Var<>();` or with initial value: `Var<List<Item>> items = new Var<>(new ArrayList<>());`
- **Set Value**: `var.set(value);`
- **Get Value**: `T value = var.get();`
- **Pass to Helper Methods**: Helper methods can update or read the value inside ACTION blocks.

### Example Usage in ParboiledDocumentParser
```java
Var<Document> document = new Var<>();
ACTION(setDocumentVar(document)) // Sets a new Document instance

Var<List<Item>> heading = new Var<>(new ArrayList<>());
ACTION(addItemToList(heading.get(), (Item) pop()))

ACTION(setAccountSectionFromStack(account, (AccountSection) pop()))
ACTION(setHeading(document.get(), heading.get()))
```

### Benefits of Var
- **State Management**: Track state across rule invocations and parsing phases
- **Type Safety**: Strongly typed, avoids generic stack operations
- **Modularity**: Clean separation of grammar and data manipulation
- **Thread Safety & Isolation**: Each parse gets its own Var instances

### Best Practices
- Always use Var for mutable state in rules
- Initialize collections in the constructor: `new Var<>(new ArrayList<>())`
- Use helper methods for all modifications
- Pass Var objects to helper methods, not just their contents
- Use descriptive names for Var variables

### Summary
Using `Var` is essential for correct, thread-safe, and maintainable Parboiled parsers. It enables mutable state management scoped to each parse operation, which cannot be achieved with simple fields or DTOs due to Java and Parboiled's execution model.

---

## Conclusion

The `ParboiledDocumentParser` demonstrates the power and elegance of PEG-based parsing with Parboiled. Key takeaways:

1. **Grammar Design**: Clear BNF specification translates directly to Parboiled rules
2. **Stack Management**: Disciplined push/pop operations enable complex AST construction
3. **Helper Methods**: Extract actions for better maintainability and debugging
4. **Error Handling**: Comprehensive error reporting and recovery strategies
5. **Testing**: Thorough unit and integration testing ensures parser reliability

This parser architecture scales well to more complex grammars and can be extended with additional 1C Client Bank Exchange format features as needed.

For further reading, consult the [Parboiled documentation](https://github.com/sirthias/parboiled/wiki) and explore the test cases in the project for additional usage examples.
