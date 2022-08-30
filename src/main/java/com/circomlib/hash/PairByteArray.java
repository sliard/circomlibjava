package com.circomlib.hash;

import java.math.BigInteger;

public class PairByteArray {

    public byte[] l;
    public byte[] r;

    public PairByteArray() {
        this.l = new byte[32];
        this.r = new byte[32];
    }

    public PairByteArray(byte[] l, byte[] r) {
        this.l = l;
        this.r = r;
    }

    boolean isZero() {
        BigInteger bl = new BigInteger(l);
        if (bl.signum() != 0) {
            return false;
        }
        BigInteger br = new BigInteger(r);
        return br.signum() == 0;
    }
}
