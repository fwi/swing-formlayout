package com.github.fwi.swing.formlayout;

import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A container using a {@link FormLayoutManager}
 * to layout components vertically 
 * (following the page-axis using constant {@link SwingConstants#VERTICAL}).
 * <br>See also {@link AbstractBox}
 */
public class PageBox extends AbstractBox {

	private static final long serialVersionUID = -5579400522618528218L;

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(PageBox.class);

	public PageBox() {
		this(null);
	}

	public PageBox(double maxGrowX) {
		this(null, maxGrowX);
	}

	public PageBox(FormGraphics fg) {
		this(fg, 0.0);
	}

	/**
	 * Constructs a vertical box. 
	 * @param fg used in the layout
	 * @param maxGrowX see {@link #setMaxGrowX(double)}.
	 */
	public PageBox(FormGraphics fg, double maxGrowX) {
		super();
		setFormGraphics(fg);
		setMaxGrowX(maxGrowX);
		setDirection(SwingConstants.VERTICAL);
		setLayout(new FormLayoutManager(fg));
	}

}
