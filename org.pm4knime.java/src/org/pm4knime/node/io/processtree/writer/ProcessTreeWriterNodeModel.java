package org.pm4knime.node.io.processtree.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.FileUtil;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeModel;
import org.knime.filehandling.core.connections.FSConnection;
import org.knime.filehandling.core.connections.FSFileSystem;
import org.knime.filehandling.core.connections.FSFiles;
import org.knime.filehandling.core.defaultnodesettings.FileSystemHelper;
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.util.NodeSettingsUtils.ExistingOutputFileHandlingMode;

@SuppressWarnings("restriction")
final class ProcessTreeWriterNodeModel extends WebUINodeModel<ProcessTreeWriterNodeSettings> {

	private static final NodeLogger LOGGER = NodeLogger.getLogger(ProcessTreeWriterNodeModel.class);

	ProcessTreeWriterNodeModel(final WebUINodeConfiguration config) {
		super(config, ProcessTreeWriterNodeSettings.class);
	}

	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs, final ProcessTreeWriterNodeSettings settings)
			throws InvalidSettingsException {
		
		if (settings.m_outputFile == null || settings.m_outputFile.getFSLocation() == null) {
			throw new InvalidSettingsException("Please specify a path to the output file!");
		}
		
		String outputPath = settings.m_outputFile.getFSLocation().getPath();
		if (StringUtils.isEmpty(outputPath)) {
			throw new InvalidSettingsException("Output path may not be blank!");
		}

		String expectedExt = settings.getExtension();
		if (!outputPath.endsWith(expectedExt)) {
			setWarningMessage(
				String.format("Output file path did not have the correct file extension \"%s\", it will be appended.", expectedExt)
			);
		}

		return new PortObjectSpec[0];
	}

	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec,
			final ProcessTreeWriterNodeSettings settings) throws Exception {
		
		CheckUtils.checkArgumentNotNull(settings.m_outputFile, "Output file selection must be present.");
		
		var fsLocation = settings.m_outputFile.getFSLocation();
		CheckUtils.checkArgumentNotNull(fsLocation, "File system location must be present.");
		
		String outputPath = fsLocation.getPath();
		CheckUtils.checkArgument(StringUtils.isNotBlank(outputPath), "Output path may not be blank!");

		String pathWithExt = pathWithExtension(outputPath, settings);
		
		FSConnection connection = FileSystemHelper.retrieveFSConnection(Optional.empty(), fsLocation)
				.orElseThrow(() -> new IOException("File system is not available"));
		FSFileSystem<?> fileSystem = connection.getFileSystem();
		
		final Path filePath = fileSystem.getPath(pathWithExt);
		
		if (FSFiles.exists(filePath) && 
			settings.m_existingFileHandlingMode != ExistingOutputFileHandlingMode.OVERWRITE) {
			throw new InvalidSettingsException(
				"Output file '" + filePath + "' exists and must not be overwritten due to user settings."
			);
		}

		final var tempDir = FileUtil.createTempDir("ProcessTreeWriterNode-").toPath();
		ProcessTreePortObject m_ptPort = (ProcessTreePortObject) inData[0];

		try {
			if (m_ptPort.getTree() != null) {
				OutputStream outStream;
				try {
					outStream = FSFiles.newOutputStream(filePath);
					m_ptPort.save_from_stream(outStream);
					outStream.close();
					LOGGER.info("Successfully wrote Process Tree file to: " + filePath);
				} catch (final FileAlreadyExistsException e) {
					throw new InvalidSettingsException("Output file '" + e.getFile()
							+ "' exists and must not be overwritten due to user settings.", e);
				}
			}
		} finally {
			exec.setProgress(1, (String) null);
			FileUtil.deleteRecursively(tempDir.toFile());
		}

		return new PortObject[0];
	}

	private String pathWithExtension(final String path, final ProcessTreeWriterNodeSettings settings) {
		String ext = settings.getExtension();
		
		if (!path.toLowerCase(Locale.US).endsWith(ext.toLowerCase(Locale.US))) {
			return path + ext;
		}
		return path;
	}
}