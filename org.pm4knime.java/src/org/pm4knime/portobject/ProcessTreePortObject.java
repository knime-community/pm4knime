package org.pm4knime.portobject;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.AbstractPortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.plugins.GraphvizProcessTree;
import org.processmining.plugins.inductiveVisualMiner.plugins.GraphvizProcessTree.NotYetImplementedException;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.ProcessTreeImpl;
import org.processmining.processtree.ptml.Ptml;
import org.processmining.processtree.ptml.importing.PtmlImportTree;

public class ProcessTreePortObject extends AbstractJSONPortObject {

    public static final PortType TYPE =
            PortTypeRegistry.getInstance().getPortType(ProcessTreePortObject.class);
    public static final PortType TYPE_OPTIONAL =
            PortTypeRegistry.getInstance().getPortType(ProcessTreePortObject.class, true);

    String FILE_NAME = "ProcessTreeObject.ptml";

    ProcessTree tree;
    ProcessTreePortObjectSpec m_spec;

    public ProcessTreePortObject(ProcessTree t) {
        this.tree = t;
    }

    public ProcessTreePortObject() {
    }

    public void setTree(ProcessTree tree) {
        this.tree = tree;
    }

    public ProcessTree getTree() {
        return tree;
    }

    @Override
    public String getSummary() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
     
        ProcessTreePortObject other = (ProcessTreePortObject) obj;
     
        ProcessTree tree1 = this.tree;
        ProcessTree tree2 = other.tree;
     
        if (tree1 == null && tree2 == null) return true;
        if (tree1 == null || tree2 == null) return false;
     
        List<String> nodes1 = getNormalizedNodes(tree1);
        List<String> nodes2 = getNormalizedNodes(tree2);
        if (!nodes1.equals(nodes2)) return false;
     
