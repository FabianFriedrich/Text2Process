/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.textToProcess.transform;

import java.util.ArrayList;
import java.util.List;

import com.inubit.research.textToProcess.worldModel.Action;

import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.Tree;

/**
 * 
 * provides utilities for textual output
 * @author ff
 *
 */
public class PrintUtils {
	
	/**
	 * @param nods
	 * @return
	 */
	public static String toString(List<Tree> nodes) {
		List<String> _result = new ArrayList<String>(nodes.size());
		for(Tree t:nodes) {
			for(Tree leaf: t.getLeaves()) {
				_result.add(leaf.value());
			}
		}
		return PTBTokenizer.ptb2Text(_result);
	}
	
	public static String toString(Tree node) {
		return toString(node.getLeaves());
	}
	
	/**
	 * @param analyzedSentence
	 */
	public static void printExtractedActions(AnalyzedSentence analyzedSentence) {
		System.out.println("finally identifed actions in ("+PrintUtils.toString(analyzedSentence.getBaseSentence().getTree())+")");
		for(Action ac:analyzedSentence.getExtractedActions()) {
			System.out.println("----------------");
			System.out.println(ac.toFullString());
		}
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++");
	}

}
