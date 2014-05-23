/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.textToProcess;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.FlowObject;

import com.inubit.research.textToProcess.gui.LaneSplitOffControler;
import com.inubit.research.textToProcess.processing.ITextParsingStatusListener;
import com.inubit.research.textToProcess.processing.T2PStanfordWrapper;
import com.inubit.research.textToProcess.text.T2PSentence;
import com.inubit.research.textToProcess.text.Text;
import com.inubit.research.textToProcess.textModel.SentenceNode;
import com.inubit.research.textToProcess.textModel.TextModel;
import com.inubit.research.textToProcess.textModel.TextModelControler;
import com.inubit.research.textToProcess.transform.ProcessModelBuilder;
import com.inubit.research.textToProcess.transform.TextAnalyzer;
import com.inubit.research.textToProcess.transform.TextModelBuilder;
import com.inubit.research.textToProcess.worldModel.Action;
import com.inubit.research.textToProcess.worldModel.SpecifiedElement;

import edu.stanford.nlp.trees.TypedDependency;

/**
 * wraps all of the functionality to create processes from text.
 * Load and analyze a text using "parseText".
 * To reanalyze a text simple use "analyzeText".
 * All information (created Text Model after parsing or generated BPMN model) is
 * returned to the TextToProcessListener.
 * @author ff
 *
 */
public class TextToProcess {
	
private T2PStanfordWrapper f_stanford = new T2PStanfordWrapper();
	
	private Text f_text;
	
	private TextModelControler f_textModelControler = null;
	private TextAnalyzer f_analyzer = new TextAnalyzer();
	private TextModelBuilder f_builder = new TextModelBuilder();
    private BPMNModel f_generatedModel = null;
    
	private HashMap<Action, FlowObject> f_elementsMap = new HashMap<Action, FlowObject>();
	private HashMap<FlowObject, Action> f_elementsMapInv = new HashMap<FlowObject, Action>();
	
	private TextToProcessListener f_listener = null;
	private LaneSplitOffControler f_lsoControler;
	
	/**
	 * 
	 */
	public TextToProcess(TextToProcessListener listener) {
		 f_listener = listener;		 
	}
	
	/**
	 * 
	 */
	public TextToProcess(TextToProcessListener listener,TextModelControler tmControler, LaneSplitOffControler lsoControler) {
		 f_listener = listener;
		 f_textModelControler = tmControler;
		 f_lsoControler = lsoControler;
	}
	
	public void setLaneSplitOffContoler(LaneSplitOffControler lsoControler) {
		f_lsoControler = lsoControler;
	}
	
	 /**
     * (Re-)starts analyzing the loaded text and creates a process model
     */
	public void analyzeText(boolean rebuildTextModel) {
		f_analyzer.analyze(f_text);
        if(rebuildTextModel) {
			TextModel _model = f_builder.createModel(f_analyzer);
			f_listener.textModelChanged(_model);			
			if(f_textModelControler != null)
				f_textModelControler.setModels(this, f_analyzer,f_builder,_model);
        }
        ProcessModelBuilder _builder = new ProcessModelBuilder(this);
        f_generatedModel = _builder.createProcessModel(f_analyzer.getWorld());
        if(f_lsoControler != null)
			f_lsoControler.setCommLinks(_builder.getCommLinks());
        f_listener.modelGenerated(f_generatedModel);
	}
	
	public void parseText(File file,ITextParsingStatusListener tpsl) {
		f_text = f_stanford.createText(file,tpsl);
		f_analyzer.clear();
		analyzeText(true);
	}
	
	/**
	 * Sets the element map which comes from the ProcessModelBuilder
	 * and can be used to map actions to task nodes etc.
	 * @param map
	 */
	public void setElementMapping(HashMap<Action, FlowObject> map) {
		f_elementsMap = map;
		for(Entry<Action, FlowObject> e:f_elementsMap.entrySet()) {
			//building inverted list
			f_elementsMapInv.put(e.getValue(), e.getKey());			
		}
	}

	/**
	 * @param o
	 */
	public void textModelElementClicked(ProcessObject o) {
		if(o instanceof SentenceNode) {
    		SentenceNode n = (SentenceNode) o;
	    	T2PSentence _sentence = f_text.getSentences().get(n.getIndex());
	       	if(_sentence != null) {
	       		f_listener.displayTree(_sentence.getTree());
	    		
	    		Collection<TypedDependency> _list = _sentence.getGrammaticalStructure().typedDependenciesCollapsed();
	    		
	    		StringBuffer _depText = new StringBuffer();
	    		for(TypedDependency td:_list) {
	    			_depText.append(td.toString());
	    		}
	    		f_listener.displayDependencies(_depText.toString());
	    		
	    		f_analyzer.analyzeSentence(_sentence,1,true);
	    	}
    	}
    	else {
    		if(f_elementsMapInv.containsKey(o)) {
    			Action _ac = f_elementsMapInv.get(o);
    			if(f_textModelControler != null)
    				f_textModelControler.highlightAction(_ac);    			
    		}
    	}
	}

	/**
	 * @return
	 */
	public TextStatistics getTextStatistics() {
		TextStatistics _result = new TextStatistics();
		_result.setNumberOfSentences(f_text.getSize());
		_result.setAvgSentenceLength(f_text.getAvgSentenceLength());
		_result.setNumOfReferences(f_analyzer.getNumberOfReferences());
		_result.setNumOfLinks(f_analyzer.getNumberOfLinks());
		return _result;
	}

	/**
	 * @return
	 */
	public TextAnalyzer getAnalyzer() {
		return f_analyzer;
	}

	/**
	 * @param _element
	 */
	public void textElementClicked(SpecifiedElement _element) {
		if(_element instanceof Action) {
			FlowObject _corr = f_elementsMap.get(_element);
			if(_corr != null) {
				f_listener.textElementClicked(_element,_corr);							
			}
		}
	}

	/**
	 * @param sentenceWordID
	 * @param sentenceWordID2
	 */
	public void addManualReferenceResolution(SentenceWordID sentenceWordID,	SentenceWordID sentenceWordID2) {
		f_analyzer.addManualReference(sentenceWordID, sentenceWordID2);
	}

	
}
