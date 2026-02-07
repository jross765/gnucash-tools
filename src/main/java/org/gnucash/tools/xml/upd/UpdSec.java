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
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.apispec.write.GnuCashWritableSecurity;
import org.gnucash.apispec.write.impl.GnuCashWritableFileExtImpl;
import org.gnucash.base.basetypes.complex.GCshCmdtyNameSpace;
import org.gnucash.base.basetypes.complex.GCshSecID;
import org.gnucash.tools.CommandLineTool;
import org.gnucash.tools.xml.helper.CmdLineHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
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
  
  private static CmdLineHelper.SecSelectMode mode = null;
  private static CmdLineHelper.SecSelectSubMode subMode = null;

  private static GCshSecID secID = null;
  
  private static String  isin     = null;
  // Possibly later:
  // private static String  wkn      = null;
  // private static String  cusip    = null;
  // private static String  sedol    = null;

  private static String  name     = null;

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
      .get();
          
    Option optFileOut = Option.builder("of")
      .required()
      .hasArg()
      .argName("file")
      .desc("GnuCash file (out)")
      .longOpt("gnucash-out-file")
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
    		"(for <mode> = " + CmdLineHelper.SecSelectMode.ID + " only)")
      .longOpt("sub-mode")
      .get();
    	        
    Option optExchange = Option.builder("exch")
      .hasArg()
      .argName("exch")
      .desc("Exchange code " +
   		    "(for <mode> = " + CmdLineHelper.SecSelectMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper.SecSelectSubMode.EXCHANGE_TICKER + " only)")
      .longOpt("exchange")
      .get();
    
    Option optTicker = Option.builder("tkr")
      .hasArg()
      .argName("ticker")
      .desc("Ticker " + 
   		    "(for <mode> = " + CmdLineHelper.SecSelectMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper.SecSelectSubMode.EXCHANGE_TICKER + " only)")
      .longOpt("ticker")
      .get();
    
    Option optMIC = Option.builder("mic")
      .hasArg()
      .argName("mic")
      .desc("MIC " +
   		    "(for <mode> = " + CmdLineHelper.SecSelectMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper.SecSelectSubMode.MIC + " only)")
      .longOpt("mic")
      .get();
    	      
    Option optMICID = Option.builder("mid")
      .hasArg()
      .argName("micid")
      .desc("MIC-ID " +
   		    "(for <mode> = " + CmdLineHelper.SecSelectMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper.SecSelectSubMode.MIC + " only)")
      .longOpt("mic-id")
      .get();
    	    
    Option optSecIDType = Option.builder("sit")
      .hasArg()
      .argName("type")
      .desc("Security ID type " + 
   		    "(for <mode> = " + CmdLineHelper.SecSelectMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper.SecSelectSubMode.SEC_ID_TYPE + " only)")
      .longOpt("secid-type")
      .get();
    	    	      
    Option optISIN = Option.builder("is")
      .hasArg()
      .argName("isin")
      .desc("ISIN " + 
  		   	"(for <mode> = " + CmdLineHelper.SecSelectMode.ISIN + " xor " +
  		   	"( <mode> = " + CmdLineHelper.SecSelectMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper.SecSelectSubMode.SEC_ID_TYPE + " ) only)")
      .longOpt("isin")
      .get();
            
    Option optName = Option.builder("n")
      .hasArg()
      .argName("name")
      .desc("Security name")
      .longOpt("name")
      .get();
    
