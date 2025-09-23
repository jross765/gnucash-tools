package org.gnucash.tools.xml.sonstige;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.tools.CommandLineTool;
import org.gnucash.tools.xml.gen.complex.ParamTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class TestDepTrxListFile extends CommandLineTool
{
  // Logger
  private static final Logger LOGGER = LoggerFactory.getLogger(TestDepTrxListFile.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  // ------------------------------

  private static String            bookingListFileName = null;

  // -----------------------------------------------------------------

  // ::MAGIC
  private final static String COMMENT_TOKEN = "#";
  private final static String SEPARATOR     = "§"; // sic, not colon, not comma, not pipe  

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      TestDepTrxListFile tool = new TestDepTrxListFile ();
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
    // cfg = new PropertiesConfiguration(System.getProperty("config"));
    // getConfigSettings(cfg);

    // Options
    // The essential ones
    Option optListFile = Option.builder("blf")
      .required()
      .hasArg()
      .argName("file")
      .desc("List file for bookings")
      .longOpt("booking-list-file")
      .build();
    	    	        
    // ---
    	    
    options = new Options();
    options.addOption(optListFile);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cs) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
	  ArrayList<ParamTuple> paramTupleList = new ArrayList<ParamTuple>();
	  
	  try
	  {
		  readListFile(paramTupleList);
		  System.out.println("Params: ");
		  for ( ParamTuple elt : paramTupleList )
			  System.out.println(" - " + elt.toString());
	  }
	  catch ( Exception exc )
	  {
		  System.err.println("Could not parse list file ");
		  LOGGER.error("Could not parse list file ");
		  return;
	  }
  }

  private void readListFile(ArrayList<ParamTuple> paramTuples) throws IOException
  {
      BufferedReader br = new BufferedReader(new FileReader(bookingListFileName));

      String zeile;
      while ( (zeile = br.readLine()) != null ) 
      {
        if ( zeile.startsWith(COMMENT_TOKEN) )
          continue;
        
        String[] eintrag = zeile.split(SEPARATOR);
        ParamTuple newTuple = new ParamTuple(eintrag[0], eintrag[1], eintrag[2],
        									 eintrag[3], eintrag[4], eintrag[5],
        									 eintrag[6], eintrag[7], eintrag[8],
        									 eintrag[9], eintrag[10], eintrag[11]);
        
        paramTuples.add(newTuple);
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

    // <booking-list-file>
    try
    {
    	bookingListFileName = cmdLine.getOptionValue("booking-list-file");
    }
    catch ( Exception exc )
    {
    	System.err.println("Could not parse <booking-list-file>");
    	throw new InvalidCommandLineArgsException();
    }

    System.err.println("Booking list file:   '" + bookingListFileName + "'");
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "TestDepTrxListFile", options );
  }
}
