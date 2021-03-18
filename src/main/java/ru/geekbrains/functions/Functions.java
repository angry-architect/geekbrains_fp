package ru.geekbrains.functions;

public class Functions {

    static int x = 0;

    public static void main(String[] args) {


    }

    public static int pureAddFunction(int a, int b) {
        int c = b;
        return a + c;
    }

    public static int addFunctionWithSideEffect(int a, int b) {
        x++;
        return a + b;
    }


}
