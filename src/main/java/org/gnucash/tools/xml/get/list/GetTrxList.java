package org.gnucash.tools.xml.get.list;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
import org.gnucash.base.basetypes.simple.GCshAcctID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.dateutils.DateHelpers;
import xyz.schnorxoborx.base.dateutils.LocalDateHelpers;

public class GetTrxList extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetTrxList.class);
  
  // -----------------------------------------------------------------

  private static final double VALUE_MIN = 0.0;     // abs.
  private static final double VALUE_MAX = 10000.0; // abs.
  private static final double VALUE_UNSET = -1.0;
  
  private static final int    NOF_SPLITS_MIN = 1;
  private static final int    NOF_SPLITS_MAX = 999;

  // -----------------------------------------------------------------
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  // ------------------------------
  
  private static String     gcshFileName  = null;
  
  private static GCshAcctID acctID        = null;
  
  private static LocalDate  dateFrom      = null; 
  private static LocalDate  dateTo        = null; 
  
  private static double     valueFrom     = VALUE_UNSET; 
  private static double     valueTo       = VALUE_UNSET; 
  
  private static int        nofSplitsFrom = 0; 
  private static int        nofSplitsTo   = 0; 
  
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
    // acctID = UUID.randomUUID();

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
    Option optAcct = Option.builder("acct")
      .hasArg()
      .argName("acct")
      .desc("From/to account")
      .longOpt("account-id")
      .build();
    	          
    Option optDateFrom = Option.builder("fd")
      .hasArg()
      .argName("date")
      .desc("From date")
      .longOpt("from-date")
      .build();
          
    Option optDateTo = Option.builder("td")
      .hasArg()
      .argName("date")
      .desc("To date")
      .longOpt("to-date")
      .build();
    	          
    Option optValueFrom = Option.builder("fv")
      .hasArg()
      .argName("value")
      .desc("From value")
      .longOpt("from-value")
      .build();
    	          
    Option optValueTo = Option.builder("tv")
      .hasArg()
      .argName("value")
      .desc("To value")
      .longOpt("to-value")
      .build();
    	    	          
    Option optNofSplitsFrom = Option.builder("fns")
      .hasArg()
      .argName("no")
      .desc("From no. of splits")
      .longOpt("from-nof-splits")
      .build();
    	          
    Option optNofSplitsTo = Option.builder("tns")
      .hasArg()
      .argName("no")
      .desc("To no. of splits")
      .longOpt("to-nof-splits")
      .build();
    
    // ::TODO
    // - action (split)
    // - state (split)
    // - payee (split)
    // - shares (split)
    // - entry date (trx)
    // - memo (split, part of)
    // - description (trx, part of)
    	    	          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optAcct);
    options.addOption(optDateFrom);
    options.addOption(optDateTo);
    options.addOption(optValueFrom);
    options.addOption(optValueTo);
    options.addOption(optNofSplitsFrom);
    options.addOption(optNofSplitsTo);
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
    
    // 1) Initial search
    List<? extends GnuCashTransaction> trxList = null; 
    if ( dateFrom != Const.TRX_SUPER_EARLY_DATE ||
         dateTo   != Const.TRX_SUPER_LATE_DATE ) 
    {
    	// Filter by date
	    System.err.println("tt0.1");
     	trxList = gcshFile.getTransactions(dateFrom, dateTo);
    }
    else 
    {
    	// Get them all
	    System.err.println("tt0.2");
    	trxList = gcshFile.getTransactions();
    }
    
    // 2) More filtering, if necessary
    // 2.1) To circumvent casting issue:
    ArrayList<GnuCashTransaction> trxList1 = new ArrayList<GnuCashTransaction>();
    trxList1.addAll(trxList);
    System.err.println("tt1: " + trxList1.size());
    
    // 2.2) Core: 
    if ( acctID != null ||
       	 valueFrom != VALUE_MIN ||
       	 valueTo   != VALUE_MAX || 
    	 nofSplitsFrom != NOF_SPLITS_MIN ||
    	 nofSplitsTo   != NOF_SPLITS_MAX )
    {
    	if ( acctID != null )
       	{
    	    System.err.println("tt2");
    	    
            ArrayList<GnuCashTransaction> trxList2 = new ArrayList<GnuCashTransaction>();
    		for ( GnuCashTransaction trx : trxList1 )
    		{
    			for ( GnuCashTransactionSplit splt : trx.getSplits() )
               	{
    				if ( splt.getAccountID().toString().equals( acctID.toString() ) )
    				{
    					if ( ! trxList2.contains( trx ) ) 
    					{
                       		trxList2.add(trx);
    					}
    				}
               	}
    		}
    		
    		trxList1.clear();
    		trxList1.addAll(trxList2);
    	    System.err.println("tt2: " + trxList1.size());
       	}
    	
    	if ( valueFrom != VALUE_MIN ||
    		 valueTo   != VALUE_MAX )
    	{
    	    System.err.println("tt4");
    	    
            ArrayList<GnuCashTransaction> trxList2 = new ArrayList<GnuCashTransaction>();
            for ( GnuCashTransaction trx : trxList1 )
            {
    			for ( GnuCashTransactionSplit splt : trx.getSplits() )
               	{
    				if ( splt.getValue().abs().doubleValue() >= valueFrom &&
    					 splt.getValue().abs().doubleValue() <= valueTo )
    				{
    					if ( ! trxList2.contains( trx ) ) 
    					{
                       		trxList2.add(trx);
    					}
    				}
               	}
            }
    		
    		trxList1.clear();
    		trxList1.addAll(trxList2);
    	    System.err.println("tt4: " + trxList1.size());
    	}
    	
    	if ( nofSplitsFrom != NOF_SPLITS_MIN ||
    		 nofSplitsTo   != NOF_SPLITS_MAX )
    	{
    	    System.err.println("tt5");
    	    
            ArrayList<GnuCashTransaction> trxList2 = new ArrayList<GnuCashTransaction>();
            for ( GnuCashTransaction trx : trxList1 )
            {
            	if ( trx.getSplitsCount() >= nofSplitsFrom &&
            		 trx.getSplitsCount() <= nofSplitsTo )
            	{
					if ( ! trxList2.contains( trx ) ) 
					{
						trxList2.add(trx);
					}
            	}
            }
            
    		trxList1.clear();
    		trxList1.addAll(trxList2);
    	    System.err.println("tt5: " + trxList1.size());
    	}
    }
    
    // 3) Show results
    if ( trxList1.size() == 0 ) 
    {
    	System.err.println("Found no transaction matching the criteria.");
    	throw new NoEntryFoundException();
    }

	System.err.println("Found " + trxList1.size() + " transaction(s).");
    for ( GnuCashTransaction trx : trxList1 )
    {
    	System.out.println(" - " + trx.toString());
        for ( GnuCashTransactionSplit splt : trx.getSplits())
        {
        	System.out.println("   o " + splt.toString());
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
      System.err.println("Account ID: " + acctID);
    
    // ---
    
    // <from-date>
    if ( cmdLine.hasOption( "from-date" ) )
    {
        try
        {
        	dateFrom = LocalDateHelpers.parseLocalDate( cmdLine.getOptionValue("from-date"), DateHelpers.DATE_FORMAT_2);
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <from-date>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    else
    {
    	dateFrom = Const.TRX_SUPER_EARLY_DATE;
    }
    
    if ( ! scriptMode )
      System.err.println("From date: " + dateFrom);
    
    // <to-date>
    if ( cmdLine.hasOption( "to-date" ) )
    {
        try
        {
        	dateTo = LocalDateHelpers.parseLocalDate( cmdLine.getOptionValue("to-date"), DateHelpers.DATE_FORMAT_2);
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <to-date>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    else
    {
    	dateTo = Const.TRX_SUPER_LATE_DATE;
    }
    
    if ( ! scriptMode )
      System.err.println("To date:   " + dateTo);
    
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
    else
    {
    	valueFrom = VALUE_MIN;
    }
    
    if ( ! scriptMode )
      System.err.println("From value: " + valueFrom);
    
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
    else
    {
    	valueTo = VALUE_MAX;
    }
    
    if ( ! scriptMode )
      System.err.println("To value:   " + valueTo);
    
    // ---
    
    // <nof-splits-from>
    if ( cmdLine.hasOption( "nof-splits-from" ) )
    {
        try
        {
        	nofSplitsFrom = Integer.parseInt( cmdLine.getOptionValue("nof-splits-from"));
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <nof-splits-from>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    else
    {
    	nofSplitsFrom = NOF_SPLITS_MIN;
    }
    
    if ( ! scriptMode )
      System.err.println("From no. of splits: " + nofSplitsFrom);
    
    // <nof-splits-to>
    if ( cmdLine.hasOption( "nof-splits-to" ) )
    {
        try
        {
        	nofSplitsTo = Integer.parseInt( cmdLine.getOptionValue("nof-splits-to"));
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <nof-splits-to>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    else
    {
    	nofSplitsTo = NOF_SPLITS_MAX;
    }
    
    if ( ! scriptMode )
      System.err.println("To no. of splits:   " + nofSplitsTo);
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GetTrxList", options );
  }
}
