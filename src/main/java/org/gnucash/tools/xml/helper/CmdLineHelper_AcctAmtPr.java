package org.gnucash.tools.xml.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.numbers.fraction.BigFraction;
import org.gnucash.base.basetypes.simple.GCshAcctID;
import org.gnucash.base.tuples.AcctIDAmountBFPair;
import org.gnucash.tools.Const;

import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class CmdLineHelper_AcctAmtPr {

  // ::MAGIC
  public  static final String ACCT_AMT_DUMMY_ARG = "DUMMY";
  private static final String ACCT_AMT_SEP_OUTER = "\\|";
  private static final String ACCT_AMT_SEP_INNER = ";";

  // -----------------------------------------------------------------
  
  public static Collection<AcctIDAmountBFPair> getExpAcctAmtMulti(CommandLine cmdLine, String argName) throws InvalidCommandLineArgsException
  {
    List<AcctIDAmountBFPair> result = new ArrayList<AcctIDAmountBFPair>();

    if ( cmdLine.hasOption(argName) )
    {
       	String arg = cmdLine.getOptionValue(argName);
   	    // System.err.println("*** expacctamt: '" + arg + "' ***");
       	return getExpAcctAmtMulti(arg, argName);
    }
    else
    {
    	// ::EMPTY
    }

    return result;
  }

  public static Collection<AcctIDAmountBFPair> getExpAcctAmtMulti(String arg, String argName) throws InvalidCommandLineArgsException
  {
    List<AcctIDAmountBFPair> result = new ArrayList<AcctIDAmountBFPair>();

    if ( arg == null )
    	return result;
    
    if ( arg.trim().equals( "" ) ||
    	 arg.equals( ACCT_AMT_DUMMY_ARG ) )
    {
    	return result;
    }
    
    try
    {
    	String[] pairListArr = arg.split(ACCT_AMT_SEP_OUTER);
    	// System.err.println("*** arr-size: " + pairListArr.length);
    	for ( String pairStr : pairListArr )
    	{
    		// System.err.println("*** pair: '" + pairStr + "'");
    		if ( ! pairStr.trim().equals( "" ) )
    		{
        		AcctIDAmountBFPair newPair = getExpAcctAmtSingle( pairStr );
        		result.add(newPair);
    		}
    	}
   	}
   	catch (Exception e)
   	{
   		System.err.println("Could not parse <" + argName + ">");
   		throw new InvalidCommandLineArgsException();
   	}

    return result;
  }

  private static AcctIDAmountBFPair getExpAcctAmtSingle(String pairStr) throws InvalidCommandLineArgsException
  {
	int pos = pairStr.indexOf(ACCT_AMT_SEP_INNER);
	if ( pos < 0 )
	{
		System.err.println("Error: List element '" + pairStr + "' does not contain the separator");
		throw new InvalidCommandLineArgsException();
	}
	String acctIDStr = pairStr.substring(0, pos);
	String amtStr    = pairStr.substring(pos + 1);
	// System.err.println(" - elt1: '" + acctIDStr + "'/'" + amtStr + "'");
        		
	GCshAcctID acctID = new GCshAcctID(acctIDStr);
	Double amtDbl = Double.valueOf(amtStr);
	// System.err.println(" - elt2: " + acctIDStr + " / " + amtStr);
        		
	AcctIDAmountBFPair newPair = new AcctIDAmountBFPair(acctID, BigFraction.from(amtDbl, Const.EPS, Const.ITER_MAX));

    return newPair;
  }
  
}

