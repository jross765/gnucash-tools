package org.gnucash.tools.xml.helper;

import org.apache.commons.cli.CommandLine;
import org.gnucash.base.basetypes.complex.GCshCmdtyNameSpace;
import org.gnucash.base.basetypes.complex.GCshSecID;
import org.gnucash.base.basetypes.complex.GCshSecID_Exchange;
import org.gnucash.base.basetypes.complex.GCshSecID_MIC;
import org.gnucash.base.basetypes.complex.GCshSecID_SecIdType;

import xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class CmdLineHelper_Sec
{
  public enum SecSelectSubMode // for <sec-select-mode> = 'ID' only
  {
	DIRECT,
    INDIRECT_EXCHANGE_TICKER,
    INDIRECT_MIC,
    INDIRECT_SEC_ID_TYPE
  }

  // -----------------------------------------------------------------

  public static GCshSecID getSecID_direct(
		  CommandLine cmdLine,
		  CmdtySecSingleSelMode secSelmode, CmdLineHelper_Prc.PrcSelectMode prcSelmode,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  return getSecID_direct(cmdLine,
			  secSelmode, prcSelmode,
			  "sec-sel-mode", "prc-sel-mode",
			  "security-id",
			  scriptMode );
  }
  
  public static GCshSecID getSecID_direct(
		  CommandLine cmdLine,
		  CmdtySecSingleSelMode secSelmode, CmdLineHelper_Prc.PrcSelectMode prcSelmode,
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
			  if ( prcSelmode != CmdLineHelper_Prc.PrcSelectMode.SEC_DATE )
			  {
				  System.err.println("<" + secIDArgName+ "> may only be set with <" + prcSelModeArgName + "> = " + CmdLineHelper_Prc.PrcSelectMode.SEC_DATE);
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
			  if ( prcSelmode == CmdLineHelper_Prc.PrcSelectMode.SEC_DATE )
			  {
				  System.err.println("<" + secIDArgName+ "> must be set with <" + prcSelModeArgName + "> = " + CmdLineHelper_Prc.PrcSelectMode.SEC_DATE);
				  throw new InvalidCommandLineArgsException();
			  }
		  }
	  }
	  
	  if ( ! scriptMode )
		  System.err.println("Security ID (direct): " + secID);
	  
	  return secID;
  }
  
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
    
	// < exchArgName >, < tickerArgName >
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
        System.err.println("Could not parse <" + tickerArgName + ">");
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
    
    // < micArgName >, < micIDArgname >
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

    // < secIDTypeArgName >, < isinArgName > ::TODO: and possibly later wkn, cusip, sedol 
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
            System.err.println("Only <" + secIDTypeArgName + "> = " + GCshCmdtyNameSpace.SecIdType.ISIN + " is supported at the moment");
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
  
  public static void setSecID_direct(GCshSecID secID,
		  						CommandLine cmdLine,
		  						CmdtySecSingleSelMode secSelmode, CmdLineHelper_Prc.PrcSelectMode prcSelMode,
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
		  CmdLineHelper_Sec.SecSelectSubMode secSelSubMode,
		  CmdLineHelper_Prc.PrcSelectMode prcSelMode,
		  GCshSecID secID, 
		  StringBuffer ticker, StringBuffer micID, StringBuffer isin, 
		  StringBuffer secName,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  parseSecStuffWrap(cmdLine,
			  			secSelMode, secSelSubMode, prcSelMode,
			  			"secid-type", "security-id", secID,
			  			"exchange", "ticker", ticker,
			  			"mic", "mic-id", micID,
			  			"isin", isin,
			  			"security-name", secName,
			  			scriptMode);
  }
  
  public static void parseSecStuffWrap(
		  CommandLine cmdLine,
		  xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode secSelMode, 
		  CmdLineHelper_Sec.SecSelectSubMode secSelSubMode,
		  CmdLineHelper_Prc.PrcSelectMode prcSelMode,
		  String secIDTypeArgName, String secIDArgName, GCshSecID secID, 
		  String exchArgName, String tickerArgName, StringBuffer ticker, 
		  String micArgName, String micIDArgName, StringBuffer micID, 
		  String isinArgName, StringBuffer isin, 
		  String secNameArgName, StringBuffer secName,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  if ( cmdLine.hasOption(secIDArgName) )
	  {
		  parseSecStuffWrap_direct(cmdLine,
				  secSelMode, prcSelMode,
				  secIDArgName, secID,
				  scriptMode);
	  }
	  else
	  {
		  parseSecStuffWrap_indirect(cmdLine,
				  secSelMode, secSelSubMode,
				  secIDTypeArgName, secIDArgName, secID,
				  exchArgName, tickerArgName, ticker, 
				  micArgName, micIDArgName, micID, 
				  isinArgName, isin, 
				  secNameArgName, secName,
				  scriptMode);
	  }
  }
  
  private static void parseSecStuffWrap_direct(
		  CommandLine cmdLine,
		  xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode secSelmode, 
		  CmdLineHelper_Prc.PrcSelectMode prcSelMode, 
		  GCshSecID secID,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  parseSecStuffWrap_direct(cmdLine,
			  				secSelmode, prcSelMode,
			  				"security-id", secID,
			  				scriptMode);
  }
  
  private static void parseSecStuffWrap_direct(
		  CommandLine cmdLine,
		  xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode secSelmode, 
		  CmdLineHelper_Prc.PrcSelectMode prcSelMode, 
		  String secIDArgName, GCshSecID secID,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  // < secIDArgName >
	  if ( cmdLine.hasOption(secIDArgName) )
	  {
		  if ( secSelmode != xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID )
		  {
			  System.err.println("<" + secIDArgName + "> may only be set with <sec-sel-mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID + "'");
	          throw new InvalidCommandLineArgsException();
		  }
	    	
		  if ( prcSelMode != null )
		  {
			  if ( prcSelMode != CmdLineHelper_Prc.PrcSelectMode.SEC_DATE )
			  {
				  System.err.println("<" + secIDArgName + "> may only be set with <prc-sel-mode> = " + CmdLineHelper_Prc.PrcSelectMode.SEC_DATE);
				  throw new InvalidCommandLineArgsException();
			  }
		  }
	    		
		  try
		  {
			  // No:
			  // secID = GCshSecID.parse( cmdLine.getOptionValue(secIDArgNanme) );
			  // Instead:
			  setSecID_direct(secID, 
					  cmdLine,
					  secSelmode, prcSelMode,
					  scriptMode);
		  }	
		  catch ( Exception exc )
		  {
			  System.err.println("Could not parse <" + secIDArgName + ">");
	          throw new InvalidCommandLineArgsException();
		  }
	  }
	  else
	  {
		  if ( secSelmode == xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID )
		  {
			  System.err.println("<" + secIDArgName + "> must be set with <sec-sel-mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID + "'");
	          throw new InvalidCommandLineArgsException();
		  }
	    	
		  if ( prcSelMode != null )
		  {
			  if ( prcSelMode == CmdLineHelper_Prc.PrcSelectMode.SEC_DATE )
			  {
				  System.err.println("<" + secIDArgName + "> must be set with <prc-sel-mode> = " + CmdLineHelper_Prc.PrcSelectMode.SEC_DATE);
				  throw new InvalidCommandLineArgsException();
			  }
		  }
	  }
  }
  
  private static void parseSecStuffWrap_indirect(
		  CommandLine cmdLine,
		  xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode secSelMode, 
		  CmdLineHelper_Sec.SecSelectSubMode secSelSubMode,
		  GCshSecID secID, 
		  StringBuffer ticker, StringBuffer micID, StringBuffer isin, 
		  StringBuffer secName,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  parseSecStuffWrap_indirect(cmdLine,
			  					secSelMode, secSelSubMode,
			  					"secid-type", "security-id", secID,
			  					"exchange", "ticker", ticker,
			  					"mic", "mic-id", micID,
			  					"isin", isin,
			  					"security-name", secName,
			  					scriptMode);
  }
  
  private static void parseSecStuffWrap_indirect(
		  CommandLine cmdLine,
		  xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode secSelMode, 
		  CmdLineHelper_Sec.SecSelectSubMode secSelSubMode,
		  String secIDTypeArgName, String secIDArgNanme, GCshSecID secID, 
		  String exchArgName, String tickerArgName, StringBuffer ticker, 
		  String micArgName, String micIDArgname, StringBuffer micID, 
		  String isinArgName, StringBuffer isin, 
		  String secNameArgName, StringBuffer secName,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	    parseSecStuff_indirect_part1(cmdLine, 
	    							secSelMode, secSelSubMode, 
	    							secIDTypeArgName, secID, 
	    							exchArgName, tickerArgName, 
	    							micArgName, micIDArgname, 
	    							isinArgName, 
	    							secNameArgName, 
	    							scriptMode );

	    parseSecStuff_indirect_part2(cmdLine, 
	    							secSelMode, 
	    							secIDTypeArgName, 
	    							exchArgName, micArgName, isinArgName, 
	    							isin, 
	    							secNameArgName, 
	    							scriptMode );
	  	
	    parseSecStuff_indirect_part3(cmdLine, 
	    							secSelMode, 
	    							secIDTypeArgName, exchArgName, micArgName, isinArgName,
	    							secNameArgName, secName, 
	    							scriptMode );
  }

  private static void parseSecStuff_indirect_part1(CommandLine cmdLine,
		xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode secSelMode,
		CmdLineHelper_Sec.SecSelectSubMode secSelSubMode, 
		String secIDTypeArgName, GCshSecID secID, 
		String exchArgName, String tickerArgName, 
		String micArgName, String micIDArgname, 
		String isinArgName, 
		String secNameArgName,
		boolean scriptMode) throws InvalidCommandLineArgsException
  {
	// < secIDArgNanme >
	// Well, not directly, but rather:
	// < exchArgName >, < tickerArgName >,
	// < micArgName >, < micIDArgname >,
	// < secIDTypeArgName >, <" + isinArgName + ">
	if ( ( cmdLine.hasOption(exchArgName)      && cmdLine.hasOption(tickerArgName) ) ||
		 ( cmdLine.hasOption(micArgName)       && cmdLine.hasOption(micIDArgname) ) ||
		 ( cmdLine.hasOption(secIDTypeArgName) && cmdLine.hasOption(isinArgName) ) )
	{
	    if ( secSelMode != xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID )
	    {
	      System.err.println("Pair <" + exchArgName      + ">/<" + tickerArgName + ">, " +
	    		  				  "<" + micArgName       + ">/<" + micIDArgname  + ">, " +
	    		  				  "<" + secIDTypeArgName + ">/<" + isinArgName   + "> " +
	    		  			 "may only be set with <sec-sel-mode> = '" + 
	    		  			 xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID + "'");
	      throw new InvalidCommandLineArgsException();
	    }
		
	    if ( secSelSubMode != SecSelectSubMode.INDIRECT_EXCHANGE_TICKER && 
	    	 secSelSubMode != SecSelectSubMode.INDIRECT_MIC && 
	    	 secSelSubMode != SecSelectSubMode.INDIRECT_SEC_ID_TYPE )
	    {
	      System.err.println("Pair <" + exchArgName      + ">/<" + tickerArgName + ">, " +
	    		  				  "<" + micArgName       + ">/<" + micIDArgname  + ">, " +
	    		  				  "<" + secIDTypeArgName + ">/<" + isinArgName   + "> " +
	    		  			 "may only be set with <sec-sel-sub-mode> = '" + 
	    		  			 SecSelectSubMode.INDIRECT_EXCHANGE_TICKER + "', " +
	    		  			 SecSelectSubMode.INDIRECT_MIC + "' or " +
	    		  			 SecSelectSubMode.INDIRECT_SEC_ID_TYPE + "'");
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
	        				   "<" + exchArgName      + ">/<" + tickerArgName + "> nor from" + 
	        				   "<" + micArgName       + ">/<" + micIDArgname  + "> nor from" + 
	        				   "<" + secIDTypeArgName + ">/<" + isinArgName   + ">");
	        throw new InvalidCommandLineArgsException();
		}
	}
	else
	{
	    if ( secSelMode == xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID )
	    {
	      System.err.println("Pair <" + exchArgName      + ">/<" + tickerArgName + ">, " +
	    		  				  "<" + micArgName       + ">/<" + micIDArgname  + ">, " +
	    		  				  "<" + secIDTypeArgName + ">/<" + isinArgName   + "> " +
	    		  			 "must be set with <sec-sel-mode> = '" + 
	    		  			 xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID + "'");
	      throw new InvalidCommandLineArgsException();
	    }
		
	    if ( secSelSubMode == SecSelectSubMode.INDIRECT_SEC_ID_TYPE )
	    {
	      System.err.println("Pair <" + exchArgName      + ">/<" + tickerArgName + ">, " +
	    		  				  "<" + micArgName       + ">/<" + micIDArgname  + ">, " +
	    		  				  "<" + secIDTypeArgName + ">/<" + isinArgName   + "> " +
	    		  			 "must be set with <sec-sel-sub-mode> = '" + SecSelectSubMode.INDIRECT_SEC_ID_TYPE + "'");
	      throw new InvalidCommandLineArgsException();
	    }
		
		if ( ! cmdLine.hasOption(isinArgName) && 
			 ! cmdLine.hasOption(secNameArgName) )
	   	{
	           System.err.println("One of the following must be set:\n" + 
	        		   			  " - <" + exchArgName      + ">/<" + tickerArgName + "> (pair) xor\n"+ 
	        		   			  " - <" + micArgName       + ">/<" + micIDArgname  + "> (pair) xor\n"+ 
	           				   	  " - <" + secIDTypeArgName + ">/<" + isinArgName   + "> (pair) xor\n"+ 
	           				   	  " - <" + isinArgName + "> (alone) xor\n"+ 
					   	  		  " - <" + secNameArgName + ">");
	           throw new InvalidCommandLineArgsException();
	   	}
	}
  }

  private static void parseSecStuff_indirect_part2(CommandLine cmdLine,
		xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode secSelMode, 
		String secIDTypeArgName,
		String exchArgName, String micArgName, String isinArgName, 
		StringBuffer isin, 
		String secNameArgName,
		boolean scriptMode) throws InvalidCommandLineArgsException
  {
	// < isinArgName > (alone)
	if ( cmdLine.hasOption(isinArgName) && 
		 ! cmdLine.hasOption(secIDTypeArgName) )
	{
	    if ( cmdLine.hasOption(exchArgName) ) 
	    {
	      System.err.println("Error: <" + isinArgName + "> and <" + exchArgName + "> are mutually exclusive");
	      throw new InvalidCommandLineArgsException();
	    }

	    if ( cmdLine.hasOption(micArgName) ) 
	    {
	      System.err.println("Error: <" + isinArgName + "> and <" + micArgName + "> are mutually exclusive");
	      throw new InvalidCommandLineArgsException();
	    }

	    if ( cmdLine.hasOption(secNameArgName) ) 
	    {
	      System.err.println("Error: <" + isinArgName + "> and <" + secNameArgName + "> are mutually exclusive");
	      throw new InvalidCommandLineArgsException();
	    }

	  if ( secSelMode != xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN ||
		   cmdLine.hasOption(secIDTypeArgName) )
	  {
	    System.err.println("<" + isinArgName + "> (alone) must only be set with <sec-sel-mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN + "'");
	    throw new InvalidCommandLineArgsException();
	  }
	  
	  try
	  {
	    isin.append( cmdLine.getOptionValue(isinArgName) );
	  }
	  catch (Exception exc)
	  {
	    System.err.println("Could not parse <" + isinArgName + ">");
	    throw new InvalidCommandLineArgsException();
	  }
	}
	else
	{
//	    	if ( ! cmdLine.hasOption("name") )
//	    	{
//	                  System.err.println("One of the following must be set:\n" + 
//	               		   			  " - <" + exchArgName + ">/<" + tickerArgName + "> (pair) xor\n"+ 
//	               		   			  " - <" + micArgName + ">/<" + micIDArgname + "> (pair) xor\n"+ 
//	                  				   	  " - <" + secIDTypeArgName + ">/<" + isinArgName + "> (pair) xor\n"+ 
//	                  				   	  " - <" + isinArgName + "> (alone) xor\n"+ 
//	       				   	  		  " - <name>");
//	                  throw new InvalidCommandLineArgsException();
//	    	}
		
	  if ( secSelMode == xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN &&
		   ! cmdLine.hasOption(secIDTypeArgName) )
	  {
	    System.err.println("<" + isinArgName + "> (alone) must be set with <sec-sel-mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN + "'");
	    throw new InvalidCommandLineArgsException();
	  }
	}

	if ( ! scriptMode && 
		 secSelMode != xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID )
	  System.err.println("ISIN:         '" + isin + "'");
  }

  private static void parseSecStuff_indirect_part3(CommandLine cmdLine,
		xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode secSelMode, 
		String secIDTypeArgName, 
		String exchArgName, String micArgName, String isinArgName, 
		String secNameArgName, StringBuffer secName,
		boolean scriptMode) throws InvalidCommandLineArgsException
  {
	// < secNameArgName >
	if ( secName != null )
	{
	    if ( cmdLine.hasOption(secNameArgName) )
	    {
	      if ( cmdLine.hasOption(exchArgName) ) 
	      {
	        System.err.println("Error: <" + secNameArgName + "> and <" + exchArgName + "> are mutually exclusive");
	        throw new InvalidCommandLineArgsException();
	      }

	      if ( cmdLine.hasOption(micArgName) ) 
	      {
	        System.err.println("Error: <" + secNameArgName + "> and <" + micArgName + "> are mutually exclusive");
	        throw new InvalidCommandLineArgsException();
	      }

	      if ( cmdLine.hasOption(secIDTypeArgName) ) 
	      {
	        System.err.println("Error: <" + secNameArgName + "> and <" + secIDTypeArgName + "> are mutually exclusive");
	        throw new InvalidCommandLineArgsException();
	      }

	      if ( cmdLine.hasOption(isinArgName) ) 
	      {
	        System.err.println("Error: <" + secNameArgName + "> and <" + isinArgName + "> are mutually exclusive");
	        throw new InvalidCommandLineArgsException();
	      }

	      if ( secSelMode != xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.NAME )
	      {
	        System.err.println("<" + secNameArgName + "> must only be set with <sec-sel-mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.NAME + "'");
	        throw new InvalidCommandLineArgsException();
	      }
	      
	      try
	      {
	        secName.append( cmdLine.getOptionValue(secNameArgName) );
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
	        System.err.println("<" + secNameArgName + "> must be set with <sec-sel-mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.NAME + "'");
	        throw new InvalidCommandLineArgsException();
	      }
	    }
	    
	    if ( ! scriptMode )
		      System.err.println("Security name: '" + secName + "'");
	}
  }

}
