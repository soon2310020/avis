package saleson.common.util;

import saleson.common.enumeration.WeightUnit;

public class UnitExchange {
    public static Double weightUnitExchange(Double weight, WeightUnit original, WeightUnit exchanged){
        if(original.equals(WeightUnit.KILOGRAMS) && exchanged.equals(WeightUnit.GRAMS)) return weight * 1000;
        else if(original.equals(WeightUnit.OUNCES) && exchanged.equals(WeightUnit.GRAMS)) return weight * 28.3495;
        else if(original.equals(WeightUnit.POUNDS) && exchanged.equals(WeightUnit.GRAMS)) return weight * 453.592;
        return weight;
    }
}
