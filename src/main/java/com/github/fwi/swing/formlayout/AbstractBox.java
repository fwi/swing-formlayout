package com.github.fwi.swing.formlayout;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fwi.swing.formlayout.builder.BaseFormBuilder;

/**
 * Abstract base class for form-box classes {@link LineBox} and {@link PageBox}.
 * Adds options for scrolling (see {@link #setScrollToMinSize(boolean)}, insets (see {@link #setInsets(int, int)})
 * and maximum grow-sizes ({@link #setMaxGrowX(double)} and {@link #setMaxGrowY(double)}).
 * <p>
 * The maximum grow-sizes only work when the whole container-hierarchy (of types of this class) is examined.
 * This is supported by the {@link BaseFormBuilder} (method <tt>applyMaxGrowFromParentContainers</tt>) 
 * but only when containers are added (i.e. changes made to the max-grow sizes after the container of this class was added, 
 * are not adjusted for any max-grow size constraints from parents).
 * <br>The {@link FormLayoutManager} restricts the maximum size of the container to the max-grow size set on the container itself
 * (using method {@link #withinMaxGrow(Dimension)}), the layout manager will not (and cannot) use the max-grow size restrictions 
 * from parent containers. 
 * 
 * @author frederik
 */
public abstract class AbstractBox extends JComponent implements Scrollable {

	private static final long serialVersionUID = 7804719261078140432L;

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AbstractBox.class);

	protected FormGraphics formGraphics = FormGraphics.getInstance();
	protected boolean scrollToMinSize;
	protected int direction;
	protected double maxGrowY;
	protected double maxGrowX;

	/**
	 * See {@link #setScrollToMinSize(boolean)}.
	 */
	public boolean isScrollToMinSize() { return scrollToMinSize; }

	/**
	 * Set to true to have this box use minimum size when scroll-bars are shown.
	 * If this is not set, the contents will jump from minimum size
	 * to preferred size as soon as scrollbars are shown.
	 * <br>Only useful if box is placed directly inside a JScrollPane 
	 * (so that parent can be a JViewport) or similar.
	 */
	public void setScrollToMinSize(boolean scrollToMinSize) { this.scrollToMinSize = scrollToMinSize; }
	
	public void setFormGraphics(FormGraphics fg) { if (fg != null) this.formGraphics = fg; }
	public FormGraphics getFormGraphics() { return formGraphics; }
	
	/** Either {@link SwingConstants#HORIZONTAL}) or {@link SwingConstants#VERTICAL}). */
	public int getDirection() { return direction; }

	/** Either {@link SwingConstants#HORIZONTAL}) or {@link SwingConstants#VERTICAL}). */
	public void setDirection(int direction) { this.direction = direction; }

	/**
	 * Sets insets top/bottom to given vertical size and left/right to given horizontal size.
	 * See also {@link #setInsets(int, int, int, int)}.
	 */
	public void setInsets(int vertical, int horizontal) {
		setInsets(vertical, horizontal, vertical, horizontal);
	}

	/**
	 * Sets the insets using an empty border.
	 */
	public void setInsets(int top, int left, int bottom, int right) {
		setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
	}

	/**
	 * Override preferred size to return minimum size when scrollbars are present.
	 * See also SO question <a href="http://stackoverflow.com/q/12769656/3080094">3080094</a>
	 * and {@link #setScrollToMinSize(boolean)}.
	 */
	@Override
	public Dimension getPreferredSize() {
		
		Dimension size = super.getPreferredSize();
		if (scrollToMinSize && getParent() instanceof JViewport) {
			if (!getScrollableTracksViewportWidth()) {
				size.width = getMinimumSize().width;
			}
			if (!getScrollableTracksViewportHeight()) {
				size.height = getMinimumSize().height;
			}
		}
		return size;
	}

	/* *** Scrollable methods *** */
	
	@Override
	public boolean getScrollableTracksViewportWidth() {
		return getParent() instanceof JViewport
				&& getParent().getWidth() >= getMinimumSize().width;
	}
	@Override
	public boolean getScrollableTracksViewportHeight() {
		return getParent() instanceof JViewport
				&& getParent().getHeight() >= getMinimumSize().height;
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return super.getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return (orientation == SwingConstants.VERTICAL ? 
				formGraphics.dheight + formGraphics.vgap: 
					(int) (formGraphics.dheight * formGraphics.squareSizeX) + formGraphics.hgap);
	}
	
	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return (orientation == SwingConstants.VERTICAL ? 
				formGraphics.dheight + formGraphics.vgap:
					formGraphics.dwidth + formGraphics.hgap);
	}

	/* *** MAX grow bounds *** */
	
	public double getMaxGrowX() {
		return maxGrowX;
	}

	/**
	 * Limits the width of the box to the given value times {@link FormGraphics#dwidth}.
	 * @param maxGrowX a value of <tt>0.0</tt> or smaller is used as "no limit".
	 * A value between 0 and 1 is divided by 1.
	 */
	public void setMaxGrowX(double maxGrowX) {
		this.maxGrowX = (maxGrowX <= 0.0 ? 0.0 : maxGrowX < 1.0 ? 1.0 / maxGrowX : maxGrowX);
	}
	
	public double getMaxGrowY() {
		return maxGrowY;
	}

	/**
	 * Limits the height of the box to the given value times {@link FormGraphics#dheight}.
	 * @param maxGrowY a value of <tt>0.0</tt> or smaller is used as "no limit".
	 * A value between 0 and 1 is divided by 1.
	 */
	public void setMaxGrowY(double maxGrowY) {
		this.maxGrowY = (maxGrowY <= 0.0 ? 0.0 : maxGrowY < 1.0 ? 1.0 / maxGrowY : maxGrowY);
	}

	/**
	 * Used by {@link FormLayoutManager} to limit the maximum size of all components in the container,
	 * to the maximum grow-size set in this container-box.
	 * See {@link #setMaxGrowX(double)} and {@link #setMaxGrowY(double)}.
	 * @param maxLayoutSize the maximum size of all components in the container 
	 * @return the maximum size limited to the maximum grow-size of this container
	 */
	public Dimension withinMaxGrow(Dimension maxLayoutSize) {

		if (getMaxGrowX() <= 0.0 && getMaxGrowY() <= 0.0) {
			return maxLayoutSize;
		}
		int maxWidth = maxLayoutSize.width;
		if (getMaxGrowX() > 0.0) {
			maxWidth =  FormGraphics.roundup(getFormGraphics().dwidth * getMaxGrowX()); 
			maxWidth = FormConstraints.wihtinWindowWidth(getFormGraphics(), maxWidth);
			if (maxWidth > maxLayoutSize.width) {
				maxWidth = maxLayoutSize.width;
			}
		}
		int maxHeight = maxLayoutSize.height;
		if (getMaxGrowY() > 0.0) {
			maxHeight = FormGraphics.roundup(getFormGraphics().dheight * getMaxGrowY());
			maxHeight = FormConstraints.wihtinWindowHeight(getFormGraphics(), maxHeight);
			if (maxHeight > maxLayoutSize.height) {
				maxHeight = maxLayoutSize.height;
			}
		}
		maxLayoutSize = new Dimension(maxWidth, maxHeight);
		return maxLayoutSize;
	}
	
}
