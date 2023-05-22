# Project2Group21

# Setting up/running the project
1. Go to project structure setting -> libraries in Intellij and then add spoon jar (located at libraries/spoon-core-10.3.0-beta-2-jar-with-dependencies.jar) to the list of libraries
2. We will perform static analysis for all files under src/resources. Feel free to modify any file under src/resources (we recommend InputJavaFile.java) for testing, or add your own file to the resources folder. Note: we support class hierchy so usage of super class is allowed as seen from existing input source code. 
3. Run main. On the terminal output, there will be a brief ERROR message beginning with "SLF4J..." -> THIS IS NORMAL. Below that are the results of the NullPointerException check.
4. Optional: to see the generated AST via swing, go to project home dir in cmd and run " java -cp libraries/spoon-core-10.3.0-beta-2-jar-with-dependencies.jar spoon.Launcher  -i src/resources/InputJavaFile.java --gui "
   (if you change the path to the InputJavaFile this command will stop working and don't include "" in the command) 

# How the tool works
* The tool does static analysis on an input source code file without compiler errors, and will print an error message for every variable that is confirmed to cause a NullPointer Exception, and a warning for possible NullPointer Exceptions. The log will appear in the form: Error/Warning found in variable: __ in line __ , see line(s) to see the cause of this error/warning.
* Our static analysis is overly pessismistic. If we have multiple control flow branches, even if only one of the branches set a field to null, we will assume that field can be null once the control flow branches end.
* Our tool analyses each class and method individually, as our goal was to provide information about all possible executions of a given method.  
* The tool supports:
   * Local variable and field declaration
      * int x;
   * Variable assignment to any expression (except Annotation, AnnotationFieldAccess and SuperAccess), as detailed on https://spoon.gforge.inria.fr/code_elements.html
      * x = 1;
      * x = 2 + 3;
      * x = null;
      * x = true ? 1 : 2;
   * Usage of parameters (their values are treated as unknown)
   * if/else if/else statements (which can be nested)
   * for, while, for each, do while loops (which can be nested)
      * note: for loops, we will keep looping until we can be certain the variables' null state (our belief about the variable's nullity) no longer changes 
   * Objects including arrays (Member values are assumed unknown unless assigned in the code being analysed)
   * method invocations (Return values are assumed unknown)
     * someObject.someMethod()
   * break, continue, throw and return
* Example
   * int x = 1; 
   * x = null;
   * int y = x + 1; -> our code will throw null pointer error 

# Overview of project architecture
* spoon is the library we use to parse java file into AST 
* under src file: ClassProcessor is kind of like our visitor in class. Main is where we do all the parsing and call the visitor. 
* under src/resources: all files that will be parsed into AST by spoon 
* Note: there are other details I left out to keep this guide clean. If you have other questions, feel free to check here for the documentation for spoon: https://spoon.gforge.inria.fr/index.html + https://spoon.gforge.inria.fr/mvnsites/spoon-core/apidocs/spoon/reflect/code/package-summary.html 
