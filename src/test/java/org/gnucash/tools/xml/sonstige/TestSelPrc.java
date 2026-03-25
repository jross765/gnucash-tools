package org.gnucash.tools.xml.sonstige;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashPrice;
import org.gnucash.api.read.impl.GnuCashFileImpl;
import org.gnucash.base.basetypes.complex.GCshSecID;
import org.gnucash.base.basetypes.simple.GCshPrcID;
import org.gnucash.tools.CommandLineTool;
import org.gnucash.tools.xml.helper.CmdLineHelper_Prc;
import org.gnucash.tools.xml.helper.LocalDateWrp;
import org.gnucash.tools.xml.helper.PriceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class TestSelPrc extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(TestSelPrc.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String gcshFileName = null;
  
  private static CmdLineHelper_Prc.PrcSelectMode    prcSelMode = null;

  // CAUTION: As opposed to most other tools, the following variables
  // have to be instantiated here.
  
  private static GCshPrcID          prcID         = new GCshPrcID();
  // Provide for selecting a currency as well ==> GCshCurrID and/or GCshCmdtyID
  private static GCshSecID          secID         = new GCshSecID(); // sic, security-ID, not commodity-ID
  private static Helper.DateFormat  dateFormat    = null;
  private static LocalDateWrp       date          = new LocalDateWrp();
  private static StringBuffer       isin          = new StringBuffer();
  // Possibly later:
  // private static StringBuffer  wkn             = new StringBuffer();
  // private static StringBuffer  cusip           = new StringBuffer();
  // private static StringBuffer  sedol           = new StringBuffer();
  
  private static boolean scriptMode = false;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      TestSelPrc tool = new TestSelPrc ();
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
//    cfg = new PropertiesConfiguration(System.getProperty("config"));
//    getConfigSettings(cfg);

    // Options
    // The essential ones
    Option optFile = Option.builder("f")
      .required()
      .hasArg()
      .argName("file")
      .desc("GnuCash file")
      .longOpt("gnucash-file")
      .get();

    Option optPrcMode = Option.builder("psm")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode for price")
      .longOpt("prc-sel-mode")
      .get();

    Option optPrcID = Option.builder("prc")
      .hasArg()
      .argName("UUID")
      .desc("Price-ID" +
    		  "(for <mode> = " + CmdLineHelper_Prc.PrcSelectMode.ID + " only)")
      .longOpt("price-id")
      .get();

    // ::TODO: Provide for selecting a currency as well
    Option optPrcSecID = Option.builder("sec")
      .hasArg()
      .argName("secID")
      .desc("Security ID (qualified) " +
    		  "(for <mode> = " + CmdLineHelper_Prc.PrcSelectMode.ID + " or " +
		       "<mode> = " + CmdLineHelper_Prc.PrcSelectMode.SEC_DATE + " only)")
      .longOpt("security-id")
      .get();

    Option optPrcDateFormat = Option.builder("df")
      .hasArg()
      .argName("date-format")
      .desc("Price date format")
      .longOpt("price-date-format")
      .get();

    Option optPrcDate = Option.builder("dat")
      .hasArg()
      .argName("date")
      .desc("Price date")
      .longOpt("price-date")
      .get();

    Option optPrcISIN = Option.builder("is")
      .hasArg()
      .argName("isin")
      .desc("ISIN " + 
    		  "(for <mode> = " + CmdLineHelper_Prc.PrcSelectMode.ISIN_DATE + " only)")
      .longOpt("isin")
      .get();

    // The convenient ones
    // ::EMPTY

    options = new Options();
    options.addOption(optFile);
    options.addOption(optPrcMode);
    options.addOption(optPrcID);
    options.addOption(optPrcSecID);
    options.addOption(optPrcDateFormat);
    options.addOption(optPrcDate);
    options.addOption(optPrcISIN);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cfg) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
	GnuCashFileImpl gcshFile = new GnuCashFileImpl(new File(gcshFileName), ! scriptMode);

    GnuCashPrice prc = PriceHelper.getPrc(prcSelMode, 
    									prcID, 
    									secID, 
    									dateFormat, date.dat,
    									isin.toString(),
    									gcshFile,
    									scriptMode);

    System.out.println("Selected price: " + prc.toString());
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
    catch ( Exception exc )
    {
      System.err.println("Could not parse <gnucash-file>");
      throw new InvalidCommandLineArgsException();
    }

    if ( ! scriptMode )
      System.err.println("GnuCash file: '" + gcshFileName + "'");

  	// ---------
  	
    // <prc-sel-mode>
    try
    {
      prcSelMode = CmdLineHelper_Prc.PrcSelectMode.valueOf(cmdLine.getOptionValue("prc-sel-mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <prc-sel-mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("Price mode:     " + prcSelMode);
    
  	// ---------

    // <prc-sel-mode>
    // <price-id>, 
    // <security-id>, <date>,
    // <isin>
    try
	{
		CmdLineHelper_Prc.parsePrcStuffWrap( cmdLine, 
										 prcSelMode,
										 prcID,
										 secID,
										 dateFormat, date, 
										 isin,
										 scriptMode );
	}
	catch ( Exception exc )
	{
		// TODO Auto-generated catch block
		exc.printStackTrace();
		throw new InvalidCommandLineArgsException();
	}
  }

  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "TestSelPrc", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    System.out.println("");
    System.out.println("Valid values for <prc-sel-mode>:");
    for ( CmdLineHelper_Prc.PrcSelectMode elt : CmdLineHelper_Prc.PrcSelectMode.values() )
      System.out.println(" - " + elt);
    
    System.out.println("");
    System.out.println("Valid values for <price-date-format>:");
    for ( Helper.DateFormat elt : Helper.DateFormat.values() )
      System.out.println(" - " + elt);
  }
}
