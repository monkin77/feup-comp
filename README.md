# Compilers Project

For this project, you need to install [Java](https://jdk.java.net/), [Gradle](https://gradle.org/install/), and [Git](https://git-scm.com/downloads/). Please check the [compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html) for Java and Gradle versions.

## Group 8A

NAME: Bruno Rosendo, NR: 201906334, GRADE: 20, CONTRIBUTION: 25%

NAME: Rui Filipe Teixeira Alves, NR: 201905853, GRADE: 20, CONTRIBUTION: 25%

NAME: João Mesquita, NR: 201906682, GRADE: 20, CONTRIBUTION: 25%

NAME: Tomás Vicente, NR: 201904609, GRADE: 20, CONTRIBUTION: 25%

Global Grade: 20

## Summary

This project consists of a compiler for a subset of the Java programming language, referred to as “jmm”, which stands for “Java-minus-minus”, going through all the stages of compilation: Lexical analysis, Syntatic analysis, Semantic analysis, High and Low level optimization and Code generation.

The generated code can then be executed using [Jasmin](http://jasmin.sourceforge.net/).


## Semantic Analysis


This stage is responsible for validating the contents of the Abstract Syntax Tree. The following validations were implemented:

- Type checking operations, assignments, method's arguments and return values, array indexing, conditional expressions and nested dot expressions. ([*TypeCheckingVisitor*](https://git.fe.up.pt/compilers2022/comp2022-8a/-/blob/master/src/pt/up/fe/comp/visitors/TypeCheckingVisitor.java) class).
- Verifying the existence of invoked functions, referenced variables, custom types. ([*ExistenceVisitor*](https://git.fe.up.pt/compilers2022/comp2022-8a/-/blob/master/src/pt/up/fe/comp/visitors/ExistenceVisitor.java) class).
- Whenever the compiler detects a semantic error, it reports it, informing the line and column where it occurred, thanks to the AST annotation.

Note that the semantic verification of imported classes is assumed to be correct.

## Code Generation

In order to ease the development of the compiler and to assure its modularity and robustness, the code generation is divided into the following stages:

### Ollir Generation

In this stage, the AST is converted to the OLLIR (Optimized Low-Level Intermediate Representation) format, which is done in the *OllirBuilder* class. The strategy for this stage was to recursively iterate the AST (visitor pattern) for the code generation and for the optimizations. The *OllirVisitor*, which is responsible for generating OLLIR code, generated temporary variables whenever they're **needed**.

The optimization done in the OLLIR stage were the following:

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

Eliminating dead code can significantly reduce the generated code size and, in some cases, improve execution time by eliminating dead instructions. For this reason, we removed *ifs/whiles* with a constant condition and instructions that are executed but aren't used afterwards.

This is done in the *DeadConditionalLoopsVisitor* and *DeadStoreRemoverVisitor* classes.

#### Simplifying Boolean Expressions and Operations

It's advantageous to simplify boolean expressions that can be statically evaluated, which is done in the *BooleanSimplifierVisitor* class.

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

#### Do-While statements

There are cases where the compiler can statically detect that the condition of a *while loop* is always true at its first iteration. In this case, it can be replaced by a do-while and reduce the number of instructions and jumps executed.

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

The final step of this compiler is to generate Jasmin code, which we then use to execute the program, with the Jasmin library.

The optimization done in the Jasmin stage were the following:

#### Register Allocation

To define the number of allocated registers, we start by checking if the received number of registers is valid:

- if the number is -1, then we use the maximum number of local variables of all methods ([*AllocateRegisters*](https://git.fe.up.pt/compilers2022/comp2022-8a/-/blob/master/src/pt/up/fe/comp/registerAllocation/AllocateRegisters.java#L36)).
- if the number is 0, then our code determines the minimum number of registers required, by iterating from 0 to a valid number ([*AllocateRegisters*](https://git.fe.up.pt/compilers2022/comp2022-8a/-/blob/master/src/pt/up/fe/comp/registerAllocation/AllocateRegisters.java#L52)).

If the number is a positive number, then we proceed to calculate the registers. We start by making a dataflow analysis, by:

- Building the successors, in, out, def and use parameters for each method ([*DataflowAnalysis*](https://git.fe.up.pt/compilers2022/comp2022-8a/-/blob/master/src/pt/up/fe/comp/registerAllocation/dataflow/DataflowAnalysis.java#L49)).
- Computating the backward Liveness Analysis, which sets up the in and out of each method ([*DataflowAnalysis*](https://git.fe.up.pt/compilers2022/comp2022-8a/-/blob/master/src/pt/up/fe/comp/registerAllocation/dataflow/DataflowAnalysis.java#L110)).
- Calculating the live range for each defined variable ([*DataflowAnalysis*](https://git.fe.up.pt/compilers2022/comp2022-8a/-/blob/master/src/pt/up/fe/comp/registerAllocation/dataflow/DataflowAnalysis.java#L180)).
- Calculating the conflicts/interference between variables, storing it in a HashMap for each variable. ([*DataflowAnalysis*](https://git.fe.up.pt/compilers2022/comp2022-8a/-/blob/master/src/pt/up/fe/comp/registerAllocation/dataflow/DataflowAnalysis.java#L228)).

After that, we construct the [*Interference Graph*](https://git.fe.up.pt/compilers2022/comp2022-8a/-/blob/master/src/pt/up/fe/comp/registerAllocation/coloring/InterferenceGraph.java#L10) and proceed to color it by:

- Iteratively pushing to the stack the nodes with degree (number of edges) lower than the number of registers ([*GraphColoring*](https://git.fe.up.pt/compilers2022/comp2022-8a/-/blob/master/src/pt/up/fe/comp/registerAllocation/coloring/GraphColoring.java#L20)).
- Iterating the stack and assigning an available color (register) to each node ([*GraphColoring*](https://git.fe.up.pt/compilers2022/comp2022-8a/-/blob/master/src/pt/up/fe/comp/registerAllocation/coloring/GraphColoring.java#L55)).

If there is no register available, then we report an error with the minimum number of JVM local variables required ([*AllocateRegisters*](https://git.fe.up.pt/compilers2022/comp2022-8a/-/blob/master/src/pt/up/fe/comp/registerAllocation/AllocateRegisters.java#L105)).

#### Optimized JVM Instructions

While generating Jasmin code, there are often many ways of getting the same result. However, different instructions perform better in different situations, so it was important to identify them and choose the instructions accordingly:

- Right type of load/store (e.g. aload 1 vs aload_1).
- Right way of loading constants to the stack (iconst_m1, bipush, ldc, etc.).
- Using *iinc* when incrementing a register.
- Choosing the right type of *if* to minimize instructions. (e.g. use *ifeq* when comparing to zero but use *if_icmpne* when comparing two values in the stack).
- Other instructions with minor impact.

#### Switch-case Instructions

In the cases where we have multiple if statements comparing to integer literals, it becomes more efficient to use [JVM's switch-cases](https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.lookupswitch). This is done in the *attemptSwitchReplacement* function, in the *MethodsBuilder* class.

Below, you can find an example of this type of instructions:

```
lookupswitch
1: body_0
2: body_1
3: body_2
4: body_3
5: body_4
default: default_switch_0

default_switch_0:
    iconst_m1
    invokestatic io/println(I)V
    goto end_switch
body_0:
    bipush 25
    invokestatic io/println(I)V
    goto end_switch
body_1:
    bipush 16   
    invokestatic io/println(I)V
    goto end_switch
body_2:
    bipush 9
    invokestatic io/println(I)V
    goto end_switch
body_3:
    iconst_4
    invokestatic io/println(I)V
    goto end_switch
body_4:
    iconst_1
    invokestatic io/println(I)V
end_switch:
    return
```

#### Inverting If conditions

Inverting *if/else* conditions can have an impact on the performance of the program, by reducing the number of jumps when executing it, especially when the *else* statement is empty.

For example, this is the code for a regular and simple *if* statement:
```
if_icmplt ifbody
    goto endif
ifbody:
    invokestatic ioPlus/printHelloWorld()V
endif:
    return
```

Independently of the condition returning true or false, we always need a jump, to the *ifbody* or to the *endif*. If we invert the *if* statement, the code becomes:

```
if_icmpge endif
    invokestatic ioPlus/printHelloWorld()V
endif:
    return
```

In this case, we only need to jump if the conditions returns false!

In order to do the optimization accordingly, it's necessary to negate the condition without using more instructions and to change the order of instructions executed.

## Pros

- The group did everything the specification asked for.
- The code follows a modular organization with intuitive design patterns.
- The compiler has many optimizations, including some which weren't specified by the teachers.
- Friendly error messages, indicating the line and column where the error occurred.

## Cons

- Assuming, in the type checking, that if the type is from an imported class or method then it works without any errors.
- The OLLIR generated code sometimes has repeated types (e.g. temp2.i32.i32). This isn't an issue in execution, thus the bug was ignored so the group could focus in more important things.
- Missing identation in the generated code.

## Project setup

There are three important subfolders inside the main folder. First, inside the subfolder named ``javacc`` you will find the initial grammar definition. Then, inside the subfolder named ``src`` you will find the entry point of the application. Finally, the subfolder named ``tutorial`` contains code solutions for each step of the tutorial. JavaCC21 will generate code inside the subfolder ``generated``.

## Compile and Running

To compile and install the program, run ``gradle installDist``. This will compile your classes and create a launcher script in the folder ``./build/install/comp2022-00/bin``. For convenience, there are two script files, one for Windows (``comp2022-00.bat``) and another for Linux (``comp2022-00``), in the root folder, that call tihs launcher script.

After compilation, a series of tests will be automatically executed. The build will stop if any test fails. Whenever you want to ignore the tests and build the program anyway, you can call Gradle with the flag ``-x test``.

## Test

To test the program, run ``gradle test``. This will execute the build, and run the JUnit tests in the ``test`` folder. If you want to see output printed during the tests, use the flag ``-i`` (i.e., ``gradle test -i``).
You can also see a test report by opening ``./build/reports/tests/test/index.html``.

## Checkpoint 1
For the first checkpoint the following is required:

1. Convert the provided e-BNF grammar into JavaCC grammar format in a .jj file
2. Resolve grammar conflicts, preferably with lookaheads no greater than 2
3. Include missing information in nodes (i.e. tree annotation). E.g. include the operation type in the operation node.
4. Generate a JSON from the AST

### JavaCC to JSON
To help converting the JavaCC nodes into a JSON format, we included in this project the JmmNode interface, which can be seen in ``src-lib/pt/up/fe/comp/jmm/ast/JmmNode.java``. The idea is for you to use this interface along with the Node class that is automatically generated by JavaCC (which can be seen in ``generated``). Then, one can easily convert the JmmNode into a JSON string by invoking the method JmmNode.toJson().

Please check the JavaCC tutorial to see an example of how the interface can be implemented.

### Reports
We also included in this project the class ``src-lib/pt/up/fe/comp/jmm/report/Report.java``. This class is used to generate important reports, including error and warning messages, but also can be used to include debugging and logging information. E.g. When you want to generate an error, create a new Report with the ``Error`` type and provide the stage in which the error occurred.


### Parser Interface

We have included the interface ``src-lib/pt/up/fe/comp/jmm/pt.up.fe.comp.parser/JmmParser.java``, which you should implement in a class that has a constructor with no parameters (please check ``src/pt/up/fe/comp/CalculatorParser.java`` for an example). This class will be used to test your pt.up.fe.comp.parser. The interface has a single method, ``parse``, which receives a String with the code to parse, and returns a JmmParserResult instance. This instance contains the root node of your AST, as well as a List of Report instances that you collected during parsing.

To configure the name of the class that implements the JmmParser interface, use the file ``config.properties``.

### Compilation Stages

The project is divided in four compilation stages, that you will be developing during the semester. The stages are Parser, Analysis, Optimization and Backend, and for each of these stages there is a corresponding Java interface that you will have to implement (e.g. for the Parser stage, you have to implement the interface JmmParser).


### config.properties

The testing framework, which uses the class TestUtils located in ``src-lib/pt/up/fe/comp``, has methods to test each of the four compilation stages (e.g., ``TestUtils.parse()`` for testing the Parser stage).

In order for the test class to find your implementations for the stages, it uses the file ``config.properties`` that is in root of your repository. It has four fields, one for each stage (i.e. ``ParserClass``, ``AnalysisClass``, ``OptimizationClass``, ``BackendClass``), and initially it only has one value, ``pt.up.fe.comp.SimpleParser``, associated with the first stage.

During the development of your compiler you will update this file in order to setup the classes that implement each of the compilation stages.