        List<String> structure1 = getNormalizedStructure(tree1);
        List<String> structure2 = getNormalizedStructure(tree2);
        return structure1.equals(structure2);
    }
     
    @Override
    public int hashCode() {
        if (tree == null) return 0;
     
        int result = 17;
        result = 31 * result + getNormalizedNodes(tree).hashCode();
        result = 31 * result + getNormalizedStructure(tree).hashCode();
        return result;
    }
     
    private List<String> getNormalizedNodes(ProcessTree tree) {
        List<String> nodes = new ArrayList<>();
        for (org.processmining.processtree.Node node : tree.getNodes()) {
            String nodeType;
            String nodeName;
            if (node.getClass().getSimpleName().toString().equals("Manual")) {
                nodeType = "manual";
                nodeName = node.getName();
            } else if (node.getClass().getSimpleName().toString().equals("Automatic")) {
                nodeType = "automatic";
                nodeName = "";
            } else if (node instanceof AbstractBlock.XorLoop) {
                nodeType = "operator";
                nodeName = "xlp";
            } else if (node instanceof AbstractBlock.And) {
                nodeType = "operator";
                nodeName = "and";
            } else if (node instanceof AbstractBlock.Seq) {
                nodeType = "operator";
                nodeName = "seq";
            } else if (node instanceof AbstractBlock.Xor) {
                nodeType = "operator";
                nodeName = "xor";
            } else {
                nodeType = "unknown";
                nodeName = "";
            }
            String normalized = nodeType + ":" + nodeName;
            nodes.add(normalized);
        }
        Collections.sort(nodes);
        return nodes;
    }
     
    private List<String> getNormalizedStructure(ProcessTree tree) {
        List<String> structure = new ArrayList<>();
        for (org.processmining.processtree.Node node : tree.getNodes()) {
            if (node instanceof AbstractBlock) {
                AbstractBlock block_node = ((AbstractBlock) node);
                String parentType = getNodeTypeAndName(node);
                if (node instanceof AbstractBlock.Seq) {
                    int order = 1;
                    for (org.processmining.processtree.Node child : block_node.getChildren()) {
                        String childType = getNodeTypeAndName(child);
                        String normalized = parentType + "->" + childType + ":" + order;
                        structure.add(normalized);
                        order = order + 1;
                    }
                } else if (node instanceof AbstractBlock.XorLoop) {
                    int xor_order = -1;
                    for (org.processmining.processtree.Node child : block_node.getChildren()) {
                        String childType = getNodeTypeAndName(child);
                        String normalized = parentType + "->" + childType + ":" + xor_order;
                        structure.add(normalized);
                        xor_order = xor_order - 1;
                    }
                } else if (node instanceof AbstractBlock.Xor || node instanceof AbstractBlock.And) {
                    for (org.processmining.processtree.Node child : block_node.getChildren()) {
                        String childType = getNodeTypeAndName(child);
                        String normalized = parentType + "->" + childType + ":0";
                        structure.add(normalized);
                    }
                }
            }
        }
        Collections.sort(structure);
        return structure;
    }
     
    private String getNodeTypeAndName(org.processmining.processtree.Node node) {
        String nodeType;
        String nodeName;
        if (node.getClass().getSimpleName().toString().equals("Manual")) {
            nodeType = "manual";
            nodeName = node.getName();
        } else if (node.getClass().getSimpleName().toString().equals("Automatic")) {
            nodeType = "automatic";
            nodeName = "";
        } else if (node instanceof AbstractBlock.XorLoop) {
            nodeType = "operator";
            nodeName = "xlp";
        } else if (node instanceof AbstractBlock.And) {
            nodeType = "operator";
            nodeName = "and";
        } else if (node instanceof AbstractBlock.Seq) {
            nodeType = "operator";
            nodeName = "seq";
        } else if (node instanceof AbstractBlock.Xor) {
            nodeType = "operator";
            nodeName = "xor";
        } else {
            nodeType = "unknown";
            nodeName = "";
        }
        return nodeType + ":" + nodeName;
    }


    @Override
    public PortObjectSpec getSpec() {
        if (m_spec != null)
            return m_spec;
        return new ProcessTreePortObjectSpec();
    }

    public void setSpec(PortObjectSpec spec) {
        m_spec = (ProcessTreePortObjectSpec) spec;
    }

    @Override
    public JComponent[] getViews() {
        return new JComponent[] {};
    }

    public DotPanel getDotPanel() {
        if (tree != null) {
            try {
                DotPanel navDot = new DotPanel(GraphvizProcessTree.convert(tree));
                navDot.setName("Generated process tree");
                return navDot;
            } catch (NotYetImplementedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String toText() {
        Ptml ptml = new Ptml().marshall(tree);
        return "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + ptml.exportElement(ptml);
    }

    public void save(String fileName) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
        bw.write(toText());
        bw.close();
    }

    public void save_from_stream(OutputStream out) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
        bw.write(toText());
        bw.close();
    }

    public void loadFromDefault(ProcessTreePortObjectSpec spec, InputStream in) throws Exception {
        PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
        PtmlImportTree importer = new PtmlImportTree();
        Ptml ptml = importer.importPtmlFromStream(context, in, spec.getFileName(), -1);
        tree = new ProcessTreeImpl(ptml.getId(), ptml.getName());
        ptml.unmarshall(tree);
        setSpec(spec);
    }

    public void loadFrom(String fileName) throws Exception {
        PluginContext context =
                PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(PtmlImportTree.class);
        PtmlImportTree importer = new PtmlImportTree();
        tree = (ProcessTree) importer.importFile(context, fileName);
    }

    public static void main(String[] args) {
        String fileName =
                "D:\\ProcessMining\\Programs\\MSProject\\dataset\\property-experiment\\model_pt_02_with_2_xor.ptml";

        PluginContext context = PM4KNIMEGlobalContext.instance()
                .getFutureResultAwarePluginContext(PtmlImportTree.class);
        PtmlImportTree importer = new PtmlImportTree();

        try {
            ProcessTree tree = (ProcessTree) importer.importFile(context, fileName);
            System.out.println(
                    "tree information " + tree.getName() + ", size of nodes: " + tree.getNodes().size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void save(PortObjectZipOutputStream out, ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        out.putNextEntry(new ZipEntry(FILE_NAME));
        out.write(toText().getBytes());
    }

    @Override
    protected void load(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        String entryName = in.getNextEntry().getName();

        if (!entryName.equals(FILE_NAME)) {
            throw new IOException("Found unexpected zip entry " + entryName + "! Expected " + FILE_NAME);
        }
        try {
            loadFromDefault((ProcessTreePortObjectSpec) spec, in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class ProcessTreePortObjectSerializer
            extends AbstractPortObject.AbstractPortObjectSerializer<ProcessTreePortObject> {
    }

    @Override
    public Map<String, List<?>> getJSON() {
        Map<String, List<?>> result = new HashMap<>();
        List<Node> nodes = new ArrayList<>();
        List<LinkWithFrequency> links = new ArrayList<>();

        for (org.processmining.processtree.Node node : tree.getNodes()) {
            if (node.getClass().getSimpleName().equals("Manual")) {
                nodes.add(new Node(node.getID().toString(), "manual", node.getName()));
            } else if (node.getClass().getSimpleName().equals("Automatic")) {
                nodes.add(new Node(node.getID().toString(), "automatic", ""));
            } else if (node instanceof AbstractBlock) {
                if (node instanceof AbstractBlock.XorLoop) {
                    nodes.add(new Node(node.getID().toString(), "operator", "xlp"));
                } else if (node instanceof AbstractBlock.And) {
                    nodes.add(new Node(node.getID().toString(), "operator", "and"));
                } else if (node instanceof AbstractBlock.Seq) {
                    nodes.add(new Node(node.getID().toString(), "operator", "seq"));
                } else if (node instanceof AbstractBlock.Xor) {
                    nodes.add(new Node(node.getID().toString(), "operator", "xor"));
                }

                AbstractBlock block_node = ((AbstractBlock) node);
                int order = 1;
                int xor_order = -1;

                if (node instanceof AbstractBlock.Seq) {
                    for (org.processmining.processtree.Node child : block_node.getChildren()) {
                        links.add(new LinkWithFrequency(block_node.getID().toString(),
                                child.getID().toString(), order));
                        order++;
                    }
                }

                if (node instanceof AbstractBlock.XorLoop) {
                    for (org.processmining.processtree.Node child : block_node.getChildren()) {
                        links.add(new LinkWithFrequency(block_node.getID().toString(),
                                child.getID().toString(), xor_order));
                        xor_order--;
                    }
                } else if (node instanceof AbstractBlock.Xor || node instanceof AbstractBlock.And) {
                    for (org.processmining.processtree.Node child : block_node.getChildren()) {
                        links.add(new LinkWithFrequency(block_node.getID().toString(),
                                child.getID().toString(), 0));
                    }
                }
            }
        }

        result.put("nodes", nodes);
        result.put("links", links);
        return result;
    }
}
