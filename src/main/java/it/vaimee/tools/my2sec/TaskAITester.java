package it.vaimee.tools.my2sec;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermLiteral;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.vaimee.sepa.producers.my2sec.FlagProducer;
import it.vaimee.tools.ITool;
import it.unibo.arces.wot.sepa.logging.Logging;

//Importa il tool da testare
import it.vaimee.tools.my2sec.TaskAI;
//Importa i moduli per testare il tool
import it.vaimee.sepa.consumers.my2sec.LogTimeConsumer;
import it.vaimee.sepa.producers.my2sec.ActivityProducer;
import it.vaimee.sepa.producers.my2sec.TaskProducer;

//import ruleManager
import it.vaimee.sepa.utilities.RuleManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.sqwrl.exceptions.SQWRLException;

import javax.tools.Tool;
import java.io.IOException;

//Il tester deve quindi, in sequenza:
//1. Far partire il TaskAi e aspettare che si sia iscritto al sepa
//2. Fare un update al sepa con una o due task per un utente a tua scelta (es defuser@vaimee.it)
//3. Fare un secondo update con delle attività, sempre per lo stesso utente
//4. Fare un terzo update con il flag di fine aggregazione
//(il flag deve contenere il nome dell'utente che ha prodotto il flag, lo vedi dal jsap)
//5. Iscriversi ai logtimes e verificare che siano quelli che ci aspettiamo

public class TaskAITester extends ITool{
    //Dichiaro
    private TaskAI taskAI;
    private ActivityProducer activityProducer;
    private TaskProducer taskProducer;
    private LogTimeConsumer logTimeConsumer;
    private FlagProducer flagProducer;

    private RuleManager ruleManager;

    public TaskAITester(JSAP appProfile) throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, SEPABindingsException {  //questo è il costrutture
        super(appProfile);
        //Inizializzo i moduli
        //taskAI = new TaskAI(appProfile);
        //activityProducer = new ActivityProducer(appProfile);
        //taskProducer = new TaskProducer(appProfile);
        //logTimeConsumer = new LogTimeConsumer(appProfile);
        //flagProducer = new FlagProducer(appProfile);
        String my2secVEROFunctional = "C:\\Users\\chiar\\OneDrive\\Desktop\\TESI\\TESI TESI\\ONTOLOGIA\\my2secOWLFunctional.owl";
        ruleManager= new RuleManager(my2secVEROFunctional);
    }

