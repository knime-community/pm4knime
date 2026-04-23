package org.pm4knime.node.io.petrinet.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.FileUtil;
import org.knime.filehandling.core.connections.FSConnection;
import org.knime.filehandling.core.connections.FSFileSystem;
import org.knime.filehandling.core.connections.FSFiles;
import org.knime.filehandling.core.defaultnodesettings.FileSystemHelper;
import org.knime.node.DefaultModel;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.util.NodeSettingsUtils.ExistingOutputFileHandlingMode;
import org.pm4knime.util.PetriNetUtil;

@SuppressWarnings("restriction")
final class PetrinetWriterNodeModel {

	private static final NodeLogger LOGGER = NodeLogger.getLogger(PetrinetWriterNodeModel.class);

	private PetrinetWriterNodeModel() {
	}

	static void configure(final DefaultModel.ConfigureInput i, final DefaultModel.ConfigureOutput o)
			throws InvalidSettingsException {
		final PetrinetWriterNodeSettings settings = i.getParameters();
		
		if (settings.m_outputFile == null || settings.m_outputFile.getFSLocation() == null) {
			throw new InvalidSettingsException("Please specify a path to the output file!");
		}
		
		String outputPath = settings.m_outputFile.getFSLocation().getPath();
		if (StringUtils.isEmpty(outputPath)) {
			throw new InvalidSettingsException("Output path may not be blank!");
		}

		String expectedExt = settings.getExtension();
		if (!outputPath.endsWith(expectedExt)) {
			o.setWarningMessage(
				String.format("Output file path did not have the correct file extension \"%s\", it will be appended.", expectedExt)
			);
		}

		o.setOutSpecs(new PortObjectSpec[0]);
	}

	static void execute(final DefaultModel.ExecuteInput i, final DefaultModel.ExecuteOutput o) {
		try {
			execute(i.getInPortObjects(), i.getExecutionContext(), i.getParameters());
			o.setOutData(new PortObject[0]);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private static PortObject[] execute(final PortObject[] inData, final org.knime.core.node.ExecutionContext exec,
			final PetrinetWriterNodeSettings settings) throws Exception {
		
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

		final var tempDir = FileUtil.createTempDir("PetrinetWriterNode-").toPath();
		PetriNetPortObject pnObj = (PetriNetPortObject) inData[0];

		try {
			if (pnObj.getANet() != null) {
				OutputStream outStream;
				try {
					outStream = FSFiles.newOutputStream(filePath);
					PetriNetUtil.exportToStream(pnObj.getANet(), outStream);
					outStream.close();
					LOGGER.info("Successfully wrote Petri net file to: " + filePath);
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

	private static String pathWithExtension(final String path, final PetrinetWriterNodeSettings settings) {
		String ext = settings.getExtension();
		
		if (!path.toLowerCase(Locale.US).endsWith(ext.toLowerCase(Locale.US))) {
			return path + ext;
		}
		return path;
	}
}
