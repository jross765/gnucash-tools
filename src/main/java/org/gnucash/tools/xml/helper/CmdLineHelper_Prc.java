package org.gnucash.tools.xml.helper;

import org.apache.commons.cli.CommandLine;
import org.gnucash.base.basetypes.complex.GCshCmdtyID;
import org.gnucash.base.basetypes.simple.GCshIDNotSetException;
import org.gnucash.base.basetypes.simple.GCshPrcID;

import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.dateutils.LocalDateHelpers;

public class CmdLineHelper_Prc {

  public enum PrcSelectMode
  {
    ID,
    SEC_DATE,
    ISIN_DATE
  }

  // -----------------------------------------------------------------
  
  public static void setPrcID_direct(GCshPrcID prcID,
			CommandLine cmdLine,
			PrcSelectMode prcSelMode,
			boolean scriptMode) throws InvalidCommandLineArgsException, GCshIDNotSetException
  {
	  GCshPrcID locPrcID = getPrcID_direct(cmdLine,
			  							prcSelMode, 
			  							scriptMode );

	  prcID.reset();
	  prcID.set(locPrcID);
  }

  public static GCshPrcID getPrcID_direct(
		  CommandLine cmdLine,
		  PrcSelectMode prcSelMode,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  return getPrcID_direct(cmdLine,
			  prcSelMode,
			  "prc-sel-mode",
			  "price-id",
			  scriptMode );
  }

  public static GCshPrcID getPrcID_direct(
		  CommandLine cmdLine,
		  PrcSelectMode prcSelMode,
		  String prcSelModeArgName,
		  String prcIDArgName,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  GCshPrcID prcID = null;
	    
	  // < prcIDArgName >
	  if ( cmdLine.hasOption( prcIDArgName ) )
	  {
		  if ( prcSelMode != PrcSelectMode.ID )
		  {
			  System.err.println("<" + prcIDArgName + "> may only be set with <" + prcSelMode + "> = " + PrcSelectMode.ID);
			  throw new InvalidCommandLineArgsException();
		  }
	    		
		  try
		  {
			  prcID = new GCshPrcID( cmdLine.getOptionValue(prcIDArgName) ); 
		  }	
		  catch ( Exception exc )
		  {
			  System.err.println("Could not parse <" + prcIDArgName+ ">");
	          throw new InvalidCommandLineArgsException();
		  }
	  }
	  else
	  {
		  if ( prcSelMode == PrcSelectMode.ID )
		  {
			  System.err.println("<" + prcIDArgName+ "> must be set with <" + prcSelMode + "> = " + PrcSelectMode.ID);
			  throw new InvalidCommandLineArgsException();
		  }
	  }
	  
	  if ( ! scriptMode )
		  System.err.println("Price ID (direct): " + prcID);
	  
	  return prcID;
  }

	// ------------------------------

  public static void parsePrcStuffWrap(
		  CommandLine cmdLine,
		  PrcSelectMode prcSelMode,
		  GCshPrcID prcID, 
		  GCshCmdtyID cmdtyID, 
		  Helper.DateFormat dateFormat, LocalDateWrp date,
		  StringBuffer isin,
		  boolean scriptMode) throws InvalidCommandLineArgsException, GCshIDNotSetException {
	  parsePrcStuffWrap(cmdLine,
			  			prcSelMode,
			  			"price-id", prcID,
			  			"security-id", cmdtyID,
			  			"price-date-format", dateFormat,
			  			"price-date", date,
			  			"isin", isin,
			  			scriptMode);
  }
  
