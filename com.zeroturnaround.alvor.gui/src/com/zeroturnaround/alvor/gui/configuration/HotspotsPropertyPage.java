package com.zeroturnaround.alvor.gui.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.zeroturnaround.alvor.common.HotspotPattern;
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
				hasChanges = true;
			}
		});
		return memo;
	}
	
	@Override
	protected Label createDescriptionLabel(Composite parent) {
		Label desc = new Label(parent, SWT.NONE);
		desc.setText("Describe which arguments to which method calls should Alvor analyze.\n\n" +
				"On each line give class name, method name and argument index (1-based), eg:\n" +
				"java.sql.Connection, prepareStatement, 1");
		return desc;
	}
	
	private String unparseHotspots(List<HotspotPattern> hotspots) {
		StringBuilder result = new StringBuilder();
		for (HotspotPattern hp : hotspots) {
			result.append(hp.getClassName() + "," + hp.getMethodName() + "," + hp.getArgumentIndex() + "\n"); 
		}
		return result.toString();
	}
	
	private List<HotspotPattern> parseHotspots(String text) {
		Scanner sc = new Scanner(text);
		
		List<HotspotPattern> list = new ArrayList<HotspotPattern>();
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
			
			
			list.add(new HotspotPattern(parts[0].trim(), parts[1].trim(), 
					Integer.parseInt(parts[2].trim())));
		}
		
		return list;
	}

	@Override
	protected void mergeChanges(ProjectConfiguration base) {
		base.setHotspots(parseHotspots(memo.getText()));
	}
	
	@Override
	public Point computeSize() {
		return new Point(550, 400);
	}
}
