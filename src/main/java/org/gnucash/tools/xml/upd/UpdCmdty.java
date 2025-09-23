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
import org.gnucash.api.write.GnuCashWritableCommodity;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.base.basetypes.complex.GCshCmdtyCurrNameSpace;
import org.gnucash.base.basetypes.complex.GCshCmdtyID;
import org.gnucash.tools.CommandLineTool;
import org.gnucash.tools.xml.helper.CmdLineHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class UpdCmdty extends CommandLineTool
{
  // Logger
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdCmdty.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String gcshInFileName  = null;
  private static String gcshOutFileName = null;
  
  private static CmdLineHelper.CmdtySelectMode mode = null;
  private static CmdLineHelper.CmdtySelectSubMode subMode = null;

  private static GCshCmdtyID cmdtyID = null;
  
  private static String  isin     = null;
  // Possibly later:
  // private static String  wkn      = null;
  // private static String  cusip    = null;
  // private static String  sedol    = null;

  private static String  name     = null;

  private static GnuCashWritableCommodity cmdty = null;

  private static boolean scriptMode = false; // ::TODO
  
  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      UpdCmdty tool = new UpdCmdty ();
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
      
    Option optMode = Option.builder("m")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode")
      .longOpt("mode")
      .build();
        
    Option optSubMode = Option.builder("sm")
      .hasArg()
      .argName("submode")
      .desc("Selection sub-mode " +
    		"(for <mode> = " + CmdLineHelper.CmdtySelectMode.ID + " only)")
      .longOpt("sub-mode")
      .build();
    	        
    Option optExchange = Option.builder("exch")
      .hasArg()
      .argName("exch")
      .desc("Exchange code " +
   		    "(for <mode> = " + CmdLineHelper.CmdtySelectMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper.CmdtySelectSubMode.EXCHANGE_TICKER + " only)")
      .longOpt("exchange")
      .build();
    
    Option optTicker = Option.builder("tkr")
      .hasArg()
      .argName("ticker")
      .desc("Ticker " + 
   		    "(for <mode> = " + CmdLineHelper.CmdtySelectMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper.CmdtySelectSubMode.EXCHANGE_TICKER + " only)")
      .longOpt("ticker")
      .build();
    
    Option optMIC = Option.builder("mic")
      .hasArg()
      .argName("mic")
      .desc("MIC " +
   		    "(for <mode> = " + CmdLineHelper.CmdtySelectMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper.CmdtySelectSubMode.MIC + " only)")
      .longOpt("mic")
      .build();
    	      
    Option optMICID = Option.builder("mid")
      .hasArg()
      .argName("micid")
      .desc("MIC-ID " +
   		    "(for <mode> = " + CmdLineHelper.CmdtySelectMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper.CmdtySelectSubMode.MIC + " only)")
      .longOpt("mic-id")
      .build();
    	    
    Option optSecIDType = Option.builder("sit")
      .hasArg()
      .argName("type")
      .desc("Security ID type " + 
   		    "(for <mode> = " + CmdLineHelper.CmdtySelectMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper.CmdtySelectSubMode.SEC_ID_TYPE + " only)")
      .longOpt("secid-type")
      .build();
    	    	      
    Option optISIN = Option.builder("is")
      .hasArg()
      .argName("isin")
      .desc("ISIN " + 
  		   	"(for <mode> = " + CmdLineHelper.CmdtySelectMode.ISIN + " xor " +
  		   	"( <mode> = " + CmdLineHelper.CmdtySelectMode.ID + " and " +
            "<sub-mode> = " + CmdLineHelper.CmdtySelectSubMode.SEC_ID_TYPE + " ) only)")
      .longOpt("isin")
      .build();
            
    Option optName = Option.builder("n")
      .hasArg()
      .argName("name")
      .desc("Security name")
      .longOpt("name")
      .build();
    
//    Option optDescr = Option.builder("desc")
//      .hasArg()
//      .argName("descr")
//      .desc("Account description")
//      .longOpt("description")
//      .build();
      
    Option optType = Option.builder("t")
      .hasArg()
      .argName("type")
      .desc("Account type")
      .longOpt("type")
      .build();
        
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
  protected void getConfigSettings(PropertiesConfiguration cs) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    GnuCashWritableFileImpl gcshFile = new GnuCashWritableFileImpl(new File(gcshInFileName));

    cmdty = null;
    if ( mode == CmdLineHelper.CmdtySelectMode.ID )
    {
    	if ( cmdtyID != null ) 
    	{
    		cmdty = gcshFile.getWritableCommodityByQualifID(cmdtyID);
    		if ( cmdty == null )
    		{
    			System.err.println("Could not find commodity with id " + cmdtyID);
    	        throw new NoEntryFoundException();
    		}
    	}
    	else
    	{
    		// Should not happen -- just in case
			System.err.println("Parsed commodity ID is null. Cannot continue");
	        throw new NoEntryFoundException();
    	}
    }
    else if ( mode == CmdLineHelper.CmdtySelectMode.ISIN )
    {
      // CAUTION: This branch is *not necessarily* redundant to
	  // the Mode.ID / SubMode.SEC_ID_TYPE branch above 
      // (it only is in the project's specific test file, which 
      // reflects the way the author organizes his data, but by 
      // no means is the only "correct", let alone conceivable way).
      cmdty = gcshFile.getWritableCommodityByXCode(isin);
      if ( cmdty == null )
      {
        System.err.println("Could not find a commodity with this ISIN.");
        throw new NoEntryFoundException();
      }
    }
    // NOT by name!
    
    // ----------------------------
    
    doChanges(gcshFile);
    System.err.println("Account after update: " + cmdty.toString());
    
    gcshFile.writeFile(new File(gcshOutFileName));
    
    System.out.println("OK");
  }

  private void doChanges(GnuCashWritableFileImpl gcshFile) throws Exception
  {
    if ( name != null )
    {
      System.err.println("Setting name");
      cmdty.setName(name);
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
      mode = CmdLineHelper.CmdtySelectMode.valueOf(cmdLine.getOptionValue("mode"));
      if ( mode == CmdLineHelper.CmdtySelectMode.NAME )
      {
          System.err.println("<mode> = " + CmdLineHelper.CmdtySelectMode.NAME + " is not allowed in this context");
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
        if ( mode != CmdLineHelper.CmdtySelectMode.ID )
        {
          System.err.println("<sub-mode> must only be set with <mode> = '" + CmdLineHelper.CmdtySelectMode.ID.toString() + "'");
          throw new InvalidCommandLineArgsException();
        }
        
        try
        {
          subMode = CmdLineHelper.CmdtySelectSubMode.valueOf(cmdLine.getOptionValue("sub-mode"));
        }
        catch ( Exception exc )
        {
          System.err.println("Could not parse <sub-mode>");
          throw new InvalidCommandLineArgsException();
        }
    }
    else
    {
        if ( mode == CmdLineHelper.CmdtySelectMode.ID )
        {
          System.err.println("<sub-mode> must be set with <mode> = '" + CmdLineHelper.CmdtySelectMode.ID.toString() + "'");
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
        if ( mode != CmdLineHelper.CmdtySelectMode.ID )
        {
          System.err.println("Pair <exchange>/<ticker>, <mic>/<mic-id>, <secid-type>/<isin> must only be set with <mode> = '" + CmdLineHelper.CmdtySelectMode.ID.toString() + "'");
          throw new InvalidCommandLineArgsException();
        }
    	
    	cmdtyID = CmdLineHelper.getCmdtyID( cmdLine,
    										mode, subMode,
    										scriptMode);
    	if ( cmdtyID == null )
    	{
            System.err.println("Could not get commodity ID from " + 
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

      if ( mode != CmdLineHelper.CmdtySelectMode.ISIN )
      {
        System.err.println("<isin> (alone) must only be set with <mode> = '" + CmdLineHelper.CmdtySelectMode.ISIN + "'");
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
    	
      if ( mode == CmdLineHelper.CmdtySelectMode.ISIN &&
    	   ! cmdLine.hasOption("secid-type") )
      {
        System.err.println("<isin> (alone) must be set with <mode> = '" + CmdLineHelper.CmdtySelectMode.ISIN + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("ISIN:         '" + isin + "'");

    // <name>
    if ( cmdLine.hasOption("name") )
    {
      try
      {
        name = cmdLine.getOptionValue("name");
      }
      catch (Exception exc)
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
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "UpdCmdty", options );
    
    System.out.println("");
    System.out.println("Valid values for <mode>:");
    for ( CmdLineHelper.CmdtySelectMode elt : CmdLineHelper.CmdtySelectMode.values() )
    {
      if ( elt != CmdLineHelper.CmdtySelectMode.NAME ) // sic
      {
    	  System.out.println(" - " + elt);
      }
    }
    
    System.out.println("");
    System.out.println("Valid values for <sub-mode>:");
    for ( CmdLineHelper.CmdtySelectSubMode elt : CmdLineHelper.CmdtySelectSubMode.values() )
      System.out.println(" - " + elt);
    
    System.out.println("");
    System.out.println("Valid values for <exchange>:");
    for ( GCshCmdtyCurrNameSpace.Exchange elt : GCshCmdtyCurrNameSpace.Exchange.values() )
      System.out.println(" - " + elt);
    
    System.out.println("");
    System.out.println("Valid values for <mic>:");
    for ( GCshCmdtyCurrNameSpace.MIC elt : GCshCmdtyCurrNameSpace.MIC.values() )
      System.out.println(" - " + elt);
    
    System.out.println("");
    System.out.println("Valid values for <secid-type>:");
    for ( GCshCmdtyCurrNameSpace.SecIdType elt : GCshCmdtyCurrNameSpace.SecIdType.values() )
      System.out.println(" - " + elt);
  }
}
