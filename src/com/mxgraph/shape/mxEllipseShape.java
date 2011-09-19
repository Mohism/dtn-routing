package com.mxgraph.shape;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import app.simulator.restrictions.Restrictions;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.view.mxCellState;

public class mxEllipseShape extends mxBasicShape
{

	/**
	 * 
	 */
	public Shape createShape(mxGraphics2DCanvas canvas, mxCellState state)
	{
		Rectangle temp = state.getRectangle();

		return new Ellipse2D.Float(temp.x, temp.y, temp.width, temp.height);
	}
	public void paintShape(mxGraphics2DCanvas canvas, mxCellState state)
	{
		
		
		Rectangle temp = state.getRectangle();
		

		Shape shape = new Ellipse2D.Double(temp.x, temp.y, temp.width, temp.height);
		//System.out.println("Hi1");
		//shape = null;
		if (shape != null)
		{
			// Paints the background
			if (configureGraphics(canvas, state, true))
			{
				canvas.fillShape(shape, hasShadow(canvas, state));
			}

			// Paints the foreground
			if (configureGraphics(canvas, state, false))
			{
				canvas.getGraphics().draw(shape);
				
				Shape shape2 =new Ellipse2D.Double(state.getCenterX()-Restrictions.defaultradius, state.getCenterY()-Restrictions.defaultradius, Restrictions.defaultradius*2, Restrictions.defaultradius*2);
				canvas.getGraphics().setColor(new Color(250,250,255));
				canvas.getGraphics().draw(shape2);
			}
		}
	}
}
