package be.iffy.fv.jakarta.support;

import be.iffy.fv.Rule;
import be.iffy.fv.jakarta.FvStaticRule;

import java.util.Map;

import static be.iffy.fv.dsl.DSL.ints;
import static be.iffy.fv.dsl.DSL.maps;

/**
 * Test model with a list of objects validated per-element — exercises intermediate indexed paths
 * (e.g. "lines[1].qty") where the index is not on the terminal segment.
 */
@FvStaticRule(on=CartMap.class, field = "RULE")
public record CartMap(Map<String, Line> map) {

    static final Rule<CartMap> RULE =  maps.<String,Line>notEmpty().on(CartMap::map).and(maps.<String, Line>validateValuesWith(
        ints.atLeast(1).on(Line::qty)
    ).on(CartMap::map));


    public record Line(String sku, int qty) {
    }

}
