package com.swiftsoft.colossus.mobileoil;

import com.swiftsoft.colossus.mobileoil.database.model.dbDriver;
import com.swiftsoft.colossus.mobileoil.database.model.dbMessageIn;
import com.swiftsoft.colossus.mobileoil.database.model.dbMessageOut;
import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;
import com.swiftsoft.colossus.mobileoil.database.model.dbSetting;
import com.swiftsoft.colossus.mobileoil.database.model.dbTrip;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrder;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrderLine;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripStock;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripStockComp;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicle;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicleChecklist;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicleChecklistSection;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicleChecklistSectionItem;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicleStock;

import java.util.Calendar;
import java.util.Date;

public class DemoData
{
	public static void Create()
	{
		Calendar c = Calendar.getInstance();

		// Delete all existing data.
		dbTripStockComp.DeleteAll();
		dbTripStock.DeleteAll();
		dbTripOrderLine.DeleteAll();
		dbTripOrder.DeleteAll();
		dbTrip.DeleteAll();
		
		dbVehicleStock.DeleteAll();
		dbVehicleChecklistSectionItem.DeleteAll();
		dbVehicleChecklistSection.DeleteAll();
		dbVehicleChecklist.DeleteAll();
		dbVehicle.DeleteAll();
		
		dbDriver.DeleteAll();
		
		dbProduct.DeleteAll();
		
		dbMessageOut.DeleteAll();
		dbMessageIn.DeleteAll();
		
		// License device.
		dbSetting setting1 = dbSetting.FindByKeyOrCreate("DeviceLicensed");
		setting1.StringValue = "true";
		setting1.save();

		dbSetting setting2 = dbSetting.FindByKeyOrCreate("DeviceNo");
		setting2.IntValue = 1;
		setting2.save();
		
		dbSetting setting3 = dbSetting.FindByKeyOrCreate("ColossusURL");
		setting3.StringValue = "127.0.0.1";
		setting3.save();
		
		dbSetting consignorName = dbSetting.FindByKeyOrCreate("ConsignorName");
		consignorName.StringValue = "Demo Consignor";
		consignorName.save();

		dbSetting consignorAdd1 = dbSetting.FindByKeyOrCreate("ConsignorAdd1");
		consignorAdd1.StringValue = "Address line 1";
		consignorAdd1.save();

		dbSetting consignorAdd2 = dbSetting.FindByKeyOrCreate("ConsignorAdd2");
		consignorAdd2.StringValue = "Address line 2";
		consignorAdd2.save();

		dbSetting consignorAdd3 = dbSetting.FindByKeyOrCreate("ConsignorAdd3");
		consignorAdd3.StringValue = "Address line 3";
		consignorAdd3.save();
		
		// Register device.
		dbSetting regDevice = dbSetting.FindByKeyOrCreate("DeviceRegistered");
		regDevice.StringValue = "true";
		regDevice.save();
		
		// Create products.
		dbProduct kero = new dbProduct();				
		kero.ColossusID = 1;
		kero.Code = "K";
		kero.Desc = "Kero";
		kero.Colour = -256;
		kero.MobileOil = 1;
		kero.save();

		dbProduct gasoil = new dbProduct();				
		gasoil.ColossusID = 1;
		gasoil.Code = "G";
		gasoil.Desc = "Gas oil";
		gasoil.Colour = -256;
		gasoil.MobileOil = 1;
		gasoil.save();

		dbProduct derv = new dbProduct();				
		derv.ColossusID = 1;
		derv.Code = "D";
		derv.Desc = "Derv";
		derv.Colour = -256;
		derv.MobileOil = 1;
		derv.save();

		// Create a demo vehicle.
		dbVehicle vehicle = new dbVehicle();
		vehicle.ColossusID = 1;
		vehicle.No = 1;
		vehicle.Reg = "OIL 1111";
		vehicle.StockByCompartment = false;
		vehicle.C0_Capacity = 98;
		vehicle.C1_Capacity = 2500;
		vehicle.C2_Capacity = 3500;
		vehicle.C3_Capacity = 3000;
		vehicle.C4_Capacity = 4000;
		vehicle.C5_Capacity = 5000;
		vehicle.C6_Capacity = 0;
		vehicle.C7_Capacity = 0;
		vehicle.C8_Capacity = 0;
		vehicle.C9_Capacity = 0;
		vehicle.save();

		// Setup line stock.
		vehicle.updateCompartmentProduct(0, kero);
		vehicle.updateCompartmentOnboard(0, vehicle.C0_Capacity);
		
		dbVehicleChecklist v1cl = new dbVehicleChecklist();
		v1cl.Vehicle = vehicle;
		v1cl.Version = 1;
		v1cl.save();
		
		dbVehicleChecklistSection v1cls1 = new dbVehicleChecklistSection();
		v1cls1.VehicleChecklist = v1cl;
		v1cls1.Title = "Documents";
		v1cls1.save();
		
		dbVehicleChecklistSectionItem v1cls1i1 = new dbVehicleChecklistSectionItem();
		v1cls1i1.VehicleChecklistSection = v1cls1;
		v1cls1i1.Title = "Vehicle insurance";
		v1cls1i1.save();
		
		dbVehicleChecklistSectionItem v1cls1i2 = new dbVehicleChecklistSectionItem();
		v1cls1i2.VehicleChecklistSection = v1cls1;
		v1cls1i2.Title = "Vehicle registration card";
		v1cls1i2.save();
		
		dbVehicleChecklistSectionItem v1cls1i3 = new dbVehicleChecklistSectionItem();
		v1cls1i3.VehicleChecklistSection = v1cls1;
		v1cls1i3.Title = "Drivers license";
		v1cls1i3.Summary = "With heavy vehicle permission";
		v1cls1i3.save();
		
		dbVehicleChecklistSection v1cls2 = new dbVehicleChecklistSection();
		v1cls2.VehicleChecklist = v1cl;
		v1cls2.Title = "Safety equipment";
		v1cls2.save();
		
		dbVehicleChecklistSectionItem v1cls2i1 = new dbVehicleChecklistSectionItem();
		v1cls2i1.VehicleChecklistSection = v1cls2;
		v1cls2i1.Title = "Fire extinguisher";
		v1cls2i1.Summary = "Fully charged with valid expiry date";
		v1cls2i1.save();
		
		dbVehicleChecklistSectionItem v1cls2i2 = new dbVehicleChecklistSectionItem();
		v1cls2i2.VehicleChecklistSection = v1cls2;
		v1cls2i2.Title = "First aid kit";
		v1cls2i2.Summary = "Valid expiry dates";
		v1cls2i2.save();
		
		dbVehicleChecklistSectionItem v1cls2i3 = new dbVehicleChecklistSectionItem();
		v1cls2i3.VehicleChecklistSection = v1cls2;
		v1cls2i3.Title = "Personal Protection Equipment";
		v1cls2i3.Summary = "Helmet, safety shoes, safety jacket, safety goggles and hand gloves";
		v1cls2i3.save();

		// Register vehicle.
		dbSetting vRegSetting = dbSetting.FindByKeyOrCreate("VehicleRegistered");
		vRegSetting.IntValue = 1;
		vRegSetting.save();
		
		// Create a demo driver.
		dbDriver driver = new dbDriver();
		driver.ColossusID = 1;
		driver.No = 1;
		driver.Name = "Joe Bloggs";
		driver.PIN = 1111;
		driver.save();

		// Create demo trip.
		dbTrip trip = new dbTrip();
		trip.Delivering = false;
		trip.Delivered = false;
		trip.ColossusID = 1;
		trip.No = 701;
		trip.Date = Utils.getCurrentTime();
		trip.Vehicle = vehicle;
		trip.Driver = driver;
		trip.LoadingNotes = "BP Ref 87220";
		trip.save();
	
		// Order #1
		dbTripOrder tripOrder1 = new dbTripOrder();
		tripOrder1.Trip = trip;
		tripOrder1.Delivering = false;
		tripOrder1.Delivered = false;
		tripOrder1.ColossusID = 1;
		tripOrder1.DeliveryOrder = 1;
		tripOrder1.InvoiceNo = "OL1451";
		tripOrder1.BrandID = 1;
		tripOrder1.CustomerCode = "DAR001";
		tripOrder1.CustomerName = "Harvey Darragh";
		tripOrder1.CustomerAddress = "78 Enniskillen Road\nBallinamallard";
		tripOrder1.CustomerPostcode = "BT94 2BD";
		tripOrder1.DeliveryName = "Harvey Darragh";
		tripOrder1.DeliveryAddress = "78 Enniskillen Road\nBallinamallard";
		tripOrder1.DeliveryPostcode = "BT94 2BD";
		tripOrder1.PhoneNos = "028 66388833\n07595392491";
		tripOrder1.RequiredBy = "11am";
		tripOrder1.Terms = "Paying by COD";
		tripOrder1.DueDate = Utils.getCurrentTime();
		tripOrder1.Notes = "";
		tripOrder1.PrepaidAmount = 0;
		tripOrder1.CodPoint = 1;
		tripOrder1.CodType = 1;
		tripOrder1.CodAmount = 315;
		tripOrder1.save();
			
		dbTripOrderLine tripOrder1Line1 = new dbTripOrderLine();
		tripOrder1Line1.TripOrder = tripOrder1;
		tripOrder1Line1.Delivered = false;
		tripOrder1Line1.ColossusID = 1;
		tripOrder1Line1.Product = kero;
		tripOrder1Line1.OrderedQty = 500;
		tripOrder1Line1.OrderedPrice = 60;
		tripOrder1Line1.Surcharge = 5;
		tripOrder1Line1.SurchargePerUOM = true;
		tripOrder1Line1.Ratio = 100;
		tripOrder1Line1.VatPerc1 = 5;
		tripOrder1Line1.VatPerc2 = 20;
		tripOrder1Line1.VatPerc2Above = 99999999;
		tripOrder1Line1.save();

		// Order #2
		c.setTime(new Date());
		c.add(Calendar.DATE, 7);
		
		dbTripOrder tripOrder2 = new dbTripOrder();
		tripOrder2.Trip = trip;
		tripOrder2.Delivering = false;
		tripOrder2.Delivered = false;
		tripOrder2.ColossusID = 1;
		tripOrder2.DeliveryOrder = 2;
		tripOrder2.InvoiceNo = "OL1455";
		tripOrder2.BrandID = 1;
		tripOrder2.CustomerCode = "JON001";
		tripOrder2.CustomerName = "Michael Jones Solicitors";
		tripOrder2.CustomerAddress = "14 Main Street\nEnniskillen";
		tripOrder2.CustomerPostcode = "BT74 1ER";
		tripOrder2.DeliveryName = "Back entrance";
		tripOrder2.DeliveryAddress = "8 East Brige Street\nEnniskillen";
		tripOrder2.DeliveryPostcode = "BT74 1AX";
		tripOrder2.PhoneNos = "078 1234 5678";
		tripOrder2.RequiredBy = "Today";
		tripOrder2.Terms = "7 days";
		tripOrder2.DueDate = c.getTime().getTime();
		tripOrder2.Notes = "Call mobile no to unlock tank";
		tripOrder2.PrepaidAmount = 0;
		tripOrder2.CodPoint = 0;
		tripOrder2.CodType = 0;
		tripOrder2.CodAmount = 0;
		tripOrder2.save();
			
		dbTripOrderLine tripOrder2Line1 = new dbTripOrderLine();
		tripOrder2Line1.TripOrder = tripOrder2;
		tripOrder2Line1.Delivered = false;
		tripOrder2Line1.ColossusID = 1;
		tripOrder2Line1.Product = kero;
		tripOrder2Line1.OrderedQty = 400;
		tripOrder2Line1.OrderedPrice = 61;
		tripOrder2Line1.Surcharge = 5;
		tripOrder2Line1.SurchargePerUOM = true;
		tripOrder2Line1.Ratio = 100;
		tripOrder2Line1.VatPerc1 = 5;
		tripOrder2Line1.VatPerc2 = 20;
		tripOrder2Line1.VatPerc2Above = 99999999;
		tripOrder2Line1.save();

		// Order #3
		dbTripOrder tripOrder3 = new dbTripOrder();
		tripOrder3.Trip = trip;
		tripOrder3.Delivering = false;
		tripOrder3.Delivered = false;
		tripOrder3.ColossusID = 1;
		tripOrder3.DeliveryOrder = 3;
		tripOrder3.InvoiceNo = "OL1457";
		tripOrder3.BrandID = 1;
		tripOrder3.CustomerCode = "SMI001";
		tripOrder3.CustomerName = "Mrs June Smith";
		tripOrder3.CustomerAddress = "51 Castle Street\nEnniskillen";
		tripOrder3.CustomerPostcode = "BT74 2ND";
		tripOrder3.DeliveryName = "Mrs June Smith";
		tripOrder3.DeliveryAddress = "51 Castle Street\nEnniskillen";
		tripOrder3.DeliveryPostcode = "BT74 2ND";
		tripOrder3.PhoneNos = "028 6633 4455";
		tripOrder3.RequiredBy = "Today";
		tripOrder3.Terms = "Paying by Card";
		tripOrder3.DueDate = Utils.getCurrentTime();
		tripOrder3.Notes = "";
		tripOrder3.PrepaidAmount = 0;
		tripOrder3.CodPoint = 0;
		tripOrder3.CodType = 0;
		tripOrder3.CodAmount = 0;
		tripOrder3.save();
			
		dbTripOrderLine tripOrder3Line1 = new dbTripOrderLine();
		tripOrder3Line1.TripOrder = tripOrder3;
		tripOrder3Line1.Delivered = false;
		tripOrder3Line1.ColossusID = 1;
		tripOrder3Line1.Product = kero;
		tripOrder3Line1.OrderedQty = 300;
		tripOrder3Line1.OrderedPrice = 63.4;
		tripOrder3Line1.Surcharge = 0;
		tripOrder3Line1.SurchargePerUOM = false;
		tripOrder3Line1.Ratio = 100;
		tripOrder3Line1.VatPerc1 = 5;
		tripOrder3Line1.VatPerc2 = 20;
		tripOrder3Line1.VatPerc2Above = 99999999;
		tripOrder3Line1.save();

		// Simulators.
		dbSetting printerName = dbSetting.FindByKeyOrCreate("PrinterName");
		printerName.StringValue = "Demo simulator";
		printerName.save();
		
		dbSetting printerAddress = dbSetting.FindByKeyOrCreate("PrinterAddress");
		printerAddress.StringValue = "00:00:00:00:00";
		printerAddress.save();
		
		dbSetting metermateName = dbSetting.FindByKeyOrCreate("MeterMateName");
		metermateName.StringValue = "Demo simulator";
		metermateName.save();
		
		dbSetting metermateAddress = dbSetting.FindByKeyOrCreate("MeterMateAddress");
		metermateAddress.StringValue = "00:00:00:00:00";
		metermateAddress.save();
	}
}