    public void startTest_ruleManager() throws OWLOntologyStorageException {
        Logging.logger.info("** running ruleManager Test");
        String user_uri="http://www.vaimee.it/my2sec/defuser@vaimee.it";
        int lastIndex= user_uri.lastIndexOf("/");
        user_uri=user_uri.substring(lastIndex+1);


        int index=user_uri.indexOf("@");
        String leftSide=user_uri.substring(0,index);
        String rightSide=user_uri.substring(index+1);
        //System.out.println(leftSide+"___"+rightSide);
        String user_uri_parsed=leftSide+"___"+rightSide;
        ruleManager.add_member(user_uri_parsed);
        ruleManager.add_task(user_uri_parsed,"_8jdsid98ud982","http://www.vaimee.it/ontology/my2sec#Researching");
        ruleManager.add_activity(user_uri_parsed,"_ceu8eu298euc20","http://www.vaimee.it/ontology/my2sec#Developing","15.4");
        ruleManager.add_activity(user_uri_parsed,"_dm8328dmj928d9","http://www.vaimee.it/ontology/my2sec#Developing","11.0");
        ruleManager.add_activity(user_uri_parsed,"_dj82i0d82mid27","http://www.vaimee.it/ontology/my2sec#Developing","85.15");
        ruleManager.add_activity(user_uri_parsed,"_du8nndh2nu2929","http://www.vaimee.it/ontology/my2sec#Researching","408.0");
        //SQWRLResult result= ruleManager.parseRule();

        //quì rimuovo dati
        /*
        ruleManager.remove_member(user_uri_parsed);
        ruleManager.remove_task(user_uri_parsed,"_8jdsid98ud982","http://www.vaimee.it/ontology/my2sec#Researching");
        ruleManager.remove_activity(user_uri_parsed,"_ceu8eu298euc20","http://www.vaimee.it/ontology/my2sec#Developing","15.4");
        ruleManager.remove_activity(user_uri_parsed,"_dm8328dmj928d9","http://www.vaimee.it/ontology/my2sec#Developing","11.0");
        ruleManager.remove_activity(user_uri_parsed,"_dj82i0d82mid27","http://www.vaimee.it/ontology/my2sec#Developing","85.15");
        ruleManager.remove_activity(user_uri_parsed,"_du8nndh2nu2929","http://www.vaimee.it/ontology/my2sec#Researching","408.0");
        */

        //ruleManager.save_ontology();
        ruleManager.add_task(user_uri_parsed,"qwerty","http://www.vaimee.it/ontology/my2sec#Developing");
        ruleManager.add_activity(user_uri_parsed,"fgh","http://www.vaimee.it/ontology/my2sec#Developing","15.3");
        ruleManager.add_activity(user_uri_parsed,"asdf","http://www.vaimee.it/ontology/my2sec#Developing","32.0");
        ruleManager.add_activity(user_uri_parsed,"sdfg","http://www.vaimee.it/ontology/my2sec#Developing","18.15");
        ruleManager.add_activity(user_uri_parsed,"zxcv","http://www.vaimee.it/ontology/my2sec#Researching","443.0");

        //quì rimuovo dati
        /*
        ruleManager.remove_task(user_uri_parsed,"qwerty","http://www.vaimee.it/ontology/my2sec#Developing");
        ruleManager.remove_activity(user_uri_parsed,"fgh","http://www.vaimee.it/ontology/my2sec#Developing","15.3");
        ruleManager.remove_activity(user_uri_parsed,"asdf","http://www.vaimee.it/ontology/my2sec#Developing","32.0");
        ruleManager.remove_activity(user_uri_parsed,"sdfg","http://www.vaimee.it/ontology/my2sec#Developing","18.15");
        ruleManager.remove_activity(user_uri_parsed,"zxcv","http://www.vaimee.it/ontology/my2sec#Researching","443.0");
        */

        SQWRLResult result2= ruleManager.parseRule();

        try {
            //int total=0;
            //int pass = 0;
            while(result2.next()) {
                int newIndex=result2.getNamedIndividual("M").toString().indexOf(":");
                String member_uri= result2.getNamedIndividual("M").toString().substring(newIndex+1);

                if(member_uri.equals(user_uri_parsed)){
                    System.out.println("Member: " + member_uri);
                    // System.out.println("Member: " + result.getLiteral("M"));
                    //System.out.println("Activity: " + result.getNamedIndividual("AV"));
                    System.out.println("Task: " + result2.getNamedIndividual("T"));
                    //System.out.println("Duration: "+result.getObjectProperty("time"));
                    String literalDuration=result2.getLiteral("time").getValue();
                    Float numericDuration=Float.parseFloat(literalDuration);
                    System.out.println("the value is:"+ numericDuration.toString());
                    // dentro pass ci devo mettere result.getDataProperty("time") però in modo tale che possa essere sommata al totale
                    // bisogna creare delle durate da poter mettere e fare elaborale alla rule collegate all'utente
                    //total = total + pass;
                }else{
                    System.out.println("Ignored result for "+result2.getNamedIndividual("M")+", expected: "+user_uri_parsed);
                }
            }

        } catch (SQWRLException e) {
            throw new RuntimeException(e);
        }
    }
    public void startTest_taskAi() throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, SEPABindingsException{

        Logging.logger.info("STARTING TASKAI TEST!");
        String user_uri="http://www.vaimee.it/my2sec/defuser@vaimee.it";

        activityProducer.setUpdateBindingValue("usergraph", new RDFTermURI(user_uri));
        activityProducer.setUpdateBindingValue("event_type", new RDFTermURI("http://www.vaimee.it/ontology/my2sec#windowEvent"));
        activityProducer.setUpdateBindingValue("datetimestamp", new RDFTermLiteral("2023-05-02T15:33:42.503000+00:00"));
        activityProducer.setUpdateBindingValue("app", new RDFTermLiteral("chrome.exe"));
        activityProducer.setUpdateBindingValue("title", new RDFTermLiteral("Portainer"));
        activityProducer.setUpdateBindingValue("activity_type", new RDFTermURI("http://www.vaimee.it/ontology/my2sec#Developing"));
        activityProducer.setUpdateBindingValue("task", new RDFTermLiteral("WP2-IMPLEMENTAZIONE COMPONENTI"));
        activityProducer.setUpdateBindingValue("duration", new RDFTermLiteral(Float.toString(16.0F)));
        activityProducer.update();


        taskProducer.setUpdateBindingValue("projecturi", new RDFTermURI("http://www.vaimee.it/projects#demo-project"));
        taskProducer.setUpdateBindingValue("task_id", new RDFTermLiteral("1"));
        taskProducer.setUpdateBindingValue("task_title", new RDFTermURI("http://www.vaimee.it/ontology/my2sec#Developing"));
        taskProducer.setUpdateBindingValue("assignee", new RDFTermURI(user_uri));
        taskProducer.setUpdateBindingValue("spent_time", new RDFTermLiteral(Float.toString(0.0F)));
        taskProducer.update();

        flagProducer.setUpdateBindingValue("usergraph", new RDFTermURI(user_uri));
        flagProducer.setUpdateBindingValue("flag_type", new RDFTermURI("http://www.vaimee.it/my2sec/awactivitiesaggregatorflag"));
        flagProducer.update();
    }

    @Override
    public void stop() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, IOException {
        //taskAI.stop();
        //logTimeConsumer.unsubscribe();
    }

    @Override
    public void start()
            throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, SEPABindingsException {
        Logging.logger.info("----------------< TESTER STARTED >-----------------");
        //taskAI.start();
        //logTimeConsumer.subscribe();
            try {
                startTest_ruleManager();
            } catch (OWLOntologyStorageException e) {
                throw new RuntimeException(e);
            }
    }
}