//    Option optDescr = Option.builder("desc")
//      .hasArg()
//      .argName("descr")
//      .desc("Account description")
//      .longOpt("description")
//      .get();
      
    Option optType = Option.builder("t")
      .hasArg()
      .argName("type")
      .desc("Account type")
      .longOpt("type")
      .get();
        
    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optMode);
    options.addOption(optSubMode);
    options.addOption(optExchange);
    options.addOption(optTicker);
    options.addOption(optMIC);
    options.addOption(optMICID);
    options.addOption(optSecIDType);
    options.addOption(optISIN);
    options.addOption(optName);
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

    sec = null;
    if ( mode == CmdLineHelper.SecSelectMode.ID )
    {
    	if ( secID != null ) 
    	{
    		sec = gcshFile.getWritableSecurityByID(secID);
    		if ( sec == null )
    		{
    			System.err.println("Could not find security with ID " + secID);
    	        throw new NoEntryFoundException();
    		}
    	}
    	else
    	{
    		// Should not happen -- just in case
			System.err.println("Parsed security ID is null. Cannot continue");
	        throw new NoEntryFoundException();
    	}
    }
    else if ( mode == CmdLineHelper.SecSelectMode.ISIN )
    {
      // CAUTION: This branch is *not necessarily* redundant to
	  // the Mode.ID / SubMode.SEC_ID_TYPE branch above 
      // (it only is in the project's specific test file, which 
      // reflects the way the author organizes his data, but by 
      // no means is the only "correct", let alone conceivable way).
      sec = gcshFile.getWritableSecurityByXCode(isin);
      if ( sec == null )
      {
        System.err.println("Could not find a security with this ISIN.");
        throw new NoEntryFoundException();
      }
    }
    // NOT by name!
    
    // ----------------------------
    
    doChanges(gcshFile);
    System.err.println("Account after update: " + sec.toString());
    
    gcshFile.writeFile(new File(gcshOutFileName));
    
    System.out.println("OK");
  }

  private void doChanges(GnuCashWritableFileImpl gcshFile) throws Exception
  {
    if ( name != null )
    {
      System.err.println("Setting name");
      sec.setName(name);
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

    // <mode>
    try
    {
      mode = CmdLineHelper.SecSelectMode.valueOf(cmdLine.getOptionValue("mode"));
      if ( mode == CmdLineHelper.SecSelectMode.NAME )
      {
          System.err.println("<mode> = " + CmdLineHelper.SecSelectMode.NAME + " is not allowed in this context");
          throw new InvalidCommandLineArgsException();
      }
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("Mode:         " + mode);

    // <sub-mode>
    if ( cmdLine.hasOption("sub-mode") )
    {
        if ( mode != CmdLineHelper.SecSelectMode.ID )
        {
          System.err.println("<sub-mode> must only be set with <mode> = '" + CmdLineHelper.SecSelectMode.ID.toString() + "'");
          throw new InvalidCommandLineArgsException();
        }
        
        try
        {
          subMode = CmdLineHelper.SecSelectSubMode.valueOf(cmdLine.getOptionValue("sub-mode"));
        }
        catch ( Exception exc )
        {
          System.err.println("Could not parse <sub-mode>");
          throw new InvalidCommandLineArgsException();
        }
    }
    else
    {
        if ( mode == CmdLineHelper.SecSelectMode.ID )
        {
          System.err.println("<sub-mode> must be set with <mode> = '" + CmdLineHelper.SecSelectMode.ID.toString() + "'");
          throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
      System.err.println("Sub-mode:     " + subMode);

    // <exchange>, <ticker>,
    // <mid>, <mic-id>,
    // <secid-type>, <isin>
    if ( ( cmdLine.hasOption("exchange")   && cmdLine.hasOption("ticker") ) ||
    	 ( cmdLine.hasOption("mic")        && cmdLine.hasOption("mic-id") ) ||
    	 ( cmdLine.hasOption("secid-type") && cmdLine.hasOption("isin") ) )
    {
        if ( mode != CmdLineHelper.SecSelectMode.ID )
        {
          System.err.println("Pair <exchange>/<ticker>, <mic>/<mic-id>, <secid-type>/<isin> must only be set with <mode> = '" + CmdLineHelper.SecSelectMode.ID.toString() + "'");
          throw new InvalidCommandLineArgsException();
        }
    	
    	secID = CmdLineHelper.getSecID( cmdLine,
    										mode, subMode,
    										scriptMode);
    	if ( secID == null )
    	{
            System.err.println("Could not get security ID from " + 
            				   "<exchange>/<ticker> nor from" + 
            				   "<mic>/<mic-id> nor from" + 
            				   "<secid-type>/<isin>");
            throw new InvalidCommandLineArgsException();
    	}
    }
    else
    {
    	if ( ! cmdLine.hasOption("isin") )
       	{
               System.err.println("One of the following must be set:\n" + 
            		   			  " - <exchange>/<ticker> (pair) xor\n"+ 
            		   			  " - <mic>/<mic-id> (pair) xor\n"+ 
               				   	  " - <secid-type>/<isin> (pair) xor\n"+ 
               				   	  " - <isin> (alone)");
               throw new InvalidCommandLineArgsException();
       	}
    }

    // <isin> (alone)
    if ( cmdLine.hasOption("isin") && 
    	 ! cmdLine.hasOption("secid-type") )
    {
        if ( cmdLine.hasOption("exchange") ) 
        {
          System.err.println("Error: <isin> and <exchange> are mutually exclusive");
          throw new InvalidCommandLineArgsException();
        }

        if ( cmdLine.hasOption("mic") ) 
        {
          System.err.println("Error: <isin> and <mic> are mutually exclusive");
          throw new InvalidCommandLineArgsException();
        }

      if ( mode != CmdLineHelper.SecSelectMode.ISIN )
      {
        System.err.println("<isin> (alone) must only be set with <mode> = '" + CmdLineHelper.SecSelectMode.ISIN + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        isin = cmdLine.getOptionValue("isin");
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <isin>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
//    	if ( ! cmdLine.hasOption("name") )
//    	{
//                  System.err.println("One of the following must be set:\n" + 
//               		   			  " - <exchange>/<ticker> (pair) xor\n"+ 
//               		   			  " - <mic>/<mic-id> (pair) xor\n"+ 
//                  				   	  " - <secid-type>/<isin> (pair) xor\n"+ 
//                  				   	  " - <isin> (alone)");
//                  throw new InvalidCommandLineArgsException();
//    	}
    	
      if ( mode == CmdLineHelper.SecSelectMode.ISIN &&
    	   ! cmdLine.hasOption("secid-type") )
      {
        System.err.println("<isin> (alone) must be set with <mode> = '" + CmdLineHelper.SecSelectMode.ISIN + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if ( ! scriptMode )
      System.err.println("ISIN:         '" + isin + "'");

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

    if (!scriptMode)
      System.err.println("Name:         '" + name + "'");
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
    System.out.println("Valid values for <mode>:");
    for ( CmdLineHelper.SecSelectMode elt : CmdLineHelper.SecSelectMode.values() )
    {
      if ( elt != CmdLineHelper.SecSelectMode.NAME ) // sic
      {
    	  System.out.println(" - " + elt);
      }
    }
    
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
