package it.vaimee.tools;

import java.io.IOException;
import java.util.regex.PatternSyntaxException;


import it.vaimee.tools.my2sec.TaskAITester;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.vaimee.sepa.init.SepaSync;
import it.vaimee.sepa.performance.PerfMonitor;
import it.vaimee.sepa.utilities.AWConverter;
import it.vaimee.tools.iot.modbus.ALMATSX1;
import it.vaimee.tools.logging.ObservationsLogger;
import it.vaimee.tools.logging.SepaBridge;
import it.vaimee.tools.my2sec.TaskAI;
import it.vaimee.tools.my2sec.TaskAITester;
import it.vaimee.tools.monitoring.RunningMonitor;
import it.vaimee.tools.monitoring.WatchDog;
import it.vaimee.tools.simulating.MonasSimulator;
import it.vaimee.tools.simulating.WeatherAgentSimulator;

public class VaimeeTools extends ITool {
	protected static final Logger logger = LogManager.getLogger();

	private static String jsapFile = null;
	private static JSAP jsap = null;

	private final ITool tool;

	public static void main(String[] args) {
		System.out.println("###################################################");
		System.out.println("#     VAIMEE Tools - The surprising toolkit       #");
		System.out.println("#                                                 #");
		System.out.println("# Developed with love by VAIMEE srl               #");
		System.out.println("# WEB: https://vaimee.it MAIL: info@vaimee.it     #");
		System.out.println("#                                                 #");
		System.out.println("# Powered by SPARQL Event Processing Architecture #");
		System.out.println("# GITHUB: https://github.com/arces-wot/sepa       #");
		System.out.println("# WEB:    http://site.unibo.it/wot                #");
		System.out.println("# WIKI:   https://github.com/arces-wot/SEPA/wiki  #");
		System.out.println("#                                                 #");
		System.out.println("# Copyright (C) 2020. All rights reserved.        #");
		System.out.println("###################################################");

		// Arguments
		parsingArgument(args);

		// Load JSAP
		try {
			jsap = new JSAP(jsapFile);
		} catch (SEPAPropertiesException | SEPASecurityException e) {
			logger.error("Failed to parse JSAP. Message:" + e.getMessage());
			if (logger.isTraceEnabled())
				e.printStackTrace();
			printUsage();
			System.exit(-1);
		}

		if (!jsap.getExtendedData().has("app")) {
			logger.error("app not found in JSAP extended data");
			printUsage();
			System.exit(-1);
		}

		// Run the tool
		VaimeeTools tool = null;
		try {
			tool = new VaimeeTools(jsap);
		} catch (SEPAProtocolException | SEPASecurityException | SEPAPropertiesException | SEPABindingsException e) {
			logger.error("Failed to create tool " + e.getMessage());
			if (logger.isTraceEnabled())
				e.printStackTrace();
			printUsage();
			System.exit(-1);
		}

		// Welcome message
		welcome();

		try {
			tool.start();
		} catch (SEPASecurityException | SEPAPropertiesException | SEPAProtocolException | SEPABindingsException e) {
			logger.error(e.getMessage());
			if (logger.isTraceEnabled())
				e.printStackTrace();
			System.exit(-1);
		}

		try {
			tool.stop();
		} catch (SEPASecurityException | SEPAPropertiesException | SEPAProtocolException | IOException e) {
			logger.error(e.getMessage());
			if (logger.isTraceEnabled())
				e.printStackTrace();
			System.exit(-1);
		}
	}

	private static void welcome() {
		System.out.println(" Application name: " + jsap.getExtendedData().get("app").getAsString());
		System.out.println(" JSAP: " + jsapFile);
		System.out.println("###################################################");
		System.out.println(" Up And Running! LeT Things Talk And Data Be Free");
		System.out.println("###################################################");
	}

	private static void printUsage() {
		System.out.println("Usage:");
		System.out.println(
				"       java [JMX] [LOG4J] -jar tools-X.Y.Z.jar [-help] -jsap <filename> [LDAP] [JKS]");
		System.out.println("");
		System.out.println("Arguments: ");
		System.out.println("      -jsap <filename>: the JSON Semantic Application Profile file");

		System.out.println("");
		System.out.println("Options: ");
		System.out.println("      -help : to print this help");

		System.out.println("");
		System.out.println("[JMX]");
		System.out.println("     -Dcom.sun.management.config.file=jmx.properties : to enable JMX remote management");

		System.out.println("");
		System.out.println("[LOG4J]");
		System.out.println("     -Dlog4j.configurationFile=path/to/log4j2.xml");
	}

	private static void parsingArgument(String[] args) throws PatternSyntaxException {
		for (int i = 0; i < args.length; i = i + 2) {
			if (args[i].equals("-help")) {
				printUsage();
				System.exit(0);
			}

			switch (args[i]) {
			case "-jsap":
				jsapFile = args[i + 1];
				break;
			default:
				logger.warn("Unrecognized argument: " + args[i]);
				break;
			}
		}
	}

	public VaimeeTools(JSAP jsap)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, SEPABindingsException {
		super(jsap);

		switch (jsap.getExtendedData().get("app").getAsString().toUpperCase()) {
		case "LOG_OBSERVATIONS":
			tool = new ObservationsLogger(jsap);
			break;
		case "WATCHDOG":
			tool = new WatchDog(jsap);
			break;
		case "MONAS_SIM":
			tool = new MonasSimulator(jsap);
			break;
		case "TSX1_MONITORING":
			tool = new ALMATSX1(jsap);
			break;
		case "PERF":
			tool = new PerfMonitor(jsap);
			break;
		case "MONITOR":
			tool = new RunningMonitor(jsap);
			break;
		case "SYNC":
			tool = new SepaSync(jsap);
			break;
		case "AW_CONVERTER":
			tool = new AWConverter(jsap);
			break;
		case "WEATHER":
			tool = new WeatherAgentSimulator(jsap);
			break;
		case "BRIDGE":
			tool = new SepaBridge(jsap);
			break;
		case "TASK_AGGREGATOR":
			tool = new TaskAI(jsap);
			break;
		case "TASKAI_TESTER":
			tool = new TaskAITester(jsap);
			break;
		default:
			tool = null;
			logger.error("Application ID not found: " + jsap.getExtendedData().get("app").getAsString());
			printUsage();
			System.exit(-1);
		}
	}

	@Override
	public void stop() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, IOException {
		tool.stop();
	}

	@Override
	public void start()
			throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, SEPABindingsException {
		tool.start();

		synchronized (tool) {
			try {
				tool.wait();
			} catch (InterruptedException e) {
				logger.warn(e.getMessage());
				if (logger.isTraceEnabled())
					e.printStackTrace();
			}
		}

	}
}
