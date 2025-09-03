package org.pm4knime.node.io.log.reader.XesImporter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import org.processmining.framework.plugin.PluginContext;
import org.apache.commons.lang3.StringUtils;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.webui.node.dialog.defaultdialog.NodeParametersUtil;
import org.knime.filehandling.core.connections.FSConnection;
import org.knime.filehandling.core.connections.FSFileSystem;
import org.knime.filehandling.core.connections.FSFiles;
import org.knime.filehandling.core.defaultnodesettings.FileSystemHelper;
import org.pm4knime.node.io.log.reader.StreamImport;
import org.pm4knime.node.io.log.reader.XesConvertToXLogAlgorithm;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.plugins.log.OpenNaiveLogFilePlugin;
import org.xesstandard.model.XesLog;
import org.xesstandard.xml.XesXmlParserLenient;


@SuppressWarnings("restriction")
public class XesImporterNodeModel extends NodeModel {

	PortObjectSpec m_spec;
	final static String[] CFG_METHODS = { "OPEN NAIVE", "IEEE Lenient" };

	private XesImporterNodeSettings m_settings = new XesImporterNodeSettings();

	private final Class<XesImporterNodeSettings> m_settingsClass;

	XLogPortObject m_Port;

	public XesImporterNodeModel(Class<XesImporterNodeSettings> class1) {
		super(new PortType[] {}, new PortType[] { XLogPortObject.TYPE });
		m_settingsClass = class1;
	}

	protected XLogPortObject write_file_from_stream(InputStream inputStream, ExecutionContext exec) throws Exception {

		StreamImport streams = new StreamImport();

		var fsLocation = m_settings.m_file.getFSLocation();
		FSConnection connection = FileSystemHelper.retrieveFSConnection(Optional.empty(), fsLocation)
				.orElseThrow(() -> new IOException("File system is not available"));
		FSFileSystem<?> fileSystem = connection.getFileSystem();
		final Path filePath = fileSystem.getPath(fsLocation);
		File file = filePath.toFile();

		XLog result = null;
		if (m_settings.readMethod.equals(CFG_METHODS[0])) {
			// Open Naive can read multiple types of event log!!
			PluginContext context = PM4KNIMEGlobalContext.instance()
					.getFutureResultAwarePluginContext(OpenNaiveLogFilePlugin.class);
			result = (XLog) streams.importFileStream(context, inputStream, file.getName(), file.length(), file);

		} else if (m_settings.readMethod.equals(CFG_METHODS[1])) {
			// this parser imports all extensions in event log.
			XesXmlParserLenient lenientParser = new XesXmlParserLenient();
			if (file.getName().endsWith(".gz") || file.getName().endsWith(".xez")) {
				inputStream = new GZIPInputStream(inputStream);
			}
			XesLog xlog = lenientParser.parse(inputStream);
			XesConvertToXLogAlgorithm convertor = new XesConvertToXLogAlgorithm();
			result = convertor.convertToLog(xlog, exec);

		}

		XLogPortObject logPO = new XLogPortObject();
		logPO.setLog(result);

		return logPO;
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		validate();
		return configureOutSpec();
	}

	protected PortObjectSpec[] configureOutSpec() {
		return new PortObjectSpec[] { new XLogPortObjectSpec() };
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {

		exec.checkCanceled();
		try {

			var fsLocation = m_settings.m_file.getFSLocation();
			FSConnection connection = FileSystemHelper.retrieveFSConnection(Optional.empty(), fsLocation)
					.orElseThrow(() -> new IOException("File system is not available"));
			FSFileSystem<?> fileSystem = connection.getFileSystem();
			final Path filePath = fileSystem.getPath(fsLocation);
			InputStream inputStream = FSFiles.newInputStream(filePath);

			m_Port = write_file_from_stream(inputStream, exec);

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		exec.checkCanceled();
		return new PortObject[] { m_Port };

	}

	public void validate() throws InvalidSettingsException {

		System.out.println(m_settings.m_file.getFSLocation());
		if (StringUtils.isEmpty(m_settings.m_file.getFSLocation().getPath())) {
			throw new InvalidSettingsException("Please specify a path to the file to read!");
		}

		if (!StringUtils.endsWithAny(m_settings.m_file.getFSLocation().getPath(), "xes", "xes.gz")) {
			throw new InvalidSettingsException("Unsupported file type: Please select a .xes or .xes.gz file");
		}

	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		if (m_settings != null) {
			NodeParametersUtil.saveSettings(m_settingsClass, m_settings, settings);
		}
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		m_settings = NodeParametersUtil.loadSettings(settings, m_settingsClass);

	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}

}