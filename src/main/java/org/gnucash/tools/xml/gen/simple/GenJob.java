package org.gnucash.tools.xml.gen.simple;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashCustomer;
import org.gnucash.api.read.GnuCashGenerJob;
import org.gnucash.api.read.GnuCashVendor;
import org.gnucash.api.read.OwnerNotFoundException;
import org.gnucash.api.read.TaxTableNotFoundException;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.api.write.spec.GnuCashWritableCustomerJob;
import org.gnucash.api.write.spec.GnuCashWritableVendorJob;
import org.gnucash.base.basetypes.simple.GCshCustID;
import org.gnucash.base.basetypes.simple.GCshID;
import org.gnucash.base.basetypes.simple.GCshVendID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GenJob extends CommandLineTool
{
  enum JobType
  {
    CUSTOMER,
    VENDOR
  }
  
  // Logger
  @SuppressWarnings("unused")
private static final Logger LOGGER = LoggerFactory.getLogger(GenJob.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String           gcshInFileName = null;
  private static String           gcshOutFileName = null;
  private static JobType          type = null;
  private static GCshID           ownerID = null;
  private static String           number = null;
  private static String           name = null;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GenJob tool = new GenJob ();
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
      .build();
          
    Option optFileOut = Option.builder("of")
      .required()
      .hasArg()
      .argName("file")
      .desc("GnuCash file (out)")
      .longOpt("gnucash-out-file")
      .build();
      
    Option optType = Option.builder("t")
      .required()
      .hasArg()
      .argName("type")
      .desc("Job type")
      .longOpt("type")
      .build();
      
    Option optOwnerID = Option.builder("own")
      .required()
      .hasArg()
      .argName("owner")
      .desc("Owner ID")
      .longOpt("owner-id")
      .build();
        
    Option optNumber = Option.builder("no")
      .required()
      .hasArg()
      .argName("number")
      .desc("Job number")
      .longOpt("number")
      .build();
      
    Option optName = Option.builder("nm")
      .required()
      .hasArg()
      .argName("name")
      .desc("Job name")
      .longOpt("name")
      .build();
      
    // The convenient ones
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optType);
    options.addOption(optOwnerID);
    options.addOption(optNumber);
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
    
    GnuCashGenerJob job = null;
    if ( type == JobType.CUSTOMER )
      job = doCustomer(gcshFile);
    else if ( type == JobType.VENDOR )
      job = doVendor(gcshFile);

    System.out.println("Job to write: " + job.toString());
    gcshFile.writeFile(new File(gcshOutFileName));
    System.out.println("OK");
  }

  // -----------------------------------------------------------------

  private GnuCashWritableCustomerJob doCustomer(GnuCashWritableFileImpl gcshFile)
      throws OwnerNotFoundException
  {
    GnuCashCustomer cust = null;
    try
    {
      cust = gcshFile.getWritableCustomerByID(new GCshCustID(ownerID));
      System.err.println("Customer: " + cust.getNumber() + " (" + cust.getName() + ")");
    }
    catch ( Exception exc )
    {
      System.err.println("Error: No customer with ID '" + ownerID + "' found");
      throw new OwnerNotFoundException();
    }
    
    GnuCashWritableCustomerJob job = gcshFile.createWritableCustomerJob(cust, number, name);
    // TODO
    // job.setNotes("Generated by GenJob");
    
    return job;
  }

  private GnuCashWritableVendorJob doVendor(GnuCashWritableFileImpl gcshFile)
      throws OwnerNotFoundException, TaxTableNotFoundException
  {
    GnuCashVendor vend = null;
    try
    {
      vend = gcshFile.getVendorByID(new GCshVendID(ownerID));
      System.err.println("Vendor: " + vend.getNumber() + " (" + vend.getName() + ")");
    }
    catch ( Exception exc )
    {
      System.err.println("Error: No vendor with ID '" + ownerID + "' found");
      throw new OwnerNotFoundException();
    }
    
    GnuCashWritableVendorJob job = gcshFile.createWritableVendorJob(vend, number, name);
    // TODO
    // job.setNotes("Generated by GenJob");
    
    return job;
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
    
    // <type>
    try
    {
      type = JobType.valueOf(cmdLine.getOptionValue("type"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <type>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Type: " + type);

    // <owner-id>
    try
    {
      ownerID = new GCshID( cmdLine.getOptionValue("owner-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <owner-id>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Owner ID: '" + ownerID + "'");

    // <number>
    try
    {
      number = cmdLine.getOptionValue("number");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <number>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Number: '" + number + "'");

    // <name>
    try
    {
      name = cmdLine.getOptionValue("name");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <name>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Name: '" + name + "'");
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GenJob", options );
    
    System.out.println("");
    System.out.println("Valid values for <type>:");
    for ( JobType elt : JobType.values() )
      System.out.println(" - " + elt);
  }
}
