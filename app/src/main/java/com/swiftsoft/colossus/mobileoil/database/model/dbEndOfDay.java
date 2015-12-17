package com.swiftsoft.colossus.mobileoil.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Table(name = "EndOfDay")
public class dbEndOfDay extends Model
{
    @Column(name = "TripId")
    public int TripId;

    @Column(name = "Product")
    public dbProduct Product;

    @Column(name = "Type")
    public String Type;

    @Column(name = "Quantity")
    public int Quantity;

    /**
     * Delete all of the rows in the EndOfDay table.
     */
    public static void deleteAll()
    {
        new Delete().from(dbEndOfDay.class).execute();
    }

    public static List<dbEndOfDay> getAll()
    {
        return new Select().from(dbEndOfDay.class).orderBy("TripId").execute();
    }

    public static List<Integer> getTripIds()
    {
        // Create List to contain the returned trip ids.
        List<Integer> uniqueTripIds = new ArrayList<Integer>();

        for (dbEndOfDay item : getAll())
        {
            if (!uniqueTripIds.contains(item.TripId))
            {
                uniqueTripIds.add(item.TripId);
            }
        }

        return uniqueTripIds;
    }

    public static List<dbProduct> getUniqueProducts()
    {
        // Create List to contain the unique products in the table
        List<dbProduct> uniqueProducts = new ArrayList<dbProduct>();

        List<dbEndOfDay> items = getAll();

        // Loop over all the items adding to output
        // list if it is not already present.
        for (dbEndOfDay item : items)
        {
            if (!uniqueProducts.contains(item.Product))
            {
                uniqueProducts.add(item.Product);
            }
        }

        // Sort the product in alphabetic order by description.
        Collections.sort(uniqueProducts, new ProductComparator());

        // Return the unique products
        return uniqueProducts;
    }

    public static List<dbEndOfDay> find(int tripId)
    {
        return new Select().from(dbEndOfDay.class).where("TripId=?", tripId).execute();
    }

    public static List<dbEndOfDay> find(int tripId, dbProduct product)
    {
        List<dbEndOfDay> list = new ArrayList<dbEndOfDay>();

        for (dbEndOfDay item : find(tripId))
        {
            if (item.Product.ColossusID == product.ColossusID)
            {
                list.add(item);
            }
        }

        return list;
    }

    public static int getQuantity(String type, dbProduct product)
    {
        int quantity = 0;

        for (dbEndOfDay item : getAll())
        {
            if (product.equals(item.Product) && type.equals(item.Type))
            {
                quantity += item.Quantity;
            }
        }

        return quantity;
    }
}
