package org.gnucash.tools.xml.get.list;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashPrice;
import org.gnucash.apispec.read.GnuCashSecurity;
import org.gnucash.apispec.read.impl.GnuCashFileExtImpl;
import org.gnucash.base.basetypes.complex.GCshCmdtyID;
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
  private static Helper.CmdtySecSingleSelMode   secMode = null;
  private static GCshCmdtyID           fromSecCurrID = null;
  private static String                fromSecIsin   = null;
  private static String                fromSecName   = null;
  
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
      .get();

    Option optMode = Option.builder("m")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Security/currency selection mode")
      .longOpt("mode")
      .get();

    Option optFromSecCurr= Option.builder("fr")
      .hasArg()
      .argName("qualifiD")
      .desc("From-security/currency qualified ID")
      .longOpt("from-sec-curr")
      .get();

    Option optFromISIN = Option.builder("is")
      .hasArg()
      .argName("isin")
      .desc("From-security/currency ISIN")
      .longOpt("isin")
      .get();

    Option optFromName = Option.builder("fn")
      .hasArg()
      .argName("name")
      .desc("From-security/currency Name (or part of)")
      .longOpt("name")
      .get();

    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    options.addOption(optFromSecCurr);
    options.addOption(optFromISIN);
    options.addOption(optFromName);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cfg) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    GnuCashFileExtImpl gcshFile = new GnuCashFileExtImpl(new File(gcshFileName), true);
    
    if ( secMode == Helper.CmdtySecSingleSelMode.ISIN )
    {
        GnuCashSecurity sec = gcshFile.getSecurityByXCode(fromSecIsin);
    	fromSecCurrID = sec.getQualifID();
    }
    else if ( secMode == Helper.CmdtySecSingleSelMode.NAME )
    {
    	GnuCashSecurity sec = gcshFile.getSecurityByNameUniq(fromSecName);
    	fromSecCurrID = sec.getQualifID();
    }
    
    List<GnuCashPrice> prcList = gcshFile.getPricesByCmdtyID( fromSecCurrID );
    if ( prcList.size() == 0 ) 
    {
    	System.err.println("Found no price with for that security/currency ID.");
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
      secMode = Helper.CmdtySecSingleSelMode.valueOf(cmdLine.getOptionValue("mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <mode>");
      throw new InvalidCommandLineArgsException();
    }

    // <from-sec-curr>
    if ( cmdLine.hasOption("from-sec-curr") )
    {
      if ( secMode != Helper.CmdtySecSingleSelMode.ID )
      {
        System.err.println("<from-sec-curr> must only be set with <mode> = '" + Helper.CmdtySecSingleSelMode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
          fromSecCurrID = GCshCmdtyID.parse(cmdLine.getOptionValue("from-sec-curr")); 
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <from-sec-curr>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( secMode == Helper.CmdtySecSingleSelMode.ID )
      {
        System.err.println("<from-sec-curr> must be set with <mode> = '" + Helper.CmdtySecSingleSelMode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("From-security/currency ID:   '" + fromSecCurrID + "'");

    // <isin>
    if ( cmdLine.hasOption("isin") )
    {
      if ( secMode != Helper.CmdtySecSingleSelMode.ISIN )
      {
        System.err.println("<isin> must only be set with <mode> = '" + Helper.CmdtySecSingleSelMode.ISIN.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
    	  fromSecIsin = cmdLine.getOptionValue("isin");
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
        System.err.println("<isin> must be set with <mode> = '" + Helper.CmdtySecSingleSelMode.ISIN.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("From-security/currency ISIN: '" + fromSecIsin + "'");

    // <name>
    if ( cmdLine.hasOption("name") )
    {
      if ( secMode != Helper.CmdtySecSingleSelMode.NAME )
      {
        System.err.println("<name> must only be set with <mode> = '" + Helper.CmdtySecSingleSelMode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
    	  fromSecName = cmdLine.getOptionValue("name");
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <name>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( secMode == Helper.CmdtySecSingleSelMode.NAME )
      {
        System.err.println("<name> must be set with <mode> = '" + Helper.CmdtySecSingleSelMode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("From-security/currency name: '" + fromSecName + "'");
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GetPrcList", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    System.out.println("");
    System.out.println("Valid values for <mode>:");
    for ( Helper.CmdtySecSingleSelMode elt : Helper.CmdtySecSingleSelMode.values() )
      System.out.println(" - " + elt);
  }
}
