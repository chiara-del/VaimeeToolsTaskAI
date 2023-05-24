package it.vaimee.sepa.utilities;

import it.unibo.arces.wot.sepa.logging.Logging;

import java.io.File;
import java.util.Optional;

//da qua sto aggiungendo
import org.checkerframework.checker.nullness.qual.NonNull;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.IRI;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
//import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.factory.SWRLAPIFactory;


//PROVO AD AGGIUNGERE QUESTE COSE 24/05/2023
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.exceptions.SWRLRuleEngineException;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.sqwrl.exceptions.SQWRLException;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.sqwrl.SQWRLResultGenerator;
//import org.swrlapi.sqwrl.SQWRLResultHandler;
import org.swrlapi.sqwrl.exceptions.SQWRLException;
//FINO A QUA


import org.swrlapi.exceptions.*;
import org.swrlapi.sqwrl.exceptions.*;
import org.swrlapi.core.SWRLAPIRule;
public class RuleManager {

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


        Logging.logger.info("Importing ontology");
        Optional<String> owlFilename = Optional.of(my2secPROVA);
        Optional<File> owlFile = (owlFilename != null && owlFilename.isPresent()) ? Optional.of(new File(owlFilename.get())) : Optional.<File>empty();

        try{
            // Create an OWL ontology using the OWLAPI
            ontologyManager = OWLManager.createOWLOntologyManager();
            ontology = owlFile.isPresent() ? ontologyManager.loadOntologyFromOntologyDocument(owlFile.get()) : ontologyManager.createOntology();
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e); //ottimizza  sto fatto
        }

        // Create SQWRL query engine using the SWRLAPI
        SQWRLQueryEngine queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(ontology);
        // questo pezzo funzionava ma sto facendo delle prova

        SQWRLResult result;

        try {
            result = queryEngine.runSQWRLQuery("Activity to Task w/ subtask");
            if (result.next())
                if(result.hasLiteralValue("M")){
                    System.out.println("Found literal");
                }else{
                    System.out.println("M is NOT a literal");
                }
                System.out.println("Member: " + result.getLiteral("M").getString());
                System.out.println("Member: " + result.getLiteral("M"));
                System.out.println("Activity: " + result.getLiteral("AV").getString());
                System.out.println("Task: " + result.getLiteral("T").getString());
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


    /*
        // Create OWLOntology instance using the OWLAPI
        Logging.logger.info("Importing ontology");
        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology;
        try{
            ontology = ontologyManager.loadOntologyFromOntologyDocument(new File("C:\\Users\\chiar\\OneDrive\\Desktop\\TESI\\my2sec_ontology_formatted_for_drools_by_GG.owl"));
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e); //ottimizza  sto fatto
        }

        String stringa= ontology.getOntologyID().toString();
        Logging.logger.info("Ontology ID: "+stringa);



        // Create a SWRL rule engine using the SWRLAP
        //SWRLRuleEngine swrlRuleEngine = SWRLAPIFactory.createSWRLRuleEngine(ontology);
        SQWRLQueryEngine queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(ontology);
    */


        // Create an OWLOntologyManager
        /*
        manager = OWLManager.createOWLOntologyManager();
        // Load an OWL ontology from IRI
        // IRI iri = IRI.create("https://raw.githubusercontent.com/vaimee/my2sec/main/0_Ontologies/src/owl/my2sec.ttl");
        try {
            ontology = manager.loadOntologyFromOntologyDocument(new File("C:\\Users\\chiar\\IdeaProjects\\VaimeeTools_taskai\\tools\\src\\main\\java\\it\\vaimee\\sepa\\utilities\\my2sec_ontology.ttl")); //iri);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e); //ottimizza  sto fatto
        }

        try {
            ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(ontology);
        } catch (SWRLRuleEngineException e) {
            throw new RuntimeException(e); //ottimizza  sto fatto
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
