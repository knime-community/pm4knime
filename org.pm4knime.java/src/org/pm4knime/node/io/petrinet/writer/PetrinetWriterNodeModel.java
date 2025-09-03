package org.pm4knime.node.io.petrinet.writer;

import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
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
import org.knime.filehandling.core.connections.FSFiles;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.util.NodeSettingsUtils.ExistingOutputFileHandlingMode;
import org.pm4knime.util.PetriNetUtil;
import org.pm4knime.util.WriterUtil;


 // uses the restricted WebUI API
final class PetrinetWriterNodeModel extends WebUINodeModel<PetrinetWriterNodeSettings> {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(PetrinetWriterNodeModel.class);


    PetrinetWriterNodeModel(final WebUINodeConfiguration config) {
        super(config, PetrinetWriterNodeSettings.class);
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs, final PetrinetWriterNodeSettings settings)
        throws InvalidSettingsException {
        CheckUtils.checkSettingNotNull(settings.m_outputFile, "Output path must be present.");
        CheckUtils.checkSetting(StringUtils.isNotBlank(settings.m_outputFile), "Output path may not be blank: \"%s\"",
            settings.m_outputFile);

        final var outputPath = pathWithExtension(settings.m_outputFile, settings.getExtension());
        final var url = WriterUtil.toURL(outputPath, WriterUtil::handleInvalidPathSetting);
        WriterUtil.toURI(url, WriterUtil::checkLocalOrKNIMEURL,
            WriterUtil::handleInvalidURLSetting);

        return new PortObjectSpec[0];
    }

    @Override
    protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec,
        final PetrinetWriterNodeSettings settings) throws Exception {
        CheckUtils.checkArgumentNotNull(settings.m_outputFile, "Output path must be present.");
        CheckUtils.checkArgument(StringUtils.isNotBlank(settings.m_outputFile), "Output path may not be blank: \"%s\"",
            settings.m_outputFile);


        final var outputPath = pathWithExtension(settings.m_outputFile, settings.getExtension());
        CheckUtils.checkDestinationFile(outputPath,
            settings.m_existingFileHandlingMode == ExistingOutputFileHandlingMode.OVERWRITE);

        final var url = WriterUtil.toURL(outputPath,
            (ex, invalid) -> WriterUtil.handleInvalidPath(ex, invalid, () -> createMessageBuilder()));
        final var outputURI = WriterUtil.toURI(url, WriterUtil::checkLocalOrKNIMEURL,
            (e, invalidUrl) -> WriterUtil.handleInvalidURL(e, invalidUrl, () -> createMessageBuilder()));

        final var tempDir = FileUtil.createTempDir("PetrinetWriterNode-").toPath();
        PetriNetPortObject pnObj = (PetriNetPortObject) inData[0];
        
        
        try {
        	if(pnObj.getANet() != null) {
        		Path path = Paths.get(outputURI); 
            	OutputStream outStream;            
            	try {
                    outStream = FSFiles.newOutputStream(path);
                } catch (final FileAlreadyExistsException e) {
                    throw new InvalidSettingsException(
                        "Output file '" + e.getFile() + "' exists and must not be overwritten due to user settings.", e);
                }
            	PetriNetUtil.exportToStream(pnObj.getANet(), outStream);
            	outStream.close();
            }



        } finally {
            exec.setProgress(1, (String)null);
            FileUtil.deleteRecursively(tempDir.toFile());
        }

        return new PortObject[0];
    }

    

    private String pathWithExtension(final String path, String ext) {
        if (!path.toLowerCase(Locale.US).endsWith(ext)) {
        	setWarningMessage(
                String.format("Output file path did not have the correct file extension, appending \"%s\".", ext));
            return path + ext;
        }
        return path;
    }

}
