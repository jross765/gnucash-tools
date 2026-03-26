package org.gnucash.tools.xml.get.sonstige;

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
import org.gnucash.api.read.GnuCashAccount;
import org.gnucash.apiext.secacct.SecuritiesAccountManager;
import org.gnucash.apispec.read.GnuCashSecurity;
import org.gnucash.apispec.read.impl.GnuCashFileExtImpl;
import org.gnucash.base.basetypes.complex.GCshCmdtyNameSpace;
import org.gnucash.base.basetypes.complex.GCshSecID;
import org.gnucash.base.basetypes.simple.GCshAcctID;
import org.gnucash.tools.CommandLineTool;
import org.gnucash.tools.xml.helper.AccountHelper;
import org.gnucash.tools.xml.helper.CmdLineHelper_Acct;
import org.gnucash.tools.xml.helper.CmdLineHelper_Sec;
import org.gnucash.tools.xml.helper.SecurityHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetStockAcct extends CommandLineTool
{
  // Logger
  private static final Logger LOGGER = LoggerFactory.getLogger(GetStockAcct.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String                gcshFileName = null;
  
  // ---
  
  private static Helper.Mode           acctSelMode  = null;

  private static GCshAcctID            acctID       = new GCshAcctID();
  // This one and the following: sic, StringBuffer, not String,
  // for it has to be mutable because of the way the args are parsed.
  private static StringBuffer          acctName     = new StringBuffer();
  
  // ---
  
  private static Helper.CmdtySecSingleSelMode secSelMode = null;
  private static CmdLineHelper_Sec.SecSelectSubMode secSelSubMode = null;

  private static GCshSecID     secID    = new GCshSecID();
  // This one and the following: sic, StringBuffer, not String,
  // for it has to be mutable because of the way the args are parsed.
  private static StringBuffer  ticker   = new StringBuffer();
  private static StringBuffer  micID    = new StringBuffer();
  private static StringBuffer  isin     = new StringBuffer();
  // Possibly later:
  // private static String  wkn      = new StringBuffer();
  // private static String  cusip    = new StringBuffer();
  // private static String  sedol    = new StringBuffer();
  private static StringBuffer  secName  = new StringBuffer();
  
  // ---
  
  private static boolean scriptMode = false;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GetStockAcct tool = new GetStockAcct ();
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
      .get();
    
    // ---
      
    Option optAcctMode = Option.builder("asm")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode for account")
      .longOpt("acct-sel-mode")
      .get();
      
    Option optAcctID = Option.builder("acct")
      .hasArg()
      .argName("UUID")
      .desc("Account-ID")
      .longOpt("account-id")
      .get();
    
    Option optAcctName = Option.builder("an")
      .hasArg()
      .argName("name")
      .desc("Account name (or part of)")
      .longOpt("account-name")
      .get();

    // ---
    
    Option optSecMode = Option.builder("ssm")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode for security")
      .longOpt("sec-sel-mode")
      .get();

    Option optSecSubMode = Option.builder("sssm")
      .hasArg()
      .argName("submode")
      .desc("Selection sub-mode for security" +
    		"(for <mode> = " + Helper.CmdtySecSingleSelMode.ID + " only)")
      .longOpt("sec-sel-sub-mode")
      .get();

    Option optSecID = Option.builder("sec")
      .hasArg()
      .argName("secid")
      .desc("Security ID (direct)" +
      		"(for <mode> = " + Helper.CmdtySecSingleSelMode.ID + " only)")
      .longOpt("security-id")
      .get();

    Option optSecExchange = Option.builder("exch")
      .hasArg()
      .argName("exch")
      .desc("Exchange code " +
    		"(Security ID indirect). " +
   		    "(for <mode> = " + Helper.CmdtySecSingleSelMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper_Sec.SecSelectSubMode.INDIRECT_EXCHANGE_TICKER + " only)")
      .longOpt("exchange")
      .get();
      
    Option optSecTicker = Option.builder("tkr")
      .hasArg()
      .argName("ticker")
      .desc("Ticker " + 
      		"(Security ID indirect). " +
   		    "(for <mode> = " + Helper.CmdtySecSingleSelMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper_Sec.SecSelectSubMode.INDIRECT_EXCHANGE_TICKER + " only)")
      .longOpt("ticker")
      .get();
    
    Option optSecMIC = Option.builder("mic")
      .hasArg()
      .argName("mic")
      .desc("MIC " +
      		"(Security ID indirect). " +
   		    "(for <mode> = " + Helper.CmdtySecSingleSelMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper_Sec.SecSelectSubMode.INDIRECT_MIC + " only)")
      .longOpt("mic")
      .get();
    	      
    Option optSecMICID = Option.builder("mid")
      .hasArg()
      .argName("micid")
      .desc("MIC-ID " +
      		"(Security ID indirect). " +
   		    "(for <mode> = " + Helper.CmdtySecSingleSelMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper_Sec.SecSelectSubMode.INDIRECT_MIC + " only)")
      .longOpt("mic-id")
      .get();

    Option optSecIDType = Option.builder("sit")
      .hasArg()
      .argName("type")
      .desc("Security ID type " + 
      		"(Security ID indirect). " +
   		    "(for <mode> = " + Helper.CmdtySecSingleSelMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper_Sec.SecSelectSubMode.INDIRECT_SEC_ID_TYPE + " only)")
      .longOpt("secid-type")
      .get();

    Option optSecISIN = Option.builder("is")
      .hasArg()
      .argName("isin")
      .desc("ISIN " + 
      		"(Security ID indirect). " +
  		   	"(for ( <mode> = " + Helper.CmdtySecSingleSelMode.ISIN + " xor " +
  		   	"( <mode> = " + Helper.CmdtySecSingleSelMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper_Sec.SecSelectSubMode.INDIRECT_SEC_ID_TYPE + " ) only)")
      .longOpt("isin")
      .get();

    Option optSecName = Option.builder("sn")
      .hasArg()
      .argName("name")
      .desc("Security name (full) " + 
  		    "(for <mode> = " + Helper.CmdtySecSingleSelMode.NAME + " only)")
      .longOpt("security-name")
      .get();

    // The convenient ones
    Option optScript = Option.builder("sl")
      .desc("Script Mode")
      .longOpt("script")
      .get();            
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optAcctMode);
    options.addOption(optAcctID);
    options.addOption(optAcctName);
    options.addOption(optSecMode);
    options.addOption(optSecSubMode);
    options.addOption(optSecID);
    options.addOption(optSecExchange);
    options.addOption(optSecTicker);
    options.addOption(optSecMIC);
    options.addOption(optSecMICID);
    options.addOption(optSecIDType);
    options.addOption(optSecISIN);
    options.addOption(optSecName);
    options.addOption(optScript);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cfg) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
	GnuCashFileExtImpl gcshFile = new GnuCashFileExtImpl(new File(gcshFileName), ! scriptMode);

    // ---

    GnuCashAccount acct = AccountHelper.getAcct(acctSelMode,
												acctID, acctName.toString(), 
												gcshFile,
												scriptMode);

    if ( ! scriptMode )
      System.out.println("Account:  " + acct.toString());
    
    // ---

    GnuCashSecurity sec = SecurityHelper.getSec(secSelMode,
												secID, isin.toString(), secName.toString(), 
												gcshFile,
												scriptMode);

    if ( ! scriptMode )
      System.out.println("Security: " + sec.toString());
    
    // ----------------------------
    
    SecuritiesAccountManager secAcctMgr = new SecuritiesAccountManager(acct);
    
    for ( GnuCashAccount chld : secAcctMgr.getShareAccts(true) ) { // ::TODO: optionally non-active accounts 
      if ( chld.getCmdtyID().toString().equals( sec.getQualifID().toString() ) ) { // important: toString()
          System.out.println(chld.getID());
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

    // <script>
    if ( cmdLine.hasOption("script") )
    {
      scriptMode = true; 
    }
    // System.err.println("Script mode: " + scriptMode);
    
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
      System.err.println("GnuCash file:     '" + gcshFileName + "'");
    
    // ----------------------------

    // <acct-sel-mode>
    try
    {
      acctSelMode = Helper.Mode.valueOf(cmdLine.getOptionValue("acct-sel-mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <acct-sel-mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("Account mode:      " + acctSelMode);
    
  	// ---------
  	
    // <sec-sel-mode>
    try
    {
      secSelMode = Helper.CmdtySecSingleSelMode.valueOf(cmdLine.getOptionValue("sec-sel-mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <sec-sel-mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("Security mode:     " + secSelMode);

    // <sec-sel-sub-mode>
    if ( cmdLine.hasOption("sec-sel-sub-mode") )
    {
        if ( secSelMode != Helper.CmdtySecSingleSelMode.ID )
        {
          System.err.println("<sec-sel-sub-mode> must only be set with <sec-sel-mode> = '" + Helper.CmdtySecSingleSelMode.ID + "'");
          throw new InvalidCommandLineArgsException();
        }
        
        try
        {
          secSelSubMode = CmdLineHelper_Sec.SecSelectSubMode.valueOf(cmdLine.getOptionValue("sec-sel-sub-mode"));
        }
        catch ( Exception exc )
        {
          System.err.println("Could not parse <sec-sel-sub-mode>");
          throw new InvalidCommandLineArgsException();
        }
    }
    else
    {
        if ( secSelMode == Helper.CmdtySecSingleSelMode.ID )
        {
          System.err.println("<sec-sel-sub-mode> must be set with <sec-sel-mode> = '" + Helper.CmdtySecSingleSelMode.ID + "'");
          throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
      System.err.println("Security sub-mode: " + secSelSubMode);
    
  	// ---------

    // <acct-sel-mode>
    // <account-id>, <acct-name>
    CmdLineHelper_Acct.parseAcctStuffWrap( cmdLine, 
    								 acctSelMode, 
    								 acctID, acctName, 
    								 scriptMode );

  	// ---------

    // <sec-sel-mode>, <sec-sel-sub-mode>,
    // <sec-id>,
    // <ticker>, <mic-id>, <isin>,
    // <sec-name>
    CmdLineHelper_Sec.parseSecStuffWrap( cmdLine, 
    								 secSelMode, secSelSubMode, null,
    								 secID, 
    								 ticker, micID, isin, 
    								 secName, 
    								 scriptMode );
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GetStockAcct", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    System.out.println("");
    System.out.println("Valid values for <acct-sel-mode>:");
    for ( Helper.Mode elt : Helper.Mode.values() )
      System.out.println(" - " + elt);

    System.out.println("");
    System.out.println("Valid values for <sec-sel-mode>:");
    for ( Helper.CmdtySecSingleSelMode elt : Helper.CmdtySecSingleSelMode.values() )
      System.out.println(" - " + elt);
    
    System.out.println("");
    System.out.println("Valid values for <sec-sel-sub-mode>:");
    for ( CmdLineHelper_Sec.SecSelectSubMode elt : CmdLineHelper_Sec.SecSelectSubMode.values() )
      System.out.println(" - " + elt);

    System.out.println("");
    System.out.println("Valid values for <exchange>:");
    for ( GCshCmdtyNameSpace.Exchange elt : GCshCmdtyNameSpace.Exchange.values() )
      System.out.println(" - " + elt);
    
    System.out.println("");
    System.out.println("Valid values for <mic>:");
    for ( GCshCmdtyNameSpace.MIC elt : GCshCmdtyNameSpace.MIC.values() )
      System.out.println(" - " + elt);
    
    System.out.println("");
    System.out.println("Valid values for <secid-type>:");
    for ( GCshCmdtyNameSpace.SecIdType elt : GCshCmdtyNameSpace.SecIdType.values() )
      System.out.println(" - " + elt);
  }
}
