package com.zeroturnaround.alvor.gui;

import org.eclipse.ui.IStartup;

public class AlvorGuiStartup implements IStartup {

	@Override
	public void earlyStartup() {
		// This was created for reading logging folder location
		// When logging was simplified then it's not necessary anymore but
		// now it just waits for some useful task to execute at startup
	}

}
