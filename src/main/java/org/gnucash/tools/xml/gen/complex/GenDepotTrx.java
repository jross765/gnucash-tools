package org.gnucash.tools.xml.gen.complex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashAccount;
import org.gnucash.api.read.GnuCashTransactionSplit;
import org.gnucash.api.write.GnuCashWritableTransaction;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.apiext.secacct.SecuritiesAccountTransactionManager;
import org.gnucash.base.basetypes.simple.GCshAcctID;
import org.gnucash.base.basetypes.simple.GCshID;
import org.gnucash.base.basetypes.simple.GCshTrxID;
import org.gnucash.base.tuples.AcctIDAmountPair;
import org.gnucash.tools.CommandLineTool;
import org.gnucash.tools.xml.helper.CmdLineHelper;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.dateutils.DateHelpers;
import xyz.schnorxoborx.base.dateutils.LocalDateHelpers;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class GenDepotTrx extends CommandLineTool
{
  enum BookMode {
	  SINGLE_TRX,
	  LISTFILE
  }

  // -----------------------------------------------------------------

  // Logger
  private static final Logger LOGGER = LoggerFactory.getLogger(GenDepotTrx.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  // ------------------------------

  private static BookMode         mode = null; 
  
  private static String           gcshInFileName = null;
  private static String           gcshOutFileName = null;
  private static GnuCashWritableFileImpl gcshFile = null;
		  
  private static String           bookingListFileName = null;
  
  // ------------------------------
  // BEGIN Core parameters

  private static SecuritiesAccountTransactionManager.Type type = null;
  
  private static GCshAcctID        stockAcctID = null;
  private static GCshAcctID        incomeAcctID = null;
  private static Collection<AcctIDAmountPair> expensesAcctAmtList = null;
  private static GCshAcctID        offsetAcctID = null;
  
  private static FixedPointNumber  nofStocks = null;
  private static FixedPointNumber  stockPrc = null;
  private static FixedPointNumber  divDistrGross = null;
  private static FixedPointNumber  stockSplitFactor = null;
  
  private static Helper.DateFormat dateFormat = null;
  private static LocalDate         datPst = null;
  private static String            descr = null;

  // END Core parameters
  // ------------------------------

  // batch-mode:
  private static boolean    silent           = false;
  private static boolean    batch            = false;
  private static String     batchOutFileName = null;
  
  // -----------------------------------------------------------------

  // ::MAGIC
  private final static String COMMENT_TOKEN = "#";
  private final static String SEPARATOR     = "§"; // sic, not colon, not comma, not pipe  

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GenDepotTrx tool = new GenDepotTrx ();
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
    datPst = LocalDateHelpers.parseLocalDate(DateHelpers.DATE_UNSET);

    // cfg = new PropertiesConfiguration(System.getProperty("config"));
    // getConfigSettings(cfg);

    // Options
    // The essential ones
    Option optMode = Option.builder("m")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Booking mode")
      .longOpt("book-mode")
      .build();
    	        
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
    
    Option optListFile = Option.builder("blf")
      .hasArg()
      .argName("file")
      .desc("List file for bookings")
      .longOpt("booking-list-file")
      .build();
    	    	        
    // ----------------------------
    // BEGIN Core parameters
        
    Option optType = Option.builder("tp")
      .hasArg()
      .argName("type")
      .desc("Transaction type")
      .longOpt("type")
      .build();
    	      
    Option optStockAcct = Option.builder("stacct")
      .hasArg()
      .argName("acctid")
      .desc("Account-ID of stock account")
      .longOpt("stock-account-id")
      .build();
      
    Option optIncomeAcct = Option.builder("inacct")
      .hasArg()
      .argName("acctid")
      .desc("Account-ID for (divid./distrib.) income")
      .longOpt("income-account-id")
      .build();
    	      
    Option optExpensesAcctAmtList = Option.builder("eaa")
      .hasArg()
      .argName("pair-list")
      .desc("Account-ID/amount pairs for expenses (taxes and fees), " + 
            "list separated by '|', pairs separated by ';'")
      .longOpt("expense-account-amounts")
      .build();
      
    Option optOffsetAcct = Option.builder("osacct")
      .hasArg()
      .argName("acctid")
      .desc("Account-ID of offsetting account")
      .longOpt("offset-account-id")
      .build();
    	      
    // ---
    
    Option optNofStocks = Option.builder("n")
      .hasArg()
      .argName("number")
      .desc("Number of stocks to buy/sell")
      .longOpt("nof-stocks")
      .build();
    	                
    Option optStockPrice = Option.builder("p")
      .hasArg()
      .argName("amount")
      .desc("Stock price")
      .longOpt("stock-price")
      .build();
              
    Option optDivDistr = Option.builder("divdistgr")
      .hasArg()
      .argName("amount")
      .desc("Gross divid./distrib.")
      .longOpt("divid-distrib-gross")
      .build();

    Option optSpltFact = Option.builder("fct")
      .hasArg()
      .argName("amount")
      .desc("Stock split factor")
      .longOpt("stock-split-factor")
      .build();

    // ---
    
    Option optDatePosted = Option.builder("dtp")
      .hasArg()
      .argName("datetime")
      .desc("Date posted")
      .longOpt("date-posted")
      .build();
            
    // The convenient ones
    Option optDateFormat = Option.builder("df")
      .hasArg()
      .argName("date-format")
      .desc("Date format")
      .longOpt("date-format")
      .build();

    Option optDescr = Option.builder("dscr")
      .hasArg()
      .argName("descr")
      .desc("Description")
      .longOpt("description")
      .build();
              
    // END Core parameters
    // ----------------------------

    Option optSilent = Option.builder("sl")
      .desc("Silent mode")
      .longOpt("silent")
      .build();

    Option optBatch = Option.builder("ba")
   	  .desc("Batch mode")
   	  .longOpt("batch")
   	  .build();

    Option optOutFile = Option.builder("bof")
      .hasArg()
      .argName("name")
      .desc("Out-file name (for batch mode)")
      .longOpt("batch-out-file")
      .build();
    
    // ---
    	    
    options = new Options();
    options.addOption(optMode);
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optListFile);
    
    options.addOption(optType);
    options.addOption(optStockAcct);
    options.addOption(optIncomeAcct);
    options.addOption(optExpensesAcctAmtList);
    options.addOption(optOffsetAcct);
    options.addOption(optNofStocks);
    options.addOption(optStockPrice);
    options.addOption(optDivDistr);
    options.addOption(optSpltFact);
    options.addOption(optDatePosted);
    options.addOption(optDateFormat);
    options.addOption(optDescr);
    
    options.addOption(optSilent);
    options.addOption(optBatch);
    options.addOption(optOutFile);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cs) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    gcshFile = new GnuCashWritableFileImpl(new File(gcshInFileName));
    
    if ( mode == BookMode.SINGLE_TRX )
    {
    	bookSingleTrx();
    }
    else if ( mode == BookMode.LISTFILE )
    {
    	bookListFile();
    }
  }

  private void bookSingleTrx() throws IOException
  {
	  BufferedWriter outFile = null;
	  if ( batch )
	  {
		  try 
		  {
			  outFile = new BufferedWriter(new FileWriter(batchOutFileName));
		  } 
		  catch ( Exception exc )
		  {
			  System.err.println("Could not open batch-out-file '" + batchOutFileName + "'");
			  System.err.println("Aborting");
			  System.exit( 1 );
		  }
	  }

	  // ---

	  bookSingleTrxPrep();
	  GCshTrxID newID = bookSingleTrxCore(outFile);
	  
	  // ---

	  gcshFile.writeFile(new File(gcshOutFileName));
		    
	  if ( ! silent )
		  System.out.println("OK");
		    
	  if ( batch )
		  outFile.close();
  }
  
  private void bookListFile() throws IOException, InvalidCommandLineArgsException
  {
	  ArrayList<ParamTuple> paramTupleList = new ArrayList<ParamTuple>();
	  
	  try
	  {
		  readListFile(paramTupleList);
//		  System.out.println("Params: ");
//		  for ( ParamTuple elt : paramTupleList )
//			  System.out.println(" - " + elt.toString());
	  }
	  catch ( Exception exc )
	  {
		  System.err.println("Could not parse list file ");
		  LOGGER.error("Could not parse list file ");
		  return;
	  }
	  
	  BufferedWriter outFile = null;
	  if ( batch )
	  {
		  try 
		  {
			  outFile = new BufferedWriter(new FileWriter(batchOutFileName));
		  } 
		  catch ( Exception exc )
		  {
			  System.err.println("Could not open batch-out-file '" + batchOutFileName + "'");
			  LOGGER.error("Could not open batch-out-file '" + batchOutFileName + "'");
			  return;
		  }
	  }

	  // ---

	  bookListFileCore( paramTupleList, outFile );
	  
	  // ---

	  gcshFile.writeFile(new File(gcshOutFileName));
		    
	  if ( ! silent )
		  System.out.println("OK");

	  if ( batch )
		  outFile.close();
  }

  private void bookSingleTrxPrep() throws IOException
  {
	GnuCashAccount stockAcct = gcshFile.getAccountByID(stockAcctID);
	if ( stockAcct == null )
	{
		System.err.println("Error: Cannot get account with ID '" + stockAcctID + "'");
		LOGGER.debug("Error: Cannot get account with ID '" + stockAcctID + "'");
	}
	
	GnuCashAccount incomeAcct = null;
	if ( incomeAcctID != null )
	{
		incomeAcct = gcshFile.getAccountByID(incomeAcctID);
		if ( incomeAcct == null )
		{
			System.err.println("Error: Cannot get account with ID '" + incomeAcctID + "'");
			LOGGER.debug("Error: Cannot get account with ID '" + incomeAcctID + "'");
		}
	}
	
	for ( AcctIDAmountPair elt : expensesAcctAmtList )
	{
		GnuCashAccount expensesAcct = gcshFile.getAccountByID(elt.accountID());
		if ( expensesAcct == null )
		{
			System.err.println("Error: Cannot get account with ID '" + elt.accountID() + "'");
			LOGGER.debug("Error: Cannot get account with ID '" + elt.accountID() + "'");
		}
	}
	
	GnuCashAccount offsetAcct = null;
	if ( type == SecuritiesAccountTransactionManager.Type.BUY_STOCK || 
		 type == SecuritiesAccountTransactionManager.Type.DIVIDEND ||
		 type == SecuritiesAccountTransactionManager.Type.DISTRIBUTION )
	{
		offsetAcct = gcshFile.getAccountByID(offsetAcctID);
		if ( offsetAcct == null )
		{
			System.err.println("Error: Cannot get account with ID '" + offsetAcctID + "'");
			LOGGER.debug("Error: Cannot get account with ID '" + offsetAcctID + "'");
		}
	}

	System.err.println("Account 1 name (stock):      '" + stockAcct.getQualifiedName() + "'");
	if ( incomeAcctID != null )
	{
		System.err.println("Account 2 name (income):     '" + incomeAcct.getQualifiedName() + "'");
		LOGGER.debug("Account 2 name (income):     '" + incomeAcct.getQualifiedName() + "'");
	}

	int counter = 1;
	for ( AcctIDAmountPair elt : expensesAcctAmtList )
	{
		GnuCashAccount expensesAcct = gcshFile.getAccountByID(elt.accountID());
		System.err.println("Account 3." + counter + " name (expenses): '" + expensesAcct.getQualifiedName() + "'");
		counter++;
	}
	
	if ( type == SecuritiesAccountTransactionManager.Type.BUY_STOCK || 
		 type == SecuritiesAccountTransactionManager.Type.DIVIDEND ||
		 type == SecuritiesAccountTransactionManager.Type.DISTRIBUTION )
	{
		System.err.println("Account 4 name (offsetting): '" + offsetAcct.getQualifiedName() + "'");
		LOGGER.debug("Account 4 name (offsetting): '" + offsetAcct.getQualifiedName() + "'");
	}
  }
  
  private GCshTrxID bookSingleTrxCore(BufferedWriter outFile) throws IOException
  {
    GnuCashWritableTransaction trx = null;
	if ( type == SecuritiesAccountTransactionManager.Type.BUY_STOCK ) 
	{
	    trx = SecuritiesAccountTransactionManager.
	    		genBuyStockTrx(gcshFile, 
	    					   stockAcctID, expensesAcctAmtList, offsetAcctID,
	    					   nofStocks, stockPrc,
	    					   datPst, descr);
	} 
	else if ( type == SecuritiesAccountTransactionManager.Type.DIVIDEND ) 
	{
	    trx = SecuritiesAccountTransactionManager.
	    		genDividDistribTrx(gcshFile, 
	    					   stockAcctID, incomeAcctID, expensesAcctAmtList, offsetAcctID,
	    					   GnuCashTransactionSplit.Action.DIVIDEND, divDistrGross,
	    					   datPst, descr);
	}
	else if ( type == SecuritiesAccountTransactionManager.Type.DISTRIBUTION ) 
	{
	    trx = SecuritiesAccountTransactionManager.
	    		genDividDistribTrx(gcshFile, 
	    					   stockAcctID, incomeAcctID, expensesAcctAmtList, offsetAcctID,
	    					   GnuCashTransactionSplit.Action.DIST, divDistrGross,
	    					   datPst, descr);
	}
	else if ( type == SecuritiesAccountTransactionManager.Type.STOCK_SPLIT ) 
	{
	    trx = SecuritiesAccountTransactionManager.
	    		genStockSplitTrx_factor(gcshFile, 
	    					     		stockAcctID,
	    					     		stockSplitFactor,
	    					     		datPst, descr);
	}
    
	// ---
	
    if ( ! silent )
    	System.out.println("Transaction to write: " + trx.toString());

    GCshTrxID newID = trx.getID();
    LOGGER.info( "Generated new Transaction: " + newID);

    if ( batch )
    {
    	try 
    	{
    		outFile.write("" + newID + "\n");
    	} 
    	catch ( Exception exc )
    	{
    		System.err.println("Could not write Transaction ID into batch-out-file");
    		LOGGER.error("Could not write Transaction ID into batch-out-file");
    	}
    }
	  
	// ---
	
    return newID;
  }
  
  private void readListFile(ArrayList<ParamTuple> paramTuples) throws IOException
  {
      BufferedReader br = new BufferedReader(new FileReader(bookingListFileName));

      String zeile;
      while ( (zeile = br.readLine()) != null ) 
      {
        if ( zeile.startsWith(COMMENT_TOKEN) )
          continue;
        
        String[] eintrag = zeile.split(SEPARATOR);
        ParamTuple newTuple = new ParamTuple(eintrag[0], eintrag[1], eintrag[2],
        									 eintrag[3], eintrag[4], eintrag[5],
        									 eintrag[6], eintrag[7], eintrag[8],
        									 eintrag[9], eintrag[10], eintrag[11]);
        
        paramTuples.add(newTuple);
      }
  }

  private void bookListFileCore(ArrayList<ParamTuple> paramTupleList, BufferedWriter outFile)
  {
	  GCshID newID = null;
	  int lineNo = 1;
	  for ( ParamTuple tuple : paramTupleList )
	  {
		  System.out.println( "------------------------------------------------" );
		  System.out.println( "Line no. " + lineNo + ": " );
		  LOGGER.info( "Line no. " + lineNo + ": " + tuple.toString() );
		  
		  try
		  {
			  parseCoreParams( tuple );
		  }
		  catch ( InvalidCommandLineArgsException exc )
		  {
			  System.err.println("Could not validate set of params, line no. " + lineNo);
			  LOGGER.error("Could not validate set of params, line no. " + lineNo);
			  lineNo++;
			  continue;
		  }
		  
		  try
		  {
			  bookSingleTrxPrep();
			  newID = bookSingleTrxCore(outFile);
		  }
		  catch ( Exception exc )
		  {
			  System.err.println("Could not book set of params, line no. " + lineNo);
			  LOGGER.error("Could not book set of params, line no. " + lineNo);
			  lineNo++;
			  continue;
		  }
		  
		  lineNo++;
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

    // <silent>
    if (cmdLine.hasOption("silent"))
    {
      silent = true;
    }
    else
    {
      silent = false;
    }
    if (! silent)
      System.err.println("silent:              " + silent);
    
    // ---

    // <book-mode>
    try
    {
      mode = BookMode.valueOf( cmdLine.getOptionValue("book-mode") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <book-mode>");
      throw new InvalidCommandLineArgsException();
    }
    if (! silent)
    	System.err.println("Book mode:           " + mode);
    
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
    if (! silent)
    	System.err.println("GnuCash file (in):  '" + gcshInFileName + "'");
    
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
    if (! silent)
    	System.err.println("GnuCash file (out): '" + gcshOutFileName + "'");
    
    // <booking-list-file>
    if ( cmdLine.hasOption( "booking-list-file" ) )
    {
    	if ( mode != BookMode.LISTFILE )
    	{
            System.err.println("<booking-list-file> may only be set with <mode> = '" + BookMode.LISTFILE + "'");
            throw new InvalidCommandLineArgsException();
    	}
    	
        try
        {
        	bookingListFileName = cmdLine.getOptionValue("booking-list-file");
        }
        catch ( Exception exc )
        {
          System.err.println("Could not parse <booking-list-file>");
          throw new InvalidCommandLineArgsException();
        }
    }
    else
    {
    	if ( mode == BookMode.LISTFILE )
    	{
            System.err.println("<booking-list-file> must be set with <mode> = '" + BookMode.LISTFILE + "'");
            throw new InvalidCommandLineArgsException();
    	}
    }
    if (! silent)
    	System.err.println("Booking list file:   '" + bookingListFileName + "'");
    
    // ----------------------------
    // BEGIN Core parameters
    
    if ( mode == BookMode.SINGLE_TRX )
    {
        ParamTuple tuple = new ParamTuple();
        
        if ( cmdLine.hasOption( "type" ) )
        	tuple.type = cmdLine.getOptionValue( "type" );

        if ( cmdLine.hasOption( "stock-account-id" ) )
        	tuple.stockAcctID = cmdLine.getOptionValue( "stock-account-id" );
        
        if ( cmdLine.hasOption( "income-account-id" ) )
        	tuple.incomeAcctID = cmdLine.getOptionValue( "income-account-id" );
        
        if ( cmdLine.hasOption( "expense-account-amounts" ) )
        	tuple.expensesAcctAmtList = cmdLine.getOptionValue( "expense-account-amounts" );
        
        if ( cmdLine.hasOption( "offset-account-id" ) )
        	tuple.offsetAcctID = cmdLine.getOptionValue( "offset-account-id" );
        
        if ( cmdLine.hasOption( "nof-stocks" ) )
        	tuple.nofStocks = cmdLine.getOptionValue( "nof-stocks" );
        
        if ( cmdLine.hasOption( "stock-price" ) )
        	tuple.stockPrc = cmdLine.getOptionValue( "stock-price" );
        
        if ( cmdLine.hasOption( "divid-distrib-gross" ) )
        	tuple.divDistrGross = cmdLine.getOptionValue( "divid-distrib-gross" );
        
        if ( cmdLine.hasOption( "stock-split-factor" ) )
        	tuple.stockSplitFactor = cmdLine.getOptionValue( "stock-split-factor" );
        
        if ( cmdLine.hasOption( "date-format" ) )
        	tuple.dateFormat = cmdLine.getOptionValue( "date-format" );

        if ( cmdLine.hasOption( "date-posted" ) )
        	tuple.datPst = cmdLine.getOptionValue( "date-posted" );
        
        if ( cmdLine.hasOption( "description" ) )
        	tuple.descr = cmdLine.getOptionValue( "description" );
        
        parseCoreParams( tuple );        
    }
    
    // END Core parameters
    // ----------------------------
    
    // <batch>
    if (cmdLine.hasOption("batch"))
    {
    	batch = true;
    }
    else
    {
    	batch = false;
    }
    if (! silent)
    	System.err.println("Batch-mode:          " + batch);

    // <batch-out-file>
    if ( cmdLine.hasOption("batch-out-file") )
    {
    	if ( ! batch )
    	{
            System.err.println("Error: <batch-out-file> must only be set with <batch> option set");
            throw new InvalidCommandLineArgsException();
    	}
    	
    	batchOutFileName = cmdLine.getOptionValue("batch-out-file");
    }
    else
    {
    	if ( batch )
    	{
            System.err.println("Error: <batch-out-file> must be set with <batch> option set");
            throw new InvalidCommandLineArgsException();
    	}
    }
    if (! silent)
    	System.err.println("Batch-out-file:      '" + batchOutFileName + "'");
  }

  private void parseCoreParams(ParamTuple tuple) throws InvalidCommandLineArgsException
  {
	// <type>
	if ( tuple.type != null )
	{
		if ( mode == BookMode.LISTFILE &&
	       	 tuple.type.trim().equals( "" ) )
		{
    		// Technically set, but logically unset
			System.err.println("<type> is empty");
			throw new InvalidCommandLineArgsException();
		}
		else
		{
		    try
		    {
		      type = SecuritiesAccountTransactionManager.Type.valueOf( tuple.type );
		    }
		    catch ( Exception exc )
		    {
		      System.err.println("Could not parse <type>");
		      throw new InvalidCommandLineArgsException();
		    }
		}		
	}
	else
	{
		System.err.println("<type> is not set");
		throw new InvalidCommandLineArgsException();
	}
    if (! silent)
    	System.err.println("Type: " + type);
    
    // --
    
    // <stock-account-id>
    if ( tuple.stockAcctID != null )
    {
    	if ( mode == BookMode.LISTFILE &&
   	       	 tuple.stockAcctID.trim().equals( "" ) )
    	{
    		// Technically set, but logically unset
        	System.err.println("<stock-account-id> is empty");
        	throw new InvalidCommandLineArgsException();
    	}
    	else
    	{
            try
            {
              stockAcctID = new GCshAcctID( tuple.stockAcctID );
            }
            catch ( Exception exc )
            {
              System.err.println("Could not parse <stock-account-id>");
              throw new InvalidCommandLineArgsException();
            }
    	}
    }
    else
    {
    	System.err.println("<stock-account-id> is not set");
    	throw new InvalidCommandLineArgsException();
    }
    if (! silent)
    	System.err.println("Stock account ID: " + stockAcctID);
    
    // <income-account-id>
    if ( tuple.incomeAcctID != null ) 
    {
    	if ( mode == BookMode.LISTFILE &&
          	 tuple.incomeAcctID.trim().equals( "" ) )
    	{
    		// Technically set, but logically unset
        	if ( type == SecuritiesAccountTransactionManager.Type.DIVIDEND ||
        		 type == SecuritiesAccountTransactionManager.Type.DISTRIBUTION )
        	{
        		System.err.println("Error: <income-account-id> must be set with <type> = '" + type + "'");
        		throw new InvalidCommandLineArgsException();
        	}
    	}
    	else
    	{
        	if ( type != SecuritiesAccountTransactionManager.Type.DIVIDEND &&
        		 type != SecuritiesAccountTransactionManager.Type.DISTRIBUTION )
        	{
        		System.err.println("Error: <income-account-id> may only be set with <type> = '" + type + "'");
        		throw new InvalidCommandLineArgsException();
        	}
        	
            try
            {
                incomeAcctID = new GCshAcctID( tuple.incomeAcctID );
            }
            catch ( Exception exc )
            {
              System.err.println("Could not parse <income-account-id>");
              throw new InvalidCommandLineArgsException();
            }
    	}
    } 
    else 
    {
    	if ( type == SecuritiesAccountTransactionManager.Type.DIVIDEND ||
    		 type == SecuritiesAccountTransactionManager.Type.DISTRIBUTION )
    	{
    		System.err.println("Error: <income-account-id> must be set with <type> = '" + type + "'");
    		throw new InvalidCommandLineArgsException();
    	}
    }
    if (! silent)
    	System.err.println("Income account ID: " + incomeAcctID);

    // <expense-account-amounts>
    // CAUTION: Logically, <expense-account-amounts> must *not necessarily* be set for buy-stock 
    // or dividend transactions (although in most cases, it is). However, technically, it does 
    // have to be set in these cases: to "DUMMY" (to be used both in interactive and in script mode
    // in order to make option handling more easy and clear).
    if ( tuple.expensesAcctAmtList != null )
    {
    	if ( mode == BookMode.LISTFILE &&
          	 tuple.expensesAcctAmtList.trim().equals( "" ) )
    	{
    		// Technically set, but logically unset
        	if ( type == SecuritiesAccountTransactionManager.Type.BUY_STOCK ||
           		 type == SecuritiesAccountTransactionManager.Type.DIVIDEND ||
           		 type == SecuritiesAccountTransactionManager.Type.DISTRIBUTION )
           	{
           		System.err.println("Error: <expense-account-amounts> must be set with <type> = '" + 
           						   SecuritiesAccountTransactionManager.Type.BUY_STOCK + "' or '" +
           						   SecuritiesAccountTransactionManager.Type.DIVIDEND + "' or '" +
           						   SecuritiesAccountTransactionManager.Type.DISTRIBUTION + "'");
           		System.err.println("If logically unset, set to '" + CmdLineHelper.ACCT_AMT_DUMMY_ARG + "'");
           		throw new InvalidCommandLineArgsException();
           	}

           	expensesAcctAmtList = new ArrayList<AcctIDAmountPair>();
    	}
    	else
    	{
        	if ( type != SecuritiesAccountTransactionManager.Type.BUY_STOCK &&
               	 type != SecuritiesAccountTransactionManager.Type.DIVIDEND &&
               	 type != SecuritiesAccountTransactionManager.Type.DISTRIBUTION )
           	{
           		System.err.println("Error: <expense-account-amounts> may only be set with <type> = '" + 
           						   SecuritiesAccountTransactionManager.Type.BUY_STOCK + "' or '" +
           						   SecuritiesAccountTransactionManager.Type.DIVIDEND + "' or '" +
           						   SecuritiesAccountTransactionManager.Type.DISTRIBUTION + "'");
           		throw new InvalidCommandLineArgsException();
           	}

           	expensesAcctAmtList = CmdLineHelper.getExpAcctAmtMulti(tuple.expensesAcctAmtList, "expense-account-amounts");
    	}    	
    }
    else
    {
    	if ( type == SecuritiesAccountTransactionManager.Type.BUY_STOCK ||
    		 type == SecuritiesAccountTransactionManager.Type.DIVIDEND ||
    		 type == SecuritiesAccountTransactionManager.Type.DISTRIBUTION )
    	{
    		System.err.println("Error: <expense-account-amounts> must be set with <type> = '" + 
    						   SecuritiesAccountTransactionManager.Type.BUY_STOCK + "' or '" +
    						   SecuritiesAccountTransactionManager.Type.DIVIDEND + "' or '" +
    						   SecuritiesAccountTransactionManager.Type.DISTRIBUTION + "'");
    		System.err.println("If logically unset, set to '" + CmdLineHelper.ACCT_AMT_DUMMY_ARG + "'");
    		throw new InvalidCommandLineArgsException();
    	}

    	expensesAcctAmtList = new ArrayList<AcctIDAmountPair>();
    }
    if (! silent)
    {
        System.err.print("Expenses account/amount pairs:");
       	if ( expensesAcctAmtList.size() == 0 )
       	{
       		System.err.println(" (none)");
       	}
       	else
       	{
       		System.err.println("");
       		for ( AcctIDAmountPair elt : expensesAcctAmtList )
       			System.err.println(" - " + elt);
       	}
    }
    
    // <offset-account-id>
    if ( tuple.offsetAcctID != null )
    {
    	if ( mode == BookMode.LISTFILE &&
          	 tuple.offsetAcctID.trim().equals( "" ) )
    	{
    		// Technically set, but logically unset
        	if ( type == SecuritiesAccountTransactionManager.Type.BUY_STOCK ||
        		 type == SecuritiesAccountTransactionManager.Type.DIVIDEND ||
        		 type == SecuritiesAccountTransactionManager.Type.DISTRIBUTION )
           	{
           		System.err.println("Error: <offset-account-id> must be set with <type> = '" + 
           						   SecuritiesAccountTransactionManager.Type.BUY_STOCK + "' or '" + 
           						   SecuritiesAccountTransactionManager.Type.DIVIDEND + "' or '" +
           						   SecuritiesAccountTransactionManager.Type.DISTRIBUTION + "'");
           		throw new InvalidCommandLineArgsException();
           	}
    	}
    	else
    	{
        	if ( type != SecuritiesAccountTransactionManager.Type.BUY_STOCK &&
           		 type != SecuritiesAccountTransactionManager.Type.DIVIDEND &&
           		 type != SecuritiesAccountTransactionManager.Type.DISTRIBUTION )
           	{
           		System.err.println("Error: <offset-account-id> may only be set with <type> = '" + 
           						   SecuritiesAccountTransactionManager.Type.BUY_STOCK + "' or '" +
           						   SecuritiesAccountTransactionManager.Type.DIVIDEND + "' or '" +
           						   SecuritiesAccountTransactionManager.Type.DISTRIBUTION + "'");
           		throw new InvalidCommandLineArgsException();
           	}
                 	
        	try
        	{
        		offsetAcctID = new GCshAcctID( tuple.offsetAcctID );
        	}
        	catch ( Exception exc )
        	{
        		System.err.println("Could not parse <offset-account-id>");
        		throw new InvalidCommandLineArgsException();
        	}
    	}
    }
    else
    {
    	if ( type == SecuritiesAccountTransactionManager.Type.BUY_STOCK ||
       		 type == SecuritiesAccountTransactionManager.Type.DIVIDEND ||
       		 type == SecuritiesAccountTransactionManager.Type.DISTRIBUTION )
    	{
    		System.err.println("Error: <offset-account-id> must be set with <type> = '" + 
    						   SecuritiesAccountTransactionManager.Type.BUY_STOCK + "' or '" + 
    						   SecuritiesAccountTransactionManager.Type.DIVIDEND + "' or '" +
    						   SecuritiesAccountTransactionManager.Type.DISTRIBUTION + "'");
    		throw new InvalidCommandLineArgsException();
    	}
    }
    if (! silent)
    	System.err.println("Offsetting account ID: " + offsetAcctID);
    
    // --
    
    // <nof-stocks>
    if ( tuple.nofStocks != null ) 
    {
    	if ( mode == BookMode.LISTFILE &&
       		 tuple.nofStocks.trim().equals("") )
       	{
    		// Technically set, but logically unset
        	if ( type == SecuritiesAccountTransactionManager.Type.BUY_STOCK )
        	{
        		System.err.println("Error: <nof-stocks> must be set with <type> = '" + SecuritiesAccountTransactionManager.Type.BUY_STOCK + "'");
        		throw new InvalidCommandLineArgsException();
        	}
       	}
    	else
    	{
        	if ( type != SecuritiesAccountTransactionManager.Type.BUY_STOCK )
        	{
        		System.err.println("Error: <nof-stocks> may only be set with <type> = '" + SecuritiesAccountTransactionManager.Type.BUY_STOCK + "'");
        		throw new InvalidCommandLineArgsException();
        	}

        	try
        	{
        		nofStocks = new FixedPointNumber(Double.parseDouble(tuple.nofStocks));
        	}
        	catch ( Exception exc )
        	{
        		System.err.println("Could not parse <nof-stocks>");
        		throw new InvalidCommandLineArgsException();
        	}
    	}
    }
    else 
    {
    	if ( type == SecuritiesAccountTransactionManager.Type.BUY_STOCK )
    	{
    		System.err.println("Error: <nof-stocks> must be set with <type> = '" + SecuritiesAccountTransactionManager.Type.BUY_STOCK + "'");
    		throw new InvalidCommandLineArgsException();
    	}
    }
    if (! silent)
    	System.err.println("No. of stocks: " + nofStocks);

    // <stock-price>
    if ( tuple.stockPrc != null ) 
    {
    	if ( mode == BookMode.LISTFILE &&
    		 tuple.stockPrc.trim().equals("") )
    	{
       		// Technically set, but logically unset
        	if ( type == SecuritiesAccountTransactionManager.Type.BUY_STOCK )
        	{
        		System.err.println("Error: <stock-price> must be set with <type> = '" + SecuritiesAccountTransactionManager.Type.BUY_STOCK + "'");
        		throw new InvalidCommandLineArgsException();
        	}
    	}
    	else
    	{
    		if ( type != SecuritiesAccountTransactionManager.Type.BUY_STOCK )
    		{
    			System.err.println("Error: <stock-price> may only be set with <type> = '" + SecuritiesAccountTransactionManager.Type.BUY_STOCK + "'");
    			throw new InvalidCommandLineArgsException();
    		}
               	
    		try
    		{
    			BigMoney betrag = BigMoney.of(CurrencyUnit.EUR, Double.parseDouble(tuple.stockPrc));
    			stockPrc = new FixedPointNumber(betrag.getAmount());
    		}
    		catch ( Exception exc )
    		{
    			System.err.println("Could not parse <stock-price>");
    			throw new InvalidCommandLineArgsException();
    		}
    	}
    } 
    else 
    {
    	if ( type == SecuritiesAccountTransactionManager.Type.BUY_STOCK )
    	{
    		System.err.println("Error: <stock-price> must be set with <type> = '" + SecuritiesAccountTransactionManager.Type.BUY_STOCK + "'");
    		throw new InvalidCommandLineArgsException();
    	}
    }
    if (! silent)
    	System.err.println("Stock price: " + stockPrc);

    // <divid-distrib-gross>
    if ( tuple.divDistrGross != null ) 
    {
    	if ( mode == BookMode.LISTFILE &&
          	 tuple.divDistrGross.trim().equals( "" ) )
    	{
    		// Technically set, but logically unset
        	if ( type == SecuritiesAccountTransactionManager.Type.DIVIDEND ||
        		 type == SecuritiesAccountTransactionManager.Type.DISTRIBUTION )
        	{
        		System.err.println("Error: <divid-distrib-gross> must be set with <type> = '" + type + "'");
        		throw new InvalidCommandLineArgsException();
        	}
    	}
    	else
    	{
        	if ( type != SecuritiesAccountTransactionManager.Type.DIVIDEND &&
        		 type != SecuritiesAccountTransactionManager.Type.DISTRIBUTION )
        	{
        		System.err.println("Error: <divid-distrib-gross> may only be set with <type> = '" + type + "'");
        		throw new InvalidCommandLineArgsException();
        	}
        	
            try
            {
              BigMoney betrag = BigMoney.of(CurrencyUnit.EUR, Double.parseDouble(tuple.divDistrGross));
              divDistrGross = new FixedPointNumber(betrag.getAmount());
            }
            catch ( Exception exc )
            {
              System.err.println("Could not parse <divid-distrib-gross>");
              throw new InvalidCommandLineArgsException();
            }
    	}
    } 
    else 
    {
    	if ( type == SecuritiesAccountTransactionManager.Type.DIVIDEND ||
    		 type == SecuritiesAccountTransactionManager.Type.DISTRIBUTION )
    	{
    		System.err.println("Error: <divid-distrib-gross> must be set with <type> = '" + type + "'");
    		throw new InvalidCommandLineArgsException();
    	}
    }
    if (! silent)
    	System.err.println("Gross divid./distrib.: " + divDistrGross);

    // <stock-split-factor>
    if ( tuple.stockSplitFactor != null ) 
    {
    	if ( mode == BookMode.LISTFILE &&
          	 tuple.stockSplitFactor.trim().equals( "" ) )
    	{
    		// Technically set, but logically unset
        	if ( type == SecuritiesAccountTransactionManager.Type.STOCK_SPLIT )
        	{
        		System.err.println("Error: <stock-split-factor> must be set with <type> = '" + SecuritiesAccountTransactionManager.Type.STOCK_SPLIT + "'");
        		throw new InvalidCommandLineArgsException();
        	}
    	}
    	else
    	{
        	if ( type != SecuritiesAccountTransactionManager.Type.STOCK_SPLIT )
        	{
        		System.err.println("Error: <stock-split-factor> may only be set with <type> = '" + SecuritiesAccountTransactionManager.Type.STOCK_SPLIT + "'");
        		System.err.println("ssf: '" + tuple.stockSplitFactor + "'");
        		throw new InvalidCommandLineArgsException();
        	}
        	
            try
            {
              BigMoney betrag = BigMoney.of(CurrencyUnit.EUR, Double.parseDouble(tuple.stockSplitFactor));
              stockSplitFactor = new FixedPointNumber(betrag.getAmount());
            }
            catch ( Exception exc )
            {
              System.err.println("Could not parse <stock-split-factor>");
              throw new InvalidCommandLineArgsException();
            }
    	}
    } 
    else 
    {
    	if ( type == SecuritiesAccountTransactionManager.Type.STOCK_SPLIT )
    	{
    		System.err.println("Error: <stock-split-factor> must be set with <type> = '" + SecuritiesAccountTransactionManager.Type.STOCK_SPLIT + "'");
    		throw new InvalidCommandLineArgsException();
    	}
    }
    if (! silent)
    	System.err.println("Stock split factor: " + stockSplitFactor);

    // --

    // <date-format>
    if ( tuple.dateFormat != null ) 
    {
    	if ( mode == BookMode.LISTFILE &&
          	 tuple.dateFormat.trim().equals( "" ) )
    	{
    		// Technically set, but logically unset
        	System.err.println("Error: <date-format> is not set");
        	throw new InvalidCommandLineArgsException();
    	}
    	else
    	{
            try
            {
            	dateFormat = CmdLineHelper.getDateFormat(tuple.dateFormat, "date-format");
            }
            catch ( Exception exc )
            {
              System.err.println("Could not parse <stock-split-factor>");
              throw new InvalidCommandLineArgsException();
            }
    	}
    } 
    else 
    {
    	System.err.println("Error: <date-format> is not set");
    	throw new InvalidCommandLineArgsException();
    }
    if (! silent)
    	System.err.println("date-format: " + dateFormat);

    // <date-posted>
    if ( tuple.datPst != null )
    {
    	if ( mode == BookMode.LISTFILE &&
          	 tuple.datPst.trim().equals( "" ) )
    	{
    		// Technically set, but logically unset
            System.err.println("<date-posted> is not set");
            throw new InvalidCommandLineArgsException();
    	}
    	else
    	{
            try
            {
            	datPst = CmdLineHelper.getDate(tuple.datPst, "date-posted", dateFormat); 
            }
            catch ( Exception exc )
            {
            	System.err.println("Could not parse <date-posted>");
            	throw new InvalidCommandLineArgsException();
            }
    	}
    }
    else
    {
        System.err.println("<date-posted> is not set");
        throw new InvalidCommandLineArgsException();
    }
    if (! silent)
    	System.err.println("Date posted: " + datPst.toString());
    
    // <description>
    if ( tuple.descr != null )
    {
    	if ( mode == BookMode.LISTFILE &&
          	 tuple.descr.trim().equals( "" ) )
    	{
    		// Technically set, but logically unset
    		descr = "Generated by GenDepotTrx, " + LocalDateTime.now();
    	}
    	else
    	{
        	try
        	{
        		descr = tuple.descr;
        	}
        	catch ( Exception exc )
        	{
        		System.err.println("Could not parse <description>");
        		throw new InvalidCommandLineArgsException();
        	}
    	}
    }
    else
    {
      descr = "Generated by GenDepotTrx, " + LocalDateTime.now();
    }
    if (! silent)
    	System.err.println("Description: '" + descr + "'");
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GenDepotTrx", options );
    
    System.err.println("");
    System.err.println("Valid values for <mode>:");
    for ( BookMode elt : BookMode.values() )
    	System.err.println(" - " + elt);

    System.err.println("");
    System.err.println("Valid values for <type>:");
    for ( SecuritiesAccountTransactionManager.Type elt : SecuritiesAccountTransactionManager.Type.values() )
    	System.err.println(" - " + elt);
  }
}
