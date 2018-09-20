package com.github.fwi.swing.formlayout;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fwi.swing.formlayout.AbstractBox;
import com.github.fwi.swing.formlayout.FormConstraints;
import com.github.fwi.swing.formlayout.FormGraphics;
import com.github.fwi.swing.formlayout.GraphicsUtil;
import com.github.fwi.swing.formlayout.LineBox;
import com.github.fwi.swing.formlayout.MirrorBox;
import com.github.fwi.swing.formlayout.PageBox;

public class MirrorTest {

	private static final Logger log = LoggerFactory.getLogger(MirrorTest.class);

	public static void main(String[] args) {

		try {
			new MirrorTest().display();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	JFrame frame;

	void display() {

		String laf = GraphicsUtil.setDefaultLookAndFeel();
		log.debug(laf);
		SwingUtilities.invokeLater(() -> {
			MirrorTest.this.buildAndShowWindow();
		});
	}

	FormGraphics formGraphics;

	void buildAndShowWindow() {

		formGraphics = new FormGraphics();
		
		frame = new JFrame("Mirror test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		formGraphics.hgap = 30;
		formGraphics.vgap = 1;
		
		PageBox pb = new PageBox(formGraphics);
		pb.setInsets(5, 5, 5, 5);
		LineBox mainBox = new LineBox(formGraphics);
		pb.add(mainBox);
		LineBox target = new LineBox(formGraphics);
		// target.setBorder(new LineBorder(Color.BLACK, 1));
		mainBox.add(target);
		addLabelText(target);
		MirrorBox mirror = new MirrorBox(formGraphics, target, SwingConstants.HORIZONTAL);
		// mirror.setBorder(new LineBorder(Color.BLACK, 1));
		mainBox.add(mirror);
		
		mainBox = new LineBox(formGraphics);
		pb.add(mainBox);
		target = new LineBox(formGraphics);
		mainBox.add(target);
		// mainBox.setBorder(new LineBorder(Color.BLACK, 1));
		addLabelText(target);
		addLabelText(target);

		/*
		mainBox = new LineBox(formGraphics);
		pb.add(mainBox);
		target = new LineBox(formGraphics);
		addLabelText(target);
		mirror = new MirrorBox(formGraphics, target);
		mainBox.add(mirror);
		mainBox.add(target);

		mainBox = new LineBox(formGraphics);
		pb.add(mainBox);
		target = new LineBox(formGraphics);
		mainBox.add(target);
		mainBox.setBorder(new LineBorder(Color.BLACK, 1));
		addLabelText(target);
		addLabelText(target);

		addLabelText(pb);
		*/
		frame.setContentPane(pb);
		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}
	
	void addLabelText(AbstractBox lb) {
		
		LineBox lt = new LineBox(formGraphics);
		lt.setBorder(new LineBorder(Color.BLACK, 1));
		lb.add(lt);
		JLabel l = new JLabel("A label");
		l.setOpaque(true);
		l.setBackground(Color.WHITE);
		lt.add(l, new FormConstraints("shrinkx::2"));
		lt.add(new JTextField(), new FormConstraints("shrinkx:growx:sizex:2"));
		lt.add(new JTextField(), new FormConstraints("shrinkx:growx:2,sizex:0.25"));
	}

}
