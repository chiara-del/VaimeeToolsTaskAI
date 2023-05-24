package it.vaimee.sepa.consumers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.BindingsResults;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTerm;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermBNode;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;
import it.unibo.arces.wot.sepa.logging.Logging;
import it.unibo.arces.wot.sepa.pattern.Consumer;
import it.unibo.arces.wot.sepa.pattern.GenericClient;
import it.unibo.arces.wot.sepa.pattern.JSAP;

public class SepaBridgeConsumer extends Consumer {
	private GenericClient client;
	private HashMap<RDFTermURI, HashMap<RDFTermBNode, RDFTermBNode>> bnodesByGraph = new HashMap<>();
	private HashMap<RDFTermURI, List<Bindings>> patterns = new HashMap<>();
	
	
	public SepaBridgeConsumer(JSAP appProfile)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
		super(appProfile, "BRIDGE");
		client = new GenericClient(appProfile, null);
	}
	
	private String buildTriples(BindingsResults results) {
		String triples = "";
		int i=0;
		bnodesByGraph.clear();
		patterns.clear();
			
		for (Bindings bindings : results.getBindings()) {
			RDFTermURI graph;
			RDFTerm s;
			RDFTerm o;
			try {
				graph = (RDFTermURI) bindings.getRDFTerm("g");
				s = bindings.getRDFTerm("s");
				o = bindings.getRDFTerm("o");
			} catch (SEPABindingsException e1) {
				Logging.logger.error(e1);
				continue;
			}
			
			// Assign blank node to subject "_:bx"
			if(s.isBNode()) {
				RDFTermBNode bNode = (RDFTermBNode) s;
				System.out.println("-------------------------------");
				System.out.println("bNode.getValue(): "+bNode.getValue());
				System.out.println("s.getValue(): "+s.getValue());
				if (!bnodesByGraph.containsKey(graph)) {
					bnodesByGraph.put(graph, new HashMap<>());
					//bnodesByGraph.get(graph).put(bNode, new RDFTermBNode("_:b"+i++)); //SOSTITUITA
					bnodesByGraph.get(graph).put(bNode, new RDFTermBNode("_:"+s.getValue()));
					System.out.println("bNodesByGraph.get(graph).get(bnode): "+bnodesByGraph.get(graph).get(bNode));
				}
				else {
					//if (!bnodesByGraph.get(graph).containsKey(bNode)) bnodesByGraph.get(graph).put(bNode, new RDFTermBNode("_:b"+i++)); //SOSTITUITA
					if (!bnodesByGraph.get(graph).containsKey(bNode)) bnodesByGraph.get(graph).put(bNode, new RDFTermBNode("_:"+s.getValue()));
					System.out.println("bNodesByGraph.get(graph).get(bNode): "+bnodesByGraph.get(graph).get(bNode));
				}
			}
			
			// Assign blank node to object "_:bx"
			if(o.isBNode()) {
				RDFTermBNode bNode = (RDFTermBNode) o;
				if (!bnodesByGraph.containsKey(graph)) {
					bnodesByGraph.put(graph, new HashMap<>());
					//bnodesByGraph.get(graph).put(bNode, new RDFTermBNode("_:b"+i++)); //SOSTITUITA
					bnodesByGraph.get(graph).put(bNode, new RDFTermBNode("_:"+o.getValue()));
				}
				else {
					//if (!bnodesByGraph.get(graph).containsKey(bNode)) bnodesByGraph.get(graph).put(bNode, new RDFTermBNode("_:b"+i++)); //SOSTITUITA
					if (!bnodesByGraph.get(graph).containsKey(bNode)) bnodesByGraph.get(graph).put(bNode, new RDFTermBNode("_:"+o.getValue()));
				}
			}
			
			// Create patterns (logiche)
			if (!patterns.containsKey(graph)) patterns.put(graph, new ArrayList<>());
			
			Bindings temp = new Bindings();
			temp.addBinding("subject", (bnodesByGraph.containsKey(graph) ? (bnodesByGraph.get(graph).containsKey(s) ? bnodesByGraph.get(graph).get(s) : s) : s));
			temp.addBinding("object", (bnodesByGraph.containsKey(graph) ? (bnodesByGraph.get(graph).containsKey(o) ? bnodesByGraph.get(graph).get(o) : o) : o));
			try {
				temp.addBinding("predicate", bindings.getRDFTerm("p"));
			} catch (SEPABindingsException e) {
				Logging.logger.error(e);
				continue;
			}
			patterns.get(graph).add(temp);
				
		}
		
		// Serialize patterns (CONCATENA LE TRIPLE)
		//replace bindings sostituisce "b" alle triple
		for (RDFTermURI graph : patterns.keySet()) { //per ogni grafo...
			Bindings g = new Bindings();
			g.addBinding("graph", graph);
			try {
				String temp = JSAP.replaceBindings("GRAPH ?graph {",g); //sostituisci il nome del grafo (così inizia temp)
				String insert = "";
				for (Bindings b : patterns.get(graph)) { //preleva tutti i bindings di un singolo grafo
					if (insert.equals("")) insert = JSAP.replaceBindings("?subject ?predicate ?object", b);
					else insert = insert + " . " + JSAP.replaceBindings("?subject ?predicate ?object", b); //CONCAT
				}
				temp = temp + insert +"}";
				triples = triples + temp;
			} catch (SEPABindingsException e) {
				Logging.logger.error(e);
				continue;
			}
		}
		
		
		return triples;
	}
	
	@Override
	public void onAddedResults(BindingsResults results) {
		System.out.println("INSERT DATA {"+buildTriples(results)+"}");

		try {
			client.update("BRIDGE","INSERT DATA {"+buildTriples(results)+"}",new Bindings());
		} catch (SEPAProtocolException | SEPASecurityException | IOException | SEPAPropertiesException
				| SEPABindingsException e) {
			Logging.logger.error(e);
		}

	}

}
