package com.github.fwi.swing.formlayout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.FontUIResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fwi.swing.formlayout.builder.SimpleFormBuilder;

/**
 * Various Swing utility methods.
 */
public class GraphicsUtil {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(GraphicsUtil.class);

	public static final String OSName;
	static {
		OSName = System.getProperty("os.name");
	}

	private GraphicsUtil() {}

	public static boolean isWindowsOs() {
		return OSName.startsWith("Windows");
	}

	/**
	 * These properties should have appropriate defaults.
	 * Only use this method if you experience issues.
	 * See also stackoverflow <a href="http://stackoverflow.com/q/179955/3080094">How to enable AA</a>
	 * and Oracle technotes on <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/2d/flags.html#aaFonts">aaFonts</a> 
	 */
	public static void setAntiAliasingSystemProperties() {
		System.setProperty("sun.java2d.xrender", "true");
		System.setProperty("swing.aatext", "true");
		System.setProperty("awt.useSystemAAFontSettings", "lcd");
	}
	
	/**
	 * Set smart revalidate to true to incur less overhead when the method
	 * {@link Component#revalidate()} is called.
	 * <br>Revalidate calls {@link Container#isValidateRoot()} which 
	 * normally results in calling {@link Component#validate()} for the top Windows/Frame/Dialog container.
	 * With smart revalidate the "validate root" can be a scroll-pane for example
	 * (see also {@link JComponent#isValidateRoot()}.
	 */
	public static void setSmartRevalidate(boolean beSmart) {
		System.setProperty("java.awt.smartInvalidate", Boolean.toString(beSmart));
	}

