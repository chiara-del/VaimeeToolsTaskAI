package it.vaimee.sepa.consumers.my2sec;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.sparql.*;
import it.unibo.arces.wot.sepa.logging.Logging;
import it.unibo.arces.wot.sepa.pattern.Consumer;
import it.unibo.arces.wot.sepa.pattern.JSAP;

import java.util.HashMap;

public class ActivityConsumer extends Consumer {
    //private GenericClient client;
    /*
     * ACTIVITIES:
     * user_uri: http://www.vaimee.it/my2sec/gregorio.monari@vaimee.it
     * 		|-- activity_type: my2sec:Developing
     * 			|-- activity_uri: uuid:10
     * 				|-- duration: 10.3
     */
    private HashMap<RDFTermURI, HashMap<RDFTermURI, HashMap<RDFTermURI,Float>>> activitiesByUser = new HashMap<>();//cache users tasks

    public ActivityConsumer(JSAP appProfile)
            throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
        super(appProfile, "ALL_USERS_ACTIVITIES");
//        client = new GenericClient(appProfile, null);
        //Logging.logger.info("Activity Consumer Online!");
    }


    public HashMap<RDFTermURI,Float> getActivitiesByUserAndType(RDFTermURI user_uri,RDFTermURI activity_type){
        return activitiesByUser.get(user_uri).get(activity_type);
    }


    public HashMap<RDFTermURI,HashMap<RDFTermURI,Float>> getActivitiesByUser(RDFTermURI user_uri){
        return activitiesByUser.get(user_uri);
    }

    @Override
    public void onAddedResults(BindingsResults results){
        System.out.println("ACTIVITIES RECEIVED!!");
        //ADD ITEMS TO CACHE
        addToCache(results);
    }

    @Override
    public void onRemovedResults(BindingsResults results){
        removeFromCache(results);
    }

    //SOLO PER DEBUGGING POI LEVALA

    @Override
    public void onFirstResults(BindingsResults results){
        //Logging.logger.info("RECEIVED FIRST RESULTS");
        //System.out.println(results);
        onAddedResults(results);
    }



    private void addToCache(BindingsResults results){
        //ADD ITEMS TO CACHE
        for (Bindings bindings : results.getBindings()) {
            RDFTermURI user_uri;
            RDFTermURI activity_type;
            RDFTermURI activity_uri;
            RDFTermLiteral duration;
            try {
                user_uri = (RDFTermURI) bindings.getRDFTerm("user_graph");
                activity_type = (RDFTermURI) bindings.getRDFTerm("activity_type");
                activity_uri = (RDFTermURI) bindings.getRDFTerm("nodeid");
                duration = (RDFTermLiteral) bindings.getRDFTerm("duration");
            } catch (SEPABindingsException e1) {
                Logging.logger.error(e1);
                continue;
            }

            if(!activitiesByUser.containsKey(user_uri)){
                activitiesByUser.put(user_uri,new HashMap<RDFTermURI,HashMap<RDFTermURI,Float>>());
            }

            if(!activitiesByUser.get(user_uri).containsKey(activity_type)){
                activitiesByUser.get(user_uri).put(activity_type,new HashMap<RDFTermURI,Float>());
            }

            activitiesByUser.get(user_uri).get(activity_type).put(activity_uri,Float.parseFloat(duration.getValue()));
            Logging.logger.info("Added results to cache, user: "+user_uri.getValue()+", activity: "+activity_type.getValue());
            Logging.logger.info("Activities size: "+activitiesByUser.get(user_uri).get(activity_type).size());
        }



    }

    private void removeFromCache(BindingsResults results){
        //ADD ITEMS TO CACHE
        for (Bindings bindings : results.getBindings()) {
            RDFTermURI user_uri;
            RDFTermURI activity_type;
            RDFTermURI activity_uri;
            try {
                user_uri = (RDFTermURI) bindings.getRDFTerm("user_graph");
                activity_type = (RDFTermURI) bindings.getRDFTerm("activity_type");
                activity_uri = (RDFTermURI) bindings.getRDFTerm("nodeid");
            } catch (SEPABindingsException e1) {
                Logging.logger.error(e1);
                continue;
            }

            if(!activitiesByUser.containsKey(user_uri)){
                continue;
            }
            if(!activitiesByUser.get(user_uri).containsKey(activity_type)){
                continue;
            }
            activitiesByUser.get(user_uri).get(activity_type).remove(activity_uri);

            if(activitiesByUser.get(user_uri).get(activity_type).isEmpty()){
                activitiesByUser.get(user_uri).remove(activity_type);
            }

            if(activitiesByUser.get(user_uri).isEmpty()){
                activitiesByUser.remove(user_uri);
            }

            Logging.logger.info("Removed results from cache, user: "+user_uri.getValue()+", activity: "+activity_type.getValue());
            if(activitiesByUser.containsKey(user_uri)){
                if(activitiesByUser.get(user_uri).containsKey(activity_type)){
                    Logging.logger.info("Activities size: "+activitiesByUser.get(user_uri).get(activity_type).size());
                }else{
                    Logging.logger.info("Activities size: 0");
                }
            }else{
                Logging.logger.info("User cache emptied!");
            }
        }
    }
}
