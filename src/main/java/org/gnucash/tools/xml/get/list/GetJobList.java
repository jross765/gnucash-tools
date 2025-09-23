package org.gnucash.tools.xml.get.list;

import java.io.File;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashGenerJob;
import org.gnucash.api.read.aux.GCshOwner;
import org.gnucash.api.read.impl.GnuCashFileImpl;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetJobList extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetJobList.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String             gcshFileName = null;
  private static Helper.JobListMode mode         = null; 
  private static GCshOwner.Type     type         = null;
  private static String             name         = null;
  
  private static boolean scriptMode = false; // ::TODO

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GetJobList tool = new GetJobList ();
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
    // jobID = UUID.randomUUID();

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
      .argName("Mode")
      .desc("Mode")
      .longOpt("mode")
      .build();
    	    	    	      
    Option optType = Option.builder("t")
      .hasArg()
      .argName("type")
      .desc("(Generic) job type")
      .longOpt("type")
      .build();
      
    Option optName = Option.builder("n")
      .hasArg()
      .argName("name")
      .desc("Job name (part of)")
      .longOpt("name")
      .build();
    	    	      
    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    options.addOption(optType);
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
    GnuCashFileImpl gcshFile = new GnuCashFileImpl(new File(gcshFileName));
    
    Collection<GnuCashGenerJob> jobList = null; 
    if ( mode == Helper.JobListMode.ALL )
    	jobList = gcshFile.getGenerJobs();
    else if ( mode == Helper.JobListMode.TYPE )
    	jobList = gcshFile.getGenerJobsByType(type);
    else if ( mode == Helper.JobListMode.NAME )
    	jobList = gcshFile.getGenerJobsByName(name);
    	
    if ( jobList.size() == 0 ) 
    {
    	System.err.println("Found no job with that type.");
    	throw new NoEntryFoundException();
    }

    for ( GnuCashGenerJob job : jobList )
    {
    	System.out.println(job.toString());	
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
      System.err.println("GnuCash file:      '" + gcshFileName + "'");
    
    // <mode>
    try
    {
      mode = Helper.JobListMode.valueOf(cmdLine.getOptionValue("mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <mode>");
      throw new InvalidCommandLineArgsException();
    }

    // <type>
    try
    {
    	if ( mode != Helper.JobListMode.TYPE )
    	{
            System.err.println("Error: <type> must only be set with <mode> = '" + Helper.JobListMode.TYPE + "'");
            throw new InvalidCommandLineArgsException();
    	}
    	
      type = GCshOwner.Type.valueOf(cmdLine.getOptionValue("type"));
      if ( type != GnuCashGenerJob.TYPE_CUSTOMER &&
    	   type != GnuCashGenerJob.TYPE_VENDOR )
      {
          System.err.println("Invalid owner type for job");
          throw new InvalidCommandLineArgsException();
      }
    }
    catch ( Exception exc )
    {
    	if ( mode == Helper.JobListMode.TYPE )
    	{
            System.err.println("Error: <type> must be set with <mode> = '" + Helper.JobListMode.TYPE + "'");
            throw new InvalidCommandLineArgsException();
    	}
    }
    
    if ( ! scriptMode )
      System.err.println("Type:              " + type);

    // <name>
    if ( cmdLine.hasOption( "name" ) )
    {
    	if ( mode != Helper.JobListMode.NAME )
    	{
            System.err.println("Error: <name> must only be set with <mode> = '" + Helper.JobListMode.NAME + "'");
            throw new InvalidCommandLineArgsException();
    	}
    	
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
    else
    {
    	if ( mode == Helper.JobListMode.NAME )
    	{
            System.err.println("Error: <name> must be set with <mode> = '" + Helper.JobListMode.NAME + "'");
            throw new InvalidCommandLineArgsException();
    	}
    	
    	name = null;
    }
    
    if ( ! scriptMode )
      System.err.println("Name:              " + name);
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GetJobList", options );
    
    System.out.println("");
    System.out.println("Valid values for <mode>:");
    for ( Helper.JobListMode elt : Helper.JobListMode.values() )
      System.out.println(" - " + elt);

    System.out.println("");
    System.out.println("Valid values for <type>:");
    System.out.println(" - " + GnuCashGenerJob.TYPE_CUSTOMER);
    System.out.println(" - " + GnuCashGenerJob.TYPE_VENDOR);
  }
}
