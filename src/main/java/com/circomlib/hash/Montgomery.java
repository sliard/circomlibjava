package com.circomlib.hash;

import java.math.BigInteger;

public class Montgomery {

    // modulus, must be odd
    public final BigInteger m;

    // m bit size
    public final int n;

    // (1<<2n) mod m
    public final BigInteger r2;

    public final int reducerbits;

    public final BigInteger reciprocal;

    public Montgomery(BigInteger m) {
        if (!m.testBit(0)) {
            throw new IllegalArgumentException("m must be odd");
        }

        this.m = m;
        this.n = m.bitLength();

        BigInteger x = BigInteger.ONE.shiftLeft(n);
        x = x.subtract(m);
        r2 = x.multiply(x).mod(m);


        this.reducerbits = ((n / 8)+1)*8;
        BigInteger reducer = BigInteger.ONE.shiftLeft(reducerbits);
        BigInteger mask = reducer.subtract(BigInteger.ONE);
        if (reducer.compareTo(m) < 0 || reducer.gcd(m).intValue() != 1) {
            throw new IllegalArgumentException("Reducer error");
        }

        this.reciprocal = reducer.modInverse(m);
    }

    public BigInteger reduceTest(BigInteger t) {
        BigInteger result = new BigInteger(t.toString());
        return result.shiftLeft(this.reducerbits).mod(this.m);
    }

    public BigInteger expendTest(BigInteger t) {
        BigInteger result = new BigInteger(t.toString());
        return result.multiply(this.reciprocal).mod(this.m);
    }

    public BigInteger expend(BigInteger t) {
        BigInteger result = new BigInteger(t.toString());

        for (int i=0; i<this.n; i++) {
            if (result.testBit(0)) {
                result = result.add(m);
            }
            result = result.shiftLeft(1);
        }
        return result.mod(m);
    }

    public BigInteger reduce(BigInteger t) {
        BigInteger result = new BigInteger(t.toString());

        for (int i=0; i<this.n; i++) {
            if (result.testBit(0)) {
                result = result.add(m);
            }
            result = result.shiftRight(1);
        }
        if (result.compareTo(m) >= 0) {
            result = result.subtract(m);
        }
        return result;
    }

    public BigInteger multiply(BigInteger a, BigInteger b) {
        BigInteger t1 = reduce(a.multiply(this.r2));
        BigInteger t2 = reduce(b.multiply(this.r2));
        BigInteger r = reduce(t1.multiply(t2));
        return reduce(r);
    }

    public BigInteger exp(BigInteger a, BigInteger b) {
        BigInteger prod = reduce(this.r2);
        BigInteger base = reduce(a.multiply(this.r2));

        BigInteger exp = new BigInteger(b.toString());
        while (exp.bitLength() > 0) {
            if (exp.testBit(0)) {
                prod = reduce(prod.multiply(base));
            }
            exp = exp.shiftRight(1);
            base = reduce(base.multiply(base));
        }
        return reduce(prod);
    }
}
