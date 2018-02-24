package com.github.fwi.swing.formlayout;

import javax.swing.SwingConstants;

/**
 * A container using a {@link FormLayoutManager}
 * to layout components horizontally 
 * (following the line-axis using constant {@link SwingConstants#HORIZONTAL}).
 * <br>See also {@link AbstractBox}
 * @author frederik
 *
 */
public class LineBox extends AbstractBox {

	private static final long serialVersionUID = 8319299191461390836L;

	public LineBox() {
		this(null);
	}

	public LineBox(double maxGrowY) {
		this(null, maxGrowY);
	}

	public LineBox(FormGraphics fg) {
		this(fg, 0.0);
	}

	/**
	 * Constructs a horizontal box. 
	 * @param fg used in the layout
	 * @param maxGrowY see {@link #setMaxGrowY(double)}.
	 */
	public LineBox(FormGraphics fg, double maxGrowY) {
		super();
		setFormGraphics(fg);
		setMaxGrowY(maxGrowY);
		setDirection(SwingConstants.HORIZONTAL);
		setLayout(new FormLayoutManager(fg, getDirection()));
	}
	
}
