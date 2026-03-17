package org.gnucash.tools.xml.helper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.gnucash.base.basetypes.complex.GCshCmdtyNameSpace;
import org.gnucash.base.basetypes.complex.GCshSecID;
import org.gnucash.base.basetypes.complex.GCshSecID_Exchange;
import org.gnucash.base.basetypes.complex.GCshSecID_MIC;
import org.gnucash.base.basetypes.complex.GCshSecID_SecIdType;
import org.gnucash.base.basetypes.simple.GCshAcctID;
import org.gnucash.base.tuples.AcctIDAmountFPPair;

import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.dateutils.LocalDateHelpers;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class CmdLineHelper
{
  public enum SecSelectSubMode // for <sec-select-mode> = 'ID' only
  {
	DIRECT,
    INDIRECT_EXCHANGE_TICKER,
    INDIRECT_MIC,
    INDIRECT_SEC_ID_TYPE
  }

  public enum PrcSelectMode
  {
    ID,
    SEC_DATE
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
      dateFormat = Helper.DateFormat.ISO;
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
      dateFormat = Helper.DateFormat.ISO;
    }
    
    return dateFormat;
  }
  
  // ------------------------------

  public static LocalDate getDate(CommandLine cmdLine, String argName,
		  						  Helper.DateFormat dateFmt) throws InvalidCommandLineArgsException
  {
    LocalDate datum = LocalDate.now();
    
    try
    {
      datum = LocalDateHelpers.parseLocalDate(cmdLine.getOptionValue(argName), dateFmt);
    }
    catch (Exception exc)
    {
      System.err.println("Error: Could not parse <" + argName + ">");
      throw new InvalidCommandLineArgsException();
    }
    
    return datum;
  }

  public static LocalDate getDate(String arg, String argName,
			  					  Helper.DateFormat dateFmt) throws InvalidCommandLineArgsException
	{
		LocalDate datum = LocalDate.now();

		try
		{
			datum = LocalDateHelpers.parseLocalDate( arg, dateFmt);
		} catch ( Exception exc )
		{
			System.err.println( "Error: Could not parse <" + argName + ">" );
			throw new InvalidCommandLineArgsException();
		}

		return datum;
	}

  // -----------------------------------------------------------------
  
  public static Collection<AcctIDAmountFPPair> getExpAcctAmtMulti(CommandLine cmdLine, String argName) throws InvalidCommandLineArgsException
  {
    List<AcctIDAmountFPPair> result = new ArrayList<AcctIDAmountFPPair>();

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

  public static Collection<AcctIDAmountFPPair> getExpAcctAmtMulti(String arg, String argName) throws InvalidCommandLineArgsException
  {
    List<AcctIDAmountFPPair> result = new ArrayList<AcctIDAmountFPPair>();

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
        		AcctIDAmountFPPair newPair = getExpAcctAmtSingle( pairStr );
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

  private static AcctIDAmountFPPair getExpAcctAmtSingle(String pairStr) throws InvalidCommandLineArgsException
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
        		
	AcctIDAmountFPPair newPair = new AcctIDAmountFPPair(acctID, new FixedPointNumber(amtDbl));

    return newPair;
  }
  
  // -----------------------------------------------------------------

  // For SecSelectMode = ID only!
  public static GCshSecID getSecID_direct(
		  CommandLine cmdLine,
		  CmdtySecSingleSelMode secSelmode, PrcSelectMode prcSelmode,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  return getSecID_direct(cmdLine,
			  secSelmode, prcSelmode,
			  "sec-sel-mode", "prc-sel-mode",
			  "security-id",
			  scriptMode );
  }
  
  // For SecSelectMode = ID only!
  public static GCshSecID getSecID_direct(
		  CommandLine cmdLine,
		  CmdtySecSingleSelMode secSelmode, PrcSelectMode prcSelmode,
		  String secSelModeArgName, String prcSelModeArgName,
		  String secIDArgName,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  GCshSecID secID = null;
	    
	  // < secSelModeArgName >
	  if ( cmdLine.hasOption( secIDArgName ) )
	  {
		  if ( secSelmode != CmdtySecSingleSelMode.ID )
		  {
			  System.err.println("<" + secIDArgName + "> may only be set with <" + secSelModeArgName + "> = " + CmdtySecSingleSelMode.ID);
			  throw new InvalidCommandLineArgsException();
		  }

		  if ( prcSelmode != null )
		  {
			  if ( prcSelmode != CmdLineHelper.PrcSelectMode.SEC_DATE )
			  {
				  System.err.println("<" + secIDArgName+ "> may only be set with <" + prcSelModeArgName + "> = " + CmdLineHelper.PrcSelectMode.SEC_DATE);
				  throw new InvalidCommandLineArgsException();
			  }
		  }
	    		
		  try
		  {
			  secID = GCshSecID.parse( cmdLine.getOptionValue(secIDArgName) ); 
		  }	
		  catch ( Exception exc )
		  {
			  System.err.println("Could not parse <" + secIDArgName+ ">");
	          throw new InvalidCommandLineArgsException();
		  }
	  }
	  else
	  {
		  if ( secSelmode == CmdtySecSingleSelMode.ID )
		  {
			  System.err.println("<" + secIDArgName+ "> must be set with <" + secSelModeArgName + "> = " + CmdtySecSingleSelMode.ID);
			  throw new InvalidCommandLineArgsException();
		  }

		  if ( prcSelmode != null )
		  {
			  if ( prcSelmode == CmdLineHelper.PrcSelectMode.SEC_DATE )
			  {
				  System.err.println("<" + secIDArgName+ "> must be set with <" + prcSelModeArgName + "> = " + CmdLineHelper.PrcSelectMode.SEC_DATE);
				  throw new InvalidCommandLineArgsException();
			  }
		  }
	  }
	  
	  if ( ! scriptMode )
		  System.err.println("Security ID (direct): " + secID);
	  
	  return secID;
  }
  
  // For SecSelectMode = ID only!
  public static GCshSecID getSecID_indirect(CommandLine cmdLine,
		  						CmdtySecSingleSelMode mode, SecSelectSubMode subMode,
		  						boolean scriptMode) throws InvalidCommandLineArgsException
  {
	  return getSecID_indirect(cmdLine,
			  			mode, subMode,
			  			"exchange", "ticker",
			  			"mic", "mic-id",
			  			"secid-type", "isin",
			  			scriptMode);
  }
  
  // For CmdtySelectMode = ID only!
  public static GCshSecID getSecID_indirect(CommandLine cmdLine,
		  						CmdtySecSingleSelMode mode, SecSelectSubMode subMode,
		  						String exchArgName, String tickerArgName,
		  						String micArgName, String micIDArgName,
		  						String secIDTypeArgName, String isinArgName,
		  						boolean scriptMode) throws InvalidCommandLineArgsException
  {
	if ( mode == null )
	{
		throw new IllegalArgumentException("arg <sec-sel-mode> is null");
	}
		
	if ( mode != CmdtySecSingleSelMode.ID )
	{
		throw new IllegalArgumentException("arg <sec-sel-mode> must be " + CmdtySecSingleSelMode.ID);
	}
	
	if ( subMode == null )
	{
		throw new IllegalArgumentException("arg <sec-sel-sub-mode> is null");
	}
	
    GCshCmdtyNameSpace.Exchange  exch      = null;
    String                       ticker    = null;
    GCshCmdtyNameSpace.MIC       mic       = null;
    String                       micID     = null;
    GCshCmdtyNameSpace.SecIdType secIDType = null;
    String                       isin      = null;
    
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

      if ( ! ( mode    == CmdtySecSingleSelMode.ID &&
               subMode == SecSelectSubMode.INDIRECT_EXCHANGE_TICKER ) )
      {
        System.err.println("<" + exchArgName + "> and <" + tickerArgName + "> must only be set with " +
                           "<sec-sel-mode> = '" + CmdtySecSingleSelMode.ID + "' and " +
                           "<sec-sel-sub-mode> = '" + SecSelectSubMode.INDIRECT_EXCHANGE_TICKER + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        exch = GCshCmdtyNameSpace.Exchange.valueOf( cmdLine.getOptionValue(exchArgName) );
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
      
      if ( mode    == CmdtySecSingleSelMode.ID &&
           subMode == SecSelectSubMode.INDIRECT_EXCHANGE_TICKER )
      {
    	  System.err.println("<" + exchArgName + "> and <" + tickerArgName + "> must only be set with " +
                             "<sec-sel-mode> = '" + CmdtySecSingleSelMode.ID + "' and " +
                             "<sec-sel-sub-mode> = '" + SecSelectSubMode.INDIRECT_EXCHANGE_TICKER + "'");
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

      if ( ! ( mode    == CmdtySecSingleSelMode.ID &&
               subMode == SecSelectSubMode.INDIRECT_MIC ) )
      {
        System.err.println("<" + micArgName + "> and <" + micIDArgName + "> must only be set with " +
                           "<sec-sel-mode> = '" + CmdtySecSingleSelMode.ID + "' and " +
                           "<sec-sel-sub-mode> = '" + SecSelectSubMode.INDIRECT_MIC + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        mic = GCshCmdtyNameSpace.MIC.valueOf( cmdLine.getOptionValue(micArgName) );
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
      
      if ( mode    == CmdtySecSingleSelMode.ID &&
           subMode == SecSelectSubMode.INDIRECT_MIC )
      {
    	  System.err.println("<" + micArgName + "> and <" + micIDArgName + "> must be set with " +
                             "<sec-sel-mode> = '" + CmdtySecSingleSelMode.ID + "' and " +
                             "<sec-sel-sub-mode> = '" + SecSelectSubMode.INDIRECT_MIC + "'");
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

      if ( ! ( mode    == CmdtySecSingleSelMode.ID &&
               subMode == SecSelectSubMode.INDIRECT_SEC_ID_TYPE ) )
      {
        System.err.println("<" + secIDTypeArgName + "> and <" + isinArgName + "> must only be set with " +
                           "<sec-sel-mode> = '" + CmdtySecSingleSelMode.ID + "' and " +
                           "<sec-sel-sub-mode> = '" + SecSelectSubMode.INDIRECT_SEC_ID_TYPE + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        secIDType = GCshCmdtyNameSpace.SecIdType.valueOf( cmdLine.getOptionValue(secIDTypeArgName) );
        if ( secIDType != GCshCmdtyNameSpace.SecIdType.ISIN )
        {
            System.err.println("Only <secid-type> = " + GCshCmdtyNameSpace.SecIdType.ISIN + " is supported at the moment");
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

      if ( mode    == CmdtySecSingleSelMode.ID &&
    	   subMode == SecSelectSubMode.INDIRECT_SEC_ID_TYPE )
      {
    	  System.err.println("<" + secIDTypeArgName + "> and <" + isinArgName + "> must be set with " +
                             "<sec-sel-mode> = '" + CmdtySecSingleSelMode.ID + "' and " +
                             "<sec-sel-sub-mode> = '" + SecSelectSubMode.INDIRECT_SEC_ID_TYPE + "'");
    	  throw new InvalidCommandLineArgsException();
      }
    }
    
    if ( ! scriptMode )
    {
      System.err.println("Sec. ID type: " + secIDType);
      System.err.println("ISIN:         '" + isin + "'");
    }

    GCshSecID secID = getSecID_indirect_Core(subMode, 
    				   	   exch, ticker, 
    				   	   mic, micID, 
    				   	   secIDType, isin);
    
    if ( ! scriptMode )
    	System.err.println("Security ID (indirect): " + secID);
    
    return secID;
  }
  
  private static GCshSecID getSecID_indirect_Core(
		  SecSelectSubMode subMode,
	      GCshCmdtyNameSpace.Exchange exch, String ticker,
	      GCshCmdtyNameSpace.MIC mic, String micID,
	      GCshCmdtyNameSpace.SecIdType secIDType, String isin)
  {
		GCshSecID secID = null;
	    
	    if ( subMode == SecSelectSubMode.INDIRECT_EXCHANGE_TICKER ) 
	    {
	    	secID = new GCshSecID_Exchange( exch, ticker );
	    }
	    else if ( subMode == SecSelectSubMode.INDIRECT_MIC )
	    {
	    	secID = new GCshSecID_MIC( mic, micID );
	    }
	    else if ( subMode == SecSelectSubMode.INDIRECT_SEC_ID_TYPE )
	    {
	    	// ::TODO / possibly later: variants for wkn, cusip, sedol
	    	// 
	    	// CAUTION: This branch is *not necessarily* redundant to
	    	// the Mode.ISIN branch in the calling program 
	    	// (It only is in the project's specific test file, which 
	    	// reflects the way the author organizes his data, but by 
	    	// no means is the only "correct", let alone conceivable way).
	    	
	    	secID = new GCshSecID_SecIdType( secIDType, isin );
	    }
	    
	    return secID;
  }
  
  // ------------------------------
  
  // For CmdtySecSelectMode = ID only!
  public static void setSecID_direct(GCshSecID secID,
		  						CommandLine cmdLine,
		  						CmdtySecSingleSelMode secSelmode, PrcSelectMode prcSelMode,
		  						boolean scriptMode) throws InvalidCommandLineArgsException
  {
	  GCshSecID locSecID = getSecID_direct(cmdLine,
			  							secSelmode, prcSelMode, 
			  							scriptMode );
	  
	  secID.reset();
	  secID.setType(locSecID.getType());
	  secID.setNameSpace(locSecID.getNameSpace());
	  secID.setCode(locSecID.getCode());
  }
  
  // For CmdtySecSelectMode = ID only!
  public static void setSecID_indirect(GCshSecID secID,
		  						CommandLine cmdLine,
		  						CmdtySecSingleSelMode secSelMode, SecSelectSubMode secSelSubMode,
		  						boolean scriptMode) throws InvalidCommandLineArgsException
  {
	  GCshSecID locSecID = getSecID_indirect(cmdLine, 
			  								secSelMode, secSelSubMode,
			  								scriptMode);
	  
	  secID.reset();
	  secID.setType(locSecID.getType());
	  secID.setNameSpace(locSecID.getNameSpace());
	  secID.setCode(locSecID.getCode());
  }
  
  // ------------------------------
  
  public static void parseSecStuffWrap(
		  CommandLine cmdLine,
		  xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode secSelMode, 
		  CmdLineHelper.SecSelectSubMode secSelSubMode,
		  PrcSelectMode prcSelMode,
		  GCshSecID secID, 
		  StringBuffer ticker, StringBuffer micID, StringBuffer isin, 
		  StringBuffer secName,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  if ( cmdLine.hasOption( "security-id" ) )
	  {
		  parseSecStuffWrap_direct(cmdLine,
				  secSelMode, prcSelMode,
				  secID,
				  scriptMode);
	  }
	  else
	  {
		  parseSecStuffWrap_indirect(cmdLine,
				  secSelMode, secSelSubMode,
				  secID,
				  ticker, micID, isin, secName,
				  scriptMode);
	  }
  }
  
  // For CmdtySelectMode = ID only!
  private static void parseSecStuffWrap_direct(
		  CommandLine cmdLine,
		  xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode secSelmode, 
		  PrcSelectMode prcSelMode, 
		  GCshSecID secID,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  // <security-id>
	  if ( cmdLine.hasOption( "security-id" ) )
	  {
		  if ( secSelmode != xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID )
		  {
			  System.err.println("<security-id> may only be set with <sec-sel-mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID + "'");
	          throw new InvalidCommandLineArgsException();
		  }
	    	
		  if ( prcSelMode != null )
		  {
			  if ( prcSelMode != CmdLineHelper.PrcSelectMode.SEC_DATE )
			  {
				  System.err.println("<security-id> may only be set with <prc-sel-mode> = " + CmdLineHelper.PrcSelectMode.SEC_DATE);
				  throw new InvalidCommandLineArgsException();
			  }
		  }
	    		
		  try
		  {
			  // No:
			  // secID = GCshSecID.parse( cmdLine.getOptionValue("security-id") );
			  // Instead:
			  setSecID_direct(secID, 
					  cmdLine,
					  secSelmode, prcSelMode,
					  scriptMode);
		  }	
		  catch ( Exception exc )
		  {
			  System.err.println("Could not parse <security-id>");
	          throw new InvalidCommandLineArgsException();
		  }
	  }
	  else
	  {
		  if ( secSelmode == xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID )
		  {
			  System.err.println("<security-id> must be set with <sec-sel-mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID + "'");
	          throw new InvalidCommandLineArgsException();
		  }
	    	
		  if ( prcSelMode != null )
		  {
			  if ( prcSelMode == CmdLineHelper.PrcSelectMode.SEC_DATE )
			  {
				  System.err.println("<security-id> must be set with <prc-sel-mode> = " + CmdLineHelper.PrcSelectMode.SEC_DATE);
				  throw new InvalidCommandLineArgsException();
			  }
		  }
	  }
  }
  
  private static void parseSecStuffWrap_indirect(
		  CommandLine cmdLine,
		  xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode secSelMode, 
		  CmdLineHelper.SecSelectSubMode secSelSubMode,
		  GCshSecID secID, 
		  StringBuffer ticker, StringBuffer micID, StringBuffer isin, 
		  StringBuffer secName,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	    // <security-id>
	    // Well, not directly, but rather:
	    // <exchange>, <ticker>,
	    // <mid>, <mic-id>,
	    // <secid-type>, <isin>
	    if ( ( cmdLine.hasOption("exchange")   && cmdLine.hasOption("ticker") ) ||
	    	 ( cmdLine.hasOption("mic")        && cmdLine.hasOption("mic-id") ) ||
	    	 ( cmdLine.hasOption("secid-type") && cmdLine.hasOption("isin") ) )
	    {
	        if ( secSelMode != xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID )
	        {
	          System.err.println("Pair <exchange>/<ticker>, <mic>/<mic-id>, <secid-type>/<isin> must only be set with <sec-sel-mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID + "'");
	          throw new InvalidCommandLineArgsException();
	        }
	    	
	        if ( secSelSubMode != SecSelectSubMode.INDIRECT_EXCHANGE_TICKER && 
	        	 secSelSubMode != SecSelectSubMode.INDIRECT_MIC && 
	        	 secSelSubMode != SecSelectSubMode.INDIRECT_SEC_ID_TYPE )
	        {
	          System.err.println("Pair <exchange>/<ticker>, <mic>/<mic-id>, <secid-type>/<isin> must only be set with <sec-sel-sub-mode> = '" + 
	      	        SecSelectSubMode.INDIRECT_EXCHANGE_TICKER + "', " +
	    	        SecSelectSubMode.INDIRECT_MIC + "' or " +
	    	        SecSelectSubMode.INDIRECT_SEC_ID_TYPE + "', ");
	          throw new InvalidCommandLineArgsException();
	        }

	        // No:
//	    	secID = getSecID_ByID(cmdLine,
//	    				mode, subMode,
//	    				scriptMode);
	    	// Instead:
	        setSecID_indirect(secID, 
	        				cmdLine,
	        				secSelMode, secSelSubMode,
	        				scriptMode);
	    	
	    	if ( secID == null )
	    	{
	            System.err.println("Could not get security ID from " + 
	            				   "<exchange>/<ticker> nor from" + 
	            				   "<mic>/<mic-id> nor from" + 
	            				   "<secid-type>/<isin>");
	            throw new InvalidCommandLineArgsException();
	    	}
	    }
	    else
	    {
	        if ( secSelMode == xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID )
	        {
	          System.err.println("Pair <exchange>/<ticker>, <mic>/<mic-id>, <secid-type>/<isin> must be set with <sec-sel-mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID + "'");
	          throw new InvalidCommandLineArgsException();
	        }
	    	
	        if ( secSelSubMode == SecSelectSubMode.INDIRECT_SEC_ID_TYPE )
	        {
	          System.err.println("Pair <exchange>/<ticker>, <mic>/<mic-id>, <secid-type>/<isin> must be set with <sec-sel-sub-mode> = '" + SecSelectSubMode.INDIRECT_SEC_ID_TYPE + "'");
	          throw new InvalidCommandLineArgsException();
	        }
	    	
	    	if ( ! cmdLine.hasOption("isin") && 
	    		 ! cmdLine.hasOption("name") )
	       	{
	               System.err.println("One of the following must be set:\n" + 
	            		   			  " - <exchange>/<ticker> (pair) xor\n"+ 
	            		   			  " - <mic>/<mic-id> (pair) xor\n"+ 
	               				   	  " - <secid-type>/<isin> (pair) xor\n"+ 
	               				   	  " - <isin> (alone) xor\n"+ 
	    				   	  		  " - <name>");
	               throw new InvalidCommandLineArgsException();
	       	}
	    }

	    // ---------
	  	
	    // <isin> (alone)
	    if ( cmdLine.hasOption("isin") && 
	    	 ! cmdLine.hasOption("secid-type") )
	    {
	        if ( cmdLine.hasOption("exchange") ) 
	        {
	          System.err.println("Error: <isin> and <exchange> are mutually exclusive");
	          throw new InvalidCommandLineArgsException();
	        }

	        if ( cmdLine.hasOption("mic") ) 
	        {
	          System.err.println("Error: <isin> and <mic> are mutually exclusive");
	          throw new InvalidCommandLineArgsException();
	        }

	        if ( cmdLine.hasOption("name") ) 
	        {
	          System.err.println("Error: <isin> and <name> are mutually exclusive");
	          throw new InvalidCommandLineArgsException();
	        }

	      if ( secSelMode != xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN ||
	    	   cmdLine.hasOption("secid-type") )
	      {
	        System.err.println("<isin> (alone) must only be set with <sec-sel-mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN + "'");
	        throw new InvalidCommandLineArgsException();
	      }
	      
	      try
	      {
	        isin.append( cmdLine.getOptionValue("isin") );
	      }
	      catch (Exception exc)
	      {
	        System.err.println("Could not parse <isin>");
	        throw new InvalidCommandLineArgsException();
	      }
	    }
	    else
	    {
//	    	if ( ! cmdLine.hasOption("name") )
//	    	{
//	                  System.err.println("One of the following must be set:\n" + 
//	               		   			  " - <exchange>/<ticker> (pair) xor\n"+ 
//	               		   			  " - <mic>/<mic-id> (pair) xor\n"+ 
//	                  				   	  " - <secid-type>/<isin> (pair) xor\n"+ 
//	                  				   	  " - <isin> (alone) xor\n"+ 
//	       				   	  		  " - <name>");
//	                  throw new InvalidCommandLineArgsException();
//	    	}
	    	
	      if ( secSelMode == xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN &&
	    	   ! cmdLine.hasOption("secid-type") )
	      {
	        System.err.println("<isin> (alone) must be set with <sec-sel-mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN + "'");
	        throw new InvalidCommandLineArgsException();
	      }
	    }

	    if ( ! scriptMode && 
	    	 secSelMode != xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID )
	      System.err.println("ISIN:         '" + isin + "'");
	  	
	  	// ---------
	  	
	    // <name>
	    if ( secName != null )
	    {
		    if ( cmdLine.hasOption("name") )
		    {
		      if ( cmdLine.hasOption("exchange") ) 
		      {
		        System.err.println("Error: <name> and <exchange> are mutually exclusive");
		        throw new InvalidCommandLineArgsException();
		      }

		      if ( cmdLine.hasOption("mic") ) 
		      {
		        System.err.println("Error: <name> and <mic> are mutually exclusive");
		        throw new InvalidCommandLineArgsException();
		      }

		      if ( cmdLine.hasOption("secid-type") ) 
		      {
		        System.err.println("Error: <name> and <secid-type> are mutually exclusive");
		        throw new InvalidCommandLineArgsException();
		      }

		      if ( cmdLine.hasOption("isin") ) 
		      {
		        System.err.println("Error: <name> and <isin> are mutually exclusive");
		        throw new InvalidCommandLineArgsException();
		      }

		      if ( secSelMode != xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.NAME )
		      {
		        System.err.println("<name> must only be set with <sec-sel-mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.NAME + "'");
		        throw new InvalidCommandLineArgsException();
		      }
		      
		      try
		      {
		        secName.append( cmdLine.getOptionValue("name") );
		      }
		      catch (Exception exc)
		      {
		        System.err.println("Could not parse <name>");
		        throw new InvalidCommandLineArgsException();
		      }
		    }
		    else
		    {
		      if ( secSelMode == xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.NAME )
		      {
		        System.err.println("<name> must be set with <sec-sel-mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.NAME + "'");
		        throw new InvalidCommandLineArgsException();
		      }
		    }
		    
		    if ( ! scriptMode )
			      System.err.println("Security name: '" + secName + "'");
	    }
  }

}

