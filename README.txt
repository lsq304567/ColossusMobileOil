V0.99t19
========

Addition of Unit tests for Utils class

Correction to make sure that List of dbProduct objects returned by dbEndOfDay.getUniqueProducts() is actually unique

Add code for creation of EoD Report to be sent to Colossus

V0.99t18
========

Addition of Stock Matrix at bottom of Trip Report

Addition of screens and printing for End of Day Report

V0.99t17
========

Fixed issure when parsing dates and using demo MeterMate

Remove Vehicle Reg. from Driver signature panel, as now displayed in top section of Delivery Docket

Monetary values calculated using BigDecimal rather than double - solves several inaccuracies that were noted

V0.99t16
========

Various corrections made to codebase after running LINT

Refactored retrieval of current time to a utility method

Printing of both Customer name & code on Trip Report and Cash Report

Addition of orders that could not be delivered to the transactions report.

Printing of vehicle and driver details at top of delivery docket

Implemented changes for unattended delivery - screens - delivery docket

Initial changes to permit viewing of phone device

Corrected formatting of date/time strings coming from MeterMate and printed on delivery docket

V0.99t15
========

On delivery ticket changed text from 'Invoice No:' to 'Delivery/Invoice No:'

Disable Payment button if pricing is hidden

Correction to use the delivered quantity to calculate the surcharge rather than use the erroneous ordered quantity

Addition of Skip Delivery functionality so that driver can indicate to Colossus the reason why he as not able to deliver an order

Added confirmation to Skip Order functionality

V0.99t14
========

Addition of Customer Type field

Addition of facility to hide prices if the customer is marked as unpriced

Changed 'Paid driver' message to 'Payment received' on delivery docket

Addition of the Customer Order Number on the Delivery docket

Meter Data output moved beneath Discount message on Delivery Docket

In discount message on delivery docket changed PPL figure to 2 dp from 4 dp

V0.99t13
========

Moved the location on docket where Customer Terms are displayed.

Moved location on docket where Meter Data is printed.

Resized fonts to give better output

Changed display of PPL to 3dp and VAT to 2 dp

Removed VAT from docket table and added percentage to the VAT amount field

Added Ordered litres column to the docket table

Print message about discounts if surcharge applied

Altered text on Date title of docket signature panels

Added Vehicle registration to driver signature panel

V0.99t12
========

Changed Start of Trip 'Spare' column to 'To Load' column. Values held there are
the amount of product the driver is required to load to meet trip requirements.

Also Load Product & Return Product screens altered to display the amount of
product required to be loaded to meet requirements (Load Product) and maximum
amount of product that can be returned and still meet Trip requirements (Return Product).

V0.99t11
========

Added required by details and delivery information to Transport Document

Corrected spacing to take account of descenders

V0.99t10
========

Decrease the increment by which a printer bitmap is extended
This is to try and reduce memory pressure

V0.99t9
=======

Corrected error on Trip Report were the opening stock was not peinting correctly

V0.99t8
=======

Added spacing on trip reports so that print is not so crowded.

V0.99t7
=======

Added some Crashltics stuff
Corrected casing of some of the methods to comply with Java standards

V0.99t6
=======

Added Logging of Bluetooth data between the application and MeterMate.
Added button to Settings screen to enable/disable the Logging.
