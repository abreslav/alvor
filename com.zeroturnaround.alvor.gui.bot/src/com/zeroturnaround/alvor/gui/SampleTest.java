package com.zeroturnaround.alvor.gui;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;



@RunWith(SWTBotJunit4ClassRunner.class)
public class SampleTest {
	private SWTWorkbenchBot bot;
	  @Test
	  public void selectSomeMenus() throws Exception {
		  SWTBotMenu m1 = bot.menu("Project");
//		  SWTBotMenu m2 = m1.menu("Help Contents");
		  SWTBotMenu m2 = m1.menu("Build Automatically");
		  m2.click();
		  Thread.sleep(5000);
	  }
	  @Before
	  public void setup() {
	    bot = new SWTWorkbenchBot();
	  }
}
