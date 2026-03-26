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
import org.gnucash.api.write.GnuCashWritableVendor;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.base.basetypes.simple.GCshVendID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class UpdVend extends CommandLineTool
{
  // Logger
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdVend.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String gcshInFileName = null;
  private static String gcshOutFileName = null;
  
  private static GCshVendID vendID = null;

  private static String newNumber = null;
  private static String newName = null;
  private static String newDescr = null;

  private static GnuCashWritableVendor vend = null;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      UpdVend tool = new UpdVend ();
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
      .desc("Vendor ID")
      .longOpt("vendor-id")
      .get();
            
    Option optNumber = Option.builder("num")
      .hasArg()
      .argName("number")
      .desc("Vendor number (new)")
      .longOpt("new-number")
      .get();
    	    
    Option optName = Option.builder("nam")
      .hasArg()
      .argName("name")
      .desc("Vendor name (new)")
      .longOpt("new-name")
      .get();
    
    Option optDescr = Option.builder("desc")
      .hasArg()
      .argName("descr")
      .desc("Vendor description (new)")
      .longOpt("new-description")
      .get();
      
    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optID);
    options.addOption(optNumber);
    options.addOption(optName);
    options.addOption(optDescr);
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
      vend = gcshFile.getWritableVendorByID(vendID);
      System.err.println("Vendor before update: " + vend.toString());
    }
    catch ( Exception exc )
    {
      System.err.println("Error: Could not find/instantiate vendor with ID '" + vendID + "'");
      // ::TODO
//      throw new VendorNotFoundException();
      throw new NoEntryFoundException();
    }
    
    doChanges();
    System.err.println("Vendor after update: " + vend.toString());
    
    gcshFile.writeFile(new File(gcshOutFileName));
    
    System.out.println("OK");
  }

  private void doChanges() throws Exception
  {
    if ( newNumber != null )
    {
      System.err.println("Setting number");
      vend.setNumber(newNumber);
    }

    if ( newName != null )
    {
      System.err.println("Setting name");
      vend.setName(newName);
    }

    if ( newDescr != null )
    {
      System.err.println("Setting description");
      vend.setNotes(newDescr);
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
    
    // <vendor-id>
    try
    {
      vendID = new GCshVendID( cmdLine.getOptionValue("vendor-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <vendor-id>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Vendor ID: " + vendID);

    // <new-number>
    if ( cmdLine.hasOption("new-number") ) 
    {
      try
      {
        newNumber = cmdLine.getOptionValue("new-number").trim();
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <new-number>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("New number: '" + newNumber + "'");

    // <new-name>
    if ( cmdLine.hasOption("new-name") ) 
    {
      try
      {
        newName = cmdLine.getOptionValue("new-name").trim();
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <new-name>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("New name: '" + newName + "'");

    // <new-description>
    if ( cmdLine.hasOption("new-description") ) 
    {
      try
      {
        newDescr = cmdLine.getOptionValue("new-description").trim();
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <new-description>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("New description: '" + newDescr + "'");
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "UpdVend", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
