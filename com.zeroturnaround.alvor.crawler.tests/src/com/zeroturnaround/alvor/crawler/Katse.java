package com.zeroturnaround.alvor.crawler;

import org.junit.Test;

public class Katse {
	
	@Test
	public void doit() {
		int i = 0;
		sura: 
			mura: {
			i++;
			
			if (i < 10) {
				System.out.println(i);
				break sura;
			}
		
		}
	}
}
