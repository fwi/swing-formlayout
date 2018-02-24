package com.github.fwi.swing.formlayout.builder;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Container;

import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fwi.swing.formlayout.AbstractBox;
import com.github.fwi.swing.formlayout.FixedGap;
import com.github.fwi.swing.formlayout.FormConstraints;
import com.github.fwi.swing.formlayout.FormGraphics;

/**
 * Adds component size and constraint functions to the {@link BaseFormBuilder}.
 *
 * @param <T> extended from {@link BaseFormBuilder}.
 */
public class ComponentFormBuilder<T extends ComponentFormBuilder<T>> extends BaseFormBuilder<T> {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ComponentFormBuilder.class);

	public ComponentFormBuilder(FormGraphics formGraphics, Container container) {
		super(formGraphics, container);
	}

	/**
	 * Add previous component to current container
	 * and prepare given component for addition to current container
	 * with constraints (or no constraints which is allowed too).
	 * See also {@link #withSize(String)} and {@link #withSize(FormConstraints)}.
	 */
	public T add(Component c) {
		addComponent();
		this.component = c ;
		return me();
	}
	
	/**
	 * Adds a fixed-sized gap (see {@link FixedGap}) using the parent containter
	 * for {@link FormGraphics} and direction. If parent container is not a {@link AbstractBox},
	 * the default {@link FormGraphics#getInstance()} is used with orientation {@link SwingConstants#VERTICAL}. 
	 */
	public T addGap() {
		
		if (getContainer() instanceof AbstractBox) {
			AbstractBox c = (AbstractBox) getContainer();
			add(new FixedGap(c.getFormGraphics(), c.getDirection()));
		} else {
			add(new FixedGap(FormGraphics.getInstance(), SwingConstants.VERTICAL));
		}
		return me();
	}

	/**
	 * The current/last added/active component.
	 */
	public Component getComponent() { return component; }
	
	/**
	 * The current/last updated form constraints for the current component.
	 */
	public FormConstraints getConstraints() { return constraints; }

	/**
	 * Creates form-constraints for the current component.
	 * @param constraints see {@link FormConstraints#FormConstraints(String)}.
	 */
	public T withSize(String constraints) {
		this.constraints = new FormConstraints(constraints);
		return me();
	}

	/**
	 * Uses the given form-constraints for the current component.
	 * These form-constraints are not copied, they are used directly 
	 * and subsequent call to growx/shrinkx will change the given form-constraints.
	 */
	public T withSize(FormConstraints constraints) {
		this.constraints = constraints;
		return me();
	}

	/**
	 * Sets constraints to a copy of {@link FormConstraints#DEFAULT}.
	 */
	public T withDefaultSize() {
		return setDefaultConstraints();
	}

	protected T setDefaultConstraints() {

		if (constraints == null) {
			constraints = FormConstraints.DEFAULT.copy();
		}
		return me();
	}

	public T sizex(double amount) {
		setDefaultConstraints();
		constraints.sizex = amount;
		return me();
	}

	public T sizey(double amount) {
		setDefaultConstraints();
		constraints.sizey = amount;
		return me();
	}

	public T sizexy(double amount) {
		return sizex(amount).sizey(amount);
	}

	public T growx(double amount) {
		setDefaultConstraints();
		constraints.growx = amount;
		return me();
	}

	public T growy(double amount) {
		setDefaultConstraints();
		constraints.growy = amount;
		return me();
	}

	public T growxy(double amount) {
		return growx(amount).growy(amount);
	}

	public T shrinkx(double amount) {
		setDefaultConstraints();
		constraints.shrinkx = amount;
		return me();
	}

	public T shrinky(double amount) {
		setDefaultConstraints();
		constraints.shrinky = amount;
		return me();
	}

	public T shrinkxy(double amount) {
		return shrinkx(amount).shrinky(amount);
	}

	public T addFillerx() {
		return add(new Canvas()).withSize(FormConstraints.FILLERX);
	}
	public T addFillery() {
		return add(new Canvas()).withSize(FormConstraints.FILLERY);
	}
	public T addFillerxy() {
		return add(new Canvas()).withSize(FormConstraints.FILLERXY);
	}

	/**
	 * Adds a fillerx and the component.
	 */
	public T addTrailing(Component c) {
		return addFillerx().add(c);
	}

	/**
	 * Adds a fillerx and the component.
	 * Sizes for the component can still be set.
	 * Before any container other update is done, the component is followed with another fillerx
	 * (effectively centering the component).
	 */
	public T addCentered(Component c) {
		addFillerx().add(c);
		centered = true;
		return me();
	}

}
