package org.gnucash.tools.xml.gen.simple;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashPrice;
import org.gnucash.api.write.GnuCashWritablePrice;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.base.basetypes.complex.GCshCmdtyID;
import org.gnucash.base.basetypes.complex.GCshCurrID;
import org.gnucash.tools.CommandLineTool;
import org.gnucash.tools.xml.helper.CmdLineHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class GenPrc extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
private static final Logger LOGGER = LoggerFactory.getLogger(GenPrc.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String gcshInFileName = null;
  private static String gcshOutFileName = null;

  private static GCshCmdtyID         fromSecCurrID = null;
  private static GCshCurrID          toCurrID = null;
  private static Helper.DateFormat   dateFmt = null;
  private static LocalDate           date = null;
  private static FixedPointNumber    value = null;
  private static GnuCashPrice.Source source = null;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GenPrc tool = new GenPrc ();
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
      .get();
          
    Option optFileOut = Option.builder("of")
      .required()
      .hasArg()
      .argName("file")
      .desc("GnuCash file (out)")
      .longOpt("gnucash-out-file")
      .get();
      
    Option optFromSecCurr = Option.builder("f")
      .required()
      .hasArg()
      .argName("sec/curr")
      .desc("From-security/currency")
      .longOpt("from-sec-curr")
      .get();
          
    Option optToCurr = Option.builder("t")
      .required()
      .hasArg()
      .argName("curr")
      .desc("To-currency")
      .longOpt("to-curr")
      .get();
    
    Option optDateFormat = Option.builder("df")
      .hasArg()
      .argName("date-format")
      .desc("Date format")
      .longOpt("date-format")
      .get();
            
    Option optDate = Option.builder("dat")
      .required()
      .hasArg()
      .argName("date")
      .desc("Date")
      .longOpt("date")
      .get();
          
    Option optValue = Option.builder("v")
      .required()
      .hasArg()
      .argName("value")
      .desc("Value")
      .longOpt("value")
      .get();
            
    // The convenient ones
    Option optSource = Option.builder("src")
      .hasArg()
      .argName("source")
      .desc("Source")
      .longOpt("source")
      .get();
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optFromSecCurr);
    options.addOption(optToCurr);
    options.addOption(optDateFormat);
    options.addOption(optDate);
    options.addOption(optValue);
    options.addOption(optSource);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cfg) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    GnuCashWritableFileImpl gcshFile = new GnuCashWritableFileImpl(new File(gcshInFileName), true);
    
    GnuCashWritablePrice prc = gcshFile.createWritablePrice(fromSecCurrID, toCurrID, date);
    // prc.setFromSecCurrQualifID(fromSecCurrID);
    // prc.setToCurrencyQualifID(toCurrID);
    prc.setType(GnuCashPrice.Type.LAST);
    // prc.setDate(date);
    prc.setValue(value);
    prc.setSource(source);
    
    System.out.println("Price to write: " + prc.toString());
    gcshFile.writeFile(new File(gcshOutFileName));
    System.out.println("OK");
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
    
    // <from-sec-curr>
    try
    {
      fromSecCurrID = GCshCmdtyID.parse(cmdLine.getOptionValue("from-sec-curr")); 
      System.err.println("from-sec-curr: " + fromSecCurrID);
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <from-sec-curr>");
      throw new InvalidCommandLineArgsException();
    }
    
    // <to-curr>
    try
    {
      toCurrID = GCshCurrID.parse(cmdLine.getOptionValue("to-curr")); 
      System.err.println("to-curr: " + toCurrID);
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <to-curr>");
      throw new InvalidCommandLineArgsException();
    }
    
    // <date-format>
    dateFmt = CmdLineHelper.getDateFormat(cmdLine, "date-format");
    System.err.println("date-format: " + dateFmt);

    // <date>
    try
    {
      date = CmdLineHelper.getDate(cmdLine, "date", dateFmt); 
      System.err.println("date: " + date);
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <date>");
      throw new InvalidCommandLineArgsException();
    }
    
    // <value>
    try
    {
      value = new FixedPointNumber( Double.parseDouble( cmdLine.getOptionValue("value") ) ) ; 
      System.err.println("value: " + value);
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <name>");
      throw new InvalidCommandLineArgsException();
    }
    
    // <source>
    if ( cmdLine.hasOption("source") )
    {
      try
      {
        source = GnuCashPrice.Source.valueOf( cmdLine.getOptionValue("source") ); 
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <name>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      source = GnuCashPrice.Source.USER_PRICE;
    }
    System.err.println("source: " + source);
    
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GenPrc", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    System.out.println("");
    System.out.println("Valid values for <source>:");
    for ( GnuCashPrice.Source elt : GnuCashPrice.Source.values() )
      System.out.println(" - " + elt);
  }
}
