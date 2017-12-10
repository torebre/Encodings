package com.kjipo.prototype;

import java.util.stream.Stream;

public interface AdjustablePrototype extends Prototype {

    Stream<? extends AdjustablePrototype> getMovements();


}