  public static void parsePrcStuffWrap(
		  CommandLine cmdLine,
		  PrcSelectMode prcSelMode,
		  String prcIDArgName, GCshPrcID prcID, 
		  String cmdtyIDArgName, GCshCmdtyID cmdtyID,
		  String dateFormatArgName, Helper.DateFormat dateFormat,
		  String dateArgName, LocalDateWrp date,
		  String isinArgName, StringBuffer isin,
		  boolean scriptMode) throws InvalidCommandLineArgsException, GCshIDNotSetException {
	  if ( prcSelMode == PrcSelectMode.ID ) {
		  if ( cmdLine.hasOption( isinArgName ) ) {
			  System.err.println("<" + isinArgName + "> must not be set with <prc-sel-mode> = '" + PrcSelectMode.ID + "'");
			  throw new InvalidCommandLineArgsException();
		  }
			
		  parsePrcStuffWrap_ID_direct(cmdLine,
				  prcSelMode,
				  prcIDArgName, prcID,
				  scriptMode);
	  } else if ( prcSelMode == PrcSelectMode.SEC_DATE ) {
		  if ( cmdLine.hasOption(prcIDArgName) )
		  {
			  System.err.println("<" + prcIDArgName + "> must not be set with <prc-sel-mode> = '" + PrcSelectMode.ISIN_DATE + "'");
			  throw new InvalidCommandLineArgsException();
		  }
		  
		  if ( cmdLine.hasOption(isinArgName) ) {
			  System.err.println("<" + isinArgName + "> must not be set with <prc-sel-mode> = '" + PrcSelectMode.SEC_DATE + "'");
			  throw new InvalidCommandLineArgsException();
		  }
			
		  parsePrcStuffWrap_SecID_etc(cmdLine,
				  prcSelMode,
				  cmdtyIDArgName, cmdtyID,
				  isinArgName,
				  dateFormatArgName, dateFormat,
				  dateArgName, date, 
				  scriptMode);
	  } else if ( prcSelMode == PrcSelectMode.ISIN_DATE ) {
		  if ( cmdLine.hasOption(prcIDArgName) )
		  {
			  System.err.println("<" + prcIDArgName + "> must not be set with <prc-sel-mode> = '" + PrcSelectMode.ISIN_DATE + "'");
			  throw new InvalidCommandLineArgsException();
		  }
		  
		  if ( cmdLine.hasOption( cmdtyIDArgName ) ) {
			  System.err.println("<" + cmdtyIDArgName + "> must not be set with <prc-sel-mode> = '" + PrcSelectMode.ISIN_DATE + "'");
			  throw new InvalidCommandLineArgsException();
		  }
			
		  parsePrcStuffWrap_ISIN_etc(cmdLine,
				  prcSelMode,
				  isinArgName, isin,
				  cmdtyIDArgName,
				  dateFormatArgName, dateFormat,
				  dateArgName, date, 
				  scriptMode);
	  }
  }
  
  private static void parsePrcStuffWrap_ID_direct(
		  CommandLine cmdLine,
		  PrcSelectMode prcSelMode,
		  GCshPrcID prcID,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  	if ( prcSelMode != PrcSelectMode.ID ) {
			System.err.println("Error: Wrong <prc-sel-mode>" );
			throw new InvalidCommandLineArgsException();
	  	}
	  
	  parsePrcStuffWrap_ID_direct(cmdLine,
			  				prcSelMode,
			  				"price-id", prcID,
			  				scriptMode);
  }
  
  private static void parsePrcStuffWrap_ID_direct(
		  CommandLine cmdLine,
		  PrcSelectMode prcSelMode,
		  String prcIDArgName, GCshPrcID prcID,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  	if ( prcSelMode != PrcSelectMode.ID ) {
			System.err.println("Error: Wrong <prc-sel-mode>" );
			throw new InvalidCommandLineArgsException();
	  	}
	  
	  // < prcIDArgName >
	  if ( cmdLine.hasOption(prcIDArgName) )
	  {
		  try
		  {
			  // No:
			  // secID = GCshSecID.parse( cmdLine.getOptionValue(secIDArgNanme) );
			  // Instead:
			  setPrcID_direct(prcID, 
					  cmdLine,
					  prcSelMode,
					  scriptMode);
		  }	
		  catch ( Exception exc )
		  {
			  System.err.println("Could not parse <" + prcIDArgName + ">");
	          throw new InvalidCommandLineArgsException();
		  }
	  }
	  else
	  {
		  System.err.println("Error: <" + prcIDArgName + "> must be set with <prc-sel-mode> = " + PrcSelectMode.ID);
		  throw new InvalidCommandLineArgsException();
	  }
  }
  
