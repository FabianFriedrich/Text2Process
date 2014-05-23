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
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.ProcessUtils.Orientation;

/**
 * @author ff
 *
 */
public class LegendNode extends ProcessNode {
	
	/**
	 * 
	 */
	public LegendNode() {
		setSize(90,66);
		setPos(new Point(SentenceNode.DISTANCE_LEFT+this.getSize().width/2,
				SentenceNode.DISTANCE_LEFT+this.getSize().height/2));
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
		//drawing colored rectangle
		
		Point _ul = this.getPos();
		_ul.x -= this.getSize().width/2;
		_ul.y -= this.getSize().height/2;
		_ul.x += 10;
		_ul.y += 10;
		_g.setColor(TextModelControler.getColorActor(0));
		drawLegendItem(_g, _ul, "Actor");
		
		_ul.y += 18;
		_g.setColor(TextModelControler.getColorAction(0));
		drawLegendItem(_g, _ul, "Action");
		
		_ul.y += 18;
		_g.setColor(TextModelControler.getColorObject(0));
		drawLegendItem(_g, _ul, "Resource");
	}

	private void drawLegendItem(Graphics2D _g, Point pos,String text) {
		_g.fillRoundRect(pos.x, pos.y, 10, 10, 2, 2);
		_g.setColor(Color.LIGHT_GRAY);
		_g.drawRoundRect(pos.x, pos.y, 10, 10, 2, 2);
		_g.setColor(Color.BLACK);
		ProcessUtils.drawText(_g, pos.x+18, pos.y-6, 150,text, Orientation.LEFT);
	}
	
	@Override
	public void setSize(int w, int h) {
		return;
	}

}
