/**
 * $Id: mxHtmlTextShape.java,v 1.4 2010-07-18 19:47:20 david Exp $
 * Copyright (c) 2010, Gaudenz Alder, David Benson
 */
package com.mxgraph.shape;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Map;

import javax.swing.CellRendererPane;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxLightweightLabel;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;

public class mxHtmlTextShape implements mxITextShape
{

	/**
	 * 
	 */
	public void paintShape(mxGraphics2DCanvas canvas, String text,
			mxCellState state, Map<String, Object> style)
	{
		mxLightweightLabel textRenderer = mxLightweightLabel
				.getSharedInstance();
		CellRendererPane rendererPane = canvas.getRendererPane();
		Rectangle rect = state.getLabelBounds().getRectangle();
		Graphics2D g = canvas.getGraphics();

		if (textRenderer != null
				&& rendererPane != null
				&& (g.getClipBounds() == null || g.getClipBounds().intersects(
						rect)))
		{
			double scale = canvas.getScale();
			int x = rect.x;
			int y = rect.y;
			int w = rect.width;
			int h = rect.height;

			if (!mxUtils.isTrue(style, mxConstants.STYLE_HORIZONTAL, true))
			{
				g.rotate(-Math.PI / 2, x + w / 2, y + h / 2);
				g.translate(w / 2 - h / 2, h / 2 - w / 2);

				int tmp = w;
				w = h;
				h = tmp;
			}

			// Renders the scaled text
			textRenderer.setText(mxUtils.createHtmlDocument(style, text));
			textRenderer.setFont(mxUtils.getFont(style, canvas.getScale()));
			g.scale(scale, scale);
			rendererPane.paintComponent(g, textRenderer, rendererPane,
					(int) (x / scale) + mxConstants.LABEL_INSET,
					(int) (y / scale) + mxConstants.LABEL_INSET,
					(int) (w / scale), (int) (h / scale), true);
		}
	}
}
