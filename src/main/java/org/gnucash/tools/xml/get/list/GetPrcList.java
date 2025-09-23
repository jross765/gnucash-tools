package org.gnucash.tools.xml.get.list;

import java.io.File;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashCommodity;
import org.gnucash.api.read.GnuCashPrice;
import org.gnucash.api.read.impl.GnuCashFileImpl;
import org.gnucash.base.basetypes.complex.GCshCmdtyCurrID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetPrcList extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetPrcList.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String                gcshFileName    = null;
  private static Helper.CmdtySecMode   cmdtyMode       = null;
  private static GCshCmdtyCurrID       fromCmdtyCurrID = null;
  private static String                fromCmdtyIsin   = null;
  private static String                fromCmdtyName   = null;
  
  private static boolean scriptMode = false; // ::TODO

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GetPrcList tool = new GetPrcList ();
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
      
    Option optMode = Option.builder("m")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Commodity/currency selection mode")
      .longOpt("mode")
      .build();
    	    	        
    Option optFromCmdtyCurr= Option.builder("fr")
      .hasArg()
      .argName("cmdty/curr")
      .desc("From-commodity/currency")
      .longOpt("from-cmdty-curr")
      .build();
    	    	          
    Option optFromISIN = Option.builder("is")
      .hasArg()
      .argName("isin")
      .desc("From-commodity/currency ISIN")
      .longOpt("isin")
      .build();
    	        
    Option optFromName = Option.builder("fn")
      .hasArg()
      .argName("name")
      .desc("From-commodity/currency Name (or part of)")
      .longOpt("name")
      .build();
    	          
    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    options.addOption(optFromCmdtyCurr);
    options.addOption(optFromISIN);
    options.addOption(optFromName);
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
    
    if ( cmdtyMode == Helper.CmdtySecMode.ISIN )
    {
        GnuCashCommodity cmdty = gcshFile.getCommodityByXCode(fromCmdtyIsin);
    	fromCmdtyCurrID = cmdty.getQualifID();
    }
    else if ( cmdtyMode == Helper.CmdtySecMode.NAME )
    {
    	GnuCashCommodity cmdty = gcshFile.getCommodityByNameUniq(fromCmdtyName);
    	fromCmdtyCurrID = cmdty.getQualifID();
    }
    
    List<GnuCashPrice> prcList = gcshFile.getPricesByCmdtyCurrID( fromCmdtyCurrID );
    if ( prcList.size() == 0 ) 
    {
    	System.err.println("Found no price with for that commodity/currency ID.");
    	throw new NoEntryFoundException();
    }

	System.err.println("Found " + prcList.size() + " price(s).");
    for ( GnuCashPrice prc : prcList )
    {
    	System.out.println(prc.toString());	
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
    
    // <mode>
    try
    {
      cmdtyMode = Helper.CmdtySecMode.valueOf(cmdLine.getOptionValue("mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <mode>");
      throw new InvalidCommandLineArgsException();
    }

    // <from-cmdty-curr>
    if ( cmdLine.hasOption("from-cmdty-curr") )
    {
      if ( cmdtyMode != Helper.CmdtySecMode.ID )
      {
        System.err.println("<from-cmdty-curr> must only be set with <mode> = '" + Helper.CmdtySecMode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
          fromCmdtyCurrID = GCshCmdtyCurrID.parse(cmdLine.getOptionValue("from-cmdty-curr")); 
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <from-cmdty-curr>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( cmdtyMode == Helper.CmdtySecMode.ID )
      {
        System.err.println("<from-cmdty-curr> must be set with <mode> = '" + Helper.CmdtySecMode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("From-commodity/currency ID:   '" + fromCmdtyCurrID + "'");

    // <isin>
    if ( cmdLine.hasOption("isin") )
    {
      if ( cmdtyMode != Helper.CmdtySecMode.ISIN )
      {
        System.err.println("<isin> must only be set with <mode> = '" + Helper.CmdtySecMode.ISIN.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
    	  fromCmdtyIsin = cmdLine.getOptionValue("isin");
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
        System.err.println("<isin> must be set with <mode> = '" + Helper.CmdtySecMode.ISIN.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("From-commodity/currency ISIN: '" + fromCmdtyIsin + "'");

    // <name>
    if ( cmdLine.hasOption("name") )
    {
      if ( cmdtyMode != Helper.CmdtySecMode.NAME )
      {
        System.err.println("<name> must only be set with <mode> = '" + Helper.CmdtySecMode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
    	  fromCmdtyName = cmdLine.getOptionValue("name");
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <name>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( cmdtyMode == Helper.CmdtySecMode.NAME )
      {
        System.err.println("<name> must be set with <mode> = '" + Helper.CmdtySecMode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("From-commodity/currency name: '" + fromCmdtyName + "'");
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GetPrcList", options );
    
    System.out.println("");
    System.out.println("Valid values for <mode>:");
    for ( Helper.CmdtySecMode elt : Helper.CmdtySecMode.values() )
      System.out.println(" - " + elt);
  }
}
