package org.pm4knime.node.io.hybridpetrinet.writer;

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
import org.pm4knime.portobject.HybridPetriNetPortObject;
import org.pm4knime.util.HybridPetriNetUtil;
import org.pm4knime.util.NodeSettingsUtils.ExistingOutputFileHandlingMode;

@SuppressWarnings("restriction")
final class HybridPetrinetWriterNodeModel extends WebUINodeModel<HybridPetrinetWriterNodeSettings> {

	private static final NodeLogger LOGGER = NodeLogger.getLogger(HybridPetrinetWriterNodeModel.class);

	HybridPetrinetWriterNodeModel(final WebUINodeConfiguration config) {
		super(config, HybridPetrinetWriterNodeSettings.class);
	}

	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs, final HybridPetrinetWriterNodeSettings settings)
			throws InvalidSettingsException {
		
		if (settings.m_outputFile == null || settings.m_outputFile.getFSLocation() == null) {
			throw new InvalidSettingsException("Please specify a path to the output file!");
		}
		
		String outputPath = settings.m_outputFile.getFSLocation().getPath();
		if (StringUtils.isEmpty(outputPath)) {
			throw new InvalidSettingsException("Output path may not be blank!");
		}

		String expectedExt = settings.getExtension();
		if (!outputPath.toLowerCase(Locale.US).endsWith(expectedExt.toLowerCase(Locale.US))) {
			setWarningMessage(
				String.format("Output file path did not have the correct file extension \"%s\", it will be appended.", expectedExt)
			);
		}

		return new PortObjectSpec[0];
	}

	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec,
			final HybridPetrinetWriterNodeSettings settings) throws Exception {
		
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

		final var tempDir = FileUtil.createTempDir("HybridPetrinetWriterNode-").toPath();
		HybridPetriNetPortObject hpnObj = (HybridPetriNetPortObject) inData[0];

		try {
			if (hpnObj.getPN() != null) {
				OutputStream outStream;
				try {
					outStream = FSFiles.newOutputStream(filePath);
					HybridPetriNetUtil.exportHybridPetrinetToFile(outStream, hpnObj.getPN());
					outStream.close();
					LOGGER.info("Successfully wrote Hybrid Petri net file to: " + filePath);
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

	private String pathWithExtension(final String path, final HybridPetrinetWriterNodeSettings settings) {
		String ext = settings.getExtension();
		
		if (!path.toLowerCase(Locale.US).endsWith(ext.toLowerCase(Locale.US))) {
			return path + ext;
		}
		return path;
	}
}