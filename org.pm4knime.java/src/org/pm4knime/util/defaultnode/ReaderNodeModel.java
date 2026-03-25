package org.pm4knime.util.defaultnode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.filehandling.core.connections.FSConnection;
import org.knime.filehandling.core.connections.FSFileSystem;
import org.knime.filehandling.core.connections.FSFiles;
import org.knime.filehandling.core.defaultnodesettings.FileSystemHelper;
import org.knime.node.DefaultModel;
import org.pm4knime.portobject.AbstractJSONPortObject;

public abstract class ReaderNodeModel {

    protected ReaderNodeSettings m_settings;
    protected final String[] extensions;
    protected AbstractJSONPortObject m_Port;

    protected ReaderNodeModel(final Class<ReaderNodeSettings> class1, final String[] types,
        final PortObjectSpec portObjectSpec, final PortType[] portTypes, final String viewName) {

        extensions = types;
    }

    public static <M extends ReaderNodeModel> void configure(final DefaultModel.ConfigureInput i,
        final DefaultModel.ConfigureOutput o, final M model) throws InvalidSettingsException {

        model.m_settings = i.getParameters();
        model.validate();
        o.setOutSpecs(model.configureOutSpec());
    }

    public static <M extends ReaderNodeModel> void execute(final DefaultModel.ExecuteInput i,
        final DefaultModel.ExecuteOutput o, final M model) {

        model.m_settings = i.getParameters();
        final ExecutionContext exec = i.getExecutionContext();

        try {
            model.m_Port = model.readPortObject();
            exec.checkCanceled();
            o.setOutData(0, model.m_Port);
            o.setInternalData(model.m_Port);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected final AbstractJSONPortObject readPortObject() throws IOException {
        final var fsLocation = m_settings.m_file.getFSLocation();
        final FSConnection connection = FileSystemHelper.retrieveFSConnection(Optional.empty(), fsLocation)
            .orElseThrow(() -> new IOException("File system is not available"));
        final FSFileSystem<?> fileSystem = connection.getFileSystem();
        final Path filePath = fileSystem.getPath(fsLocation);

        try (InputStream inputStream = FSFiles.newInputStream(filePath)) {
            return write_file_from_stream(inputStream);
        }
    }

    protected abstract AbstractJSONPortObject write_file_from_stream(InputStream inputStream);

    protected abstract PortObjectSpec[] configureOutSpec();

    public void validate() throws InvalidSettingsException {

        if (StringUtils.isEmpty(m_settings.m_file.getFSLocation().getPath())) {
            throw new InvalidSettingsException("Please specify a path to the file to read!");
        }

        if (!StringUtils.endsWith(m_settings.m_file.getFSLocation().getPath(), extensions[0])) {
            throw new InvalidSettingsException("Unsupported file type: Please select a " + extensions[0] + " file");
        }
    }
}
