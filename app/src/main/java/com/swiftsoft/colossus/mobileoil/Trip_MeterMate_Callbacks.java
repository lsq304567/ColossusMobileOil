package com.swiftsoft.colossus.mobileoil;

import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;

public interface Trip_MeterMate_Callbacks
{
	// Litres to preset.
	int getLitres();
	
	// Product to meter.
	dbProduct getProduct();
	
	// Called when ticket is complete.
	void onTicketComplete();
	
	// Called when 'Next' is clicked.
	void onNextClicked();
}
