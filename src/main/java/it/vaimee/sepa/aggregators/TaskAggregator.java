package it.vaimee.sepa.aggregators;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.sparql.BindingsResults;
import it.unibo.arces.wot.sepa.pattern.Aggregator;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.vaimee.sepa.consumers.my2sec.ActivityConsumer;
import it.vaimee.sepa.consumers.my2sec.TaskConsumer;

public class TaskAggregator extends Aggregator {
    private TaskConsumer taskConsumer;
    private ActivityConsumer activityConsumer;

    public TaskAggregator(JSAP appProfile)
            throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, SEPABindingsException {
        super(appProfile,"GET_SYNCHRONIZATION_FLAG","ADD_TASK");
        taskConsumer = new TaskConsumer(appProfile);
        taskConsumer.subscribe();
        activityConsumer = new ActivityConsumer(appProfile);
        activityConsumer.subscribe();


    }


    //se siamo qui, l'activity consumer ha finito di aggregare le attività di un singolo utente
    @Override
    public void onAddedResults(BindingsResults results) {

    }




    @Override
    public void onUnsubscribe(String spuid){
        try {
            taskConsumer.unsubscribe();
        } catch (SEPASecurityException e) {
            throw new RuntimeException(e);
        } catch (SEPAPropertiesException e) {
            throw new RuntimeException(e);
        } catch (SEPAProtocolException e) {
            throw new RuntimeException(e);
        }

        try {
            activityConsumer.unsubscribe();
        } catch (SEPASecurityException e) {
            throw new RuntimeException(e);
        } catch (SEPAPropertiesException e) {
            throw new RuntimeException(e);
        } catch (SEPAProtocolException e) {
            throw new RuntimeException(e);
        }
    }


}
