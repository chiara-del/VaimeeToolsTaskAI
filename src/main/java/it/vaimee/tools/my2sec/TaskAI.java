package it.vaimee.tools.my2sec;

import com.github.jsonldjava.core.RDFDataset;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.BindingsResults;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermLiteral;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTerm;
import it.unibo.arces.wot.sepa.logging.Logging;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.vaimee.sepa.consumers.my2sec.ActivityConsumer;
import it.vaimee.sepa.consumers.my2sec.FlagConsumer;
import it.vaimee.sepa.consumers.my2sec.TaskConsumer;
import it.vaimee.sepa.producers.my2sec.ActivityRemover;
import it.unibo.arces.wot.sepa.pattern.GenericClient;
import it.vaimee.sepa.producers.my2sec.FlagRemover;
import it.vaimee.sepa.producers.my2sec.LogTimeProducer;
import it.vaimee.sepa.utilities.RuleManager;
import it.vaimee.tools.ITool;

import java.io.IOException;
//import java.security.PublicKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

//import org.graalvm.compiler.nodes.java.NewArrayNode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

//new Import
import it.vaimee.sepa.producers.my2sec.FlagProducer;
import it.vaimee.sepa.consumers.my2sec.LogTimeConsumer;
import it.vaimee.sepa.producers.my2sec.ActivityProducer;
import it.vaimee.sepa.producers.my2sec.TaskProducer;

//import ruleManager
import it.vaimee.sepa.utilities.RuleManager;
import org.semanticweb.owlapi.model.IRI;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.sqwrl.exceptions.SQWRLException;

import javax.tools.Tool;
import java.io.IOException;


//THE MASTER
public class TaskAI extends ITool {

    private ActivityConsumer activityConsumer;
    private TaskConsumer taskConsumer;
    private FlagConsumer flagConsumer;
    //private ActivityRemover activityRemover;
    private GenericClient activityRemover;
    private FlagRemover flagRemover;
    private LogTimeProducer logTimeProducer;

    private BindingsResults flag;
    //Dichiaro
    private TaskAI taskAI;
    private ActivityProducer activityProducer;
    private TaskProducer taskProducer;
    private LogTimeConsumer logTimeConsumer;
    private FlagProducer flagProducer;

    private RuleManager ruleManager;


    public TaskAI(JSAP appProfile)
            throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, SEPABindingsException {
        super(appProfile);
        
        activityConsumer = new ActivityConsumer(appProfile);
        taskConsumer = new TaskConsumer(appProfile);

        //WORKAROUND
        String jsonString="{\"head\":{\"vars\":[\"flag\",\"usergraph\",\"timestamp\"]},\"results\":{\"bindings\":[{\"flag\":{\"type\":\"uri\",\"value\":\"urn:uuid:c86646bc-0f24-417b-8120-48db61fb0f4e\"},\"usergraph\":{\"type\":\"uri\",\"value\":\"http://www.vaimee.it/my2sec/defuser@vaimee.it\"},\"timestamp\":{\"type\":\"literal\",\"datatype\":\"http://www.w3.org/2001/XMLSchema#dateTimeStamp\",\"value\":\"2023-01-16T12:40:34.118953Z\"}}]}}";
        JsonObject jobj=new JsonParser().parse(jsonString).getAsJsonObject();
        flag= new BindingsResults(jobj);

        flagConsumer = new FlagConsumer(
                appProfile, //jsap
                new RDFTermURI("http://www.vaimee.it/my2sec/awactivitiesaggregatorflag"), //flag type
                flag //sync
        );
        
        activityRemover = new GenericClient(appProfile, null);//new ActivityRemover(appProfile);
        flagRemover = new FlagRemover(appProfile);
        logTimeProducer = new LogTimeProducer(appProfile);
        
        new TaskAIThread().start();
    }
    
    class TaskAIThread extends Thread {
    	AtomicBoolean running = new AtomicBoolean(true);
    	
