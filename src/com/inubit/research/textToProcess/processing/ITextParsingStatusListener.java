/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.textToProcess.processing;

/**
 * @author ff
 *
 */
public interface ITextParsingStatusListener {
	
	public void setNumberOfSentences(int number);
	public void sentenceParsed(int number);

}
