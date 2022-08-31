package com.circomlib.hash;

import java.math.BigInteger;

public class BabyJub {

    public byte[] subOrder;

    public byte[] p;
    public byte[] half;
    public byte[] pm1d2;

    private byte[] A;
    private byte[] D;

    public BabyJub() {
        BigInteger order = new BigInteger("21888242871839275222246405745257275088614511777268538073601725287587578984328", 10);
        this.subOrder = ByteArrayOperator.shiftRight(order.toByteArray(), 3);
        this.p = new BigInteger("21888242871839275222246405745257275088548364400416034343698204186575808495617", 10).toByteArray();

        this.pm1d2 = ByteArrayOperator.div(ByteArrayOperator.sub(this.p, ByteArrayOperator.e(1)),  ByteArrayOperator.e(2));

        this.half = ByteArrayOperator.shiftRight(this.p, 1);

        this.A = new BigInteger("168700", 10).toByteArray();
        this.D = new BigInteger("168696", 10).toByteArray();
    }


    public PairByteArray addPoint(PairByteArray a, PairByteArray b) {
        PairByteArray res = new PairByteArray();

        /* does the equivalent of:
        res[0] = bigInt((a[0]*b[1] + b[0]*a[1]) *  bigInt(bigInt("1") + d*a[0]*b[0]*a[1]*b[1]).inverse(q)).affine(q);
        res[1] = bigInt((a[1]*b[1] - cta*a[0]*b[0]) * bigInt(bigInt("1") - d*a[0]*b[0]*a[1]*b[1]).inverse(q)).affine(q);
        */

        byte[] beta = ByteArrayOperator.mul(a.l,b.r);
        byte[] gamma = ByteArrayOperator.mul(a.r,b.l);
        byte[] delta = ByteArrayOperator.mul(
                ByteArrayOperator.sub(a.r, ByteArrayOperator.mul(this.A, a.l)),
                ByteArrayOperator.add(b.l, b.r)
        );
        byte[] tau = ByteArrayOperator.mul(beta, gamma);
        byte[] dtau = ByteArrayOperator.mul(this.D, tau);

        res.l = ByteArrayOperator.div(
                ByteArrayOperator.add(beta, gamma),
                ByteArrayOperator.add(ByteArrayOperator.ONE, dtau)
        );

        res.r = ByteArrayOperator.div(
                ByteArrayOperator.add(delta, ByteArrayOperator.sub(ByteArrayOperator.mul(this.A,beta), gamma)),
                ByteArrayOperator.sub(ByteArrayOperator.ONE, dtau)
        );

        return res;
    }

    public PairByteArray mulPointEscalar(PairByteArray base, byte[] e) {

        PairByteArray res = new PairByteArray(ByteArrayOperator.ZERO,ByteArrayOperator.ONE);
        byte[] rem = e;
        PairByteArray exp = base;

        while (! ByteArrayOperator.isZero(rem)) {
            if (ByteArrayOperator.isOdd(rem)) {
                res = this.addPoint(res, exp);
            }
            exp = this.addPoint(exp, exp);
            rem = ByteArrayOperator.shiftRight(rem, 1);
        }

        return res;
    }

    public boolean inSubgroup(PairByteArray p) {
        if (!this.inCurve(p)) {
            return false;
        }
        PairByteArray res = this.mulPointEscalar(p, this.subOrder);
        return (ByteArrayOperator.isZero(res.l) && ByteArrayOperator.eq(res.r, ByteArrayOperator.ONE));
    }

    public boolean inCurve(PairByteArray p) {
        byte[] x2 = ByteArrayOperator.square(p.l);
        byte[] y2 = ByteArrayOperator.square(p.r);

        return ByteArrayOperator.eq(
                ByteArrayOperator.add(ByteArrayOperator.mul(this.A, x2), y2),
                ByteArrayOperator.add(ByteArrayOperator.ONE, ByteArrayOperator.mul(ByteArrayOperator.mul(x2, y2), this.D)));
    }


    public PairByteArray unpackPoint(byte[] buff) {
        boolean sign = false;
        PairByteArray p = new PairByteArray();
        if ((buff[31] & 0x80) != 0) {
            sign = true;
            buff[31] = (byte)(buff[31] & 0x7F);
        }
        p.r = ByteArrayOperator.fromRprLE(buff, 0);
        if (ByteArrayOperator.gt(p.r, this.p)) {
            return null;
        }

        byte[] y2 = ByteArrayOperator.square(p.r);

        byte[] x2 = ByteArrayOperator.div(
                ByteArrayOperator.sub(ByteArrayOperator.ONE, y2),
                ByteArrayOperator.sub(this.A, ByteArrayOperator.mul(this.D, y2))
        );

        byte[] x2h = ByteArrayOperator.exp(x2, this.half);
        if (! ByteArrayOperator.eq(ByteArrayOperator.ONE, x2h)) {
            return null;
        }

        byte[] x;
        try {
           x = ByteArrayOperator.sqrt(x2);
        } catch (Exception e) {
            return null;
        }

        if (sign) {
            x = ByteArrayOperator.neg(x);
        }
        p.l = x;
        return p;
    }

    public byte[] packPoint(PairByteArray p) {
        System.out.println("p.l="+ByteArrayOperator.toString(p.l, 16));
        System.out.println("p.r="+ByteArrayOperator.toString(p.r, 16));
        byte[] buff = ByteArrayOperator.toRprLE(p.r, 0);
        System.out.println("buff="+ByteArrayOperator.toString(buff, 16));
        System.out.println("buff="+new BigInteger(buff).toString(16));
        System.out.println("buff[31]="+buff[31]);
        System.out.println("buff[30]="+buff[30]);
        System.out.println("buff[1]="+buff[1]);
        System.out.println("buff[0]="+buff[0]);
        if (ByteArrayOperator.gt(p.l, this.pm1d2)) {
            buff[31] = (byte)(buff[31] | 0x80);
        }
        System.out.println("buff="+ByteArrayOperator.toString(buff, 16));
        System.out.println("buff[31]="+buff[31]);
        System.out.println("buff[30]="+buff[30]);
        System.out.println("buff[1]="+buff[1]);
        System.out.println("buff[0]="+buff[0]);
        return buff;
    }
}