	/**
	 * Sets the look and feel to OS L&F, "Nimbus" L&F or Cross-Platform L&F in that order.
	 * <b>Warning</b>: once any frame/window is displayed, changing the L&F will never work properly
	 * and has all kinds of weird "caching" issues. An application restart is required to get a different
	 * L&F working properly. In case of Nimbus L&F, even changing default font size does not work properly
	 * (due to similar caching issues) and default font-sizes have to be set before any frame/window is displayed.
	 * @return the name of the L&F set.
	 */
	public static String setDefaultLookAndFeel() {

		String sysPropLaf = System.getProperty("swing.defaultlaf");
		if (sysPropLaf != null && !sysPropLaf.trim().isEmpty()) {
			return sysPropLaf;
		}
		String osLaf = UIManager.getSystemLookAndFeelClassName();
		String defaultLaf = UIManager.getCrossPlatformLookAndFeelClassName();
		if (osLaf != null && !osLaf.equals(defaultLaf)) {
			try {
				UIManager.setLookAndFeel(osLaf);
				return osLaf;
			} catch (Exception ignored) {

			}
		}
		Set<String> lafs = new HashSet<>();
		String nimbusLaf = null;
		for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
			String lafCname = laf.getClassName();
			if (defaultLaf.equals(lafCname)) {
				continue;
			}
			lafs.add(lafCname);
			if (lafCname.toLowerCase().contains("nimbus")) {
				nimbusLaf = lafCname;
			}
		}
		if (nimbusLaf != null) {
			try {
				UIManager.setLookAndFeel(nimbusLaf);
				return nimbusLaf;
			} catch (Exception ignored) {

			}
		}
		try {
			UIManager.setLookAndFeel(defaultLaf);
			return defaultLaf;
		} catch (Exception ignored) {

		}
		return null;
	}
	
	/**
	 * Used by {@link SimpleFormBuilder#getDefaultBorderFont()}.
	 */
	public static Font getLabelFont() {
		return UIManager.getFont("Label.font");
	}

	public static Font getMonospacedFont() {
		return new Font("Monospaced", 0, getLabelFont().getSize());
	}

	public static Font getTextFieldFont() {
		return UIManager.getFont("TextField.font");
	}
	
	public static Font getTitledBorderFont() {
		return UIManager.getFont("TitledBorder.font");
	}

	public static void resizeTitledBorderFont(float resizeFactor) {
		
		Font originalFont = UIManager.getFont("TitledBorder.font");
		Font resizedFont = originalFont.deriveFont(originalFont.getSize() * resizeFactor);
		UIManager.put("TitledBorder.font", new javax.swing.plaf.FontUIResource(resizedFont));
	}

	/**
	 * Calculate the resize factor using the current default size of the label font
	 * for use n {@link #resizeApplicationFont(float)}.
	 * @param desiredSize e.g. 12
	 */
	public static float getResizeFontFactor(int desiredSize) {
		return (desiredSize / (float) getLabelFont().getSize());
	}

	/**
	 * Resize all fonts given the factor. Requires a UI update to show.
	 * <br>Warning</b>: Nimbus L&F only partially updates, default font-size has to be set before displaying any frame/window. 
	 * @param resizeFactor grows or shrinks font-sizes according to the given factor, see {@link #getResizeFontFactor(int)}.
	 */
	public static void resizeApplicationFont(float resizeFactor) {
		
		// Use enumeration and not key-set, key-set only returns a partial set.
		Enumeration<Object> enumer = UIManager.getDefaults().keys();
		// UIManager uses lazy initialization, already processed keys return in the enumeration.
		Set<Object> processedKeys = new HashSet<>();
		List<Object> updatedFonts = new LinkedList<>();
		while(enumer.hasMoreElements()) {
			Object key = enumer.nextElement();
			if (processedKeys.contains(key)) {
				 continue;
			}
			processedKeys.add(key);
			Object value = UIManager.get(key);
			Font resizedFont = null;
			if (value instanceof FontUIResource) {
				FontUIResource originalFont = (FontUIResource) value;
				resizedFont = new FontUIResource(originalFont.deriveFont(originalFont.getSize2D() * resizeFactor));
			} else if (value instanceof Font) {
				Font originalFont = (Font) value;
				resizedFont = originalFont.deriveFont(originalFont.getSize2D() * resizeFactor);
			}
			if (resizedFont != null) {
				// log.debug(key + "  \t " + value.getClass().getName() + " \t " + ((Font)value).getSize2D());
				updatedFonts.add(key);
				updatedFonts.add(resizedFont);
			}
		}
		if (updatedFonts.size() > 0) {
			UIManager.getDefaults().putDefaults(updatedFonts.toArray(new Object[updatedFonts.size()]));
		}
	}

	/**
	 * Returns the bounds of the primary/default screen and all screens together, adjusted for insets.
	 * <br>The total bounds can be used to determine if a window is completely invisible
	 * (if a window position is outside the total bounds, it is "virtual" and not visible to the user).
	 * See also {@link #fitToBounds(Window, Rectangle)}.
	 */
	public static Rectangle[] getScreenSizes() {
		
		Rectangle totalBounds = new Rectangle();
		Rectangle primaryBounds = new Rectangle();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice defaultScreen = ge.getDefaultScreenDevice(); 
		GraphicsDevice[] screens = ge.getScreenDevices();
		for (int j = 0; j < screens.length; j++) {
			GraphicsDevice screen = screens[j];
			boolean primary = (screen == defaultScreen);
			GraphicsConfiguration gc = screen.getDefaultConfiguration();
			Rectangle bounds = gc.getBounds();
			Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
			bounds.x += screenInsets.left;
		    bounds.y += screenInsets.top;
		    bounds.height -= screenInsets.bottom;
		    bounds.width -= screenInsets.right;
		    totalBounds = totalBounds.union(bounds);
			if (primary) {
				primaryBounds = primaryBounds.union(bounds);
			}
		}
		return new Rectangle[] { primaryBounds, totalBounds };
	}
	
	/**
	 * The method {@link Window#getGraphicsConfiguration()} returns the
	 * value when the window was created. This method returns the
	 * screen that the window is currently displayed on
	 * (use {@link GraphicsDevice#getDefaultConfiguration()} for the graphics configuration).
	 */
	public static GraphicsDevice getCurrentScreen(Window window) {

		window.getGraphics();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] screens = ge.getScreenDevices();
		GraphicsDevice screen = null;
		Point windowLocation = window.getLocationOnScreen();
		for (int i = 0; i < screens.length; i++) {
			Rectangle screenBounds = screens[i].getDefaultConfiguration().getBounds();
			int x = (int) windowLocation.getX();
			int y = (int) windowLocation.getY();
			if (x >= screenBounds.x && x <= screenBounds.x + screenBounds.width
					&& y >= screenBounds.y && y <= screenBounds.y + screenBounds.height) {
				screen = screens[i];
				break;
			}
		}
		return (screen == null ? ge.getDefaultScreenDevice() : screen);
	}

	/**
	 * Places the window in the middle of the default screen,
	 * or if the window was already visible,
	 * places the window in the middle of the screen that the window was first shown.
	 */
	public static void positionAtMiddleOfScreen(Window window) {

		GraphicsConfiguration gc = window.getGraphicsConfiguration();
		if (gc == null) {
			gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		}
		positionAtMiddleOfScreen(window, gc);
	}
	
	/**
	 * Places the window in the middle of the screen (taking into account the insets of the screen-graphics)
	 */
	public static void positionAtMiddleOfScreen(Window window, GraphicsConfiguration gc) {
		
		Rectangle screenBounds = gc.getBounds();
		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
		Dimension windowSize = window.getSize();
		int screenWidth = screenBounds.width - screenInsets.left - screenInsets.right;
		int screenHeight = screenBounds.height - screenInsets.top - screenInsets.bottom;
		int left = screenBounds.x + screenInsets.left;
		int x = (int) (left + (screenWidth / 2.0) - (windowSize.getWidth() / 2));
		x = Math.max(left, x);
		int top = screenBounds.y + screenInsets.top;
		int y = (int) (top + (screenHeight / 2.0) - (windowSize.getHeight() / 2));
		y = Math.max(top, y);
		window.setLocation(x, y);
	}
	
	/**
	 * If the window is not within the given bounds,
	 * the window is moved and/or resized to fit within the bounds.
	 * <br> See also {@link #getScreenSizes()} and {@link #isPartOf(Rectangle, Rectangle)}
	 */
	public static void fitToBounds(Window window, Rectangle bounds) {
		
		Point windowLocation = window.getLocationOnScreen();
		Point loc = new Point(windowLocation.x, windowLocation.y);
		boolean locUpdated = false;
		if (windowLocation.x < bounds.x) {
			loc.x = bounds.x;
			locUpdated = true;
		}
		if (windowLocation.y < bounds.y) {
			loc.y = bounds.y;
			locUpdated = true;
		}
		if (locUpdated) {
			window.setLocation(loc);
		}
		Dimension windowSize = window.getSize();
		Dimension size = new Dimension(windowSize.width, windowSize.height);
		boolean sizeUpdated = false;
		if (windowSize.width > bounds.width) {
			size.width = bounds.width;
			sizeUpdated = true;
		}
		if (windowSize.height > bounds.height) {
			size.height = bounds.height;
			sizeUpdated = true;
		}
		if (sizeUpdated) {
			window.setSize(windowSize);
		}
	}
	
	/**
	 * Tests if the first rectangle (e.g. screen size) can contain (a part of) the second rectangle (e.g. window size).
	 */
	public static boolean isPartOf(Rectangle screenBounds, Rectangle windowBounds) {
		
		if (windowBounds.x < screenBounds.x) {
			return false;
		}
		if (windowBounds.y < screenBounds.y) {
			return false;
		}
		if (windowBounds.width > screenBounds.width) {
			return false;
		}
		if (windowBounds.height > screenBounds.height) {
			return false;
		}
		return true;
	}

}
