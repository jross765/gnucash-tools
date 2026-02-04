package org.gnucash.tools.xml.gen.simple;

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
import org.gnucash.api.Const;
import org.gnucash.apispec.read.GnuCashSecurity;
import org.gnucash.apispec.write.GnuCashWritableSecurity;
import org.gnucash.apispec.write.impl.GnuCashWritableFileExtImpl;
import org.gnucash.base.basetypes.complex.GCshCmdtyNameSpace;
import org.gnucash.base.basetypes.complex.GCshSecID_SecIdType;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GenSec extends CommandLineTool
{
  // Logger
  private static final Logger LOGGER = LoggerFactory.getLogger(GenSec.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String gcshInFileName = null;
  private static String gcshOutFileName = null;

  private static String     isin      = null;
  private static String     name      = null;
  
  private static String     symbol    = null;
  private static int        fraction  = 0;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GenSec tool = new GenSec ();
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
  protected void getConfigSettings(PropertiesConfiguration cfg) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    GnuCashWritableFileExtImpl gcshFile = new GnuCashWritableFileExtImpl(new File(gcshInFileName), true);
    
    // 1) Check whether there already is a security with that ISIN
    // 1.1) Variant 1: Qualif. ID (non-technical, as opposed to all other IDs in GnuCash)
    GCshSecID_SecIdType qualifID = new GCshSecID_SecIdType(GCshCmdtyNameSpace.SecIdType.ISIN, isin);
    GnuCashSecurity checkSec = gcshFile.getSecurityByID( qualifID );
    if ( checkSec != null )
    {
    	LOGGER.error("kernel: Encountered a security with ID '" + qualifID + "' in GnuCash file");
    	LOGGER.error("kernel: Aborting");
    	System.err.println("Error: There already is a security with ID '" + qualifID + "' in GnuCash file");
    	System.err.println("Aborting");
    	System.exit( 1 );
    }
    // 1.2) Variant 2: X-Code
    checkSec = gcshFile.getSecurityByXCode( isin );
    if ( checkSec != null )
    {
    	LOGGER.error("kernel: Encountered a security with X-code '" + isin + "' in GnuCash file");
    	LOGGER.error("kernel: Aborting");
    	System.err.println("Error: There already is a security with X-code '" + isin + "' in GnuCash file");
    	System.err.println("Aborting");
    	System.exit( 1 );
    }
    
    // 2) Generate security
    GnuCashWritableSecurity sec = gcshFile.createWritableSecurity(qualifID, isin, name);

    if ( symbol != null )
    	sec.setSymbol(symbol);
    
    if ( fraction != 0 )
    	sec.setFraction(fraction);
    
    // 3) Write to file
    System.out.println("Security to write: " + sec.toString());
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
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GenSec", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
