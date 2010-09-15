package com.zeroturnaround.alvor.gui;

import org.eclipse.ui.IStartup;

import com.zeroturnaround.alvor.common.logging.Logs;

public class AlvorGuiStartup implements IStartup {

	@Override
	public void earlyStartup() {
		Logs.configureFromStream(CleanCheckProjectHandler.class.getClassLoader().getResourceAsStream("logging.properties"));
	}

}
