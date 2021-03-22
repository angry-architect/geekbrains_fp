package ru.geekbrains.shuntingyard;

import org.junit.Assert;
import org.junit.Test;
import ru.geekbrains.monads.IO;


public class ShuntingYardFPTest {
    @Test
    public void simpleTest() {

        var result = IO.apply(() -> "1+4/2")
                .map(ShuntingYardFP::parse)
                .map(ShuntingYardFP::toRPN)
                .map(ShuntingYardFP::rpnToString)
                .unsafeRun();

        Assert.assertEquals("1 4 2 / + ", result);
    }

    @Test
    public void complexTest() {

        var result = IO.apply(() -> "3+4*2/(1-5)^2^3")
                .map(ShuntingYardFP::parse)
                .map(ShuntingYardFP::toRPN)
                .map(ShuntingYardFP::rpnToString)
                .unsafeRun();

        Assert.assertEquals("3 4 2 * 1 5 - 2 3 ^ ^ / + ", result);
    }
}