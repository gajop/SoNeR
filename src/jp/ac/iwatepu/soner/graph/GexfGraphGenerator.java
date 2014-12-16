package jp.ac.iwatepu.soner.graph;

import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.HashMap;

import jp.ac.iwatepu.soner.DBConnector;
import jp.ac.iwatepu.soner.ranking.Person;
import jp.ac.iwatepu.soner.synonym.SynonymMerge;

public class GexfGraphGenerator {
	
	public static void main(String[] args) {
		GexfGraphGenerator gephiVisualizer = new GexfGraphGenerator();
		gephiVisualizer.run();
	}
	
	public void run() {
		Gexf gexf = new GexfImpl();
		Calendar date = Calendar.getInstance();
		
		gexf.getMetadata()
			.setLastModified(date.getTime())
			.setCreator("SoNeR")
			.setDescription("SoNeR Extracted SocialNetwork");
		gexf.setVisualization(true);

		Graph graph = gexf.getGraph();
		graph.setDefaultEdgeType(EdgeType.DIRECTED).setMode(Mode.STATIC);
		
		AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);
		graph.getAttributeLists().add(attrList);		
	 
		Person[] people;
		int [] knows;
		try {
			people = DBConnector.getInstance().getAllPeople();
			knows = DBConnector.getInstance().getAllKnownRelationships();
		} catch (Exception e1) {			
			e1.printStackTrace();
			return;
		}
		
		HashMap<Integer, Node> nodeMap = new HashMap<Integer, Node>();
		for (int i = 0; i < people.length; ++i) { 
			Person person = people[i];
			Node personNode = graph.createNode(String.valueOf(i));
			personNode.setLabel(person.getLocalURL());
				//	.addValue(attIndegree, "1");
			//gephi.getShapeEntity().setNodeShape(NodeShape.DIAMOND).setUri("GephiURI");
			nodeMap.put(i, personNode);
		}
		
		System.out.println("Merging synonyms...");
		SynonymMerge synMerge = new SynonymMerge(true, true);
		try {
			synMerge.applySynonymsToKnownRelationships(knows);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		for (int i = 0; i < knows.length; i += 2) {
			int person1 = knows[i];
			int person2 = knows[i+1];
			
			try {
				nodeMap.get(person1).connectTo(nodeMap.get(person2));
			} catch (Exception ex) {
				System.err.println("Already exists: " + person1 + " -> " + person2);
			}
		}


		StaxGraphWriter graphWriter = new StaxGraphWriter();
		File f = new File("static_graph_sample.gexf");
		Writer out;
		try {
			out =  new FileWriter(f, false);
			graphWriter.writeToStream(gexf, out, "UTF-8");
			System.out.println(f.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
