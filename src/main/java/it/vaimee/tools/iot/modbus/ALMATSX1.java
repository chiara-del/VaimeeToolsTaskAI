package it.vaimee.tools.iot.modbus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.vaimee.sepa.producers.TSX1ModbusTCP;
import it.vaimee.tools.ITool;

public class ALMATSX1 extends ITool {
	ArrayList<TSX1ModbusTCP> devices = new ArrayList<>();

	public ALMATSX1(JSAP appProfile)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, SEPABindingsException {
		super(appProfile);

		Set<Entry<String, JsonElement>> pool = appProfile.getExtendedData().getAsJsonObject("devices").entrySet();
		logger.info("Number of devices: " + pool.size());
		for (Entry<String, JsonElement> device : pool) {
			String urn = device.getKey();
			JsonObject deviceConfig = appProfile.getExtendedData().getAsJsonObject("devices").getAsJsonObject(urn);
			
			JsonArray sensors = deviceConfig.getAsJsonArray("sensors");
			ArrayList<String> sensorUris = new ArrayList<String>();
			for (JsonElement e : sensors) {
				sensorUris.add(e.getAsString());
			}
			
			JsonObject modbusConfig = appProfile.getExtendedData().getAsJsonObject("devices").getAsJsonObject(urn).getAsJsonObject("modbus");

			devices.add(new TSX1ModbusTCP(appProfile, sensorUris, deviceConfig.get("foi").getAsString(),
					modbusConfig.get("sleep").getAsLong(), deviceConfig.get("graph").getAsString(),
					modbusConfig.get("ip").getAsString(),modbusConfig.get("port").getAsInt()));
		}
	}

	@Override
	public void start()
			throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, SEPABindingsException {
		for (TSX1ModbusTCP deviceModbusTCP : devices)
			deviceModbusTCP.start();

	}

	@Override
	public void stop() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, IOException {
		for (TSX1ModbusTCP deviceModbusTCP : devices)
			deviceModbusTCP.stop();

	}

}
