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
import org.gnucash.api.read.GnuCashGenerJob;
import org.gnucash.api.read.aux.GCshOwner;
import org.gnucash.api.read.impl.GnuCashFileImpl;
import org.gnucash.api.read.impl.spec.GnuCashCustomerJobImpl;
import org.gnucash.api.read.impl.spec.GnuCashVendorJobImpl;
import org.gnucash.api.read.spec.GnuCashJobInvoice;
import org.gnucash.base.basetypes.simple.GCshGenerJobID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetGenerJobInfo extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetGenerJobInfo.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String         gcshFileName = null;
  private static Helper.Mode    mode         = null;
  private static GCshGenerJobID jobID        = null;
  private static String         jobName      = null;
  
  private static boolean showInvoices = false;
  
  private static boolean scriptMode = false; // ::TODO

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GetGenerJobInfo tool = new GetGenerJobInfo ();
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
    // trxID = UUID.randomUUID();

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
        
    Option optJobID = Option.builder("job")
      .hasArg()
      .argName("UUID")
      .desc("Job-ID")
      .longOpt("job-id")
      .build();
    
    Option optJobName = Option.builder("n")
      .hasArg()
      .argName("name")
      .desc("Job name")
      .longOpt("name")
      .build();
      
    // The convenient ones
    Option optShowInvc = Option.builder("sinvc")
      .desc("Show invoices")
      .longOpt("show-invoices")
      .build();
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    options.addOption(optJobID);
    options.addOption(optJobName);
    options.addOption(optShowInvc);
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
    
    GnuCashGenerJob job = null;
    if ( mode == Helper.Mode.ID )
    {
      job = gcshFile.getGenerJobByID(jobID);
      if ( job == null )
      {
        System.err.println("Found no job with that ID");
        throw new NoEntryFoundException();
      }
    }
    else if ( mode == Helper.Mode.NAME )
    {
      Collection <GnuCashGenerJob> jobList = null; 
      jobList = gcshFile.getGenerJobsByName(jobName);
      if ( jobList.size() == 0 ) 
      {
        System.err.println("Could not find jobs matching that name.");
        throw new NoEntryFoundException();
      }
      else if ( jobList.size() > 1 ) 
      {
        System.err.println("Found " + jobList.size() + " jobs matching that name.");
        System.err.println("Please specify more precisely.");
        throw new TooManyEntriesFoundException();
      }
      job = jobList.iterator().next();
    }
    
    try
    {
      System.out.println("ID:              " + job.getID());
    }
    catch ( Exception exc )
    {
      System.out.println("ID:              " + "ERROR");
    }
    
    try
    {
      System.out.println("toString (gener.): " + job.toString());
    }
    catch ( Exception exc )
    {
      System.out.println("toString (gener.): " + "ERROR");
    }
    
    try
    {
      if ( job.getOwnerType() == GCshOwner.Type.CUSTOMER )
      {
        GnuCashCustomerJobImpl spec = new GnuCashCustomerJobImpl(job);
        System.out.println("toString (spec):   " + spec.toString());
      }
      else if ( job.getOwnerType() == GCshOwner.Type.VENDOR )
      {
        GnuCashVendorJobImpl spec = new GnuCashVendorJobImpl(job);
        System.out.println("toString (spec):   " + spec.toString());
      }
    }
    catch ( Exception exc )
    {
      System.out.println("toString (spec):   " + "ERROR");
    }
    
    try
    {
      System.out.println("Number:          " + job.getNumber());
    }
    catch ( Exception exc )
    {
      System.out.println("Number:          " + "ERROR");
    }
        
    try
    {
      System.out.println("Name:            " + job.getName());
    }
    catch ( Exception exc )
    {
      System.out.println("Name:            " + "ERROR");
    }
    
    try
    {
      System.out.println("Type:            " + job.getType());
    }
    catch ( Exception exc )
    {
      System.out.println("Type:            " + "ERROR");
    }

    try
    {
      System.out.println("Owner type:      " + job.getOwnerType());
    }
    catch ( Exception exc )
    {
      System.out.println("Owner type:      " + "ERROR");
    }

    try
    {
      System.out.println("Owner ID:        " + job.getOwnerID());
    }
    catch ( Exception exc )
    {
      System.out.println("Owner ID:        " + "ERROR");
    }

    System.out.println("");
    try
    {
      System.out.println("Income generated:  " + job.getIncomeGeneratedFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Income generated:  " + "ERROR");
    }

    try
    {
      System.out.println("Outstanding value: " + job.getOutstandingValueFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Outstanding value: " + "ERROR");
    }
    
    // ---

    if ( showInvoices )
      showInvoices(job);
  }

  // -----------------------------------------------------------------

  private void showInvoices(GnuCashGenerJob job) throws Exception
  {
    System.out.println("");
    System.out.println("Invoices:");

    System.out.println("");
    System.out.println("Paid invoices:");
    for ( GnuCashJobInvoice invc : job.getPaidInvoices() )
    {
      System.out.println(" - " + invc.toString());
    }
    
    System.out.println("");
    System.out.println("Unpaid invoices:");
    for ( GnuCashJobInvoice invc : job.getUnpaidInvoices() )
    {
      System.out.println(" - " + invc.toString());
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

    // <job-id>
    if ( cmdLine.hasOption("job-id") )
    {
      if ( mode != Helper.Mode.ID )
      {
        System.err.println("<job-id> must only be set with <mode> = '" + Helper.Mode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
          jobID = new GCshGenerJobID( cmdLine.getOptionValue("job-id") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <job-id>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( mode == Helper.Mode.ID )
      {
        System.err.println("<job-id> must be set with <mode> = '" + Helper.Mode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }      
    }
    
    if ( ! scriptMode )
      System.err.println("Job ID: '" + jobID + "'");

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
        jobName = cmdLine.getOptionValue("name");
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
      System.err.println("Name: '" + jobName + "'");

    // <show-invoices>
    if ( cmdLine.hasOption("show-invoices"))
    {
      showInvoices = true;
    }
    else
    {
      showInvoices = false;
    }
    
    if ( ! scriptMode )
      System.err.println("Show invoices: " + showInvoices);
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GetGenerJobInfo", options );
    
    System.out.println("");
    System.out.println("Valid values for <mode>:");
    for ( Helper.Mode elt : Helper.Mode.values() )
      System.out.println(" - " + elt);
  }
}
