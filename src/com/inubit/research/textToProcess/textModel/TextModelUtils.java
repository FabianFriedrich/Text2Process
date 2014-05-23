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

import com.inubit.research.layouter.ProcessLayouter;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 * @author ff
 *
 */
public class TextModelUtils extends ProcessUtils {

	@Override
	public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {
		return null;
	}

	@Override
	public List<ProcessLayouter> getLayouters() {
		return new ArrayList<ProcessLayouter>(0);
	}

}
