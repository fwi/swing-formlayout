package com.github.fwi.swing.formlayout;

import static com.github.fwi.swing.formlayout.FormGraphics.roundup;

import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fwi.swing.formlayout.builder.SimpleFormBuilder;

/**
 * Form constraints for components layed out using the {@link FormLayoutManager}.
 * <br>Component can have a preferred <b>size</b> in both x (horizontal) and y (vertical) direction.
 * This size may <b>grow</b> or <b>shrink</b>. A value of zero indicates "grow to maximum size"
 * or "shrink to zero size".
 * <br>Any instance of this class starts the same as the {@link #DEFAULT} with size, grow and shrink set to 1
 * which is the setting for a "fixed size" with width {@link FormGraphics#dwidth} and height {@link FormGraphics#dheight}
 * (rougly button-size).
 * As such, the {@link FormGraphics} used determines for a large part the size of the shown components.   
 * <br>Fillers (which have zero preferred size but can grow to maximum size)
 * can be used to create trailing components (by placing a filler before the component)
 * and centered components (by placing a component between two fillers).
 * <br>The {@link SimpleFormBuilder} has various convenience methods for updating the constraints.
 * All sizes can also be provided as a string, e.g. {@code "sizexy:growxy:shrinkxy:1"}. 
 * See also {@link FormConstraints#FormConstraints(String)}.
 * <p>
 * Instances of this class can be re-used, no size-data is cached during the layout of components.
 * The {@link #copy()} function can be used to derive constraints.
 */
public class FormConstraints {

	private static final Logger log = LoggerFactory.getLogger(FormConstraints.class);

	/*
	 * This class can do with a builder and the extensible generic api design.
	 */

	public static final String EMPTY_STRING = "";
	public static DecimalFormat toStringDf = new DecimalFormat("#.##"); 
	public static final FormConstraints DEFAULT = new FormConstraints("sizexy:growxy:shrinkxy:1");
	public static final FormConstraints FILLERX = new FormConstraints("growx:0,shrinkx:0");
	public static final FormConstraints FILLERY = new FormConstraints("growy:0,shrinky:0");
	public static final FormConstraints FILLERXY = new FormConstraints("growxy:0,shrinkxy:0");

	public static FormConstraints square() {
		return square(FormGraphics.getInstance());
	}

	public static FormConstraints square(FormGraphics fg) {
		return new FormConstraints("sizex:" + fg.squareSizeX);
	}
	
	public String constraints;
	public double sizex, sizey;
	public double growx, growy, shrinkx, shrinky;

	public FormConstraints() {
		this(null);
	}

	/**
	 * Accepts the form-constraints as string values. The following shows format and options:
	 * <br>{@code sizex:1,sizey:1,sizexy:1,growx:shrinkx:2,growy:1.5}
	 * <br>Variables are: <tt>size shrink grow</tt> followd by <tt>x y</tt> or <tt>xy</tt>
	 * <br>Multiple variables can be set to one value, variables and the value are separated by a <tt>:</tt> (colon).
	 * E.g. to set all values to 2, use <tt>"sizexy:growxy:shrinkxy:2"</tt>. Separate the values for a variable using 
	 * a <tt>,</tt> (comma). Extra spaces between colons and commas are allowed.
	 * <br>Note that a shrink value of <tt>2</tt> is equal to <tt>0.5</tt> (and for a grow-value the reverse).
	 * <br>A value is of type double and by default is 1 for all variables (x corresponds to {@link FormGraphics#dwidth}
	 * and y corresponds to{@link FormGraphics#dheight}).
	 * <br>A value of zero for growing indicates "grow to maximum size" see {@link FormGraphics#maxWindowWidth}
	 * and {@link FormGraphics#maxWindowHeight}) and for shrinking indicates "shrink to size zero" (i.e. make invisible). 
	 */
	public FormConstraints(String pconstraints) {
		super();
		constraints = (pconstraints == null ? EMPTY_STRING : pconstraints.trim());
		if (constraints.isEmpty()) {
			constraints = EMPTY_STRING;
		}
		sizex = sizey = growx = growy = shrinkx = shrinky = 1.0;
		parseConstraints();
	}

