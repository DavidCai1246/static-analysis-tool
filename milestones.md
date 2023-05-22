<H3>MILESTONE 1:</H3>

We went into the TA meeting with a really tentative idea. We thinking of using our DSL from project 1, and performing static analysis such as loop checking for null pointer exceptions.
To fufull the other requirements, we thought about building a frontend that displayed the mistakes, while giving the code a rating since our use case was for a beginner programmer.
After talking to our TA, he heavily recommended us to use a established programming language instead to perform static analysis on, so we met after the meeting to come up with some ideas for that.
This is what we came up with. 

Overall direction: we will use option 1 and 2 (use static analysis on real world programming language that will be demo in class - very possibly Java) 

Static analysis idea:
- Infinite loop detection
- Error detection at a specific iteration of the loop
- Null pointer detection
- Array out of bounds detection
- Variable not used detection?

Potential use case:
- New coder using if statements/for loops, give their program a grade based on the potential mistakes
- Give a % chance of how likely it is going to crash when deployed, a fun plugin for users

Our follow up tasks are just to listen to the upcoming lectures on how to do program analysis, so that we can get a better idea of what is possible to do.
We want to meet before next weeks meeting to solidify what we will analyse and our use case. 


<H3>MILESTONE 2:</H3>

We met before the ta meeting and came up with some more detailed ideas: 
- Infinite loop detection
    - Check whether or not the conditional variable for the loop gets closer to terminating condition after 1st or 2nd iteration of the loop 
    - Check whether or not the conditional variable is modified within the loop?
- Array out of bounds detection (?)
    -   See if index call to to array exceeds array length 
- Overflow detection 
- Variable not used detection?
- Null pointer detection
    - Need to tie in with control flow 

We were under the impression we had to implmement a few of these ideas to meet our use case 1 from above. But, at the TA meeting we were told we only needed to do one of these. We decided then to do the null pointer detection with if statement support. 
Our task division now is: 
- Ewan, Wilson looks up library
- Kevin, David make some code examples 
- Someone do the user studies (maybe Spencer, he missed the meeting so we need to catchup)
The first two tasks should be done by our next TA meeting Friday, and the final one should be done on that weekend before Milestone 3. It is impossible to plan further at this point in the project, with the knowledge we have from class. 

 
<H3>MILESTONE 3:</H3>

We have a much better idea of what we want to do with our project now. Since we are focusing on doing static analysis of null-pointer detection on a real world programming language (java), we don't have a frontend planned. That means there is no big mockup for our project, we just have some examples where our static analysis can be used. We want to be able to detect if our code might have a null-pointer exception in for loops and if statements, as well as function calls. Specifically, especially given the results of our first user studies (see below), we will pessimistically over approximate so that we catch as many exceptions as possible, and categorize different types of issues so that devs know certain issues may not actually exist. Here are some code examples:

<H5>Good example 1:</H5>
<pre><code>
class ExampleClass1 {
    public static void main(boolean arg1, int arg1) {
        int printValue = 0;
        
        if(arg1) {
            printValue = foo(arg1);
        }
        else {
            for(int i = 0; i < 4; i++) {
                printValue += foo(arg1);
            }
        }
        
        System.out.println(printValue);
    }
    
    public static int foo(int argument) {
        int retVal = argument * 2;
        return retVal;
    }
}
</code></pre>

<H5>Good example 2:</H5>
<pre><code>
class ExampleClass2 {
    public static void main(boolean arg1, int arg1) {
        int[] array = {10, 20, 30, 40};
        
        if(arg1) {
            for(int i = 0; i < array.length; i++) {
                array[i] = foo(array[i])
            }
        }
        
        System.out.println(array);
    }
    
    public static int foo(int argument) {
        int retVal = argument * 2;
        return retVal;
    }
}

</code></pre>



<H5>Good example 3:</H5>
<pre><code>
class ExampleClass3 {
    public static void main(int arg1, int arg2) {
        
        int printVal = arg1;
        
        for(int i = 0; i < arg2; i++) {
            int checker = Math.random();
            
            if(checker % 2 == 0) {
                printVal = null;
            }
            
            else {
                printVal = arg1;
            }
        }
        
        System.out.println(printVal);
    }
}
</code></pre>


<H5>Bad example 1:</H5>
<pre><code>
class ExampleClass1 {
    public static void main(String[] args) {
        System.out.println("add or subtract two numbers together");
        Calculator calculator = new Calculator();
        String command = "subtract"; // will not call the calculator, causing the error
        if (command.equals("add")) {
            calculator.addToValue(1, 2);
        } else if (command.equals("sub")) {
            calculator.subToValue(2, 1);
        }
        System.out.println(getCalculatorNumber(calculator));
        calculator = new Calculator(); 
    }
    
    public static int getCalculatorNumber(Calculator calculator) {
        return calculator.returnNumber();
    }
}

class Calculator {
    Integer printValue;
    public void addToValue(int one, int two) {
        printValue = new Integer(two + one);
    }
    public void subToValue(int one, int two) {
        printValue = new Integer(one - two);
    }
    public int returnNumber() {
        return printValue.intValue();
    }
    
}
</code></pre>

