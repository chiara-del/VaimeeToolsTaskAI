package it.vaimee.sepa.producers;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.msg.ReadInputRegistersRequest;
import com.serotonin.modbus4j.msg.ReadInputRegistersResponse;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.response.Response;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermLiteral;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.unibo.arces.wot.sepa.pattern.Producer;
import it.vaimee.tools.SEPABeans;
import it.vaimee.tools.ToolStatistics;

public class TSX1ModbusTCP extends Producer implements TSX1ModbusTCPMBean {
	protected static final Logger logger = LogManager.getLogger();

	private String valueA = "0", valueB = "0", valueC = "0", valueD = "0";
	private AtomicBoolean runningAtomicBoolean = new AtomicBoolean(true);
	private Thread thread;

	private ToolStatistics statistics = new ToolStatistics();

	ModbusMaster master;

	protected void updateValues() throws ModbusTransportException {
		float[] values = readInputRegistersTest(master, 247, 0, 8);

		valueA = String.valueOf(values[0]);
		valueB = String.valueOf(values[1]);
		valueC = String.valueOf(values[2]);
		valueD = String.valueOf(values[3]);
	}

	private float[] readInputRegistersTest(ModbusMaster master, int slaveId, int start, int len)
			throws ModbusTransportException {
		ReadInputRegistersRequest request = new ReadInputRegistersRequest(slaveId, start, len);
		ReadInputRegistersResponse response = (ReadInputRegistersResponse) master.send(request);

		if (response.isException()) {
			logger.error("Exception response: message=" + response.getExceptionMessage());
		} else {
			byte[] data = response.getData();
			logger.debug(
					String.format("Data 32bit: %02X%02X%02X%02X %02X%02X%02X%02X %02X%02X%02X%02X %02X%02X%02X%02X",
							data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9],
							data[10], data[11], data[12], data[13], data[14], data[15]));

			float[] probes = new float[4];
			byte[] probeA = new byte[4];
			byte[] probeB = new byte[4];
			byte[] probeC = new byte[4];
			byte[] probeD = new byte[4];

			probeA[0] = data[3];
			probeA[1] = data[2];
			probeA[2] = data[1];
			probeA[3] = data[0];
			probes[0] = ByteBuffer.wrap(probeA).getFloat();

			probeB[0] = data[7];
			probeB[1] = data[6];
			probeB[2] = data[5];
			probeB[3] = data[4];
			probes[1] = ByteBuffer.wrap(probeB).getFloat();

			probeC[0] = data[11];
			probeC[1] = data[10];
			probeC[2] = data[9];
			probeC[3] = data[8];
			probes[2] = ByteBuffer.wrap(probeC).getFloat();

			probeD[0] = data[15];
			probeD[1] = data[14];
			probeD[2] = data[13];
			probeD[3] = data[12];
			probes[3] = ByteBuffer.wrap(probeD).getFloat();

			logger.debug(String.format("Probe A 32bit: %02X%02X%02X%02X %.2f", probeA[0], probeA[1], probeA[2],
					probeA[3], ByteBuffer.wrap(probeA).getFloat()));
			logger.debug(String.format("Probe B 32bit: %02X%02X%02X%02X %.2f", probeB[0], probeB[1], probeB[2],
					probeB[3], ByteBuffer.wrap(probeB).getFloat()));
			logger.debug(String.format("Probe C 32bit: %02X%02X%02X%02X %.2f", probeC[0], probeC[1], probeC[2],
					probeC[3], ByteBuffer.wrap(probeC).getFloat()));
			logger.debug(String.format("Probe D 32bit: %02X%02X%02X%02X %.2f", probeD[0], probeD[1], probeD[2],
					probeD[3], ByteBuffer.wrap(probeD).getFloat()));

			return probes;
		}

		return null;
	}

	public void start() {
		try {
			master.init();
		} catch (ModbusInitException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return;
		}

		logger.info("Start polling...");
		thread.start();
	}

	public void stop() {
		runningAtomicBoolean.set(false);
		thread.interrupt();
	}

	// *** For filename and client_id replace ':' with '.' ***
	public TSX1ModbusTCP(JSAP jsap, ArrayList<String> urn, String foi, long sleep, String graph, String ip, int port)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, SEPABindingsException {
		super(jsap, "TSX1_OBSERVATION");

		SEPABeans.registerMBean("SEPA:type=" + this.getClass().getSimpleName(), this);

		setUpdateBindingValue("graph", new RDFTermURI(graph));
		setUpdateBindingValue("foi", new RDFTermURI(foi));

		setUpdateBindingValue("probe1", new RDFTermURI(urn.get(0)));
		setUpdateBindingValue("probe2", new RDFTermURI(urn.get(1)));
		setUpdateBindingValue("probe3", new RDFTermURI(urn.get(2)));
		setUpdateBindingValue("probe4", new RDFTermURI(urn.get(3)));

		// MODBUS INIT
		IpParameters ipParameters = new IpParameters();
		ipParameters.setHost(ip);
		ipParameters.setPort(port);

		ModbusFactory modbusFactory = new ModbusFactory();
		master = modbusFactory.createTcpMaster(ipParameters, false);

		thread = new Thread() {
			public void run() {

				while (runningAtomicBoolean.get()) {
					try {
						Thread.sleep(sleep);
						logger.info("TSX1 FOI: " + foi + " GRAPH: " + graph + " sleep: " + sleep + " ms");
					} catch (InterruptedException e) {
						return;
					}

					try {
						updateValues();
					} catch (ModbusTransportException e1) {
						logger.error(e1.getMessage());
						continue;
					}

					logger.info("ProbeA: " + urn.get(0) + " Value: " + String.valueOf(valueA));
					logger.info("ProbeB: " + urn.get(1) + " Value: " + String.valueOf(valueB));
					logger.info("ProbeC: " + urn.get(2) + " Value: " + String.valueOf(valueC));
					logger.info("ProbeD: " + urn.get(3) + " Value: " + String.valueOf(valueD));

					try {

						setUpdateBindingValue("value1", new RDFTermLiteral(String.valueOf(valueA)));
						setUpdateBindingValue("value2", new RDFTermLiteral(String.valueOf(valueB)));
						setUpdateBindingValue("value3", new RDFTermLiteral(String.valueOf(valueC)));
						setUpdateBindingValue("value4", new RDFTermLiteral(String.valueOf(valueD)));

						statistics.updateStart();
						Response ret = update(5000, 3);
						statistics.updateStop();

						if (ret.isError()) {
							statistics.updateFailed();
							logger.error(ret);
						}
					} catch (SEPABindingsException | SEPASecurityException | SEPAProtocolException
							| SEPAPropertiesException e) {
						logger.error(e.getMessage());
						continue;
					}
				}
			}
		};
		thread.setName(foi);
	}

	@Override
	public String getUpdateID() {
		return SPARQL_ID;
	}

	@Override
	public String getUpdateHost() {
		return appProfile.getUpdateHost(SPARQL_ID);
	}

	@Override
	public long getFailedUpdates() {
		return statistics.getFailedUpdates();
	}

	@Override
	public long getUpdates() {
		return statistics.getUpdates();
	}

	@Override
	public long getAverageUpdateTime() {
		return statistics.getUpdateAverageTime();
	}

	@Override
	public long getMaxUpdateTime() {
		return statistics.getUpdateMaxTime();
	}

	@Override
	public long getMinUpdateTime() {
		return statistics.getUpdateMinTime();
	}
}
