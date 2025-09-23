package org.gnucash.tools.xml.sonstige;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.gnucash.tools.CommandLineTool;
import org.w3c.dom.Document;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class TestValidate extends CommandLineTool
{
  // Logger
  private static Logger logger = Logger.getLogger(TestValidate.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String gcshFileName = null;
  private static String schemaFileName = null;

  public static void main( String[] args )
  {
    try
    {
      TestValidate tool = new TestValidate ();
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
    Option optFile = Option.builder("f")
      .required()
      .hasArg()
      .argName("file")
      .desc("GnuCash file")
      .longOpt("GnuCash file")
      .build();
      
    Option optSchema = Option.builder("s")
      .required()
      .hasArg()
      .argName("file")
      .desc("GnuCash schema file (XSD)")
      .longOpt("schema-file")
      .build();
        
    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optSchema);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cs) throws Exception
  {
    // ::EMPTY
  }
  
  // https://howtodoinjava.com/java/xml/read-xml-dom-parser-example/
  // https://stackoverflow.com/questions/26651391/how-to-determine-whether-a-given-file-is-an-xml-valide-file
  // https://stackoverflow.com/questions/55921120/xml-validation-against-schema-fails-in-java-with-namespace
  @Override
  protected void kernel() throws Exception
  {
    File gcshFile = new File(gcshFileName);
    File schemaFile = new File(schemaFileName);
    
    Document doc = null;
    try
    {
      DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
      fact.setNamespaceAware(true);
      DocumentBuilder builder = fact.newDocumentBuilder();
      doc = builder.parse(gcshFile);
      System.err.println("Parsed document sucessfully");
    }
    catch (Exception exc)
    {
      System.err.println("Could not parse document");
      System.err.println("Aborting");
      throw exc;
    }

    Schema schema = null;
    try
    {
      String lang = XMLConstants.W3C_XML_SCHEMA_NS_URI;
      SchemaFactory fact = SchemaFactory.newInstance(lang);
      schema = fact.newSchema(schemaFile);
      System.err.println("Built schema sucessfully");
    }
    catch (Exception exc)
    {
      System.err.println("Could not build schema");
      System.err.println("Aborting");
      throw exc;
    }

    DOMResult validResult = null;
    try
    {
      Validator valdtr = schema.newValidator();
      valdtr.validate(new DOMSource(doc), validResult);
      System.out.println("Validated document successfully: " + validResult);
    }
    catch (Exception exc)
    {
      System.err.println("Could not validate document: " + validResult);
      System.err.println("Aborting");
      throw exc;
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

    // <GnuCash file>
    try
    {
      gcshFileName = cmdLine.getOptionValue("GnuCash file");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <GnuCash file>");
      throw new InvalidCommandLineArgsException();
    }
    
    System.err.println("GnuCash file: '" + gcshFileName + "'");

    // <schema-file>
    try
    {
      schemaFileName = cmdLine.getOptionValue("schema-file");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <schema-file>");
      throw new InvalidCommandLineArgsException();
    }
    
    System.err.println("GnuCash schema file: '" + schemaFileName + "'");
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "TestValidate", options );
  }
}
