package ee.stacc.productivity.edsl.cache;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IPosition;

public interface ICacheService {

	IAbstractString getAbstractString(IPosition position);

	void addAbstractString(IPosition iPosition, IAbstractString result);

}
