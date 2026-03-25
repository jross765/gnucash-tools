package org.gnucash.tools.xml.helper;

import java.time.LocalDate;

import org.gnucash.api.read.GnuCashCommodity;
import org.gnucash.api.read.GnuCashFile;
import org.gnucash.api.read.GnuCashPrice;
import org.gnucash.api.write.GnuCashWritableFile;
import org.gnucash.api.write.GnuCashWritablePrice;
import org.gnucash.base.basetypes.complex.GCshCmdtyID;
import org.gnucash.base.basetypes.simple.GCshPrcID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;

public class PriceHelper
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PriceHelper.class);

	public static GnuCashPrice getPrc(
			CmdLineHelper_Prc.PrcSelectMode prcSelMode, 
			GCshPrcID prcID, 
			GCshCmdtyID cmdtyID, 
			Helper.DateFormat dateFormat, LocalDate date,
			String isin,
			GnuCashFile gcshFile,
			boolean scriptMode) throws Exception {
		GnuCashPrice prc = null;

	    if ( prcSelMode == CmdLineHelper_Prc.PrcSelectMode.ID ) {
			if ( prcID != null ) {
				prc = gcshFile.getPriceByID(prcID);
				if ( prc == null )
				{
					if ( ! scriptMode )
			          System.err.println("Could not find a price with this ID");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <prcID> is null");
			}
	    } else if ( prcSelMode == CmdLineHelper_Prc.PrcSelectMode.SEC_DATE ) {
			if ( cmdtyID != null &&
				 date != null ) {
				if ( ! cmdtyID.isSet() ) {
					LOGGER.debug("getPrc: security-ID is not set");
					return null;
				}
				prc = gcshFile.getPriceByCmdtyIDDate(cmdtyID, date);
				if ( prc == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find a price for this security-ID and date.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <secID> or <date> is null");
			}
	    } else if ( prcSelMode == CmdLineHelper_Prc.PrcSelectMode.ISIN_DATE ) {
			if ( isin != null &&
				 date != null ) {
				if ( isin.equals("") ) {
					LOGGER.debug("getPrc: ISIN is empty");
					return null;
				}
				GnuCashCommodity sec = gcshFile.getCommodityByXCode(isin.toString());
				LOGGER.debug("getPrc: Security with ISIN '" + isin + "' is: " + sec.getQualifID());
				prc = gcshFile.getPriceByCmdtyIDDate(sec.getQualifID(), date);
				if ( prc == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find a price for this ISIN and date.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <isin> or <date> is null");
			}
	    }
		
		if ( prc == null ) {
			System.err.println("Something's wrong: Cannot get price.");
		}
		
		return prc;
	}

	public static GnuCashWritablePrice getWrtPrc(
			CmdLineHelper_Prc.PrcSelectMode prcSelMode, 
			GCshPrcID prcID, 
			GCshCmdtyID cmdtyID, 
			Helper.DateFormat dateFormat, LocalDate date,
			String isin,
			GnuCashWritableFile gcshFile,
			boolean scriptMode) throws Exception {
		GnuCashWritablePrice prc = null;

	    if ( prcSelMode == CmdLineHelper_Prc.PrcSelectMode.ID ) {
			if ( prcID != null ) {
				prc = gcshFile.getWritablePriceByID(prcID);
				if ( prc == null )
				{
					if ( ! scriptMode )
			          System.err.println("Could not find a price with this ID");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <prcID> is null");
			}
	    } else if ( prcSelMode == CmdLineHelper_Prc.PrcSelectMode.SEC_DATE ) {
			if ( cmdtyID != null &&
				 date != null ) {
				if ( isin.equals("") ) {
					LOGGER.debug("getPrc: security-ID is not set");
					return null;
				}
				prc = gcshFile.getWritablePriceByCmdtyIDDate(cmdtyID, date);
				if ( prc == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find a price for this security-ID and date.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <secID> or <date> is null");
			}
	    } else if ( prcSelMode == CmdLineHelper_Prc.PrcSelectMode.ISIN_DATE ) {
			if ( isin != null &&
				 date != null ) {
				if ( isin.equals("") ) {
					LOGGER.debug("getPrc: ISIN is empty");
					return null;
				}
				GnuCashCommodity sec = gcshFile.getCommodityByXCode(isin.toString());
				LOGGER.debug("getWrtPrc: Security with ISIN '" + isin + "' is: " + sec.getQualifID());
				prc = gcshFile.getWritablePriceByCmdtyIDDate(sec.getQualifID(), date);
				if ( prc == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find a price for this ISIN and date.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <isin> or <date> is null");
			}
	    }
		
		if ( prc == null ) {
			System.err.println("Something's wrong: Cannot get price.");
		}
		
		return prc;
	}

}
