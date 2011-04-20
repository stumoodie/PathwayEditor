/*
  Licensed to the Court of the University of Edinburgh (UofE) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The UofE licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/
package org.pathwayeditor.visualeditor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.UIManager;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.apache.log4j.Logger;


public class PathwayEditorCmd {
	private static final String FILE_OPTION = "file";
	private final Logger logger = Logger.getLogger(this.getClass());
	private static final int SUCCESS = 0;
	private static final int FAIL = 1;
	private static final List<String> HELP_OPTIONS = Arrays.asList("?", "h", "help");
	private static final String USAGE = "program <options> <idb file>"; //$NON-NLS-1$
	private static final int NUM_EXPECTED_NONOPTIONS = 0;
	private final OptionParser cmdLineParser = new OptionParser();
	private final OptionSpec<Void> helpOption;
	private int exitStatus = FAIL;
	private File structureFile;
	private VisualEditor visualEditor;
//	private final boolean initialised;
	private boolean areParamatersValid;
	private boolean commandLineRead = false;


	public PathwayEditorCmd(){
//		this.initialised = false;
		this.cmdLineParser.posixlyCorrect(true);
		this.cmdLineParser.accepts(FILE_OPTION, "Name of diagram file to load on startup").withRequiredArg();
		this.helpOption = this.cmdLineParser.acceptsAll(HELP_OPTIONS, "Display command line usage");
	}

//	public void splashScreen(){
//		new StartupSplashScreen();
//	}
	
	public void runApplication(){
		try {
			IVisualEditorController vec = new VisualEditorController(new NotationSubsystemPool());
			this.visualEditor = new VisualEditor("Pathway Editor", vec);
			if(this.structureFile != null){
				vec.openFile(structureFile);
			}
			this.exitStatus = SUCCESS;
		} catch (RuntimeException e) {
			logger.fatal("Error: A bug has been detected.", e);
		} catch (Exception e) {
			logger.fatal("An error was detected", e);
		}
	}
	
	
	public int getExitStatus() {
		return this.exitStatus;
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

	private void validateParameters() {
		this.setParamatersValid(true);
		if (this.structureFile != null && (!this.structureFile.isFile() || !this.structureFile.canRead())) {
			logger.fatal(this.structureFile + " is not a file or cannot be read");
			this.setParamatersValid(false);
		}
	}
	
	private void setParamatersValid(boolean areParamatersValid) {
		this.areParamatersValid = areParamatersValid;
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
			}
			else if(options.has(FILE_OPTION)){
				String fileName = (String)options.valueOf(FILE_OPTION);
				this.structureFile = new File(fileName);
				this.commandLineRead  = true;
			}
			else if (options.nonOptionArguments().size() == NUM_EXPECTED_NONOPTIONS) {
				this.structureFile = null;
				this.commandLineRead  = true;
			}
			else {
				System.err.println("Invalid arguments: type --help for correct usage."); //$NON-NLS-1$
			}
		} catch (OptionException e) {
			System.err.println(e.getMessage());
		}
	}

	public static final void main(String args[]){
		try {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "EPEe");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			PathwayEditorCmd cmd = new PathwayEditorCmd();
			cmd.processParameters(args);
			if(cmd.areParametersValid()){
				cmd.runApplication();
			}
			else{
				System.err.println("Error handling parameters");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
