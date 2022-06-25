package com.github.fwi.swing.formlayout;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fwi.swing.formlayout.builder.SimpleFormBuilder;

public class FontSizeWindow implements ActionListener, ListSelectionListener {

	private static final Logger log = LoggerFactory.getLogger(FontSizeWindow.class);

	public static void main(String[] args) {

		try {
			new FontSizeWindow().display();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static final Integer[] FONT_SIZES = { 8, 10, 11, 12, 14, 16, 18,
		      20, 24, 30, 36, 40, 48, 60, 72 };

	public static final String EXAMPLE_TEXT = "The Quick Brown Fox Jumped Over The Lazy Dog";
	
	JFrame frame;
	JLabel currentFontSizeLabel;
	JList<Integer> fontSizeList;
	JTextArea exampleText;
	int currentFontSize, selectedFontSize;
	JButton buttonApply, buttonClose;
	
	void display() {

		String laf = GraphicsUtil.setDefaultLookAndFeel();
		log.debug(laf);
		GraphicsUtil.resizeTitledBorderFont(0.85f);
		build();
		show();
	}

	public void build() {
		
		frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setTitle("Font size");

		AbstractBox formRootBox;
		currentFontSize = selectedFontSize = GraphicsUtil.getLabelFont().getSize();
		// WHY does the max-size not work for the button-linebox? Fixed.
		SimpleFormBuilder form = new SimpleFormBuilder(formRootBox = new PageBox(4));
		form.withGapInsets();
		formRootBox.setScrollToMinSize(true);

		form.add(currentFontSizeLabel = new JLabel("Current size: " + currentFontSize)).withDefaultSize().sizex(2);
		
		form.addLineBox(FONT_SIZES.length);
		form.addPageBox();
		form.add(new JLabel("Select font size:")).withDefaultSize();
		fontSizeList = new JList<Integer>(FONT_SIZES);
		form.add(new JScrollPane(fontSizeList)).sizey(6).growy(0);
		
		form.up().addPageBox();
		form.add(new JLabel("Example text:")).withDefaultSize();
		exampleText = new JTextArea(EXAMPLE_TEXT);
		exampleText.setFont(GraphicsUtil.getLabelFont());
		exampleText.setWrapStyleWord(true);
		exampleText.setLineWrap(true);
		form.add(new JScrollPane(exampleText)).growxy(0);
		
		form.upToRoot().addLineBox();
		form.addFillerx().add(buttonApply = new JButton("Apply")).withDefaultSize();
		form.add(buttonClose = new JButton("Close")).withDefaultSize();
		//form.addGap();
		
		buttonApply.addActionListener(this);
		buttonClose.addActionListener(this);
		int index = 0;
		while (index < FONT_SIZES.length) {
			if (FONT_SIZES[index] == currentFontSize) {
				break;
			}
			index++;
		}
		fontSizeList.setSelectedIndex(index >= FONT_SIZES.length ? 0 : index);
		fontSizeList.ensureIndexIsVisible(fontSizeList.getSelectedIndex());
		fontSizeList.addListSelectionListener(this);
		
		// WHY do the component not grow when a scollpane is added? Fixed.
		//frame.setContentPane(form.build());
		frame.setContentPane(new JScrollPane(form.build()));
		frame.pack();
		frame.setLocationByPlatform(true);
	}

	void show() {
		
		EventQueue.invokeLater(() -> {
			frame.setVisible(true);
			fontSizeList.ensureIndexIsVisible(fontSizeList.getSelectedIndex());
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == buttonApply) {
			int fsize = FONT_SIZES[fontSizeList.getSelectedIndex()];
			float rsize = GraphicsUtil.getResizeFontFactor(fsize);
			log.info("Updating to font size " + fsize + " with factor " + rsize);
			GraphicsUtil.resizeApplicationFont(rsize);
			FormGraphics.getInstance().init();
			SwingUtilities.updateComponentTreeUI(frame);
			currentFontSizeLabel.setText("Current size: " + fsize);
			frame.pack();
		} else if (e.getSource() == buttonClose) {
			System.exit(0);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		if (e.getSource() == fontSizeList) {
			if (e.getValueIsAdjusting()) {
				return;
			}
			int index = fontSizeList.getSelectedIndex();
			int fsize = FONT_SIZES[index];
			exampleText.setFont(GraphicsUtil.getLabelFont().deriveFont((float)fsize));
			log.debug("Font size updated to " + fsize);
		}
	}

}
