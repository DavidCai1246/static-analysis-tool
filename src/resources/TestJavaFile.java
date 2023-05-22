package resources;

import java.util.Random;

class ExampleClass3 {
    public static void main(int arg1, int arg2) {
        Integer printVal = arg1;
        Random random = new Random();

        for(int i = 0; i < arg2; i++) {
            int checker = random.nextInt();

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

