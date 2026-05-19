package org.gnucash.tools.xml.get.info;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashBudget;
import org.gnucash.api.read.aux.GCshBudgetAccount;
import org.gnucash.api.read.aux.GCshBudgetPeriod;
import org.gnucash.api.read.impl.GnuCashFileImpl;
import org.gnucash.base.basetypes.simple.GCshBdgtID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetBdgtInfo extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetBdgtInfo.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String      gcshFileName = null;
  private static Helper.Mode mode        = null;
  private static GCshBdgtID   bdgtID      = null;
  private static String      name        = null;
  
  private static boolean showAcct   = false;
  private static boolean showPrd    = false;

  private static boolean scriptMode = false; // ::TODO

  public static void main( String[] args )
  {
    try
    {
      GetBdgtInfo tool = new GetBdgtInfo ();
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
      
    Option optMode = Option.builder("m")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode")
      .longOpt("mode")
      .get();
        
    Option optBdgtID = Option.builder("bdgt")
      .hasArg()
      .argName("ID")
      .desc("Budget ID")
      .longOpt("bdgtitution-id")
      .get();
          
    Option optName = Option.builder("n")
      .hasArg()
      .argName("name")
      .desc("Name (or part of)")
      .longOpt("name")
      .get();
          
    // The convenient ones
    Option optShowAcct = Option.builder("sacct")
      .desc("Show accounts")
      .longOpt("show-accounts")
      .get();
            
    Option optShowPrd = Option.builder("sprd")
      .desc("Show periods")
      .longOpt("show-periods")
      .get();
    	            
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    options.addOption(optBdgtID);
    options.addOption(optName);
    options.addOption(optShowAcct);
    options.addOption(optShowPrd);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cfg) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    GnuCashFileImpl gcshFile = new GnuCashFileImpl(new File(gcshFileName), true);

    GnuCashBudget bdgt = null;
    
    if ( mode == Helper.Mode.ID )
    {
      bdgt = gcshFile.getBudgetByID(bdgtID);
      if ( bdgt == null )
      {
        System.err.println("Could not find a budget with this ID.");
        throw new NoEntryFoundException();
      }
    }
    else if ( mode == Helper.Mode.NAME )
    {
      Collection<GnuCashBudget> bdgtList = gcshFile.getBudgetsByName(name); 
      if ( bdgtList.size() == 0 )
      {
        System.err.println("Could not find budgets matching this name.");
        throw new NoEntryFoundException();
      }
      if ( bdgtList.size() > 1 )
      {
        System.err.println("Found " + bdgtList.size() + " budgets matching this name.");
        System.err.println("Please specify more precisely.");
        throw new TooManyEntriesFoundException();
      }
      bdgt = bdgtList.iterator().next(); // first element
    }
    
    // ----------------------------

    try
    {
      System.out.println("ID:                '" + bdgt.getID() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("ID:                " + "ERROR");
    }

    try
    {
      System.out.println("toString:          " + bdgt.toString());
    }
    catch (Exception exc)
    {
      System.out.println("toString:          " + "ERROR");
    }
    
    try
    {
      System.out.println("Name:              '" + bdgt.getName() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("Name:              " + "ERROR");
    }

    // ---
    
    if ( showAcct )
      showAccounts(bdgt);
  }

  private void showAccounts(GnuCashBudget bdgt)
  {
    System.out.println("");
    System.out.println("Accounts:");
    
    for ( GCshBudgetAccount acct : bdgt.getAccounts() )
    {
      System.out.println(" - " + acct.toString());
      
      if ( showPrd )
      {
          for ( GCshBudgetPeriod prd : acct.getPeriods() )
          {
            System.out.println("   o " + prd.toString());
          }
      }
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
      mode = Helper.Mode.valueOf(cmdLine.getOptionValue("mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("Mode:     " + mode);

    // <bdgtitution-id>
    if ( cmdLine.hasOption("bdgtitution-id") )
    {
      if ( mode != Helper.Mode.ID )
      {
        System.err.println("<bdgtitution-id> must only be set with <mode> = '" + Helper.Mode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        bdgtID = new GCshBdgtID( cmdLine.getOptionValue("bdgtitution-id") );
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <bdgtitution-id>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( mode == Helper.Mode.ID )
      {
        System.err.println("<bdgtitution-id> must be set with <mode> = '" + Helper.Mode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("Budget ID: '" + bdgtID + "'");

    // <name>
    if ( cmdLine.hasOption("name") )
    {
      if ( mode != Helper.Mode.NAME )
      {
        System.err.println("<name> must only be set with <mode> = '" + Helper.Mode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        name = cmdLine.getOptionValue("name");
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <name>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( mode == Helper.Mode.NAME )
      {
        System.err.println("<name> must be set with <mode> = '" + Helper.Mode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("Name:     '" + name + "'");
    
    // <show-accounts>
    if ( cmdLine.hasOption("show-accounts"))
    {
      showAcct = true;
    }
    else
    {
    	showAcct = false;
    }
    
    if ( ! scriptMode )
      System.err.println("Show accounts: " + showAcct);
    
    // <show-periods>
    if ( cmdLine.hasOption("show-periods"))
    {
      showPrd = true;
    }
    else
    {
    	showPrd = false;
    }
    
    if ( ! scriptMode )
      System.err.println("Show periods:  " + showPrd);
  }

  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GetBdgtInfo", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    System.out.println("");
    System.out.println("Valid values for <mode>:");
    for ( Helper.Mode elt : Helper.Mode.values() )
      System.out.println(" - " + elt);
  }
}
