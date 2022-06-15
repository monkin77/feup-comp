# Compilers Project

For this project, you need to install [Java](https://jdk.java.net/), [Gradle](https://gradle.org/install/),
and [Git](https://git-scm.com/downloads/). Please check
the [compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html) for Java and Gradle versions.

## Group 8A

NAME: Bruno Rosendo, NR: 201906334, GRADE: 20, CONTRIBUTION: 25%
NAME: Rui Filipe Teixeira Alves, NR: 201905853, GRADE: 20, CONTRIBUTION: 25%
NAME: João Mesquita, NR: 201906682, GRADE: 20, CONTRIBUTION: 25%
NAME: Tomás Vicente, NR: 201904609, GRADE: 20, CONTRIBUTION: 25%

Global Grade: 20

## Summary

This project consists of a compiler for a subset of the Java programming language (referred to as JMM, which stands
for “Java-minus-minus”), going through all the stages of compilation: Lexical Analysis, Syntactic Analysis, Semantic
Analysis, High & Low level Optimization, and Code Generation.

The generated code can then be executed using [Jasmin](http://jasmin.sourceforge.net/).

## Semantic Analysis

This stage is responsible for validating the contents of the Abstract Syntax Tree. The following validations were
implemented:

- Type checking operations, assignments, methods' arguments and return values, array indexing, conditional expressions
  and nested dot expressions. ([*TypeCheckingVisitor*](src/pt/up/fe/comp/visitors/TypeCheckingVisitor.java) class).
- Verifying the existence of invoked functions, referenced variables, custom types. ([*
  ExistenceVisitor*](src/pt/up/fe/comp/visitors/ExistenceVisitor.java) class).
- Whenever the compiler detects a semantic error, it reports it, informing the line and column where it occurred, thanks
  to the AST annotation.

Note that the semantic verification of imported classes is assumed to be correct.

## Code Generation

In order to ease the development of the compiler and to assure its modularity and robustness, the code generation is
divided into multiple stages: AST optimizations, OLLIR generation, register allocation, and Jasmin code generation.

### AST Optimizations

Regarding the AST, the following optimizations were implemented: constant propagation, constant folding, and dead code
elimination (simple and advanced).

#### Constant Propagation and Folding

Before generating OLLIR code, the *ConstantPropagatorVisitor* replaces constant variables with their respective values.

Similarly, the *ConstantFolderVisitor* computes operations with constant values, to eliminate unnecessary instructions.

```java
public int f(int x, int y) {
    int z;
    int w;
    z = 100 / 20;
    // z is propagated inside the if and else blocks
    if (x < y) { w = x + z; }
    else { w = y - z;}
    y = z; // z was not modified, so it can still be propagated
    return w;
}
```

```
.method public f(x.i32, y.i32).i32 {
    z.i32 :=.i32 5.i32;
    if (x.i32 <.bool y.i32) goto ifbody_0;
    w.i32 :=.i32 y.i32 -.i32 5.i32;
    goto endif_0;
    ifbody_0:
    w.i32 :=.i32 x.i32 +.i32 5.i32;
    endif_0:
    y.i32 :=.i32 5.i32;
    ret.i32 w.i32;
}
```

#### Dead Code Elimination (Advanced)

Eliminating dead code can significantly reduce the generated code size and, in some cases, improve execution time by
eliminating dead instructions. For this reason, we removed *ifs/whiles* with a constant condition and instructions that
are executed but aren't used afterwards.

This is done in the *DeadConditionalLoopsVisitor* and *DeadStoreRemoverVisitor* classes.

#### Simplifying Boolean Expressions and Operations

It's advantageous to simplify boolean expressions that can be statically evaluated, which is done in the *
BooleanSimplifierVisitor* class.

For example, the following snippets of code can be simplified (ignoring other optimizations):

```java
int x; int y; boolean b;

x = 1 * y;
x = 0 + y;
x = y * 0;
x = y / 1;
x = y - 0;

b = 1 < 2;
b = 1 < 2 && 4 < 3;
b = (x < y) && !(y < x); // x < y && x >= y
b = (!(x < y) && !(y < x)) && !(!(y < x) && !(x < y)); // x == y && x != y
b = false && (x < y);
b = (x < 10) && true;

if (!(x < y));
if (!(x < y) && !(y < x));
if (!(!(x < y) && !(y < x)));
```

into:

```
x.i32 :=.i32 y.i32;
x.i32 :=.i32 y.i32;
x.i32 :=.i32 0.i32;
x.i32 :=.i32 y.i32;
x.i32 :=.i32 y.i32;

b.bool :=.bool true.bool;
b.bool :=.bool false.bool;
b.bool :=.bool x.i32 <.bool y.i32;
b.bool :=.bool false.bool;
b.bool :=.bool false.bool;
b.bool :=.bool x.i32 <.bool 10.i32;

if (x.i32 >=.bool y.i32) goto ifbody_0;
if (x.i32 ==.bool y.i32) goto ifbody_1;
if (x.i32 !=.bool y.i32) goto ifbody_2;
```

### Ollir Generation

In this stage, the AST is converted to the OLLIR (Optimized Low-Level Intermediate Representation) format, which is done
in the *OllirBuilder* class. The strategy for this stage was to recursively iterate the AST (visitor pattern) for the
code generation and for the optimizations. The *OllirVisitor*, which is responsible for generating OLLIR code, generated
temporary variables whenever they're **needed**.

The optimization done in the OLLIR stage were the following:

#### Do-While statements

There are cases where the compiler can statically detect that the condition of a *while loop* is always true at its
first iteration. In this case, it can be replaced by a do-while and reduce the number of instructions and jumps
executed.

Example of a regular while:

```
if_icmplt whilebody_0
goto endwhile_0
whilebody_0:
    iconst_1
    invokestatic io/println(I)V
    iinc 1 1
    iload_1
    iconst_2
    if_icmplt whilebody_0
endwhile_0:
    return
```

Example of a do-while:

```
whilebody_0:
    iconst_1
    invokestatic io/println(I)V
    iinc 1 1
    iload_1
    iconst_2
    if_icmplt whilebody_0
    return
```

### Jasmin Generation

The final step of this compiler is to generate Jasmin code, which we then use to execute the program, with the Jasmin
library.

The optimization done in the Jasmin stage were the following:

#### Register Allocation

To define the number of allocated registers, we start by checking if the received number of registers is valid:

- if the number is -1, then we use the maximum number of local variables of all methods ([*
  AllocateRegisters*](src/pt/up/fe/comp/registerAllocation/AllocateRegisters.java#L36)).
- if the number is 0, then our code determines the minimum number of registers required, by iterating from 0 to a valid
  number ([*AllocateRegisters*](src/pt/up/fe/comp/registerAllocation/AllocateRegisters.java#L52)).

If the number is a positive number, then we proceed to calculate the registers. We start by making a dataflow analysis,
by:

- Building the successors, in, out, def and use parameters for each method ([*
  DataflowAnalysis*](src/pt/up/fe/comp/registerAllocation/dataflow/DataflowAnalysis.java#L49)).
- Computating the backward Liveness Analysis, which sets up the in and out of each method ([*
  DataflowAnalysis*](src/pt/up/fe/comp/registerAllocation/dataflow/DataflowAnalysis.java#L110)).
- Calculating the live range for each defined variable ([*
  DataflowAnalysis*](src/pt/up/fe/comp/registerAllocation/dataflow/DataflowAnalysis.java#L180)).
- Calculating the conflicts/interference between variables, storing it in a HashMap for each variable. ([*
  DataflowAnalysis*](src/pt/up/fe/comp/registerAllocation/dataflow/DataflowAnalysis.java#L228)).

After that, we construct the [*Interference
Graph*](src/pt/up/fe/comp/registerAllocation/coloring/InterferenceGraph.java#L10) and proceed to color it by:

- Iteratively pushing to the stack the nodes with degree (number of edges) lower than the number of registers ([*
  GraphColoring*](src/pt/up/fe/comp/registerAllocation/coloring/GraphColoring.java#L20)).
- Iterating the stack and assigning an available color (register) to each node ([*
  GraphColoring*](src/pt/up/fe/comp/registerAllocation/coloring/GraphColoring.java#L55)).

If there is no register available, then we report an error with the minimum number of JVM local variables required ([*
AllocateRegisters*](src/pt/up/fe/comp/registerAllocation/AllocateRegisters.java#L105)).

#### Optimized JVM Instructions

While generating Jasmin code, there are often many ways of getting the same result. However, different instructions
perform better in different situations, so it was important to identify them and choose the instructions accordingly:

- Right type of load/store (e.g. aload 1 vs aload_1).
- Right way of loading constants to the stack (iconst_m1, bipush, ldc, etc.).
- Using *iinc* when incrementing a register.
- Choosing the right type of *if* to minimize instructions. (e.g. use *ifeq* when comparing to zero but use *if_icmpne*
  when comparing two values in the stack).
- Other instructions with minor impact.

#### Optimizing conditional statements

In addition to simplifying boolean expressions and statically evaluating branch conditions, it is relevant to optimize
those that can only be evaluated at runtime. For that, four techniques are employed:
[lookupswitch](https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.lookupswitch) statements,
[tableswitch](https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.tableswitch) statements,
replacing intermediate jumps, and negating conditionals.

##### Replacing intermediate jumps

Considering the following Java code:

```java
public int ifElseifElse(int x, int y) {
  int r;
  if (x > y) {r = y - x;}
  else if (x < y) {r = x - y;}
  else {r = 0;}
  return r;
}
```

In order to write this code in JMM, we would need to replace the `else if` block with an `if` statement inside
the `else` block:

```java
public int ifElseifElse(int x, int y) {
  int r;
  if (x < y) {r = 10;}
  else {
    if (y < x) {r = 20;}
    else {r = 0;}
  }
  return r;
}
```

This would correspond to the OLLIR code:

```ollir
.method public ifElseifElse(x.i32, y.i32).i32 {
  if (x.i32 <.bool y.i32) goto ifbody_1;
  if (y.i32 <.bool x.i32) goto ifbody_0;
  r.i32 :=.i32 0.i32;
  goto endif_0;
  ifbody_0: r.i32 :=.i32 y.i32 -.i32 x.i32;
  endif_0: goto endif_1;
  ifbody_1: r.i32 :=.i32 x.i32 -.i32 y.i32;
  endif_1:
  ret.i32 r.i32;
}
```

Looking at the intermediate representation, we can see that when both conditions are false, two jumps are
performed (`goto endif_0` -> `goto endif_1`). The jasmin code would then be:

```j
.method public ifElseifElse(II)I
  # ... if condition
  if_icmplt ifbody_1
  # ... else if condition
  if_icmplt ifbody_0
  # ... else body
  goto endif_0
  ifbody_0: # ... else if body
  endif_0: goto endif_1
  ifbody_1:
  # ... if body
  endif_1:
  
  iload_3
  ireturn
.end method
```

To avoid this inefficiency, when generating the final Jasmin code, goto statements that jump to another goto statement
are replaced with a goto statement that jumps to the final destination:

```j
.method public ifElseifElse(II)I
  # ... if condition
  if_icmplt ifbody_1
  # ... else if condition
  if_icmplt ifbody_0
  # ... else body
  goto endif_1
  ifbody_0: # ... else if body
  endif_0: goto endif_1
  ifbody_1:
  # ... if body
  endif_1:
  
  iload_3
  ireturn
.end method
```

Taking advantage of the new boolean operators introduced (section TODO), the following Java code:

```java
public int Switch(int x, int y) {
  int r;
  r = switch (x) {
    case 1 -> 10;
    case 2 -> 20;
    default -> 0;
  };
  return r;
}
```

written in JMM as:

```java
public int nestedEqualsSwitch(int x) {
  int r;
  if (!(x < 1) && !(1 < x)) {r = 10;}
  else {
    if (!(x < 2) && !(2 < x)) {r = 20;}
    else {r = 0;}
  }
  return r;
}
```

can be rewritten in OLLIR as:

```ollir
.method public nestedEquals(x.i32, y.i32).i32 {
  if (x.i32 ==.bool 1.i32) goto ifbody_1;
  if (x.i32 ==.bool 2.i32) goto ifbody_0;
  r.i32 :=.i32 0.i32;
  goto endif_0;
  ifbody_0:
    r.i32 :=.i32 20.i32;
  endif_0:
  goto endif_1;
  ifbody_1:
    r.i32 :=.i32 10.i32;
  endif_1:
  ret.i32 r.i32;
}
```

Now, we can recognize that nested `if/else` statements comparing the same variable to an integer value actually
correspond to the original `switch` statement.
The JVM offers two ways to implement a `switch` statement: `lookupswitch` and `tableswitch`. These differ in the fact
that a `lookupswitch` will look up the value of the switch variable in a table of values, while a `tableswitch` will
binary search the value of the switch variable in a table of ranges, providing an efficient lookup.

##### Tableswitch statements

When the nested `if/else` statements correspond to a `switch` statement with a contiguous set of values, the generated
code will use a `tableswitch` statement.
Beginning with:

```java
public int continuousSwitchCase(int x) {
  int r;
  if (!(x < 1) && !(1 < x)) {r = 1;}
  else {
   if (!(x < 2) && !(2 < x)) {r = 4;}
   else {
    if (!(x < 3) && !(3 < x)) {r = 9;}
    else {
     if (!(x < 4) && !(4 < x)) {r = 16;}
     else {
      if (!(x < 5) && !(5 < x)) {r = 25;}
      else {
       if (!(x < 6) && !(6 < x)) {r = 36;}
       else {
        if (!(x < 7) && !(7 < x)) {r = 49;}
        else {
         if (!(x < 8) && !(8 < x)) {r = 64;}
         else {r = 0 - 1;}}}}}}}}
  return r;
}
```

And get the Jasmin code:

```j
.method public continuousSwitchCase(I)I
  .limit stack 2
  .limit locals 3

  iload_1
  tableswitch 1 8
    ifbody_7
    ifbody_6
    ifbody_5
    ifbody_4
    ifbody_3
    ifbody_2
    ifbody_1
    ifbody_0
  default: default_switch_0
  
  # Labels for the switch cases...
  
  iload_2
  ireturn
.end method
```

If the `switch` statement is not contiguous but the gaps are not too large, the `tableswitch` statement can still be
used, replacing the missing values with the default branch target:

```java
public int continuousSwitchCaseWithSmallHoles(int x) {
  int r;
  if (!(x < 1) && !(1 < x)) {r = 1;}
  else {
    if (!(x < 3) && !(3 < x)) {r = 9;}
    else {
     if (!(x < 4) && !(4 < x)) {r = 16;}
     else {
      if (!(x < 6) && !(6 < x)) {r = 36;}
      else {
       if (!(x < 7) && !(7 < x)) {r = 49;}
       else {r = 0 - 1;}}}}}
  return r;
}
```

```j
.method public continuousSwitchCaseWithSmallHoles(I)I
.limit stack 2
.limit locals 3
  iload_1
  tableswitch 1 7
    ifbody_4
    default_switch_0
    ifbody_3
    ifbody_2
    default_switch_0
    ifbody_1
    ifbody_0
  default: default_switch_0

  # Labels for the switch cases...

  iload_2
  ireturn
.end method
```

##### Lookupswitch statements

When the set of switch values is too sparse, the `lookupswitch` statement can be used.

```java
 public int sparseSwitchCase(int x) {
  int r;
  if (!(x < 10) && !(10 < x)) {r = 1;}
  else {
   if (!(x < 20) && !(20 < x)) {r = 4;}
   else {
    if (!(x < 30) && !(30 < x)) {r = 9;}
    else {
     if (!(x < 40) && !(40 < x)) {r = 16;}
     else {
      if (!(x < 50) && !(50 < x)) {r = 25;}
      else {
       if (!(x < 60) && !(60 < x)) {r = 36;}
       else {
        if (!(x < 70) && !(70 < x)) {r = 49;}
        else {
         if (!(x < 80) && !(80 < x)) {r = 64;}
         else {r = 0 - 1;}}}}}}}}
  return r;
}
```

```j
.method public sparseSwitchCase(I)I
    .limit stack 2
    .limit locals 3

    iload_1
    lookupswitch
    10: ifbody_7
    20: ifbody_6
    30: ifbody_5
    40: ifbody_4
    50: ifbody_3
    60: ifbody_2
    70: ifbody_1
    80: ifbody_0
    default: default_switch_0

    # Labels for the switch cases...

    iload_2
    ireturn
.end method
```

##### Inverting If conditions

Inverting *if/else* conditions can have an impact on the performance of the program, by reducing the number of jumps
when executing it, especially when the *else* statement is empty.

For example, this is the code for a regular and simple *if* statement:

```
if_icmplt ifbody
    goto endif
ifbody:
    invokestatic ioPlus/printHelloWorld()V
endif:
    return
```

Independently of the condition returning true or false, we always need a jump, to the *ifbody* or to the *endif*. If we
invert the *if* statement, the code becomes:

```
if_icmpge endif
    invokestatic ioPlus/printHelloWorld()V
endif:
    return
```

In this case, we only need to jump if the conditions returns false!

In order to do the optimization accordingly, it's necessary to negate the condition without using more instructions and
to change the order of instructions executed.

## Pros

- The group did everything the specification asked for.
- The code follows a modular organization with intuitive design patterns.
- The compiler has many optimizations, including some which weren't specified by the teachers.
- Friendly error messages, indicating the line and column where the error occurred.

## Cons

- Assuming, in the type checking, that if the type is from an imported class or method then it works without any errors.
- The OLLIR generated code sometimes has repeated types (e.g. temp2.i32.i32). This isn't an issue in execution, thus the
  bug was ignored so the group could focus in more important things.
- Missing indentation in the generated code.

## Project setup

There are three important subfolders inside the main folder. First, inside the subfolder named ``javacc`` you will find
the initial grammar definition. Then, inside the subfolder named ``src`` you will find the entry point of the
application. Finally, the subfolder named ``tutorial`` contains code solutions for each step of the tutorial. JavaCC21
will generate code inside the subfolder ``generated``.

## Compile and Running

To compile and install the program, run ``gradle installDist``. This will compile your classes and create a launcher
script in the folder ``./build/install/comp2022-00/bin``. For convenience, there are two script files, one for
Windows (``comp2022-00.bat``) and another for Linux (``comp2022-00``), in the root folder, that call tihs launcher
script.

After compilation, a series of tests will be automatically executed. The build will stop if any test fails. Whenever you
want to ignore the tests and build the program anyway, you can call Gradle with the flag ``-x test``.

## Test

To test the program, run ``gradle test``. This will execute the build, and run the JUnit tests in the ``test`` folder.
If you want to see output printed during the tests, use the flag ``-i`` (i.e., ``gradle test -i``).
You can also see a test report by opening ``./build/reports/tests/test/index.html``.

## Checkpoint 1

For the first checkpoint the following is required:

1. Convert the provided e-BNF grammar into JavaCC grammar format in a .jj file
2. Resolve grammar conflicts, preferably with lookaheads no greater than 2
3. Include missing information in nodes (i.e. tree annotation). E.g. include the operation type in the operation node.
4. Generate a JSON from the AST

### JavaCC to JSON

To help converting the JavaCC nodes into a JSON format, we included in this project the JmmNode interface, which can be
seen in ``src-lib/pt/up/fe/comp/jmm/ast/JmmNode.java``. The idea is for you to use this interface along with the Node
class that is automatically generated by JavaCC (which can be seen in ``generated``). Then, one can easily convert the
JmmNode into a JSON string by invoking the method JmmNode.toJson().

Please check the JavaCC tutorial to see an example of how the interface can be implemented.

### Reports

We also included in this project the class ``src-lib/pt/up/fe/comp/jmm/report/Report.java``. This class is used to
generate important reports, including error and warning messages, but also can be used to include debugging and logging
information. E.g. When you want to generate an error, create a new Report with the ``Error`` type and provide the stage
in which the error occurred.

### Parser Interface

We have included the interface ``src-lib/pt/up/fe/comp/jmm/pt.up.fe.comp.parser/JmmParser.java``, which you should
implement in a class that has a constructor with no parameters (please check ``src/pt/up/fe/comp/CalculatorParser.java``
for an example). This class will be used to test your pt.up.fe.comp.parser. The interface has a single method, ``parse``
, which receives a String with the code to parse, and returns a JmmParserResult instance. This instance contains the
root node of your AST, as well as a List of Report instances that you collected during parsing.

To configure the name of the class that implements the JmmParser interface, use the file ``config.properties``.

### Compilation Stages

The project is divided in four compilation stages, that you will be developing during the semester. The stages are
Parser, Analysis, Optimization and Backend, and for each of these stages there is a corresponding Java interface that
you will have to implement (e.g. for the Parser stage, you have to implement the interface JmmParser).

### config.properties

The testing framework, which uses the class TestUtils located in ``src-lib/pt/up/fe/comp``, has methods to test each of
the four compilation stages (e.g., ``TestUtils.parse()`` for testing the Parser stage).

In order for the test class to find your implementations for the stages, it uses the file ``config.properties`` that is
in root of your repository. It has four fields, one for each stage (i.e. ``ParserClass``, ``AnalysisClass``
, ``OptimizationClass``, ``BackendClass``), and initially it only has one value, ``pt.up.fe.comp.SimpleParser``,
associated with the first stage.

During the development of your compiler you will update this file in order to setup the classes that implement each of
the compilation stages.
