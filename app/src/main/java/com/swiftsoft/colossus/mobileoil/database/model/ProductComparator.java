package com.swiftsoft.colossus.mobileoil.database.model;

import java.util.Comparator;

public class ProductComparator implements Comparator<dbProduct>
{
    @Override
    public int compare(dbProduct o1, dbProduct o2)
    {
        return o1.Desc.compareTo(o2.Desc);
    }
}
