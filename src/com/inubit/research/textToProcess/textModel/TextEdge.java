/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.textToProcess.textModel;

import java.awt.Shape;
import java.awt.Stroke;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessUtils;

/**
 * @author ff
 *
 */
public class TextEdge extends ProcessEdge {

	
	/**
	 * 
	 */
	public TextEdge() {
		super();
	}
	
	@Override
	public Stroke getLineStroke() {
		return ProcessUtils.defaultStroke;
	}

	@Override
	public Shape getSourceShape() {
		return null;
	}

	@Override
	public Shape getTargetShape() {
		return ProcessUtils.standardArrowFilled;
	}

}
