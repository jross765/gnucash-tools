package org.gnucash.tools.xml.upd;

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
import org.gnucash.apispec.write.GnuCashWritableSecurity;
import org.gnucash.apispec.write.impl.GnuCashWritableFileExtImpl;
import org.gnucash.base.basetypes.complex.GCshCmdtyNameSpace;
import org.gnucash.base.basetypes.complex.GCshSecID;
import org.gnucash.tools.CommandLineTool;
import org.gnucash.tools.xml.get.list.Helper;
import org.gnucash.tools.xml.helper.CmdLineHelper_Sec;
import org.gnucash.tools.xml.helper.SecurityHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class UpdSec extends CommandLineTool
{
  // Logger
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdSec.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String gcshInFileName  = null;
  private static String gcshOutFileName = null;
  
  private static Helper.CmdtySecSingleSelMode secSelMode = null;
  private static CmdLineHelper_Sec.SecSelectSubMode secSelSubMode = null;

  // CAUTION: As opposed to most other tools, the following variables
  // have to be instantiated here.
  
  private static GCshSecID     secID    = new GCshSecID();
  // This one and the following: sic, StringBuffer, not String,
  // for it has to be mutable because of the way the args are parsed.
  private static StringBuffer  ticker   = new StringBuffer();
  private static StringBuffer  micID    = new StringBuffer();
  private static StringBuffer  isin     = new StringBuffer();
  // Possibly later:
  // private static StringBuffer  wkn      = new StringBuffer();
  // private static StringBuffer  cusip    = new StringBuffer();
  // private static StringBuffer  sedol    = new StringBuffer();
  // private static StringBuffer  secName  = new StringBuffer(); // <-- NOT for selection

  private static String          newName  = null;

  private static GnuCashWritableSecurity sec = null;

  private static boolean scriptMode = false; // ::TODO

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      UpdSec tool = new UpdSec ();
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

    Option optMode = Option.builder("ssm")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode for security")
      .longOpt("sec-sel-mode")
      .get();

    Option optSubMode = Option.builder("sssm")
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

    Option optExchange = Option.builder("exch")
      .hasArg()
      .argName("exch")
      .desc("Exchange code " +
    		"(Security ID indirect). " +
   		    "(for <mode> = " + Helper.CmdtySecSingleSelMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper_Sec.SecSelectSubMode.INDIRECT_EXCHANGE_TICKER + " only)")
      .longOpt("exchange")
      .get();

    Option optTicker = Option.builder("tkr")
      .hasArg()
      .argName("ticker")
      .desc("Ticker " + 
      		"(Security ID indirect). " +
   		    "(for <mode> = " + Helper.CmdtySecSingleSelMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper_Sec.SecSelectSubMode.INDIRECT_EXCHANGE_TICKER + " only)")
      .longOpt("ticker")
      .get();
    
    Option optMIC = Option.builder("mic")
      .hasArg()
      .argName("mic")
      .desc("MIC " +
      		"(Security ID indirect). " +
   		    "(for <mode> = " + Helper.CmdtySecSingleSelMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper_Sec.SecSelectSubMode.INDIRECT_MIC + " only)")
      .longOpt("mic")
      .get();
    	      
    Option optMICID = Option.builder("mid")
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

    Option optISIN = Option.builder("is")
      .hasArg()
      .argName("isin")
      .desc("ISIN " + 
      		"(Security ID indirect). " +
  		   	"(for <mode> = " + Helper.CmdtySecSingleSelMode.ISIN + " xor " +
  		   	"( <mode> = " + Helper.CmdtySecSingleSelMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper_Sec.SecSelectSubMode.INDIRECT_SEC_ID_TYPE + " ) only)")
      .longOpt("isin")
      .get();

    // ---
    
    Option optNewName = Option.builder("nam")
      .hasArg()
      .argName("name")
      .desc("Security name (new)") // <-- !
      .longOpt("new-name")
      .get();
   
    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optMode);
    options.addOption(optSubMode);
    options.addOption(optSecID);
    options.addOption(optExchange);
    options.addOption(optTicker);
    options.addOption(optMIC);
    options.addOption(optMICID);
    options.addOption(optSecIDType);
    options.addOption(optISIN);
    options.addOption(optNewName);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cfg) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    GnuCashWritableFileExtImpl gcshFile = new GnuCashWritableFileExtImpl(new File(gcshInFileName), true);

    sec = SecurityHelper.getWrtSec(secSelMode,
    							secID, isin.toString(), null, // <-- sic, not by name 
    							gcshFile,
    							scriptMode);
    System.err.println("Security before update: " + sec.toString());
    
	// ----------------------------
    
    doChanges();
    System.err.println("Security after update: " + sec.toString());
    
    gcshFile.writeFile(new File(gcshOutFileName));
    
    System.out.println("OK");
  }

  private void doChanges() throws Exception
  {
    if ( newName != null )
    {
      System.err.println("Setting name");
      sec.setName(newName);
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
    
    if ( ! scriptMode )
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

    if ( ! scriptMode )
    	System.err.println("GnuCash file (out): '" + gcshOutFileName + "'");

  	// ---------
  	
    // <sec-sel-mode>
    try
    {
      secSelMode = Helper.CmdtySecSingleSelMode.valueOf(cmdLine.getOptionValue("sec-sel-mode"));
      
      if ( secSelMode == Helper.CmdtySecSingleSelMode.NAME )
      {
        System.err.println("<sec-sel-mode> '" + Helper.CmdtySecSingleSelMode.NAME + "' must not be used here");
        throw new InvalidCommandLineArgsException();
      }
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <sec-sel-mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("Security mode:         " + secSelMode);

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
      System.err.println("Security sub-mode:     " + secSelSubMode);
    
  	// ---------

    // <sec-sel-mode>, <sec-sel-sub-mode>,
    // <exchange>, <ticker>,
    // <mid>, <mic-id>,
    // <secid-type>, <isin>
    // NOT NAME!
    CmdLineHelper_Sec.parseSecStuffWrap( cmdLine, 
    								 secSelMode, secSelSubMode, null,
    								 secID, 
    								 ticker, micID, isin, 
    								 null, // <-- !
    								 scriptMode );

  	// ---------

    // <name>
    if ( cmdLine.hasOption("new-name") )
    {
      try
      {
        newName = cmdLine.getOptionValue("new-name").trim();
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <new-name>");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("New name:     '" + newName + "'");
  }

  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "UpdSec", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
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
