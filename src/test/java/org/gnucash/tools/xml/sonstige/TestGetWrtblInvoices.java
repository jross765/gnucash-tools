package org.gnucash.tools.xml.sonstige;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.gnucash.api.read.GnuCashGenerInvoice;
import org.gnucash.api.write.GnuCashWritableCustomer;
import org.gnucash.api.write.GnuCashWritableEmployee;
import org.gnucash.api.write.GnuCashWritableGenerInvoice;
import org.gnucash.api.write.GnuCashWritableGenerJob;
import org.gnucash.api.write.GnuCashWritableVendor;
import org.gnucash.api.write.impl.GnuCashWritableCustomerImpl;
import org.gnucash.api.write.impl.GnuCashWritableEmployeeImpl;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.api.write.impl.GnuCashWritableGenerJobImpl;
import org.gnucash.api.write.impl.GnuCashWritableVendorImpl;
import org.gnucash.base.basetypes.simple.GCshCustID;
import org.gnucash.base.basetypes.simple.GCshEmplID;
import org.gnucash.base.basetypes.simple.GCshGenerJobID;
import org.gnucash.base.basetypes.simple.GCshVendID;
import org.gnucash.tools.CommandLineTool;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class TestGetWrtblInvoices extends CommandLineTool
{
  // Logger
  private static Logger logger = Logger.getLogger(TestGetWrtblInvoices.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String         gcshFileName = null;
  private static GCshCustID     custID       = null;
  private static GCshVendID     vendID       = null;
  private static GCshEmplID     emplID       = null;
  private static GCshGenerJobID jobID        = null;
  
  public static void main( String[] args )
  {
    try
    {
      TestGetWrtblInvoices tool = new TestGetWrtblInvoices ();
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
    custID = new GCshCustID();
    vendID = new GCshVendID();
	emplID = new GCshEmplID();
	jobID  = new GCshGenerJobID();

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
      
    Option optCustID = Option.builder("cust")
      .hasArg()
      .argName("UUID")
      .desc("Customer-ID")
      .longOpt("customer-id")
      .build();
    
    Option optVendID = Option.builder("vend")
      .hasArg()
      .argName("UUID")
      .desc("Vendor-ID")
      .longOpt("vendor-id")
      .build();
    	    
    Option optEmplID = Option.builder("empl")
      .hasArg()
      .argName("UUID")
      .desc("Employee-ID")
      .longOpt("employee-id")
      .build();
    	    	    
    Option optJobID = Option.builder("job")
      .hasArg()
      .argName("UUID")
      .desc("Job-ID")
      .longOpt("job-id")
      .build();
    	    	    
    // The convenient ones
    // ::EMPTY
            
    options = new Options();
    options.addOption(optFile);
    options.addOption(optCustID);
    options.addOption(optVendID);
    options.addOption(optEmplID);
    options.addOption(optJobID);
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
    
    if ( custID.isSet() ) 
    {
        System.out.println("");
        GnuCashWritableCustomer cust = gcshFile.getWritableCustomerByID(custID);
        System.out.println("Customer: " + cust.toString());
        
        System.out.println("Writable cust./job invoices as gener. invoices:");
        for ( GnuCashGenerInvoice invc : ((GnuCashWritableCustomerImpl) cust).getInvoices() )
        {
            System.out.println(" - " + ((GnuCashWritableGenerInvoice) invc).toString());
        }
    }
    
    if ( vendID.isSet() ) 
    {
        System.out.println("");
        GnuCashWritableVendor vend = gcshFile.getWritableVendorByID(vendID);
        System.out.println("Vendor: " + vend.toString());
        
        System.out.println("Writable vend./job bills as gener. invoices:");
        for ( GnuCashGenerInvoice invc : ((GnuCashWritableVendorImpl) vend).getBills() )
        {
            System.out.println(" - " + ((GnuCashWritableGenerInvoice) invc).toString());
        }
    }
    
    if ( emplID.isSet() ) 
    {
        System.out.println("");
        GnuCashWritableEmployee empl = gcshFile.getWritableEmployeeByID(emplID);
        System.out.println("Employee: " + empl.toString());
        
        System.out.println("Writable empl. vouchers as gener. invoices:");
        for ( GnuCashGenerInvoice invc : ((GnuCashWritableEmployeeImpl) empl).getVouchers() )
        {
            System.out.println(" - " + ((GnuCashWritableGenerInvoice) invc).toString());
        }
    }
    
    if ( jobID.isSet() ) 
    {
        System.out.println("");
        GnuCashWritableGenerJob job = gcshFile.getWritableGenerJobByID(jobID);
        System.out.println("(Gener.) Job: " + job.toString());
        
        System.out.println("Writable invoices:");
        for ( GnuCashGenerInvoice invc : ((GnuCashWritableGenerJobImpl) job).getInvoices() )
        {
            System.out.println(" - " + ((GnuCashWritableGenerInvoice) invc).toString());
        }
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
    
    System.err.println("GnuCash file: '" + gcshFileName + "'");
    
    // <customer-id>
    if ( cmdLine.hasOption("customer-id") )
    {
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
    
    System.err.println("Customer ID: " + custID);
    
    // <vendor-id>
    if ( cmdLine.hasOption("vendor-id") )
    {
        try
        {
          vendID = new GCshVendID( cmdLine.getOptionValue("vendor-id") );
        }
        catch ( Exception exc )
        {
          System.err.println("Could not parse <vendor-id>");
          throw new InvalidCommandLineArgsException();
        }
    }
    
    System.err.println("Vendor ID:   " + vendID);
    
    // <employee-id>
    if ( cmdLine.hasOption("employee-id") )
    {
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
    
    System.err.println("Employee ID: " + emplID);
    
    // <job-id>
    if ( cmdLine.hasOption("job-id") )
    {
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
    
    System.err.println("Job ID:      " + jobID);
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "TestGetWrtblInvoices", options );
  }
}
