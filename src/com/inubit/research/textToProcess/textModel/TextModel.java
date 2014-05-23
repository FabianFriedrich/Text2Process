/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.textToProcess.textModel;

import java.util.ArrayList;
import java.util.List;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 * @author ff
 *
 */
public class TextModel extends ProcessModel {
	
	private LegendNode f_legend = new LegendNode();
	
	/**
	 * 
	 */
	public TextModel() {
		this.addNode(f_legend);
	}

	@Override
	public String getDescription() {
		return "Text Model";
	}

	@Override
	public ProcessUtils getUtils() {
		if(super.getUtils() != null) {
			return super.getUtils();
		}//else
		return new TextModelUtils();
	}
	
	@Override
	public List<Class<? extends ProcessEdge>> getSupportedEdgeClasses() {
		ArrayList<Class<? extends ProcessEdge>> _edges = new ArrayList<Class<? extends ProcessEdge>>(1);
		_edges.add(TextEdge.class);
		return _edges;
	}

	@Override
	public List<Class<? extends ProcessNode>> getSupportedNodeClasses() {
		List<Class<? extends ProcessNode>> _nodes = new ArrayList<Class<? extends ProcessNode>>(2);
		_nodes.add(SentenceNode.class);
		_nodes.add(WordNode.class);
		_nodes.add(LegendNode.class);
		return _nodes;
	}
	
	public LegendNode getLegend() {
		return f_legend;
	}

}
