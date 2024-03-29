package com.swiftsoft.colossus.mobileoil.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.swiftsoft.colossus.mobileoil.CrashReporter;

import java.math.BigDecimal;
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

    @Column(name = "Value")
    public String Value;

    public BigDecimal getValue()
    {
        if (Value == null)
        {
            setValue(BigDecimal.ZERO);
        }

        return new BigDecimal(Value);
    }

    public void setValue(BigDecimal value)
    {
        Value = value.toString();
    }

    /**
     * Delete all of the rows in the EndOfDay table.
     */
    public static void deleteAll()
    {
        CrashReporter.leaveBreadcrumb("dbEndOfDay: deletedAll");

        new Delete().from(dbEndOfDay.class).execute();
    }

    private static List<dbEndOfDay> getAll()
    {
        CrashReporter.leaveBreadcrumb("dbEndOfDay: getAll");

        return new Select().from(dbEndOfDay.class).orderBy("TripId").execute();
    }

    public static int getCount()
    {
        CrashReporter.leaveBreadcrumb("dbEndOfDay: getCount");

        return getAll().size();
    }

    public static int getNumberOfPayments()
    {
        CrashReporter.leaveBreadcrumb("dbEndOfDay: getNumberOfPayments");

        return new Select().from(dbEndOfDay.class).where("Type like ?", "Payment_%").execute().size();
    }

    public static BigDecimal getCashPayments()
    {
        CrashReporter.leaveBreadcrumb("dbEndOfDay: getCashPayments");

        BigDecimal total = BigDecimal.ZERO;

        List<dbEndOfDay> cashPayments = new Select().from(dbEndOfDay.class).where("Type like ?", "Payment_Cash").execute();

        for (dbEndOfDay item : cashPayments)
        {
            total = total.add(item.getValue());
        }

        return total;
    }

    public static BigDecimal getChequePayments()
    {
        CrashReporter.leaveBreadcrumb("dbEndOfDay: getChequePayments");

        BigDecimal total = BigDecimal.ZERO;

        List<dbEndOfDay> chequePayments = new Select().from(dbEndOfDay.class).where("Type like ?", "Payment_Cheque").execute();

        for (dbEndOfDay item : chequePayments)
        {
            total = total.add(item.getValue());
        }

        return total;
    }

    public static BigDecimal getVoucherPayments()
    {
        CrashReporter.leaveBreadcrumb("dbEndOfDay: getVoucherPayments");

        BigDecimal total = BigDecimal.ZERO;

        List<dbEndOfDay> voucherPayments = new Select().from(dbEndOfDay.class).where("Type like ?", "Payment_Voucher").execute();

        for (dbEndOfDay item : voucherPayments)
        {
            total = total.add(item.getValue());
        }

        return total;
    }

    public static List<Integer> getUniqueTripIds()
    {
        CrashReporter.leaveBreadcrumb("dbEndOfDay: getUniqueTripIds");

        List<Integer> uniqueTripIds = new ArrayList<Integer>();

        for (dbEndOfDay item : getAll())
        {
            if (!uniqueTripIds.contains(item.TripId))
            {
                uniqueTripIds.add(item.TripId);
            }
        }

        Collections.sort(uniqueTripIds);

        return uniqueTripIds;
    }

    public static List<dbProduct> getUniqueProducts()
    {
        CrashReporter.leaveBreadcrumb("dbEndOfDay: getUniqueProducts");

        // Create List to contain the unique products in the table
        List<dbProduct> uniqueProducts = new ArrayList<dbProduct>();

        // Get a list of all the items where the product is not null
        List<dbEndOfDay> items = new Select().from(dbEndOfDay.class).where("Product is not null").execute();

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

    private static List<dbEndOfDay> find(int tripId)
    {
        CrashReporter.leaveBreadcrumb("dbEndOfDay: find");

        return new Select().from(dbEndOfDay.class).where("TripId=?", tripId).execute();
    }

    public static List<dbEndOfDay> find(int tripId, dbProduct product)
    {
        CrashReporter.leaveBreadcrumb("dbEndOfDay: find");

        List<dbEndOfDay> list = new ArrayList<dbEndOfDay>();

        for (dbEndOfDay item : find(tripId))
        {
            if (item.Product != null && item.Product.ColossusID == product.ColossusID)
            {
                list.add(item);
            }
        }

        return list;
    }

    public static int getStartingQuantity(dbProduct product)
    {
        CrashReporter.leaveBreadcrumb("dbEndOfDay: getStartingQuantity");

        int firstId = getUniqueTripIds().get(0);

        for (dbEndOfDay item : find(firstId))
        {
            if (item.Product != null && product.equals(item.Product) && item.Type.equals("Start"))
            {
                return item.Quantity;
            }
        }

        return 0;
    }

    public static int getFinishingQuantity(dbProduct product)
    {
        CrashReporter.leaveBreadcrumb("dbEndOfDay: getFinishingQuantity");

        List<Integer> tripIds = getUniqueTripIds();

        int firstId = tripIds.get(tripIds.size() - 1);

        for (dbEndOfDay item : find(firstId))
        {
            if (item.Product != null && product.equals(item.Product) && item.Type.equals("Finish"))
            {
                return item.Quantity;
            }
        }

        return 0;
    }

    public static int getLoadedQuantity(dbProduct product)
    {
        CrashReporter.leaveBreadcrumb("dbEndOfDay: getLoadedQuantity");

        return getQuantity("Load", product);
    }

    public static int getDeliveredQuantity(dbProduct product)
    {
        CrashReporter.leaveBreadcrumb("dbEndOfDay: getDeliveredQuantity");

        return getQuantity("Deliver", product);
    }

    public static int getReturnedQuantity(dbProduct product)
    {
        CrashReporter.leaveBreadcrumb("dbEndOfDay: getReturnedQuantity");

        return getQuantity("Return", product);
    }

    private static int getQuantity(String type, dbProduct product)
    {
        CrashReporter.leaveBreadcrumb("dbEndOfDay: getQuantity");

        int quantity = 0;

        List<dbEndOfDay> items = new Select().from(dbEndOfDay.class).where("Product is not null and Type=?", type).execute();

        for (dbEndOfDay item : items)
        {
            if (product.equals(item.Product))
            {
                quantity += item.Quantity;
            }
        }

        return quantity;
    }
}
