package org.gnucash.tools;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;

public abstract class CommandLineTool extends xyz.schnorxoborx.base.cmdlinetools.CommandLineTool
{
	@Override
	public void execute(String[] args) throws CouldNotExecuteException
	{
		// Initialize
		try {
			init();
		} catch ( Exception exc ) {
			System.err.println( "Could not initialize environment." );
			exc.printStackTrace();
			throw new CouldNotExecuteException();
		}

		// Parse command line
		try	{
			parseCommandLineArgs( args );
		} catch ( Exception exc ) {
			System.err.println( "Invalid command line args." );
			printUsage();
			throw new CouldNotExecuteException();
		}

		try	{
			kernel();
		} catch ( Exception exc ) {
			System.err.println( "Error in Tool kernel." );
			exc.printStackTrace();
			throw new CouldNotExecuteException();
		}
	}

	protected abstract void kernel() throws Exception;
}
