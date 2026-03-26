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
import org.gnucash.api.read.GnuCashPrice;
import org.gnucash.apispec.read.GnuCashSecurity;
import org.gnucash.apispec.read.impl.GnuCashFileExtImpl;
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

public class GetSecInfo extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetSecInfo.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String  gcshFileName = null;
  
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
  private static StringBuffer  secName  = new StringBuffer();
  
  private static boolean showQuotes = false;
  
  private static boolean scriptMode = false; // ::TODO
  
  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GetSecInfo tool = new GetSecInfo ();
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

    Option optSecName = Option.builder("sn")
      .hasArg()
      .argName("name")
      .desc("Security name (full) " + 
  		    "(for <mode> = " + Helper.CmdtySecSingleSelMode.NAME + " only)")
      .longOpt("security-name")
      .get();

    // The convenient ones
    Option optShowQuote = Option.builder("squt")
      .desc("Show quotes")
      .longOpt("show-quotes")
      .get();
            
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    options.addOption(optSubMode);
    options.addOption(optSecID);
    options.addOption(optExchange);
    options.addOption(optTicker);
    options.addOption(optMIC);
    options.addOption(optMICID);
    options.addOption(optSecIDType);
    options.addOption(optISIN);
    options.addOption(optSecName);
    options.addOption(optShowQuote);
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

    GnuCashSecurity sec = SecurityHelper.getSec(secSelMode,
    											secID, isin.toString(), secName.toString(), 
												gcshFile,
												scriptMode);
    
    // ----------------------------

    try
    {
      System.out.println("Qualified ID:      '" + sec.getQualifID() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("Qualified ID:      " + "ERROR");
    }

    try
    {
      System.out.println("XCode (ISIN):      '" + sec.getXCode() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("XCode (ISIN):      " + "ERROR");
    }

    try
    {
      System.out.println("toString:          " + sec.toString());
    }
    catch (Exception exc)
    {
      System.out.println("toString:          " + "ERROR");
    }
    
    try
    {
      System.out.println("Symbol:            '" + sec.getSymbol() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("Symbol:            " + "ERROR");
    }

    try
    {
      System.out.println("Name:              '" + sec.getName() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("Name:              " + "ERROR");
    }

    try
    {
      System.out.println("Fraction:          " + sec.getFraction());
    }
    catch (Exception exc)
    {
      System.out.println("Fraction:          " + "ERROR");
    }

    // ---

    if ( showQuotes )
      showQuotes(sec);
  }

  // -----------------------------------------------------------------

  private void showQuotes(GnuCashSecurity sec)
  {
    System.out.println("");
    System.out.println("Quotes:");

    System.out.println("");
    System.out.println("Number of quotes: " + sec.getQuotes().size());
    
    System.out.println("");
    for ( GnuCashPrice prc : sec.getQuotes() )
    {
      System.out.println(" - " + prc.toString());
    }

    System.out.println("");
    System.out.println("Youngest Quote:");
    System.out.println(sec.getYoungestQuote());
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
    // <name>
    CmdLineHelper_Sec.parseSecStuffWrap( cmdLine, 
    								 secSelMode, secSelSubMode, null,
    								 secID, 
    								 ticker, micID, isin, 
    								 secName, 
    								 scriptMode );

    // <show-quotes>
    if (cmdLine.hasOption("show-quotes"))
    {
      showQuotes = true;
    }
    else
    {
      showQuotes = false;
    }

    if (!scriptMode)
      System.err.println("Show quotes: " + showQuotes);
  }

  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GetSecInfo", "", options, "", true );
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
    System.out.println("Valid values for <sub-mode>:");
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
