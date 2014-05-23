/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.textToProcess.worldModel;

import com.inubit.research.textToProcess.text.T2PSentence;

/**
 * @author ff
 *
 */
public class OriginatedElement {
	
	private T2PSentence f_origin;
	
	/**
	 * 
	 */
	public OriginatedElement(T2PSentence origin) {
		f_origin = origin;
	}

	
	/**
	 * returns the sentence from which this Action was extracted
	 * @return the f_origin
	 */
	public T2PSentence getOrigin() {
		return f_origin;
	}
}
