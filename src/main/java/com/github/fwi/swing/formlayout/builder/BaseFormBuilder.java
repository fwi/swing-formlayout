package com.github.fwi.swing.formlayout.builder;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fwi.swing.formlayout.AbstractBox;
import com.github.fwi.swing.formlayout.FormConstraints;
import com.github.fwi.swing.formlayout.FormGraphics;
import com.github.fwi.swing.formlayout.GraphicsUtil;
import com.github.fwi.swing.formlayout.LineBox;
import com.github.fwi.swing.formlayout.MirrorBox;
import com.github.fwi.swing.formlayout.PageBox;

/**
 * Helper base class for building a form using the generic fluent api design.
 * Sets the given {@link FormGraphics} to all added containers.
 * <br>All up*, down*, add*Box, with*Insets and with*Border methods work on the containers that can receive a component.
 * <br>All other methods work on components (sizes), see {@link ComponentFormBuilder}.
 * <p>
 * Copied from fluent generic design (a.k.a. "Curiously Recurring Template Pattern") described at:
 * <br>http://www.unquietcode.com/blog/2011/programming/using-generics-to-build-fluent-apis-in-java/
 */
public class BaseFormBuilder<T extends BaseFormBuilder<T>> {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(BaseFormBuilder.class);

	protected FormGraphics formGraphics;
	protected FormConstraints constraints;
	protected Component component;
	protected final ArrayList<Container> containers = new ArrayList<>();
	protected boolean centered;
	protected Font borderFont;
	protected int containerIndex;

	public BaseFormBuilder(FormGraphics formGraphics, Container container) {
		super();
		if (container == null) {
			throw new IllegalArgumentException("A root container must be provided.");
		}
		this.formGraphics = (formGraphics == null ? FormGraphics.getInstance() : formGraphics);
		containers.add(container);
	}
	
	@SuppressWarnings("unchecked")
	protected T me() {
		return (T) this;
	}
	
	public FormGraphics getGraphics() {
		return formGraphics;
	}
	
	/**
	 * Adds the current component if needed and returns the root-container.
	 */
	public Container build() {
		addComponent();
		return containers.get(0);
	}

	/**
	 * The current/active container used to add components in.
	 */
	public Container getContainer() {
		return containers.get(containerIndex);
	}

	/**
	 * General method to add a component currently being build to the current container.
	 * This method is not in line with the fluent API design:
	 * some variables are used that are only set in the extending class ComponentFormBuilder.
	 * Not sure how to solve that.
	 */
	protected T addComponent() {

		if (component != null) {
			if (constraints == null) {
				getContainer().add(component);
			} else {
				getContainer().add(component, constraints);
				constraints = null;
			}
			if (centered) {
				getContainer().add(new Canvas(), FormConstraints.FILLERX);
				centered = false;
			}
			component = null;
		}
		return me();
	}

	/**
	 * Go up one in the container-list.
	 * @throws IndexOutOfBoundsException if current container is root container
	 */
	public T up() {

		addComponent();
		if (containerIndex > 0) {
			containerIndex--;
		} else {
			throw new IndexOutOfBoundsException("No further parent containers available.");
		}
		return me();
	}

	/**
	 * Use given container as current container.
	 * @throws NoSuchElementException if given container is not in the container-list
	 */
	public T upTo(Container c) {

		addComponent();
		int i = containers.indexOf(c);
		if (i < 0) {
			throw new NoSuchElementException("Container not available in hierarachy: " + c);
		}
		containerIndex = i;
		return me();
	}

	/**
	 * Set the root container as current container.
	 */
	public T upToRoot() {

		addComponent();
		containerIndex = 0;
		return me();
	}

	public T down() {

		addComponent();
		if (containerIndex < containers.size() - 1) {
			containerIndex++;
		} else {
			throw new IndexOutOfBoundsException("No further child containers available.");
		}
		return me();
	}

	/**
	 * Adds the given container to the current container
	 * and makes the given container the current active container.
	 */
	public T addContainer(Container container) {

		if (container == null) {
			return me();
		}
		addComponent();
		getContainer().add(container);
		containers.add(++containerIndex, container);
		while (containers.size() > containerIndex + 1) containers.remove(containers.size() - 1);
		if (container instanceof AbstractBox) {
			applyMaxGrowFromParentContainers((AbstractBox)container);
		}
		return me();
	}
	
