package org.pentaho.di.www.ge.trans;

import java.util.List;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.www.ge.BaseUpdate;

public class TransGridUpdate extends BaseUpdate {
	private List<RowMetaAndData> stepLines;

	public TransGridUpdate() {
	}
	
	public TransGridUpdate(List<RowMetaAndData> stepLines) {
		this.stepLines = stepLines;
	}	
}
