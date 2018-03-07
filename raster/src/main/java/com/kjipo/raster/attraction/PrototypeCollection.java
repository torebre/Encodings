package com.kjipo.raster.attraction;


import com.kjipo.prototype.Prototype;
import com.kjipo.raster.segment.Segment;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PrototypeCollection<T extends Prototype> implements Prototype {
    private final Collection<T> prototypes;


    public PrototypeCollection(Collection<T> prototypes) {
        this.prototypes = prototypes;
    }

    public Collection<T> getPrototypes() {
        return prototypes;
    }

    @Override
    public List<Segment> getSegments() {
        return prototypes.stream()
                .flatMap(prototype -> prototype.getSegments().stream())
                .collect(Collectors.toList());
    }
}
