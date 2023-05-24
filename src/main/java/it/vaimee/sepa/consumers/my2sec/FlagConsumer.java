package it.vaimee.sepa.consumers.my2sec;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.BindingsResults;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;
import it.unibo.arces.wot.sepa.pattern.Consumer;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.unibo.arces.wot.sepa.logging.Logging;

public class FlagConsumer extends Consumer {

	private BindingsResults sync;
	
    public FlagConsumer(JSAP appProfile,RDFTermURI flag_type, BindingsResults sync)
            throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, SEPABindingsException {
        super(appProfile, "GET_SYNCHRONIZATION_FLAG");

        //System.out.println("Flag Consumer online!");

        this.sync = sync;
        
        setSubscribeBindingValue("flag_type", flag_type);
        subscribe();

    }

    @Override
    public void onAddedResults(BindingsResults res) {
    	//Logging.logger.info("PORCODIO");
        //sync = res;
        for(Bindings bind : res.getBindings()) {
            //RESET SYNC
            for(Bindings syncbind : sync.getBindings()){//for every bind in sync, remove it
                sync.remove(syncbind);
            }
            //NOW SYNC IS EMPTY
            sync.add(bind);
            System.out.println(res);
            System.out.println(sync);
            synchronized(sync) {
                sync.notify();
            }
        }
    }

    /*
    //SOLO PER DEBUGGING POI LEVALA
    @Override
    public void onFirstResults(BindingsResults res) {
        Logging.logger.info("MAO:");
        onAddedResults(res);
    }
    */



    /**
     * //?flag ?user ?timestamp
     * @return
     * @throws InterruptedException
     */
    public synchronized BindingsResults waitFlag() throws InterruptedException {
    	synchronized (sync){
            sync.wait();
        }
    	return sync;
    }


}
