/**
 * $Id: mxCurveLabelShape.java,v 1.10 2010-08-05 09:12:30 david Exp $
 * Copyright (c) 2010, David Benson, Gaudenz Alder
 */
package com.mxgraph.shape;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.Map;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxCurve;
import com.mxgraph.util.mxLine;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;

/**
 * Draws the edge label along a curve derived from the curve describing
 * the edge's path
 */
public class mxCurveLabelShape implements mxITextShape
{
	/**
	 * Cache of the label text
	 */
	protected String lastValue;

	/**
	 * Cache of the label font
	 */
	protected Font lastFont;

	/**
	 * Cache of the last set of guide points that this label was calculated for
	 */
	protected List<mxPoint> lastPoints;

	/**
	 * Cache of the points between which drawing straight lines views as a
	 * curve
	 */
	protected mxCurve curve;

	/**
	 * Cache the state associated with this shape
	 */
	protected mxCellState state;

	/**
	 * Cache of information describing characteristics relating to drawing 
	 * each glyph of this label
	 */
	protected LabelGlyphCache[] labelGlyphs;

	/**
	 * Cache of the total length of the branch label
	 */
	protected double labelSize;

	/**
	 * Cache of the bounds of the label
	 */
	protected mxRectangle labelBounds;

	/**
	 * ADT to encapsulate label positioning information
	 */
	protected LabelPosition labelPosition = new LabelPosition();

	/**
	 * Buffer at both ends of the label
	 */
	public static double LABEL_BUFFER = 30;
	
	/**
	 * Shared FRC for font size calculations
	 */
	public static FontRenderContext frc = new FontRenderContext(null, false, false);


	/**
	 *
	 */
	protected boolean rotationEnabled = true;

	public mxCurveLabelShape(mxCellState state, mxCurve value)
	{
		this.state = state;
		this.curve = value;
	}

	/**
	 *
	 */
	public boolean getRotationEnabled()
	{
		return rotationEnabled;
	}

	/**
	 *
	 */
	public void setRotationEnabled(boolean value)
	{
		rotationEnabled = value;
	}

	/**
	 * 
	 */
	public void paintShape(mxGraphics2DCanvas canvas, String text,
			mxCellState state, Map<String, Object> style)
	{
		Rectangle rect = state.getLabelBounds().getRectangle();
		Graphics2D g = canvas.getGraphics();

		if (labelGlyphs == null)
		{
			updateLabelBounds(text, style);
		}

		if (labelGlyphs != null
				&& (g.getClipBounds() == null || g.getClipBounds().intersects(
						rect)))
		{
			// Creates a temporary graphics instance for drawing this shape
			float opacity = mxUtils.getFloat(style, mxConstants.STYLE_OPACITY,
					100);
			Graphics2D previousGraphics = g;
			g = canvas.createTemporaryGraphics(style, opacity, state);

			Font font = mxUtils.getFont(style, canvas.getScale());
			g.setFont(font);

			Color fontColor = mxUtils.getColor(style,
					mxConstants.STYLE_FONTCOLOR, Color.black);
			g.setColor(fontColor);

			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			for (int j = 0; j < text.length(); j++)
			{
				mxLine parallel = labelGlyphs[j].glyphVector;
				double rotation = (Math.atan(parallel.getEndPoint().getY()
						/ parallel.getEndPoint().getX()));

				final AffineTransform old = g.getTransform();
				g.translate(parallel.getX(), parallel.getY());
				g.rotate(rotation);
				g.drawString(text.substring(j, j + 1), 0f, 0f);
				g.setTransform(old);
			}

			g.dispose();
			g = previousGraphics;
		}
	}

