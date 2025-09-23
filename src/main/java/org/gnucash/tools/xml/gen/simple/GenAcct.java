package org.gnucash.tools.xml.gen.simple;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashAccount;
import org.gnucash.api.read.GnuCashFile;
import org.gnucash.api.write.GnuCashWritableAccount;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.base.basetypes.complex.GCshCmdtyCurrID;
import org.gnucash.base.basetypes.simple.GCshAcctID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GenAcct extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
private static final Logger LOGGER = LoggerFactory.getLogger(GenAcct.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String gcshInFileName = null;
  private static String gcshOutFileName = null;
  
  private static String               name        = null;
  private static GnuCashAccount.Type  type        = null;
  private static GCshCmdtyCurrID      cmdtyCurrID = null;
  private static GCshAcctID           parentID    = null;
  
  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GenAcct tool = new GenAcct ();
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
    // invcID = UUID.randomUUID();

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
      .build();
          
    Option optFileOut = Option.builder("of")
      .required()
      .hasArg()
      .argName("file")
      .desc("GnuCash file (out)")
      .longOpt("gnucash-out-file")
      .build();
      
    Option optName = Option.builder("n")
      .required()
      .hasArg()
      .argName("name")
      .desc("Account name")
      .longOpt("name")
      .build();
    
    Option optType = Option.builder("t")
      .required()
      .hasArg()
      .argName("type")
      .desc("Account type")
      .longOpt("type")
      .build();
    	    
    Option optCmdtyCurr = Option.builder("sc")
      .required()
      .hasArg()
      .argName("cmdty/curr")
      .desc("Account currency: a (qualified) commodity or a currency ID")
      .longOpt("commodity-currency")
      .build();
    	    
    Option optParent = Option.builder("p")
      .required()
      .hasArg()
      .argName("acctid")
      .desc("Parent account ID")
      .longOpt("parent")
      .build();
    	    
    // The convenient ones
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optName);
    options.addOption(optType);
    options.addOption(optCmdtyCurr);
    options.addOption(optParent);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cs) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    GnuCashWritableFileImpl gcshFile = new GnuCashWritableFileImpl(new File(gcshInFileName));

    if ( ! isPlausiCheckOK(gcshFile) ) {
    	System.err.println("Did not pass plausi checks");
    	throw new Exception();
    }
    
    GnuCashWritableAccount acct = gcshFile.createWritableAccount(type, cmdtyCurrID, parentID, name);
    
    System.out.println("Account to write: " + acct.toString());
    gcshFile.writeFile(new File(gcshOutFileName));
    System.out.println("OK");
  }
  
  private boolean isPlausiCheckOK(final GnuCashFile gcshFile) {
	    GnuCashAccount parentAcct = gcshFile.getAccountByID( parentID );
	    if ( type == GnuCashAccount.Type.STOCK &&
	    	 parentAcct.getType() != GnuCashAccount.Type.ASSET ) {
	    	System.err.println("Error: <type> = " + type + ", but parent's type is not " + GnuCashAccount.Type.ASSET);
	    	return false;
	    } else if ( ( type == GnuCashAccount.Type.BANK ||
	    			  type == GnuCashAccount.Type.CREDIT ||
	    			  type == GnuCashAccount.Type.MUTUAL ||
	    			  type == GnuCashAccount.Type.CASH ||
	    			  type == GnuCashAccount.Type.RECEIVABLE ) &&
	       	        parentAcct.getType() != GnuCashAccount.Type.ASSET ) {
	    	System.err.println("Error: <type> = " + type + ", but parent's type is not " + GnuCashAccount.Type.ASSET);
	    	return false;
	    } else if ( ( type == GnuCashAccount.Type.PAYABLE ) &&
	   	            parentAcct.getType() != GnuCashAccount.Type.LIABILITY ) {
	    	System.err.println("Error: <type> = " + type + ", but parent's type is not " + GnuCashAccount.Type.LIABILITY);
	    	return false;
	    } else if ( ( type == GnuCashAccount.Type.ASSET ||
	    		      type == GnuCashAccount.Type.LIABILITY ||
	    		      type == GnuCashAccount.Type.INCOME ||
	    		      type == GnuCashAccount.Type.EXPENSE ||
	    		      type == GnuCashAccount.Type.EQUITY ) &&
		            type != parentAcct.getType() ) {
	    	System.err.println("Error: <type> = " + type + ", but parent's type is not");
	    	return false;
	    }
	    
	    return true;
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
    System.err.println("GnuCash file (in):          '" + gcshInFileName + "'");
    
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
    System.err.println("GnuCash file (out):         '" + gcshOutFileName + "'");
    
    // <name>
    try
    {
      name = cmdLine.getOptionValue("name");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <name>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Name:                        '" + name + "'");
    
    // <type>
    try
    {
      type = GnuCashAccount.Type.valueOf( cmdLine.getOptionValue("type") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <type>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Type:                        " + type);
    
    // <commodity-currency>
    try
    {
      cmdtyCurrID = GCshCmdtyCurrID.parse( cmdLine.getOptionValue("commodity-currency") );
      if ( ( cmdtyCurrID.getType() == GCshCmdtyCurrID.Type.SECURITY_EXCHANGE ||
    		 cmdtyCurrID.getType() == GCshCmdtyCurrID.Type.SECURITY_MIC ||
    		 cmdtyCurrID.getType() == GCshCmdtyCurrID.Type.SECURITY_SECIDTYPE ||
    		 cmdtyCurrID.getType() == GCshCmdtyCurrID.Type.SECURITY_GENERAL ) &&
    	   type != GnuCashAccount.Type.STOCK ) {
          System.err.println("<commodity-currency> may be set to a security only if <type> = " + GnuCashAccount.Type.STOCK + "");
          throw new InvalidCommandLineArgsException();
      } else if ( cmdtyCurrID.getType() == GCshCmdtyCurrID.Type.CURRENCY &&
    		      type == GnuCashAccount.Type.STOCK ) {
          System.err.println("<commodity-currency> may be set to a (real) currency only if <type> != " + GnuCashAccount.Type.STOCK + "");
          throw new InvalidCommandLineArgsException();
      }
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <security-currency>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Account currency (cmdty/curr): " + cmdtyCurrID);
    
    // <parent>
    try
    {
      parentID = new GCshAcctID( cmdLine.getOptionValue("parent") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <name>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Parent account ID:           " + parentID);
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GenAcct", options );
  }
}
