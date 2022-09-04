package com.circomlib.provider.digest;

import com.circomlib.hash.ByteArrayOperator;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class Blake256Test {

    @Test
    public void hashHello() {
        byte[] message = "Hello world!".getBytes(StandardCharsets.UTF_8);
        Blake256 bb = new Blake256();
        assertEquals("e0d8a3b73d07feca605c2376f5e54820cf8280af4a195d125ff5eadbf214adf3", ByteArrayOperator.toHexString(bb.digest(message)));
    }

    @Test
    public void hashZero() {
        byte[] message = new byte[1];
        Blake256 bb = new Blake256();
        assertEquals("0ce8d4ef4dd7cd8d62dfded9d4edb0a774ae6a41929a74da23109e8f11139c87", ByteArrayOperator.toHexString(bb.digest(message)));
    }

    @Test
    public void hashZeroBig() {
        byte[] message = new byte[72];
        Blake256 bb = new Blake256();
        assertEquals("d419bad32d504fb7d44d460c42c5593fe544fa4c135dec31e21bd9abdcc22d41", ByteArrayOperator.toHexString(bb.digest(message)));
    }

}