<H5>Bad example 2:</H5>
<pre><code>
class ExampleClass1 {
    public static void main(String[] args) {
        Integer skip = new Integer(5);
        ExampleClass2 exampleClass2 = new ExampleClass2();
        for (int i = 0; i < 10; i++) {
            
            if (i == skip) {
                exampleClass2 = null;    
            } 
        }
        exampleClass2.printMsg();
    }
}

class ExampleClass2 {
    public void printMsg() {
        System.out.println("");
    }
}

</code></pre>

I'm not sure why the formatting fails for the second class, but our examples are basic programs written by someone new to coding and just learning how to write if, loops, and functions. Our checker would help warn them about potential null-pointer errors before they happen. On the coding side, Ewan and Wilson have setup our project with the framework we are going to use: spoon, so we are on track on that front. Spoon handles the parsing of the given java code file into AST. After that we handle the result in a similar manner to how project 1 was done with antlr.

In our user study, the participants were tasked to review some of our examples and determine if/where a NullPointerException would occur. They performed well with small and simple snippets, but as size and complexity grew, their accuracy decreased. After the task driven portion, they were surveyed on their perceived usefulness of our proposed tool. They believed that a tool such as our proposal would be useful for those complex and large cases, especially if it's more verbose than runtime error logging. When presented with a few options for how the tool could log its findings, they agreed that the most useful method would be to report every issue it detects, but classify certainties as errors and possibilities as warnings.

The plan for this week is for everyone to familiarize themselves with the framework so coding can begin towards the end of the week. Looking further, we have two more milestones before the project is due at the end of the month. Ideally, we want to be able to understand and operate on the parsed AST by the weekend of milestone 4, that way we can finish implementing our static analysis in the following week. The period after milestone 5 will be spent making our video. 


<H3>MILESTONE 4:</H3>

Our progress is going really well. Some clarification of our ideas from last week after some more discussion: 
- our plan is to store a hashmap for each class in our input file, as well as a hashmap for each method within that class
- the hashmaps will contain a mapping of the variable name to an object that tracks if the variable can be null or not
    - to make the hashmaps, we will pessimistically look at if statements (by seeing if the variable is set to null in either branch) and loops (see if variable could be set to null after one iteration)
    - we will also keep track of fields within a class in the class hashmap, to see if they're ever intialized, and if it's possible they're called before they are
    - we will throw warnings for areas where there is a possibility of a null ptr exception, but where finding out for certain would require some dynamic analysis 
- based on the result of that hashmap, we will output error or warning messages to the console
    

Currently, we have the creation of the class hashmap and are working on the method hashmap as well as the if and for loop handling. Our plans for the final user study fall to the last week of the project, when we have more of our implementation done. As for the timeline, a few of our group members will try to complete the if and for loops this weekend, while the others will start next wednesday on what is remaining. The video will be completed in the final week of the project. We are pretty on track for our goals defined in milestone 2, but we will see as we get further into our implementation what happens. 



<H3>MILESTONE 5:</H3>

For our final milestone update, we are continuing to work on the implementation of the project. We wanted to do the control flow implementation last week, but we hit a snag while trying to do it. We had to cover something else first before getting to the control flow:
To best describe it for our framework I will show an example: If we have a line like exampleField = 1 + b;
The line is known as a statement, and the right side of the "=" is known as an expression. We had to implement handling for the expression first, so if b might be null, our method hashmap will be updated. We are currently working on the statement side of the static analysis, so we look through each line and call the expression handling to update our hashmap. This includes if statements and loops, which we are ready to implement now. Our video and user study ideas are pretty standard, user study will be similar in format to our first one, and the video will just be a simple demo of our project, with some talk about our implementation methods and process. 


I think we have covered everything we wanted to cover from milestone 2/3 with these checks, as they are pretty comprehensive. We are still working on the final error log printout that the user sees, but we are confident it will be good for our use case, which is a new programmer. Our plan for the remaining few days are: 
- Fri/sat/sun, finish error printout and for loops, and make progress on the if loops
- since the if loops are the hardest, we will finish that by monday
- Mon/Tues will be our user study and video 

UPDATE for our user study:
So we did a new user study on two different users. Like in the first user study, we gave them a pieces of code and asked them to find any null-pointer exceptions. This time we specifically chose users that matched our use case: more novice programmers. Their accuracy in finding the null-pointer exceptions was measured, and we saw they were able to identify just over half of the null pointer exceptions. We then asked them to run our program on the example code, and recorded their feedback and suggestions about the tool. Both particpants were surprised at the amount of null-pointer exceptions that were actually in the code, and both were impressed by how accurately our program could detect it. As they had encounted many null-pointer exceptions in their coding experiences, both participants stated that this tool would be very useful to them. They particularly liked the warning section, as it could be a learning experience as they try to get to the reason of the warning. One suggestion was to clarify the logging by making the language simpler and adding a line to say which class the errors belonged to. Overall the experience was very positive for our users. 
