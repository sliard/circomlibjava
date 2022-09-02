package com.circomlib.hash;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class PedersenTest {

    @Test
    public void hashTest() {
        Pedersen pedersen = new Pedersen();
        byte[] result = pedersen.hash("Hello".getBytes(StandardCharsets.UTF_8));
        assertEquals("0e90d7d613ab8b5ea7f4f8bc537db6bb0fa2e5e97bbac1c1f609ef9e6a35fd8b", ByteArrayOperator.toHexString(result));
    }

}
