package com.circomlib.crypto.digests;

import org.bouncycastle.crypto.ExtendedDigest;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * BLAKE is a cryptographic hash function based on Daniel J. Bernstein's
 * ChaCha stream cipher, but a permuted copy of the input block, XORed
 * with round constants, is added before each ChaCha round
 */
public class BlakeDigest implements ExtendedDigest {

    // Message word permutations:
    protected static byte[][] sigma = new byte[][]{
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15},
            {14, 10, 4, 8, 9, 15, 13, 6, 1, 12, 0, 2, 11, 7, 5, 3},
            {11, 8, 12, 0, 5, 2, 15, 13, 10, 14, 3, 6, 7, 1, 9, 4},
            {7, 9, 3, 1, 13, 12, 11, 14, 2, 6, 5, 10, 4, 0, 15, 8},
            {9, 0, 5, 7, 2, 4, 10, 15, 14, 1, 11, 12, 6, 8, 3, 13},
            {2, 12, 6, 10, 0, 11, 8, 3, 4, 13, 7, 5, 15, 14, 1, 9},
            {12, 5, 1, 15, 14, 13, 4, 10, 0, 7, 6, 3, 9, 2, 8, 11},
            {13, 11, 7, 14, 12, 1, 3, 9, 5, 0, 15, 4, 8, 6, 2, 10},
            {6, 15, 14, 9, 11, 3, 0, 8, 12, 2, 13, 7, 1, 4, 10, 5},
            {10, 2, 8, 4, 7, 6, 1, 5, 15, 11, 9, 14, 3, 12, 13, 0},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15},
            {14, 10, 4, 8, 9, 15, 13, 6, 1, 12, 0, 2, 11, 7, 5, 3},
            {11, 8, 12, 0, 5, 2, 15, 13, 10, 14, 3, 6, 7, 1, 9, 4},
            {7, 9, 3, 1, 13, 12, 11, 14, 2, 6, 5, 10, 4, 0, 15, 8},
            {9, 0, 5, 7, 2, 4, 10, 15, 14, 1, 11, 12, 6, 8, 3, 13},
            {2, 12, 6, 10, 0, 11, 8, 3, 4, 13, 7, 5, 15, 14, 1, 9}
    };

    protected static final byte[] padding = new byte[]{
            (byte) 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };


    private static final int[] u256 = new int[]{
            0x243f6a88, 0x85a308d3, 0x13198a2e, 0x03707344,
            0xa4093822, 0x299f31d0, 0x082efa98, 0xec4e6c89,
            0x452821e6, 0x38d01377, 0xbe5466cf, 0x34e90c6c,
            0xc0ac29b7, 0xc97c50dd, 0x3f84d5b5, 0xb5470917
    };

    // Blake Initialization Vector:
    private final int[] vectorH = new int[]{
            0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
            0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
    };


    private final int[] s = new int[4];

    private boolean nullt;

    private static final byte[] zo = new byte[]{0x01};
    private static final byte[] oo = new byte[]{(byte) 0x81};

    protected byte[] block;

    protected int blockOffset;

    protected long[] length = new long[2];

    public BlakeDigest() {
        init();
    }

    protected void lengthCarry(long[] arr) {
        for (int j = 0; j < arr.length; ++j) {
            if (arr[j] < 0x0100000000L) {
                break;
            }
            arr[j] -= 0x0100000000L;
            arr[j + 1] += 1;
        }
    }

    /**
     * update the message digest with a block of bytes.
     *
     * @param in    the byte array containing the data.
     */
    public void update(byte[] in) {
        update(in, 0, in.length);
    }

    /**
     * update the message digest with a block of bytes.
     *
     * @param data  the byte array containing the data.
     * @param inOff the offset into the byte array where the data starts.
     * @param len   the length of the data.
     */
    @Override
    public void update(byte[] data, int inOff, int len) {
        int offset = 0;

        while (this.blockOffset + len - offset >= block.length) {
            for (int i = this.blockOffset; i < block.length; ) {
                block[i++] = data[inOff + offset++];
            }

            this.length[0] += block.length * 8L;
            this.lengthCarry(this.length);

            this.compress();
            this.blockOffset = 0;
        }

        while (offset < len) {
            block[this.blockOffset++] = data[inOff + offset++];
        }
    }

    /**
     * update the message digest with a single byte.
     *
     * @param in the input byte to be entered.
     */
    @Override
    public void update(byte in) {
        byte[] data = new byte[]{in};
        update(data, 0, 1);
    }


    private static int ROUNDS = 12; // to use for Catenas H'
    private final static int BLOCK_LENGTH_BYTES = 256;// bytes


    @Override
    public int getByteLength() {
        return BLOCK_LENGTH_BYTES;
    }

    /**
     * return the algorithm name
     *
     * @return the algorithm name
     */
    @Override
    public String getAlgorithmName() {
        return "BLAKE";
    }

    /**
     * return the size, in bytes, of the digest produced by this message digest.
     *
     * @return the size, in bytes, of the digest produced by this message digest.
     */
    @Override
    public int getDigestSize() {
        return 32;
    }


    /**
     * close the digest, producing the final digest value. The doFinal
     * call leaves the digest reset.
     *
     * @param out    the array the digest is to be copied into.
     * @param outOff the offset into the out array the digest is to start at.
     */
    @Override
    public int doFinal(byte[] out, int outOff) {
        this.padding();
        byte[] buffer = new byte[32];
        u32ArrayTo8(vectorH, buffer);
        System.arraycopy(buffer, 0, out, outOff, 32);
        return 32;
    }

    /**
     * reset the digest back to it's initial state.
     */
    @Override
    public void reset() {
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

        for (i = 0; i < m.length; i++) {
            m[i] = u8to32(block, i * 4);
        }

        for (i = 0; i < 8; i++) {
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

        for (i = 0; i < 14; i++) {
            /* column step */
            g(v, m, 0, 4, 8, 12, 0, i);
            g(v, m, 1, 5, 9, 13, 2, i);
            g(v, m, 2, 6, 10, 14, 4, i);
            g(v, m, 3, 7, 11, 15, 6, i);
            /* diagonal step */
            g(v, m, 0, 5, 10, 15, 8, i);
            g(v, m, 1, 6, 11, 12, 10, i);
            g(v, m, 2, 7, 8, 13, 12, i);
            g(v, m, 3, 4, 9, 14, 14, i);
        }

        for (i = 0; i < 16; i++) {
            vectorH[i % 8] ^= v[i];
        }

        for (i = 0; i < 8; i++) {
            vectorH[i] ^= s[i % 4];
        }
    }

    // left shift
    public int rot(int x, int n) {
        long xl = x & 0x00000000ffffffffL;
        long r = ((xl << (32 - n)) | ((xl >> (n)))) & 0x00000000ffffffffL;
        return (int) r;
    }

    public void g(int[] v, int[] m, int a, int b, int c, int d, int e, int i) {

        long va = ((long) v[a] + ((long) (m[sigma[i][e]] ^ u256[sigma[i][e + 1]]) & 0x00000000ffffffffL) + (long) v[b]) & 0x00000000ffffffffL;
        v[a] = (int) va;

        v[d] = rot(v[d] ^ v[a], 16);

        long vc = ((long) v[c] + (long) v[d]) & 0x00000000ffffffffL;
        v[c] = (int) vc;

        v[b] = rot(v[b] ^ v[c], 12);

        va = ((long) v[a] + ((long) (m[sigma[i][e + 1]] ^ u256[sigma[i][e]]) & 0x00000000ffffffffL) + (long) v[b]) & 0x00000000ffffffffL;
        v[a] = (int) va;

        v[d] = rot(v[d] ^ v[a], 8);

        vc = ((long) v[c] + (long) v[d]) & 0x00000000ffffffffL;
        v[c] = (int) vc;

        v[b] = rot(v[b] ^ v[c], 7);
    }


    private void padding() {
        long lo = this.length[0] + this.blockOffset * 8L;
        long hi = this.length[1];
        if (lo >= 0x0100000000L) {
            lo -= 0x0100000000L;
            hi += 1;
        }

        byte[] msglen = new byte[8];
        msglen[0] = u32to8((int) (hi & 0xFFFFFFFFL))[0];
        msglen[1] = u32to8((int) (hi & 0xFFFFFFFFL))[1];
        msglen[2] = u32to8((int) (hi & 0xFFFFFFFFL))[2];
        msglen[3] = u32to8((int) (hi & 0xFFFFFFFFL))[3];
        msglen[4] = u32to8((int) (lo & 0xFFFFFFFFL))[0];
        msglen[5] = u32to8((int) (lo & 0xFFFFFFFFL))[1];
        msglen[6] = u32to8((int) (lo & 0xFFFFFFFFL))[2];
        msglen[7] = u32to8((int) (lo & 0xFFFFFFFFL))[3];

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
        byte[] part = new byte[4];
        System.arraycopy(v, index, part, 0, 4);
        return ByteBuffer.wrap(part).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    private byte[] u32to8(int v) {
        byte[] result = new byte[4];
        result[0] = (byte) ((v & 0xFF000000) >> (8 * 3));
        result[1] = (byte) ((v & 0x00FF0000) >> (8 * 2));
        result[2] = (byte) ((v & 0x0000FF00) >> 8);
        result[3] = (byte) (v & 0x000000FF);
        return result;
    }

    private void u32ArrayTo8(int[] source, byte[] out) {
        if (source.length * 4 != out.length) {
            throw new IllegalArgumentException("Bad result size");
        }
        for (int i = 0; i < source.length; i++) {
            out[i * 4] = (byte) ((source[i] & 0xFF000000) >> (8 * 3));
            out[i * 4 + 1] = (byte) ((source[i] & 0x00FF0000) >> (8 * 2));
            out[i * 4 + 2] = (byte) ((source[i] & 0x0000FF00) >> 8);
            out[i * 4 + 3] = (byte) (source[i] & 0x000000FF);
        }
    }

}
