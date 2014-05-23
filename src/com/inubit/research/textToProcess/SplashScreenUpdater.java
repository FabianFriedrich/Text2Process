/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.textToProcess;

import java.util.TimerTask;

import com.inubit.research.gui.SplashScreen;

/**
 * @author ff
 *
 */
public class SplashScreenUpdater extends TimerTask {
	
	private SplashScreen f_screen;
	private int f_inc;

	/**
	 * @param splashScreen
	 * @param i
	 */
	public SplashScreenUpdater(SplashScreen splashScreen, int inc) {
		f_screen = splashScreen;
		f_inc = inc;
	}

	@Override
	public void run() {
		f_screen.incrementProgress(f_inc);		
	}

}
