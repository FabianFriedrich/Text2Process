/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.textToProcess.textModel;

import java.awt.Stroke;

import net.frapu.code.visualization.ProcessUtils;

/**
 * @author ff
 *
 */
public class TextLinkEdge extends TextEdge {

	@Override
	public Stroke getLineStroke() {
		return ProcessUtils.dottedDashedStroke;
	}
}