	/**
	 * Parses the constraints from a string. See {@link FormConstraints#FormConstraints(String)}.
	 * The {@link #EMPTY_STRING} is used as a marker to not parse anything.
	 */
	public void parseConstraints() {

		if (constraints == EMPTY_STRING) {
			return;
		}
		try {
			String[] settings = constraints.split(","); 
			for (String setting : settings) {
				String[] variables = setting.split(":");
				double value = Double.valueOf(variables[variables.length - 1].trim());
				if (log.isTraceEnabled()) {
					log.trace("Parsing setting [{}] with value {}", setting, toString(value));
				}
				for (int i = 0; i < variables.length - 1; i++) {
					String varName = variables[i].trim();
					log.trace("Parsing variable [{}]", varName);
					if (varName.startsWith("size")) {
						if (varName.contains("x")) {
							sizex = value;
						}
						if (varName.contains("y")) {
							sizey = value;
						}
					}
					if (varName.startsWith("grow")) {
						if (varName.contains("x")) {
							growx = value;
						}
						if (varName.contains("y")) {
							growy = value;
						}
					}
					if (varName.startsWith("shrink")) {
						if (varName.contains("x")) {
							shrinkx = value;
						}
						if (varName.contains("y")) {
							shrinky = value;
						}
					}
				}
			}
			if (log.isTraceEnabled()) {
				log.trace("After parsing [{}]", toString());
			}
		} catch (Exception e) {
			log.warn("Unable to parse contraints [{}] - {}", constraints, e.toString());
		}
	}

	/**
	 * Double value to string using forrmatter {@link #toStringDf} (number with 2 decimals). 
	 */
	public static String toString(double d) {
		return toStringDf.format(d);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " sizexy: " 
				+ toString(sizex) + " / " + toString(sizey) + " growxy: "
				+ toString(growx) + " / " + toString(growy) + " shrinkxy: "
				+ toString(shrinkx) + " / " + toString(shrinky);
	}

	public FormConstraints copy() {

		FormConstraints copy = new FormConstraints(EMPTY_STRING);
		copy.growx = growx;
		copy.growy = growy;
		copy.shrinkx = shrinkx;
		copy.shrinky = shrinky;
		copy.sizex = sizex;
		copy.sizey = sizey;
		return copy;
	}

	public int minWidth(FormGraphics fg) {
		int w = roundup(prefWidthExact(fg) * shrinking(shrinkx));
		return wihtinWindowWidth(fg, w);
	}
	
	public static int wihtinWindowWidth(FormGraphics fg, int w) {
		return (w < 0 ? 0 : w > fg.maxWindowWidth ? fg.maxWindowWidth : w);
	}

	public int minHeight(FormGraphics fg) {
		int h = roundup(prefHeightExact(fg) * shrinking(shrinky));
		return wihtinWindowHeight(fg, h);
	}

	public static int wihtinWindowHeight(FormGraphics fg, int h) {
		return (h < 0 ? 0 : h > fg.maxWindowHeight ? fg.maxWindowHeight : h);
	}

	protected double prefWidthExact(FormGraphics fg) {
		return fg.dwidth * sizex;
	}

	protected double prefHeightExact(FormGraphics fg) {
		return fg.dheight * sizey;
	}

	public int prefWidth(FormGraphics fg) {
		int w = roundup(prefWidthExact(fg));
		return wihtinWindowWidth(fg, w);
	}

	public int prefHeight(FormGraphics fg) {
		int h = roundup(prefHeightExact(fg));
		return wihtinWindowHeight(fg, h);
	}

	public int maxWidth(FormGraphics fg) {
		int w = (growx <= 0.0 ? fg.maxWindowWidth : roundup(prefWidthExact(fg) * growing(growx)));
		return wihtinWindowWidth(fg, w);
	}

	public int maxHeight(FormGraphics fg) {
		int h = (growy <= 0.0 ? fg.maxWindowHeight : roundup(prefHeightExact(fg) * growing(growy)));
		return wihtinWindowHeight(fg, h);
	}
	
	/**
	 * Returns given value if value is 1 or greater, else divides value by 1.
	 * @return value 1 or larger
	 */
	public static double growing(double d) {
		return (d < 1.0 ? 1.0 / d : d);
	}

	/**
	 * Returns 0 if value is 0 or less, else divides 1 by value if value is larger than 1.
	 * @return value between 0 and 1
	 */
	public static double shrinking(double d) {
		return (d <= 0.0 ? 0.0 : (d > 1.0 ? 1.0 / d : d));
	}

}
