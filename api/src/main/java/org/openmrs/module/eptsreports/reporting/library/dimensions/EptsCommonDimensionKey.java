package org.openmrs.module.eptsreports.reporting.library.dimensions;

import org.springframework.stereotype.Service;

@Service
public class EptsCommonDimensionKey {

    public String keyFor(DimensionKey dimensionKey){
        return  dimensionKey.getKey();
    }
}
