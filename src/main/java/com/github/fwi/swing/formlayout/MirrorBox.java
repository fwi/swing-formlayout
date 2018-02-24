package com.github.fwi.swing.formlayout;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A mirror-box mimics the sizes of another box but does not display anything (empty canvas), or,
 * in case one or more components are added, draws the components according to the min/pref/max-sizes of the mirrored box.
 * <br>The first case (empty canvas) can be used to keep boxes aligned, 
 * allthough it never works perfectly for all window sizes but it can be good enough.
 * <br>The second case (components are added) can be used to make boxes with different components
 * keep the same sizing behavior. This also helps alignment of boxes but it does require the
 * components in the mirror-box to adjust their grow/shrink rates to fit within the boundaries of the mirror-box
 * (e.g. the total minimum size of the mirror-box will not be the total minimum size of all components
 * in the mirror-box because the minimum size is fixed to the minimum size of the mirrored box).  
 */
public class MirrorBox extends AbstractBox {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(FormLayoutManager.class);

	private static final long serialVersionUID = 4706724496432701449L;

	protected transient MirrorCanvas mirrorCanvas;
	protected transient FormLayoutManager mirrorLayout;
	protected boolean haveComponents;
	protected final transient Component mirrorTarget;

	/**
	 * See {@link MirrorBox#MirrorBox(FormGraphics, Component)}
	 * <br>The form-graphics are taken from the given target box if available,
	 * otherwise defaults to {@link FormGraphics#getInstance()}.
	 */
	public MirrorBox(Component target) {
		this(target instanceof AbstractBox ? ((AbstractBox)target).getFormGraphics() :
				FormGraphics.getInstance(), target);
	}

	/**
	 * See {@link MirrorBox#MirrorBox(FormGraphics, Component, int)}
	 * <br>The direction is taken from the given target box if possible,
	 * else defaults to {@link SwingConstants#VERTICAL}.
	 */
	public MirrorBox(FormGraphics formGraphics, Component target) {
		this(formGraphics, target, 
				target instanceof AbstractBox ? ((AbstractBox)target).getDirection() : SwingConstants.VERTICAL);
	}
	
	/**
	 * Creates a mirror-box. Uses a {@link FormLayoutManager} with the mirror-option set 
	 * ({@link FormLayoutManager#setUseMirrorSizes(boolean)}).
	 * The mirror-option is unset when one or more components are added,
	 * but these components will still obey the sizes from the mirrored component.
	 * @param formGraphics used by layout-manager if components are added
	 * @param target the container to mimic the size from.
	 * @param direction correct value is needed in case one or more components are added to this container, otherwise direction has no influence.
	 */
	public MirrorBox(FormGraphics formGraphics, Component target, int direction) {
		super();
		setFormGraphics(formGraphics);
		setDirection(direction);
		mirrorLayout = new FormLayoutManager(formGraphics, direction);
		mirrorLayout.setUseMirrorSizes(true);
		setLayout(mirrorLayout);
		mirrorTarget = target;
		super.addImpl(mirrorCanvas = new MirrorCanvas(), null, -1);
	}
	
	@Override
    protected void addImpl(Component comp, Object constraints, int index) {

		if (!haveComponents) {
			remove(mirrorCanvas);
			haveComponents = true;
			mirrorLayout.setUseMirrorSizes(false);
		}
		super.addImpl(comp, constraints, index);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return mirrorTarget.getMinimumSize();
	}

	@Override
	public Dimension getPreferredSize() {
		return mirrorTarget.getPreferredSize();
	}
	
	@Override
	public Dimension getMaximumSize() {
		return mirrorTarget.getMaximumSize();
	}

	/*
	 * TODO: the mirror-canvas might not be required, removes option setUseMirrorSizes in FormLayoutManager.
	 */
	
	/**
	 * Inner class of a {@link MirrorBox} acting as a proxy for the sizes of the target component/container.
	 */
	@SuppressWarnings("serial")
	class MirrorCanvas extends Canvas {

		@Override
		public boolean isVisible() {
			return mirrorTarget.isVisible();
		}
		
		@Override
		public Dimension getSize() {
			/*
			 * Overriding this method is not necessary but it does no harm either.
			 */
			return mirrorTarget.getSize();
		}

		@Override
		public Dimension getMinimumSize() {
			return mirrorTarget.getMinimumSize();
		}

		@Override
		public Dimension getPreferredSize() {
			return mirrorTarget.getPreferredSize();
		}
		
		@Override
		public Dimension getMaximumSize() {
			return mirrorTarget.getMaximumSize();
		}
		
	}
}
