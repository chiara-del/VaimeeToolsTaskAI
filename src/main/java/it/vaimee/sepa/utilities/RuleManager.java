package it.vaimee.sepa.utilities;

//UTILS
import it.unibo.arces.wot.sepa.logging.Logging;
import java.io.File;
import java.util.Optional;

//Ontology
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

//Engine
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.exceptions.SQWRLException;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.parser.SWRLParseException;


//Data


//import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.*;

import org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory;


public class RuleManager {
    private OWLClass member;
    private OWLClass activityType;
    private OWLClass activity;

    private OWLClass task;

    //OBJ PROP
    private OWLObjectProperty attachedTo;
    private OWLObjectProperty hasActivityType;
    private OWLObjectProperty hasMember;
    private OWLObjectProperty hasTask;

    //datatype
    private OWLDataProperty numericDuration;



    private OWLOntologyManager ontologyManager;
    private OWLOntology ontology;
    private SQWRLQueryEngine queryEngine;
    public RuleManager (String ontologyPath)
            throws RuntimeException{
        //Import ontology
        if(ontologyPath==null) throw new RuntimeException("Ontology Path cannot be null!");
        Logging.logger.info("Importing ontology from file: "+ontologyPath);
        Optional<String> owlFilename = Optional.of(ontologyPath);
        Optional<File> owlFile = (owlFilename != null && owlFilename.isPresent()) ? Optional.of(new File(owlFilename.get())) : Optional.<File>empty();

        //Create ontology
        try{
            // Create an OWL ontology using the OWLAPI
            ontologyManager = OWLManager.createOWLOntologyManager();
            ontology = owlFile.isPresent() ? ontologyManager.loadOntologyFromOntologyDocument(owlFile.get()) : ontologyManager.createOntology();
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }

        // Create SQWRL query engine using the SWRLAPI
        queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(ontology);

        //Dichiaro classi e le proprietà
        member = OWLFunctionalSyntaxFactory.Class(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#Member"));
        task = OWLFunctionalSyntaxFactory.Class(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#Task"));
        activity = OWLFunctionalSyntaxFactory.Class(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#Activity"));
        activityType = OWLFunctionalSyntaxFactory.Class(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#ActivityType"));
        attachedTo = OWLFunctionalSyntaxFactory.ObjectProperty(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#attachedTo"));
        hasActivityType = OWLFunctionalSyntaxFactory.ObjectProperty(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#hasActivityType"));
        hasMember = OWLFunctionalSyntaxFactory.ObjectProperty(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#hasMember"));
        numericDuration = OWLFunctionalSyntaxFactory.DataProperty(OWLFunctionalSyntaxFactory.IRI("http://www.w3.org/2006/time#numericDuration"));
        hasTask = OWLFunctionalSyntaxFactory.ObjectProperty(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#hasTask")); //questa l'ho creata ma dovrebbe dedurla la rule
        /*
        SQWRLResult result;
        try {
            result = queryEngine.runSQWRLQuery("Activity to Task w/ subtask");
            while(result.next()) {
                System.out.println("Member: " + result.getNamedIndividual("M"));
                // System.out.println("Member: " + result.getLiteral("M"));
                System.out.println("Activity: " + result.getNamedIndividual("AV"));
                System.out.println("Task: " + result.getNamedIndividual("T"));
            }
        } catch (SQWRLException e) {
            throw new RuntimeException(e);
        }*/

    }




    public void add_member(String usergraph){
        OWLNamedIndividual individual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(usergraph));
        ontologyManager.addAxiom(ontology,OWLFunctionalSyntaxFactory.ClassAssertion(member,individual));
    }
    public void remove_member(String usergraph){
        OWLNamedIndividual individual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(usergraph));
        ontologyManager.removeAxiom(ontology,OWLFunctionalSyntaxFactory.ClassAssertion(member,individual));
        Logging.logger.trace("ho rimosso member");
    }
    public void add_task(String usergraph,String taskUri, String activity_type){
        OWLNamedIndividual individual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(usergraph));
        OWLNamedIndividual individualTask = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(taskUri));
        OWLNamedIndividual activityTypeIndividual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(activity_type));
        ontologyManager.addAxiom(ontology,OWLFunctionalSyntaxFactory.ClassAssertion(task,individualTask));
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //QUI C'E' UN WORKAROUND: DICHIARO activityTypeIndividual solo in add_task, serve anche in add_activity ma lo faccio solo una volta qui
        //SE arrivano attività il quale activity_type non è presente tra le task,
        ontologyManager.addAxiom(ontology, OWLFunctionalSyntaxFactory.ClassAssertion(activityType,activityTypeIndividual));
        ontologyManager.addAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(hasActivityType,individualTask,activityTypeIndividual));
        ontologyManager.addAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(attachedTo,individualTask,individual));
    }
    public void remove_task(String usergraph,String taskUri, String activity_type){
        OWLNamedIndividual individual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(usergraph));
        OWLNamedIndividual individualTask = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(taskUri));
        OWLNamedIndividual activityTypeIndividual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(activity_type));
        ontologyManager.addAxiom(ontology,OWLFunctionalSyntaxFactory.ClassAssertion(task,individualTask));

        //questi sono i remove
        ontologyManager.removeAxiom(ontology, OWLFunctionalSyntaxFactory.ClassAssertion(activityType,activityTypeIndividual));
        ontologyManager.removeAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(hasActivityType,individualTask,activityTypeIndividual));
        ontologyManager.removeAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(attachedTo,individualTask,individual));
        Logging.logger.trace("ho rimosso task");
    }
    public void add_activity(String usergraph, String activityUri, String activity_type, String duration){
        OWLNamedIndividual individual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(usergraph));
        OWLNamedIndividual activityTypeIndividual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(activity_type));
        OWLNamedIndividual activity1 = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(activityUri));
        OWLLiteral individualDuration = OWLFunctionalSyntaxFactory.Literal(duration);
        //OWLDataProperty individualDuration = OWLFunctionalSyntaxFactory.Literal();
        ontologyManager.addAxiom(ontology,OWLFunctionalSyntaxFactory.ClassAssertion(activityType,activityTypeIndividual));
        ontologyManager.addAxiom(ontology,OWLFunctionalSyntaxFactory.ClassAssertion(activity,activity1));
        ontologyManager.addAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(hasActivityType,activity1,activityTypeIndividual));
        ontologyManager.addAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(hasMember,activity1,individual));
        ontologyManager.addAxiom(ontology, OWLFunctionalSyntaxFactory.DataPropertyAssertion(numericDuration, activity1,individualDuration));
    }
    public void remove_activity(String usergraph, String activityUri, String activity_type, String duration){
        //vediamo se ricreando gli individui funziona
        OWLNamedIndividual individual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(usergraph));
        OWLNamedIndividual activityTypeIndividual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(activity_type));
        OWLNamedIndividual activity1 = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(activityUri));
        OWLLiteral individualDuration = OWLFunctionalSyntaxFactory.Literal(duration);

        //questi sono i remove
        ontologyManager.removeAxiom(ontology, OWLFunctionalSyntaxFactory.ClassAssertion(activityType,activityTypeIndividual));
        ontologyManager.removeAxiom(ontology, OWLFunctionalSyntaxFactory.ClassAssertion(activity,activity1));
        ontologyManager.removeAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(hasActivityType,activity1,activityTypeIndividual));
        ontologyManager.removeAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(hasMember,activity1,individual));
        ontologyManager.removeAxiom(ontology, OWLFunctionalSyntaxFactory.DataPropertyAssertion(numericDuration, activity1,individualDuration));
        Logging.logger.trace("ho rimosso activity");
    }


    //COPY
    /*
    public void add_member(String usergraph){
        OWLNamedIndividual individual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(usergraph));
        ontologyManager.addAxiom(ontology,OWLFunctionalSyntaxFactory.ClassAssertion(member,individual));
    }
    public void remove_member(String usergraph){
        OWLNamedIndividual individual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(usergraph));
        ontologyManager.removeAxiom(ontology,OWLFunctionalSyntaxFactory.ClassAssertion(member,individual));
    }
    public void add_task(String usergraph,String taskUri, String activity_type){
        OWLNamedIndividual individual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(usergraph));
        OWLNamedIndividual individualTask = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(taskUri));
        OWLNamedIndividual activityTypeIndividual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(activity_type));
        ontologyManager.addAxiom(ontology,OWLFunctionalSyntaxFactory.ClassAssertion(task,individualTask));
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //QUI C'E' UN WORKAROUND: DICHIARO activityTypeIndividual solo in add_task, serve anche in add_activity ma lo faccio solo una volta qui
        //SE arrivano attività il quale activity_type non è presente tra le task,
        ontologyManager.addAxiom(ontology, OWLFunctionalSyntaxFactory.ClassAssertion(activityType,activityTypeIndividual));
        ontologyManager.addAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(hasActivityType,individualTask,activityTypeIndividual));
        ontologyManager.addAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(attachedTo,individualTask,individual));
    }
    public void remove_task(String usergraph,String taskUri, String activity_type){
        OWLNamedIndividual individual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(usergraph));
        OWLNamedIndividual individualTask = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(taskUri));
        OWLNamedIndividual activityTypeIndividual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(activity_type));
        ontologyManager.addAxiom(ontology,OWLFunctionalSyntaxFactory.ClassAssertion(task,individualTask));

        //questi sono i remove
        ontologyManager.removeAxiom(ontology, OWLFunctionalSyntaxFactory.ClassAssertion(activityType,activityTypeIndividual));
        ontologyManager.removeAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(hasActivityType,individualTask,activityTypeIndividual));
        ontologyManager.removeAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(attachedTo,individualTask,individual));
    }
    public void add_activity(String usergraph, String activityUri, String activity_type, String duration){
        OWLNamedIndividual individual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(usergraph));
        OWLNamedIndividual activityTypeIndividual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(activity_type));
        OWLNamedIndividual activity1 = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(activityUri));
        OWLLiteral individualDuration = OWLFunctionalSyntaxFactory.Literal(duration);
        //OWLDataProperty individualDuration = OWLFunctionalSyntaxFactory.Literal();
        ontologyManager.addAxiom(ontology,OWLFunctionalSyntaxFactory.ClassAssertion(activityType,activityTypeIndividual));
        ontologyManager.addAxiom(ontology,OWLFunctionalSyntaxFactory.ClassAssertion(activity,activity1));
        ontologyManager.addAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(hasActivityType,activity1,activityTypeIndividual));
        ontologyManager.addAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(hasMember,activity1,individual));
        ontologyManager.addAxiom(ontology, OWLFunctionalSyntaxFactory.DataPropertyAssertion(numericDuration, activity1,individualDuration));
    }
    public void remove_activity(String usergraph, String activityUri, String activity_type, String duration){
        //vediamo se ricreando gli individui funziona
        OWLNamedIndividual individual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(usergraph));
        OWLNamedIndividual activityTypeIndividual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(activity_type));
        OWLNamedIndividual activity1 = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI(activityUri));
        OWLLiteral individualDuration = OWLFunctionalSyntaxFactory.Literal(duration);

        //questi sono i remove
        ontologyManager.removeAxiom(ontology, OWLFunctionalSyntaxFactory.ClassAssertion(activityType,activityTypeIndividual));
        ontologyManager.removeAxiom(ontology, OWLFunctionalSyntaxFactory.ClassAssertion(activity,activity1));
        ontologyManager.removeAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(hasActivityType,activity1,activityTypeIndividual));
        ontologyManager.removeAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(hasMember,activity1,individual));
        ontologyManager.removeAxiom(ontology, OWLFunctionalSyntaxFactory.DataPropertyAssertion(numericDuration, activity1,individualDuration));

    }
        public void save_ontology() throws OWLOntologyStorageException {
            ontologyManager.saveOntology(ontology);
        }
     */


    public SQWRLResult parseRule(){
        //System.out.println("PARSING RULE...");
        Logging.logger.info("PARSING RULE");
        SQWRLResult result;
        try {
            result = queryEngine.runSQWRLQuery("Activity to Task w/ Subtask with Duration");
        } catch (SQWRLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
