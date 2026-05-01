package org.gnucash.tools.xml.upd.other;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.numbers.fraction.BigFraction;
import org.gnucash.api.read.GnuCashAccount;
import org.gnucash.api.write.GnuCashWritableAccount;
import org.gnucash.api.write.GnuCashWritableFile;
import org.gnucash.api.write.GnuCashWritableTransaction;
import org.gnucash.api.write.GnuCashWritableTransactionSplit;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.base.basetypes.simple.GCshAcctID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.AccountNotFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.dateutils.LocalDateHelpers;

public class CloseBooks extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(CloseBooks.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String gcshInFileName = null;
  private static String gcshOutFileName = null;
  
  private static GCshAcctID acctIncID = null; // sic, not GCshComplAcctID
  private static GCshAcctID acctExpID = null; // sic, not GCshComplAcctID

  private static LocalDate closingDate = null; 

  private static GnuCashWritableAccount acctInc = null;
  private static GnuCashWritableAccount acctExp = null;
  
  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      CloseBooks tool = new CloseBooks ();
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
    Option optFileIn = Option.builder("if")
      .required()
      .hasArg()
      .argName("file")
      .desc("GnuCash file (in)")
      .longOpt("gnucash-in-file")
      .get();
          
    Option optFileOut = Option.builder("of")
      .required()
      .hasArg()
      .argName("file")
      .desc("GnuCash file (out)")
      .longOpt("gnucash-out-file")
      .get();
      
    Option optAcctIncID = Option.builder("acci")
      .required()
      .hasArg()
      .argName("acctid")
      .desc("Account-ID (equity account for closing income accounts)")
      .longOpt("account-income-id")
      .get();

    Option optAcctExpID = Option.builder("acce")
      .required()
      .hasArg()
      .argName("acctid")
      .desc("Account-ID (equity account for closing expenses accounts)")
      .longOpt("account-expenses-id")
      .get();
      
    Option optDate = Option.builder("dat")
      .required()
      .hasArg()
      .argName("date")
      .desc("Closing date")
      .longOpt("closing-date")
      .get();
    	      
    // The convenient ones
    // ::EMPTY
    	      
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optAcctIncID);
    options.addOption(optAcctExpID);
    options.addOption(optDate);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cfg) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    GnuCashWritableFileImpl gcshFile = new GnuCashWritableFileImpl(new File(gcshInFileName), true);

    // CAUTION: Here, we intentionally do not use AccountHelper.getWrtAcct().
    try 
    {
      acctInc = gcshFile.getWritableAccountByID(acctIncID);
      System.err.println("Account before update: " + acctInc.toString());
    }
    catch ( Exception exc )
    {
      System.err.println("Error: Could not find/instantiate account with ID '" + acctIncID + "'");
      throw new AccountNotFoundException();
    }
    
    if ( acctInc.getType() != GnuCashAccount.Type.EQUITY )
    {
        System.err.println("Error: Account " + acctIncID + " is not of type " + GnuCashAccount.Type.EQUITY + ": " + acctInc.getType() );
        throw new AccountNotFoundException();
    }
    
    // CAUTION: Here, we intentionally do not use AccountHelper.getWrtAcct().
    try 
    {
      acctExp = gcshFile.getWritableAccountByID(acctExpID);
      System.err.println("Account before update: " + acctExp.toString());
    }
    catch ( Exception exc )
    {
      System.err.println("Error: Could not find/instantiate account with ID '" + acctExpID + "'");
      throw new AccountNotFoundException();
    }
    
    if ( acctExp.getType() != GnuCashAccount.Type.EQUITY )
    {
        System.err.println("Error: Account " + acctExpID + " is not of type " + GnuCashAccount.Type.EQUITY + ": " + acctExp.getType() );
        throw new AccountNotFoundException();
    }
    
    closeBooks(gcshFile);
    
    gcshFile.writeFile(new File(gcshOutFileName));
    
    System.out.println("OK");
  }

  // ::TODO: Check whether all income/equity accounts have the same
  // currency. If not, this code will not work properly.
  private void closeBooks(GnuCashWritableFile gcshFile) throws Exception
  {
	  for ( GnuCashAccount topAcct : gcshFile.getTopAccounts() )
	  {
		  if ( topAcct.getType() == GnuCashAccount.Type.INCOME )
		  {
			  closeIncomeAccounts( gcshFile, topAcct );
		  }
		  else if ( topAcct.getType() == GnuCashAccount.Type.EXPENSE )
		  {
			  closeExpensesAccounts( gcshFile, topAcct );
		  }
	  }
  }

  private void closeIncomeAccounts(GnuCashWritableFile gcshFile, GnuCashAccount topAcct)
  {
	  System.out.println("Income accounts: ");
	  GnuCashWritableTransaction closingTrx = gcshFile.createWritableTransaction();
	  closingTrx.setDatePosted( closingDate );
	  closingTrx.setDateEntered( LocalDateTime.now() );
	  closingTrx.setDescription("Buchabschluss (Inc/Trx)");
	  
	  BigFraction sum = BigFraction.ZERO; 
	  int nofSplits = 0;
	  for ( GnuCashAccount subAcct : topAcct.getChildrenRecursive() )
	  {
		  System.out.println(" - " + subAcct.getQualifiedName());
		  if ( subAcct.getType() == GnuCashAccount.Type.INCOME ) // just in case...
		  {
			  if ( subAcct.getBalanceRat(closingDate).compareTo(BigFraction.ZERO) != 0 )
			  {
				  // Generate new split for closing transaction 
				  System.out.println("   Balance is not zero");
				  System.out.println("   Generating closing split");
				  BigFraction blc = subAcct.getBalanceRat(closingDate);
				  GnuCashWritableTransactionSplit newSplt = closingTrx.createWritableSplit(subAcct);
				  newSplt.setValue( blc.negate() );
				  newSplt.setQuantity( blc.negate() );
				  newSplt.setDescription("Buchabschluss (Inc/Splt)");
				  sum = sum.add( blc.negate() );
				  nofSplits++;
			  }
			  else
			  {
				  System.out.println("   Balance is zero");
				  System.out.println("   Omitting");
			  }
		  }
		  else
		  {
			  System.out.println("   Error: This is not an income account");
			  System.out.println("   Omitting");
		  }
	  }
	  
	  // Split on equity account
	  System.out.println("No. of closing splits generated: " + nofSplits);
	  if ( nofSplits > 0 )
	  {
		  System.out.println("Generating last split");
		  GnuCashWritableTransactionSplit lastSplt = closingTrx.createWritableSplit(acctInc);
		  lastSplt.setValue( sum.negate() );
		  lastSplt.setQuantity( sum.negate() );
	  }
	  else
	  {
		  System.out.println("No last split to generate");
	  }
	  System.out.println("New Transaction: " + closingTrx);
  }

  private void closeExpensesAccounts(GnuCashWritableFile gcshFile, GnuCashAccount topAcct)
  {
	  System.out.println("Expenses accounts: ");
	  GnuCashWritableTransaction closingTrx = gcshFile.createWritableTransaction();
	  closingTrx.setDatePosted( closingDate );
	  closingTrx.setDateEntered( LocalDateTime.now() );
	  closingTrx.setDescription("Buchabschluss (Exp/Trx)");
	  
	  BigFraction sum = BigFraction.ZERO;
	  int nofSplits = 0;
	  for ( GnuCashAccount subAcct : topAcct.getChildrenRecursive() )
	  {
		  System.out.println(" - " + subAcct.getQualifiedName());
		  if ( subAcct.getType() == GnuCashAccount.Type.EXPENSE ) // just in case...
		  {
			  if ( subAcct.getBalanceRat(closingDate).compareTo(BigFraction.ZERO) != 0 )
			  {
				  // Generate new split for closing transaction 
				  System.out.println("   Balance is not zero");
				  System.out.println("   Generating closing split");
				  BigFraction blc = subAcct.getBalanceRat(closingDate);
				  GnuCashWritableTransactionSplit newSplt = closingTrx.createWritableSplit(subAcct);
				  newSplt.setValue( blc.negate() );
				  newSplt.setQuantity( blc.negate() );
				  newSplt.setDescription("Buchabschluss (Exp/Splt)");
				  sum = sum.add( blc.negate() );
				  nofSplits++;
			  }
			  else
			  {
				  System.out.println("   Balance is zero");
				  System.out.println("   Omitting");
			  }
		  }
		  else
		  {
			  System.out.println("   Error: This is not an expenses account");
			  System.out.println("   Omitting");
		  }
	  }
	  
	  // Split on equity account
	  System.out.println("No. of closing splits generated: " + nofSplits);
	  if ( nofSplits > 0 )
	  {
		  System.out.println("Generating last split");
		  GnuCashWritableTransactionSplit lastSplt = closingTrx.createWritableSplit(acctExp);
		  lastSplt.setValue( sum.negate() );
		  lastSplt.setQuantity( sum.negate() );
	  }
	  else
	  {
		  System.out.println("No last split to generate");
	  }
	  System.out.println("New Transaction: " + closingTrx);
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
    
    // ---

    // CAUTION: Here, we CmdLineHelper_Acct.parseAcctStuffWrap(),
    // <account-income-id>
    try
    {
      acctIncID = new GCshAcctID( cmdLine.getOptionValue("account-income-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <account-income-id>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Account ID (income):   " + acctIncID);

    // <account-expenses-id>
    try
    {
      acctExpID = new GCshAcctID( cmdLine.getOptionValue("account-expenses-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <account-expenses-id>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Account ID (expenses): " + acctExpID);

    // <closing-date>
    try
    {
      closingDate = LocalDateHelpers.parseLocalDate( cmdLine.getOptionValue( "closing-date" ) );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <closing-date>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Stichtag:              " + closingDate);
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "CloseBooks", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
