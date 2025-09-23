package org.gnucash.tools.xml.get.info;

import java.io.File;
import java.time.LocalDate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashPrice;
import org.gnucash.api.read.impl.GnuCashFileImpl;
import org.gnucash.base.basetypes.complex.GCshCmdtyID;
import org.gnucash.base.basetypes.simple.GCshPrcID;
import org.gnucash.tools.CommandLineTool;
import org.gnucash.tools.xml.helper.CmdLineHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetPrcInfo extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetPrcInfo.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String      gcshFileName  = null;
  
  private static CmdLineHelper.PrcSelectMode mode = null;
  
  private static GCshPrcID         prcID         = null;
  // Provide for selecting a currency as well ==> GCshCurrID and/ord GCshCmdtyCurrID
  private static GCshCmdtyID       cmdtyID       = null;  
  private static Helper.DateFormat dateFormat    = null;
  private static LocalDate         date          = null;
  
  private static boolean scriptMode = false;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GetPrcInfo tool = new GetPrcInfo ();
      tool.execute(args);
    }
    catch (CouldNotExecuteException exc) 
    {
      System.err.println("Execution exception. Aborting.");
      exc.printStackTrace();
      System.exit(1);
    }
  }

  @Override
  protected void init() throws Exception
  {
  	prcID = new GCshPrcID();
  	cmdtyID = new GCshCmdtyID();

//    cfg = new PropertiesConfiguration(System.getProperty("config"));
//    getConfigSettings(cfg);

    // Options
    // The essential ones
    Option optFile = Option.builder("if")
      .required()
      .hasArg()
      .argName("file")
      .desc("GnuCash file")
      .longOpt("gnucash-file")
      .build();
      
    Option optMode = Option.builder("m")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode")
      .longOpt("mode")
      .build();
    	      
    Option optID = Option.builder("prc")
      .hasArg()
      .argName("UUID")
      .desc("Price ID (for mode = '" + CmdLineHelper.PrcSelectMode.ID + "' only)")
      .longOpt("price-id")
      .build();
    	          
    // ::TODO:
    //  - Provide for selecting a currency as well
    //  - For commodity: This is a temporary solution.
    //    We need the commodity-sub-selection mode here as well,
    //    just as in GetCmdtyInfo.
    //    (And, of course, do that with minimal code redundancies.)
    Option optCmdtyID = Option.builder("cmdty")
      .hasArg()
      .argName("cmdtyid")
      .desc("Commodity ID (qualified) (for mode = '" + CmdLineHelper.PrcSelectMode.CMDTY_DATE + "' only)")
      .longOpt("commodity-id")
      .build();
    	    	          
    Option optDateFormat = Option.builder("df")
      .hasArg()
      .argName("date-format")
      .desc("Date format")
      .longOpt("date-format")
      .build();
    	    	            
    Option optDate = Option.builder("dat")
      .hasArg()
      .argName("date")
      .desc("Date")
      .longOpt("date")
      .build();
    	    	          
    // The convenient ones
    // ::EMPTY
            
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    options.addOption(optID);
    options.addOption(optCmdtyID);
    options.addOption(optDateFormat);
    options.addOption(optDate);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cs) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    GnuCashFileImpl gcshFile = new GnuCashFileImpl(new File(gcshFileName));
    
    GnuCashPrice prc = null;
    
    if ( mode == CmdLineHelper.PrcSelectMode.ID )
    {
        prc = gcshFile.getPriceByID(prcID);
        if ( prc == null )
        {
          System.err.println("Could not find a price with this ID.");
          throw new NoEntryFoundException();
        }
    }
    else if ( mode == CmdLineHelper.PrcSelectMode.CMDTY_DATE )
    {
        prc = gcshFile.getPriceByCmdtyIDDate(cmdtyID, date);
        if ( prc == null )
        {
          System.err.println("Could not find a price matching this commodity-ID/date.");
          throw new NoEntryFoundException();
        }
    }

    // ----------------------------

    try
    {
      System.out.println("toString:          " + prc.toString());
    }
    catch (Exception exc)
    {
      System.out.println("toString:          " + "ERROR");
    }
    
    try
    {
      System.out.println("From cmdty/curr:   " + prc.getFromCmdtyCurrQualifID());
    }
    catch (Exception exc)
    {
      System.out.println("From cmdty/curr:   " + "ERROR");
    }

    try
    {
      System.out.println("To curr:           " + prc.getToCurrencyQualifID());
    }
    catch (Exception exc)
    {
      System.out.println("To curr:           " + "ERROR");
    }

    try
    {
      System.out.println("Date:              " + prc.getDate());
    }
    catch (Exception exc)
    {
      System.out.println("Date:              " + "ERROR");
    }

    try
    {
      System.out.println("Value:             " + prc.getValueFormatted());
    }
    catch (Exception exc)
    {
      System.out.println("Value:             " + "ERROR");
    }

    try
    {
      System.out.println("Type:              " + prc.getType());
    }
    catch (Exception exc)
    {
      System.out.println("Type:              " + "ERROR");
    }

    try
    {
      System.out.println("Source:            " + prc.getSource());
    }
    catch (Exception exc)
    {
      System.out.println("Source:            " + "ERROR");
    }
  }

  // -----------------------------------------------------------------

  @Override
  protected void parseCommandLineArgs(String[] args)
      throws InvalidCommandLineArgsException
  {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmdLine = null;
    try
    {
      cmdLine = parser.parse(options, args);
    }
    catch (ParseException exc)
    {
      System.err.println("Parsing options failed. Reason: " + exc.getMessage());
      throw new InvalidCommandLineArgsException();
    }

    // ---

    // <gnucash-file>
    try
    {
      gcshFileName = cmdLine.getOptionValue("gnucash-file");
    }
    catch (Exception exc)
    {
      System.err.println("Could not parse <gnucash-file>");
      throw new InvalidCommandLineArgsException();
    }

    if (!scriptMode)
      System.err.println("GnuCash file: '" + gcshFileName + "'");

    // <mode>
    try
    {
      mode = CmdLineHelper.PrcSelectMode.valueOf(cmdLine.getOptionValue("mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if (!scriptMode)
        System.err.println("Mode:         " + mode);

    // <price-id>
    if ( cmdLine.hasOption( "price-id" ) )
    {
    	if ( mode != CmdLineHelper.PrcSelectMode.ID )
    	{
            System.err.println("<price-id> may only be set with <mode> = " + CmdLineHelper.PrcSelectMode.ID);
            throw new InvalidCommandLineArgsException();
    	}
    		
        try
        {
          prcID = new GCshPrcID( cmdLine.getOptionValue("price-id") ); 
        }
        catch ( Exception exc )
        {
          System.err.println("Could not parse <price-id>");
          throw new InvalidCommandLineArgsException();
        }
    }
    else
    {
    	if ( mode == CmdLineHelper.PrcSelectMode.ID )
    	{
            System.err.println("<price-id> must be set with <mode> = " + CmdLineHelper.PrcSelectMode.ID);
            throw new InvalidCommandLineArgsException();
    	}
    }
    
    if (!scriptMode)
        System.err.println("Price ID:     " + prcID);

    // <commodity-id>
    if ( cmdLine.hasOption( "commodity-id" ) )
    {
    	if ( mode != CmdLineHelper.PrcSelectMode.CMDTY_DATE )
    	{
            System.err.println("<commodity-id> may only be set with <mode> = " + CmdLineHelper.PrcSelectMode.CMDTY_DATE);
            throw new InvalidCommandLineArgsException();
    	}
    		
        try
        {
          cmdtyID = GCshCmdtyID.parse( cmdLine.getOptionValue("commodity-id") ); 
        }
        catch ( Exception exc )
        {
          System.err.println("Could not parse <commodity-id>");
          throw new InvalidCommandLineArgsException();
        }
    }
    else
    {
    	if ( mode == CmdLineHelper.PrcSelectMode.CMDTY_DATE )
    	{
            System.err.println("<commodity-id> must be set with <mode> = " + CmdLineHelper.PrcSelectMode.CMDTY_DATE);
            throw new InvalidCommandLineArgsException();
    	}
    }
    
    if (!scriptMode)
        System.err.println("Commodity ID: " + cmdtyID);

    // <date-format>
    if ( cmdLine.hasOption( "date-format" ) )
    {
    	if ( mode != CmdLineHelper.PrcSelectMode.CMDTY_DATE )
    	{
            System.err.println("<date-format> may only be set with <mode> = " + CmdLineHelper.PrcSelectMode.CMDTY_DATE);
            throw new InvalidCommandLineArgsException();
    	}
    	
    	dateFormat = CmdLineHelper.getDateFormat(cmdLine, "date-format");
    }
    else
    {
    	if ( mode == CmdLineHelper.PrcSelectMode.CMDTY_DATE )
    	{
            System.err.println("<date-format> must be set with <mode> = " + CmdLineHelper.PrcSelectMode.CMDTY_DATE);
            throw new InvalidCommandLineArgsException();
    	}
    }

    if (!scriptMode)
        System.err.println("Date format:  " + dateFormat);

    // <date>
    if ( cmdLine.hasOption( "date" ) )
    {
    	if ( mode != CmdLineHelper.PrcSelectMode.CMDTY_DATE )
    	{
            System.err.println("<date> may only be set with <mode> = " + CmdLineHelper.PrcSelectMode.CMDTY_DATE);
            throw new InvalidCommandLineArgsException();
    	}
    		
        date = CmdLineHelper.getDate(cmdLine, "date", dateFormat); 
    }
    else
    {
    	if ( mode == CmdLineHelper.PrcSelectMode.CMDTY_DATE )
    	{
            System.err.println("<date> must be set with <mode> = " + CmdLineHelper.PrcSelectMode.CMDTY_DATE);
            throw new InvalidCommandLineArgsException();
    	}
    }
    
    if (!scriptMode)
        System.err.println("Date:         " + date);

  }

  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("GetPrcInfo", options);
    
    System.out.println("");
    System.out.println("Valid values for <mode>:");
    for ( CmdLineHelper.PrcSelectMode elt : CmdLineHelper.PrcSelectMode.values() )
      System.out.println(" - " + elt);
    
    System.out.println("");
    System.out.println("Valid values for <date-format>:");
    for ( Helper.DateFormat elt : Helper.DateFormat.values() )
      System.out.println(" - " + elt);
  }
}