	/**
	 * Updates the cached position and size of each glyph in the edge label. 
	 * @param label the entire string of the label.
	 * @param style the edge style
	 */
	public mxRectangle updateLabelBounds(String label, Map<String, Object> style)
	{
		double scale = state.getView().getScale();
		Font font = mxUtils.getFont(style, scale);

		// Check that the size of the widths array matches 
		// that of the label size
		if (labelGlyphs == null || label.length() != labelGlyphs.length)
		{
			labelGlyphs = new LabelGlyphCache[label.length()];
		}

		if (!label.equals(lastValue) || !font.equals(lastFont))
		{
			labelSize = 0;

			for (int i = 0; i < label.length(); i++)
			{
				String glyph = label.substring(i, i + 1);
				mxRectangle size = new mxRectangle(font.getStringBounds(glyph,
						mxCurveLabelShape.frc));

				if (labelGlyphs[i] == null)
				{
					labelGlyphs[i] = new LabelGlyphCache();
				}

				labelGlyphs[i].labelGlyphBounds = size;
				labelSize += size.getWidth();
				labelGlyphs[i].glyph = glyph;
			}

			// Update values used to determine whether or not the label cache 
			// is valid or not
			lastValue = label;
			lastFont = font;
			lastPoints = curve.getGuidePoints();
		}

		// Store the start/end buffers that pad out the ends of the branch so the label is
		// visible. We work initially as the start section being at the start of the
		// branch and the end at the end of the branch. Note that the actual label curve
		// might be reversed, so we allow for this after completing the buffer calculations,
		// otherwise they'd need to be constant isReversed() checks throughout
		labelPosition.startBuffer = LABEL_BUFFER * scale;
		labelPosition.endBuffer = LABEL_BUFFER * scale;

		calculationLabelPosition(style, label);

		if (curve.isLabelReversed())
		{
			double temp = labelPosition.startBuffer;
			labelPosition.startBuffer = labelPosition.endBuffer;
			labelPosition.endBuffer = temp;
		}

		double curveLength = curve.getCurveLength(mxCurve.LABEL_CURVE);
		double currentPos = labelPosition.startBuffer / curveLength;
		// Beyond this value we stop drawing. The +3 is to avoid rounding errors 
		// cutting off the last character
		// double limitPos = (curveLength - endBuffer + 3) / curveLength; // TODO

		mxRectangle overallLabelBounds = null;

		// TODO on translation just move the points, don't recalculate
		// Might be better than the curve is the only thing updated and
		// the curve shapes listen to curve events
		// !lastPoints.equals(curve.getGuidePoints())
		 
		for (int j = 0; j < label.length(); j++)
		{
			mxLine parallel = curve.getCurveParallel(mxCurve.LABEL_CURVE,
					currentPos);
			labelGlyphs[j].glyphVector = parallel;
			labelGlyphs[j].labelGlyphBounds.setX(parallel.getX());
			labelGlyphs[j].labelGlyphBounds.setY(parallel.getY());

			// Hook for sub-classers
			postprocessGlyph(curve, j, currentPos);

			currentPos += (labelGlyphs[j].labelGlyphBounds.getWidth() + labelPosition.defaultInterGlyphSpace)
					/ curveLength;

			if (overallLabelBounds == null)
			{
				overallLabelBounds = (mxRectangle) labelGlyphs[j].labelGlyphBounds
						.clone();
			}
			else
			{
				overallLabelBounds.add(labelGlyphs[j].labelGlyphBounds);
			}
		}

		if (overallLabelBounds == null)
		{
			// Return a small rectangle in the center of the label curve
			// Null label bounds causes NPE when editing
			mxLine labelCenter = curve.getCurveParallel(mxCurve.LABEL_CURVE,
					0.5);
			overallLabelBounds = new mxRectangle(labelCenter.getX(),
					labelCenter.getY(), 1, 1);
		}

		this.labelBounds = overallLabelBounds;
		return overallLabelBounds;
	}

	/**
	 * Hook for sub-classers to perform additional processing on
	 * each glyph
	 * @param j the index of the label
	 * @param currentPos the distance along the label curve the glyph is
	 */
	protected void postprocessGlyph(mxCurve curve, int j, double currentPos)
	{
	}

	/**
	 * Returns whether or not the rectangle passed in hits any part of this
	 * curve.
	 * @param rect the rectangle to detect for a hit
	 * @return whether or not the rectangle hits this curve
	 */
	public boolean intersectsRect(mxRectangle rect)
	{
		// To save CPU, we can test if the rectangle intersects the entire
		// bounds of this label
		if (labelBounds != null
				&& (!labelBounds.getRectangle().intersects(rect.getRectangle())))
		{
			return false;
		}

		for (int i = 0; i < labelGlyphs.length; i++)
		{
			if (rect.getRectangle().intersects(
					labelGlyphs[i].labelGlyphBounds.getRectangle()))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Hook method to override how the label is positioned on the curve
	 * @param style the style of the curve
	 * @param label the string label to be displayed on the curve
	 */
	protected void calculationLabelPosition(Map<String, Object> style,
			String label)
	{
		double curveLength = curve.getCurveLength(mxCurve.LABEL_CURVE);
		double availableLabelSpace = curveLength - labelPosition.startBuffer
				- labelPosition.endBuffer;
		labelPosition.startBuffer = Math.max(labelPosition.startBuffer,
				labelPosition.startBuffer + availableLabelSpace / 2 - labelSize
						/ 2);
		labelPosition.endBuffer = Math.max(labelPosition.endBuffer,
				labelPosition.endBuffer + availableLabelSpace / 2 - labelSize
						/ 2);
	}

	/**
	 * @return the curve
	 */
	public mxCurve getCurve()
	{
		return curve;
	}

	/**
	 * @param curve the curve to set
	 */
	public void setCurve(mxCurve curve)
	{
		this.curve = curve;
	}

	/**
	 * Utility class to describe the characteristics of each glyph of a branch
	 * branch label. Each instance represents one glyph
	 *
	 */
	public class LabelGlyphCache
	{
		/**
		 * Cache of the bounds of the individual element of the label of this 
		 * edge. Note that these are the unrotated values used to determine the 
		 * width of each glyph. To determine the entire label bounds a reasonable 
		 * approximation is to take the union of these rects and add a buffer that 
		 * is the maximum increase that could be formed by rotating all of the 
		 * glyphs by 90 degrees.
		 */
		public mxRectangle labelGlyphBounds;

		/**
		 * The glyph being drawn
		 */
		public String glyph;

		/**
		 * A line parallel to the curve segment at which the element is to be
		 * drawn
		 */
		public mxLine glyphVector;
	}

	/**
	 * Utility class that stores details of how the label is positioned
	 * on the curve
	 */
	public class LabelPosition
	{
		public double startBuffer = LABEL_BUFFER;

		public double endBuffer = LABEL_BUFFER;

		public double defaultInterGlyphSpace = 0;;
	}
}
