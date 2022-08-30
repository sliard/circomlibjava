package com.circomlib.hash;

import org.bouncycastle.jcajce.provider.digest.Keccak;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class MimcSponge {

    private final byte[][] constantBase;
    private final int nRounds;

    public MimcSponge(String seed, int nRounds) {
        this.constantBase = getConstants(seed, nRounds);
        this.nRounds = nRounds;
    }
    public MimcSponge(byte[][] base) {
        this.constantBase = base;
        this.nRounds = constantBase.length;
    }
    public MimcSponge() {
        this.constantBase = getConstants("mimcsponge", 220);
        this.nRounds = 220;
    }

    private byte[][] getConstants(String seed, int nRounds) {
        Keccak.DigestKeccak kecc = new Keccak.Digest256();
        byte[][] result = new byte[nRounds][32];
        byte[] c = kecc.digest(seed.getBytes(StandardCharsets.UTF_8));

        for(int i=1; i<nRounds; i++) {
            c = kecc.digest(c);
            result[i] = ByteArrayOperator.e(c);
        }
        result[0] = longToBytes(0);
        result[nRounds - 1] = longToBytes(0);

        System.out.println("const : " + ByteArrayOperator.toString(result[0], 10));
        System.out.println("const : " + ByteArrayOperator.toString(result[1], 10));
        System.out.println("const : " + ByteArrayOperator.toString(result[2], 10));

        return result;
    }

    public PairHash hash(long l, long r) {
        return hash(longToBytes(l), longToBytes(r));
    }

    public PairHash hash(long l, long r, long k) {
        return hash(longToBytes(l), longToBytes(r), k);
    }

    public PairHash hash(byte[] l, byte[] r) {
        return hash(l, r, 0);
    }

    public PairHash hash(byte[] l, byte[] r, long k) {

        byte[] xl = l.clone();
        byte[] xr = r.clone();
        byte[] xk = longToBytes(k);

        for(int i=0; i<nRounds; i++) {
            byte[] c = constantBase[i];
            byte[] t = (i==0) ? ByteArrayOperator.add(xl, xk) : ByteArrayOperator.add(xl, xk, c);
            byte[] t2 = ByteArrayOperator.square(t);
            byte[] t4 = ByteArrayOperator.square(t2);
            byte[] t5 = ByteArrayOperator.mul(t4,t);
            byte[] xrTmp = xr.clone();
            if(i < nRounds - 1) {
                xr = xl;
                xl = ByteArrayOperator.add(xrTmp, t5);
            } else {
                xr = ByteArrayOperator.add(xrTmp, t5);
            }
        }
        return new PairHash(xl, xr);
    }

    public byte[][] multiHash(byte[][] input) {
        return multiHash(input, 0, 1);
    }


    public byte[][] multiHash(byte[][] input, int key, int numOutputs) {
        byte[][] result = new byte[numOutputs][];

        byte[] r = longToBytes(0);
        byte[] c = longToBytes(0);

        for (byte[] bytes : input) {
            r = ByteArrayOperator.add(r, bytes);
            PairHash s = hash(r, c, key);
            r = s.l;
            c = s.r;
        }
        result[0] = r;
        for (int i=1; i < numOutputs; i++) {
            PairHash s = hash(r, c, key);
            r = s.l;
            c = s.r;
            result[i] = r;
        }
        return result;
    }

    private byte[] longToBytes(long x) {
        BigInteger ba = new BigInteger(String.valueOf(x));
        byte[] val = ba.toByteArray();
        byte[] result = new byte[32];
        System.arraycopy(val, 0, result, 32 - val.length, val.length);
        return result;
    }
}
