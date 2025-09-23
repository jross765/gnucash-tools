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
import org.apache.log4j.Logger;
import org.gnucash.api.read.GnuCashGenerInvoice;
import org.gnucash.api.read.GnuCashGenerInvoiceEntry;
import org.gnucash.api.read.GnuCashTransaction;
import org.gnucash.api.read.impl.spec.GnuCashCustomerInvoiceEntryImpl;
import org.gnucash.api.read.impl.spec.GnuCashEmployeeVoucherEntryImpl;
import org.gnucash.api.read.impl.spec.GnuCashJobInvoiceEntryImpl;
import org.gnucash.api.read.impl.spec.GnuCashVendorBillEntryImpl;
import org.gnucash.api.read.spec.GnuCashCustomerInvoiceEntry;
import org.gnucash.api.read.spec.GnuCashEmployeeVoucherEntry;
import org.gnucash.api.read.spec.GnuCashJobInvoiceEntry;
import org.gnucash.api.read.spec.GnuCashVendorBillEntry;
import org.gnucash.api.read.spec.WrongInvoiceTypeException;
import org.gnucash.api.write.GnuCashWritableGenerInvoice;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.api.write.impl.GnuCashWritableGenerInvoiceImpl;
import org.gnucash.api.write.impl.spec.GnuCashWritableCustomerInvoiceImpl;
import org.gnucash.api.write.impl.spec.GnuCashWritableEmployeeVoucherImpl;
import org.gnucash.api.write.impl.spec.GnuCashWritableJobInvoiceImpl;
import org.gnucash.api.write.impl.spec.GnuCashWritableVendorBillImpl;
import org.gnucash.base.basetypes.simple.GCshGenerInvcID;
import org.gnucash.base.basetypes.simple.GCshID;
import org.gnucash.tools.CommandLineTool;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class TestGetGenerInvcInfo extends CommandLineTool
{
  // Logger
  private static Logger logger = Logger.getLogger(TestGetGenerInvcInfo.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String  gcshFileName     = null;
  private static GCshGenerInvcID invcID   = null;
  
  private static boolean showEntries      = false;
  private static boolean showTransactions = false;
  
  private static boolean scriptMode       = false; // ::TODO

  public static void main( String[] args )
  {
    try
    {
      TestGetGenerInvcInfo tool = new TestGetGenerInvcInfo ();
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
      .longOpt("GnuCash file")
      .build();
      
    Option optInvcID = Option.builder("invc")
      .required()
      .hasArg()
      .argName("UUID")
      .desc("Invoice-ID")
      .longOpt("invoice-id")
      .build();
    
    // The convenient ones
    Option optShowEntr = Option.builder("sentr")
      .desc("Show entries")
      .longOpt("show-entries")
      .build();
            
    Option optShowTrx = Option.builder("strx")
      .desc("Show transactions")
      .longOpt("show-transactions")
      .build();        
            
    options = new Options();
    options.addOption(optFile);
    options.addOption(optInvcID);
    options.addOption(optShowEntr);
    options.addOption(optShowTrx);
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
    
    GnuCashWritableGenerInvoice invc = gcshFile.getWritableGenerInvoiceByID(invcID);
    System.err.println("no. of gener. entries: " + invc.getGenerEntries().size());
    
    try
    {
      System.out.println("ID:                " + invc.getID());
    }
    catch ( Exception exc )
    {
      System.out.println("ID:                " + "ERROR");
    }
    
    try
    {
      System.out.println("toString (gener.): " + invc.toString());
    }
    catch ( Exception exc )
    {
      System.out.println("toString (gener.): " + "ERROR");
    }
    
    try
    {
      if ( invc.getType() == GnuCashGenerInvoice.TYPE_CUSTOMER )
      {
        GnuCashWritableCustomerInvoiceImpl spec = new GnuCashWritableCustomerInvoiceImpl((GnuCashWritableGenerInvoiceImpl) invc);
        System.out.println("toString (spec):   " + spec.toString());
      }
      else if ( invc.getType() == GnuCashGenerInvoice.TYPE_VENDOR )
      {
        GnuCashWritableVendorBillImpl spec = new GnuCashWritableVendorBillImpl((GnuCashWritableGenerInvoiceImpl) invc);
        System.out.println("toString (spec):   " + spec.toString());
      }
      else if ( invc.getType() == GnuCashGenerInvoice.TYPE_EMPLOYEE )
      {
        GnuCashWritableEmployeeVoucherImpl spec = new GnuCashWritableEmployeeVoucherImpl((GnuCashWritableGenerInvoiceImpl) invc);
        System.out.println("toString (spec):   " + spec.toString());
      }
      else if ( invc.getType() == GnuCashGenerInvoice.TYPE_JOB )
      {
        GnuCashWritableJobInvoiceImpl spec = new GnuCashWritableJobInvoiceImpl((GnuCashWritableGenerInvoiceImpl) invc);
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
      System.out.println("Owner (dir.):      " + invc.getOwnerID(GnuCashGenerInvoice.ReadVariant.DIRECT));
    }
    catch ( Exception exc )
    {
      System.out.println("Owner (dir.):      " + "ERROR");
    }

    try
    {
      System.out.println("Owner type:        " + invc.getOwnerType(GnuCashGenerInvoice.ReadVariant.DIRECT));
    }
    catch ( Exception exc )
    {
      System.out.println("Owner type:        " + "ERROR");
    }
    
    try
    {
      if ( invc.getOwnerType(GnuCashGenerInvoice.ReadVariant.DIRECT).equals(GnuCashGenerInvoice.TYPE_JOB) )
        System.out.println("Owner (via job):   " + invc.getOwnerID(GnuCashGenerInvoice.ReadVariant.VIA_JOB));
      else
        System.out.println("Owner (via job):   " + "n/a");
    }
    catch ( Exception exc )
    {
      System.out.println("Owner (via job):   " + "ERROR");
    }

    try
    {
      if ( invc.getOwnerType(GnuCashGenerInvoice.ReadVariant.DIRECT).equals(GnuCashGenerInvoice.TYPE_JOB) )
        System.out.println("Owning job's owner type: " + invc.getOwnerType(GnuCashGenerInvoice.ReadVariant.VIA_JOB));
      else
        System.out.println("Owning job's owner type: " + "n/a");
    }
    catch ( Exception exc )
    {
      System.out.println("Owning job's owner type:   " + "ERROR");
    }

    try
    {
      System.out.println("Number:            '" + invc.getNumber() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Number:            " + "ERROR");
    }

    try
    {
      System.out.println("Description:       '" + invc.getDescription() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Description:       " + "ERROR");
    }
        
    System.out.println("");
    try
    {
      System.out.println("Date opened:       " + invc.getDateOpened());
    }
    catch ( Exception exc )
    {
      System.out.println("Date opened:       " + "ERROR");
    }
    
    try
    {
      System.out.println("Date posted:       " + invc.getDatePosted());
    }
    catch ( Exception exc )
    {
      System.out.println("Date posted:       " + "ERROR");
    }

    System.out.println("");
    try
    {
      if ( invc.getType() == GnuCashGenerInvoice.TYPE_CUSTOMER )
        System.out.println("Amount w/o tax:       " + invc.getCustInvcAmountWithoutTaxesFormatted());
      else if ( invc.getType() == GnuCashGenerInvoice.TYPE_VENDOR )
        System.out.println("Amount w/o tax:       " + invc.getVendBllAmountWithoutTaxesFormatted());
      else if ( invc.getType() == GnuCashGenerInvoice.TYPE_EMPLOYEE )
        System.out.println("Amount w/o tax:       " + invc.getEmplVchAmountWithoutTaxesFormatted());
      else if ( invc.getType() == GnuCashGenerInvoice.TYPE_JOB )
        System.out.println("Amount w/o tax:       " + invc.getJobInvcAmountWithoutTaxesFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Amount w/o tax:       " + "ERROR");
    }

    try
    {
      if ( invc.getType() == GnuCashGenerInvoice.TYPE_CUSTOMER )
        System.out.println("Amount w/ tax:        " + invc.getCustInvcAmountWithTaxesFormatted());
      else if ( invc.getType() == GnuCashGenerInvoice.TYPE_VENDOR )
        System.out.println("Amount w/ tax:        " + invc.getVendBllAmountWithTaxesFormatted());
      else if ( invc.getType() == GnuCashGenerInvoice.TYPE_EMPLOYEE )
        System.out.println("Amount w/ tax:        " + invc.getEmplVchAmountWithTaxesFormatted());
      else if ( invc.getType() == GnuCashGenerInvoice.TYPE_JOB )
        System.out.println("Amount w/ tax:        " + invc.getJobInvcAmountWithTaxesFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Amount w/ tax:        " + "ERROR");
    }

    try
    {
      if ( invc.getType() == GnuCashGenerInvoice.TYPE_CUSTOMER )
        System.out.println("Amount paid w/ tax:   " + invc.getCustInvcAmountPaidWithTaxesFormatted());
      else if ( invc.getType() == GnuCashGenerInvoice.TYPE_VENDOR )
        System.out.println("Amount paid:          " + invc.getVendBllAmountPaidWithTaxesFormatted());
      else if ( invc.getType() == GnuCashGenerInvoice.TYPE_EMPLOYEE )
        System.out.println("Amount paid:          " + invc.getEmplVchAmountPaidWithTaxesFormatted());
      else if ( invc.getType() == GnuCashGenerInvoice.TYPE_JOB )
        System.out.println("Amount paid:          " + invc.getJobInvcAmountPaidWithTaxesFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Amount paid w/ tax:   " + "ERROR");
    }

    try
    {
      if ( invc.getType() == GnuCashGenerInvoice.TYPE_CUSTOMER )
        System.out.println("Amount Unpaid w/ tax: " + invc.getCustInvcAmountUnpaidWithTaxesFormatted());
      else if ( invc.getType() == GnuCashGenerInvoice.TYPE_VENDOR )
        System.out.println("Amount Unpaid:        " + invc.getVendBllAmountUnpaidWithTaxesFormatted());
      else if ( invc.getType() == GnuCashGenerInvoice.TYPE_EMPLOYEE )
        System.out.println("Amount Unpaid:        " + invc.getEmplVchAmountUnpaidWithTaxesFormatted());
      else if ( invc.getType() == GnuCashGenerInvoice.TYPE_JOB )
        System.out.println("Amount Unpaid:        " + invc.getJobInvcAmountUnpaidWithTaxesFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Amount Unpaid w/ tax: " + "ERROR");
    }

    try
    {
      if ( invc.getType() == GnuCashGenerInvoice.TYPE_CUSTOMER )
        System.out.println("Fully paid:           " + invc.isCustInvcFullyPaid());
      else if ( invc.getType() == GnuCashGenerInvoice.TYPE_VENDOR )
        System.out.println("Fully paid:           " + invc.isVendBllFullyPaid());
      else if ( invc.getType() == GnuCashGenerInvoice.TYPE_EMPLOYEE )
        System.out.println("Fully paid:           " + invc.isEmplVchFullyPaid());
      else if ( invc.getType() == GnuCashGenerInvoice.TYPE_JOB )
        System.out.println("Fully paid:           " + invc.isJobInvcFullyPaid());
    }
    catch ( Exception exc )
    {
      System.out.println("Fully paid:           " + "ERROR");
    }

    // ---
    
    if ( showEntries )
      showEntries(invc);

    if ( showTransactions )
      showTransactions(invc);
  }

  // -----------------------------------------------------------------

  private void showEntries(GnuCashGenerInvoice invc) throws Exception
  {
    System.out.println("");
    System.out.println("Entries:");
    
    for ( GnuCashGenerInvoiceEntry entry : invc.getGenerEntries() )
    {
      showOneEntry(entry);
    }
  }

  private void showOneEntry(GnuCashGenerInvoiceEntry entry) throws Exception
  {
    try
    {
      if ( entry.getType() == GnuCashGenerInvoice.TYPE_CUSTOMER )
      {
        try 
        {
          GnuCashCustomerInvoiceEntry entrySpec = new  GnuCashCustomerInvoiceEntryImpl(entry);
          System.out.println(" - " + entrySpec.toString());
        }
        catch ( Exception exc )
        {
          System.out.println(" - " + entry.toString());
        }
      }
      else if ( entry.getType() == GnuCashGenerInvoice.TYPE_VENDOR )
      {
        try 
        {
          GnuCashVendorBillEntry entrySpec = new  GnuCashVendorBillEntryImpl(entry);
          System.out.println(" - " + entrySpec.toString());
        }
        catch ( Exception exc )
        {
          System.out.println(" - " + entry.toString());
        }
      }
      else if ( entry.getType() == GnuCashGenerInvoice.TYPE_EMPLOYEE )
      {
        try 
        {
          GnuCashEmployeeVoucherEntry entrySpec = new  GnuCashEmployeeVoucherEntryImpl(entry);
          System.out.println(" - " + entrySpec.toString());
        }
        catch ( Exception exc )
        {
          System.out.println(" - " + entry.toString());
        }
      }
      else if ( entry.getType() == GnuCashGenerInvoice.TYPE_JOB )
      {
        try 
        {
          GnuCashJobInvoiceEntry entrySpec = new  GnuCashJobInvoiceEntryImpl(entry);
          System.out.println(" - " + entrySpec.toString());
        }
        catch ( Exception exc )
        {
          System.out.println(" - " + entry.toString());
        }
      }
    }
    catch (WrongInvoiceTypeException e)
    {
      System.out.println(" - " + "ERROR");
    }
  }

  private void showTransactions(GnuCashGenerInvoice invc)
  {
    System.out.println("");
    System.out.println("Transactions:");
    
    try
    {
      System.out.println("Posting transaction: " + invc.getPostTransaction());
    }
    catch ( Exception exc )
    {
      System.out.println("Posting transaction: " + "ERROR");
    }

    System.out.println("Paying transactions:");
    for ( GnuCashTransaction trx : invc.getPayingTransactions() )
    {
      try 
      {
        System.out.println(" - " + trx.toString());
      }
      catch ( Exception exc )
      {
        System.out.println(" - " + "ERROR");
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
    
    if ( ! scriptMode )
      System.err.println("GnuCash file: '" + gcshFileName + "'");
    
    // <invoice-id>
    try
    {
      invcID = new GCshGenerInvcID( cmdLine.getOptionValue("invoice-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <invoice-id>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("Invoice ID: '" + invcID + "'");

    // <show-entries>
    if ( cmdLine.hasOption("show-entries"))
    {
      showEntries = true;
    }
    else
    {
      showEntries = false;
    }
    
    if ( ! scriptMode )
      System.err.println("Show entries: " + showEntries);

    // <show-entries>
    if ( cmdLine.hasOption("show-transactions"))
    {
      showTransactions = true;
    }
    else
    {
      showTransactions = false;
    }
    
    if ( ! scriptMode )
      System.err.println("Show transactions: " + showTransactions);
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "TestGetGenerInvcInfo", options );
  }
}
