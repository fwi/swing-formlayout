package com.github.fwi.swing.formlayout;

import java.awt.Canvas;
import java.awt.Dimension;

import javax.swing.SwingConstants;

public class FixedGap extends Canvas {

	private static final long serialVersionUID = -5131638878856178231L;
	
	protected FormGraphics formGraphics; 
	protected int direction;
	
	public FixedGap(FormGraphics formGraphics, int direction) {
		super();
		this.formGraphics = formGraphics;
		this.direction = direction;
	}
	
	@Override
	public Dimension getMinimumSize() {
		return (direction == SwingConstants.HORIZONTAL ? 
				new Dimension(formGraphics.hgap, 1) : new Dimension(1, formGraphics.vgap));
	}

	@Override
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	@Override
	public Dimension getMaximumSize() {
		return getMinimumSize();
	}

}
