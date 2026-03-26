package org.gnucash.tools.xml.upd;

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
import org.gnucash.api.write.GnuCashWritablePrice;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.base.basetypes.simple.GCshPrcID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class UpdPrc extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdPrc.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String gcshInFileName = null;
  private static String gcshOutFileName = null;
  
  private static GCshPrcID prcID = null;

  private static GnuCashPrice.Type    newType   = null;
  private static GnuCashPrice.Source  newSource = null;
  private static FixedPointNumber     newValue  = null;

  private static GnuCashWritablePrice prc = null;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      UpdPrc tool = new UpdPrc ();
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

    Option optID = Option.builder("id")
      .required()
      .hasArg()
      .argName("UUID")
      .desc("Price ID")
      .longOpt("price-id")
      .get();

    Option optType = Option.builder("t")
      .hasArg()
      .argName("type")
      .desc("Price type (new)")
      .longOpt("new-type")
      .get();

    Option optSource = Option.builder("s")
      .hasArg()
      .argName("source")
      .desc("Price source (new)")
      .longOpt("new-source")
      .get();

    Option optValue = Option.builder("v")
      .hasArg()
      .argName("value")
      .desc("Price value (new)")
      .longOpt("new-value")
      .get();

    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optID);
    options.addOption(optType);
    options.addOption(optSource);
    options.addOption(optValue);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cfg) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    GnuCashWritableFileImpl gcshFile = new GnuCashWritableFileImpl(new File(gcshInFileName), true);

    try 
    {
      prc = gcshFile.getWritablePriceByID(prcID);
      System.err.println("Price before update: " + prc.toString());
    }
    catch ( Exception exc )
    {
      System.err.println("Error: Could not find/instantiate price with ID '" + prcID + "'");
      // ::TODO
//      throw new PriceNotFoundException();
      throw new NoEntryFoundException();
    }
    
    doChanges();
    System.err.println("Price after update: " + prc.toString());
    
    gcshFile.writeFile(new File(gcshOutFileName));
    
    System.out.println("OK");
  }

  private void doChanges() throws Exception
  {
    if ( newType != null )
    {
      System.err.println("Setting type");
      prc.setType(newType);
    }

    if ( newSource != null )
    {
      System.err.println("Setting source");
      prc.setSource(newSource);
    }

    if ( newValue != null )
    {
      System.err.println("Setting value");
      prc.setValue(newValue);
    }
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
    System.err.println("GnuCash file (in): '" + gcshInFileName + "'");
    
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
    
    // <price-id>
    try
    {
      prcID = new GCshPrcID( cmdLine.getOptionValue("price-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <price-id>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Price ID: " + prcID);

    // <new-type>
    if ( cmdLine.hasOption("new-type") ) 
    {
      try
      {
        newType = GnuCashPrice.Type.valueOf( cmdLine.getOptionValue("new-type") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <new-type>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("New type: " + newType);

    // <new-source>
    if ( cmdLine.hasOption("new-source") ) 
    {
      try
      {
    	newSource = GnuCashPrice.Source.valueOf( cmdLine.getOptionValue("new-source") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <new-source>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("New source: " + newSource);

    // <new-value>
    if ( cmdLine.hasOption("new-value") ) 
    {
      try
      {
        newValue = new FixedPointNumber( cmdLine.getOptionValue("new-value") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <new-value>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("New value: " + newValue);
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "UpdPrc", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
