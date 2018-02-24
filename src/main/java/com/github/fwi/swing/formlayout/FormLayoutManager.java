package com.github.fwi.swing.formlayout;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingConstants;

import static javax.swing.SwingConstants.VERTICAL;
import static javax.swing.SwingConstants.HORIZONTAL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A layout manager that uses {@link FormConstraints} to calculate component location and size.
 * If no form-constraints are set for a component, the min/pref/max-sizes from the component itself are used.
 * The values for gap sizes, row height and column width are retrieved from {@link FormGraphics}.
 * The layout manager can either layout components {@link SwingConstants#HORIZONTAL}
 * or {@link SwingConstants#VERTICAL}. Combining these layouts using (many) boxes
 * (see {@link LineBox} and {@link PageBox}) allows for the creation of forms that resize properly in 2D.
 * <p>
 * An instance of this class can NOT be re-used. 
 * Some (size related) data is cached during calculations 
 * which means that one instance of this class used in two containers will result in faulty calculations.
 * <p>
 * Alternatives: <a href="http://www.jgoodies.com/freeware/libraries/forms/">JGoodies FormLayout</a>
 * and <a href="http://www.miglayout.com/">MigLayout</a>
 */
public class FormLayoutManager implements LayoutManager2 {

	private static final Logger log = LoggerFactory.getLogger(FormLayoutManager.class);

	protected int direction;
	protected final Map<Component, FormConstraints> constraints = new HashMap<>();
	protected final Map<Component, ComponentSizes> sizes = new HashMap<>();
	protected FormGraphics graphics;
	protected boolean useMirrorSizes;

	/**
	 * Aligns components vertically, see also {@link FormLayoutManager#FormLayoutManager(int)}.
	 */
	public FormLayoutManager() {
		this(VERTICAL);
	}

	/**
	 * Aligns components vertically, see also {@link FormLayoutManager#FormLayoutManager(int)}.
	 */
	public FormLayoutManager(FormGraphics graphics) {
		this(graphics, VERTICAL);
	}

	/**
	 * Aligns components vertically or horizontally.
	 * @param direction either {@link SwingConstants#VERTICAL} or {@link SwingConstants#HORIZONTAL}
	 */
	public FormLayoutManager(int direction) {
		this(null, direction);
	}

	/**
	 * Aligns components vertically or horizontally.
	 * @param direction either {@link SwingConstants#VERTICAL} or {@link SwingConstants#HORIZONTAL}
	 */
	public FormLayoutManager(FormGraphics graphics, int direction) {
		super();
		if (graphics == null) {
			this.graphics = FormGraphics.getInstance();
		} else {
			this.graphics = graphics;
		}
		if (direction == VERTICAL) {
			this.direction = VERTICAL;
		} else {
			this.direction = HORIZONTAL;
		}
		if (log.isTraceEnabled()) {
			log.trace(logId() + " " + (direction == HORIZONTAL ? " line" : "page") + " axis");
		}
	}

	public FormGraphics getFormGraphics() { return graphics; }
	public void setFormGraphics(FormGraphics formGraphics) { if (formGraphics != null) this.graphics = formGraphics; }

	/**
	 * See {@link #setUseMirrorSizes(boolean)}).
	 */
	public boolean isUseMirrorSizes() { return useMirrorSizes; }

	/**
	 * For usage with a {@link MirrorBox}.
	 * Omits gap-calculations for min/pref/max layout sizes and just takes the sizes
	 * directly from the component being mimicked.
	 */
	public void setUseMirrorSizes(boolean useMirrorSizes) { this.useMirrorSizes = useMirrorSizes; }

	/**
	 * Deprecated method. Name is used as constraints as with {@link #addLayoutComponent(Component, Object)}.
	 */
	@Override
	public void addLayoutComponent(String constraints, Component comp) {
		addLayoutComponent(comp, constraints);
	}

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {

		/*
		 * Somehow the over-heritence does not work here
		 */
		if (constraints == null) {
			addLayoutComponent(comp, (FormConstraints) null);
		} else if (constraints instanceof String) {
			addLayoutComponent(comp, (String) constraints);
		} else if (constraints instanceof FormConstraints) {
			addLayoutComponent(comp, (FormConstraints) constraints);
		} else {
			addLayoutComponent(comp, (FormConstraints) null);
		}
	}

	public void addLayoutComponent(Component comp, String constraints) {

		if (constraints == null) {
			addLayoutComponent(comp, (FormConstraints) null);
		} else {
			if (constraints.equals(FormConstraints.EMPTY_STRING) || constraints.trim().isEmpty()) {
				addLayoutComponent(comp, FormConstraints.DEFAULT);
			} else {
				addLayoutComponent(comp, new FormConstraints(constraints));
			}
		}
	}

	public void addLayoutComponent(Component comp, FormConstraints componentConstraints) {

		constraints.put(comp, componentConstraints);
		if (log.isDebugEnabled()) {
			log.debug("{} added {} - {}", logId(), comp.getClass().getSimpleName(), 
					(componentConstraints== null ? -1.0 : componentConstraints.sizex));
		}
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		constraints.remove(comp);
		// invalidate is called later by the parent container.
	}

	@Override
	public void layoutContainer(Container target) {

		synchronized(target.getTreeLock()) {
			layoutContainerSynced(target);
		}
	}

	public ComponentSizes getSizes(Component c) {

		ComponentSizes cs = sizes.get(c);
		if (cs == null) {
			cs = new ComponentSizes(c, constraints.get(c), graphics);
			sizes.put(c, cs);
		}
		return cs;
	}

	/*
	 * layoutSize are calculated further on.
	 */
	protected Dimension minLayoutSize;
	protected Dimension prefLayoutSize;
	protected Dimension maxLayoutSize;

	/* *** The heart of the layout manager, calculating sizes for components adjusted to fit available space. *** */

	protected void layoutContainerSynced(Container target) {

		if (minLayoutSize == null || prefLayoutSize == null || maxLayoutSize == null) {
			calculateLayoutSizesSynced(target);
		}
		// A negative size-change means shrinking is required.
		int varSize = (direction == HORIZONTAL ? 
				target.getWidth() - prefLayoutSize.width :
					target.getHeight() - prefLayoutSize.height);
		List<Component> varSizeComponents = new LinkedList<>();
		findVarSizeComponents(target, varSize, varSizeComponents);
		Map<Component, Integer> varSizes = new HashMap<>();
		calculateVarSizes(target, varSize, varSizeComponents, varSizes);
		layoutComponents(target, varSizes);
	}

	protected void findVarSizeComponents(Container target, int varSize, List<Component> varSizeComponents) {

		if (varSize == 0) {
			return;
		}
		for (Component c : target.getComponents()) {
			if (!c.isVisible()) {
				continue;
			}
			if (hasVariableSize(c, varSize)) {
				varSizeComponents.add(c);
			}
		}
	}

	private boolean hasVariableSize(Component c, int varSize) {

		ComponentSizes cs = getSizes(c);
		if (varSize > 0) {
			if (direction == HORIZONTAL
					&& cs.maxSize().width > cs.prefSize().width) {
				return true;
			} else if (direction == VERTICAL
					&& cs.maxSize().height > cs.prefSize().height) {
				return true;
			}
		} else { // shrinking
			if (direction == HORIZONTAL
					&& cs.prefSize().width > cs.minSize().width) {
				return true;
			} else if (direction == VERTICAL
					&& cs.prefSize().height > cs.minSize().height) {
				return true;
			}
		}
		return false;
	}

	/*
	 * This is a complex method but one pattern is repeated 4 times:
	 * for each component that can be adjusted, 
	 * adjust the size in equal steps for all components.
	 * If a component can no longer be resized (min or max-size reached)
	 * remove the component from the adjustable-list
	 * and try to resize the remaining components with the remaining size to change.
	 * 
	 * The adjustedSizes-map will contain the size-change per component 
	 * when this is finished.
	 */
	protected void calculateVarSizes(final Container target, final int varSize, 
			List<Component> varSizeComponents, Map<Component, Integer> varSizes) {

		if (varSizeComponents.size() < 1 || varSize == 0) {
			return;
		}
		int sizeChangePerComponent = Math.round(varSize / varSizeComponents.size());
		if (sizeChangePerComponent == 0) {
			sizeChangePerComponent = (varSize > 0 ? 1 : -1);
		}
		int available = varSize;
		while (varSizeComponents.size() > 0) {
			if (available == 0 || (available < 0 && varSize > 0) || (available > 0 && varSize < 0)) {
				break;
			}
			Component[] components = varSizeComponents.toArray(new Component[varSizeComponents.size()]);
			for (Component c : components) {
				ComponentSizes cs = getSizes(c);
				if (varSize > 0) { // growing
					int prevChange = (varSizes.containsKey(c) ? varSizes.get(c) : 0);
					if (direction == HORIZONTAL) {
						int w = cs.prefSize().width + 
								(available > sizeChangePerComponent ? sizeChangePerComponent : available)
								+ prevChange;
						if (w >= cs.maxSize().width) {
							w = cs.maxSize().width;
							varSizeComponents.remove(c);
						}
						int adjusted = w - cs.prefSize().width;
						varSizes.put(c, adjusted);
						available -= (adjusted - prevChange); 
					} else {
						int h = cs.prefSize().height + 
								(available > sizeChangePerComponent ? sizeChangePerComponent : available)
								+ prevChange;
						if (h >= cs.maxSize().height) {
							h = cs.maxSize().height;
							varSizeComponents.remove(c);
						}
						int adjusted = h - cs.prefSize().height;
						varSizes.put(c, adjusted);
						available -= (adjusted - prevChange); 
					}
				} else { // shrinking, variable "available" is a negative number.
					int prevChange = (varSizes.containsKey(c) ? varSizes.get(c) : 0);
					if (direction == HORIZONTAL) {
						int w = cs.prefSize().width + 
								(available < sizeChangePerComponent ? sizeChangePerComponent : available)
								+ prevChange;
						if (w <= cs.minSize().width) {
							w = cs.minSize().width;
							varSizeComponents.remove(c);
						}
						int adjusted = w - cs.prefSize().width;
						varSizes.put(c, adjusted);
						available += (prevChange - adjusted); 
					} else {
						int h = cs.prefSize().height + 
								(available < sizeChangePerComponent ? sizeChangePerComponent : available)
								+ prevChange;
						if (h <= cs.minSize().height) {
							h = cs.minSize().height;
							varSizeComponents.remove(c);
						}
						int adjusted = h - cs.prefSize().height;
						varSizes.put(c, adjusted);
						available += (prevChange - adjusted); 
					}
				} // if growing / shrinking
			} // for each adjustable component
		} // while adjustable components
	} // calculateVarSizes

	protected void layoutComponents(Container target, Map<Component, Integer> varSizes) {

		boolean ltr = (target.getComponentOrientation() != ComponentOrientation.RIGHT_TO_LEFT);
		Insets insets = target.getInsets();
		int x = (ltr ? insets.left : target.getWidth() - insets.left);
		int y = insets.top;
		int availableSize = (direction == HORIZONTAL ?
				target.getHeight() - insets.top - insets.bottom :
					target.getWidth() - insets.left - insets.right);
		for (Component c : target.getComponents()) {
			if (!c.isVisible()) {
				continue;
			}
			ComponentSizes cs = getSizes(c);
			int sizex = (direction == HORIZONTAL ? cs.prefSize().width : availableSize);
			int sizey = (direction == HORIZONTAL ? availableSize : cs.prefSize().height);
			if (varSizes.containsKey(c)) {
				if (direction == HORIZONTAL) {
					sizex += varSizes.get(c);
				} else {
					sizey += varSizes.get(c);
				}
			}
			if (direction == HORIZONTAL) {
				if (cs.maxSize().height < sizey) {
					sizey = cs.maxSize().height;
				} else if (cs.minSize().height > sizey) {
					sizey = cs.minSize().height;
				}
				c.setBounds(ltr ? x : x - sizex, y, sizex, sizey);
				x = (ltr ? x + sizex + graphics.hgap : x - sizex - graphics.hgap);
			} else {
				if (cs.maxSize().width < sizex) {
					sizex = cs.maxSize().width;
				} else if (cs.minSize().width > sizex) {
					sizex = cs.minSize().width;
				}
				c.setBounds(ltr ? x : x - sizex, y, sizex, sizey);
				y += sizey + graphics.vgap;
			}
			if (log.isTraceEnabled()) {
				log.trace("{} {} bounds {}", logId(), c.getClass().getSimpleName(), c.getBounds());
			}
		}
	}

	/* *** Calculating the min / pref / max sizes from components and using gaps and insets. * ***/

	protected void calculateLayoutSizes(Container target) {

		synchronized(target.getTreeLock()) {
			calculateLayoutSizesSynced(target);
		}
	}

	protected void calculateLayoutSizesSynced(Container target) {

		if (isUseMirrorSizes()) {
			if (log.isTraceEnabled()) {
				log.trace("{} copying sizes from mirrorbox.", logId());
			}
			minLayoutSize = target.getComponent(0).getMinimumSize();
			prefLayoutSize = target.getComponent(0).getPreferredSize();
			maxLayoutSize = target.getComponent(0).getMaximumSize();
			return;
		}
		minLayoutSize = new Dimension(0, 0);
		prefLayoutSize = new Dimension(0, 0);
		maxLayoutSize = new Dimension(0, 0);
		int visibleComponents = 0;
		for (Component c : target.getComponents()) {
			if (!c.isVisible()) {
				continue;
			}
			visibleComponents++;
			ComponentSizes cs = getSizes(c);
			if (direction == HORIZONTAL) {
				minLayoutSize.width += cs.minSize().width + graphics.hgap;
				prefLayoutSize.width += cs.prefSize().width + graphics.hgap;
				maxLayoutSize.width += cs.maxSize().width + graphics.hgap;
				minLayoutSize.height = Math.max(minLayoutSize.height, cs.minSize().height);
				prefLayoutSize.height = Math.max(prefLayoutSize.height, cs.prefSize().height);
				maxLayoutSize.height = Math.max(maxLayoutSize.height, cs.maxSize().height);
			} else {
				minLayoutSize.height += cs.minSize().height + graphics.vgap;
				prefLayoutSize.height += cs.prefSize().height + graphics.vgap;
				maxLayoutSize.height += cs.maxSize().height + graphics.vgap;
				minLayoutSize.width = Math.max(minLayoutSize.width, cs.minSize().width);
				prefLayoutSize.width = Math.max(prefLayoutSize.width, cs.prefSize().width);
				maxLayoutSize.width = Math.max(maxLayoutSize.width, cs.maxSize().width);
			}
		} // for components
		Insets insets = target.getInsets();
		int insetsWidth = insets.left + insets.right;
		int insetsHeight = insets.top + insets.bottom;
		minLayoutSize.width += insetsWidth;
		prefLayoutSize.width += insetsWidth;
		maxLayoutSize.width += insetsWidth;
		minLayoutSize.height += insetsHeight;
		prefLayoutSize.height += insetsHeight;
		maxLayoutSize.height += insetsHeight;
		// remove last gap to prevent doubling up on gaps
		if (visibleComponents > 0) {
			if (direction == HORIZONTAL) {
				minLayoutSize.width -= graphics.hgap;
				prefLayoutSize.width -= graphics.hgap;
				maxLayoutSize.width -= graphics.hgap;
			} else {
				minLayoutSize.height -= graphics.vgap;
				prefLayoutSize.height -= graphics.vgap;
				maxLayoutSize.height -= graphics.vgap;
			}
		}
		if (target instanceof AbstractBox) {
			maxLayoutSize = ((AbstractBox)target).withinMaxGrow(maxLayoutSize); 
		}
		if (log.isTraceEnabled()) {
			log.trace("{} calculated sizes min {} / pref {} / max {}", logId(), 
					toString(minLayoutSize), toString(prefLayoutSize), toString(maxLayoutSize));
		}
	}

	public static String toString(Dimension d) {
		return "[" + d.width + ", " + d.height + "]";
	}

	protected String logId() {
		return getClass().getSimpleName() + "/" +  direction + "@" + hashCode();
	}

	@Override
	public synchronized Dimension minimumLayoutSize(Container target) {

		if (minLayoutSize == null) {
			calculateLayoutSizes(target);
		}
		return minLayoutSize;
	}

	@Override
	public synchronized Dimension preferredLayoutSize(Container target) {

		if (prefLayoutSize == null) {
			calculateLayoutSizes(target);
		}
		if (log.isTraceEnabled()) {
			log.trace("{} prefsize {}", logId(), prefLayoutSize);
		}
		return prefLayoutSize;
	}

	@Override
	public synchronized Dimension maximumLayoutSize(Container target) {

		if (maxLayoutSize == null) {
			calculateLayoutSizes(target);
		}
		return maxLayoutSize;
	}

	/**
	 * Indicates that a child has changed its layout related information,
	 * and thus any cached calculations should be flushed.
	 * <br>However, it appears this method is called way too often by Swing - even when nothing has changed. 
	 * <p>
	 * Synchronized method, see comments in {@link javax.swing.BoxLayout#invalidateLayout(Container)}.
	 */
	@Override
	public synchronized void invalidateLayout(Container target) {

		minLayoutSize = prefLayoutSize = maxLayoutSize = null;
		sizes.clear();
		if (log.isTraceEnabled()) {
			log.trace("{} invalidated layout", logId());
		}
	}

	@Override
	public float getLayoutAlignmentX(Container target) {
		return 0;
	}

	@Override
	public float getLayoutAlignmentY(Container target) {
		return 0;
	}

}
