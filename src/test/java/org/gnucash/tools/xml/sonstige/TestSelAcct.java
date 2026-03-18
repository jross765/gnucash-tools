package org.gnucash.tools.xml.sonstige;

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
import org.gnucash.apispec.read.impl.GnuCashFileExtImpl;
import org.gnucash.base.basetypes.simple.GCshAcctID;
import org.gnucash.tools.CommandLineTool;
import org.gnucash.tools.xml.get.list.Helper;
import org.gnucash.tools.xml.helper.AccountHelper;
import org.gnucash.tools.xml.helper.CmdLineHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class TestSelAcct extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(TestSelAcct.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String  gcshFileName = null;
  
  private static Helper.Mode acctSelMode = null;

  // CAUTION: As opposed to most other tools, the following variables
  // have to be instantiated here.
  
  private static GCshAcctID    acctID   = new GCshAcctID();
  // This following: sic, StringBuffer, not String,
  // for it has to be mutable because of the way the args are parsed.
  private static StringBuffer  acctName = new StringBuffer();
  
  private static boolean scriptMode = false;
  
  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      TestSelAcct tool = new TestSelAcct ();
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

    Option optAcctName = Option.builder("n")
      .hasArg()
      .argName("name")
      .desc("Account name (or part of)")
      .longOpt("account-name")
      .get();

    // The convenient ones
    // ::EMPTY

    options = new Options();
    options.addOption(optFile);
    options.addOption(optAcctMode);
    options.addOption(optAcctID);
    options.addOption(optAcctName);
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

    GnuCashAccount acct = AccountHelper.getAcct(acctSelMode, 
    											acctID, acctName.toString(), 
    											gcshFile,
    											scriptMode);

    System.out.println("Selected account: " + acct.toString());
  }

  // -----------------------------------------------------------------

  @Override
  protected void parseCommandLineArgs(String[] args)
      throws InvalidCommandLineArgsException
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
      System.err.println("GnuCash file: '" + gcshFileName + "'");

  	// ---------
  	
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
    CmdLineHelper.parseAcctStuffWrap( cmdLine, 
    								 acctSelMode, 
    								 acctID, acctName, 
    								 scriptMode );
  }

  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "TestSelAcct", "", options, "", true );
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
