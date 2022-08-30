package com.circomlib.hash;

import java.math.BigInteger;

/**
 * byte[] operator. Use only BigInteger for the moment : TODO Need change all BigInteger reference
 */
public class ByteArrayOperator {

    private static final BigInteger maxBig = new BigInteger("21888242871839275222246405745257275088548364400416034343698204186575808495617",10);

    public static byte[] mul(byte[] a, byte[] b) {
        BigInteger ba = new BigInteger(a);
        BigInteger bb = new BigInteger(b);
        BigInteger bc = ba.multiply(bb);
        return e(bc);
    }

    public static byte[] square(byte[] a) {
        BigInteger ba = new BigInteger(a);
        BigInteger bc = ba.multiply(ba);
        return e(bc);
    }

    public static byte[] add(byte[] a, byte[] b) {
        BigInteger ba = new BigInteger(a);
        BigInteger bb = new BigInteger(b);
        BigInteger bc = ba.add(bb);
        return e(bc);
    }

    public static byte[] add(byte[] a, byte[] b, byte[] c) {
        BigInteger ba = new BigInteger(a);
        BigInteger bb = new BigInteger(b);
        BigInteger bc = new BigInteger(c);
        BigInteger bd = ba.add(bb).add(bc);
        return e(bd);
    }

    public static byte[] e(byte[] v) {
        BigInteger b = new BigInteger(v);
        if ((v[0] & 128) == 128) {
            byte[] v2 = new byte[v.length+1];
            System.arraycopy(v, 0, v2, 1, v.length);
            b = new BigInteger(v2);
        }
        return e(b);
    }

    private static byte[] e(BigInteger b) {
        if (b.signum() < 0) {
            b = b.multiply(new BigInteger("-1"));
            b = b.mod(maxBig);
            b = maxBig.subtract(b);
        } else {
            b = b.mod(maxBig);
        }
        return b.toByteArray();
    }

    public static String toString(byte[] v, int base) {
        BigInteger b = new BigInteger(e(v));
        return b.toString(base);
    }
}
