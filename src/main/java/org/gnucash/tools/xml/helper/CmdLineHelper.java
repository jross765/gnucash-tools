package org.gnucash.tools.xml.helper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.gnucash.base.basetypes.complex.GCshCmdtyCurrNameSpace;
import org.gnucash.base.basetypes.complex.GCshCmdtyID;
import org.gnucash.base.basetypes.complex.GCshCmdtyID_Exchange;
import org.gnucash.base.basetypes.complex.GCshCmdtyID_MIC;
import org.gnucash.base.basetypes.complex.GCshCmdtyID_SecIdType;
import org.gnucash.base.basetypes.simple.GCshAcctID;
import org.gnucash.base.tuples.AcctIDAmountPair;

import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.dateutils.DateHelpers;
import xyz.schnorxoborx.base.dateutils.LocalDateHelpers;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class CmdLineHelper
{
  public enum CmdtySelectMode
  {
    ID,
    ISIN,
    NAME
  }
	  
  public enum CmdtySelectSubMode // for <cmdty-select-mode> = 'ID' only
  {
    EXCHANGE_TICKER,
    MIC,
    SEC_ID_TYPE
  }

  public enum PrcSelectMode
  {
    ID,
    CMDTY_DATE
  }

  // -----------------------------------------------------------------

  // ::MAGIC
  public  static final String ACCT_AMT_DUMMY_ARG = "DUMMY";
  private static final String ACCT_AMT_SEP_OUTER = "\\|";
  private static final String ACCT_AMT_SEP_INNER = ";";

  // -----------------------------------------------------------------
  
  public static Helper.DateFormat getDateFormat(CommandLine cmdLine, String argName) throws InvalidCommandLineArgsException
  {
    Helper.DateFormat dateFormat;
    
    if ( cmdLine.hasOption(argName) )
    {
      try
      {
        dateFormat = Helper.DateFormat.valueOf(cmdLine.getOptionValue(argName));
      }
      catch (Exception exc)
      {
        System.err.println("Error: Could not parse <" + argName + ">");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      dateFormat = Helper.DateFormat.ISO_8601;
    }
    
    return dateFormat;
  }

  public static Helper.DateFormat getDateFormat(String arg, String argName) throws InvalidCommandLineArgsException
  {
    Helper.DateFormat dateFormat;
    
    if ( arg != null )
    {
      try
      {
        dateFormat = Helper.DateFormat.valueOf(arg);
      }
      catch (Exception exc)
      {
        System.err.println("Error: Could not parse <" + argName + ">");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      dateFormat = Helper.DateFormat.ISO_8601;
    }
    
    return dateFormat;
  }
  
  // ------------------------------

  public static LocalDate getDate(CommandLine cmdLine, String argName,
		  						  Helper.DateFormat dateFormat) throws InvalidCommandLineArgsException
  {
    LocalDate datum = LocalDate.now();
    
    try
    {
      if ( dateFormat == Helper.DateFormat.ISO_8601 )
        datum = LocalDateHelpers.parseLocalDate(cmdLine.getOptionValue(argName), DateHelpers.DATE_FORMAT_2);
      else if ( dateFormat == Helper.DateFormat.DE )
        datum = LocalDateHelpers.parseLocalDate(cmdLine.getOptionValue(argName));
    }
    catch (Exception exc)
    {
      System.err.println("Error: Could not parse <" + argName + ">");
      throw new InvalidCommandLineArgsException();
    }
    
    return datum;
  }

  public static LocalDate getDate(String arg, String argName,
			  					  Helper.DateFormat dateFormat) throws InvalidCommandLineArgsException
	{
		LocalDate datum = LocalDate.now();

		try
		{
			if ( dateFormat == Helper.DateFormat.ISO_8601 )
				datum = LocalDateHelpers.parseLocalDate( arg, DateHelpers.DATE_FORMAT_2 );
			else if ( dateFormat == Helper.DateFormat.DE )
				datum = LocalDateHelpers.parseLocalDate( arg );
		} catch ( Exception exc )
		{
			System.err.println( "Error: Could not parse <" + argName + ">" );
			throw new InvalidCommandLineArgsException();
		}

		return datum;
	}

  // -----------------------------------------------------------------
  
  public static Collection<AcctIDAmountPair> getExpAcctAmtMulti(CommandLine cmdLine, String argName) throws InvalidCommandLineArgsException
  {
    List<AcctIDAmountPair> result = new ArrayList<AcctIDAmountPair>();

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

  public static Collection<AcctIDAmountPair> getExpAcctAmtMulti(String arg, String argName) throws InvalidCommandLineArgsException
  {
    List<AcctIDAmountPair> result = new ArrayList<AcctIDAmountPair>();

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
        		AcctIDAmountPair newPair = getExpAcctAmtSingle( pairStr );
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

  private static AcctIDAmountPair getExpAcctAmtSingle(String pairStr) throws InvalidCommandLineArgsException
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
        		
	AcctIDAmountPair newPair = new AcctIDAmountPair(acctID, new FixedPointNumber(amtDbl));

    return newPair;
  }
  
  // -----------------------------------------------------------------

  // For CmdtySelectMode = ID only!
  public static GCshCmdtyID getCmdtyID(CommandLine cmdLine,
		  							   CmdtySelectMode mode, CmdtySelectSubMode subMode,
		  							   boolean scriptMode) throws InvalidCommandLineArgsException
  {
	  return getCmdtyID(cmdLine,
			  			mode, subMode,
			  			"exchange", "ticker",
			  			"mic", "mic-id",
			  			"secid-type", "isin",
			  			scriptMode);
  }
  
  // For CmdtySelectMode = ID only!
  public static GCshCmdtyID getCmdtyID(CommandLine cmdLine,
		                               CmdtySelectMode mode, CmdtySelectSubMode subMode,
		  						       String exchArgName, String tickerArgName,
		  						       String micArgName, String micIDArgName,
		  						       String secIDTypeArgName, String isinArgName,
		  						       boolean scriptMode) throws InvalidCommandLineArgsException
  {
	if ( mode == null )
	{
		throw new IllegalArgumentException("arg <mode> is null");
	}
		
	if ( mode != CmdtySelectMode.ID )
	{
		throw new IllegalArgumentException("arg <mode> must be " + CmdtySelectMode.ID);
	}
	
	if ( subMode == null )
	{
		throw new IllegalArgumentException("arg <sub-mode> is null");
	}
	
    GCshCmdtyCurrNameSpace.Exchange  exch      = null;
    String                           ticker    = null;
    GCshCmdtyCurrNameSpace.MIC       mic       = null;
    String                           micID     = null;
    GCshCmdtyCurrNameSpace.SecIdType secIDType = null;
    String                           isin      = null;
    
	// <exchange>, <ticker>
    if ( cmdLine.hasOption(exchArgName) )
    {
      if ( ! cmdLine.hasOption(tickerArgName) ) 
      {
        System.err.println("Error: <" + exchArgName + "> and <" + tickerArgName + "> must both either be set or unset");
        throw new InvalidCommandLineArgsException();
      }

      if ( cmdLine.hasOption(micArgName) ) 
      {
        System.err.println("Error: <" + exchArgName + "> and <" + micArgName + "> are mutually exclusive");
        throw new InvalidCommandLineArgsException();
      }

      if ( cmdLine.hasOption(secIDTypeArgName) ) 
      {
        System.err.println("Error: <" + exchArgName + "> and <" + secIDTypeArgName + "> are mutually exclusive");
        throw new InvalidCommandLineArgsException();
      }

      if ( cmdLine.hasOption(isinArgName) ) 
      {
        System.err.println("Error: <" + exchArgName + "> and <" + isinArgName + "> are mutually exclusive");
        throw new InvalidCommandLineArgsException();
      }

//      if ( cmdLine.hasOption("name") ) 
//      {
//        System.err.println("Error: <" + exchArgName + "> and <name> are mutually exclusive");
//        throw new InvalidCommandLineArgsException();
//      }

      if ( ! ( mode    == CmdtySelectMode.ID &&
               subMode == CmdtySelectSubMode.EXCHANGE_TICKER ) )
      {
        System.err.println("<" + exchArgName + "> and <" + tickerArgName + "> must only be set with " +
                           "<mode> = '" + CmdtySelectMode.ID + "' and " +
                           "<sub-mode> = '" + CmdtySelectSubMode.EXCHANGE_TICKER + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        exch = GCshCmdtyCurrNameSpace.Exchange.valueOf( cmdLine.getOptionValue(exchArgName) );
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <" + exchArgName + ">");
        throw new InvalidCommandLineArgsException();
      }

      try
      {
        ticker = cmdLine.getOptionValue(tickerArgName);
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <ticker>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( cmdLine.hasOption(tickerArgName) )
      {
        System.err.println("Error: <" + exchArgName + "> and <" + tickerArgName + "> must both either be set or unset");
        throw new InvalidCommandLineArgsException();
      }
      
      if ( mode    == CmdtySelectMode.ID &&
           subMode == CmdtySelectSubMode.EXCHANGE_TICKER )
      {
    	  System.err.println("<" + exchArgName + "> and <" + tickerArgName + "> must only be set with " +
                             "<mode> = '" + CmdtySelectMode.ID + "' and " +
                             "<sub-mode> = '" + CmdtySelectSubMode.EXCHANGE_TICKER + "'");
    	  throw new InvalidCommandLineArgsException();
      }
    }

    if ( ! scriptMode )
    {
      System.err.println("Exchange:     " + exch);
      System.err.println("Ticker:       '" + ticker + "'");
    }

    // <mic>, <mic-id>
    if ( cmdLine.hasOption(micArgName) )
    {
      if ( ! cmdLine.hasOption(micIDArgName) ) 
      {
        System.err.println("Error: <" + micArgName + "> and <" + micIDArgName + "> must both either be set or unset");
        throw new InvalidCommandLineArgsException();
      }

      if ( cmdLine.hasOption(exchArgName) ) 
      {
        System.err.println("Error: <" + micArgName + "> and <" + exchArgName + "> are mutually exclusive");
        throw new InvalidCommandLineArgsException();
      }

      if ( cmdLine.hasOption(secIDTypeArgName) ) 
      {
        System.err.println("Error: <" + micArgName + "> and <" + secIDTypeArgName + "> are mutually exclusive");
        throw new InvalidCommandLineArgsException();
      }

      if ( cmdLine.hasOption(isinArgName) ) 
      {
        System.err.println("Error: <" + micArgName + "> and <" + isinArgName + "> are mutually exclusive");
        throw new InvalidCommandLineArgsException();
      }

//      if ( cmdLine.hasOption("name") ) 
//      {
//        System.err.println("Error: <" + micArgName + "> and <name> are mutually exclusive");
//        throw new InvalidCommandLineArgsException();
//      }

      if ( ! ( mode    == CmdtySelectMode.ID &&
               subMode == CmdtySelectSubMode.MIC ) )
      {
        System.err.println("<" + micArgName + "> and <" + micIDArgName + "> must only be set with " +
                           "<mode> = '" + CmdtySelectMode.ID + "' and " +
                           "<sub-mode> = '" + CmdtySelectSubMode.MIC + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        mic = GCshCmdtyCurrNameSpace.MIC.valueOf( cmdLine.getOptionValue(micArgName) );
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <" + micArgName + ">");
        throw new InvalidCommandLineArgsException();
      }

      try
      {
        micID = cmdLine.getOptionValue(micIDArgName);
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <" + micIDArgName + ">");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( cmdLine.hasOption(micIDArgName) )
      {
        System.err.println("Error: <" + micArgName + "> and <" + micIDArgName + "> must both either be set or unset");
        throw new InvalidCommandLineArgsException();
      }
      
      if ( mode    == CmdtySelectMode.ID &&
           subMode == CmdtySelectSubMode.MIC )
      {
    	  System.err.println("<" + micArgName + "> and <" + micIDArgName + "> must be set with " +
                             "<mode> = '" + CmdtySelectMode.ID + "' and " +
                             "<sub-mode> = '" + CmdtySelectSubMode.MIC + "'");
    	  throw new InvalidCommandLineArgsException();
      }
    }

    if ( ! scriptMode )
    {
      System.err.println("MIC:          " + mic);
      System.err.println("MIC-ID:       '" + micID + "'");
    }

    // <secid-type>, <isin> ::TODO: and possibly later wkn, cusip, sedol 
    if ( cmdLine.hasOption(secIDTypeArgName) )
    {
      if ( ! cmdLine.hasOption(isinArgName) ) 
      {
        System.err.println("Error: <" + secIDTypeArgName + "> and <" + isinArgName + "> must both either be set or unset");
        throw new InvalidCommandLineArgsException();
      }

      if ( cmdLine.hasOption(exchArgName) ) 
      {
        System.err.println("Error: <" + secIDTypeArgName + "> and <" + exchArgName + "> are mutually exclusive");
        throw new InvalidCommandLineArgsException();
      }

      if ( cmdLine.hasOption(micArgName) ) 
      {
        System.err.println("Error: <" + secIDTypeArgName + "> and <" + micArgName + "> are mutually exclusive");
        throw new InvalidCommandLineArgsException();
      }

//      if ( cmdLine.hasOption("name") ) 
//      {
//        System.err.println("Error: <" + secIDTypeArgName + "> and <name> are mutually exclusive");
//        throw new InvalidCommandLineArgsException();
//      }

      if ( ! ( mode    == CmdtySelectMode.ID &&
               subMode == CmdtySelectSubMode.SEC_ID_TYPE ) )
      {
        System.err.println("<" + secIDTypeArgName + "> and <" + isinArgName + "> must only be set with " +
                           "<mode> = '" + CmdtySelectMode.ID + "' and " +
                           "<sub-mode> = '" + CmdtySelectSubMode.SEC_ID_TYPE + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        secIDType = GCshCmdtyCurrNameSpace.SecIdType.valueOf( cmdLine.getOptionValue(secIDTypeArgName) );
        if ( secIDType != GCshCmdtyCurrNameSpace.SecIdType.ISIN )
        {
            System.err.println("Only <secid-type> = " + GCshCmdtyCurrNameSpace.SecIdType.ISIN + " is supported at the moment");
            throw new InvalidCommandLineArgsException();
        }
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <" + secIDTypeArgName + ">");
        throw new InvalidCommandLineArgsException();
      }

      try
      {
        isin = cmdLine.getOptionValue(isinArgName);
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <" + isinArgName + ">");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( cmdLine.hasOption(isinArgName) )
      {
        System.err.println("Error: <" + secIDTypeArgName + "> and <" + isinArgName + "> must both either be set or unset");
        throw new InvalidCommandLineArgsException();
      }

      if ( mode    == CmdtySelectMode.ID &&
    	   subMode == CmdtySelectSubMode.SEC_ID_TYPE )
      {
    	  System.err.println("<" + secIDTypeArgName + "> and <" + isinArgName + "> must be set with " +
                             "<mode> = '" + CmdtySelectMode.ID + "' and " +
                             "<sub-mode> = '" + CmdtySelectSubMode.SEC_ID_TYPE + "'");
    	  throw new InvalidCommandLineArgsException();
      }
    }
    
    if ( ! scriptMode )
    {
      System.err.println("Sec. ID type: " + secIDType);
      System.err.println("ISIN:         '" + isin + "'");
    }

    return getCmdtyIDCore( subMode, 
    				   	   exch, ticker, 
    				   	   mic, micID, 
    				   	   secIDType, isin);
  }
  
  private static GCshCmdtyID getCmdtyIDCore(
		  CmdtySelectSubMode subMode,
	      GCshCmdtyCurrNameSpace.Exchange exch, String ticker,
	      GCshCmdtyCurrNameSpace.MIC mic, String micID,
	      GCshCmdtyCurrNameSpace.SecIdType secIDType, String isin)
  {
		GCshCmdtyID cmdtyID = null;
	    
	    if ( subMode == CmdtySelectSubMode.EXCHANGE_TICKER ) 
	    {
	    	cmdtyID = new GCshCmdtyID_Exchange( exch, ticker );
	    }
	    else if ( subMode == CmdtySelectSubMode.MIC )
	    {
	    	cmdtyID = new GCshCmdtyID_MIC( mic, micID );
	    }
	    else if ( subMode == CmdtySelectSubMode.SEC_ID_TYPE )
	    {
	    	// ::TODO / possibly later: variants for wkn, cusip, sedol
	    	// 
	    	// CAUTION: This branch is *not necessarily* redundant to
	    	// the Mode.ISIN branch in the calling program 
	    	// (It only is in the project's specific test file, which 
	    	// reflects the way the author organizes his data, but by 
	    	// no means is the only "correct", let alone conceivable way).
	    	cmdtyID = new GCshCmdtyID_SecIdType( secIDType, isin );
	    }
	    
	    return cmdtyID;
  }

}
