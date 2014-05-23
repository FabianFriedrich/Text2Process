/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.textToProcess.textModel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.ProcessUtils.Orientation;

/**
 * @author ff
 *
 */
public class WordNode extends ProcessNode {
	
	/**
	 * 
	 */
	public static final Color HIGHLIGHT_COLOR = new Color(255,255,224);

	private static final int PADDING = 3; //pixels

	/**
	 * 
	 */
	public WordNode(String word) {
		setText(word.replaceAll("\\/", "/"));
		setBackground(Color.WHITE);
	}

	/**
	 * 
	 */
	public WordNode() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected Shape getOutlineShape() {
		Rectangle2D outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height);
        return outline;
	}

	@Override
	protected void paintInternal(Graphics g) {
		Graphics2D _g = (Graphics2D) g;
		Shape _s = getOutlineShape();
		if(isSelected()) {
			_g.setColor(WordNode.HIGHLIGHT_COLOR);
		}else {
			_g.setColor(getBackground());
			_g.setStroke(ProcessUtils.defaultStroke);
		}
		_g.fill(_s);
		_g.setColor(Color.LIGHT_GRAY);
		_g.draw(_s);
		_g.setColor(Color.BLACK);
		if(getText() == null) {
			ProcessUtils.drawText(_g, getPos().x, getPos().y, getSize().width, "Word", Orientation.CENTER);
		}else {
			ProcessUtils.drawText(_g, getPos().x, getPos().y, getSize().width, getText(), Orientation.CENTER);
		}
	}
	
	@Override
	public void setSize(int w, int h) {
		return;
	}
	
	@Override
	public void setPos(int x, int y) {
		return;
	}

	/**
	 * @return
	 */
	public String getWord() {
		return getText();
	}

	/**
	 * @param stringBounds
	 */
	public void setBounds(Rectangle2D b) {
		super.setSize((int)b.getWidth() + 2* PADDING, (int)b.getHeight() + 2* PADDING);
	}

	/**
	 * @param _wx
	 * @param y
	 */
	public void setLocation(int x, int y) {
		super.setPos(x,y);
	}
	
	@Override
	public String toString() {
		return "WordNode ("+getText()+")";
	}
}
