package com.github.fwi.swing.formlayout;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fwi.swing.formlayout.builder.SimpleFormBuilder;

/**
 * Demonstrates FormLayout capabilities.
 * See {@code https://community.oracle.com/people/joconner/blog/2006/10/12/more-information-about-address-book-frame}
 * for the intention of this form, allthough the wiki with details and images have gone.
 */
public class AddressBookDemo {

	private static final Logger log = LoggerFactory.getLogger(AddressBookDemo.class);

	public static void main(String[] args) {

		try {
			new AddressBookDemo().display();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	JFrame frame;

	void display() {

		String laf = GraphicsUtil.setDefaultLookAndFeel();
		log.debug("Initial Look and Feel set to " + laf);
		GraphicsUtil.resizeTitledBorderFont(0.85f);
		build();
		show();
	}

	public static final String NAMES[] = { "Bunny, Bugs", "Cat, Sylvester", 
			"Coyote, Wile", "Devil, Tasmanian", "Duck, Daffy", "Fudd, Elmer",
			"Le Pew, Pepé", "Martian, Marvin" };

	FormGraphics formGraphics;
	JList<String> namesList;

	void build() {

		// create formGraphics after setting look and feel, 
		// or call formGraphics.init() after setting look and feel.
		formGraphics = new FormGraphics();
		// formGraphics.hgap = 25;

		frame = new JFrame("Addres book demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		AbstractBox formRootBox;
		SimpleFormBuilder form = new SimpleFormBuilder(formRootBox = new PageBox(formGraphics));
		formRootBox.setScrollToMinSize(true);
		JButton switchOrientation;
		form.addLineBox().addGap().add(new JLabel("Form UI controls:")).withDefaultSize()
		.add(switchOrientation = new JButton()).withDefaultSize();
		setOrientationActions(switchOrientation);
		JCheckBox useBigFont;
		form.add(useBigFont = new JCheckBox("Use big font")).withDefaultSize();
		setBigFontActions(useBigFont);
		// Changing L&F after a frame is displayed does not work properly in Java Swing.
		// There are all kinds of "caching" issues, this is best-effort only and will probably never work properly.
		JComboBox<String> lookAndFeel;
		form.add(lookAndFeel = new JComboBox<String>(getLFNames())).withDefaultSize();
		setLFActions(lookAndFeel);
		

		form.up().addLineBox();
		form.withTitledBorder("Main line box");

		JScrollPane scroller = new JScrollPane(namesList = new JList<String>(NAMES));
		scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		form.add(scroller).sizex(1.5).sizey(4).growy(0).growx(1.0);

		form.addPageBox(10).withTitledBorder("Address page box", Color.GREEN);
		// Keeping track of all the boxes is difficult. Use this box as "upTo" anchor.
		Container addressBox = form.getContainer();

		form.addLineBox();
		addLabeledTextField(form.addLineBox(), "Last name", "Martian");
		Container modelBox = form.getContainer();
		form.up();
		// Use of a mirrored modelBox is a bit trivial here 
		// since most boxes contain the same components.
		// But it shows the intention: all these label-textfield boxes should (re)size in the same manner.
		addLabeledTextField(form.addMirrorBox(modelBox), "First name", "Marvin");

		// This is the one non-trivial case for the mirrored modelBox.
		// All this to keep the house-number field (with number 42) aligned.
		form.upTo(addressBox).addLineBox();
		form.addMirrorBox(modelBox);
		JLabel label = getTextFieldLabel("Street & house number");
		// There are two text-fields instead of one, they need to shrink a bit more to fit within the model-box at minimum size.
		formatLabel(form.add(label));
		formatTextField(form.add(getTextField("Dust Drive"))).sizex(1.5).shrinkx(4);
		// Align textfields with numbers in opposite direction
		JTextField streetNumber = new JTextField("42");
		streetNumber.setHorizontalAlignment(SwingConstants.TRAILING);
		form.add(streetNumber).sizex(0.5).growx(2).shrinkx(2);
		/*
		 * We need a mirror-box here to keep the street-line from expanding all the way to the right.
		 * Mirror-boxes require a stand-alone box to mimic, 
		 * else the gaps between the boxes will mess up the alignment.
		 * I.e. we need this structure:
		 * 
		 *  -- line box --------------------------
		 * |  -- line box ---    -- line box ---  |
		 * | |               |  |               | |
		 * | | label - field |  | mirror        | |
		 * | |               |  |               | |
		 * |  ---------------    ---------------  |
		 *  --------------------------------------
		 * 
		 */
		form.up().addMirrorBox(modelBox); 
		// nothing is added to the mirror-box created above,
		// so an empty canvas is shown.

		// address 2 can expand all the way from left to right.
		form.upTo(addressBox);
		addLabeledTextField(form.addLineBox(), "Address 2", null);
		form.growx(0);

		form.upTo(addressBox).addLineBox();
		addLabeledTextField(form.addMirrorBox(modelBox), "City", "Red Rock");
		form.up().addMirrorBox(modelBox);

		form.upTo(addressBox).addLineBox();
		addLabeledTextField(form.addMirrorBox(modelBox), "State", "Looney Tunes").up();
		addLabeledTextField(form.addMirrorBox(modelBox), "Postal code", "ZZY BRBR");

		form.upTo(addressBox).addLineBox();
		addLabeledTextField(form.addMirrorBox(modelBox), "Country", "Warner Bros");
		form.up().addMirrorBox(modelBox);

		form.upTo(addressBox);
		form.add(new JLabel("Notes"));
		@SuppressWarnings("serial")
		JTextArea notes = new JTextArea(getMarvinNotes()) {
			/*
			 * Using "setFont" only works first time.
			 * When the UI is updated (e.g. when "useBigFont" is clicked), the font that was set is forgotten.
			 */
			@Override public Font getFont() {
				return GraphicsUtil.getTextFieldFont();
			}
		};
		notes.setLineWrap(true);
		notes.setWrapStyleWord(true);
		form.add(new JScrollPane(notes)).withSize("sizex:2, sizey:3, shrinky:0.7, shrinkx:growxy: 10");

		form.addLineBox().withTitledBorder("Button box", Color.BLUE);
		form.addLineBox().withTitledBorder("Normal box", Color.CYAN)
		.add(new JButton("Add")).withDefaultSize()
		.add(new JButton("Modify")).withDefaultSize();
		form.up().addLineBox().withTitledBorder("Centered box", Color.ORANGE)
		.addCentered(new JButton("Delete")).withDefaultSize();

		// Set form within scroll-pane so that scroll-bars appear when window-size gets to small.
		frame.setContentPane(new JScrollPane(form.build()));
		//frame.setContentPane(form.build());
		frame.pack();
		// Put initial focus on the list with Marvin the Martian selected.
		namesList.setSelectedValue(NAMES[NAMES.length-1], true);
		frame.setLocationByPlatform(true);
	}
	
	void show() {
		
		EventQueue.invokeLater(() -> {
			frame.setVisible(true);
			namesList.ensureIndexIsVisible(namesList.getSelectedIndex());
			namesList.requestFocusInWindow();
		});
	}

	void setOrientationActions(AbstractButton b) {

		setOrientationButtonText(b);
		b.addActionListener((ActionEvent e) -> {
			ComponentOrientation orientation = frame.getComponentOrientation().isLeftToRight() ?
					ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT;
			frame.applyComponentOrientation(orientation);
			setOrientationButtonText(b);
		});
	}

	void setOrientationButtonText(AbstractButton b) {
		b.setText(frame.getComponentOrientation().isLeftToRight() ? "Right to left" : "Left to right");
	}

	void setBigFontActions(AbstractButton b) {

		b.addActionListener((ActionEvent e) -> {
			float resize = (b.isSelected() ? 2.0f : 0.5f);
			GraphicsUtil.resizeApplicationFont(resize);
			formGraphics.init(); // this will update default height and width used in the form
			SwingUtilities.updateComponentTreeUI(frame);
			frame.pack();
		});
	}
	
	void resetAndUpdateFrameUI() {
		
		formGraphics.init(); // this will update default height and width used in the form
		SwingUtilities.updateComponentTreeUI(frame);
		frame.pack();
	}
	
	void setLFActions(JComboBox<String> b) {
		setCurrentLF(b);
		b.addActionListener((ActionEvent e) -> {
			String lfName = (String) b.getSelectedItem();
			LookAndFeelInfo lafi = getLFByName(lfName);
			try {
				UIManager.setLookAndFeel(lafi.getClassName());
				log.debug("Look and Feel set to " + lafi.getClassName());
				// changing look and feel requires a frame dispose to remove old UI resources
				frame.dispose();
				resetAndUpdateFrameUI();
				frame.setVisible(true);
			} catch (Exception lafe) {
				log.debug("Failed to set L&F", lafe);
			}
			setCurrentLF(b);
		});
	}
	
	void setCurrentLF(JComboBox<String> b) {
		b.setSelectedItem(UIManager.getLookAndFeel().getName());
	}

	/* *** static utility methods *** */

	public static LookAndFeelInfo getLFByName(String lfName) {
		
		for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
			if (laf.getName().equals(lfName)) {
				return laf;
			}
		}
		return null;
	}

	public static String[] getLFNames() {
		return Arrays.stream(UIManager.getInstalledLookAndFeels()).map(e -> e.getName()).toArray(size -> new String[size]);
	}

	public static SimpleFormBuilder formatLabel(SimpleFormBuilder fb) {
		// Do not shrink the labels, keep fixed width.
		// The "Address 2" and "Street & house number" fields have different (amount of) components,
		// they will not align with the other label-fields when labels shrink. 
		fb.withDefaultSize().shrinkx(1);
		return fb;
	}

	public static SimpleFormBuilder formatTextField(SimpleFormBuilder fb) {
		fb.withSize("sizex: growx: 2, shrinkx: 2");
		return fb;
	}

	public static SimpleFormBuilder addLabeledTextField(SimpleFormBuilder fb, String labelText, String defaultText) {

		LabeledTextField ltf = new LabeledTextField(labelText, defaultText);
		formatLabel(fb.add(ltf.label)).add(ltf.textField);
		return formatTextField(fb);
	}

	public static JLabel getTextFieldLabel(String labelText) {

		JLabel label = new JLabel(labelText);
		label.setHorizontalAlignment(SwingConstants.TRAILING);
		label.setVerticalAlignment(SwingConstants.CENTER);
		return label;
	}

	public static JTextField getTextField(String defaultText) {

		JTextField textField = new JTextField();
		if (defaultText != null) {
			textField.setText(defaultText);
			textField.setCaretPosition(0);
		}
		return textField;
	}

	public static class LabeledTextField {

		JTextField textField;
		JLabel label;

		public LabeledTextField(String labelText, String defaultText) {

			label = getTextFieldLabel(labelText);
			textField = getTextField(defaultText);
		}
	}

	public static String getMarvinNotes() {

		String lf = System.lineSeparator();
		StringBuilder sb = new StringBuilder("Home: \tMars");
		sb.append(lf).append("Spaceship: \tThe Martian Maggot");
		sb.append(lf).append("Pet: \tCommander K-9 the space dog.");
		sb.append(lf).append("Plan: \tTo blow up Earth with his Illudium Q-36 Explosive Space Modulator, since Earth blocks his view of Venus.");
		sb.append(lf).append("Jobs:");
		sb.append(lf).append("\t- Secret missions for the M3 Squad.");
		sb.append(lf).append("\t- Evil super villain.");
		sb.append(lf).append("\t- Pizza maker.");
		sb.append(lf).append("\t- Super store assistant manager.");
		sb.append(lf).append("\t- Basketball referee.");
		return sb.toString();
	}

}
