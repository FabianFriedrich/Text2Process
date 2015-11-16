/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.textToProcess.processing;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.didion.jwnl.data.POS;

import com.inubit.research.textToProcess.Constants;
import com.inubit.research.textToProcess.transform.SearchUtils;
import com.inubit.research.textToProcess.worldModel.Action;
import com.inubit.research.textToProcess.worldModel.SpecifiedElement;
import com.inubit.research.textToProcess.worldModel.Specifier;

import de.saar.coli.salsa.reiter.framenet.DatabaseReader;
import de.saar.coli.salsa.reiter.framenet.FNDatabaseReader;
import de.saar.coli.salsa.reiter.framenet.Frame;
import de.saar.coli.salsa.reiter.framenet.FrameElement;
import de.saar.coli.salsa.reiter.framenet.FrameNet;
import de.saar.coli.salsa.reiter.framenet.LexicalUnit;
import de.saar.coli.salsa.reiter.framenet.fncorpus.AnnotatedLexicalUnit;
import de.saar.coli.salsa.reiter.framenet.fncorpus.AnnotationCorpus;
import de.saar.coli.salsa.reiter.framenet.fncorpus.FEGroupRealization;
import de.saar.coli.salsa.reiter.framenet.fncorpus.ValencePattern;
import de.saar.coli.salsa.reiter.framenet.fncorpus.ValenceUnit;

/**
 * @author ff
 *
 */
public class FrameNetWrapper {
	
	public enum PhraseType{
		CORE,
		PERIPHERAL,
		EXTRA_THEMATIC,
		GENITIVE,
		UNKNOWN
	}
	
	private static String f_frameNetHome = "/fndata-1.5/";
	private static FrameNet f_frameNet;
	private static AnnotationCorpus f_corpus;
	//public static final boolean LOAD_ANNOTATIONS = true;

	public static void init() {		
		try {
			
			long _start = System.currentTimeMillis();
			// Create a new FrameNet object
			f_frameNet = new FrameNet();
			//f_frameNet.getLexicalUnits(lemma, PartOfSpeech.Verb)
			// Create a new DatabaseReader
			DatabaseReader reader;
			
			URL _url = FrameNetWrapper.class.getResource(f_frameNetHome);
			if(_url == null){
				//loading from an IDE
				File _fnDirectory = new File("resources"+f_frameNetHome);
				reader = new FNDatabaseReader(_fnDirectory, false);
			}else{
				//loading from a jar file
				reader = new FNDatabaseReader(new File(_url.toURI()), false);
			}
						
			// Reading FrameNet
			f_frameNet.readData(reader);			
			Logger _l = Logger.getLogger("this");
			_l.setLevel(Level.SEVERE);
			
			//logging loading time
			long _annoStart = System.currentTimeMillis();			
			System.out.println("Loaded FrameNet in: "+(_annoStart-_start)+"ms");
			
			//reading valence patterns from reduced corpus
			f_corpus = new AnnotationCorpus(f_frameNet,_l);
			f_corpus.setScanSubCorpuses(false);
			
			URL _u = FrameNetWrapper.class.getResource(f_frameNetHome+"lu");
			if(_u == null){
				//loading from an IDE
				File _fnluFiles = new File("resources/"+f_frameNetHome+"lu");
				f_corpus.parse(_fnluFiles);
			}else{
				//loading from a jar file
				f_corpus.parse(new File(_u.getFile()));
			}
				
			//logging loading time
			System.out.println("Loaded FrameNet-Annotations in: "+(System.currentTimeMillis()-_annoStart)+"ms");			
		} catch (Exception ex) {
			System.err.print("could not initialize FrameNetWrapper: "+ex.getMessage());
			ex.printStackTrace();
		} 
	}

	public static void printAllFrames() {
		// Iterate over all frames
		for (Frame frame : f_frameNet.getFrames()) {

		    // Print out the name of each frame
		    System.out.println(frame.getName());
		    
		   // Iterate over all frame elements of the frame
		    for (FrameElement fe : frame.frameElements()) {
		    
		       // Print out the name and semantic types of the frame element
		       System.out.println("  " + fe.getName() + " / " + Arrays.toString(fe.getSemanticTypes()));
		    }    
		    System.out.println(" This frame uses the frames: ");
		    
		    // Iterate over all frames that are used by this frame
		    for (Frame uses : frame.uses()) {
		       
		       // Print out their names
		          System.out.println("  " + uses.getName());
		    }
		}
	}	
	
	public static void determineSpecifierFrameElement(SpecifiedElement element, Specifier spec) {
		if("of".equals(spec.getHeadWord()) && !(element instanceof Action)){
			spec.setPhraseType(PhraseType.GENITIVE);
		}else {
			HashMap<FrameElement,Integer> _countMap = new HashMap<FrameElement, Integer>();
			boolean _verb = element instanceof Action;
			String _baseForm = WordNetWrapper.getBaseForm(element.getName(), false, _verb?POS.VERB:POS.NOUN);
			if(_verb && ((Action)element).getPrt()!=null) {
				_baseForm += " "+((Action)element).getPrt();
			}
			Collection<LexicalUnit> _units = f_frameNet.getLexicalUnits(_baseForm, (_verb?"v":"n"));
			int _testedLUs = _units.size();
			for(LexicalUnit lu:_units) {
				AnnotatedLexicalUnit _alu = f_corpus.getAnnotation(lu);
				if(_alu != null) {
					for(FEGroupRealization fegr:_alu.getFERealizations()) {
						for(ValencePattern pat:fegr.getPatterns()) {
							for(ValenceUnit vu:pat.getUnits()) {
								if(vu.getPhraseType().startsWith("PP") && vu.getPhraseType().contains("["+spec.getHeadWord()+"]")) {
									if(_countMap.containsKey(vu.getFrameElement())) {
										Integer _newVal = _countMap.get(vu.getFrameElement()) + pat.getTotalCount();
										_countMap.put(vu.getFrameElement(), _newVal);
									}else {
										_countMap.put(vu.getFrameElement(),pat.getTotalCount());
									}
								}
							}
						}
					}	
				}
			}
			FrameElement _best = (FrameElement) SearchUtils.getMaxCountElement(_countMap);
			if(_best == null) {
				spec.setPhraseType(PhraseType.UNKNOWN);
			}else {
				spec.setPhraseType(toPT(_best));
				spec.setFrameElement(_best);
			}
			if(Constants.DEBUG_FRAME_ASSIGNMENT) {
				System.out.println("Valence Units for: "+_baseForm+" ("+_testedLUs+") - "+spec.getPhrase());
				for(FrameElement fe:_countMap.keySet()) {
					System.out.println("\t\t "+_countMap.get(fe)+" "+fe+" "+fe.getCoreTypeString());
				}		
				System.out.println("\t+ Best: "+_best);
			}
			
		}
	}

	/**
	 * @param _best
	 * @return
	 */
	private static PhraseType toPT(FrameElement fe) {
		switch(fe.getCoreType()) {
			case Core: return PhraseType.CORE;
			case Core_Unexpressed: return PhraseType.CORE; 
			case Extra_Thematic: return PhraseType.EXTRA_THEMATIC; 
			case Peripheral: return PhraseType.PERIPHERAL;
			default: return PhraseType.UNKNOWN;
		}
	}

}
