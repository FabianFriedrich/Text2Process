/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.textToProcess.transform;

import java.awt.Color;
import java.util.HashMap;

import net.frapu.code.visualization.ProcessNode;

import com.inubit.research.textToProcess.text.T2PSentence;
import com.inubit.research.textToProcess.textModel.SentenceNode;
import com.inubit.research.textToProcess.textModel.TextEdge;
import com.inubit.research.textToProcess.textModel.TextLinkEdge;
import com.inubit.research.textToProcess.textModel.TextModel;
import com.inubit.research.textToProcess.textModel.WordNode;
import com.inubit.research.textToProcess.worldModel.Action;
import com.inubit.research.textToProcess.worldModel.ExtractedObject;
import com.inubit.research.textToProcess.worldModel.SpecifiedElement;

/**
 * @author ff
 *
 */
public class TextModelBuilder {

	/**
	 * 
	 */
	public static final Color COLOR_REFERENCE_EDGES = Color.BLACK;
	/**
	 * 
	 */
	public static final Color COLOR_LINK_EDGES = Color.BLUE;
	/**
	 * 
	 */
	public static final float DEFAULT_EDGE_ALPHA = 0.5f;
	private TextAnalyzer f_analyzer;
	private HashMap<T2PSentence, SentenceNode> f_sentenceMap = new HashMap<T2PSentence, SentenceNode>();
	
	/**
	 * 
	 */
	public TextModel createModel(TextAnalyzer analyzer) {
		f_analyzer = analyzer;
		return buildModel();
	}

    
	
	/**
	 * 
	 */
	private TextModel buildModel() {
		TextModel _result = new TextModel();
		for(T2PSentence s: f_analyzer.getText().getSentences()) {
			SentenceNode _sn = new SentenceNode(s.getID());
			f_sentenceMap.put(s, _sn);
			_result.addNode(_sn);
			for(int w = 0; w<s.size(); w++) {
				WordNode _wn = new WordNode(s.get(w).value());
				_result.addNode(_wn);
				_sn.addWord(_wn);
			}
		}
		//building edges for all relative references
		for(ExtractedObject ele:f_analyzer.getWorld().getElements()) {
			if(ele.needsResolve()) {
				WordNode _start = (WordNode) getProcessNode(ele);
				if(ele.getReference() != null) {
					SpecifiedElement _target = ele.getReference();
					WordNode _end = (WordNode) getProcessNode(_target);
					TextEdge _edge = new TextEdge();
					_edge.setColor(TextModelBuilder.COLOR_REFERENCE_EDGES);
					_edge.setAlpha(TextModelBuilder.DEFAULT_EDGE_ALPHA);
					_edge.setSource(_start);
					_edge.setTarget(_end);
					_result.addEdge(_edge);
				}else {
					_start.setBackground(new Color(255,200,200));
				}
			}			
		}			
		//building dashed edges for links
		for(Action a:f_analyzer.getWorld().getActions()) {
			if(a.getLink() == null) {
				continue;
			}
			ProcessNode _aNode = getProcessNode(a);
			ProcessNode _bNode = getProcessNode(a.getLink());		
			TextLinkEdge _edge = new TextLinkEdge();
			_edge.setColor(TextModelBuilder.COLOR_LINK_EDGES);
			_edge.setAlpha(0.5f);
			_edge.setSource(_aNode);
			_edge.setTarget(_bNode);
			_result.addEdge(_edge);					
		}
		
		int y = (int)((f_analyzer.getText().getSentences().size()) * 
				(SentenceNode.SENTENCE_HEIGHT+SentenceNode.SENTENCE_DISTANCE)) + SentenceNode.SENTENCE_DISTANCE;
		_result.getLegend().setPos(
				SentenceNode.DISTANCE_LEFT+_result.getLegend().getSize().width/2, 
				y+_result.getLegend().getSize().height/2);
		
		return _result;
	}
	
	public ProcessNode getProcessNode(SpecifiedElement a) {
		return f_sentenceMap.get(a.getOrigin()).getProcessNodes().get(a.getWordIndex()-1);
	}
	
	public SentenceNode getSentenceNode(T2PSentence sent) {
		return f_sentenceMap.get(sent);
	}
}
