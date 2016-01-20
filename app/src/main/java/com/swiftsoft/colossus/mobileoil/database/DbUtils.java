package com.swiftsoft.colossus.mobileoil.database;

import com.swiftsoft.colossus.mobileoil.Active;
import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;

/**
 * Created by Alan on 20/01/2016.
 */
public class DbUtils
{
    public static String getInfoviewLineProduct(dbProduct product)
    {
        return product == null ? "Line: None" : String.format("Line: %s(%d)", product.Desc, Active.vehicle.getHosereelCapacity());
    }
}
