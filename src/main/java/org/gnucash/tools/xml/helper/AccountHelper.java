package org.gnucash.tools.xml.helper;

import org.gnucash.api.read.GnuCashAccount;
import org.gnucash.api.read.GnuCashFile;
import org.gnucash.api.write.GnuCashWritableAccount;
import org.gnucash.api.write.GnuCashWritableFile;
import org.gnucash.base.basetypes.simple.GCshAcctID;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;

public class AccountHelper
{

	public static GnuCashAccount getAcct(
			Helper.Mode acctSelMode, 
			GCshAcctID acctID, String acctName, boolean acctNameQualif,
			GnuCashFile gcshFile,
			boolean scriptMode) throws Exception {
		GnuCashAccount acct = null;

	    if ( acctSelMode == Helper.Mode.ID ) {
			if ( acctID != null ) {
				acct = gcshFile.getAccountByID(acctID);
				if ( acct == null )
				{
					if ( ! scriptMode )
			          System.err.println("Could not find an account with this ID");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <acctID> is null");
			}
	    } else if ( acctSelMode == Helper.Mode.NAME ) {
			if ( acctName != null ) {
				acct = gcshFile.getAccountByNameUniq(acctName, acctNameQualif);
				if ( acct == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find an account (uniquely) matching this name.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <acctName> is null");
			}
	    }
		
		if ( acct == null ) {
			System.err.println("Something's wrong: Cannot get account.");
		}
		
		return acct;
	}

	public static GnuCashWritableAccount getWrtAcct(
			Helper.Mode acctSelMode, 
			GCshAcctID acctID, String acctName, 
			GnuCashWritableFile gcshFile,
			boolean scriptMode) throws Exception {
		GnuCashWritableAccount acct = null;

	    if ( acctSelMode == Helper.Mode.ID ) {
			if ( acctID != null ) {
				acct = gcshFile.getWritableAccountByID(acctID);
				if ( acct == null )
				{
					if ( ! scriptMode )
			          System.err.println("Could not find an account with this ID");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <acctID> is null");
			}
	    } else if ( acctSelMode == Helper.Mode.NAME ) {
			if ( acctName != null ) {
				acct = gcshFile.getWritableAccountByNameUniq(acctName, true);
				if ( acct == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find an account (uniquely) matching this name.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <acctName> is null");
			}
	    }
		
		if ( acct == null ) {
			System.err.println("Something's wrong: Cannot get account.");
		}
		
		return acct;
	}

}
