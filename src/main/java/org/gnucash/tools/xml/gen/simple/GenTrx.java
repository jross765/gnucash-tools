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
import org.gnucash.api.write.GnuCashWritableTransaction;
import org.gnucash.api.write.GnuCashWritableTransactionSplit;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.base.basetypes.simple.GCshAcctID;
import org.gnucash.tools.CommandLineTool;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.dateutils.DateHelpers;
import xyz.schnorxoborx.base.dateutils.LocalDateHelpers;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class GenTrx extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
private static final Logger LOGGER = LoggerFactory.getLogger(GenTrx.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String           gcshInFileName = null;
  private static String           gcshOutFileName = null;
  private static GCshAcctID       fromAcctID = null;
  private static GCshAcctID       toAcctID = null;
  private static FixedPointNumber amount = null;
  private static FixedPointNumber quantity = null;
  private static LocalDate        datePosted = null;
  private static String           description = null;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GenTrx tool = new GenTrx ();
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
    datePosted = LocalDateHelpers.parseLocalDate(DateHelpers.DATE_UNSET);

    // cfg = new PropertiesConfiguration(System.getProperty("config"));
    // getConfigSettings(cfg);

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
        
    Option optFromAcctID = Option.builder("facct")
      .required()
      .hasArg()
      .argName("UUID")
      .desc("Account-ID to be booked from")
      .longOpt("from-account-id")
      .build();
      
    Option optToAcctID = Option.builder("tacct")
      .required()
      .hasArg()
      .argName("UUID")
      .desc("Account-ID to be booked to")
      .longOpt("to-account-id")
      .build();
      
    Option optAmount = Option.builder("amt")
      .required()
      .hasArg()
      .argName("amount")
      .desc("Amount")
      .longOpt("amount")
      .build();
              
    Option optQuantity = Option.builder("qty")
      .required()
      .hasArg()
      .argName("quantity")
      .desc("Quantity")
      .longOpt("quantity")
      .build();
                
    Option optDatePosted = Option.builder("dtp")
      .required()
      .hasArg()
      .argName("datetime")
      .desc("Date posted")
      .longOpt("date-posted")
      .build();
            
    // The convenient ones
    Option optDescr = Option.builder("dscr")
      .hasArg()
      .argName("descr")
      .desc("Description")
      .longOpt("description")
      .build();
              
        
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optFromAcctID);
    options.addOption(optToAcctID);
    options.addOption(optAmount);
    options.addOption(optQuantity);
    options.addOption(optDatePosted);
    options.addOption(optDescr);
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
    
    System.err.println("Account name (from): '" + gcshFile.getAccountByID(fromAcctID).getQualifiedName() + "'");
    System.err.println("Account name (to):   '" + gcshFile.getAccountByID(toAcctID).getQualifiedName() + "'");
    
    GnuCashWritableTransaction trx = gcshFile.createWritableTransaction();
    trx.setDescription(description);

    GnuCashWritableTransactionSplit split1 = trx.createWritableSplit(gcshFile.getAccountByID(fromAcctID));
    split1.setValue(new FixedPointNumber(amount.copy().negate()));
    split1.setQuantity(new FixedPointNumber(quantity.copy().negate()));
    
    GnuCashWritableTransactionSplit split2 = trx.createWritableSplit(gcshFile.getAccountByID(toAcctID));
    split2.setValue(new FixedPointNumber(amount));
    split2.setQuantity(new FixedPointNumber(quantity));
    
    trx.setDatePosted(datePosted);
    trx.setDateEntered(LocalDateTime.now());
    
    System.out.println("Transaction to write: " + trx.toString());
    gcshFile.writeFile(new File(gcshOutFileName));
    System.out.println("OK");
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
    
    // <from-account-id>
    try
    {
      fromAcctID = new GCshAcctID( cmdLine.getOptionValue("from-account-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <from-account-id>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Account ID (from): '" + fromAcctID + "' ");
    
    // <to-account-id>
    try
    {
      toAcctID = new GCshAcctID( cmdLine.getOptionValue("to-account-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <to-account-id>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Account ID (to): '" + toAcctID + "'");
    
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

    // <quantity>
    try
    {
      quantity = new FixedPointNumber(Double.parseDouble(cmdLine.getOptionValue("quantity")));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <quantity>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Quantity: " + quantity);

    // <date-posted>
    try
    {
      datePosted = LocalDateHelpers.parseLocalDate(cmdLine.getOptionValue("date-posted"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <date-posted>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Date posted: " + datePosted);

    // <description>
    if ( cmdLine.hasOption("description") )
    {
      try
      {
        description = cmdLine.getOptionValue("description");
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <description>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      description = "Generated by GenTrx" + LocalDateTime.now();
    }
    System.err.println("description: '" + description + "'");
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GenTrx", options );
  }
}
