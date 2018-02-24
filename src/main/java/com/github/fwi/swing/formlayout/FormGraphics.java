package com.github.fwi.swing.formlayout;

import java.awt.Rectangle;

import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for {@link FormConstraints}, {@link FormLayoutManager} and related {@link AbstractBox}.
 * <br>This class sets the default for the default form (line) height and form (button) width, 
 * along with used gaps. The {@link #getInstance()} is used as default, 
 * but it is recommended to use a new instance (which copies values from the defualt) 
 * so that custom gaps etc. can be adjusted when needed.
 * <p>
 * After a UI change (e.g. {@link GraphicsUtil#resizeApplicationFont(float)}) the {@link #init()}
 * must be called to adjust the defaults to accomodate new screen (default) sizes.  
 * @author frederik
 *
 */
public class FormGraphics {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(FormGraphics.class);

	private static class InstanceHolder {
		private static final FormGraphics instance = new FormGraphics(null);
	}
	
	public static FormGraphics getInstance() {
		return InstanceHolder.instance;
	}
	
	/**
	 * Defaults to 16:9. Used in calculation for {@link #squareSizeX}.
	 */
	public static double DisplayAspectRatio = (16.0 / 9.0);
	
	/** The maximum window sizes. See also {@link #init()}.*/
	public int maxWindowWidth, maxWindowHeight;
	
	/** The default form (line) height and (button) width. See also {@link #init()}.*/
	public int dheight, dwidth;
	
	/** The default gaps, initialized to 2 for vertical and 3 for horizontal. */
	public int vgap, hgap;
	
	/** 
	 * A factor to multiply with vertical size to get horizontal-size to show a square box on screen.
	 * Set in the {@link #init()} method but relies on the {@link #DisplayAspectRatio} which is not detected,
	 * just set by default to 16:9. 
	 */
	public double squareSizeX;
	
	/**
	 * Create a new instance copying values from {@link #getInstance()}.
	 */
	public FormGraphics() {
		this(getInstance());
	}

	public FormGraphics(FormGraphics original) {
		super();
		if (original == null) {
			init();
		} else {
			original.copyTo(this);
		}
	}
	
	/**
	 * Sets the default values for all public fields of this class.
	 * <br>The default (line) height is taken from a {@link JTextField}
	 * and the default (button) width is 5 times the default height.
	 * <br>The maximum window sizes are taken from {@link GraphicsUtil#getScreenSizes()}.
	 * <br>The value for {@link #squareSizeX} is guestimated using the {@link FormGraphics#DisplayAspectRatio}.
	 */
	public void init() {
		
		Rectangle[] screenBounds = GraphicsUtil.getScreenSizes();
		Rectangle maxSizes = screenBounds[1];
		maxWindowWidth = maxSizes.width - maxSizes.x;
		maxWindowHeight = maxSizes.height - maxSizes.y;
		JTextField line = new JTextField("y'old graphics Test");
		dheight = line.getPreferredSize().height;
		dwidth = dheight * 5;
		squareSizeX = (dheight / (dwidth * 1.0)) * (1.0 / DisplayAspectRatio) 
				* (screenBounds[0].getWidth() / screenBounds[0].getHeight());
		vgap = 2;
		hgap = 3;
	}
	
	public void copyTo(FormGraphics copy) {
		
		copy.dheight = dheight;
		copy.hgap = hgap;
		copy.maxWindowHeight = maxWindowHeight;
		copy.maxWindowWidth = maxWindowWidth;
		copy.dwidth = dwidth;
		copy.squareSizeX = squareSizeX;
		copy.vgap = vgap;
	}
	
	/**
	 * Rounds double up to int, e.g 1.1 --> 2
	 */
	public static int roundup(double d) {
		return new Long(Math.round(0.499 + d)).intValue();
	}
	
}
