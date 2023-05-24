package it.vaimee.tools.simulating;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.response.Response;
import it.unibo.arces.wot.sepa.commons.security.OAuthProperties;
import it.unibo.arces.wot.sepa.commons.security.ClientSecurityManager;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.vaimee.sepa.producers.TSX1SimSingleUpdate;
import it.vaimee.tools.ITool;

public class MonasSimulator extends ITool {
	private static ArrayList<TSX1SimSingleUpdate> devices = new ArrayList<>();

	public MonasSimulator(JSAP appProfile)
			throws SEPAProtocolException, SEPASecurityException {
		super(appProfile);
	}
	
	@Override
	public void start()
			throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, SEPABindingsException {
		logger.info("Starting devices...");
		Set<Entry<String, JsonElement>> pool = appProfile.getExtendedData().getAsJsonObject("devices").entrySet();
		logger.info("Number of devices: " + pool.size());
		for (Entry<String, JsonElement> device : pool) {
			String urn = device.getKey();
			long sleep = appProfile.getExtendedData().getAsJsonObject("devices").getAsJsonObject(urn).get("sleep")
					.getAsLong();
			String graph = appProfile.getExtendedData().getAsJsonObject("devices").getAsJsonObject(urn).get("graph")
					.getAsString();
			String foi = appProfile.getExtendedData().getAsJsonObject("devices").getAsJsonObject(urn).get("foi")
					.getAsString();
			JsonArray sensors = appProfile.getExtendedData().getAsJsonObject("devices").getAsJsonObject(urn).getAsJsonArray("sensors");
			ArrayList<String> sensorUris = new ArrayList<String>();
			for (JsonElement e : sensors) {
				sensorUris.add(e.getAsString());
			}
			
			logger.info("Starting simulator: " + urn+ " sleep: "+sleep+" graph: "+graph);

			ClientSecurityManager sm = null;
			JSAP deviceConfig;
			
			if (appProfile.isSecure()) {
				deviceConfig = new JSAP(urn.replace(':', '.') + ".jsap");
				OAuthProperties oauth = deviceConfig.getAuthenticationProperties();
				
				sm = new ClientSecurityManager(oauth);
				
				if (!appProfile.getAuthenticationProperties().isClientRegistered()) {
					Response ret = sm.registerClient(urn.replace(':', '.'),oauth.getUsername(),oauth.getInitialAccessToken());
					if (ret.isError())
						logger.error(ret);
				}

				sm.refreshToken();
				appProfile.getAuthenticationProperties().storeProperties();
			}
			else deviceConfig = appProfile;

			devices.add(new TSX1SimSingleUpdate(deviceConfig, sensorUris,foi, sleep, graph));
		}

	}

	@Override
	public void stop() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, IOException {
		for (TSX1SimSingleUpdate deviceSim : devices)
			deviceSim.close();

	}

}
