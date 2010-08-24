package com.zeroturnaround.alvor.crawler;

import com.zeroturnaround.alvor.string.AbstractStringEqualsVisitor;
import com.zeroturnaround.alvor.string.IAbstractString;

public class AbstractStringEqualsVisitorExtended extends AbstractStringEqualsVisitor implements IAbstractStringVisitorExtended<Boolean, IAbstractString> {

	@Override
	public Boolean visitNamedString(NamedString oneStr,
			IAbstractString data) {
		if (data instanceof NamedString) {
			NamedString otherStr = (NamedString) data;
			return oneStr.getKey() == otherStr.getKey()
				&& abstractStringEquals(oneStr.getBody(), otherStr.getBody())
				&& positionEquals(oneStr, otherStr);
		} else {
			return false;
		}
	}

	@Override
	public Boolean visitRecursiveStringChoice(
			RecursiveStringChoice oneStr, IAbstractString data) {
		if (data instanceof RecursiveStringChoice) {
			RecursiveStringChoice otherStr = (RecursiveStringChoice)data;
			return abstractStringEquals(oneStr.getBase(), otherStr.getBase())
				&& oneStr.getRecKey() == oneStr.getRecKey()
				&& positionEquals(oneStr, otherStr);
		}
		else {
			return false;
		}
	}

}
