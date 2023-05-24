package it.vaimee.sepa.utilities;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.vaimee.tools.ITool;

public class AWConverter extends ITool {

	public AWConverter(JSAP appProfile) throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
		super(appProfile);

	}

	@Override
	public void start()
			throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, SEPABindingsException {

		switch (appProfile.getExtendedData().get("mode").getAsString()) {
		case "convert":
			try {
				json2CSV(appProfile);
			} catch (IOException e) {
				logger.error(e.getMessage());
				throw new SEPAPropertiesException(e.getMessage());
			}
			break;
		case "fix":
			try {
				fix(appProfile);
			} catch (IOException e) {
				logger.error(e.getMessage());
				throw new SEPAPropertiesException(e.getMessage());
			}
			break;
		}

	}

	@Override
	public void stop() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, IOException {

	}

	private void json2CSV(JSAP appProfile) throws IOException {

		File file = new File(appProfile.getExtendedData().get("out").getAsString());
		FileWriter outputfile = new FileWriter(file);
		CSVWriter writer = new CSVWriter(outputfile);
		String[] header = { "app", "title", "url", "activity_type", "timestamp", "duration" };
		logger.info(
				header[0] + " " + header[1] + " " + header[2] + " " + header[3] + " " + header[4] + " " + header[5]);
		writer.writeNext(header);

		FileReader in = new FileReader(appProfile.getExtendedData().get("in").getAsString());

		JsonObject json = new JsonParser().parse(in).getAsJsonObject();
		for (Entry<String, JsonElement> bucket : json.getAsJsonObject("buckets").entrySet()) {
			JsonArray events = bucket.getValue().getAsJsonObject().get("events").getAsJsonArray();
			logger.info("Events: " + events.size());
			int unknown = 0;
			for (JsonElement event : events) {
				logger.info(event);

				JsonElement app = event.getAsJsonObject().getAsJsonObject("data").get("app");
				JsonElement title = event.getAsJsonObject().getAsJsonObject("data").get("title");
				JsonElement timestamp = event.getAsJsonObject().get("timestamp");
				JsonElement duration = event.getAsJsonObject().get("duration");

				String[] row = { app == null ? "" : app.getAsString(), title == null ? "" : title.getAsString(), "",
						timestamp == null ? "" : timestamp.getAsString(),
						duration == null ? "" : duration.getAsString() };

				if (row[0].equals("unknown") || row[1].equals("unknown")) {
					unknown++;
					continue;
				}

				writer.writeNext(row);
			}

			logger.info("Events: " + events.size() + " Unknown: " + unknown);
		}

		writer.close();
	}
	
	private void fix(JSAP appProfile) throws IOException {
		HashMap<String, String[]> fixHashMap = new HashMap<>();
		
		// Create hashmap from JSON
		FileReader in = new FileReader(appProfile.getExtendedData().get("in").getAsString());
		JsonObject json = new JsonParser().parse(in).getAsJsonObject();
		for (Entry<String, JsonElement> bucket : json.getAsJsonObject("buckets").entrySet()) {
			JsonArray events = bucket.getValue().getAsJsonObject().get("events").getAsJsonArray();
			logger.info("Events: " + events.size());
			int unknown = 0;
			for (JsonElement event : events) {
				logger.info(event);

				JsonElement app = event.getAsJsonObject().getAsJsonObject("data").get("app");
				JsonElement title = event.getAsJsonObject().getAsJsonObject("data").get("title");
				JsonElement timestamp = event.getAsJsonObject().get("timestamp");
				JsonElement duration = event.getAsJsonObject().get("duration");

				String[] row = { app == null ? "" : app.getAsString(), title == null ? "" : title.getAsString(), "",
						timestamp == null ? "" : timestamp.getAsString(),
						duration == null ? "" : duration.getAsString() };

				if (row[0].equals("unknown") || row[1].equals("unknown")) {
					unknown++;
					continue;
				}

				fixHashMap.put(timestamp.getAsString(), row);
			}

			logger.info("Events: " + events.size() + " Unknown: " + unknown);
		}

		
		// WRITER
		File file = new File(appProfile.getExtendedData().get("out").getAsString());
		FileWriter outputfile = new FileWriter(file);
		CSVWriter writer = new CSVWriter(outputfile);
		
		// READER
		File fix = new File(appProfile.getExtendedData().get("fix").getAsString());
		FileReader inputFileReader = new FileReader(fix);
		CSVReader reader =new CSVReaderBuilder(inputFileReader).withCSVParser(new CSVParserBuilder().withSeparator(';').build()).build();
		
		String[] header = { "app", "title", "activity_type", "timestamp", "duration" };
		logger.info(
				header[0] + " " + header[1] + " " + header[2] + " " + header[3] + " " + header[4]);
		
		writer.writeNext(header);
		
		String[] lineString = reader.readNext(); //skip header
		if (lineString!=null) lineString = reader.readNext();
		while(lineString != null) {
			String[] row = fixHashMap.get(lineString[3]);
			lineString[0] = row[0];
			lineString[1] = row[1];
			writer.writeNext(lineString);
			lineString = reader.readNext();
		}
		
		logger.info("With activity type: "+reader.getLinesRead());
		
		reader.close();
		writer.close();
		
		System.exit(0);
	}

}
