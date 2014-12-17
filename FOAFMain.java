package jp.ac.iwatepu.soner.processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import jp.ac.iwatepu.soner.Util;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

//FIXME: This file isn't needed, at all
public class FOAFMain {
	String currentFileName = "";
	PrintWriter pw;
	
	public static void main(String[] args) {
		FOAFMain foaf = new FOAFMain();	
		foaf.run();
	}

	private NxParser getNxParser() {
		try {
			FileInputStream is = new FileInputStream(currentFileName);	
			NxParser nxp = new NxParser(is);
			return nxp;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	private void parseURIs(File [] files) throws IOException {
		int i = 0;
    //	pw = new PrintWriter(new FileOutputStream(Util.getInstance().getOutputDirName() + "/people_uri.csv"));    	
    	for (File file : files) {
    		System.out.println("Processing URI, file " + (i + 1) + "/" + files.length);
    		currentFileName = file.getAbsolutePath();
    		getPeopleURIs(getNxParser());
    		i++;
    		pw.flush();
    	}
    	pw.close();
	}
	
	private void parseNames(File[] files) throws IOException {
		int i = 0;
    //	pw = new PrintWriter(new FileOutputStream(Util.getInstance().getOutputDirName() + "/people_name.csv"));
    	for (File file : files) {
    		System.out.println("Processing name, file " + (i + 1) + "/" + files.length);
    		currentFileName = file.getAbsolutePath();
    		getPeopleNames(getNxParser());
    		i++;
    		pw.flush();
    	}
    	pw.close();
	}
	
	private void parseKnownPeople(File[] files) throws IOException {
		int i = 0;
    //	pw = new PrintWriter(new FileOutputStream(Util.getInstance().getOutputDirName() + "/people_known.csv"));
    	for (File file : files) {
    		System.out.println("Processing known URL, file " + (i + 1) + "/" + files.length);
    		currentFileName = file.getAbsolutePath();
    		getKnowsURL(getNxParser());
    		i++;
    		pw.flush();
    	}
    	pw.close();
	}
	
	private void parseSynonyms(File[] files) throws IOException {
    	int i = 0;
    //	pw = new PrintWriter(new FileOutputStream(Util.getInstance().getOutputDirName() + "/people_synonym.csv"));
    	for (File file : files) {
    		System.out.println("Processing synonym, file " + (i + 1) + "/" + files.length);
    		currentFileName = file.getAbsolutePath();
    		getSynonyms(getNxParser());
    		i++;
    		pw.flush();
    	}
    	pw.close();
	}
	
	private void parseSameAs(File[] files) throws IOException {
    	int i = 0;
    	//pw = new PrintWriter(new FileOutputStream(Util.getInstance().getOutputDirName() + "/people_same_as.csv"));
    	for (File file : files) {
    		System.out.println("Processing same_as, file " + (i + 1) + "/" + files.length);
    		currentFileName = file.getAbsolutePath();
    		getSameAs(getNxParser());
    		i++;
    		pw.flush();
    	}
    	pw.close();
	}
	
	private void parseCustomTag(File[] files, String tag) throws IOException {
    	int i = 0;
    //	pw = new PrintWriter(new FileOutputStream(Util.getInstance().getOutputDirName() + "/people_" + tag + ".csv"));
    	for (File file : files) {
    		System.out.println("Processing " + tag + ", file " + (i + 1) + "/" + files.length);
    		currentFileName = file.getAbsolutePath();
    		getCustomTag(getNxParser(), tag);
    		i++;
    		pw.flush();
    	}
    	pw.close();
	}
	
	private void getCustomTag(NxParser nxp, String tag) throws FileNotFoundException, IOException {
		boolean once = true;
		for (Node[] nxx; nxp.hasNext();) {
		     nxx = nxp.next();
		     String nxx0;
		     String nxx1;
		     String nxx2;
		     String nxx3;
		     try {
			     nxx0 = nxx[0].toString();
			     nxx1 = nxx[1].toString();
			     nxx2 = nxx[2].toString();
			     nxx3 = nxx[3].toString();
		     } catch (Exception ex) {	
		    	 //input parsing error, ignore it
		    	 continue;
		     }		     
		     if (nxx1.equals("http://xmlns.com/foaf/0.1/" + tag)) {
		    	 if (once) {
		    		 System.out.println(nxx1);
		    		 once = false;
		    	 }
		    	 pw.println(nxx0);		    	 
		    	 pw.println(nxx3);
		    	 pw.println(nxx2);
		    	 if (Util.getInstance().isDEBUG()) {
		    		 System.out.println(tag + " " + nxx0 + ", " + nxx2 + ", " + nxx3);
		    	 }
		     }		     
		}
	}

	public void run() {
		try {		
			File inputDir = new File(Util.getInstance().getInputDirName());
	    	File[] files = inputDir.listFiles();
			parseURIs(files);
			parseNames(files);
			parseKnownPeople(files);
			parseSynonyms(files);
			parseSameAs(files);
	    	
	    	for (String tag : Util.getInstance().getTags()) {
	    		parseCustomTag(files, tag);
	    	}
			System.out.println("Finished!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void getKnowsURL(NxParser nxp) throws FileNotFoundException, IOException {
		String knowsURI = "http://xmlns.com/foaf/0.1/knows";
		for (Node[] nxx; nxp.hasNext();) {
		     nxx = nxp.next();
		     String nxx0;
		     String nxx1;
		     String nxx2;
		     String nxx3;
		     try {
			     nxx0 = nxx[0].toString();
			     nxx1 = nxx[1].toString();
			     nxx2 = nxx[2].toString();
			     nxx3 = nxx[3].toString();
		     } catch (Exception ex) {
		    	 //input parsing error, ignore it
		    	 continue;
		     }		     		     
		     if (nxx1.equals(knowsURI)) {
		    	 pw.println(nxx0);
		    	 pw.println(nxx2);
		    	 pw.println(nxx3);
		     }
		}
	}
	
	private void getPeopleNames(NxParser nxp) throws FileNotFoundException, IOException {
		String nameURI = "http://xmlns.com/foaf/0.1/name";
		for (Node[] nxx; nxp.hasNext();) {
		     nxx = nxp.next();
		     String nxx0;
		     String nxx1;
		     String nxx2;
		     String nxx3;
		     try {
			     nxx0 = nxx[0].toString();
			     nxx1 = nxx[1].toString();
			     nxx2 = nxx[2].toString();
			     nxx3 = nxx[3].toString();
		     } catch (Exception ex) {
		    	 //input parsing error, ignore it
		    	 continue;
		     }		     		     
		     if (nxx1.equals(nameURI)) {
		    	 pw.println(nxx0);
		    	 pw.println(nxx2);
		    	 pw.println(nxx3);
		     }
		}
	}
	
	public void getPeopleURIs(NxParser nxp) throws FileNotFoundException, IOException {
		String personURI = "http://xmlns.com/foaf/0.1/Person";
		for (Node[] nxx; nxp.hasNext();) {
		     nxx = nxp.next();
		     String nxx0;
		     String nxx1;
		     String nxx2;
		     String nxx3;
		     try {
			     nxx0 = nxx[0].toString();
			     nxx1 = nxx[1].toString();
			     nxx2 = nxx[2].toString();
			     nxx3 = nxx[3].toString();
		     } catch (Exception ex) {
		    	 //input parsing error, ignore it
		    	 continue;
		     }		     
		     if (nxx2.equals(personURI)) {
		    	 pw.println(nxx0);		    	 
		    	 pw.println(nxx3);
		     }		     
		}
	}
	
	public void getSynonyms(NxParser nxp) {
		String seeAlsoURI = "http://www.w3.org/2000/01/rdf-schema#seeAlso";
		for (Node[] nxx; nxp.hasNext();) {
		     nxx = nxp.next();
		     String nxx0;
		     String nxx1;
		     String nxx2;
		     String nxx3;
		     try {
			     nxx0 = nxx[0].toString();
			     nxx1 = nxx[1].toString();
			     nxx2 = nxx[2].toString();
			     nxx3 = nxx[3].toString();
		     } catch (Exception ex) {
		    	 //input parsing error, ignore it
		    	 continue;
		     }		     
		     if (nxx1.equals(seeAlsoURI)) {
		    	 pw.println(nxx0);
		    	 pw.println(nxx2);
		    	 pw.println(nxx3);		    	
		    	 if (Util.getInstance().isDEBUG()) {
		    		 System.out.println("See also uri " + nxx0 + ", " + nxx2 + ", " + nxx3);
		    	 }
		     }		     
		}
	}
	
	public void getSameAs(NxParser nxp) {
		String sameAs = "http://www.w3.org/2002/07/owl#sameAs";
		for (Node[] nxx; nxp.hasNext();) {
		     nxx = nxp.next();
		     String nxx0;
		     String nxx1;
		     String nxx2;
		     String nxx3;
		     try {
			     nxx0 = nxx[0].toString();
			     nxx1 = nxx[1].toString();
			     nxx2 = nxx[2].toString();
			     nxx3 = nxx[3].toString();
		     } catch (Exception ex) {
		    	 //input parsing error, ignore it
		    	 continue;
		     }		     
		     if (nxx1.equals(sameAs)) {
		    	 pw.println(nxx0);
		    	 pw.println(nxx2);
		    	 pw.println(nxx3);		    	
		    	 if (Util.getInstance().isDEBUG()) {
		    		 System.out.println("Same AS " + nxx0 + ", " + nxx2 + ", " + nxx3);
		    	 }
		     }		     
		}
	}
}
