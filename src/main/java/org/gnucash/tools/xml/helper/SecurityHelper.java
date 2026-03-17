package org.gnucash.tools.xml.helper;

import org.gnucash.apispec.read.GnuCashFileExt;
import org.gnucash.apispec.read.GnuCashSecurity;
import org.gnucash.apispec.write.GnuCashWritableFileExt;
import org.gnucash.apispec.write.GnuCashWritableSecurity;
import org.gnucash.base.basetypes.complex.GCshSecID;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;

public class SecurityHelper
{

	public static GnuCashSecurity getSec(
			Helper.CmdtySecSingleSelMode secSelMode, 
			GCshSecID secID, String isin, String secName, 
			GnuCashFileExt gcshFile,
			boolean scriptMode) throws Exception {
		GnuCashSecurity sec = null;

		if ( secSelMode == Helper.CmdtySecSingleSelMode.ID ) {
			if ( secID != null ) {
				sec = gcshFile.getSecurityByID(secID);
				if ( sec == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find security with this ID");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <secID> is null");
			}
		} else if ( secSelMode == Helper.CmdtySecSingleSelMode.ISIN ) {
			// CAUTION: This branch is *not necessarily* redundant to
			// the Mode.ID / SubMode.SEC_ID_TYPE branch above
			// (it only is in the project's specific test file, which
			// reflects the way the author organizes his data, but by
			// no means is the only "correct", let alone conceivable way).
			if ( isin != null ) {
				sec = gcshFile.getSecurityByXCode(isin);
				if ( sec == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find a security with this ISIN.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <isin> is null");
			}
		} else if ( secSelMode == Helper.CmdtySecSingleSelMode.NAME ) {
			if ( secName != null ) {
				sec = gcshFile.getSecurityByNameUniq(secName);
				if ( sec == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find a security (uniquely) matching this name.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <secName> is null");
			}
		}
		
		if ( sec == null ) {
			System.err.println("Something's wrong: Cannot get security.");
		}
		
		return sec;
	}

	public static GnuCashWritableSecurity getWrtSec(
			Helper.CmdtySecSingleSelMode secSelMode, 
			GCshSecID secID, String isin, String secName, 
			GnuCashWritableFileExt gcshFile,
			boolean scriptMode) throws Exception {
		GnuCashWritableSecurity sec = null;

		if ( secSelMode == Helper.CmdtySecSingleSelMode.ID ) {
			if ( secID != null ) {
				sec = gcshFile.getWritableSecurityByID(secID);
				if ( sec == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find security with this ID");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <secID> is null");
			}
		} else if ( secSelMode == Helper.CmdtySecSingleSelMode.ISIN ) {
			// CAUTION: This branch is *not necessarily* redundant to
			// the Mode.ID / SubMode.SEC_ID_TYPE branch above
			// (it only is in the project's specific test file, which
			// reflects the way the author organizes his data, but by
			// no means is the only "correct", let alone conceivable way).
			if ( isin != null ) {
				sec = gcshFile.getWritableSecurityByXCode(isin);
				if ( sec == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find a security with this ISIN.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <isin> is null");
			}
		} else if ( secSelMode == Helper.CmdtySecSingleSelMode.NAME ) {
			if ( secName != null ) {
				sec = gcshFile.getWritableSecurityByNameUniq(secName);
				if ( sec == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find a security (uniquely) matching this name.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <secName> is null");
			}
		}
		
		if ( sec == null ) {
			System.err.println("Something's wrong: Cannot get security.");
		}
		
		return sec;
	}

}
