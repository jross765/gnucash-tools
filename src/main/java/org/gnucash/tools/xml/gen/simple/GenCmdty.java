package org.gnucash.tools.xml.gen.simple;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.Const;
import org.gnucash.api.read.GnuCashCommodity;
import org.gnucash.api.write.GnuCashWritableCommodity;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.base.basetypes.complex.GCshCmdtyCurrNameSpace;
import org.gnucash.base.basetypes.complex.GCshCmdtyID_SecIdType;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GenCmdty extends CommandLineTool
{
  // Logger
  private static final Logger LOGGER = LoggerFactory.getLogger(GenCmdty.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String gcshInFileName = null;
  private static String gcshOutFileName = null;

  private static String  name     = null;
  private static String  isin     = null;
  
  private static String  symbol   = null;
  private static int     fraction = 0;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GenCmdty tool = new GenCmdty ();
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
    // invcID = UUID.randomUUID();

//    cfg = new PropertiesConfiguration(System.getProperty("config"));
//    getConfigSettings(cfg);

    // Options
    // The essential ones
    Option optFileIn = Option.builder("if")
      .required()
      .hasArg()
      .argName("file")
      .desc("GnuCash file (in)")
      .longOpt("gnucash-in-file")
      .get();
          
    Option optFileOut = Option.builder("of")
      .required()
      .hasArg()
      .argName("file")
      .desc("GnuCash file (out)")
      .longOpt("gnucash-out-file")
      .get();
      
    Option optISIN = Option.builder("is")
      .required()
      .hasArg()
      .argName("isin")
      .desc("ISIN")
      .longOpt("isin")
      .get();
          
    Option optName = Option.builder("n")
      .required()
      .hasArg()
      .argName("name")
      .desc("Name")
      .longOpt("name")
      .get();
    
    // The convenient ones
    Option optSymbol = Option.builder("sy")
      .hasArg()
      .argName("symb")
      .desc("Symbol (ticker)")
      .longOpt("symbol")
      .get();

    Option optFraction = Option.builder("fr")
      .hasArg()
      .argName("num")
      .desc("Fraction (default " + Const.CMDTY_FRACTION_DEFAULT + ")")
      .longOpt("fraction")
      .get();

          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optISIN);
    options.addOption(optName);
    options.addOption(optSymbol);
    options.addOption(optFraction);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cs) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    GnuCashWritableFileImpl gcshFile = new GnuCashWritableFileImpl(new File(gcshInFileName));
    
    // 1) Check whether there already is a commodity with that ISIN
    // 1.1) Variant 1: Qualif. ID (non-technical, as opposed to all other IDs in GnuCash)
    GCshCmdtyID_SecIdType qualifID = new GCshCmdtyID_SecIdType(GCshCmdtyCurrNameSpace.SecIdType.ISIN, isin);
    GnuCashCommodity checkCmdty = gcshFile.getCommodityByQualifID( qualifID );
    if ( checkCmdty != null )
    {
    	LOGGER.error("kernel: Encountered a commodity with ID '" + qualifID + "' in GnuCash file");
    	LOGGER.error("kernel: Aborting");
    	System.err.println("Error: There already is a commodity with ID '" + qualifID + "' in GnuCash file");
    	System.err.println("Aborting");
    	System.exit( 1 );
    }
    // 1.2) Variant 2: X-Code
    checkCmdty = gcshFile.getCommodityByXCode( isin );
    if ( checkCmdty != null )
    {
    	LOGGER.error("kernel: Encountered a commodity with X-code '" + isin + "' in GnuCash file");
    	LOGGER.error("kernel: Aborting");
    	System.err.println("Error: There already is a commodity with X-code '" + isin + "' in GnuCash file");
    	System.err.println("Aborting");
    	System.exit( 1 );
    }
    
    // 2) Generate commodity
    GnuCashWritableCommodity cmdty = gcshFile.createWritableCommodity(qualifID, isin, name);

    if ( symbol != null )
    	cmdty.setSymbol(symbol);
    
    if ( fraction != 0 )
    	cmdty.setFraction(fraction);
    
    // 3) Write to file
    System.out.println("Commodity to write: " + cmdty.toString());
    gcshFile.writeFile(new File(gcshOutFileName));
    System.out.println("OK");
  }

  // -----------------------------------------------------------------

  @Override
  protected void parseCommandLineArgs(String[] args) throws InvalidCommandLineArgsException
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

    // <gnucash-in-file>
    try
    {
      gcshInFileName = cmdLine.getOptionValue("gnucash-in-file");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <gnucash-in-file>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("GnuCash file (in):  '" + gcshInFileName + "'");
    
    // <gnucash-out-file>
    try
    {
      gcshOutFileName = cmdLine.getOptionValue("gnucash-out-file");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <gnucash-out-file>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("GnuCash file (out): '" + gcshOutFileName + "'");
    
    // --
    
    // <isin>
    try
    {
      isin = cmdLine.getOptionValue("isin");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <isin>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("ISIN:               '" + isin + "'");
    
    // <name>
    try
    {
      name = cmdLine.getOptionValue("name");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <name>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Name:               '" + name + "'");
    
    // --
    
    // <symbol>
    if ( cmdLine.hasOption("symbol") )
    {
        try
        {
        	symbol = cmdLine.getOptionValue("symbol");
        }
        catch ( Exception exc )
        {
          System.err.println("Could not parse <symbol>");
          throw new InvalidCommandLineArgsException();
        }
    }

    System.err.println("Symbol:             '" + symbol + "'");

    // <fraction>
    if ( cmdLine.hasOption("fraction") )
    {
        try
        {
        	fraction = Integer.parseInt( cmdLine.getOptionValue("fraction") );
        }
        catch ( Exception exc )
        {
          System.err.println("Could not parse <fraction>");
          throw new InvalidCommandLineArgsException();
        }
    }
    // Implicit!
//    else
//    {
//    	fraction = Const.CMDTY_FRACTION_DEFAULT;
//    }

    System.err.println("Fraction:           " + fraction);
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GenCmdty", options );
  }
}
