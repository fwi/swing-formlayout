package com.github.fwi.swing.formlayout;

public class RoundTest {

	public static void main(String[] args) {
		
		System.out.println("1.0 roundup: " + FormGraphics.roundup(1.0));
		System.out.println("1.001 roundup: " + FormGraphics.roundup(1.001));
		System.out.println("1.4 roundup: " + FormGraphics.roundup(1.4));
		System.out.println("1.5 roundup: " + FormGraphics.roundup(1.5));
		System.out.println("1.6 roundup: " + FormGraphics.roundup(1.6));
		System.out.println("1.9 roundup: " + FormGraphics.roundup(1.9));
		System.out.println("2.0 roundup: " + FormGraphics.roundup(2.0));
	}
	
}
