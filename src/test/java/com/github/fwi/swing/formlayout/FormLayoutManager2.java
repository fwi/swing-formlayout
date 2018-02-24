package com.github.fwi.swing.formlayout;

import static javax.swing.SwingConstants.HORIZONTAL;
import static javax.swing.SwingConstants.VERTICAL;

import java.awt.Component;
import java.awt.Container;
import java.util.List;
import java.util.Map;

import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fwi.swing.formlayout.ComponentSizes;
import com.github.fwi.swing.formlayout.FormGraphics;
import com.github.fwi.swing.formlayout.FormLayoutManager;

/**
 * Alternative to {@link FormLayoutManager}
 * that calculates sizes relative to how much components want to grow or shrink.
 * This provides a more even distribution among components that can change size,
 * but the calculations cause a small rounding error which mis-aligns components by one pixel.
 * This may seem trivial but is very annoying in a form.
 * <p>
 * Apart from that, components can jump or be pushed off the window.
 * That can be fixed, but requires more work.
 * <p>
 * Size calculation methods copied from HVLayout:
 * https://github.com/fwi/HVLayout
 * 
 */
public class FormLayoutManager2 extends FormLayoutManager {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(FormLayoutManager2.class);

	/**
	 * Aligns components vertically, see also {@link FormLayoutManager2#FormLayoutManager(int)}.
	 */
	public FormLayoutManager2() {
		this(VERTICAL);
	}

	/**
	 * Aligns components vertically, see also {@link FormLayoutManager2#FormLayoutManager(int)}.
	 */
	public FormLayoutManager2(FormGraphics graphics) {
		this(graphics, VERTICAL);
	}

	/**
	 * Aligns components vertically or horizontally.
	 * @param direction either {@link SwingConstants#VERTICAL} or {@link SwingConstants#HORIZONTAL}
	 */
	public FormLayoutManager2(int direction) {
		this(null, direction);
	}

	/**
	 * Aligns components vertically or horizontally.
	 * @param direction either {@link SwingConstants#VERTICAL} or {@link SwingConstants#HORIZONTAL}
	 */
	public FormLayoutManager2(FormGraphics graphics, int direction) {
		super(graphics, direction);
	}

	private double varSizeChange;

	protected void findVarSizeComponents(Container target, int varSize, List<Component> varSizeComponents) {

		varSizeChange = 0.0;
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
				varSizeChange += cs.prefSize().width;
				return true;
			} else if (direction == VERTICAL
					&& cs.maxSize().height > cs.prefSize().height) {
				varSizeChange += cs.prefSize().height;
				return true;
			}
		} else { // shrinking
			if (direction == HORIZONTAL
					&& cs.prefSize().width > cs.minSize().width) {
				varSizeChange += cs.prefSize().width - cs.minSize().width;
				return true;
			} else if (direction == VERTICAL
					&& cs.prefSize().height > cs.minSize().height) {
				varSizeChange += cs.prefSize().height - cs.minSize().height;
				return true;
			}
		}
		return false;
	}

	@Override
	protected void calculateVarSizes(final Container target, int varSize, 
			List<Component> varSizeComponents, Map<Component, Integer> varSizes) {

		if (varSizeComponents.size() < 1 || varSize == 0) {
			return;
		}
		boolean componentHasReachedMaxVarSize = false;
		do {
			Component[] components = varSizeComponents.toArray(new Component[varSizeComponents.size()]);
			for (Component c : components) {
				if (hasMaxVarSize(c, varSize, varSizes)) {
					varSizeComponents.remove(c);
					varSize -= varSizes.get(c);
					componentHasReachedMaxVarSize = true;
				}
			}			
		} while (componentHasReachedMaxVarSize && varSizeComponents.size() > 0);
		for (Component c : varSizeComponents) {
			ComponentSizes cs = getSizes(c);
			// ROUNDING ERRORS: Always one pixel off ...
			double vsize = getVarSize(cs, varSize);
			if (vsize > 0) {
				vsize -= 0.49;
			} else {
				vsize += 0.49;
			}
			varSizes.put(c, new Long(Math.round(vsize)).intValue());
		}
	} // calculateVarSizes

	private boolean hasMaxVarSize(Component c, int varSize, Map<Component, Integer> varSizes) {

		ComponentSizes cs = getSizes(c);
		if (varSize > 0) {
			if (direction == HORIZONTAL
					&& getVarSize(cs, varSize) > cs.maxSize().width) {
				varSizeChange -= cs.prefSize().width;
				varSizes.put(c, cs.maxSize().width - cs.prefSize().width);
				return true;
			} else if (direction == VERTICAL
					&& getVarSize(cs, varSize) > cs.maxSize().height) {
				varSizeChange -= cs.prefSize().height;
				varSizes.put(c, cs.maxSize().height - cs.prefSize().height);
				return true;
			}
		} else { // shrinking
			if (direction == HORIZONTAL
					&& cs.prefSize().width - getVarSize(cs, varSize) < cs.minSize().width) {
				varSizeChange -= cs.prefSize().width - cs.minSize().width;
				varSizes.put(c, cs.minSize().width - cs.prefSize().width);
				return true;
			} else if (direction == VERTICAL
					&& cs.prefSize().height - getVarSize(cs, varSize) < cs.minSize().height) {
				varSizeChange -= cs.prefSize().height - cs.minSize().height;
				varSizes.put(c, cs.minSize().height - cs.prefSize().height);
				return true;
			}
		}
		return false;
	}
	
	private double getVarSize(ComponentSizes cs, int varSize) {
		
		if (varSize > 0) {
			if (direction == HORIZONTAL) {
				return varSize * (cs.prefSize().width / (double) varSizeChange);
			} else if (direction == VERTICAL) {
				return varSize * (cs.prefSize().height / (double) varSizeChange);
			}
		} else { // shrinking
			if (direction == HORIZONTAL) {
				return varSize * ((cs.prefSize().width - cs.minSize().width) / (double) varSizeChange);
			} else if (direction == VERTICAL) {
				return varSize * ((cs.prefSize().height - cs.minSize().height) / (double) varSizeChange);
			}
		}
		return 0.0;
	}

}
