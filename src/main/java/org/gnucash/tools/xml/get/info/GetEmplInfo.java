package org.gnucash.tools.xml.get.info;

import java.io.File;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashEmployee;
import org.gnucash.api.read.impl.GnuCashFileImpl;
import org.gnucash.api.read.spec.GnuCashEmployeeVoucher;
import org.gnucash.base.basetypes.simple.GCshEmplID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetEmplInfo extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetEmplInfo.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String      gcshFileName = null;
  private static Helper.Mode mode         = null;
  private static GCshEmplID  emplID       = null;
  private static String      emplName     = null;
  
  private static boolean showVouchers  = false;
  
  private static boolean scriptMode = false; // ::TODO

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GetEmplInfo tool = new GetEmplInfo ();
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
    // acctID = UUID.randomUUID();

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
      .build();
      
    Option optMode = Option.builder("m")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode")
      .longOpt("mode")
      .build();
        
    Option optEmplID = Option.builder("empl")
      .hasArg()
      .argName("UUID")
      .desc("Employee-ID")
      .longOpt("employee-id")
      .build();
    
    Option optEmplName = Option.builder("n")
      .hasArg()
      .argName("name")
      .desc("Employee name")
      .longOpt("name")
      .build();
      
    // The convenient ones
    Option optShowVch = Option.builder("svch")
      .desc("Show vouchers")
      .longOpt("show-vouchers")
      .build();
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    options.addOption(optEmplID);
    options.addOption(optEmplName);
    options.addOption(optShowVch);
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
    
    GnuCashEmployee empl = null;
    if ( mode == Helper.Mode.ID )
    {
      empl = gcshFile.getEmployeeByID(emplID);
      if ( empl == null )
      {
        System.err.println("Found no employee with that ID");
        throw new NoEntryFoundException();
      }
    }
    else if ( mode == Helper.Mode.NAME )
    {
      Collection <GnuCashEmployee> emplList = null; 
      emplList = gcshFile.getEmployeesByUserName(emplName);
      if ( emplList.size() == 0 ) 
      {
        System.err.println("Could not find employees matching that name.");
        throw new NoEntryFoundException();
      }
      else if ( emplList.size() > 1 ) 
      {
        System.err.println("Found " + emplList.size() + " employees matching that name.");
        System.err.println("Please specify more precisely.");
        throw new TooManyEntriesFoundException();
      }
      empl = emplList.iterator().next();
    }
    
    try
    {
      System.out.println("ID:                " + empl.getID());
    }
    catch ( Exception exc )
    {
      System.out.println("ID:                " + "ERROR");
    }
    
    try
    {
      System.out.println("toString:          " + empl.toString());
    }
    catch ( Exception exc )
    {
      System.out.println("toString:          " + "ERROR");
    }
    
    try
    {
      System.out.println("Number:            '" + empl.getNumber() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Number:            " + "ERROR");
    }
    
    try
    {
      System.out.println("User name:         '" + empl.getUserName() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("User name:         " + "ERROR");
    }
    
    try
    {
      System.out.println("Address:           '" + empl.getAddress() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Address:           " + "ERROR");
    }
    
    System.out.println("");
    System.out.println("Expenses generated:");
    try
    {
      System.out.println(" - direct:  " + empl.getExpensesGeneratedFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println(" - direct:  " + "ERROR");
    }

    System.out.println("Outstanding value:");
    try
    {
      System.out.println(" - direct: " + empl.getOutstandingValueFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println(" - direct: " + "ERROR");
    }
    
    // ---
    
    if ( showVouchers )
      showInvoices(empl);
  }

  // -----------------------------------------------------------------

  private void showInvoices(GnuCashEmployee empl) throws Exception
  {
    System.out.println("");
    System.out.println("Vouchers:");

    System.out.println("Number of open vouchers: " + empl.getNofOpenVouchers());

    System.out.println("");
    System.out.println("Paid vouchers (direct):");
    for ( GnuCashEmployeeVoucher invc : empl.getPaidVouchers() )
    {
      System.out.println(" - " + invc.toString());
    }

    System.out.println("");
    System.out.println("Unpaid vouchers (direct):");
    for ( GnuCashEmployeeVoucher invc : empl.getUnpaidVouchers() )
    {
      System.out.println(" - " + invc.toString());
    }
    
    // There are no "employee jobs" and thus no paid/unpaid 
    // invoices "via jobs"
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
      System.err.println("Mode:         " + mode + "'");

    // <employee-id>
    if ( cmdLine.hasOption("employee-id") )
    {
      if ( mode != Helper.Mode.ID )
      {
        System.err.println("<employee-id> must only be set with <mode> = '" + Helper.Mode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        emplID = new GCshEmplID( cmdLine.getOptionValue("employee-id") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <employee-id>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( mode == Helper.Mode.ID )
      {
        System.err.println("<employee-id> must be set with <mode> = '" + Helper.Mode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }      
    }
    
    if ( ! scriptMode )
      System.err.println("Employee ID: '" + emplID + "'");

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
        emplName = cmdLine.getOptionValue("name");
      }
      catch ( Exception exc )
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
    
    if ( ! scriptMode )
      System.err.println("Name: '" + emplName + "'");

    // <show-vouchers>
    if ( cmdLine.hasOption("show-vouchers"))
    {
      showVouchers = true;
    }
    else
    {
      showVouchers = false;
    }
    
    if ( ! scriptMode )
      System.err.println("Show vouchers: " + showVouchers);
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GetEmplInfo", options );
    
    System.out.println("");
    System.out.println("Valid values for <mode>:");
    for ( Helper.Mode elt : Helper.Mode.values() )
      System.out.println(" - " + elt);
  }
}
