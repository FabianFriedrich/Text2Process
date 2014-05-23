/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.textToProcess;

import java.util.ArrayList;

import com.inubit.research.gui.Workbench;
import com.inubit.research.gui.plugins.WorkbenchPlugin;

/**
 * @author ff
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TextToProcessPlugin _plugin = new TextToProcessPlugin();
		ArrayList<WorkbenchPlugin> _plugins = new ArrayList<WorkbenchPlugin>();
		_plugins.add(_plugin);
		Workbench _w = new Workbench(true,_plugins);
		_plugin.setWorkbench(_w);
		_w.addPlugin(_plugin);
		_w.setVisible(true);
		_plugin.openFrame();
	}

}
