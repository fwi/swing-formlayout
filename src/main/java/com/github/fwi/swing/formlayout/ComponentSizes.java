package com.github.fwi.swing.formlayout;

import java.awt.Component;
import java.awt.Dimension;

/**
 * Calculates and caches sizes of a component given constraints (if any).
 * @author frederik
 *
 */
public class ComponentSizes {

	protected Component c;
	protected FormConstraints fc;
	protected FormGraphics fg;
	
	private Dimension min, pref, max;
	
	public ComponentSizes(Component c, FormConstraints fc, FormGraphics fg) {
		super();
		this.c = c;
		this.fc = fc;
		this.fg = fg;
	}
	
	public Dimension minSize() {
		
		if (min == null) {
			if (fc == null) {
				min = c.getMinimumSize();
			} else {
				min = new Dimension(fc.minWidth(fg), fc.minHeight(fg));
			}
		}
		return min;
	}

	public Dimension prefSize() {
		
		if (pref == null) {
			if (fc == null) {
				pref = c.getPreferredSize();
			} else {
				pref = new Dimension(fc.prefWidth(fg), fc.prefHeight(fg));
			}
		}
		return pref;
	}

	public Dimension maxSize() {
		
		if (max == null) {
			if (fc == null) {
				max = c.getMaximumSize();
			} else {
				max = new Dimension(fc.maxWidth(fg), fc.maxHeight(fg));
			}
		}
		return max;
	}

	protected void invalidateLayout() {
		min = pref = max  = null;
	}
}
