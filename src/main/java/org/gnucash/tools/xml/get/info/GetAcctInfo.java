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
import org.gnucash.api.read.GnuCashAccount;
import org.gnucash.api.read.GnuCashTransaction;
import org.gnucash.api.read.aux.GCshAcctReconInfo;
import org.gnucash.api.read.impl.GnuCashFileImpl;
import org.gnucash.base.basetypes.simple.GCshAcctID;
import org.gnucash.tools.CommandLineTool;
import org.gnucash.tools.xml.helper.AccountHelper;
import org.gnucash.tools.xml.helper.CmdLineHelper_Acct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetAcctInfo extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetAcctInfo.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String  gcshFileName = null;

  private static Helper.Mode acctSelMode = null;

  // CAUTION: As opposed to most other tools, the following variables
  // have to be instantiated here.
  
  private static GCshAcctID    acctID   = new GCshAcctID();
  // The following: sic, StringBuffer, not String,
  // for it has to be mutable because of the way the args are parsed.
  private static StringBuffer  acctName = new StringBuffer();
  
  private static boolean showParents  = false;
  private static boolean showChildren = false;
  private static boolean showTrx      = false;
  private static boolean showRcn      = false;
  
  private static boolean scriptMode = false; // ::TODO

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GetAcctInfo tool = new GetAcctInfo ();
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

    // The convenient ones
    Option optShowPrnt = Option.builder("sprnt")
      .desc("Show parents")
      .longOpt("show-parents")
      .get();
        
    Option optShowChld = Option.builder("schld")
      .desc("Show children")
      .longOpt("show-children")
      .get();
          
    Option optShowTrx = Option.builder("strx")
      .desc("Show transactions")
      .longOpt("show-transactions")
      .get();
          
    Option optShowRcn = Option.builder("srcn")
      .desc("Show reconciliation info")
      .longOpt("show-recon-info")
      .get();

    options = new Options();
    options.addOption(optFile);
    options.addOption(optAcctMode);
    options.addOption(optAcctID);
    options.addOption(optAcctName);
    options.addOption(optShowPrnt);
    options.addOption(optShowChld);
    options.addOption(optShowTrx);
    options.addOption(optShowRcn);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cfg) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    GnuCashFileImpl gcshFile = new GnuCashFileImpl(new File(gcshFileName), ! scriptMode);

    GnuCashAccount acct = AccountHelper.getAcct(acctSelMode, 
    											acctID, acctName.toString(), 
    											gcshFile,
    											scriptMode);

    printAcctInfo(acct, 0);
  }

  private void printAcctInfo(GnuCashAccount acct, int depth)
  {
    System.out.println("Depth:           " + depth);

    try
    {
      System.out.println("ID:              " + acct.getID());
    }
    catch ( Exception exc )
    {
      System.out.println("ID:              " + "ERROR");
    }
    
    try
    {
      System.out.println("toString:        " + acct.toString());
    }
    catch ( Exception exc )
    {
      System.out.println("toString:        " + "ERROR");
    }
    
    try
    {
      System.out.println("Type:            " + acct.getType());
    }
    catch ( Exception exc )
    {
      System.out.println("Type:            " + "ERROR");
    }
    
    try
    {
      System.out.println("Name:            '" + acct.getName() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Name:            " + "ERROR");
    }
    
    try
    {
      System.out.println("Qualified name:  '" + acct.getQualifiedName() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Qualified name:  " + "ERROR");
    }
    
    try
    {
      System.out.println("Parent ID:       " + acct.getParentAccountID());
    }
    catch ( Exception exc )
    {
      System.out.println("Parent ID:       " + "ERROR");
    }
    
    try
    {
      System.out.println("Hidden:          " + acct.isHidden());
    }
    catch ( Exception exc )
    {
      System.out.println("Hidden:          " + "ERROR");
    }
    
    try
    {
      System.out.println("Description:     '" + acct.getDescription() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Description:     " + "ERROR");
    }
    
    try
    {
      System.out.println("Sec/Curr:        '" + acct.getCmdtyID() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Sec/Curr:        " + "ERROR");
    }
    
    System.out.println("");
    try
    {
      System.out.println("Balance:         " + acct.getBalance());
    }
    catch ( Exception exc )
    {
      System.out.println("Balance:         " + "ERROR");
    }

    try
    {
      System.out.println("Balance (exact): " + acct.getBalanceRat());
    }
    catch ( Exception exc )
    {
      System.out.println("Balance (exact): " + "ERROR");
    }

    try
    {
      System.out.println("Balance (fmt):   " + acct.getBalanceFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Balance (fmt):   " + "ERROR");
    }

    System.out.println("");
    try
    {
      System.out.println("Balance recurs.:         " + acct.getBalanceRecursive());
    }
    catch ( Exception exc )
    {
      System.out.println("Balance recurs.: " + "ERROR");
    }

    try
    {
      System.out.println("Balance recurs. (exact): " + acct.getBalanceRecursiveRat());
    }
    catch ( Exception exc )
    {
      System.out.println("Balance recurs. (exact): " + "ERROR");
    }

    try
    {
      System.out.println("Balance recurs. (fmt):   " + acct.getBalanceRecursiveFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Balance recurs. (fmt):   " + "ERROR");
    }

    // ---
        
    if ( showParents )
      showParents(acct, depth);
    
    if ( showChildren )
      showChildren(acct, depth);
    
    if ( showTrx )
      showTransactions(acct);
    
    if ( showRcn )
      showReconInfo(acct);
  }

  // -----------------------------------------------------------------

  private void showParents(GnuCashAccount acct, int depth)
  {
    if ( depth <= 0 &&
         acct.getType() != GnuCashAccount.Type.ROOT )
    {
      System.out.println("");
      System.out.println(">>> BEGIN Parent Account");
      printAcctInfo(acct.getParentAccount(), depth - 1);
      System.out.println("<<< END Parent Account");
    }
  }
  
  private void showChildren(GnuCashAccount acct, int depth)
  {
    System.out.println("");
    System.out.println("Children (1st Level, Overview):");
    
    for ( GnuCashAccount chld : acct.getChildren() )
    {
        System.out.println(" - " + chld.toString());
    }
    
    System.out.println("");
    System.out.println("Children (Recursive, Details):");
    
    if ( depth >= 0 )
    {
      System.out.println(">>> BEGIN Child Account");
      for ( GnuCashAccount childAcct : acct.getChildren())
      {
        printAcctInfo(childAcct, depth + 1);
      }
      System.out.println("<<< END Child Account");
    }
  }
  
  private void showTransactions(GnuCashAccount acct)
  {
    System.out.println("");
    System.out.println("Transactions:");
    
    for ( GnuCashTransaction trx : acct.getTransactions() )
    {
      System.out.println(" - " + trx.toString());
    }
  }

  private void showReconInfo(GnuCashAccount acct)
  {
    System.out.println("");
    System.out.println("Reconciliation Info:");
    
    GCshAcctReconInfo rcnInfo = acct.getReconcileInfo();
    System.out.println(rcnInfo.toString());
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
      System.err.println("GnuCash file:  '" + gcshFileName + "'");
    
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
      System.err.println("Account mode:  " + acctSelMode);

  	// ---------

    // <acct-sel-mode>
    // <account-id>, <acct-name>
    CmdLineHelper_Acct.parseAcctStuffWrap( cmdLine, 
    								 acctSelMode, 
    								 acctID, acctName, 
    								 scriptMode );

    // <show-parents>
    if ( cmdLine.hasOption("show-parents"))
    {
      showParents = true;
    }
    else
    {
      showParents = false;
    }
    
    if ( ! scriptMode )
      System.err.println("Show parents:             " + showParents);

    // <show-children>
    if ( cmdLine.hasOption("show-children"))
    {
      showChildren = true;
    }
    else
    {
      showChildren = false;
    }
    
    if ( ! scriptMode )
      System.err.println("Show children:            " + showChildren);

    // <show-transactions>
    if ( cmdLine.hasOption("show-transactions"))
    {
      showTrx = true;
    }
    else
    {
      showTrx = false;
    }
    
    if ( ! scriptMode )
      System.err.println("Show transactions:        " + showTrx);

    // <show-recon-info>
    if ( cmdLine.hasOption("show-recon-info"))
    {
      showRcn = true;
    }
    else
    {
      showRcn = false;
    }
    
    if ( ! scriptMode )
      System.err.println("Show reconciliation info: " + showRcn);
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GetAcctInfo", "", options, "", true );
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
  }
}
