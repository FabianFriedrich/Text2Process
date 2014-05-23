/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.textToProcess;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Timer;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import com.inubit.research.gui.SplashScreen;
import com.inubit.research.gui.Workbench;
import com.inubit.research.gui.plugins.WorkbenchPlugin;
import com.inubit.research.textToProcess.gui.T2Pgui;



/**
 * @author ff
 *
 */
public class TextToProcessPlugin extends WorkbenchPlugin implements ActionListener {

	private JMenuItem f_mi = new JMenuItem("Generate Model from Text...");
	private T2Pgui f_gui;
	//private static final String f_testFile = "TestData/Oracle - Expense Report Process - eng.txt";
	private static final String f_testFile = "TestData/Inubit tutorial - eng.txt";
	


	
	/**
	 * @param workbench
	 */
	public TextToProcessPlugin() {
		super();
		f_mi.addActionListener(this);	     
	}

	@Override
	public void init(SplashScreen splashScreen) {
		super.init(splashScreen);
		splashScreen.setStatus("Loading NLP-Utils...");
		int _ttu = 95 - splashScreen.getProgress();
		int _timeNeeded = 40;//20 seconds is what it usually takes for SP and WN and again for FN
		int _interval = _ttu /_timeNeeded; 
		Timer _t = new Timer();
		_t.scheduleAtFixedRate(new SplashScreenUpdater(splashScreen,_interval), 1000,1000);
		f_gui = getGUI(); //the actual loading starts here
		_t.cancel();
	}
	
	
	
	public T2Pgui getGUI() {
		if(f_gui == null) {
			f_gui = new T2Pgui();
		}
		return f_gui;
	}
	
	@Override
	public Component getMenuEntry() {
		return f_mi;
	}
	
	public void openFrame() {
		SwingUtilities.updateComponentTreeUI(getGUI());
		getGUI().setVisible(true);			
	}
	
	public void openFrameWithDefaultText() {
		SwingUtilities.updateComponentTreeUI(getGUI());
		getGUI().setVisible(true);	
		getGUI().loadText(new File(f_testFile));
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		SwingUtilities.updateComponentTreeUI(getGUI());
		getGUI().setVisible(true);
	}
	
	@Override
	public void setWorkbench(Workbench wb) {
		super.setWorkbench(wb);
		f_gui.setWorkbench(wb);
		
		
		
	    GraphicsConfiguration _gc = f_gui.getGraphicsConfiguration();
        Dimension _dim = Toolkit.getDefaultToolkit().getScreenSize();    
        Insets _insets = Toolkit.getDefaultToolkit().getScreenInsets(_gc);
        _dim.height -= _insets.bottom;
        _dim.height -= _insets.top;
        _dim.width -= _insets.left;
        _dim.width -= _insets.right;
        double _wbSize = 0.5;
        f_gui.setSize(new Dimension(_dim.width,(int)(_dim.height*(1-_wbSize))));
        f_gui.setLocation(_insets.left, _insets.top);
        wb.setSize(new Dimension(_dim.width,(int)(_dim.height*_wbSize)));		
        wb.setLocation(_insets.left, _insets.top+f_gui.getSize().height);
        
	}

	
}
