package org.gnucash.tools.xml.gen.complex;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashAccount;
import org.gnucash.api.write.GnuCashWritableAccount;
import org.gnucash.apiext.secacct.SecuritiesAccountManager;
import org.gnucash.apiext.secacct.WritableSecuritiesAccountManager;
import org.gnucash.apispec.read.GnuCashSecurity;
import org.gnucash.apispec.write.impl.GnuCashWritableFileExtImpl;
import org.gnucash.base.basetypes.complex.GCshCmdtyNameSpace;
import org.gnucash.base.basetypes.complex.GCshSecID_SecIdType;
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
  private static GnuCashWritableFileExtImpl gcshFile = null;
		  
  // ------------------------------

  private static Helper.Mode           acctMode     = null;
  private static GCshAcctID            acctID       = null;
  private static String                acctName     = null;
  
  private static Helper.CmdtySecSingleSelMode   secMode      = null;
  private static GCshSecID_SecIdType   secID        = null;
  private static String                isin         = null;
  private static String                secName      = null;

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
      .get();
        
    Option optFileOut = Option.builder("of")
      .required()
      .hasArg()
      .argName("file")
      .desc("GnuCash file (out)")
      .longOpt("gnucash-out-file")
      .get();
    
    Option optAcctMode = Option.builder("am")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode for account")
      .longOpt("account-mode")
      .get();
    	      
    Option optAcctID = Option.builder("acct")
      .hasArg()
      .argName("acctid")
      .desc("Account-ID")
      .longOpt("account-id")
      .get();
    	    
    Option optAcctName = Option.builder("an")
      .hasArg()
      .argName("name")
      .desc("Account name (or part of)")
      .longOpt("account-name")
      .get();
    	      
    Option optSecMode = Option.builder("sm")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode for security")
      .longOpt("security-mode")
      .get();
    	    	        
    Option optSecID = Option.builder("sec")
      .hasArg()
      .argName("ID")
      .desc("Security ID")
      .longOpt("security-id")
      .get();
    	            
    Option optSecISIN = Option.builder("is")
      .hasArg()
      .argName("isin")
      .desc("ISIN")
      .longOpt("isin")
      .get();
    	          
    Option optSecName = Option.builder("sn")
      .hasArg()
      .argName("name")
      .desc("Security name (or part of)")
      .longOpt("security-name")
      .get();
    
    // ---
    	    
    Option optSilent = Option.builder("sl")
      .desc("Silent mode")
      .longOpt("silent")
      .get();

    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optAcctMode );
    options.addOption(optAcctID);
    options.addOption(optAcctName);
    options.addOption(optSecMode);
    options.addOption(optSecID);
    options.addOption(optSecISIN);
    options.addOption(optSecName);
    options.addOption(optSilent);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cfg) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
	  gcshFile = new GnuCashWritableFileExtImpl(new File(gcshInFileName), true);

	  GnuCashWritableAccount acct = getSecAccount();
	  GnuCashSecurity sec = getSecurity();
	  
	  WritableSecuritiesAccountManager secAcctMgr = new WritableSecuritiesAccountManager(acct);
	  if ( stockAcctAlreadyExists(secAcctMgr, sec) )
	  {
		  System.err.println("Error: Stock account already exists");
		  throw new IllegalStateException("Stock account already exists");
	  }
	  
	  GnuCashWritableAccount newStockAcct = secAcctMgr.genShareAcct( sec );
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
  
  private GnuCashSecurity getSecurity() throws Exception
  {
    GnuCashSecurity sec = null;
    
    if ( secMode == Helper.CmdtySecSingleSelMode.ID )
    {
      sec = gcshFile.getSecurityByID(secID);
      if ( sec == null )
      {
        if ( ! silent )
          System.err.println("Could not find a security with this ID.");
        throw new NoEntryFoundException();
      }
    }
    else if ( secMode == Helper.CmdtySecSingleSelMode.ISIN )
    {
      sec = gcshFile.getSecurityByXCode(isin);
      if ( sec == null )
      {
        if ( ! silent )
          System.err.println("Could not find securities with this ISIN.");
        throw new NoEntryFoundException();
      }
    }
    else if ( secMode == Helper.CmdtySecSingleSelMode.NAME )
    {
      Collection<GnuCashSecurity> secList = gcshFile.getSecuritiesByName(secName); 
      if ( secList.size() == 0 )
      {
        if ( ! silent )
          System.err.println("Could not find securities matching this name.");
        throw new NoEntryFoundException();
      }
      else if ( secList.size() > 1 )
      {
        if ( ! silent )
        {
          System.err.println("Found " + secList.size() + "securities matching this name.");
          System.err.println("Please specify more precisely.");
        }
        throw new TooManyEntriesFoundException();
      }
      sec = secList.iterator().next(); // first element
    }
    
    if ( ! silent )
      System.out.println("Security: " + sec.toString());

    return sec;
  }
  
  private boolean stockAcctAlreadyExists(SecuritiesAccountManager secAcctMgr, GnuCashSecurity sec)
  {
	  for ( GnuCashAccount acct : secAcctMgr.getShareAccts() )
	  {
		  if ( acct.getCmdtyID().toString().equals( sec.getQualifID().toString() ) ) // Important: toString()
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

    // <security-mode>
    try
    {
      secMode = Helper.CmdtySecSingleSelMode.valueOf(cmdLine.getOptionValue("security-mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <security-mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! silent )
      System.err.println("Security mode: " + secMode);

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

    // <security-id>
    if ( cmdLine.hasOption("security-id") )
    {
      if ( secMode != Helper.CmdtySecSingleSelMode.ID )
      {
        System.err.println("<security-id> must only be set with <security-mode> = '" + Helper.CmdtySecSingleSelMode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        secID = new GCshSecID_SecIdType( GCshCmdtyNameSpace.SecIdType.ISIN, cmdLine.getOptionValue("security-id") );
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <security-id>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( secMode == Helper.CmdtySecSingleSelMode.ID )
      {
        System.err.println("<security-id> must be set with <security-mode> = '" + Helper.CmdtySecSingleSelMode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!silent)
      System.err.println("Security ID:  '" + secID + "'");

    // <isin>
    if ( cmdLine.hasOption("isin") )
    {
      if ( secMode != Helper.CmdtySecSingleSelMode.ISIN )
      {
        System.err.println("<isin> must only be set with <security-mode> = '" + Helper.CmdtySecSingleSelMode.ISIN.toString() + "'");
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
      if ( secMode == Helper.CmdtySecSingleSelMode.ISIN )
      {
        System.err.println("<isin> must be set with <security-mode> = '" + Helper.CmdtySecSingleSelMode.ISIN.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!silent)
      System.err.println("Security ISIN: '" + isin + "'");

    // <security-name>
    if ( cmdLine.hasOption("security-name") )
    {
      if ( secMode != Helper.CmdtySecSingleSelMode.NAME )
      {
        System.err.println("<security-name> must only be set with <security-mode> = '" + Helper.CmdtySecSingleSelMode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        secName = cmdLine.getOptionValue("security-name");
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <security-name>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( secMode == Helper.CmdtySecSingleSelMode.NAME )
      {
        System.err.println("<security-name> must be set with <security-mode> = '" + Helper.CmdtySecSingleSelMode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!silent)
      System.err.println("Security name: '" + secName + "'");
  }

  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GenStockAcct", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    System.out.println("");
    System.out.println("Valid values for <account-mode>:");
    for ( Helper.Mode elt : Helper.Mode.values() )
      System.out.println(" - " + elt);

    System.out.println("");
    System.out.println("Valid values for <security-mode>:");
    for ( Helper.CmdtySecSingleSelMode elt : Helper.CmdtySecSingleSelMode.values() )
    {
    	System.out.println(" - " + elt);
    }
  }
}
