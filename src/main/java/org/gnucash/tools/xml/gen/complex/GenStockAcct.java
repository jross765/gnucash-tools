package org.gnucash.tools.xml.gen.complex;

import java.io.File;
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
import org.gnucash.api.read.GnuCashCommodity;
import org.gnucash.api.write.GnuCashWritableAccount;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.apiext.secacct.SecuritiesAccountManager;
import org.gnucash.apiext.secacct.WritableSecuritiesAccountManager;
import org.gnucash.base.basetypes.complex.GCshCmdtyCurrNameSpace;
import org.gnucash.base.basetypes.complex.GCshCmdtyID_SecIdType;
import org.gnucash.base.basetypes.simple.GCshAcctID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GenStockAcct extends CommandLineTool
{
  enum BookMode {
	  SINGLE_TRX,
	  LISTFILE
  }

  // -----------------------------------------------------------------

  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GenStockAcct.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  // ------------------------------

  private static String           gcshInFileName = null;
  private static String           gcshOutFileName = null;
  private static GnuCashWritableFileImpl gcshFile = null;
		  
  // ------------------------------

  private static Helper.Mode           acctMode     = null;
  private static GCshAcctID            acctID       = null;
  private static String                acctName     = null;
  
  private static Helper.CmdtySecMode   cmdtyMode      = null;
  private static GCshCmdtyID_SecIdType cmdtyID      = null;
  private static String                isin         = null;
  private static String                cmdtyName    = null;

  // ------------------------------

  // batch-mode:
  private static boolean    silent           = false;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GenStockAcct tool = new GenStockAcct ();
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
    
    Option optAcctMode = Option.builder("am")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode for account")
      .longOpt("account-mode")
      .build();
    	      
    Option optAcctID = Option.builder("acct")
      .hasArg()
      .argName("acctid")
      .desc("Account-ID")
      .longOpt("account-id")
      .build();
    	    
    Option optAcctName = Option.builder("an")
      .hasArg()
      .argName("name")
      .desc("Account name (or part of)")
      .longOpt("account-name")
      .build();
    	      
    Option optCmdtyMode = Option.builder("cm")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode for commodity")
      .longOpt("commodity-mode")
      .build();
    	    	        
    Option optCmdtyID = Option.builder("cmdty")
      .hasArg()
      .argName("ID")
      .desc("Commodity ID")
      .longOpt("commodity-id")
      .build();
    	            
    Option optCmdtyISIN = Option.builder("is")
      .hasArg()
      .argName("isin")
      .desc("ISIN")
      .longOpt("isin")
      .build();
    	          
    Option optCmdtyName = Option.builder("sn")
      .hasArg()
      .argName("name")
      .desc("Commodity name (or part of)")
      .longOpt("commodity-name")
      .build();
    
    // ---
    	    
    Option optSilent = Option.builder("sl")
      .desc("Silent mode")
      .longOpt("silent")
      .build();

    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optAcctMode );
    options.addOption(optAcctID);
    options.addOption(optAcctName);
    options.addOption(optCmdtyMode);
    options.addOption(optCmdtyID);
    options.addOption(optCmdtyISIN);
    options.addOption(optCmdtyName);
    options.addOption(optSilent);
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

	  GnuCashWritableAccount acct = getSecAccount();
	  GnuCashCommodity cmdty = getCommodity();
	  
	  WritableSecuritiesAccountManager secAcctMgr = new WritableSecuritiesAccountManager(acct);
	  if ( stockAcctAlreadyExists(secAcctMgr, cmdty) )
	  {
		  System.err.println("Error: Stock account already exists");
		  throw new IllegalStateException("Stock account already exists");
	  }
	  
	  GnuCashWritableAccount newStockAcct = secAcctMgr.genShareAcct( cmdty );
	  System.out.println("Stock account generated: " + newStockAcct);
	  
	  // ---

	  gcshFile.writeFile(new File(gcshOutFileName));
		    
	  if ( ! silent )
		  System.out.println("OK");
  }
  
  private GnuCashWritableAccount getSecAccount() throws Exception
  {
    GnuCashWritableAccount acct = null;
    
    if (acctMode == Helper.Mode.ID)
    {
      acct = gcshFile.getWritableAccountByID(acctID);
      if (acct == null)
      {
        if ( ! silent )
          System.err.println("Found no account with that name");
        throw new NoEntryFoundException();
      }
    }
    else if (acctMode == Helper.Mode.NAME)
    {
      Collection<GnuCashAccount> acctList = null;
      acctList = gcshFile.getAccountsByTypeAndName(GnuCashAccount.Type.ASSET, acctName, 
    		  									   true, true);
      if ( acctList.size() == 0 )
      {
        if ( ! silent )
        {
          System.err.println("Could not find accounts matching this name.");
        }
        throw new NoEntryFoundException();
      }
      else if ( acctList.size() > 1 )
      {
        if ( ! silent )
        {
          System.err.println("Found " + acctList.size() + " accounts with that name.");
          System.err.println("Please specify more precisely.");
        }
        throw new TooManyEntriesFoundException();
      }
      // No:
      // acct = acctList.iterator().next();
      acct = gcshFile.getWritableAccountByID(acctList.iterator().next().getID());
    }

    if ( ! silent )
      System.out.println("Account:  " + acct.toString());
    
    return acct;
  }
  
  private GnuCashCommodity getCommodity() throws Exception
  {
    GnuCashCommodity cmdty = null;
    
    if ( cmdtyMode == Helper.CmdtySecMode.ID )
    {
      cmdty = gcshFile.getCommodityByQualifID(cmdtyID);
      if ( cmdty == null )
      {
        if ( ! silent )
          System.err.println("Could not find a commodity with this ID.");
        throw new NoEntryFoundException();
      }
    }
    else if ( cmdtyMode == Helper.CmdtySecMode.ISIN )
    {
      cmdty = gcshFile.getCommodityByXCode(isin);
      if ( cmdty == null )
      {
        if ( ! silent )
          System.err.println("Could not find securities with this ISIN.");
        throw new NoEntryFoundException();
      }
    }
    else if ( cmdtyMode == Helper.CmdtySecMode.NAME )
    {
      Collection<GnuCashCommodity> cmdtyList = gcshFile.getCommoditiesByName(cmdtyName); 
      if ( cmdtyList.size() == 0 )
      {
        if ( ! silent )
          System.err.println("Could not find securities matching this name.");
        throw new NoEntryFoundException();
      }
      else if ( cmdtyList.size() > 1 )
      {
        if ( ! silent )
        {
          System.err.println("Found " + cmdtyList.size() + "securities matching this name.");
          System.err.println("Please specify more precisely.");
        }
        throw new TooManyEntriesFoundException();
      }
      cmdty = cmdtyList.iterator().next(); // first element
    }
    
    if ( ! silent )
      System.out.println("Commodity: " + cmdty.toString());

    return cmdty;
  }
  
  private boolean stockAcctAlreadyExists(SecuritiesAccountManager secAcctMgr, GnuCashCommodity cmdty)
  {
	  for ( GnuCashAccount acct : secAcctMgr.getShareAccts() )
	  {
		  if ( acct.getCmdtyCurrID().toString().equals( cmdty.getQualifID().toString() ) ) // Important: toString()
		  {
			  return true;
		  }
	  }
	  
	  return false;
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
    
    // <account-mode>
    try
    {
      acctMode = Helper.Mode.valueOf(cmdLine.getOptionValue("account-mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <account-mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! silent )
      System.err.println("Account mode:  " + acctMode);

    // <commodity-mode>
    try
    {
      cmdtyMode = Helper.CmdtySecMode.valueOf(cmdLine.getOptionValue("commodity-mode"));
      if ( cmdtyMode == Helper.CmdtySecMode.TYPE )
      {
    	  // sic, not valid
          System.err.println("Could not parse <commodity-mode>");
          throw new InvalidCommandLineArgsException();
      }
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <commodity-mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! silent )
      System.err.println("Commodity mode: " + cmdtyMode);

    // <account-id>
    if ( cmdLine.hasOption("account-id") )
    {
      if ( acctMode != Helper.Mode.ID )
      {
        System.err.println("<account-id> must only be set with <account-mode> = '" + Helper.Mode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
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
    else
    {
      if ( acctMode == Helper.Mode.ID )
      {
        System.err.println("<account-id> must be set with <account-mode> = '" + Helper.Mode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }      
    }
    
    if ( ! silent )
      System.err.println("Account ID:    '" + acctID + "'");

    // <account-name>
    if ( cmdLine.hasOption("account-name") )
    {
      if ( acctMode != Helper.Mode.NAME )
      {
        System.err.println("<account-name> must only be set with <account-mode> = '" + Helper.Mode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        acctName = cmdLine.getOptionValue("account-name");
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <name>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( acctMode == Helper.Mode.NAME )
      {
        System.err.println("<account-name> must be set with <account-mode> = '" + Helper.Mode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }      
    }
    
    if ( ! silent )
      System.err.println("Account name:  '" + acctName + "'");

    // <commodity-id>
    if ( cmdLine.hasOption("commodity-id") )
    {
      if ( cmdtyMode != Helper.CmdtySecMode.ID )
      {
        System.err.println("<commodity-id> must only be set with <commodity-mode> = '" + Helper.CmdtySecMode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        cmdtyID = new GCshCmdtyID_SecIdType( GCshCmdtyCurrNameSpace.SecIdType.ISIN, cmdLine.getOptionValue("commodity-id") );
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <commodity-id>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( cmdtyMode == Helper.CmdtySecMode.ID )
      {
        System.err.println("<commodity-id> must be set with <commodity-mode> = '" + Helper.CmdtySecMode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!silent)
      System.err.println("Commodity ID:  '" + cmdtyID + "'");

    // <isin>
    if ( cmdLine.hasOption("isin") )
    {
      if ( cmdtyMode != Helper.CmdtySecMode.ISIN )
      {
        System.err.println("<isin> must only be set with <commodity-mode> = '" + Helper.CmdtySecMode.ISIN.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        isin = cmdLine.getOptionValue("isin");
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <isin>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( cmdtyMode == Helper.CmdtySecMode.ISIN )
      {
        System.err.println("<isin> must be set with <commodity-mode> = '" + Helper.CmdtySecMode.ISIN.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!silent)
      System.err.println("Commodity ISIN: '" + isin + "'");

    // <commodity-name>
    if ( cmdLine.hasOption("commodity-name") )
    {
      if ( cmdtyMode != Helper.CmdtySecMode.NAME )
      {
        System.err.println("<commodity-name> must only be set with <commodity-mode> = '" + Helper.CmdtySecMode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        cmdtyName = cmdLine.getOptionValue("commodity-name");
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <commodity-name>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( cmdtyMode == Helper.CmdtySecMode.NAME )
      {
        System.err.println("<commodity-name> must be set with <commodity-mode> = '" + Helper.CmdtySecMode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!silent)
      System.err.println("Commodity name: '" + cmdtyName + "'");
  }

  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GenStockAcct", options );
    
    System.out.println("");
    System.out.println("Valid values for <account-mode>:");
    for ( Helper.Mode elt : Helper.Mode.values() )
      System.out.println(" - " + elt);

    System.out.println("");
    System.out.println("Valid values for <commodity-mode>:");
    for ( Helper.CmdtySecMode elt : Helper.CmdtySecMode.values() )
    {
      if ( elt != Helper.CmdtySecMode.TYPE ) // sic
        System.out.println(" - " + elt);
    }
  }
}
