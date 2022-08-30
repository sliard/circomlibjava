package com.circomlib.hash;

import org.bouncycastle.jcajce.provider.digest.Blake2b;
import org.bouncycastle.jcajce.provider.digest.Blake2s;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;

public class Pedersen {

    private static final String GENPOINT_PREFIX = "PedersenGenerator";
    private static final int WINDOWS_SIZE = 4;
    private static final int N_WINDOWS_PER_SEGMENT = 50;

    private final MessageDigest baseHash;

    private final BabyJub babyJub = new BabyJub();

    private final HashMap<Integer, PairByteArray> bases = new HashMap();

    public Pedersen(String type) {
        if ("blake".equals(type)) {
            this.baseHash = new Blake2s.Blake2s256();
        } else if ("blake2b".equals(type)) {
            this.baseHash = new Blake2b.Blake2b512();
        } else {
            throw new IllegalArgumentException("bad type");
        }
    }
    public Pedersen() {
        this.baseHash = new Blake2s.Blake2s256();

    }

    public byte[] hash(byte[] input) {
        int bitsPerSegment = WINDOWS_SIZE * N_WINDOWS_PER_SEGMENT;
        boolean[] bits = buffer2bits(input);

        int nSegments = Math.floorDiv((bits.length - 1),(bitsPerSegment))+1;

        PairByteArray accP = new PairByteArray(longToBytes(0), longToBytes(1));

        for (int s=0; s<nSegments; s++) {
            int nWindows;
            if (s == nSegments-1) {
                nWindows = Math.floorDiv(((bits.length - (nSegments - 1) * bitsPerSegment) - 1), WINDOWS_SIZE) +1;
            } else {
                nWindows = N_WINDOWS_PER_SEGMENT;
            }
            byte[] escalar = longToBytes(0);
            byte[] exp = longToBytes(1);

            for (int w=0; w < nWindows; w++) {
                int o = s*bitsPerSegment + w*WINDOWS_SIZE;
                byte[] acc = longToBytes(1);

                for (int b=0; (b<(WINDOWS_SIZE-1))&&(o<bits.length) ; b++) {
                    if (bits[o]) {
                        acc = ByteArrayOperator.add(acc, ByteArrayOperator.shiftLeft(longToBytes(1), b) );
                    }
                    o++;
                }
                if (o<bits.length) {
                    if (bits[o]) {
                        acc = ByteArrayOperator.neg(acc);
                    }
                    o++;
                }
                escalar = ByteArrayOperator.add(escalar, ByteArrayOperator.mul(acc, exp));
                exp = ByteArrayOperator.shiftLeft(exp, WINDOWS_SIZE+1);
            }
            if (ByteArrayOperator.isNeg(escalar)) {
                escalar = ByteArrayOperator.add( escalar, babyJub.subOrder);
            }

            accP = babyJub.addPoint(accP, babyJub.mulPointEscalar(this.getBasePoint(s), escalar));
        }
        return babyJub.packPoint(accP);
    }


    private PairByteArray getBasePoint(int pointIdx) {
        if (this.bases.containsKey(pointIdx)) {
            return this.bases.get(pointIdx);
        }
        PairByteArray p= null;
        int tryIdx = 0;
        while (p==null) {
            String s = GENPOINT_PREFIX + "_" + this.padLeftZeros(pointIdx, 32) + "_" + this.padLeftZeros(tryIdx, 32);
            byte[] h = this.baseHash.digest(s.getBytes(StandardCharsets.UTF_8));
            h[31] = (byte)(h[31] & 0xBF);  // Set 255th bit to 0 (256th is the signal and 254th is the last possible bit to 1)
            p = babyJub.unpackPoint(h);
            tryIdx++;
        }

        PairByteArray p8 = babyJub.mulPointEscalar(p, new BigInteger("8",10).toByteArray());

        if (!babyJub.inSubgroup(p8)) {
            throw new Error("Point not in curve");
        }

        this.bases.put(pointIdx,p8);
        return p8;
    }

    private String padLeftZeros(int idx, int n) {
        StringBuilder sidx = new StringBuilder("" + idx);
        while (sidx.length() < n) {
            sidx.insert(0, "0");
        }
        return sidx.toString();
    }

    private boolean[] buffer2bits(byte[] buff) {
        boolean[] result = new boolean[buff.length * 8];
        for (int i=0; i<buff.length; i++) {
            byte b = buff[i];
            result[i*8] = (b & 0x01) > 0;
            result[i*8+1] = ((b & 0x02) >> 1) > 0;
            result[i*8+2] = ((b & 0x04) >> 2) > 0;
            result[i*8+3] = ((b & 0x08) >> 3) > 0;
            result[i*8+4] = ((b & 0x10) >> 4) > 0;
            result[i*8+5] = ((b & 0x20) >> 5) > 0;
            result[i*8+6] = ((b & 0x40) >> 6) > 0;
            result[i*8+7] = ((b & 0x80) >> 7) > 0;
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
