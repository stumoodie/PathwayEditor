package org.pathwayeditor.visualeditor;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.management.INotationSubsystemPool;
import org.pathwayeditor.visualeditor.query.INetworkQueryEngine;
import org.pathwayeditor.visualeditor.query.IPathwayQueryController;
import org.pathwayeditor.visualeditor.query.NetworkQueryEngine;
import org.pathwayeditor.visualeditor.query.PathwayQueryController;
import org.pathwayeditor.visualeditor.query.QueryData;
import org.postgresql.ds.PGPoolingDataSource;

public class PathwayQueryCmd {

	private final Logger logger = Logger.getLogger(this.getClass());
	private static final String DEFAULT_THRESHOLD = "6.906e-06";
	private static final int SUCCESS = 0;
	private static final int FAIL = 1;
	private static final List<String> HELP_OPTIONS = Arrays.asList("?", "h", "help");
	private static final String USAGE = "program <options> <idb file> <server name> <database name> <username> <password>"; //$NON-NLS-1$
	private static final int IDB_IDX_POSN = 0;
	private static final int SERVER_NAME_IDX_POSN = 1;
	private static final int DB_NAME_IDX_POSN = 2;
	private static final int USERNAME_IDX_POSN = 3;
	private static final int PASSWORD_IDX_POSN = 4;
	private static final int NUM_EXPECTED_NONOPTIONS = 5;

	private final OptionParser cmdLineParser = new OptionParser();
	private final OptionSpec<Void> helpOption;
	private int exitStatus = FAIL;
	private File idbFile;
	private boolean areParamatersValid;
	private boolean commandLineRead = false;
	private String databaseName;
	private String serverName;
	private String userName;
	private String password;
	@SuppressWarnings("unused")
	private VisualEditor gui;
	private IPathwayQueryController controller;
	private boolean initialised;


	public PathwayQueryCmd() {
		this.controller = null;
		this.initialised = false;
		this.cmdLineParser.posixlyCorrect(true);
		this.helpOption = this.cmdLineParser.acceptsAll(HELP_OPTIONS, "Display command line usage");
	}
	
	
	public int getExitStatus() {
		return this.exitStatus;
	}

	public static void main(String[] args) {
		PathwayQueryCmd cmd = new PathwayQueryCmd();
		cmd.processParameters(args);
		if(cmd.areParametersValid()){
			cmd.initialiseData();
			if(cmd.isDataInitialised()){
				cmd.runApplication();
			}
			else{
				System.err.println("Error occured initialising data");
			}
		}
		else{
			System.err.println("Error handling parameters");
		}
//		System.exit(cmd.getExitStatus());
	}

	private boolean isDataInitialised() {
		return this.initialised;
	}


	private boolean areParametersValid() {
		return this.areParamatersValid;
	}


	private void processParameters(String[] args){
		try {
			readCommandLine(args);
			if (this.commandLineRead) {
				validateParameters();
			}
		} catch (RuntimeException e) {
			logger.fatal("Error: A bug has been detected.", e);
		} catch (Exception e) {
			logger.fatal("An error was detected", e);
		}
	}
	
	private void initialiseData() {
		if (this.areParamatersValid) {
			PGPoolingDataSource source = new PGPoolingDataSource();
			source.setServerName(serverName);
			source.setDatabaseName(databaseName);
			source.setUser(userName);
			source.setPassword(password);
			source.setMaxConnections(5);
			INotationSubsystemPool pool = new NotationSubsystemPool(); 
			INetworkQueryEngine engine = new NetworkQueryEngine(idbFile, pool);
			controller = new PathwayQueryController(engine, source);
			QueryData queryData = new QueryData();
			queryData.setConfScoreCutoff(new BigDecimal(DEFAULT_THRESHOLD));
			this.controller.setQueryData(queryData);
			this.initialised = true;
		}
	}


	public void runApplication() {
		try {
			gui = new VisualEditor("Query Tool", this.controller);
			this.exitStatus = SUCCESS;
		} catch (RuntimeException e) {
			logger.fatal("Error: A bug has been detected.", e);
		} catch (Exception e) {
			logger.fatal("An error was detected", e);
		}
	}

	private void readCommandLine(String[] args) {
		try {
			OptionSet options = cmdLineParser.parse(args);
			if (options.has(helpOption)) {
				try {
					System.out.println(USAGE);
					this.cmdLineParser.printHelpOn(System.out);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else if (options.nonOptionArguments().size() == NUM_EXPECTED_NONOPTIONS) {
				this.databaseName = options.nonOptionArguments().get(DB_NAME_IDX_POSN);
				this.serverName = options.nonOptionArguments().get(SERVER_NAME_IDX_POSN);
				this.userName = options.nonOptionArguments().get(USERNAME_IDX_POSN);
				this.password = options.nonOptionArguments().get(PASSWORD_IDX_POSN);
				this.idbFile = new File(options.nonOptionArguments().get(IDB_IDX_POSN));
				this.commandLineRead  = true;
			} else if(options.nonOptionArguments().isEmpty()){
				this.databaseName = Messages.getString("PathwayQueryCmd.dbName"); //$NON-NLS-1$;
				this.serverName  = Messages.getString("PathwayQueryCmd.serverName"); //$NON-NLS-1$
				this.userName = Messages.getString("PathwayQueryCmd.userName"); //$NON-NLS-1$
				this.password = Messages.getString("PathwayQueryCmd.password"); //$NON-NLS-1$
				this.idbFile = new File(Messages.getString("PathwayQueryCmd.idb")); //$NON-NLS-1$
				this.commandLineRead  = true;
			}	
			else {
				System.err.println("Invalid arguments: type --help for correct usage."); //$NON-NLS-1$
			}
		} catch (OptionException e) {
			System.err.println(e.getMessage());
		}
	}

	private void validateParameters() {
		this.setParamatersValid(true);
		if (!this.idbFile.isFile() || !this.idbFile.canRead()) {
			logger.fatal(this.idbFile + " is not a file or cannot be read");
			this.setParamatersValid(false);
		}
	}
	
	private void setParamatersValid(boolean areParamatersValid) {
		this.areParamatersValid = areParamatersValid;
	}

}
