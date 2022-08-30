package com.circomlib.hash;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class MimcSpongeTest {

    @Test
    public void hashTest() {
        MimcSponge mimc = new MimcSponge();
        byte[][] input = intArrayToBytesArray(new int[]{1, 2});
        byte[][] result = mimc.multiHash(input);
        assertEquals("2bcea035a1251603f1ceaf73cd4ae89427c47075bb8e3a944039ff1e3d6d2a6f", ByteArrayOperator.toString(result[0], 16));
    }

    @Test
    public void hashTest4() {
        MimcSponge mimc = new MimcSponge();
        byte[][] input = intArrayToBytesArray(new int[]{1, 2, 3, 4});
        byte[][] result = mimc.multiHash(input);
        assertEquals("3e86bdc4eac70bd601473c53d8233b145fe8fd8bf6ef25f0b217a1da305665c", ByteArrayOperator.toString(result[0], 16));
    }

    private byte[][] intArrayToBytesArray(int[] x) {
        byte[][] result = new byte[x.length][];
        for(int i=0; i<x.length; i++) {
            result[i] =  BigInteger.valueOf(x[i]).toByteArray();
        }
        return result;
    }
}
