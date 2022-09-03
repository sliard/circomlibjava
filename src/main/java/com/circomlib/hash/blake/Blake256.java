package com.circomlib.hash.blake;

import com.circomlib.hash.ByteArrayOperator;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Blake256 extends Blake {

    private static final int[] u256 = new int[] {
            0x243f6a88, 0x85a308d3, 0x13198a2e, 0x03707344,
            0xa4093822, 0x299f31d0, 0x082efa98, 0xec4e6c89,
            0x452821e6, 0x38d01377, 0xbe5466cf, 0x34e90c6c,
            0xc0ac29b7, 0xc97c50dd, 0x3f84d5b5, 0xb5470917
    };

    private int[] vectorH = new int[] {
            0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
            0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
    };


    private int[] s = new int[4];

    private int buflen = 0;

    private boolean nullt;

    private static final byte[] zo = new byte[]{0x01};
    private static final byte[] oo = new byte[]{(byte) 0x81};

    public Blake256() {
        init();
    }

    public void init() {
        vectorH[0] = 0x6a09e667;
        vectorH[1] = 0xbb67ae85;
        vectorH[2] = 0x3c6ef372;
        vectorH[3] = 0xa54ff53a;
        vectorH[4] = 0x510e527f;
        vectorH[5] = 0x9b05688c;
        vectorH[6] = 0x1f83d9ab;
        vectorH[7] = 0x5be0cd19;
        s[0] = s[1] = s[2] = s[3] = 0;

        this.block = new byte[64];

        this.blockOffset = 0;
        this.length = new long[2];

        nullt = false;
    }

    public void compress() {
        int[] v = new int[16];
        int[] m = new int[16];
        int i;

        for(i=0; i<m.length; i++) {
            m[i] = u8to32(block, i*4);
        }

        for(i=0; i<8; i++) {
            v[i] = vectorH[i];
        }

        v[8] = s[0] ^ u256[0];
        v[9] = s[1] ^ u256[1];
        v[10] = s[2] ^ u256[2];
        v[11] = s[3] ^ u256[3];
        v[12] = u256[4];
        v[13] = u256[5];
        v[14] = u256[6];
        v[15] = u256[7];

        if (!nullt) {
            v[12] ^= length[0];
            v[13] ^= length[0];
            v[14] ^= length[1];
            v[15] ^= length[1];
        }

        for(i=0; i<14; i++) {
            /* column step */
            g( v, m, 0,  4,  8, 12,  0, i);
            g( v, m, 1,  5,  9, 13,  2, i);
            g( v, m, 2,  6, 10, 14,  4, i);
            g( v, m, 3,  7, 11, 15,  6, i);
            /* diagonal step */
            g( v, m, 0,  5, 10, 15,  8, i);
            g( v, m, 1,  6, 11, 12, 10, i);
            g( v, m, 2,  7,  8, 13, 12, i);
            g( v, m, 3,  4,  9, 14, 14, i);
        }

        for(i=0; i<16; i++) {
            vectorH[i % 8] ^= v[i];
        }

        for(i=0; i<8 ; i++) {
            vectorH[i] ^= s[i % 4];
        }
    }

    // left shift
    public int rot(int x, int n) {
        return (((x)<<(32-n))|( (x)>>(n)));
    }

    public void g(int[] v, int[] m, int a, int b, int c, int d, int e, int i) {
        v[a] += (m[sigma[i][e]] ^ u256[sigma[i][e+1]]) + v[b];
        v[d] = rot( v[d] ^ v[a],16);
        v[c] += v[d];
        v[b] = rot( v[b] ^ v[c],12);
        v[a] += (m[sigma[i][e+1]] ^ u256[sigma[i][e]])+v[b];
        v[d] = rot( v[d] ^ v[a], 8);
        v[c] += v[d];
        v[b] = rot( v[b] ^ v[c], 7);
    }


    private void padding () {
        long lo = this.length[0] + this.blockOffset * 8L;
        long hi = this.length[1];
        if (lo >= 0x0100000000L) {
            lo -= 0x0100000000L;
            hi += 1;
        }

        byte[] msglen = new byte[8];
        msglen[0] = u32to8((int)(hi&0xFFFFFFFFL))[0];
        msglen[1] = u32to8((int)(hi&0xFFFFFFFFL))[1];
        msglen[2] = u32to8((int)(hi&0xFFFFFFFFL))[2];
        msglen[3] = u32to8((int)(hi&0xFFFFFFFFL))[3];
        msglen[4] = u32to8((int)(lo&0xFFFFFFFFL))[0];
        msglen[5] = u32to8((int)(lo&0xFFFFFFFFL))[1];
        msglen[6] = u32to8((int)(lo&0xFFFFFFFFL))[2];
        msglen[7] = u32to8((int)(lo&0xFFFFFFFFL))[3];

        if (this.blockOffset == 55) {
            this.length[0] -= 8;
            this.update(oo);
        } else {
            if (this.blockOffset < 55) {
                if (this.blockOffset == 0) {
                    this.nullt = true;
                }
                this.length[0] -= (55 - this.blockOffset) * 8L;
                byte[] paddingSlice = Arrays.copyOfRange(padding, 0, 55 - this.blockOffset);
                this.update(paddingSlice);
            } else {
                this.length[0] -= (64 - this.blockOffset) * 8L;
                byte[] paddingSlice = Arrays.copyOfRange(padding, 0, 64 - this.blockOffset);
                this.update(paddingSlice);
                this.length[0] -= 55 * 8L;
                paddingSlice = Arrays.copyOfRange(padding, 1, 1 + 55);
                this.update(paddingSlice);
                this.nullt = true;
            }

            this.update(zo);
            this.length[0] -= 8;
        }

        this.length[0] -= 64;
        this.update(msglen);
    }

    private int u8to32(byte[] v, int index) {
        int result = 0;
        for(int i=0; i<4; i++) {
            result += v[index+i] << (8*(3-i));
        }
        return result;
    }

    private byte[] u32to8(int v) {
        byte[] result = new byte[4];
        result[0] = (byte)((v & 0xFF000000) >> (8*3));
        result[1] = (byte)((v & 0x00FF0000) >> (8*2));
        result[2] = (byte)((v & 0x0000FF00) >> 8);
        result[3] = (byte)(v & 0x000000FF);
        return result;
    }

    private void u32ArrayTo8(int[] source, byte[] out) {
        if (source.length*4 != out.length) {
            throw new IllegalArgumentException("Bad result size");
        }
        for (int i=0; i<source.length; i++) {
            out[i*4] = (byte)((source[i] & 0xFF000000) >> (8*3));
            out[i*4 + 1] = (byte)((source[i] & 0x00FF0000) >> (8*2));
            out[i*4 + 2] = (byte)((source[i] & 0x0000FF00) >> 8);
            out[i*4 + 3] = (byte)(source[i] & 0x000000FF);
        }
    }

    public byte[] digest() {
        this.padding();

        byte[] buffer = new byte[32];
        u32ArrayTo8(vectorH, buffer);
        return buffer;
    }



    public static void main(String[] args) {
        /*
        byte[] message = "Hello world!".getBytes(StandardCharsets.UTF_8);

        Blake256 bb = new Blake256();
        bb.init();
        bb.update(message);

        byte[] res = bb.digest();

        System.out.println(ByteArrayOperator.toHexString(res));
        // "Hello world!" target e0d8a3b73d07feca605c2376f5e54820cf8280af4a195d125ff5eadbf214adf3


         */
        byte[] message2 = new byte[1];

        Blake256 bb2 = new Blake256();
        bb2.init();
        bb2.update(message2);

        byte[] res2 = bb2.digest();

        System.out.println(ByteArrayOperator.toHexString(res2));
    }

}
