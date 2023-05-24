package it.vaimee.sepa.consumers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.BindingsResults;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;
import it.unibo.arces.wot.sepa.logging.Logging;
import it.unibo.arces.wot.sepa.pattern.Consumer;
import it.unibo.arces.wot.sepa.pattern.JSAP;

public class MySqlObservationConsumer extends Consumer {

	private Connection conn = null;
	private final String liveGraph;
	private final String table;
	private final String client_id;
	private final String client_secret;
	private final String url;
	private final String db;
	private final String connectionString;

	private String message() {
		return "URL: " + url + " CLIENT_ID: " + client_id + " GRAPH: " + liveGraph + " --> DB: " + db + " TABLE: "
				+ table;
	}

	public MySqlObservationConsumer(JSAP appProfile)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, SEPABindingsException {
		super(appProfile, "LOGGER");

		try {
			if (appProfile.isSecure()) {
				// NOTE: MAX username length: 32 bytes
				if (appProfile.getAuthenticationProperties().getClientId().length() > 32)
					client_id = appProfile.getAuthenticationProperties().getClientId().substring(0, 32);
				else
					client_id = appProfile.getAuthenticationProperties().getClientId();
				// NOTE: Missing UPPERCASE and special characters in Keycloak client_secret, but required by MySQL
				client_secret = "VAIMEE@" + appProfile.getAuthenticationProperties().getClientSecret();
			} else {
				client_id = appProfile.getExtendedData().getAsJsonObject("logging").getAsJsonObject("mysql")
						.get("client_id").getAsString();
				client_secret = appProfile.getExtendedData().getAsJsonObject("logging").getAsJsonObject("mysql")
						.get("client_secret").getAsString();
			}
		} catch (Exception e) {
			throw new SEPASecurityException(e.getMessage());
		}

		try {
			table = appProfile.getExtendedData().getAsJsonObject("logging").getAsJsonObject("mysql").get("table")
					.getAsString();
			url = appProfile.getExtendedData().getAsJsonObject("logging").getAsJsonObject("mysql").get("url")
					.getAsString();

			db = appProfile.getExtendedData().getAsJsonObject("logging").getAsJsonObject("mysql").get("db")
					.getAsString();

			liveGraph = appProfile.getExtendedData().getAsJsonObject("logging").get("liveGraph").getAsString();
			setSubscribeBindingValue("graph", new RDFTermURI(liveGraph));

		} catch (Exception e) {
			throw new SEPAPropertiesException(e.getMessage());
		}

		try {
			connectionString = "jdbc:mysql://" + url + "/" + db + "?user=" + client_id + "&password=" + client_secret;

			Logging.logger.debug(connectionString);

			conn = DriverManager.getConnection(connectionString);
		} catch (SQLException ex) {
			Logging.logger.error("SQLException: " + ex.getMessage());
			Logging.logger.error("SQLState: " + ex.getSQLState());
			Logging.logger.error("VendorError: " + ex.getErrorCode());
			throw new SEPAProtocolException(ex.getMessage());
		}

	}

	@Override
	public void onAddedResults(BindingsResults results) {
		Logging.logger.debug("Added results " + results);

		Logging.logger.info(message());

		// ?urn ?foi ?timestamp ?value
		for (Bindings res : results.getBindings()) {
			String urn = res.getValue("urn");
			String foi = res.getValue("foi");

			String timestamp = res.getValue("timestamp").replace('T', ' ');
			timestamp = timestamp.substring(0, timestamp.length() - 2);

			String value = res.getValue("value");

			String values = "'" + urn + "','" + foi + "','" + timestamp + "'," + value;
			String sql = "insert into " + table + " (urn,foi,timestamp,value) values (" + values + ")";

			Logging.logger.info(sql);

			Statement statement;
			try {
				statement = conn.createStatement();
				statement.executeUpdate(sql);
				statement.close();
			} catch (SQLException e) {
				Logging.logger.error(e.getMessage());

				try {
					if (conn.isClosed()) {
						Logging.logger.error("Connection is closed. Try to connect...");
						conn = DriverManager.getConnection(connectionString);
					}
				} catch (SQLException ex) {
					Logging.logger.error("SQLException: " + ex.getMessage());
					Logging.logger.error("SQLState: " + ex.getSQLState());
					Logging.logger.error("VendorError: " + ex.getErrorCode());
				}
			}

		}

	}

}
