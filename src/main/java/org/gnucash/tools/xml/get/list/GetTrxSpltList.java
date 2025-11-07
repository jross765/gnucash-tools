package org.gnucash.tools.xml.get.list;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashTransactionSplit;
import org.gnucash.api.read.impl.GnuCashFileImpl;
import org.gnucash.apiext.Const;
import org.gnucash.apiext.trxmgr.TransactionSplitFilter;
import org.gnucash.apiext.trxmgr.TransactionSplitFinder;
import org.gnucash.base.basetypes.simple.GCshAcctID;
import org.gnucash.base.basetypes.simple.GCshIDNotSetException;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class GetTrxSpltList extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetTrxSpltList.class);
  
  // -----------------------------------------------------------------
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  // ------------------------------
  
  private static String     gcshFileName    = null;
  
  private static GnuCashTransactionSplit.Action     action     = null;
  private static GnuCashTransactionSplit.ReconState reconState = null;
  
  private static GCshAcctID acctID          = null;
  
  private static double     valueFrom       = Const.UNSET_VALUE; 
  private static double     valueTo         = Const.UNSET_VALUE; 
  
  private static double     quantityFrom    = Const.UNSET_VALUE; 
  private static double     quantityTo      = Const.UNSET_VALUE; 
  
  private static boolean    showFlt         = false; 
  
  // ------------------------------
  
  private static boolean scriptMode = false; // ::TODO

  // -----------------------------------------------------------------
  
  public static void main( String[] args )
  {
    try
    {
      GetTrxSpltList tool = new GetTrxSpltList ();
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
    
    Option optShowFilter = Option.builder("sflt")
      .desc("Show filter (for debugging purposes)")
      .longOpt("show-filter")
      .build();
    	    	    
    Option optShowSplits = Option.builder("ssplt")
      .desc("Show splits")
      .longOpt("show-splits")
      .build();
    	    
    // ::TODO
    // - memo (split, part of)
    // - description (trx, part of)
    	    	          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optAction);
    options.addOption(optReconState);
    options.addOption(optAcct);
    options.addOption(optValueFrom);
    options.addOption(optValueTo);
    options.addOption(optNofSharesFrom);
    options.addOption(optNofSharesTo);
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
    TransactionSplitFilter spltFlt = setFilter();
    
    if ( showFlt )
    {
        System.err.println("");
        System.err.println("Filter: " + spltFlt.toString());
        System.err.println("");
    }
    
    // 2) Find transactions, applying filter
    TransactionSplitFinder spltFnd = new TransactionSplitFinder(gcshFile);
    ArrayList<GnuCashTransactionSplit> spltList = spltFnd.find(spltFlt);
    
    // 3) Show results
    showResults( spltList );
  }

  // -----------------------------------------------------------------

  private TransactionSplitFilter setFilter() throws GCshIDNotSetException
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
    
    // ---

	return spltFlt;
  }

  private void showResults(ArrayList<GnuCashTransactionSplit> spltList) throws NoEntryFoundException
  {
	if ( spltList.size() == 0 ) 
    {
    	System.err.println("Found no transaction splits matching the criteria.");
    	throw new NoEntryFoundException();
    }

	System.err.println("Found " + spltList.size() + " transaction split(s).");
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
    
    // <show-filter>
    if ( cmdLine.hasOption( "show-filter" ) )
    {
        showFlt = true;
    }
    
    if ( ! scriptMode )
      System.err.println("Show filter:        " + showFlt);
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
