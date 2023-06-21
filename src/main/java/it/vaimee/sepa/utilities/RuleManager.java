package it.vaimee.sepa.utilities;

//UTILS
import it.unibo.arces.wot.sepa.logging.Logging;
import java.io.File;
import java.util.Optional;

//Ontology
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

//Engine
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.exceptions.SQWRLException;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.parser.SWRLParseException;


//Data
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;


//import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.*;

import org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory;


public class RuleManager {
    /*public class OWL2RLIT extends IntegrationTestBase{
        //boh
    }*/

// classi
    private OWLClass member;
    private OWLClass activityType;
    private OWLClass activity;

    private OWLClass task;

    //OBJ PROP
    private OWLObjectProperty attachedTo;
    private OWLObjectProperty hasActivityType;
    private OWLObjectProperty hasMember;
    private OWLObjectProperty hasTask;

    //INDIVIDUAL
    private OWLNamedIndividual individualTask;
    private OWLNamedIndividual activityTypeIndividual;
    private OWLNamedIndividual individual;
    private OWLNamedIndividual activity1;


    private OWLOntologyManager ontologyManager;
    private OWLOntology ontology;
    private SQWRLQueryEngine queryEngine;
    public RuleManager ()
            throws RuntimeException{

        //String my2secOntologyPath= "C:\\Users\\chiar\\OneDrive\\Desktop\\TESI\\my2sec_ontology_formatted_for_drools_by_GG.owl";
        //String testOntologyFunctionalPath= "C:\\Users\\chiar\\OneDrive\\Desktop\\TESI\\provaOntologiaFuncional.owl";
        //String testOntologyPath= "C:\\Users\\chiar\\OneDrive\\Desktop\\TESI\\provaOntologia.owl";
        //String my2secUPDATED = "C:\\Users\\chiar\\OneDrive\\Desktop\\TESI\\my2secOWLxml.owl";
        String my2secUPDATED2 = "C:\\Users\\chiar\\OneDrive\\Desktop\\TESI\\PROVAAA.owl"; //quì ho provato a specificare nella label di member il rdfs: Litteral
        String my2secPROVA = "C:\\Users\\chiar\\OneDrive\\Desktop\\TESI\\PROVADISTRUTTIBILE.owl";
        String my2secPROVA2 = "C:\\Users\\chiar\\OneDrive\\Desktop\\TESI\\PROVA.DISTRUTTIBILE.ULTIMA.VERSIONE.owl"; //questa dovrebbe essere la versione finita

        String my2secVERO = "C:\\Users\\chiar\\OneDrive\\Desktop\\TESI\\TESI TESI\\ONTOLOGIA\\my2secOWL.owl";
        String my2secVEROFunctional = "C:\\Users\\chiar\\OneDrive\\Desktop\\TESI\\TESI TESI\\ONTOLOGIA\\my2secOWLFunctional.owl";

        Logging.logger.info("Importing ontology");
        Optional<String> owlFilename = Optional.of(my2secVEROFunctional);
        Optional<File> owlFile = (owlFilename != null && owlFilename.isPresent()) ? Optional.of(new File(owlFilename.get())) : Optional.<File>empty();

        //ON CREATION
        //final OWLClass C = Class(iri("Member"));
        //final OWLNamedIndividual I = NamedIndividual(iri("Simone"));
        //OWLFunctionalSyntaxFactory.IRI
        try{
            // Create an OWL ontology using the OWLAPI
            ontologyManager = OWLManager.createOWLOntologyManager();
            ontology = owlFile.isPresent() ? ontologyManager.loadOntologyFromOntologyDocument(owlFile.get()) : ontologyManager.createOntology();
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }



        //ON RUNTIME

        // Create SQWRL query engine using the SWRLAPI
        SQWRLQueryEngine queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(ontology);

        //dichiaro le classi
        member = OWLFunctionalSyntaxFactory.Class(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#Member"));
        task = OWLFunctionalSyntaxFactory.Class(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#Task"));
        activity = OWLFunctionalSyntaxFactory.Class(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#Activity"));
        activityType = OWLFunctionalSyntaxFactory.Class(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#ActivityType"));

        //dichiaro le obj
        attachedTo = OWLFunctionalSyntaxFactory.ObjectProperty(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#attachedTo"));
        hasActivityType = OWLFunctionalSyntaxFactory.ObjectProperty(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#hasActivityType"));
        hasMember = OWLFunctionalSyntaxFactory.ObjectProperty(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#hasMember"));
        hasTask = OWLFunctionalSyntaxFactory.ObjectProperty(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#hasTask")); //questa l'ho creata ma dovrebbe dedurla la rule

        //dichiaro gli individui delle classi
        individualTask = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#Task1"));
        individual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#chiara"));
        activityTypeIndividual = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#Developing"));
        activity1 = OWLFunctionalSyntaxFactory.NamedIndividual(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#activity1"));


        //assiomi
        ontologyManager.addAxiom(ontology,OWLFunctionalSyntaxFactory.ClassAssertion(task,individualTask));
        ontologyManager.addAxiom(ontology,OWLFunctionalSyntaxFactory.ClassAssertion(member,individual));
        ontologyManager.addAxiom(ontology,OWLFunctionalSyntaxFactory.ClassAssertion(activityType,activityTypeIndividual));
        ontologyManager.addAxiom(ontology,OWLFunctionalSyntaxFactory.ClassAssertion(activity,activity1));
        ontologyManager.addAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(hasActivityType,individualTask,activityTypeIndividual));
        ontologyManager.addAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(hasActivityType,activity1,activityTypeIndividual));
        ontologyManager.addAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(hasMember,activity1,individual));
        ontologyManager.addAxiom(ontology, OWLFunctionalSyntaxFactory.ObjectPropertyAssertion(attachedTo,individualTask,individual));



        //addAxiom()
        //-------------17/06 provo a fare add axiom di nuovo

        //final OWLClass C = Class(OWLFunctionalSyntaxFactory.IRI("http://vaimee.com/My2Sec#Member"));

        //final OWLNamedIndividual Task = OWLNamedIndividual.class(C).asOWLObjectProperty();


        //final OWLDataProperty OBJ = OWLDataProperty.createObjectProperty();
        //final OWLNamedIndividual S = OWLNamedIndividual.createIndividual("chiara");
        //addOWLAxioms(ontology, ClassAssertion(C, I));

        /*
        final OWLClass MemberClass = Class(IRI("http://vaimee.com/My2Sec#Member"));
        final OWLNamedIndividual Chiara = createIndividual();


        addOWLAxioms(ontology, ClassAssertion(MemberClass, Chiara));
        while(MemberClass.asOWLNamedIndividual().next()) {
            System.out.println("Member: " + OWLNamedIndividual.class.getCanonicalName());
        }
        */


        SQWRLResult result;
        try {
            result = queryEngine.runSQWRLQuery("Activity to Task w/ subtask");
            while(result.next()){
                System.out.println("Member: " + result.getNamedIndividual("M"));
                // System.out.println("Member: " + result.getLiteral("M"));
                System.out.println("Activity: " + result.getNamedIndividual("AV"));
                System.out.println("Task: " + result.getNamedIndividual("T"));
            }

        //Assert.assertTrue(result.next());

                //System.out.println("Activity: " + result.getLiteral("size").getInteger());
        } catch (SQWRLException e) {
            throw new RuntimeException(e);
        }



        // Process the results of the SQWRL query

        /*
        while (result.next()) {
            System.out.println("member: " + result.getLiteral("M").getString());
            System.out.println("number: " + result.getLiteral("size").getInteger());
        }
        */
    }

    public void parseRule(){
        //System.out.println("PARSING RULE...");
        Logging.logger.info("PARSING RULE");

    }


    private void preprocessInput(){

    }

    private void postprocessOutput(){

    }



}
