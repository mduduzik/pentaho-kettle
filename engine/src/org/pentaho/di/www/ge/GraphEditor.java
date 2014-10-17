package org.pentaho.di.www.ge;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;

public class GraphEditor {

	private String uuid;

	private LogChannel log;

	private TransExecutionConfiguration transExecutionConfiguration;

	private TransExecutionConfiguration transPreviewExecutionConfiguration;

	private TransExecutionConfiguration transDebugExecutionConfiguration;

	private JobExecutionConfiguration jobExecutionConfiguration;

	private RowMetaAndData variables;

	public GraphEditor(String uuid) {
		super();
		this.uuid = uuid;
		
		init();
	}

	private void init() {
	    transExecutionConfiguration = new TransExecutionConfiguration();
	    transExecutionConfiguration.setGatheringMetrics( true );
	    transPreviewExecutionConfiguration = new TransExecutionConfiguration();
	    transPreviewExecutionConfiguration.setGatheringMetrics( true );
	    transDebugExecutionConfiguration = new TransExecutionConfiguration();
	    transDebugExecutionConfiguration.setGatheringMetrics( true );

	    jobExecutionConfiguration = new JobExecutionConfiguration();

	    // Clean out every time we start, auto-loading etc, is not a good idea
	    // If they are needed that often, set them in the kettle.properties file
	    //
	    variables = new RowMetaAndData( new RowMeta() );
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public LogChannel getLog() {
		return log;
	}

	public void setLog(LogChannel log) {
		this.log = log;
	}

	public TransExecutionConfiguration getTransExecutionConfiguration() {
		return transExecutionConfiguration;
	}

	public void setTransExecutionConfiguration(
			TransExecutionConfiguration transExecutionConfiguration) {
		this.transExecutionConfiguration = transExecutionConfiguration;
	}

	public TransExecutionConfiguration getTransPreviewExecutionConfiguration() {
		return transPreviewExecutionConfiguration;
	}

	public void setTransPreviewExecutionConfiguration(
			TransExecutionConfiguration transPreviewExecutionConfiguration) {
		this.transPreviewExecutionConfiguration = transPreviewExecutionConfiguration;
	}

	public TransExecutionConfiguration getTransDebugExecutionConfiguration() {
		return transDebugExecutionConfiguration;
	}

	public void setTransDebugExecutionConfiguration(
			TransExecutionConfiguration transDebugExecutionConfiguration) {
		this.transDebugExecutionConfiguration = transDebugExecutionConfiguration;
	}

	public JobExecutionConfiguration getJobExecutionConfiguration() {
		return jobExecutionConfiguration;
	}

	public void setJobExecutionConfiguration(
			JobExecutionConfiguration jobExecutionConfiguration) {
		this.jobExecutionConfiguration = jobExecutionConfiguration;
	}

	public RowMetaAndData getVariables() {
		return variables;
	}

	public void setVariables(RowMetaAndData variables) {
		this.variables = variables;
	}
	
}