	protected T applyMaxGrowFromParentContainers(AbstractBox box) {
		
		for (int i = 0; i < containerIndex; i++) {
			if (containers.get(i) instanceof AbstractBox) {
				AbstractBox parentBox = (AbstractBox) containers.get(i);
				if (parentBox.getMaxGrowX() > 0.0 
						&& (box.getMaxGrowX() <= 0.0 || box.getMaxGrowX() > parentBox.getMaxGrowX())) {
					box.setMaxGrowX(parentBox.getMaxGrowX());
				}
				if (parentBox.getMaxGrowY() > 0.0 
						&& (box.getMaxGrowY() <= 0.0 || box.getMaxGrowY() > parentBox.getMaxGrowY())) {
					box.setMaxGrowY(parentBox.getMaxGrowY());
				}
			}
		}
		return me();
	}

	public T addPageBox() {
		return addPageBox(0.0);
	}

	public T addPageBox(double maxGrowX) {
		return addContainer(new PageBox(formGraphics, maxGrowX));
	}

	public T addLineBox() {
		return addLineBox(0.0);
	}

	public T addLineBox(double maxGrowY) {
		return addContainer(new LineBox(formGraphics, maxGrowY));
	}

	/**
	 * See {@link #addMirrorBox(Component, int)}.
	 * <br>The direction is taken from the given target box if possible,
	 * else defaults to {@link SwingConstants#VERTICAL}.
	 */
	public T addMirrorBox(Component target) {
		
		int direction = SwingConstants.VERTICAL;
		if (target instanceof AbstractBox) {
			direction = ((AbstractBox)getContainer()).getDirection();
		}
		return addMirrorBox(target, direction);
	}

	/**
	 * Adds a mirror-box using sizes from given target.
	 * It is expected that components are added to the mirror-box after this call,
	 * if not, consider using {@link #addMirrorBox()} or {@link #addMirrorBox(int)} instead.
	 * @param target the component/container to mimic sizes from
	 */
	public T addMirrorBox(Component target, int direction) {
		
		return addContainer(new MirrorBox(formGraphics, target, direction));
	}

	/**
	 * See {@link #addMirrorBox(int)}.
	 * <br>Copies the direction from the current active container.
	 */
	public T addMirrorBox() {
		
		int direction = SwingConstants.VERTICAL;
		if (getContainer() instanceof AbstractBox) {
			direction = ((AbstractBox)getContainer()).getDirection();
		}
		return addMirrorBox(direction);
	}
	
	/**
	 * Adds a {@link MirrorBox} mimicking the current active container.
	 * After return of this method, the active container will be the parent of the currently active container
	 * (i.e {@link #up()} is called before returning).
	 * If components need to be added to the mirror-box, consider using
	 * {@link #addMirrorBox(Component)} or {@link #addMirrorBox(Component, int)} instead.
	 */
	public T addMirrorBox(int direction) {

		MirrorBox mirrorContainer = new MirrorBox(formGraphics, getContainer(), direction);
		return up().addContainer(mirrorContainer).up();
	}

	public T withGapInsets() {
		return withInsets(formGraphics.vgap, formGraphics.hgap);
	}

	public T withInsets(int top, int left) {
		return withInsets(top, left, 0, 0);
	}

	public T withInsets(int top, int left, int bottom, int right) {
		((JComponent)getContainer()).setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
		return me();
	}

	/**
	 * The font set via {@link #setDefaultBorderFont(Font)} or else the {@link GraphicsUtil#getTitledBorderFont()}.titled border font
	 */
	public Font getDefaultBorderFont() {
		return (borderFont == null ? GraphicsUtil.getTitledBorderFont() : borderFont);
	}

	public T setDefaultBorderFont(Font borderFont) {
		this.borderFont = borderFont;
		return me();
	}

	public T withLineBorder(int borderLineSize) {
		return withLineBorder(Color.BLACK, borderLineSize);
	}
	
	public T withLineBorder(Color color, int borderLineSize) {
		
		LineBorder lb = new LineBorder(color, borderLineSize);
		((JComponent)getContainer()).setBorder(lb);
		return me();
	}

	public T withTitledBorder(String title) {
		return withTitledBorder(title, Color.BLACK);
	}

	public T withTitledBorder(String title, Color color) {
		return withTitledBorder(title, color, 2);
	}

	public T withTitledBorder(String title, Color color, Font borderFont) {
		return withTitledBorder(title, color, 2, borderFont);
	}

	public T withTitledBorder(String title, int borderLineSize) {
		return withTitledBorder(title, Color.BLACK, borderLineSize);
	}

	public T withTitledBorder(String title, Color color, int borderLineSize) {
		return withTitledBorder(title, color, borderLineSize, borderFont);
	}

	public T withTitledBorder(String title, Color color, int borderLineSize, Font borderFont) {

		TitledBorder tb;
		if (color == null) {
			tb = new TitledBorder(null, title, TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, borderFont);
		} else {
			tb = new TitledBorder(new LineBorder(color, borderLineSize), title, TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, borderFont);
		}
		((JComponent)getContainer()).setBorder(tb);
		return me();
	}

}
