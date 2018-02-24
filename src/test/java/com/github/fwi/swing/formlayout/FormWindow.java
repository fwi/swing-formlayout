package com.github.fwi.swing.formlayout;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fwi.swing.formlayout.FormConstraints;
import com.github.fwi.swing.formlayout.FormGraphics;
import com.github.fwi.swing.formlayout.GraphicsUtil;
import com.github.fwi.swing.formlayout.LineBox;
import com.github.fwi.swing.formlayout.PageBox;

// TODO: Add checkbox to switch to small / normal / big font.

public class FormWindow {

	private static final Logger log = LoggerFactory.getLogger(FormWindow.class);

	public static void main(String[] args) {
		
		try {
			new FormWindow().display();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	JFrame frame;
	
	void display() {
		
		String laf = GraphicsUtil.setDefaultLookAndFeel();
		log.debug(laf);
		SwingUtilities.invokeLater(() -> {
			FormWindow.this.buildAndShowWindow();
		});
	}

	boolean optionalTfShown = true;
	
	private void buildAndShowWindow() {
		
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		PageBox vb;
		frame.setContentPane(vb = new PageBox());
		vb.setInsets(FormGraphics.getInstance().vgap, FormGraphics.getInstance().hgap);
		LineBox hb;
		JCheckBox ltr = new JCheckBox("Switch left to right.");
		vb.add(ltr);
		ltr.addActionListener((ActionEvent l) -> {
			if (ltr.isSelected()) {
				vb.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			} else {
				vb.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			}
			vb.revalidate();
		});
		vb.add(hb = new LineBox());
		JLabel l = new JLabel("Name:");
		l.setHorizontalAlignment(SwingConstants.TRAILING);
		l.setOpaque(true);
		l.setBackground(Color.GREEN);
		hb.add(l, FormConstraints.DEFAULT);
		
		hb.add(new JTextField(), new FormConstraints("sizex:1,shrinkx:8,growx:3"));
		JTextField optionalTf;
		
		hb.add(optionalTf = new JTextField(), new FormConstraints("sizexy:2,shrinkxy:4,growxy:3"));
		hb.add(new JTextField(), new FormConstraints("shrinkx:4"));
		optionalTf.setText("Click button to remove/add");
		optionalTf.setCaretPosition(0);
		
		Canvas c;
		vb.add(c = new Canvas(), FormConstraints.DEFAULT);
		c.setBackground(Color.BLACK);
		LineBox hb2;
		vb.add(hb2 = new LineBox());
		hb2.add(new JTextField(), new FormConstraints("sizex:2,shrinkx:4,growxy:4"));
		JButton b;
		b = new JButton("With BIG text");
		b.setFont(b.getFont().deriveFont(Font.BOLD, b.getFont().getSize() * 1.5f));
		hb2.add(b);

		LineBox lb = new LineBox();
		LineBox lbt = new LineBox();
		JButton left = new JButton("Left button");
		JButton only = new JButton("Only button");

		vb.add(b = new JButton("A button here."), FormConstraints.DEFAULT);
		b.addActionListener((ActionEvent e) -> {
			if (optionalTfShown) {
				hb.remove(optionalTf);
				left.setVisible(false);
				//lb.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				only.setVisible(false);
			} else {
				hb.add(optionalTf, new FormConstraints("sizex:2,shrinkx:4,growx:3"), 1);
				left.setVisible(true);
				//lb.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
				only.setVisible(true);
			}
			optionalTfShown = !optionalTfShown;
			lb.revalidate();
			hb.revalidate();
		});
		vb.add(b = new JButton("Square"), FormConstraints.square());

		LineBox lb2;
		vb.add(lb2 = new LineBox());
		lb2.add(new Canvas(), FormConstraints.FILLERX);
		lb2.add(new JButton("Traling button"), FormConstraints.DEFAULT);
		vb.add(lb);
		lb.add(left, FormConstraints.DEFAULT);
		lb.add(new Canvas(), FormConstraints.FILLERX);
		lb.add(new JButton("Centered button"), FormConstraints.DEFAULT);
		lb.add(new Canvas(), FormConstraints.FILLERX);
		lb.add(new JButton("Right button"), FormConstraints.DEFAULT);
		
		vb.add(lbt);
		lbt.add(new JButton("stay"), FormConstraints.DEFAULT);
		lbt.add(only, FormConstraints.DEFAULT);
		
		frame.pack();
		//GraphicsUtil.positionAtMiddleOfScreen(frame);
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}

}
