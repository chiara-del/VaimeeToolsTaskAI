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
        ruleManager= new RuleManager();
    }

    public void startTest_ruleManager(){
        Logging.logger.info("** running ruleManager Test");
        ruleManager.parseRule();
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
        startTest_ruleManager();
    }
}

