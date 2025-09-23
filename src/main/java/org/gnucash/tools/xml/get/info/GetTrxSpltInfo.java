package org.gnucash.tools.xml.get.info;

import java.io.File;

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
import org.gnucash.base.basetypes.simple.GCshSpltID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetTrxSpltInfo extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetTrxSpltInfo.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String     gcshFileName = null;
  private static GCshSpltID spltID = null;
  
  private static boolean scriptMode = false; // ::TODO

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GetTrxSpltInfo tool = new GetTrxSpltInfo ();
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
    // trxID = UUID.randomUUID();

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
      
    Option optSpltID = Option.builder("splt")
      .required()
      .hasArg()
      .argName("UUID")
      .desc("Transaction-split-ID")
      .longOpt("split-id")
      .build();
    
    // The convenient ones
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optSpltID);
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
    
    GnuCashTransactionSplit splt = gcshFile.getTransactionSplitByID(spltID);
    
    try
    {
      System.out.println("ID:          " + splt.getID());
    }
    catch ( Exception exc )
    {
      System.out.println("ID:          " + "ERROR");
    }
    
    try
    {
      System.out.println("toString:    " + splt.toString());
    }
    catch ( Exception exc )
    {
      System.out.println("toString:    " + "ERROR");
    }
    
    try
    {
      System.out.println("Account ID:  " + splt.getAccountID());
    }
    catch ( Exception exc )
    {
      System.out.println("Account ID:  " + "ERROR");
    }
    
    try
    {
      System.out.println("Lot:         " + splt.getLotID());
    }
    catch ( Exception exc )
    {
      System.out.println("Lot:         " + "ERROR");
    }
        
    try
    {
      System.out.println("Action (code): " + splt.getAction());
    }
    catch (Exception exc)
    {
      System.out.println("Action (code): " + "ERROR");
    }

    try
    {
      System.out.println("Action (str): " + splt.getActionStr());
    }
    catch (Exception exc)
    {
      System.out.println("Action (str): " + "ERROR");
    }

    try
    {
      System.out.println("Value:       " + splt.getValueFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Value:       " + "ERROR");
    }
        
    try
    {
      System.out.println("Quantity:    " + splt.getQuantityFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Quantity:    " + "ERROR");
    }
        
    try
    {
      System.out.println("Description: '" + splt.getDescription() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Description: " + "ERROR");
    }
  }

  // -----------------------------------------------------------------

  @SuppressWarnings("unused")
private void showLots(GnuCashTransaction trx)
  {
    System.out.println("");
    System.out.println("Splits:");
    
    for ( GnuCashTransactionSplit splt : trx.getSplits() )
    {
      System.out.println(" - " + splt.toString());
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
      System.err.println("GnuCash file: '" + gcshFileName + "'");
    
    // <split-id>
    try
    {
      spltID = new GCshSpltID( cmdLine.getOptionValue("split-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <split-id>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("Split ID: " + spltID);
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GetTrxSpltInfo", options );
  }
}
