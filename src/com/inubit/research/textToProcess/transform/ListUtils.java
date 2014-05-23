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

import com.inubit.research.textToProcess.worldModel.Actor;
import com.inubit.research.textToProcess.worldModel.ExtractedObject;

/**
 * @author ff
 *
 */
public class ListUtils {
	
	/**
	 * @param _subjects
	 * @return
	 */
	public static ArrayList<ExtractedObject> toExtractedObjects(List<Actor> list) {
		ArrayList<ExtractedObject> _result = new ArrayList<ExtractedObject>();
		for(Actor a:list) {
			_result.add(a);
		}
		return _result;
	}

	/**
	 * @param string
	 * @return
	 */
	public static List<String> getList(String... string) {
		ArrayList<String> _result = new ArrayList<String>();
		for(String s:string) {
			_result.add(s);
		}
		return _result;
	}

}
