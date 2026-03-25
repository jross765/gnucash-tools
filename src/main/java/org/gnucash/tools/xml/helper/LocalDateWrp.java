package org.gnucash.tools.xml.helper;

import java.time.LocalDate;

// Ugly, but needed due to the way the args are parsed
// (mutable vs. immutable).
public class LocalDateWrp
{
	public LocalDate dat;
	
	public LocalDateWrp() {
		dat = LocalDate.now();
	}
}
