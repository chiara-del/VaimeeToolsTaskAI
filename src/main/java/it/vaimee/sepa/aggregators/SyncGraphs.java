package it.vaimee.sepa.aggregators;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.BindingsResults;
import it.unibo.arces.wot.sepa.logging.Logging;
import it.unibo.arces.wot.sepa.pattern.Aggregator;
import it.unibo.arces.wot.sepa.pattern.JSAP;

public class SyncGraphs extends Aggregator {

	public SyncGraphs(JSAP appProfile, String subscribeID, String updateID)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
		super(appProfile, subscribeID, updateID);
	}

	@Override
	public void onFirstResults(BindingsResults results) {
		onAddedResults(results);
	}
	
	@Override
	public void onAddedResults(BindingsResults results) {
		Logging.logger.debug("Added results " + results);

		int tot = results.getBindings().size();
		int i = 1;
		for (Bindings binding : results.getBindings()) {
			Logging.logger.info("("+i+++"/"+tot+") "+binding);
			// boolean settingsSuccess = true;

			for (String variable : binding.getVariables()) {
				try {
					setUpdateBindingValue(variable, binding.getRDFTerm(variable));
				} catch (SEPABindingsException e) {
					Logging.logger.error("setUpdateBindingValue " + variable);
					// settingsSuccess = false;
				}
			}

			// if (settingsSuccess)
			try {
				update();
			} catch (SEPASecurityException | SEPAProtocolException | SEPAPropertiesException
					| SEPABindingsException e) {
				Logging.logger.error("update " + binding);
			}
		}
	}
}
