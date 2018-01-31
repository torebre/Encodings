package com.kjipo.prototype;

import com.kjipo.raster.segment.Pair;
import org.junit.Test;

public class LinePrototypeTest {


    @Test
    public void movementTest() {
        LinePrototype linePrototype = new LinePrototype(Pair.of(0, 0), Pair.of(0, 1));

        linePrototype.getMovements().forEach(System.out::println);

    }


}
