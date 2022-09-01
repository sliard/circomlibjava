package com.circomlib.hash;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class ByteArrayOperatorTest {

    @Test
    public void sqrtTest() {
        byte[] res3 = ByteArrayOperator.sqrt(new BigInteger("24274985305168ce05999431bdaa51e5210bb63f7c09482df6436771bfed2a26", 16).toByteArray());
        assertEquals("916918a0fe026931c8f7116da5b9a7d8a4e9267bf4f0754d906a297511857d5", ByteArrayOperator.toString(res3));
    }
}
