package com.zeroturnaround.alvor.gui.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.zeroturnaround.alvor.configuration.HotspotProperties;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;

public class HotspotsPropertyPage extends CommonPropertyPage {
	private Text memo;

	@Override
	protected Control createContents(Composite parent) {
		
		memo = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		memo.setFont(JFaceResources.getTextFont());
		memo.setText(unparseHotspots(readConfiguration().getHotspots()));
		
		memo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// TODO validate lines after entering a newline
				HotspotsPropertyPage.this.setErrorMessage("erromerro");
				hasChanges = true;
			}
		});
		return memo;
	}
	
	@Override
	protected Label createDescriptionLabel(Composite parent) {
		Label desc = new Label(parent, SWT.WRAP);
		desc.setText("Here you should list methods and argument indexes (1-based) which shoud be searched for SQL");
		return desc;
	}
	
	private String unparseHotspots(List<HotspotProperties> hotspots) {
		String result = "";
		for (HotspotProperties hp : hotspots) {
			result += hp.getClassName() + "," + hp.getMethodName() + "," + hp.getArgumentIndex() + "\n"; 
		}
		return result;
	}
	
	private List<HotspotProperties> parseHotspots(String text) {
		Scanner sc = new Scanner(text);
		
		List<HotspotProperties> list = new ArrayList<HotspotProperties>();
		int i = 1;
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] parts = line.split(",");
			if (!line.trim().isEmpty()) {
				if (parts.length != 3) {
					throw new IllegalArgumentException("Invalid hotspot on line " + i 
							+ ", should have 3 parts");
				}
				// TODO validate individual parts
			}
			i++;
			
			
			list.add(new HotspotProperties(parts[0].trim(), parts[1].trim(), 
					Integer.parseInt(parts[2].trim())));
		}
		
		return list;
	}

	@Override
	protected void mergeChanges(ProjectConfiguration base) {
		base.setHotspots(parseHotspots(memo.getText()));
	}
}
