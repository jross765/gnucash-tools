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
import org.apache.log4j.Logger;
import org.gnucash.api.read.GnuCashCustomer;
import org.gnucash.api.read.GnuCashGenerInvoice;
import org.gnucash.api.read.aux.GCshBillTerms;
import org.gnucash.api.read.aux.GCshTaxTable;
import org.gnucash.api.read.impl.GnuCashCustomerImpl;
import org.gnucash.api.read.spec.GnuCashCustomerJob;
import org.gnucash.api.write.GnuCashWritableCustomer;
import org.gnucash.api.write.impl.GnuCashWritableCustomerImpl;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.api.write.spec.GnuCashWritableCustomerInvoice;
import org.gnucash.base.basetypes.simple.GCshCustID;
import org.gnucash.base.basetypes.simple.GCshID;
import org.gnucash.base.basetypes.simple.aux.GCshBllTrmID;
import org.gnucash.base.basetypes.simple.aux.GCshTaxTabID;
import org.gnucash.tools.CommandLineTool;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class TestGetCustInfo extends CommandLineTool
{
  // Logger
  private static Logger logger = Logger.getLogger(TestGetCustInfo.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String      gcshFileName = null;
  private static Helper.Mode mode         = null;
  private static GCshCustID  custID       = null;
  private static String      custName     = null;
  
  private static boolean showJobs      = false;
  private static boolean showInvoices  = false;
  
  private static boolean scriptMode = false; // ::TODO

  public static void main( String[] args )
  {
    try
    {
      TestGetCustInfo tool = new TestGetCustInfo ();
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
      .longOpt("GnuCash file")
      .build();
      
    Option optMode = Option.builder("m")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode")
      .longOpt("mode")
      .build();
        
    Option optCustID = Option.builder("cust")
      .hasArg()
      .argName("UUID")
      .desc("Customer-ID")
      .longOpt("customer-id")
      .build();
    
    Option optCustName = Option.builder("n")
      .hasArg()
      .argName("name")
      .desc("Customer name")
      .longOpt("name")
      .build();
      
    // The convenient ones
    Option optShowJob = Option.builder("sjob")
      .desc("Show jobs")
      .longOpt("show-jobs")
      .build();
            
    Option optShowInvc = Option.builder("sinvc")
      .desc("Show invoices")
      .longOpt("show-invoices")
      .build();
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    options.addOption(optCustID);
    options.addOption(optCustName);
    options.addOption(optShowJob);
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
    GnuCashWritableFileImpl gcshFile = new GnuCashWritableFileImpl(new File(gcshFileName));
    
    GnuCashWritableCustomer cust = null;
    if ( mode == Helper.Mode.ID )
    {
      cust = gcshFile.getWritableCustomerByID(custID);
      if ( cust == null )
      {
        System.err.println("Could not find customers matching that name.");
        throw new NoEntryFoundException();
      }
    }
    else if ( mode == Helper.Mode.NAME )
    {
      Collection<GnuCashCustomer> custList = null; 
      custList = gcshFile.getCustomersByName(custName);
      if ( custList.size() == 0 ) 
      {
        System.err.println("Found no customer with that name.");
        throw new NoEntryFoundException();
      }
      else if ( custList.size() > 1 ) 
      {
        System.err.println("Found " + custList.size() + " customers matching that name.");
        System.err.println("Please specify more precisely.");
        throw new TooManyEntriesFoundException();
      }
      cust = new GnuCashWritableCustomerImpl( (GnuCashCustomerImpl) custList.iterator().next() );
    }
    
    try
    {
      System.out.println("ID:                " + cust.getID());
    }
    catch ( Exception exc )
    {
      System.out.println("ID:                " + "ERROR");
    }
    
    try
    {
      System.out.println("toString:          " + cust.toString());
    }
    catch ( Exception exc )
    {
      System.out.println("toString:          " + "ERROR");
    }
    
    try
    {
      System.out.println("Number:            '" + cust.getNumber() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Number:            " + "ERROR");
    }
    
    try
    {
      System.out.println("Name:              '" + cust.getName() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Name:              " + "ERROR");
    }
    
    try
    {
      System.out.println("Address:           '" + cust.getAddress() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Address:           " + "ERROR");
    }
    
    System.out.println("");
    try
    {
      System.out.println("Discount:          " + cust.getDiscount());
    }
    catch ( Exception exc )
    {
      System.out.println("Discount:          " + "ERROR");
    }
    
    try
    {
      System.out.println("Credit:            " + cust.getCredit());
    }
    catch ( Exception exc )
    {
      System.out.println("Credit:            " + "ERROR");
    }
    
    System.out.println("");
    try
    {
      GCshTaxTabID taxTabID = cust.getTaxTableID();
      System.out.println("Tax table ID:      " + taxTabID);
      
      if ( cust.getTaxTableID() != null )
      {
        try 
        {
          GCshTaxTable taxTab = gcshFile.getTaxTableByID(taxTabID);
          System.out.println("Tax table:        " + taxTab.toString());
        }
        catch ( Exception exc2 )
        {
          System.out.println("Tax table:        " + "ERROR");
        }
      }
    }
    catch ( Exception exc )
    {
      System.out.println("Tax table ID:      " + "ERROR");
    }
    
    System.out.println("");
    try
    {
      GCshBllTrmID bllTrmID = cust.getTermsID();
      System.out.println("Bill terms ID:     " + bllTrmID);
      
      if ( cust.getTermsID() != null )
      {
        try 
        {
          GCshBillTerms bllTrm = gcshFile.getBillTermsByID(bllTrmID);
          System.out.println("Bill Terms:        " + bllTrm.toString());
        }
        catch ( Exception exc2 )
        {
          System.out.println("Bill Terms:        " + "ERROR");
        }
      }
    }
    catch ( Exception exc )
    {
      System.out.println("Bill terms ID:     " + "ERROR");
    }
    
    System.out.println("");
    System.out.println("Income generated:");
    try
    {
      System.out.println(" - direct:  " + cust.getIncomeGeneratedFormatted(GnuCashGenerInvoice.ReadVariant.DIRECT));
    }
    catch ( Exception exc )
    {
      System.out.println(" - direct:  " + "ERROR");
    }

    try
    {
      System.out.println(" - via all jobs:  " + cust.getIncomeGeneratedFormatted(GnuCashGenerInvoice.ReadVariant.VIA_JOB));
    }
    catch ( Exception exc )
    {
      System.out.println(" - via all jobs:  " + "ERROR");
    }

    System.out.println("Outstanding value:");
    try
    {
      System.out.println(" - direct: " + cust.getOutstandingValueFormatted(GnuCashGenerInvoice.ReadVariant.DIRECT));
    }
    catch ( Exception exc )
    {
      System.out.println(" - direct: " + "ERROR");
    }
    
    try
    {
      System.out.println(" - via all jobs: " + cust.getOutstandingValueFormatted(GnuCashGenerInvoice.ReadVariant.VIA_JOB));
    }
    catch ( Exception exc )
    {
      System.out.println(" - via all jobs: " + "ERROR");
    }
    
    // ---
    
    if ( showJobs )
      showJobs(cust);
        
    if ( showInvoices )
      showInvoices((GnuCashWritableCustomerImpl) cust);
  }

  // -----------------------------------------------------------------

  private void showJobs(GnuCashCustomer cust)
  {
    System.out.println("");
    System.out.println("Jobs:");
    for ( GnuCashCustomerJob job : cust.getJobs() )
    {
      System.out.println(" - " + job.toString());
    }
  }

  private void showInvoices(GnuCashWritableCustomerImpl cust) throws Exception
  {
    System.out.println("");
    System.out.println("Invoices:");

    System.out.println("Number of open invoices: " + cust.getNofOpenInvoices());

    System.out.println("");
    System.out.println("Paid invoices (direct):");
    for ( GnuCashWritableCustomerInvoice invc : cust.getPaidWritableInvoices_direct() )
    {
      System.out.println(" - " + invc.toString());
    }

//    System.out.println("");
//    System.out.println("Paid invoices (via all jobs):");
//    for ( GnuCashJobInvoice invc : cust.getPaidWritableInvoices_viaAllJobs() )
//    {
//      System.out.println(" - " + invc.toString());
//    }

    System.out.println("");
    System.out.println("Unpaid invoices (direct):");
    for ( GnuCashWritableCustomerInvoice invc : cust.getUnpaidWritableInvoices_direct() )
    {
      System.out.println(" - " + invc.toString());
    }

//    System.out.println("");
//    System.out.println("Unpaid invoices (via all jobs):");
//    for ( GnuCashJobInvoice invc : cust.getUnpaidWritableInvoices_viaAllJobs() )
//    {
//      System.out.println(" - " + invc.toString());
//    }
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

    // <GnuCash file>
    try
    {
      gcshFileName = cmdLine.getOptionValue("GnuCash file");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <GnuCash file>");
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

    // <customer-id>
    if ( cmdLine.hasOption("customer-id") )
    {
      if ( mode != Helper.Mode.ID )
      {
        System.err.println("<customer-id> must only be set with <mode> = '" + Helper.Mode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        custID = new GCshCustID( cmdLine.getOptionValue("customer-id") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <customer-id>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( mode == Helper.Mode.ID )
      {
        System.err.println("<customer-id> must be set with <mode> = '" + Helper.Mode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }      
    }
    
    if ( ! scriptMode )
      System.err.println("Customer ID: '" + custID + "'");

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
        custName = cmdLine.getOptionValue("name");
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
      System.err.println("Name: '" + custName + "'");

    // <show-jobs>
    if ( cmdLine.hasOption("show-jobs"))
    {
      showJobs = true;
    }
    else
    {
      showJobs = false;
    }
    
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
    formatter.printHelp( "TestGetCustInfo", options );
    
    System.out.println("");
    System.out.println("Valid values for <mode>:");
    for ( Helper.Mode elt : Helper.Mode.values() )
      System.out.println(" - " + elt);
  }
}
