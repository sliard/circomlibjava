package com.circomlib.hash;

import org.junit.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BabyJubTest {

    @Test
    public void addPoint() {
        BabyJub babyJub = new BabyJub();
        PairByteArray out = babyJub.addPoint(
                new PairByteArray(ByteArrayOperator.e(0), ByteArrayOperator.e(1)),
                new PairByteArray(ByteArrayOperator.e(0), ByteArrayOperator.e(1)));
        assertEquals(0, new BigInteger(out.l).intValue());
        assertEquals(1, new BigInteger(out.r).intValue());
    }

    @Test
    public void generatorTest() {
        BigInteger generatorL = new BigInteger("995203441582195749578291179787384436505546430278305826713579947235728471134", 10);
        BigInteger generatorR = new BigInteger("5472060717959818805561601436314318772137091100104008585924551046643952123905", 10);

        PairByteArray generator = new PairByteArray(ByteArrayOperator.e(generatorL), ByteArrayOperator.e(generatorR));

        BabyJub babyJub = new BabyJub();
        PairByteArray out = babyJub.addPoint(generator, generator);
        out = babyJub.addPoint(out, out);
        out = babyJub.addPoint(out, out);

        String base8L = "5299619240641551281634865583518297030282874472190772894086521144482721001553";
        String base8R ="16950150798460657717958625567821834550301663161624707787222815936182638968203";

        assertEquals(base8L, new BigInteger(out.l).toString(10));
        assertEquals(base8R, new BigInteger(out.r).toString(10));
    }

    @Test
    public void addSamePoint() {
        BigInteger pL = new BigInteger("17777552123799933955779906779655732241715742912184938656739573121738514868268", 10);
        BigInteger pR = new BigInteger("2626589144620713026669568689430873010625803728049924121243784502389097019475", 10);

        PairByteArray generator = new PairByteArray(ByteArrayOperator.e(pL), ByteArrayOperator.e(pR));

        BabyJub babyJub = new BabyJub();
        PairByteArray out = babyJub.addPoint(generator, generator);

        assertEquals("6890855772600357754907169075114257697580319025794532037257385534741338397365", new BigInteger(out.l).toString(10));
        assertEquals("4338620300185947561074059802482547481416142213883829469920100239455078257889", new BigInteger(out.r).toString(10));
    }

    @Test
    public void addDifferentPoint() {
        BigInteger p1L = new BigInteger("17777552123799933955779906779655732241715742912184938656739573121738514868268", 10);
        BigInteger p1R = new BigInteger("2626589144620713026669568689430873010625803728049924121243784502389097019475", 10);

        BigInteger p2L = new BigInteger("16540640123574156134436876038791482806971768689494387082833631921987005038935", 10);
        BigInteger p2R = new BigInteger("20819045374670962167435360035096875258406992893633759881276124905556507972311", 10);

        PairByteArray p1 = new PairByteArray(ByteArrayOperator.e(p1L), ByteArrayOperator.e(p1R));
        PairByteArray p2 = new PairByteArray(ByteArrayOperator.e(p2L), ByteArrayOperator.e(p2R));

        BabyJub babyJub = new BabyJub();
        PairByteArray out = babyJub.addPoint(p1, p2);

        assertEquals("7916061937171219682591368294088513039687205273691143098332585753343424131937", new BigInteger(out.l).toString(10));
        assertEquals("14035240266687799601661095864649209771790948434046947201833777492504781204499", new BigInteger(out.r).toString(10));
    }

    @Test
    public void mulPointEscalarZero() {
        BigInteger pL = new BigInteger("17777552123799933955779906779655732241715742912184938656739573121738514868268", 10);
        BigInteger pR = new BigInteger("2626589144620713026669568689430873010625803728049924121243784502389097019475", 10);

        PairByteArray p = new PairByteArray(ByteArrayOperator.e(pL), ByteArrayOperator.e(pR));

        BabyJub babyJub = new BabyJub();
        PairByteArray r = babyJub.mulPointEscalar(p, ByteArrayOperator.e(3));
        PairByteArray r2 = babyJub.addPoint(p, p);
        r2 = babyJub.addPoint(r2, p);

        assertEquals(ByteArrayOperator.toString(r2.l, 16), ByteArrayOperator.toString(r.l, 16));
        assertEquals(ByteArrayOperator.toString(r2.r, 16), ByteArrayOperator.toString(r.r, 16));
        assertEquals("19372461775513343691590086534037741906533799473648040012278229434133483800898", new BigInteger(r.l).toString(10));
        assertEquals("9458658722007214007257525444427903161243386465067105737478306991484593958249", new BigInteger(r.r).toString(10));
    }

    @Test
    public void mulPointEscalarOne() {
        BigInteger pL = new BigInteger("17777552123799933955779906779655732241715742912184938656739573121738514868268", 10);
        BigInteger pR = new BigInteger("2626589144620713026669568689430873010625803728049924121243784502389097019475", 10);

        PairByteArray p = new PairByteArray(ByteArrayOperator.e(pL), ByteArrayOperator.e(pR));

        BigInteger mul = new BigInteger("14035240266687799601661095864649209771790948434046947201833777492504781204499", 10);

        BabyJub babyJub = new BabyJub();
        PairByteArray r = babyJub.mulPointEscalar(p, ByteArrayOperator.e(mul));
        assertEquals("17070357974431721403481313912716834497662307308519659060910483826664480189605", new BigInteger(r.l).toString(10));
        assertEquals("4014745322800118607127020275658861516666525056516280575712425373174125159339", new BigInteger(r.r).toString(10));
    }

    @Test
    public void mulPointEscalarTwo() {
        BigInteger pL = new BigInteger("6890855772600357754907169075114257697580319025794532037257385534741338397365", 10);
        BigInteger pR = new BigInteger("4338620300185947561074059802482547481416142213883829469920100239455078257889", 10);

        PairByteArray p = new PairByteArray(ByteArrayOperator.e(pL), ByteArrayOperator.e(pR));

        BigInteger mul = new BigInteger("20819045374670962167435360035096875258406992893633759881276124905556507972311", 10);

        BabyJub babyJub = new BabyJub();
        PairByteArray r = babyJub.mulPointEscalar(p, ByteArrayOperator.e(mul));
        assertEquals("13563888653650925984868671744672725781658357821216877865297235725727006259983", new BigInteger(r.l).toString(10));
        assertEquals("8442587202676550862664528699803615547505326611544120184665036919364004251662", new BigInteger(r.r).toString(10));
    }

    @Test
    public void inCurveOne() {
        BigInteger pL = new BigInteger("17777552123799933955779906779655732241715742912184938656739573121738514868268", 10);
        BigInteger pR = new BigInteger("2626589144620713026669568689430873010625803728049924121243784502389097019475", 10);

        PairByteArray p = new PairByteArray(ByteArrayOperator.e(pL), ByteArrayOperator.e(pR));

        BabyJub babyJub = new BabyJub();
        boolean res = babyJub.inCurve(p);
        assertTrue(res);
    }

    @Test
    public void inCurveTwo() {
        BigInteger pL = new BigInteger("6890855772600357754907169075114257697580319025794532037257385534741338397365", 10);
        BigInteger pR = new BigInteger("4338620300185947561074059802482547481416142213883829469920100239455078257889", 10);

        PairByteArray p = new PairByteArray(ByteArrayOperator.e(pL), ByteArrayOperator.e(pR));

        BabyJub babyJub = new BabyJub();
        boolean res = babyJub.inCurve(p);
        assertTrue(res);
    }

    @Test
    public void inSubgroupOne() {
        BigInteger pL = new BigInteger("17777552123799933955779906779655732241715742912184938656739573121738514868268", 10);
        BigInteger pR = new BigInteger("2626589144620713026669568689430873010625803728049924121243784502389097019475", 10);

        PairByteArray p = new PairByteArray(ByteArrayOperator.e(pL), ByteArrayOperator.e(pR));

        BabyJub babyJub = new BabyJub();
        boolean res = babyJub.inSubgroup(p);
        assertTrue(res);
    }

    @Test
    public void inSubgroupTwo() {
        BigInteger pL = new BigInteger("6890855772600357754907169075114257697580319025794532037257385534741338397365", 10);
        BigInteger pR = new BigInteger("4338620300185947561074059802482547481416142213883829469920100239455078257889", 10);

        PairByteArray p = new PairByteArray(ByteArrayOperator.e(pL), ByteArrayOperator.e(pR));

        BabyJub babyJub = new BabyJub();
        boolean res = babyJub.inSubgroup(p);
        assertTrue(res);
    }

    @Test
    public void packPointUnpackPointOne() {
        BigInteger pL = new BigInteger("17777552123799933955779906779655732241715742912184938656739573121738514868268", 10);
        BigInteger pR = new BigInteger("2626589144620713026669568689430873010625803728049924121243784502389097019475", 10);

        PairByteArray p = new PairByteArray(ByteArrayOperator.e(pL), ByteArrayOperator.e(pR));

        BabyJub babyJub = new BabyJub();
        byte[] res1= babyJub.packPoint(p);
        assertEquals("53b81ed5bffe9545b54016234682e7b2f699bd42a5e9eae27ff4051bc698ce85", new BigInteger(res1).toString(10));
        PairByteArray res = babyJub.unpackPoint(res1);
        assertEquals("17777552123799933955779906779655732241715742912184938656739573121738514868268", new BigInteger(res.l).toString(10));
        assertEquals("2626589144620713026669568689430873010625803728049924121243784502389097019475", new BigInteger(res.r).toString(10));
    }

    @Test
    public void packPointUnpackPointTwo() {
        BigInteger pL = new BigInteger("6890855772600357754907169075114257697580319025794532037257385534741338397365", 10);
        BigInteger pR = new BigInteger("4338620300185947561074059802482547481416142213883829469920100239455078257889", 10);

        PairByteArray p = new PairByteArray(ByteArrayOperator.e(pL), ByteArrayOperator.e(pR));

        BabyJub babyJub = new BabyJub();
        byte[] res1= babyJub.packPoint(p);
        assertEquals("e114eb17eddf794f063a68fecac515e3620e131976108555735c8b0773929709", new BigInteger(res1).toString(10));
        PairByteArray res = babyJub.unpackPoint(res1);
        assertEquals("6890855772600357754907169075114257697580319025794532037257385534741338397365", new BigInteger(res.l).toString(10));
        assertEquals("4338620300185947561074059802482547481416142213883829469920100239455078257889", new BigInteger(res.r).toString(10));
    }

}
