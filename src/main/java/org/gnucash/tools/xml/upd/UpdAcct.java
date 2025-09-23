package org.gnucash.tools.xml.upd;

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
import org.gnucash.api.write.GnuCashWritableAccount;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.base.basetypes.complex.GCshCmdtyCurrID;
import org.gnucash.base.basetypes.simple.GCshAcctID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.AccountNotFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class UpdAcct extends CommandLineTool
{
  // Logger
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdAcct.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String gcshInFileName = null;
  private static String gcshOutFileName = null;
  
  private static GCshAcctID acctID = null;

  private static String              name = null;
  private static String              descr = null;
  private static GnuCashAccount.Type type = null;
  private static GCshCmdtyCurrID     cmdtyCurrID = null;

  private static GnuCashWritableAccount acct = null;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      UpdAcct tool = new UpdAcct ();
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
      
    Option optID = Option.builder("id")
      .required()
      .hasArg()
      .argName("UUID")
      .desc("Account ID")
      .longOpt("account-id")
      .build();
            
    Option optName = Option.builder("n")
      .hasArg()
      .argName("name")
      .desc("Account name")
      .longOpt("name")
      .build();
    
    Option optDescr = Option.builder("desc")
      .hasArg()
      .argName("descr")
      .desc("Account description")
      .longOpt("description")
      .build();
      
    Option optType = Option.builder("t")
      .hasArg()
      .argName("type")
      .desc("Account type")
      .longOpt("type")
      .build();
        
    Option optCmdtyCurr = Option.builder("cmdty")
      .hasArg()
      .argName("cmdty/curr-id")
      .desc("Commodity/currency ID")
      .longOpt("commodity-currency-id")
      .build();
      
    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optID);
    options.addOption(optName);
    options.addOption(optDescr);
    options.addOption(optType);
    options.addOption(optCmdtyCurr);
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

    try 
    {
      acct = gcshFile.getWritableAccountByID(acctID);
      System.err.println("Account before update: " + acct.toString());
    }
    catch ( Exception exc )
    {
      System.err.println("Error: Could not find/instantiate account with ID '" + acctID + "'");
      throw new AccountNotFoundException();
    }
    
    doChanges(gcshFile);
    System.err.println("Account after update: " + acct.toString());
    
    gcshFile.writeFile(new File(gcshOutFileName));
    
    System.out.println("OK");
  }

  private void doChanges(GnuCashWritableFileImpl gcshFile) throws Exception
  {
    if ( name != null )
    {
      System.err.println("Setting name");
      acct.setName(name);
    }

    if ( descr != null )
    {
      System.err.println("Setting description");
      acct.setDescription(descr);
    }

    if ( type != null )
    {
      System.err.println("Setting type");
      acct.setType(type);
    }

    if ( cmdtyCurrID != null )
    {
      System.err.println("Setting commodity/currency");
      acct.setCmdtyCurrID(cmdtyCurrID);
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
    System.err.println("GnuCash file (in): '" + gcshInFileName + "'");
    
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
    System.err.println("GnuCash file (out): '" + gcshOutFileName + "'");
    
    // <account-id>
    try
    {
      acctID = new GCshAcctID( cmdLine.getOptionValue("account-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <account-id>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Account ID: " + acctID);

    // <name>
    if ( cmdLine.hasOption("name") ) 
    {
      try
      {
        name = cmdLine.getOptionValue("name");
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <name>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("Name: '" + name + "'");

    // <description>
    if ( cmdLine.hasOption("description") ) 
    {
      try
      {
        descr = cmdLine.getOptionValue("description");
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <description>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("Description: '" + descr + "'");
    
    // <type>
    if ( cmdLine.hasOption("type") ) 
    {
      try
      {
        type = GnuCashAccount.Type.valueOf( cmdLine.getOptionValue("type") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <type>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("Type: '" + type + "'");

    // <commodity-currency-id>
    if ( cmdLine.hasOption("commodity-currency-id") ) 
    {
      try
      {
        cmdtyCurrID = GCshCmdtyCurrID.parse( cmdLine.getOptionValue("commodity-currency-id") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <commodity-currency-id>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("Cmdty/Curr: '" + cmdtyCurrID + "'");
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "UpdAcct", options );
    
    System.out.println("");
    System.out.println("Valid values for <type>:");
    for ( GnuCashAccount.Type elt : GnuCashAccount.Type.values() )
      System.out.println(" - " + elt);
  }
}