    	public void run() {
    		while(running.get()) {
    			try {
					BindingsResults flag = flagConsumer.waitFlag();
                    System.out.println("FLAG TRIGGERED");
					for(Bindings bind : flag.getBindings()) {
						String flagUri = bind.getRDFTerm("flag").getValue();
						String userUri = bind.getRDFTerm("usergraph").getValue();
						String timestamp = bind.getRDFTerm("timestamp").getValue();
						
						Logging.logger.info("New http://www.vaimee.it/my2sec/flags/activityFlag Flag: "+flagUri+" User: "+userUri+" Timestamp: "+timestamp);
						//Remove flag
                        Logging.logger.info("Removing flag: "+flagUri);
                        flagRemover.setUpdateBindingValue("flag", new RDFTermURI(flagUri));
                        flagRemover.update();

						HashMap<RDFTermURI,HashMap<RDFTermURI,Float>> activities = activityConsumer.getActivitiesByUser((RDFTermURI) bind.getRDFTerm("usergraph"));
						HashMap<RDFTermURI,HashSet<RDFTermURI>> tasks = taskConsumer.getTasksByUser((RDFTermURI) bind.getRDFTerm("usergraph"));
						
						aggregate(userUri,activities,tasks);

                        //removeActivities(activities);
						
					}
					
				} catch (InterruptedException e) {

					break;
				} catch (SEPABindingsException e) {
					Logging.logger.warn(e);
					continue;
				} catch (SEPAProtocolException e) {
                    Logging.logger.warn(e);
                    continue;
                } catch (SEPASecurityException e) {
                    Logging.logger.warn(e);
                    continue;
                } catch (SEPAPropertiesException e) {
                    Logging.logger.warn(e);
                    continue;
                }
    		}
    	}
    }

    private void removeActivities(HashMap<RDFTermURI,HashMap<RDFTermURI,Float>> activities)
            throws SEPABindingsException{

        Logging.logger.info("REMOVING ACTIVITIES");
        String stringUri="";
        for(RDFTermURI activity_type : activities.keySet()){
            HashMap<RDFTermURI,Float> activitiesByType=activities.get(activity_type);
            for(RDFTermURI activity_uri : activitiesByType.keySet()){
                stringUri=stringUri+"<"+activity_uri.getValue()+"> ";
                //activityRemover.setUpdateBindingValue("activity", activity_uri);
                //activityRemover.update();
            }
        }

        String leftcut="DELETE {GRAPH <http://vaimee.it/my2sec/activities> {?activity ?p ?o ; my2sec:hasTimeInterval ?d . ?d ?p1 ?o1} } WHERE{ GRAPH <http://vaimee.it/my2sec/activities> {?activity ?p ?o ; my2sec:hasTimeInterval ?d . ?d ?p1 ?o1}";
        String rightcut="}";

        String updateString=leftcut+"VALUES ?activity {"+stringUri+"}"+rightcut;
        Logging.logger.info(updateString);

		try {
			activityRemover.update("REMOVE_ACTIVITY",updateString,new Bindings());
			Logging.logger.info("ACTIVITIES REMOVED!");
		} catch (SEPAProtocolException | SEPASecurityException | IOException | SEPAPropertiesException
				| SEPABindingsException e) {
			Logging.logger.error(e);
		}
    }



