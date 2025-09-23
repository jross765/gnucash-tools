package org.gnucash.tools.xml.gen.simple;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashAccount;
import org.gnucash.api.read.GnuCashCustomer;
import org.gnucash.api.read.GnuCashEmployee;
import org.gnucash.api.read.GnuCashGenerInvoice;
import org.gnucash.api.read.GnuCashGenerInvoiceEntry;
import org.gnucash.api.read.GnuCashGenerJob;
import org.gnucash.api.read.GnuCashVendor;
import org.gnucash.api.read.OwnerNotFoundException;
import org.gnucash.api.read.aux.GCshOwner;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.api.write.spec.GnuCashWritableCustomerInvoice;
import org.gnucash.api.write.spec.GnuCashWritableCustomerInvoiceEntry;
import org.gnucash.api.write.spec.GnuCashWritableEmployeeVoucher;
import org.gnucash.api.write.spec.GnuCashWritableEmployeeVoucherEntry;
import org.gnucash.api.write.spec.GnuCashWritableJobInvoice;
import org.gnucash.api.write.spec.GnuCashWritableJobInvoiceEntry;
import org.gnucash.api.write.spec.GnuCashWritableVendorBill;
import org.gnucash.api.write.spec.GnuCashWritableVendorBillEntry;
import org.gnucash.base.basetypes.simple.GCshAcctID;
import org.gnucash.base.basetypes.simple.GCshCustID;
import org.gnucash.base.basetypes.simple.GCshEmplID;
import org.gnucash.base.basetypes.simple.GCshGenerJobID;
import org.gnucash.base.basetypes.simple.GCshID;
import org.gnucash.base.basetypes.simple.GCshVendID;
import org.gnucash.tools.CommandLineTool;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.AccountNotFoundException;
import xyz.schnorxoborx.base.beanbase.WrongAccountTypeException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.dateutils.LocalDateHelpers;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class GenInvc extends CommandLineTool
{
  enum InvoiceType
  {
    CUSTOMER,
    VENDOR,
    EMPLOYEE,
    JOB
  }
  
  // For account specification:
  enum Mode
  { 
    ID,
    NAME
  }
  
  // Logger
  @SuppressWarnings("unused")
private static final Logger LOGGER = LoggerFactory.getLogger(GenInvc.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String           gcshInFileName = null;
  private static String           gcshOutFileName = null;
  private static InvoiceType      type = null;
  private static Mode             mode = null;
  private static GCshID           ownerID = null;
  private static String           incExpAcctIDOrName = null;
  private static String           recvblPayblAcctIDOrName = null;
  private static String           number = null;
  private static LocalDate        dateOpen = null;
  private static LocalDate        datePost = null;
  private static LocalDate        dateDue = null;
  private static FixedPointNumber amount = null;

  private static GnuCashAccount   incExpAcct = null;
  private static GnuCashAccount   recvblPayblAcct = null;

  // -----------------------------------------------------------------
  
  public static void main( String[] args )
  {
    try
    {
      GenInvc tool = new GenInvc ();
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
    Option optFileIn = Option.builder("if")
      .required()
      .hasArg()
      .argName("file")
      .desc("GnuCash file (in)")
      .longOpt("gnucash-in-file")
      .build();
          
    Option optFileOut = Option.builder("of")
      .required()
      .hasArg()
      .argName("file")
      .desc("GnuCash file (out)")
      .longOpt("gnucash-out-file")
      .build();
      
    Option optType = Option.builder("t")
      .required()
      .hasArg()
      .argName("type")
      .desc("Invoice type")
      .longOpt("type")
      .build();
      
    Option optMode = Option.builder("m")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Mode to specify accounts by")
      .longOpt("mode")
      .build();
        
    Option optOwnerID = Option.builder("own")
      .required()
      .hasArg()
      .argName("UUID")
      .desc("Owner ID")
      .longOpt("owner-id")
      .build();
        
    Option optIncExpAcctIDOrName = Option.builder("ieacct")
      .required()
      .hasArg()
      .argName("UUID")
      .desc("Income/expense account ID or name")
      .longOpt("income-expense-account")
      .build();
            
    Option optRecvblPayblAcctIDOrName = Option.builder("rpacct")
      .required()
      .hasArg()
      .argName("UUID")
      .desc("Receivable/payable account ID or name")
      .longOpt("receivable-payable-account")
      .build();
            
    Option optNumber = Option.builder("no")
      .required()
      .hasArg()
      .argName("number")
      .desc("Invoice number")
      .longOpt("number")
      .build();
    
    Option optOpenDate = Option.builder("odat")
      .required()
      .hasArg()
      .argName("date")
      .desc("Date opened")
      .longOpt("opened-date")
      .build();

    Option optPostDate = Option.builder("pdat")
      .required()
      .hasArg()
      .argName("date")
      .desc("Post date")
      .longOpt("post-date")
      .build();

    Option optDueDate = Option.builder("ddat")
      .required()
      .hasArg()
      .argName("date")
      .desc("Due date")
      .longOpt("due-date")
      .build();

    Option optAmount = Option.builder("amt")
      .required()
      .hasArg()
      .argName("amount")
      .desc("Amount")
      .longOpt("amount")
      .build();
                
    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optType);
    options.addOption(optMode);
    options.addOption(optOwnerID);
    options.addOption(optIncExpAcctIDOrName);
    options.addOption(optRecvblPayblAcctIDOrName);
    options.addOption(optNumber);
    options.addOption(optOpenDate);
    options.addOption(optPostDate);
    options.addOption(optDueDate);
    options.addOption(optAmount);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cs) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    GnuCashWritableFileImpl gcshFile = new GnuCashWritableFileImpl(new File(gcshInFileName));

    instantiateAccounts(gcshFile);
    
    GnuCashGenerInvoice invc = null;
    if ( type == InvoiceType.CUSTOMER )
      invc = doCustomer(gcshFile);
    else if ( type == InvoiceType.VENDOR )
      invc = doVendor(gcshFile);
    else if ( type == InvoiceType.EMPLOYEE )
      invc = doEmployee(gcshFile);
    else if ( type == InvoiceType.JOB )
      invc = doJob(gcshFile);

    System.out.println("Invoice to write: " + invc.toString());
    gcshFile.writeFile(new File(gcshOutFileName));
    System.out.println("OK");
  }
  
  // -----------------------------------------------------------------

  private void instantiateAccounts(GnuCashWritableFileImpl gcshFile)
      throws AccountNotFoundException
  {
    try 
    {
      if ( mode == Mode.ID )
      {
        incExpAcct = gcshFile.getAccountByID(new GCshAcctID(incExpAcctIDOrName));
      }
      else if ( mode == Mode.NAME )
      {
        incExpAcct = gcshFile.getAccountByNameUniq(incExpAcctIDOrName, true);
      }
      System.err.println("Income/expense account:     " + 
                         "Code: " + incExpAcct.getCode() + ", " +
                         "Type: " + incExpAcct.getType() + ", " + 
                         "Name: '" + incExpAcct.getQualifiedName() + "'");
    
      if ( incExpAcct.getType() != GnuCashAccount.Type.INCOME && 
           incExpAcct.getType() != GnuCashAccount.Type.EXPENSE )
      {
        System.err.println("Error: Account is neither an income nor an expenses account");
        throw new WrongAccountTypeException();
      }
    }
    catch ( Exception exc )
    {
      System.err.println("Error: Could not instantiate account with ID/name '" + incExpAcctIDOrName + "'");
      throw new AccountNotFoundException();
    }
    
    try 
    {
      if ( mode == Mode.ID )
      {
        recvblPayblAcct = gcshFile.getAccountByID(new GCshAcctID(recvblPayblAcctIDOrName));
      }
      else if ( mode == Mode.NAME )
      {
        recvblPayblAcct = gcshFile.getAccountByNameUniq(recvblPayblAcctIDOrName, true);
      }
      System.err.println("Receivable/payable account: " + 
                         "Code: " + recvblPayblAcct.getCode() + ", " +
                         "Type: " + recvblPayblAcct.getType() + ", " + 
                         "Name: '" + recvblPayblAcct.getQualifiedName() + "'");

      if ( recvblPayblAcct.getType() != GnuCashAccount.Type.RECEIVABLE && 
           recvblPayblAcct.getType() != GnuCashAccount.Type.PAYABLE )
      {
        System.err.println("Error: Account is neither a receivable nor a payable account");
        throw new WrongAccountTypeException();
      }
    }
    catch ( Exception exc )
    {
      System.err.println("Error: Could not instantiate account with ID '" + recvblPayblAcctIDOrName + "'");
      throw new AccountNotFoundException();
    }
  }

  private GnuCashWritableCustomerInvoice doCustomer(GnuCashWritableFileImpl gcshFile) throws Exception
  {
    if ( incExpAcct.getType() != GnuCashAccount.Type.INCOME )
    {
      System.err.println("Error: You selected a customer invoice, but account " + incExpAcctIDOrName.toString() + " is not an income account");
      throw new WrongAccountTypeException();
    }
    
    if ( recvblPayblAcct.getType() != GnuCashAccount.Type.RECEIVABLE )
    {
      System.err.println("Error: You selected a customer invoice, but account " + recvblPayblAcct.toString() + " is not an income account");
      throw new WrongAccountTypeException();
    }
    
    GnuCashCustomer cust = null;
    try
    {
      cust = gcshFile.getWritableCustomerByID(new GCshCustID(ownerID));
      System.err.println("Customer: " + cust.getNumber() + " (" + cust.getName() + ")");
    }
    catch ( Exception exc )
    {
      System.err.println("Error: No customer with ID '" + ownerID + "' found");
      throw new OwnerNotFoundException();
    }
    
    GnuCashWritableCustomerInvoice invc = gcshFile.createWritableCustomerInvoice(
                                                        number, 
                                                        cust, 
                                                        incExpAcct, recvblPayblAcct, 
                                                        dateOpen, datePost, dateDue);
    invc.setDescription("Generated by GenInvc " + LocalDateTime.now().toString());
    
    GnuCashWritableCustomerInvoiceEntry entry = invc.createEntry(incExpAcct, 
                                                                 new FixedPointNumber(amount), 
                                                                 new FixedPointNumber(1),
                                                                 "DE_USt_Std");
    entry.setAction(GnuCashGenerInvoiceEntry.Action.HOURS);
    entry.setDescription("Entry no. 1");
    entry.setDate(dateOpen);

    return invc;
  }

  private GnuCashWritableVendorBill doVendor(GnuCashWritableFileImpl gcshFile) throws Exception
  {
    if ( incExpAcct.getType() != GnuCashAccount.Type.EXPENSE )
    {
      System.err.println("Error: You selected a vendor bill, but account " + incExpAcctIDOrName.toString() + " is not an expenses account");
      throw new WrongAccountTypeException();
    }
    
    if ( recvblPayblAcct.getType() != GnuCashAccount.Type.PAYABLE )
    {
      System.err.println("Error: You selected a vendor bill, but account " + recvblPayblAcct.toString() + " is not a payable account");
      throw new WrongAccountTypeException();
    }
    
    GnuCashVendor vend = null;
    try
    {
      vend = gcshFile.getVendorByID(new GCshVendID(ownerID));
      System.err.println("Vendor: " + vend.getNumber() + " (" + vend.getName() + ")");
    }
    catch ( Exception exc )
    {
      System.err.println("Error: No vendor with ID '" + ownerID + "' found");
      throw new OwnerNotFoundException();
    }
    
    GnuCashWritableVendorBill bll = gcshFile.createWritableVendorBill(
                                                    number, 
                                                    vend, 
                                                    incExpAcct, recvblPayblAcct, 
                                                    dateOpen, datePost, dateDue);
    bll.setDescription("Generated by GenInvc " + LocalDateTime.now().toString());
    
    GnuCashWritableVendorBillEntry entry = bll.createEntry(incExpAcct, 
                                                           new FixedPointNumber(amount), 
                                                           new FixedPointNumber(1),
                                                           "DE_USt_Std");
    entry.setAction(GnuCashGenerInvoiceEntry.Action.HOURS);
    entry.setDescription("Entry no. 2");
    entry.setDate(dateOpen);

    return bll;
  }
  
  private GnuCashWritableEmployeeVoucher doEmployee(GnuCashWritableFileImpl gcshFile) throws Exception
  {
    if ( incExpAcct.getType() != GnuCashAccount.Type.EXPENSE )
    {
      System.err.println("Error: You selected an employee voucher, but account " + incExpAcctIDOrName.toString() + " is not an expenses account");
      throw new WrongAccountTypeException();
    }
    
    if ( recvblPayblAcct.getType() != GnuCashAccount.Type.PAYABLE )
    {
      System.err.println("Error: You selected an employee voucher, but account " + recvblPayblAcct.toString() + " is not a payable account");
      throw new WrongAccountTypeException();
    }
    
    GnuCashEmployee empl = null;
    try
    {
      empl = gcshFile.getEmployeeByID(new GCshEmplID(ownerID));
      System.err.println("Employee: " + empl.getNumber() + " (" + empl.getUserName() + ")");
    }
    catch ( Exception exc )
    {
      System.err.println("Error: No employee with ID '" + ownerID + "' found");
      throw new OwnerNotFoundException();
    }
    
    GnuCashWritableEmployeeVoucher vch = gcshFile.createWritableEmployeeVoucher(
                                                    number, 
                                                    empl, 
                                                    incExpAcct, recvblPayblAcct, 
                                                    dateOpen, datePost, dateDue);
    vch.setDescription("Generated by GenInvc " + LocalDateTime.now().toString());
    
    GnuCashWritableEmployeeVoucherEntry entry = vch.createEntry(incExpAcct, 
                                                                new FixedPointNumber(amount),
                                                                new FixedPointNumber(1),
                                                                "DE_USt_Std");
    entry.setAction(GnuCashGenerInvoiceEntry.Action.HOURS);
    entry.setDescription("Entry no. 2");
    entry.setDate(dateOpen);

    return vch;
  }
  
  private GnuCashWritableJobInvoice doJob(GnuCashWritableFileImpl gcshFile) throws Exception
  {
    GnuCashGenerJob job = null;
    try
    {
      job = gcshFile.getGenerJobByID(new GCshGenerJobID(ownerID));
      System.err.println("(Gener.) job: " + job.getNumber() + " (" + job.getName() + ")");
    }
    catch ( Exception exc )
    {
      System.err.println("Error: No (gener.) job with ID '" + ownerID + "' found");
      throw new OwnerNotFoundException();
    }
    
    if ( job.getOwnerType() == GCshOwner.Type.CUSTOMER &&
         incExpAcct.getType() != GnuCashAccount.Type.INCOME )
    {
      System.err.println("Error: You selected a customer job invoice, but account " + incExpAcctIDOrName.toString() + " is not an income account");
      throw new WrongAccountTypeException();
    }
    else if ( job.getOwnerType() == GCshOwner.Type.VENDOR &&
              incExpAcct.getType() != GnuCashAccount.Type.EXPENSE )
    {
      System.err.println("Error: You selected a vendor job invoice, but account " + incExpAcctIDOrName.toString() + " is not an expenses account");
      throw new WrongAccountTypeException();
    }
    
    if ( job.getOwnerType() == GCshOwner.Type.CUSTOMER &&
         recvblPayblAcct.getType() != GnuCashAccount.Type.RECEIVABLE )
    {
      System.err.println("Error: You selected a customer job invoice, but account " + recvblPayblAcct.toString() + " is not a receivable account");
      throw new WrongAccountTypeException();
    }
    else if ( job.getOwnerType() == GCshOwner.Type.VENDOR &&
              recvblPayblAcct.getType() != GnuCashAccount.Type.PAYABLE )
    {
      System.err.println("Error: You selected a vendor job invoice, but account " + recvblPayblAcct.toString() + " is not a payable account");
      throw new WrongAccountTypeException();
    }
    
    GnuCashWritableJobInvoice invc = gcshFile.createWritableJobInvoice(
                                                    number, 
                                                    job, 
                                                    incExpAcct, recvblPayblAcct, 
                                                    dateOpen, datePost, dateDue);
    invc.setDescription("Generated by GenInvc " + LocalDateTime.now().toString());
    
    GnuCashWritableJobInvoiceEntry entry = invc.createEntry(incExpAcct, 
                                                            new FixedPointNumber(amount), 
                                                            new FixedPointNumber(1),
                                                            "DE_USt_Std");
    entry.setAction(GnuCashGenerInvoiceEntry.Action.HOURS);
    entry.setDescription("Entry no. 2");
    entry.setDate(dateOpen);

    return invc;
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
    
    // <type>
    try
    {
      type = InvoiceType.valueOf(cmdLine.getOptionValue("type"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <type>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Type: " + type);

    // <mode>
    try
    {
      mode = Mode.valueOf(cmdLine.getOptionValue("mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <mode>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Mode: " + mode);

    // <owner-id>
    try
    {
      ownerID = new GCshID( cmdLine.getOptionValue("owner-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <owner-id>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Owner ID: '" + ownerID + "'");

    // <income-expense-account>
    try
    {
      incExpAcctIDOrName = cmdLine.getOptionValue("income-expense-account");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <income-expense-account>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Income/expense account ID/name: '" + incExpAcctIDOrName + "'");

    // <receivable-payable-account>
    try
    {
      recvblPayblAcctIDOrName = cmdLine.getOptionValue("receivable-payable-account");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <receivable-payable-account>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Receivable/payable account ID/name: '" + recvblPayblAcctIDOrName + "'");

    // <number>
    try
    {
      number = cmdLine.getOptionValue("number");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <number>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Number: '" + number + "'");

    // <opened-date>
    try
    {
      dateOpen = LocalDateHelpers.parseLocalDate(cmdLine.getOptionValue("opened-date"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <opened-date>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Opened date: " + dateOpen);

    // <post-date>
    try
    {
      datePost = LocalDateHelpers.parseLocalDate(cmdLine.getOptionValue("post-date"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <date>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Post date:   " + datePost);

    // <due-date>
    try
    {
      dateDue = LocalDateHelpers.parseLocalDate(cmdLine.getOptionValue("due-date"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <due-date>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Due date:    " + dateDue);

    // <amount>
    try
    {
      BigMoney betrag = BigMoney.of(CurrencyUnit.EUR, Double.parseDouble(cmdLine.getOptionValue("amount")));
      amount = new FixedPointNumber(betrag.getAmount());
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <amount>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Amount: " + amount);
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GenInvc", options );
  }
}
