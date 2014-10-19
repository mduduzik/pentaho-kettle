package org.pentaho.di.www.ge;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.KettleRepositoryLostException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.job.dialog.JobLoadProgressDialog;
import org.pentaho.di.ui.repository.RepositorySecurityUI;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegates;
import org.pentaho.di.ui.trans.dialog.TransLoadProgressDialog;
import org.pentaho.di.www.ge.delegates.GraphEditorDelegates;
import org.pentaho.di.www.ge.trans.TransGraph;
import org.pentaho.di.www.ge.websocket.GERequest;

public class GraphEditor {

	private String uuid;

	private LogChannel log;

	private TransExecutionConfiguration transExecutionConfiguration;

	private TransExecutionConfiguration transPreviewExecutionConfiguration;

	private TransExecutionConfiguration transDebugExecutionConfiguration;

	private JobExecutionConfiguration jobExecutionConfiguration;

	private RowMetaAndData variables;

	private Repository repository;

	private GERequest request;

	private TransGraph activeTransGraph;

	private GraphEditorDelegates delegates;

	public GraphEditor(String uuid, GERequest request) {
		super();
		this.uuid = uuid;
		this.request = request;

		init();
	}

	private void init() {
		transExecutionConfiguration = new TransExecutionConfiguration();
		transExecutionConfiguration.setGatheringMetrics(true);
		transPreviewExecutionConfiguration = new TransExecutionConfiguration();
		transPreviewExecutionConfiguration.setGatheringMetrics(true);
		transDebugExecutionConfiguration = new TransExecutionConfiguration();
		transDebugExecutionConfiguration.setGatheringMetrics(true);

		jobExecutionConfiguration = new JobExecutionConfiguration();

		// Clean out every time we start, auto-loading etc, is not a good idea
		// If they are needed that often, set them in the kettle.properties file
		//
		variables = new RowMetaAndData(new RowMeta());

		delegates = new GraphEditorDelegates(this);
	}

	public void loadObjectFromRepository(ObjectId objectId,
			RepositoryObjectType objectType, String versionLabel)
			throws Exception {
		// Try to open the selected transformation.
		if (objectType.equals(RepositoryObjectType.TRANSFORMATION)) {
			try {
				TransMeta transMeta = getRepository().loadTransformation(
						objectId, versionLabel);
				addTransGraph(transMeta);
			} catch (Exception e) {
				throw e;
			}
		} else if (objectType.equals(RepositoryObjectType.JOB)) {
			try {
				TransMeta transMeta = getRepository().loadTransformation(
						objectId, versionLabel);
				addTransGraph(transMeta);
			} catch (Exception e) {
				throw e;
			}
		}
	}

	public void addTransGraph(TransMeta transMeta) {
		delegates.getTransformationDelegate().addTransGraph(transMeta);
	}

	public void debugTransformation() {
		executeTransformation(getActiveTransGraph().getTransMeta(), true, false, false,
				false, true,
				transPreviewExecutionConfiguration.getReplayDate(), true,
				transPreviewExecutionConfiguration.getLogLevel());
	}
	
	  public void executeTransformation( final TransMeta transMeta, final boolean local, final boolean remote,
			    final boolean cluster, final boolean preview, final boolean debug, final Date replayDate,
			    final boolean safe, final LogLevel logLevel ) {

			    Thread thread = new Thread(new Runnable() {
			          public void run() {
				            try {
				              delegates.getTransformationDelegate().executeTransformation(
				                transMeta, local, remote, cluster, preview, debug, replayDate, safe, logLevel );
				            } catch ( Exception e ) {
				            	e.printStackTrace();
				            }
				          }
				        });
			    thread.start();
			  }

	public Repository getRepository() throws KettleException {
		if (repository == null)
			repository = openRepository(getRepositoryId(),
					getRepositoryUsername(), getRepositoryPassword());
		return repository;
	}

	private Repository openRepository(String repositoryName, String user,
			String pass) throws KettleException {

		if (Const.isEmpty(repositoryName)) {
			return null;
		}

		RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
		repositoriesMeta.readData();
		RepositoryMeta repositoryMeta = repositoriesMeta
				.findRepository(repositoryName);
		if (repositoryMeta == null) {
			throw new KettleException("Unable to find repository: "
					+ repositoryName);
		}
		PluginRegistry registry = PluginRegistry.getInstance();
		Repository repository = registry.loadClass(RepositoryPluginType.class,
				repositoryMeta, Repository.class);
		repository.init(repositoryMeta);
		repository.connect(user, pass);
		return repository;
	}

	public String getRepositoryId() {
		return request.getRepositoryId();
	}

	public String getRepositoryUsername() {
		return request.getRepositoryUsername();
	}

	public String getRepositoryPassword() {
		return request.getRepositoryPassword();
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

	public int getDefaultPreviewSize() {
		return 100;
	}

	public void setActiveTransGraph(TransGraph transGraph) {
		this.activeTransGraph = transGraph;
	}

	public TransGraph getActiveTransGraph() {
		return this.activeTransGraph;
	}

}
