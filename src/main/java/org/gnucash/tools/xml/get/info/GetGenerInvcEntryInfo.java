package org.gnucash.tools.xml.get.info;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashGenerInvoice;
import org.gnucash.api.read.GnuCashGenerInvoiceEntry;
import org.gnucash.api.read.impl.GnuCashFileImpl;
import org.gnucash.api.read.impl.spec.GnuCashCustomerInvoiceEntryImpl;
import org.gnucash.api.read.impl.spec.GnuCashEmployeeVoucherEntryImpl;
import org.gnucash.api.read.impl.spec.GnuCashJobInvoiceEntryImpl;
import org.gnucash.api.read.impl.spec.GnuCashVendorBillEntryImpl;
import org.gnucash.base.basetypes.simple.GCshGenerInvcEntrID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetGenerInvcEntryInfo extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetGenerInvcEntryInfo.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String              gcshFileName = null;
  private static GCshGenerInvcEntrID invcEntrID = null;
  
  private static boolean scriptMode = false; // ::TODO

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GetGenerInvcEntryInfo tool = new GetGenerInvcEntryInfo ();
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
    Option optFile = Option.builder("f")
      .required()
      .hasArg()
      .argName("file")
      .desc("GnuCash file")
      .longOpt("gnucash-file")
      .build();
      
    Option optInvcEntrID = Option.builder("entr")
      .required()
      .hasArg()
      .argName("UUID")
      .desc("Invoice-entry-ID")
      .longOpt("invoice-entry-id")
      .build();
    
    // The convenient ones
    // ::EMPTY        
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optInvcEntrID);
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
    
    GnuCashGenerInvoiceEntry entr = gcshFile.getGenerInvoiceEntryByID(invcEntrID);
    
    try
    {
      System.out.println("ID:                " + entr.getID());
    }
    catch ( Exception exc )
    {
      System.out.println("ID:                " + "ERROR");
    }
    
    try
    {
      System.out.println("toString (gener.): " + entr.toString());
    }
    catch ( Exception exc )
    {
      System.out.println("toString (gener.): " + "ERROR");
    }
    
    try
    {
      if ( entr.getType() == GnuCashGenerInvoice.TYPE_CUSTOMER )
      {
        GnuCashCustomerInvoiceEntryImpl spec = new GnuCashCustomerInvoiceEntryImpl(entr);
        System.out.println("toString (spec):   " + spec.toString());
      }
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_VENDOR )
      {
        GnuCashVendorBillEntryImpl spec = new GnuCashVendorBillEntryImpl(entr);
        System.out.println("toString (spec):   " + spec.toString());
      }
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_EMPLOYEE )
      {
        GnuCashEmployeeVoucherEntryImpl spec = new GnuCashEmployeeVoucherEntryImpl(entr);
        System.out.println("toString (spec):   " + spec.toString());
      }
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_JOB )
      {
        GnuCashJobInvoiceEntryImpl spec = new GnuCashJobInvoiceEntryImpl(entr);
        System.out.println("toString (spec):   " + spec.toString());
      }
    }
    catch ( Exception exc )
    {
      System.out.println("toString (spec):   " + "ERROR");
    }
    
    System.out.println("");
    try
    {
      System.out.println("Type:              " + entr.getType());
    }
    catch ( Exception exc )
    {
      System.out.println("Type:              " + "ERROR");
    }
    
    try
    {
      System.out.println("Gener. Invoice ID: " + entr.getGenerInvoiceID());
    }
    catch ( Exception exc )
    {
      System.out.println("Gener. Invoice ID: " + "ERROR");
    }

    try
    {
      System.out.println("Action:            " + entr.getAction());
    }
    catch ( Exception exc )
    {
      System.out.println("Action:            " + "ERROR");
    }

    try
    {
      System.out.println("Description:       '" + entr.getDescription() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Description:       " + "ERROR");
    }
        
    System.err.println("");
    System.err.println("Taxes:");
    try
    {
      if ( entr.getType() == GnuCashGenerInvoice.TYPE_CUSTOMER )
        System.out.println("Taxable:           " + entr.isCustInvcTaxable());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_VENDOR )
        System.out.println("Taxable:           " + entr.isVendBllTaxable());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_EMPLOYEE )
        System.out.println("Taxable:           " + entr.isEmplVchTaxable());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_JOB )
        System.out.println("Taxable:           " + entr.isJobInvcTaxable());
    }
    catch ( Exception exc )
    {
      System.out.println("Taxable:           " + "ERROR");
    }

    try
    {
      if ( entr.getType() == GnuCashGenerInvoice.TYPE_CUSTOMER )
        System.out.println("Tax perc.:         " + entr.getCustInvcApplicableTaxPercentFormatted());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_VENDOR )
        System.out.println("Tax perc.:         " + entr.getVendBllApplicableTaxPercentFormatted());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_EMPLOYEE )
        System.out.println("Tax perc.:         " + entr.getEmplVchApplicableTaxPercentFormatted());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_JOB )
        System.out.println("Tax perc.:         " + entr.getJobInvcApplicableTaxPercentFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Tax perc.:         " + "ERROR");
    }

    try
    {
      System.out.println("Tax-table:");
      if ( entr.getType() == GnuCashGenerInvoice.TYPE_CUSTOMER )
        System.out.println(entr.getCustInvcTaxTable().toString());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_VENDOR )
        System.out.println(entr.getVendBllTaxTable().toString());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_EMPLOYEE )
        System.out.println(entr.getEmplVchTaxTable().toString());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_JOB )
        System.out.println(entr.getJobInvcTaxTable().toString());
    }
    catch ( Exception exc )
    {
      System.out.println("ERROR");
    }

    System.out.println("");
    try
    {
      if ( entr.getType() == GnuCashGenerInvoice.TYPE_CUSTOMER )
        System.out.println("Price:             " + entr.getCustInvcPriceFormatted());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_VENDOR )
        System.out.println("Price:             " + entr.getVendBllPriceFormatted());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_EMPLOYEE )
        System.out.println("Price:             " + entr.getEmplVchPriceFormatted());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_JOB )
        System.out.println("Price:             " + entr.getJobInvcPriceFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Price:             " + "ERROR");
    }
    
    try
    {
      System.out.println("Quantity:          " + entr.getQuantityFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Quantity:          " + "ERROR");
    }

    try
    {
      if ( entr.getType() == GnuCashGenerInvoice.TYPE_CUSTOMER )
        System.out.println("Sum:               " + entr.getCustInvcSumFormatted());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_VENDOR )
        System.out.println("Sum:               " + entr.getVendBllSumFormatted());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_EMPLOYEE )
        System.out.println("Sum:               " + entr.getEmplVchSumFormatted());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_JOB )
        System.out.println("Sum:               " + entr.getJobInvcSumFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Sum:               " + "ERROR");
    }
    
    try
    {
      if ( entr.getType() == GnuCashGenerInvoice.TYPE_CUSTOMER )
        System.out.println("Sum w/ tax:        " + entr.getCustInvcSumInclTaxesFormatted());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_VENDOR )
        System.out.println("Sum w/ tax:        " + entr.getVendBllSumInclTaxesFormatted());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_EMPLOYEE )
        System.out.println("Sum w/ tax:        " + entr.getEmplVchSumInclTaxesFormatted());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_JOB )
        System.out.println("Sum w/ tax:        " + entr.getJobInvcSumInclTaxesFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Sum w/ tax:        " + "ERROR");
    }
    
    try
    {
      if ( entr.getType() == GnuCashGenerInvoice.TYPE_CUSTOMER )
        System.out.println("Sum w/o tax:       " + entr.getCustInvcSumExclTaxesFormatted());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_VENDOR )
        System.out.println("Sum w/o tax:       " + entr.getVendBllSumExclTaxesFormatted());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_EMPLOYEE )
        System.out.println("Sum w/o tax:       " + entr.getEmplVchSumExclTaxesFormatted());
      else if ( entr.getType() == GnuCashGenerInvoice.TYPE_JOB )
        System.out.println("Sum w/o tax:       " + entr.getJobInvcSumExclTaxesFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Sum w/o tax:       " + "ERROR");
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
    
    // <invoice-entry-id>
    try
    {
      invcEntrID = new GCshGenerInvcEntrID( cmdLine.getOptionValue("invoice-entry-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <invoice-entry-id>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("Invoice-entry ID: '" + invcEntrID + "'");
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GetGenerInvcEntryInfo", options );
  }
}