    private void aggregate(String userUri, HashMap<RDFTermURI,HashMap<RDFTermURI,Float>> activities,HashMap<RDFTermURI,HashSet<RDFTermURI>> tasks)
            throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, SEPABindingsException {
        Logging.logger.info("####################");
        Logging.logger.info("### AGGREGATING! ###");
        Logging.logger.info("####################");
        Logging.logger.info("To analyze: " + tasks.size() + " tasks");
        Logging.logger.info("To analyze: " + activities.size() + " types of activities");

        // DA QUI IN POI STO MODIFICANDO
        String my2secVEROFunctional = "C:\\Users\\chiar\\OneDrive\\Desktop\\TESI\\TESI TESI\\ONTOLOGIA\\my2secOWLFunctional.owl";
        ruleManager = new RuleManager(my2secVEROFunctional);

        //public void startTest_ruleManager () {
        Logging.logger.info("** running ruleManager Test");
        //modifico la mail dell'utente per farla leggere a SWRL
        int lastIndex = userUri.lastIndexOf("/");
        String userUriTemp = userUri.substring(lastIndex + 1);
        int index = userUriTemp.indexOf("@");
        String leftSide = userUriTemp.substring(0, index);
        String rightSide = userUriTemp.substring(index + 1);
        String user_uri_parsed = leftSide + "___" + rightSide;

        //aggiungo il member
        ruleManager.add_member(user_uri_parsed);
       /* for (RDFTermURI activity_type: tasks.keySet()) {
            ruleManager.add_task(user_uri_parsed,activity_type.getValue(),activity_type);
        }*/

        //con dei cicli entro negli HashMap di activities e tasks per aggiungere attività e task al ruleManager
        //fuori sto ciclando tutti gli activitytype, quindi prendo per ogni activitytype
        for (RDFTermURI activity_type : activities.keySet()){
            //dentro sto prendendo per ogni activitytype e vado a ciclarre sugli uri
            for(RDFTermURI activity_uri : activities.get(activity_type).keySet()) {
                    Logging.logger.info("Validating: " + activity_type.getValue().toString());
                    //capisci qua come accedere
                    ruleManager.add_activity(user_uri_parsed, activity_uri.toString(), activity_type.toString(),activities.get(activity_type).get(activity_uri).toString());
                    //System.out.println("activityUri:" + activity_uri.toString());
                    //System.out.println("activityType:" + activity_type.toString());
                    //System.out.println("duration:" + activities.get(activity_type).get(activity_uri).toString());
            }
        }
        for (RDFTermURI task_type: tasks.keySet()) {
            for(RDFTermURI task_uri : tasks.get(task_type)) {
                ruleManager.add_task(user_uri_parsed,task_uri.toString() , task_type.toString());
                //System.out.println("taskUri:" + task_uri.toString());
                //System.out.println("taskType:" + task_type.toString());
            }
        }
        SQWRLResult result= ruleManager.parseRule();

        HashMap<RDFTermURI,Float> taskDurationCache = new HashMap<RDFTermURI,Float>();
        try {
            while(result.next()) {

                int memberNameIndex=result.getNamedIndividual("M").toString().indexOf(":");
                String memberUri= result.getNamedIndividual("M").toString().substring(memberNameIndex+1);
                if(memberUri.equals(user_uri_parsed)){

                //GET RESULTS
                int newIndex=result.getNamedIndividual("M").toString().indexOf(":");
                String member_uri= result.getNamedIndividual("M").toString().substring(newIndex+1);
                Logging.logger.trace("Member: " + member_uri);
                // System.out.println("Member: " + result.getLiteral("M"));
                Logging.logger.trace("Duration: " + result.getLiteral("time").getValue());
                Logging.logger.trace("Task: " + result.getNamedIndividual("T").getIRI());

                //PREPROCESS
                String newStringifiedTask = result.getNamedIndividual("T").getIRI().toString();
                RDFTermURI taskUri = new RDFTermURI(newStringifiedTask);

                String literalDuration=result.getLiteral("time").getValue();
                Float singleActivityDuration=Float.parseFloat(literalDuration);

                //Abbiamo: uri della task associata all'attività, member, float duration
                //user_uri, taskUri, activityDuration
                if (!taskDurationCache.containsKey(taskUri)){ //result.getNamedIndividual("T").getIRI().equals(task_uri_duration)){
                    //se non contiene la key, creane una nuova
                    taskDurationCache.put(taskUri, singleActivityDuration);
                } else {
                    //aggiungi valore all'entry esistente
                    float newCalculatedDuration = taskDurationCache.get(taskUri) + singleActivityDuration;
                    taskDurationCache.put(taskUri, newCalculatedDuration);
                }

            }else{
                    System.out.println("Ignored result for "+result.getNamedIndividual("M")+", expected: "+user_uri_parsed);
                 }
            }
        } catch (SQWRLException e) {
            throw new RuntimeException(e);
        }
        Logging.logger.info(taskDurationCache);

        //ORA MI CICLO L'HASH MAP E PRODUCO I LOG TIMES
        for(RDFTermURI currTaskUri: taskDurationCache.keySet()){
            System.out.println("task uri term: "+currTaskUri);
            System.out.println("task uri value string: "+currTaskUri.getValue());
            //user uri, currTaskUri, taskLogTime
            Float currTaskLogTime = taskDurationCache.get(currTaskUri);
            Logging.logger.info(">> LOG TIME: "+currTaskLogTime+" TASK URI: "+currTaskUri);
            Logging.logger.info("Updating log time to sepa");
            logTimeProducer.setUpdateBindingValue("usergraph", new RDFTermURI(userUri));
            logTimeProducer.setUpdateBindingValue("task_uri", new RDFTermURI(currTaskUri.getValue()));//new RDFTerm(task_uri.getValue()));
            logTimeProducer.setUpdateBindingValue("log_time", new RDFTermLiteral(Float.toString(currTaskLogTime))); //new RDFTermLiteral(Float.toString(currTaskLogTime)));
            logTimeProducer.update();
            Logging.logger.info("UPDATED!");
        }


        //OLD CODE
        /*
        for (RDFTermURI activity_type : activities.keySet()) {
            //cerca un match tra le task e le attività
            Logging.logger.info("Validating: "+activity_type.getValue());
            Boolean foundTask=false;
            RDFTermURI task_uri=new RDFTermURI(""); //find matching task
            for (RDFTermURI task : tasks.keySet()) {
                System.out.println(task.getValue());
                if(task.getValue().equals(activity_type.getValue())){
                   foundTask=true;
                   HashSet<RDFTermURI> taskSet = tasks.get(task);
                   for(RDFTermURI temp : taskSet){
                        System.out.println(temp.getValue());
                        task_uri=temp;
                   }
                   //task_uri=new RDFTermURI();
                   break;
                }
            }
            //se l'activity type non è presente nelle task assegnate...
            Float log_time=0.0f;
            if(!foundTask){
                //throw new error
                Logging.logger.warn("Invalid activity");
            }else {
                //else
                Logging.logger.info("> Calculating time spent on activity_type: "+activity_type.getValue());
                HashMap<RDFTermURI,Float> activitiesByType=activities.get(activity_type);
                for(RDFTermURI activity_uri : activitiesByType.keySet()){
                    Float activity_duration=activitiesByType.get(activity_uri);
                    log_time=log_time+activity_duration;
                }
                Logging.logger.info(">> LOG TIME: "+log_time+" TASK URI: "+task_uri.getValue());
                Logging.logger.info("Updating log time to sepa");
                logTimeProducer.setUpdateBindingValue("usergraph", new RDFTermURI(userUri));
                logTimeProducer.setUpdateBindingValue("task_uri", task_uri);//new RDFTerm(task_uri.getValue()));
                logTimeProducer.setUpdateBindingValue("log_time", new RDFTermLiteral(Float.toString(log_time)));
                logTimeProducer.update();
                Logging.logger.info("UPDATED!");
            }
        }//end of for()
        */
    }



    @Override
    public void start()
            throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, SEPABindingsException {
        activityConsumer.subscribe();
        flagConsumer.subscribe();
        taskConsumer.subscribe();
    }

    @Override
    public void stop()
            throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, IOException {
        activityConsumer.unsubscribe();
        flagConsumer.unsubscribe();
        taskConsumer.unsubscribe();
        
        activityConsumer.close();
        flagConsumer.close();
        taskConsumer.close();
    }
}
