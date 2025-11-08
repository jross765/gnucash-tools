package org.gnucash.tools.xml.get.list;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashTransaction;
import org.gnucash.api.read.GnuCashTransactionSplit;
import org.gnucash.api.read.impl.GnuCashFileImpl;
import org.gnucash.apiext.Const;
import org.gnucash.apiext.trxmgr.TransactionFilter;
import org.gnucash.apiext.trxmgr.TransactionFilter.SplitLogic;
import org.gnucash.apiext.trxmgr.TransactionFinder;
import org.gnucash.apiext.trxmgr.TransactionSplitFilter;
import org.gnucash.base.basetypes.simple.GCshAcctID;
import org.gnucash.base.basetypes.simple.GCshIDNotSetException;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.dateutils.DateHelpers;
import xyz.schnorxoborx.base.dateutils.LocalDateHelpers;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class GetTrxList extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetTrxList.class);
  
  // -----------------------------------------------------------------
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  // ------------------------------
  
  private static String     gcshFileName    = null;
  
  private static GnuCashTransactionSplit.Action     action     = null;
  private static GnuCashTransactionSplit.ReconState reconState = null;
  
  private static GCshAcctID acctID          = null;
  
  private static LocalDate  datePostedFrom  = TransactionFilter.DATE_UNSET; 
  private static LocalDate  datePostedTo    = TransactionFilter.DATE_UNSET; 
  
  private static LocalDate  dateEnteredFrom = TransactionFilter.DATE_UNSET; 
  private static LocalDate  dateEnteredTo   = TransactionFilter.DATE_UNSET; 
  
  private static double     valueFrom       = Const.UNSET_VALUE; 
  private static double     valueTo         = Const.UNSET_VALUE; 
  
  private static double     quantityFrom    = Const.UNSET_VALUE; 
  private static double     quantityTo      = Const.UNSET_VALUE; 
  
  private static int        nofSplitsFrom   = TransactionFilter.NOF_SPLT_UNSET; 
  private static int        nofSplitsTo     = TransactionFilter.NOF_SPLT_UNSET; 
  
  private static String     descrTrx        = null; 
  private static String     descrSplt       = null; 
  
  private static boolean    showFlt         = false; 
  private static boolean    showSplt        = false; 
  
  // ------------------------------
  
  private static boolean scriptMode = false; // ::TODO

  // -----------------------------------------------------------------
  
  public static void main( String[] args )
  {
    try
    {
      GetTrxList tool = new GetTrxList ();
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
    Option optFile = Option.builder("f")
      .required()
      .hasArg()
      .argName("file")
      .desc("GnuCash file")
      .longOpt("gnucash-file")
      .build();
      
    // The convenient ones
    Option optAction = Option.builder("act")
      .hasArg()
      .argName("act")
      .desc("Action (split level)")
      .longOpt("action")
      .build();
    	          
    Option optReconState = Option.builder("stat")
      .hasArg()
      .argName("stat")
      .desc("Reconciliation state (split level)")
      .longOpt("recon-state")
      .build();
    
    // ---
    
    Option optAcct = Option.builder("acct")
      .hasArg()
      .argName("acct")
      .desc("Account ID")
      .longOpt("account-id")
      .build();
    
    // ---
    
    Option optDatePostedFrom = Option.builder("fdp")
      .hasArg()
      .argName("date")
      .desc("From date posted")
      .longOpt("from-date-posted")
      .build();
    
    Option optDatePostedTo = Option.builder("tdp")
      .hasArg()
      .argName("date")
      .desc("To date posted")
      .longOpt("to-date-posted")
      .build();
    
    // ---
    
    Option optDateEnteredFrom = Option.builder("fde")
      .hasArg()
      .argName("date")
      .desc("From date entered")
      .longOpt("from-date-entered")
      .build();
    
    Option optDateEnteredTo = Option.builder("tde")
      .hasArg()
      .argName("date")
      .desc("To date entered")
      .longOpt("to-date-entered")
      .build();
    
    // ---
    
    Option optValueFrom = Option.builder("fv")
      .hasArg()
      .argName("value")
      .desc("From value (split level)")
      .longOpt("from-value")
      .build();
    	          
    Option optValueTo = Option.builder("tv")
      .hasArg()
      .argName("value")
      .desc("To value (split level)")
      .longOpt("to-value")
      .build();
    	    	          
    // ---
    
    Option optNofSharesFrom = Option.builder("fq")
      .hasArg()
      .argName("no")
      .desc("From quantity (split level)")
      .longOpt("from-quantity")
      .build();
    	          
    Option optNofSharesTo = Option.builder("tq")
      .hasArg()
      .argName("no")
      .desc("To quantity (split level)")
      .longOpt("to-quantity")
      .build();
    	    	          
    // ---
    
    Option optNofSplitsFrom = Option.builder("fnsp")
      .hasArg()
      .argName("no")
      .desc("From no. of splits")
      .longOpt("from-nof-splits")
      .build();
    	          
    Option optNofSplitsTo = Option.builder("tnsp")
      .hasArg()
      .argName("no")
      .desc("To no. of splits")
      .longOpt("to-nof-splits")
      .build();
    
    // ---
    
    Option optDescrTrx = Option.builder("dtrx")
      .hasArg()
      .argName("str")
      .desc("Description (transaction level)s")
      .longOpt("description-transaction")
      .build();
    	          
    Option optDescrSplt = Option.builder("dsplt")
      .hasArg()
      .argName("str")
      .desc("Description (split level)")
      .longOpt("description-split")
      .build();
    
    // ---
    
    Option optShowFilter = Option.builder("sflt")
      .desc("Show filter (for debugging purposes)")
      .longOpt("show-filter")
      .build();
    	    	    
    Option optShowSplits = Option.builder("ssplt")
      .desc("Show splits")
      .longOpt("show-splits")
      .build();
    	    
    	    	          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optAction);
    options.addOption(optReconState);
    options.addOption(optAcct);
    options.addOption(optDatePostedFrom);
    options.addOption(optDatePostedTo);
    options.addOption(optDateEnteredFrom);
    options.addOption(optDateEnteredTo);
    options.addOption(optValueFrom);
    options.addOption(optValueTo);
    options.addOption(optNofSharesFrom);
    options.addOption(optNofSharesTo);
    options.addOption(optNofSplitsFrom);
    options.addOption(optNofSplitsTo);
    options.addOption(optDescrTrx);
    options.addOption(optDescrSplt);
    options.addOption(optShowFilter);
    options.addOption(optShowSplits);
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
    
    // 1) Set filter
    TransactionFilter trxFlt = setFilter();
    
    if ( showFlt )
    {
        System.err.println("");
        System.err.println("Filter: " + trxFlt.toString());
        System.err.println("");
    }
    
    // 2) Find transactions, applying filter
    TransactionFinder trxFnd = new TransactionFinder(gcshFile);
    ArrayList<GnuCashTransaction> trxList = trxFnd.find(trxFlt, true, SplitLogic.OR);
    
    // 3) Show results
    showResults( trxList );
  }

  // -----------------------------------------------------------------

  private TransactionFilter setFilter() throws GCshIDNotSetException
  {
	TransactionSplitFilter spltFlt = new TransactionSplitFilter();
    
    if ( action != null )
    	spltFlt.action = action;
    if ( reconState != null )
    	spltFlt.reconState = reconState;
    
    if ( acctID != null )
    	spltFlt.acctID.set( acctID );
    
    if ( valueFrom != Const.UNSET_VALUE )
    	spltFlt.valueFrom = new FixedPointNumber(valueFrom);
    if ( valueTo   != Const.UNSET_VALUE )
    	spltFlt.valueTo   = new FixedPointNumber(valueTo);
    spltFlt.valueAbs = true;

    if ( quantityFrom != Const.UNSET_VALUE )
    	spltFlt.quantityFrom = new FixedPointNumber(quantityFrom);
    if ( quantityTo   != Const.UNSET_VALUE )
    	spltFlt.quantityTo   = new FixedPointNumber(quantityTo);
    spltFlt.quantityAbs = true;
    
    if ( descrSplt != null )
    	spltFlt.descrPart = descrSplt;

    // ---

    TransactionFilter trxFlt = new TransactionFilter();
    
    if ( ! datePostedFrom.equals(TransactionFilter.DATE_UNSET) )
    	trxFlt.datePostedFrom  = datePostedFrom;
    if ( ! datePostedTo.equals(TransactionFilter.DATE_UNSET) )
    	trxFlt.datePostedTo    = datePostedTo;
    
    if ( ! dateEnteredFrom.equals(TransactionFilter.DATE_UNSET) )
    	trxFlt.dateEnteredFrom = dateEnteredFrom;
    if ( ! dateEnteredTo.equals(TransactionFilter.DATE_UNSET) )
    	trxFlt.dateEnteredTo   = dateEnteredTo;
    
    if ( nofSplitsFrom != TransactionFilter.NOF_SPLT_UNSET )
    	trxFlt.nofSpltFrom = nofSplitsFrom;
    if ( nofSplitsTo   != TransactionFilter.NOF_SPLT_UNSET )
    	trxFlt.nofSpltTo   = nofSplitsTo;
    
    if ( descrTrx != null )
    	trxFlt.descrPart = descrTrx;

    trxFlt.spltFilt = spltFlt;
    
    // ---

	return trxFlt;
  }

  private void showResults(ArrayList<GnuCashTransaction> trxList) throws NoEntryFoundException
  {
	if ( trxList.size() == 0 ) 
    {
    	System.err.println("Found no transaction matching the criteria.");
    	throw new NoEntryFoundException();
    }

	System.err.println("Found " + trxList.size() + " transaction(s).");
    for ( GnuCashTransaction trx : trxList )
    {
    	System.out.println(" - " + trx.toString());
        if ( showSplt )
        {
        	for ( GnuCashTransactionSplit splt : trx.getSplits())
        	{
        		System.out.println("   o " + splt.toString());
        	}
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
      System.err.println("GnuCash file:      '" + gcshFileName + "'");
    
    // ---
    
    // <action>
    if ( cmdLine.hasOption( "action" ) )
    {
        try
        {
        	action = GnuCashTransactionSplit.Action.valueOf( cmdLine.getOptionValue("action") );
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <action>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
      System.err.println("Action:             " + action);
    
    // <recon-state>
    if ( cmdLine.hasOption( "recon-state" ) )
    {
        try
        {
        	reconState = GnuCashTransactionSplit.ReconState.valueOf( cmdLine.getOptionValue("recon-state") );
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <recon-state>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
      System.err.println("Reconcil. state:    " + reconState);
    
    // ---
    
    // <account-id>
    if ( cmdLine.hasOption( "account-id" ) )
    {
        try
        {
        	acctID = new GCshAcctID( cmdLine.getOptionValue("account-id") );
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <account-id>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
      System.err.println("Account ID:         " + acctID);
    
    // ---
    
    // <from-date-posted>
    if ( cmdLine.hasOption( "from-date-posted" ) )
    {
        try
        {
        	datePostedFrom = LocalDateHelpers.parseLocalDate( cmdLine.getOptionValue("from-date-posted"), DateHelpers.DATE_FORMAT_2);
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <from-date-posted>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
        if ( datePostedFrom.equals(TransactionFilter.DATE_UNSET) )
        	System.err.println("From date posted:   " + "(unset)");
        else
        	System.err.println("From date posted:   " + datePostedFrom);
    }
    
    // <to-date-posted>
    if ( cmdLine.hasOption( "to-date-posted" ) )
    {
        try
        {
        	datePostedTo = LocalDateHelpers.parseLocalDate( cmdLine.getOptionValue("to-date-posted"), DateHelpers.DATE_FORMAT_2);
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <to-date-posted>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
        if ( datePostedTo.equals(TransactionFilter.DATE_UNSET) )
        	System.err.println("To date posted:     " + "(unset)");
        else
        	System.err.println("To date posted:     " + datePostedTo);
    }
    
    // ---
    
    // <from-date-entered>
    if ( cmdLine.hasOption( "from-date-entered" ) )
    {
        try
        {
        	dateEnteredFrom = LocalDateHelpers.parseLocalDate( cmdLine.getOptionValue("from-date-entered"), DateHelpers.DATE_FORMAT_2);
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <from-date-entered>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
        if ( dateEnteredFrom.equals(TransactionFilter.DATE_UNSET) )
        	System.err.println("From date entered:  " + "(unset)");
        else
        	System.err.println("From date entered:  " + dateEnteredFrom);
    }
    
    // <to-date-entered>
    if ( cmdLine.hasOption( "to-date-entered" ) )
    {
        try
        {
        	dateEnteredTo = LocalDateHelpers.parseLocalDate( cmdLine.getOptionValue("to-date-entered"), DateHelpers.DATE_FORMAT_2);
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <to-date-entered>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
        if ( dateEnteredTo.equals(TransactionFilter.DATE_UNSET) )
        	System.err.println("To date entered:    " + "(unset)");
        else
        	System.err.println("To date entered:    " + dateEnteredTo);
    }
    
    // ---
    
    // <from-value>
    if ( cmdLine.hasOption( "from-value" ) )
    {
        try
        {
        	valueFrom = Double.parseDouble( cmdLine.getOptionValue("from-value") );
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <from-value>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
    	if ( valueFrom == Const.UNSET_VALUE )
    		System.err.println("From value:         " + "(unset)");
    	else
    		System.err.println("From value:         " + valueFrom);
    }
  
    // <to-value>
    if ( cmdLine.hasOption( "to-value" ) )
    {
        try
        {
        	valueTo = Double.parseDouble( cmdLine.getOptionValue("to-value") );
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <to-value>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
    	if ( valueTo == Const.UNSET_VALUE )
    		System.err.println("To value:           " + "(unset)");
    	else
    		System.err.println("To value:           " + valueTo);
    }
    
    // ---
    
    // <from-quantity>
    if ( cmdLine.hasOption( "from-quantity" ) )
    {
        try
        {
        	quantityFrom = Double.parseDouble( cmdLine.getOptionValue("from-quantity") );
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <from-quantity>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
    	if ( quantityFrom == Const.UNSET_VALUE )
    		System.err.println("From quantity:      " + "(unset)");
    	else
    		System.err.println("From quantity:      " + quantityFrom);
    }
  
    // <to-quantity>
    if ( cmdLine.hasOption( "to-quantity" ) )
    {
        try
        {
        	quantityTo = Double.parseDouble( cmdLine.getOptionValue("to-quantity") );
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <to-quantity>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
    	if ( quantityTo == Const.UNSET_VALUE )
    		System.err.println("To quantity:        " + "(unset)");
    	else
    		System.err.println("To quantity:        " + quantityTo);
    }
    
    // ---
    
    // <from-nof-splits>
    if ( cmdLine.hasOption( "from-nof-splits" ) )
    {
        try
        {
        	nofSplitsFrom = Integer.parseInt( cmdLine.getOptionValue("from-nof-splits"));
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <from-nof-splits>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
    	if ( nofSplitsFrom == TransactionFilter.NOF_SPLT_UNSET )
    		System.err.println("From no. of splits: " + "(unset)");
    	else
    		System.err.println("From no. of splits: " + nofSplitsFrom);
    }
    
    // <to-nof-splits>
    if ( cmdLine.hasOption( "to-nof-splits" ) )
    {
        try
        {
        	nofSplitsTo = Integer.parseInt( cmdLine.getOptionValue("to-nof-splits"));
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <to-nof-splits>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
    	if ( nofSplitsTo == TransactionFilter.NOF_SPLT_UNSET )
    		System.err.println("To no. of splits:   " + "(unset)");
    	else
    	 	System.err.println("To no. of splits:   " + nofSplitsTo);
    }
    
    // ---
    
    // <description-transaction>
    if ( cmdLine.hasOption( "description-transaction" ) )
    {
        try
        {
        	descrTrx = cmdLine.getOptionValue("description-transaction");
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <description-transaction>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
    	if ( descrTrx == null )
    		System.err.println("Descr. (trx level): " + "(unset)");
    	else
    	 	System.err.println("Descr. (trx level):   " + descrTrx);
    }
    
    // <description-split>
    if ( cmdLine.hasOption( "description-split" ) )
    {
        try
        {
        	descrSplt = cmdLine.getOptionValue("description-split");
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <description-split>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
    	if ( descrSplt == null )
    		System.err.println("Descr. (split level): " + "(unset)");
    	else
    	 	System.err.println("Descr. (split level): " + descrSplt);
    }
    
    // ---
    
    // <show-filter>
    if ( cmdLine.hasOption( "show-filter" ) )
    {
        showFlt = true;
    }
    
    if ( ! scriptMode )
      System.err.println("Show filter:        " + showSplt);
    
    // <show-splits>
    if ( cmdLine.hasOption( "show-splits" ) )
    {
        showSplt = true;
    }
    
    if ( ! scriptMode )
      System.err.println("Show splits:        " + showSplt);
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GetTrxList", options );
    
    System.out.println("");
    System.out.println("Valid values for <action>:");
    for ( GnuCashTransactionSplit.Action elt : GnuCashTransactionSplit.Action.values() )
      System.out.println(" - " + elt);
    
    System.out.println("");
    System.out.println("Valid values for <recon-state>:");
    for ( GnuCashTransactionSplit.ReconState elt : GnuCashTransactionSplit.ReconState.values() )
      System.out.println(" - " + elt);
  }
}
