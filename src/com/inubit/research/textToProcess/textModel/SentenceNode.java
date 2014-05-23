/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.textToProcess.textModel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 * @author ff
 *
 */
public class SentenceNode extends Cluster {


	public static final int DISTANCE_LEFT = 10; //pixel
	public static final int WORD_DISTANCE = 5; //pixel
	public static final int SENTENCE_DISTANCE = 10; //pixel
	
	public static final int SENTENCE_HEIGHT = 30; //pixel
	private boolean f_firstPaint = true;
	
	private ArrayList<WordNode> f_wordNodes = new ArrayList<WordNode>();
	private int f_index = 0;

	/**
	 * 
	 */
	public SentenceNode() {

	}
	
	/**
	 * @param index 
	 * 
	 */
	public SentenceNode(int index) {
		f_index = index;		
	}
	
	public void addWord(WordNode word){
		f_wordNodes .add(word);
		super.addProcessNode(word);
	}
	
	@Override
	protected Shape getOutlineShape() {
		RoundRectangle2D outline = new RoundRectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height, 10, 10);
        return outline;
	}

	@Override
	protected void paintInternal(Graphics g) {
		
		Graphics2D _g = (Graphics2D) g;
		if(f_firstPaint) {
			FontMetrics _metrics = _g.getFontMetrics();
			for(WordNode w:f_wordNodes) {
				w.setBounds(_metrics.getStringBounds(w.getWord(), _g));
			}
			//determining position (left, center)
			int x = DISTANCE_LEFT;
			int y = (int)((f_index+0.5) * (SENTENCE_HEIGHT+SENTENCE_DISTANCE)) + SENTENCE_DISTANCE;
			//determining width and height of the sentence
			int _width = 0;
			for(WordNode w:f_wordNodes) {
				Dimension _d = w.getSize();
				int _wx = x + _width + WORD_DISTANCE + (_d.width/2) ;
				w.setLocation(_wx,y);
				_width += WORD_DISTANCE + _d.getWidth();
			}
			_width += WORD_DISTANCE;
			super.setSize(_width, SENTENCE_HEIGHT);
			super.setPos(x + _width/2,y);
			f_firstPaint = false;
		}		
		if(isSelected()) {
			_g.setColor(WordNode.HIGHLIGHT_COLOR);
		}else {
			_g.setColor(Color.WHITE);
			_g.setStroke(ProcessUtils.defaultStroke);
		}
		_g.fill(getOutlineShape());
		_g.setColor(Color.LIGHT_GRAY);
		_g.draw(getOutlineShape());		
	}
	
	@Override
	public void setSize(int w, int h) {
		return;
	}
	
	@Override
	public synchronized void setPos(int x, int y) {
		return;
	}

	/**
	 * @return
	 */
	public int getIndex() {
		return f_index;
	}
	
	@Override
	public void removeProcessNode(ProcessNode n) {
		//not possible its a build only model
	}

}
