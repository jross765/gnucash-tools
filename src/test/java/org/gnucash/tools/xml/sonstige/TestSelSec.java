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
import org.gnucash.apispec.read.GnuCashSecurity;
import org.gnucash.apispec.read.impl.GnuCashFileExtImpl;
import org.gnucash.base.basetypes.complex.GCshCmdtyNameSpace;
import org.gnucash.base.basetypes.complex.GCshSecID;
import org.gnucash.tools.CommandLineTool;
import org.gnucash.tools.xml.helper.CmdLineHelper;
import org.gnucash.tools.xml.helper.EnumSecSelectSubMode;
import org.gnucash.tools.xml.helper.EnumSecSingleSelMode;
import org.gnucash.tools.xml.helper.SecurityHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class TestSelSec extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(TestSelSec.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String  gcshFileName = null;
  
  private static EnumSecSingleSelMode mode = new EnumSecSingleSelMode();
  private static EnumSecSelectSubMode subMode = new EnumSecSelectSubMode();

  // CAUTION: As opposed to most other tools, the following variables
  // have to be instantiated here.
  
  private static GCshSecID secID = new GCshSecID();
  
  // This one and the following: sic, StringBuffer, not String,
  // for it has to be mutable because of the way the args are parsed.
  private static StringBuffer  ticker   = new StringBuffer();
  private static StringBuffer  micID    = new StringBuffer();
  private static StringBuffer  isin     = new StringBuffer();
  // Possibly later:
  // private static StringBuffer  wkn      = new StringBuffer();
  // private static StringBuffer  cusip    = new StringBuffer();
  // private static StringBuffer  sedol    = new StringBuffer();
  
  private static StringBuffer  secName     = new StringBuffer();
  
  private static boolean scriptMode = false;
  
  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      TestSelSec tool = new TestSelSec ();
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
      .desc("Selection mode")
      .longOpt("mode")
      .get();
        
    Option optSubMode = Option.builder("sm")
      .hasArg()
      .argName("submode")
      .desc("Selection sub-mode " +
    		"(for <mode> = " + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID + " only)")
      .longOpt("sub-mode")
      .get();
    	        
    Option optExchange = Option.builder("exch")
      .hasArg()
      .argName("exch")
      .desc("Exchange code " +
   		    "(for <mode> = " + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper.SecSelectSubMode.EXCHANGE_TICKER + " only)")
      .longOpt("exchange")
      .get();
      
    Option optTicker = Option.builder("tkr")
      .hasArg()
      .argName("ticker")
      .desc("Ticker " + 
   		    "(for <mode> = " + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper.SecSelectSubMode.EXCHANGE_TICKER + " only)")
      .longOpt("ticker")
      .get();
    
    Option optMIC = Option.builder("mic")
      .hasArg()
      .argName("mic")
      .desc("MIC " +
   		    "(for <mode> = " + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper.SecSelectSubMode.MIC + " only)")
      .longOpt("mic")
      .get();
    	      
    Option optMICID = Option.builder("mid")
      .hasArg()
      .argName("micid")
      .desc("MIC-ID " +
   		    "(for <mode> = " + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper.SecSelectSubMode.MIC + " only)")
      .longOpt("mic-id")
      .get();
    	    
    Option optSecIDType = Option.builder("sit")
      .hasArg()
      .argName("type")
      .desc("Security ID type " + 
   		    "(for <mode> = " + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper.SecSelectSubMode.SEC_ID_TYPE + " only)")
      .longOpt("secid-type")
      .get();
    	    	      
    Option optISIN = Option.builder("is")
      .hasArg()
      .argName("isin")
      .desc("ISIN " + 
  		   	"(for <mode> = " + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN + " xor " +
  		   	"( <mode> = " + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper.SecSelectSubMode.SEC_ID_TYPE + " ) only)")
      .longOpt("isin")
      .get();
        
    Option optSecName = Option.builder("n")
      .hasArg()
      .argName("name")
      .desc("Security name (full) " + 
  		    "(for <mode> = " + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.NAME + " only)")
      .longOpt("name")
      .get();
          
    // The convenient ones
    // ::EMPTY
            
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    options.addOption(optSubMode);
    options.addOption(optExchange);
    options.addOption(optTicker);
    options.addOption(optMIC);
    options.addOption(optMICID);
    options.addOption(optSecIDType);
    options.addOption(optISIN);
    options.addOption(optSecName);
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

	System.err.println("mmodexx: " + mode);
    GnuCashSecurity sec = SecurityHelper.getSec(mode.mode, 
    											secID, isin.toString(), secName.toString(), 
    											gcshFile);

    System.out.println("Selected security: " + sec.toString());
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
    catch (Exception exc)
    {
      System.err.println("Could not parse <gnucash-file>");
      throw new InvalidCommandLineArgsException();
    }

    if (!scriptMode)
      System.err.println("GnuCash file: '" + gcshFileName + "'");

    // <mode>, <sub-mode>,
    // <exchange>, <ticker>,
    // <mid>, <mic-id>,
    // <secid-type>, <isin>
    // <name>
    CmdLineHelper.parseSecStuffWrap( cmdLine, 
    								 mode, subMode, 
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
		formatter.printHelp( "TestSelSec", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    System.out.println("");
    System.out.println("Valid values for <mode>:");
    for ( xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode elt : xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.values() )
      System.out.println(" - " + elt);
    
    System.out.println("");
    System.out.println("Valid values for <sub-mode>:");
    for ( CmdLineHelper.SecSelectSubMode elt : CmdLineHelper.SecSelectSubMode.values() )
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
