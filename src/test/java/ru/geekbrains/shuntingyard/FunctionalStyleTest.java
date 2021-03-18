package ru.geekbrains.shuntingyard;


import org.junit.Assert;
import org.junit.Test;

public class FunctionalStyleTest {

    @Test
    public void simpleTest() {

        var result = FunctionalStyle.IO.apply(() -> "1+4/2")
                .map(FunctionalStyle::parse)
                .map(FunctionalStyle::toRPN)
                .map(FunctionalStyle::rpnToString)
                .unsafeRun();

        Assert.assertEquals("1 4 2 / + ", result);
    }

    @Test
    public void complexTest() {

        var result = FunctionalStyle.IO.apply(() -> "3+4*2/(1-5)^2^3")
                .map(FunctionalStyle::parse)
                .map(FunctionalStyle::toRPN)
                .map(FunctionalStyle::rpnToString)
                .unsafeRun();

        Assert.assertEquals("3 4 2 * 1 5 - 2 3 ^ ^ / + ", result);
    }

}