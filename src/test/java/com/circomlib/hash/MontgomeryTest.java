package com.circomlib.hash;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class MontgomeryTest {

    @Test
    public void simpleMultiply() {
        BigInteger bp = new BigInteger("13");
        Montgomery montgomery = new Montgomery(bp);
        BigInteger ba = new BigInteger("5");
        BigInteger bb = new BigInteger("6");
        assertEquals("4",montgomery.multiply(ba,bb).toString(10));
    }

    @Test
    public void simpleExp() {
        BigInteger bp = new BigInteger("13");
        Montgomery montgomery = new Montgomery(bp);
        BigInteger ba = new BigInteger("5");
        BigInteger bb = new BigInteger("6");
        assertEquals("12",montgomery.exp(ba, bb).toString(10));
    }

    @Test
    public void checkExp() {
        BigInteger bp = new BigInteger("524287");
        Montgomery montgomery = new Montgomery(bp);
        BigInteger ba = new BigInteger("777");
        BigInteger bb = new BigInteger("105");
        assertEquals("296633",montgomery.exp(ba, bb).toString(10));
    }

    @Test
    public void complexeExp() {
        BigInteger bp = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819949");
        Montgomery montgomery = new Montgomery(bp);
        BigInteger ba = new BigInteger("57896044618658097711785492504343953926634992332820281301830804312103976049700");

        System.out.println(ba.toString(16));
        System.out.println(montgomery.reduce(ba).toString(16));
        System.out.println(montgomery.expend(montgomery.reduce(ba)).toString(16));

        BigInteger bb = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003955491078125");
        assertEquals("40504055762004792620159537441437949886475081163592261781667958256380085618313",montgomery.exp(ba, bb).toString(10));
    }


    @Test
    public void expendReduce() {
        BigInteger bp = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819949");
        Montgomery montgomery = new Montgomery(bp);
        BigInteger ba = new BigInteger("57896044618658097711785492504343953926634992332820281301830804312103976049700");
        assertEquals("57896044618658097711785492504343953926634992332820281301830804312103976049700",montgomery.expend(montgomery.reduce(ba)).toString(10));
    }

    @Test
    public void reduceExpend() {
        BigInteger bp = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819949");
        Montgomery montgomery = new Montgomery(bp);
        BigInteger ba = new BigInteger("57896044618658097711785492504343953926634992332820281301830804312103976049700");
        assertEquals("57896044618658097711785492504343953926634992332820281301830804312103976049700",montgomery.reduce(montgomery.expend(ba)).toString(10));
    }

}