  private static void parsePrcStuffWrap_SecID_etc(
		  CommandLine cmdLine,
		  PrcSelectMode prcSelMode,
		  String cmdtyIDArgName, GCshCmdtyID cmdtyID,
		  String isinArgName,
		  String dateFormatArgName, Helper.DateFormat dateFormat,
		  String dateArgName, LocalDateWrp date,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  	if ( prcSelMode != PrcSelectMode.SEC_DATE ) {
			System.err.println("Error: Wrong <prc-sel-mode>" );
			throw new InvalidCommandLineArgsException();
	  	}
	  
		// < secCurrIDArgName >, < toCurrIDArgName >, < dateArgName >
		if ( cmdLine.hasOption( cmdtyIDArgName ) )
		{
			if ( cmdLine.hasOption( isinArgName ) )
			{
				System.err.println("Error: <" + isinArgName + "> must not be set with <prc-sel-mode> = " + PrcSelectMode.SEC_DATE);
				throw new InvalidCommandLineArgsException();
			}

			if ( !cmdLine.hasOption( dateFormatArgName ) )
			{
				System.err.println("Error: <" + dateFormatArgName + "> must be set with <prc-sel-mode> = " + PrcSelectMode.SEC_DATE);
				throw new InvalidCommandLineArgsException();
			}

			if ( !cmdLine.hasOption( dateArgName ) )
			{
				System.err.println("Error: <" + dateArgName + "> must be set with <prc-sel-mode> = " + PrcSelectMode.SEC_DATE);
				throw new InvalidCommandLineArgsException();
			}

			try
			{
				GCshCmdtyID locFromSecCurrID = GCshCmdtyID.parse( cmdLine.getOptionValue( cmdtyIDArgName ) );
				cmdtyID.set( locFromSecCurrID );
			}
			catch ( Exception exc )
			{
				System.err.println( "Could not parse <" + cmdtyIDArgName + ">" );
				throw new InvalidCommandLineArgsException();
			}
			
			try
			{
				dateFormat = Helper.DateFormat.valueOf( cmdLine.getOptionValue( dateFormatArgName ) );
			}
			catch ( Exception exc )
			{
				System.err.println( "Could not parse <" + dateFormatArgName + ">" );
				throw new InvalidCommandLineArgsException();
			}

			try
			{
				date.dat = LocalDateHelpers.parseLocalDate( cmdLine.getOptionValue( dateArgName ), dateFormat );
			}
			catch ( Exception exc )
			{
				System.err.println( "Could not parse <" + dateArgName + ">" );
				throw new InvalidCommandLineArgsException();
			}
		}
		else
		{
			System.err.println("Error: <" + cmdtyIDArgName + "> must be set with <prc-sel-mode> = " + PrcSelectMode.SEC_DATE);
			throw new InvalidCommandLineArgsException();
		}

		if ( !scriptMode )
		{
			System.err.println( "Sec/Curr:       " + cmdtyID );
			System.err.println( "Date format:    " + dateFormat );
			System.err.println( "Date:           " + date.dat );
		}
  }

  private static void parsePrcStuffWrap_ISIN_etc(
		  CommandLine cmdLine,
		  PrcSelectMode prcSelMode, 
		  String isinArgName, StringBuffer isin,
		  String fromSecCurrIDArgName,
		  String dateFormatArgName, Helper.DateFormat dateFormat,
		  String dateArgName, LocalDateWrp date,
		  boolean scriptMode) throws InvalidCommandLineArgsException, GCshIDNotSetException {
	  	if ( prcSelMode != PrcSelectMode.ISIN_DATE ) {
			System.err.println("Error: Wrong <prc-sel-mode>" );
			throw new InvalidCommandLineArgsException();
	  	}
	  
		// < isinArgName >, < toCurrIDArgName >, < dateArgName >
		if ( cmdLine.hasOption( isinArgName ) )
		{
			if ( cmdLine.hasOption( fromSecCurrIDArgName ) )
			{
				System.err.println("Error: <" + fromSecCurrIDArgName + "> must not be set with <prc-sel-mode> = " + PrcSelectMode.ISIN_DATE );
				throw new InvalidCommandLineArgsException();
			}

			if ( ! cmdLine.hasOption( dateFormatArgName ) )
			{
				System.err.println("Error: <" + dateFormatArgName + "> must be set with <prc-sel-mode> = " + PrcSelectMode.ISIN_DATE);
				throw new InvalidCommandLineArgsException();
			}

			if ( ! cmdLine.hasOption( dateArgName ) )
			{
				System.err.println("Error: <" + dateArgName + "> must be set with <prc-sel-mode> = " + PrcSelectMode.ISIN_DATE);
				throw new InvalidCommandLineArgsException();
			}

			try
			{
				isin.append( cmdLine.getOptionValue( isinArgName ) );
			}
			catch ( Exception exc )
			{
				System.err.println( "Could not parse <" + isinArgName + ">" );
				throw new InvalidCommandLineArgsException();
			}

			try
			{
				dateFormat = Helper.DateFormat.valueOf( cmdLine.getOptionValue( dateFormatArgName ) );
			}
			catch ( Exception exc )
			{
				System.err.println( "Could not parse <" + dateFormatArgName + ">" );
				throw new InvalidCommandLineArgsException();
			}

			try
			{
				date.dat = LocalDateHelpers.parseLocalDate( cmdLine.getOptionValue( dateArgName ), dateFormat );
			}
			catch ( Exception exc )
			{
				System.err.println( "Could not parse <" + dateArgName + ">" );
				throw new InvalidCommandLineArgsException();
			}
		}
		else
		{
			if ( cmdLine.hasOption( dateFormatArgName ) )
			{
				System.err.println("Error: <" + isinArgName + "> and <" + dateFormatArgName + "> must both either be set or unset" );
				throw new InvalidCommandLineArgsException();
			}

			if ( cmdLine.hasOption( dateArgName ) )
			{
				System.err.println("Error: <" + isinArgName + "> and <" + dateArgName + "> must both either be set or unset" );
				throw new InvalidCommandLineArgsException();
			}
		}

		if ( !scriptMode )
		{
			System.err.println( "ISIN:           " + isin );
			System.err.println( "Date format:    " + dateFormat );
			System.err.println( "Date:           " + date.dat );
		}
  }

}
