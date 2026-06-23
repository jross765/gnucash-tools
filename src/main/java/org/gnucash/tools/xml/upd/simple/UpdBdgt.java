package org.gnucash.tools.xml.upd.simple;

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
import org.gnucash.api.write.GnuCashWritableBudget;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.base.basetypes.simple.GCshBdgtID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class UpdBdgt extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdBdgt.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String gcshInFileName = null;
  private static String gcshOutFileName = null;
  
  private static GCshBdgtID bdgtID = null;

  // ---

  private static GnuCashWritableBudget bdgt = null;

  private static String              newName      = null;
  private static String              newDescr     = null;
  
  private static boolean scriptMode = false;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      UpdBdgt tool = new UpdBdgt ();
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
      
    Option optID = Option.builder("bdgt")
      .required()
      .hasArg()
      .argName("UUID")
      .desc("Budget ID")
      .longOpt("budget-id")
      .get();

    Option optName = Option.builder("nam")
      .hasArg()
      .argName("name")
      .desc("Budget name (new)")
      .longOpt("new-name")
      .get();
    
    Option optDescr = Option.builder("desc")
      .hasArg()
      .argName("descr")
      .desc("Budget description (new)")
      .longOpt("new-description")
      .get();
      
    // The convenient ones
    Option optScript = Option.builder("sl")
      .desc("Script Mode")
      .longOpt("script")
      .get();            
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optID);
    options.addOption(optName);
    options.addOption(optDescr);
    options.addOption(optScript);
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

    // CAUTION: Here, we intentionally do not use BudgetHelper.getWrtBdgt(),
    // because that would necessitate the use of CmdLineHelper_Bdgt.parseBdgtStuffWrap(),
    // and that makes no sense here because there is only one way to select an
    // budget: by its ID (the name arg. is for tne *new* name)
    try 
    {
      bdgt = gcshFile.getWritableBudgetByID(bdgtID);
      System.err.println("Budget before update: " + bdgt.toString());
    }
    catch ( Exception exc )
    {
      System.err.println("Error: Could not find/instantiate budget with ID '" + bdgtID + "'");
      throw new NoEntryFoundException();
    }
    
    doChanges();
    System.err.println("Budget after update: " + bdgt.toString());
    
    gcshFile.writeFile(new File(gcshOutFileName));
    
    System.out.println("OK");
  }

  private void doChanges() throws Exception
  {
    if ( newName != null )
    {
      System.err.println("Setting name");
      bdgt.setName(newName);
    }

    if ( newDescr != null )
    {
      System.err.println("Setting description");
      bdgt.setDescription(newDescr);
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

    // <script>
    if ( cmdLine.hasOption("script") )
    {
      scriptMode = true; 
    }
    // System.err.println("Script mode: " + scriptMode);
    
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
    
    // CAUTION: Here, we CmdLineHelper_Bdgt.parseBdgtStuffWrap(),
    // because there is only one way to select an budget: by its ID 
    // (the name arg. is for tne *new* name).
    // <budget-id>
    try
    {
      bdgtID = new GCshBdgtID( cmdLine.getOptionValue("budget-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <budget-id>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Budget ID: " + bdgtID);

    // <new-name>
    if ( cmdLine.hasOption("new-name") ) 
    {
      try
      {
        newName = cmdLine.getOptionValue("new-name");
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
        newDescr = cmdLine.getOptionValue("new-description");
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
		formatter.printHelp( "UpdBdgt", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
