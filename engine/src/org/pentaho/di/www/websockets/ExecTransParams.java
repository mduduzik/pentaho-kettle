package org.pentaho.di.www.websockets;

public final class ExecTransParams{
    private String transName;
    private Long  objectId;

    public ExecTransParams(){
    }

	public ExecTransParams(String transName, Long objectId) {
		super();
		this.transName = transName;
		this.objectId = objectId;
	}

	public String getTransName() {
		return transName;
	}

	public void setTransName(String transName) {
		this.transName = transName;
	}

	public Long getObjectId() {
		return objectId;
	}

	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}
}