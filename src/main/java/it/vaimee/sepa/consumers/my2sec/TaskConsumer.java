package it.vaimee.sepa.consumers.my2sec;


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

import java.util.HashMap;
import java.util.HashSet;

public class TaskConsumer extends Consumer {

    /*
    * TASKS:
    * user_uri: http://www.vaimee.it/my2sec/gregorio.monari@vaimee.it
    * activity_type: my2sec:Developing
    * task_uri: uuid:10....
    */
    private HashMap<RDFTermURI, HashMap<RDFTermURI,HashSet<RDFTermURI>>> tasksByUser = new HashMap<>();//cache users tasks
    public TaskConsumer(JSAP appProfile)
            throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
        super(appProfile, "ALL_OP_TASKS");
    }

    @Override
    public void onAddedResults(BindingsResults results) {
        //System.out.println("TASKS RECEIVED!!");
        this.addToCache(results);
    }

    //TEMPORARY FOR DEBUGGING; REMOVE LATER
    /*
    @Override
    public void onFirstResults(BindingsResults results) {
        //System.out.println("TASKS RECEIVED!!");
        //System.out.println(results);
        this.onAddedResults(results);
    }*/


    @Override
    public void onRemovedResults(BindingsResults results){
        this.removeFromCache(results);
    }

    public HashMap<RDFTermURI,HashSet<RDFTermURI>> getTasksByUser(RDFTermURI user_uri){
        Logging.logger.info("Getting tasks of user: "+user_uri.getValue());
        for(RDFTermURI taskname : tasksByUser.keySet()){
            Logging.logger.info("Avaiable users: "+taskname.getValue());
        }
        return tasksByUser.get(user_uri);
    }
    
    private void addToCache(BindingsResults results){
        System.out.println(results);
        //ADD ITEMS TO CACHE
        for (Bindings bindings : results.getBindings()) {
            RDFTermURI user_uri;
            RDFTermURI activity_type;
            RDFTermURI activity_uri;
            try {
                /*
                user_uri = (RDFTermURI) bindings.getRDFTerm("user_graph");
                activity_type = (RDFTermURI) bindings.getRDFTerm("activity_type");
                activity_uri = (RDFTermURI) bindings.getRDFTerm("title");
                */
                user_uri = (RDFTermURI) bindings.getRDFTerm("assignee");
                activity_type = (RDFTermURI) bindings.getRDFTerm("tasktitle");
                activity_uri = (RDFTermURI) bindings.getRDFTerm("bnode");
                System.out.println("Adding task: user"+user_uri.getValue()+" activity_type "+activity_type.getValue()+" activity_uri "+activity_uri.getValue());
            } catch (SEPABindingsException e1) {
                Logging.logger.error(e1);
                continue;
            }

            if(!tasksByUser.containsKey(user_uri)){
                tasksByUser.put(user_uri,new HashMap<RDFTermURI, HashSet<RDFTermURI>>());
            }

            if(!tasksByUser.get(user_uri).containsKey(activity_type)){
                tasksByUser.get(user_uri).put(activity_type,new HashSet<RDFTermURI>());
            }

            tasksByUser.get(user_uri).get(activity_type).add(activity_uri);
            //Logging.logger.info(tasksByUser.get(user_uri).get(activity_type));
        }
        Logging.logger.info("Added tasks to cache");
    }

    private void removeFromCache(BindingsResults results){
        //ADD ITEMS TO CACHE
        System.out.println("REMOVING TASK FROM CACHE");
        for (Bindings bindings : results.getBindings()) {
            RDFTermURI user_uri;
            RDFTermURI activity_type;
            RDFTermURI activity_uri;
            try {
                /*
                user_uri = (RDFTermURI) bindings.getRDFTerm("user_graph");
                activity_type = (RDFTermURI) bindings.getRDFTerm("activity_type");
                activity_uri = (RDFTermURI) bindings.getRDFTerm("title");
                */
                user_uri = (RDFTermURI) bindings.getRDFTerm("assignee");
                activity_type = (RDFTermURI) bindings.getRDFTerm("tasktitle");
                activity_uri = (RDFTermURI) bindings.getRDFTerm("bnode");
            } catch (SEPABindingsException e1) {
                Logging.logger.error(e1);
                continue;
            }

            if(!tasksByUser.containsKey(user_uri)){
                continue;
            }
            if(!tasksByUser.get(user_uri).containsKey(activity_type)){
                continue;
            }
            tasksByUser.get(user_uri).get(activity_type).remove(activity_uri);

            if(tasksByUser.get(user_uri).get(activity_type).isEmpty()){
                tasksByUser.get(user_uri).remove(activity_type);
            }

            if(tasksByUser.get(user_uri).isEmpty()){
                tasksByUser.remove(user_uri);
            }

        }
    }

}
