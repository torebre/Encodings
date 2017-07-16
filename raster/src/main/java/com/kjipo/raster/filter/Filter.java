package com.kjipo.raster.filter;

import java.util.List;

public interface Filter {

    List<boolean[][]> applyFilter(boolean raster[][]);

}
