package com.circomlib.hash;

import java.math.BigInteger;

/**
 * byte[] operator. Use only BigInteger for the moment : TODO Need change all BigInteger reference
 */
public class ByteArrayOperator {

    public static final byte[] ZERO = new byte[32];
    public static final byte[] ONE = new BigInteger("1",10).toByteArray();

    public static final BigInteger maxBig = new BigInteger("21888242871839275222246405745257275088548364400416034343698204186575808495617",10);

    public static byte[] mul(byte[] a, byte[] b) {
        BigInteger ba = new BigInteger(a);
        BigInteger bb = new BigInteger(b);
        BigInteger bc = ba.multiply(bb);
        return e(bc);
    }

    public static byte[] div(byte[] a, byte[] b) {
        BigInteger ba = new BigInteger(a);
        BigInteger bb = new BigInteger(b);
        BigInteger bc = ba.multiply(bb.modInverse(maxBig));
        return e(bc);
    }

    public static byte[] square(byte[] a) {
        BigInteger ba = new BigInteger(a);
        BigInteger bc = ba.multiply(ba);
        return e(bc);
    }

    public static byte[] sqrt(byte[] a) {
        BigInteger ba = new BigInteger(a);
        BigInteger bc = ba.sqrt();
        return e(bc);
    }

    public static byte[] add(byte[] a, byte[] b) {
        BigInteger ba = new BigInteger(a);
        BigInteger bb = new BigInteger(b);
        BigInteger bc = ba.add(bb);
        return e(bc);
    }

    public static byte[] sub(byte[] a, byte[] b) {
        BigInteger ba = new BigInteger(a);
        BigInteger bb = new BigInteger(b);
        BigInteger bc = ba.subtract(bb);
        return e(bc);
    }

    public static byte[] add(byte[] a, byte[] b, byte[] c) {
        BigInteger ba = new BigInteger(a);
        BigInteger bb = new BigInteger(b);
        BigInteger bc = new BigInteger(c);
        BigInteger bd = ba.add(bb).add(bc);
        return e(bd);
    }

    public static byte[] e(long v) {
        BigInteger b = new BigInteger(""+v);
        return e(b);
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

    // Pases a buffer with Little Endian Representation
    public static byte[] fromRprLE(byte[] v, int offset) {
        byte[] result = new byte[v.length];
        for(int i=0; i<v.length; i++) {
            result[i] = v[v.length-1-i];
        }
        return result;
    }

    // Returns a buffer with Little Endian Representation
    public static byte[] toRprLE(byte[] v, int offset) {
        BigInteger b = new BigInteger(v);
        BigInteger r = new BigInteger(ONE).shiftLeft(4*64);
        BigInteger rInv = r.modInverse(maxBig);
        return e(b.multiply(rInv));
    }


    public static byte[] e(BigInteger b) {
        if (b.signum() < 0) {
            b = b.multiply(new BigInteger("-1"));
            b = b.mod(maxBig);
            b = maxBig.subtract(b);
        } else {
            b = b.mod(maxBig);
        }
        return b.toByteArray();
    }

    public static byte[] exp(byte[] a, byte[] b) {
        BigInteger ba = new BigInteger(a);
        BigInteger bb = new BigInteger(b);
        return e(ba.modPow(bb, maxBig));
    }


    public static byte[] shiftLeft(byte[] b1, int range) {
        BigInteger b = new BigInteger(b1);
        return e(b.shiftLeft(range).toByteArray());
    }

    public static byte[] shiftRight(byte[] b1, int range) {
        BigInteger b = new BigInteger(b1);
        return e(b.shiftRight(range).toByteArray());
    }

    public static byte[] neg(byte[] b1) {
        BigInteger b = new BigInteger(b1);
        return b.negate().toByteArray();
    }

    public static boolean isNeg(byte[] b1) {
        BigInteger b = new BigInteger(b1);
        return b.signum() < 0;
    }

    public static boolean isZero(byte[] b1) {
        BigInteger b = new BigInteger(b1);
        return b.signum() == 0;
    }

    public static boolean eq(byte[] a, byte[] b) {
        BigInteger ba = new BigInteger(a);
        BigInteger bb = new BigInteger(b);
        return ba.equals(bb);
    }

    public static boolean gt(byte[] a, byte[] b) {
        BigInteger ba = new BigInteger(a);
        BigInteger bb = new BigInteger(b);
        return ba.compareTo(bb) > 0;
    }

    public static boolean isOdd(byte[] b1) {
        return (b1[0] & 0x01) == 1;
    }


    public static String toString(byte[] v, int base) {
        BigInteger b = new BigInteger(e(v));
        return b.toString(base);
    }
}
