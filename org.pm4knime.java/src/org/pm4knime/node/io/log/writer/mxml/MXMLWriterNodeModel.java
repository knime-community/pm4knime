package org.pm4knime.node.io.log.writer.mxml;

import java.io.IOException;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XSerializer;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.FileUtil;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeModel;
import org.knime.filehandling.core.connections.FSFiles;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.util.NodeSettingsUtils.ExistingOutputFileHandlingMode;
import org.pm4knime.util.WriterUtil;
import org.deckfour.xes.out.XMxmlGZIPSerializer;
import org.deckfour.xes.out.XMxmlSerializer;

 // uses the restricted WebUI API
final class MXMLWriterNodeModel extends WebUINodeModel<MXMLWriterNodeSettings> {

	protected final String[] FILE_SUFFIXES = new String[] { ".mxml", ".mxml.gz" };

	MXMLWriterNodeModel(final WebUINodeConfiguration config) {
		super(config, MXMLWriterNodeSettings.class);
	}

	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs, final MXMLWriterNodeSettings settings)
			throws InvalidSettingsException {
		CheckUtils.checkSettingNotNull(settings.m_outputFile, "Output path must be present.");
		CheckUtils.checkSetting(StringUtils.isNotBlank(settings.m_outputFile), "Output path may not be blank: \"%s\"",
				settings.m_outputFile);

		final var outputPath = pathWithExtension(settings.m_outputFile, settings.getExtension(), settings);
		final var url = WriterUtil.toURL(outputPath, WriterUtil::handleInvalidPathSetting);
		WriterUtil.toURI(url, WriterUtil::checkLocalOrKNIMEURL, WriterUtil::handleInvalidURLSetting);

		return new PortObjectSpec[0];
	}

	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec,
			final MXMLWriterNodeSettings settings) throws Exception {
		CheckUtils.checkArgumentNotNull(settings.m_outputFile, "Output path must be present.");
		CheckUtils.checkArgument(StringUtils.isNotBlank(settings.m_outputFile), "Output path may not be blank: \"%s\"",
				settings.m_outputFile);

		final var outputPath = pathWithExtension(settings.m_outputFile, settings.getExtension(), settings);
		CheckUtils.checkDestinationFile(outputPath,
				settings.m_existingFileHandlingMode == ExistingOutputFileHandlingMode.OVERWRITE);

		final var url = WriterUtil.toURL(outputPath,
				(ex, invalid) -> WriterUtil.handleInvalidPath(ex, invalid, () -> createMessageBuilder()));
		final var outputURI = WriterUtil.toURI(url, WriterUtil::checkLocalOrKNIMEURL,
				(e, invalidUrl) -> WriterUtil.handleInvalidURL(e, invalidUrl, () -> createMessageBuilder()));

		final var tempDir = FileUtil.createTempDir("MXMLWriterNode-").toPath();
		XLogPortObject logData = (XLogPortObject) inData[0];
		
			
		try {
			if (logData.getLog() != null) {
				Path path = Paths.get(outputURI);
				OutputStream outStream;
				try {
					outStream = FSFiles.newOutputStream(path);
					writeToFile(outStream, logData.getLog(), settings);
				} catch (final FileAlreadyExistsException e) {
					throw new InvalidSettingsException("Output file '" + e.getFile()
							+ "' exists and must not be overwritten due to user settings.", e);
				}
				outStream.close();
			}

		} finally {
			exec.setProgress(1, (String) null);
			FileUtil.deleteRecursively(tempDir.toFile());
		}

		return new PortObject[0];
	}

	protected void writeToFile(OutputStream outputStream, XLog log, final MXMLWriterNodeSettings settings)
			throws IOException {
		
		System.out.println("write to file running");
		
		if (settings.m_compressWithGzipChecker) {
			XSerializer logSerializer = new XMxmlGZIPSerializer();
			logSerializer.serialize(log, outputStream);
			outputStream.close();
		} else {
			XSerializer logSerializer = new XMxmlSerializer();
			logSerializer.serialize(log, outputStream);
			outputStream.close();
		}
	}

	private String pathWithExtension(final String path, String ext, final MXMLWriterNodeSettings settings) {

		if (settings.m_compressWithGzipChecker)
			ext = ".mxml.gz";
		else
			ext = ".mxml";

		if (!path.toLowerCase(Locale.US).endsWith(ext)) {
			setWarningMessage(
					String.format("Output file path did not have the correct file extension, appending \"%s\".", ext));
			return path + ext;
		}
		return path;
	}

}