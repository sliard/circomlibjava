package com.circomlib.provider.digest;

import com.circomlib.crypto.digests.BlakeDigest;
import org.bouncycastle.crypto.digests.Blake2bDigest;
import org.bouncycastle.jcajce.provider.digest.BCMessageDigest;

public class Blake256 extends BCMessageDigest implements Cloneable {

    public Blake256() {
        super(new BlakeDigest());
    }

    public Object clone()
            throws CloneNotSupportedException {
        Blake256 d = (Blake256) super.clone();
        d.digest = new Blake2bDigest((Blake2bDigest) digest);

        return d;
    }
}
