package org.gnucash.tools.xml.get.info;

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
import org.apache.commons.numbers.fraction.BigFraction;
import org.gnucash.api.read.GnuCashTransaction;
import org.gnucash.api.read.GnuCashTransactionSplit;
import org.gnucash.api.read.impl.GnuCashFileImpl;
import org.gnucash.api.read.impl.GnuCashTransactionImpl;
import org.gnucash.apispec.read.GnuCashStockDividendTransaction;
import org.gnucash.apispec.read.impl.GnuCashStockDividendTransactionImpl;
import org.gnucash.base.basetypes.simple.GCshTrxID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class TestGetStockDivTrxInfo extends CommandLineTool
{
	  // Logger
	  @SuppressWarnings("unused")
	  private static final Logger LOGGER = LoggerFactory.getLogger(GetTrxInfo.class);
	  
	  // -----------------------------------------------------------------

	  // private static PropertiesConfiguration cfg = null;
	  private static Options options;
	  
	  private static String    gcshFileName = null;
	  private static GCshTrxID trxID = null;
	  
	  // -----------------------------------------------------------------

	public static void main(String[] args) {
		try {
			TestGetStockDivTrxInfo tool = new TestGetStockDivTrxInfo();
			tool.execute(args);
		} catch (Exception exc) {
			System.err.println("Execution exception. Aborting.");
			exc.printStackTrace();
			System.exit(1);
		}
	}

  @Override
  protected void init() throws Exception
  {
//	    cfg = new PropertiesConfiguration(System.getProperty("config"));
//	    getConfigSettings(cfg);

    // Options
    // The essential ones
    Option optFile = Option.builder("f")
      .required()
      .hasArg()
      .argName("file")
      .desc("GnuCash file")
      .longOpt("gnucash-file")
      .get();
      
    Option optTrxID = Option.builder("trx")
      .required()
      .hasArg()
      .argName("UUID")
      .desc("Transaction-ID")
      .longOpt("transaction-id")
      .get();
    
    // The convenient ones
    Option optShowSplt = Option.builder("ssplt")
      .desc("Show splits")
      .longOpt("show-splits")
      .get();
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optTrxID);
    options.addOption(optShowSplt);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cfg) throws Exception
  {
    // ::EMPTY
  }
  
  protected void kernel() throws Exception {
	GnuCashFileImpl gcshFile = new GnuCashFileImpl(new File(gcshFileName));

	GnuCashTransaction genTrx = gcshFile.getTransactionByID(trxID);
	if ( genTrx == null ) {
		System.err.println("Error: No (generic) transaction with ID '" + trxID + "' in GnuCash file");
		System.exit(-1); 
	}

	GnuCashStockDividendTransaction specTrx = new GnuCashStockDividendTransactionImpl((GnuCashTransactionImpl) genTrx);
	if ( specTrx == null ) {
		System.err.println("Error: Transaction with ID '" + trxID + "' does not meet criteria for stock dividend transaction");
		System.exit(-1); 
	}
		
	// ---
	// Inherited from GnuCashTransaction:
	
	FixedPointNumber balance = specTrx.getBalance();
	BigFraction balanceRat = specTrx.getBalanceRat();
	
	int nofSplits = specTrx.getSplitsCount();
	GnuCashTransactionSplit splt1 = specTrx.getSplits().get(0);

	System.out.println("");
	System.out.println("No. of splits:   " + nofSplits);
	System.out.println("First split:     " + splt1);
	
	System.out.println("");
	System.out.println("Balance:         " + balance);
	System.out.println("Balance (exact): " + balanceRat);
	
	// ---
	// Inherited from GnuCashStockDividendTransaction:
	
	FixedPointNumber grossDiv = specTrx.getGrossDividend();
	FixedPointNumber feeTax = specTrx.getFeesTaxes();
	FixedPointNumber netDiv = specTrx.getNetDividend();

	BigFraction grossDivRat = specTrx.getGrossDividendRat();
	BigFraction feeTaxRat = specTrx.getFeesTaxesRat();
	BigFraction netDivRat = specTrx.getNetDividendRat();
	
	System.out.println("");
	System.out.println("Gross dividend:         " + grossDiv);
	System.out.println("Gross dividend (exact): " + grossDivRat);
	
	System.out.println("");
	System.out.println("Fees/taxes:             " + feeTax);
	System.out.println("Fees/taxes (exact):     " + feeTaxRat);
	
	System.out.println("");
	System.out.println("Net dividend:           " + netDiv);
	System.out.println("Net dividend (exact):   " + netDivRat);
	
	// ---
	
	System.out.println("------------------");
	System.out.println("");
	System.out.println("Stock acct. split:      " + specTrx.getStockAccountSplit());
	System.out.println("Income acct. split:     " + specTrx.getIncomeAccountSplit());
	for ( GnuCashTransactionSplit splt : specTrx.getExpensesSplits() ) {
		System.out.println("Expenses acct. split:   " + splt);
	}
	System.out.println("Offsetting acct. split: " + specTrx.getOffsettingAccountSplit());
	
	// ---
	
	System.out.println("------------------");
	System.out.println("");
	System.out.println("String rep. (1): " + specTrx.toString() );
	
	System.out.println("");
	System.out.println("String rep. (2): " + ((GnuCashStockDividendTransactionImpl) specTrx).toStringHuman() );
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
    
    System.err.println("GnuCash file: '" + gcshFileName + "'");
    
    // <transaction-id>
    try
    {
      trxID = new GCshTrxID( cmdLine.getOptionValue("transaction-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <transaction-id>");
      throw new InvalidCommandLineArgsException();
    }
    
    System.err.println("Transaction ID: " + trxID);
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "TestGetStockDivTrxInfo", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
