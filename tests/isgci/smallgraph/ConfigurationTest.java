package tests.isgci.smallgraph;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import teo.isgci.smallgraph.Configuration;
import teo.isgci.smallgraph.Graph;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test class for teo.isgci.smallgraph.Configuration.
 *
 *
 * Created by momits on 02.06.15.
 */
public class ConfigurationTest {

    // place to look for configuration samples
    private final String samplePath = "tests/data/configsamples/";

    // number of available configuration samples
    private final int numSamples = 54;

    private final String normalEdgeSep = "-";
    private final String optionalEdgeSep = "=";

    private Pattern noNodesPattern;
    private Pattern namePattern;
    private Pattern nodesPattern;
    private Pattern edgesPattern;

    private Configuration conf;

    /**
     * Class describing the differences between the representatives
     * of a configuration sample and the current implementation.
     */
    private class RepDiff {
        private final int sampleNo;
        public final ArrayList<Graph> sampleOnly;
        public final ArrayList<Graph> internalOnly;

        public RepDiff(int sampleNo,
                       ArrayList<Graph> sampleOnly,
                       ArrayList<Graph> internalOnly) {
            this.sampleNo = sampleNo;
            this.sampleOnly = sampleOnly;
            this.internalOnly = internalOnly;
        }

        /**
         * @return true if sampleOnly and internalOnly are both empty
         */
        public boolean isMatch() {
            return sampleOnly.isEmpty() && internalOnly.isEmpty();
        }
    }

    public ConfigurationTest() {
        // patterns for parsing the Configuration spec
        noNodesPattern = Pattern.compile("\\{[0-9]*\\}");
        namePattern = Pattern.compile("\\[.*\\]");

        // patterns for parsing the Graph spec
        nodesPattern = Pattern.compile("\\[(([0-9]+,\\ )*[0-9]+)*\\]");
        // the following edges pattern only works if the first
        // occurrence of nodesPattern is removed from the matched
        // string first
        edgesPattern = Pattern.compile("\\[.*\\]");
    }

    @Before
    public void setUp() throws Exception {
        conf = new Configuration();
    }

    @After
    public void tearDown() throws Exception {}

    /**
     * Matches against a pattern and returns the first match.
     *
     * @param pattern pattern to be matched
     * @param string candidate string
     * @return the first matched substring
     * @throws Exception if string has no substring which matches
     */
    private String matchRegex(Pattern pattern, String string) throws Exception {
        Matcher m = pattern.matcher(string);
        if (m.find()) {
            return m.group();
        } else {
            throw new Exception("Pattern could not be matched: " + pattern.pattern());
        }
    }

    private String cutFirstAndLast(String s) {
        return s.substring(1, s.length() - 1);
    }

    /**
     * Adds an an edge to the internal Configuration object
     * using a string representation. If a, b are nodes then
     * "a - b" specifies a normal edge and
     * "a = b" an optional edge.
     *
     * See teo.isgci.smallgraph.Configuration.toString().
     *
     * @param edge string representation of the edge
     */
    private void addEdgeFromString(String edge) {
        String[] nodes;
        boolean optional = false;

        if (edge.contains(normalEdgeSep)) {
            nodes = edge.replace(" ", "").split(normalEdgeSep);
        } else if (edge.contains(optionalEdgeSep)) {
            nodes = edge.replace(" ", "").split(optionalEdgeSep);
            optional = true;
        } else {
            System.err.println("Unknown edge type: " + edge);
            return;
        }

        int node1 = new Integer(nodes[0]);
        int node2 = new Integer(nodes[1]);

        if (!optional) {
            conf.addEdge(node1, node2);
        } else {
            conf.addOptedge(node1, node2);
        }
    }

    /**
     * Configure the internal Configuration object according to the
     * string representation confSpec.
     * This representation is given through Configuration.toString().
     *
     * @param confSpec the string representation
     */
    private void setupConfigurationFromString(String confSpec) {
        try {
            Integer noNodes = new Integer(cutFirstAndLast(matchRegex(noNodesPattern, confSpec)));
            String name = cutFirstAndLast(matchRegex(namePattern, confSpec));

            String edgeSpec = confSpec.replace("\n", "").split("] ")[1];
            String[] edges = edgeSpec.split("; ");

            conf.addNodesCount(noNodes);
            conf.addName(name);

            for (String edge : edges) {
                addEdgeFromString(edge);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a graph from a string representation.
     *
     * @param graphSpec string representation of the graph, given by
     *                  org.jgrapht.graph.AbstractGraph.toString().
     * @return the newly created graph
     */
    private Graph createGraphFromString(String graphSpec) {
        try {
            String[] nodeSpecs = cutFirstAndLast(matchRegex(nodesPattern, graphSpec)).split(", ");

            String edges = matchRegex(edgesPattern,
                    graphSpec.replaceFirst(nodesPattern.pattern(), ""));

            String[] edgeSpecs = cutFirstAndLast(edges).split(", ");

            Graph g = new Graph(nodeSpecs.length);

            // split produces array containing the empty string, if edges are empty
            if (!edges.equals("[]")) {
                for (String edgeSpec : edgeSpecs) {
                    addGraphEdgeFromString(g, edgeSpec);
                }
            }
            return g;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds an edge from a string representation.
     *
     * @param g Graph to add the edge to
     * @param edgeSpec string representation of the edge as in
     *                 org.jgrapht.graph.AbstractGraph.toString()
     */
    private void addGraphEdgeFromString(Graph g, String edgeSpec) {
        String[] nodes = cutFirstAndLast(edgeSpec).split(",");
        g.addEdge(new Integer(nodes[0]), new Integer(nodes[1]));
    }

    /**
     * Reads in a sample from "tests/data/configsamples" and
     * sets up the internal Configuration object according to the sample spec.
     * Then Configuration.getGraphs() is matched against the graphs in the
     * sample.
     *
     * @param sampleNo the number of the sample to parse
     * @return a RepDiff object containing the representatives which could not be
     * matched
     */
    private RepDiff testGetGraphsConfigurationSample(int sampleNo) {
        String file = String.format(samplePath + "sample-configuration-%02d", sampleNo);

        // Parse the sample configuration
        try {
            String confSample = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);

            String[] parts = confSample.split("contains");
            String confSpec = parts[0];
            String contained = parts[1].split("induces")[0];

            setupConfigurationFromString(confSpec);

            ArrayList<Graph> confGraphs = new ArrayList<>();
            ArrayList<String> graphSpecs = new ArrayList<>(Arrays.asList(contained.trim().split("\\n")));

            // create all graphs which are known to be in the configuration
            for (String graphSpec : graphSpecs) {
                confGraphs.add(createGraphFromString(graphSpec));
            }

            ArrayList<Graph> calculatedGraphs= new ArrayList<>(conf.getGraphs());

            // search isomorphic Graph in calculatedGraphs for
            // each graph in confGraphs. Then drop both.
            for (Graph sampleGraph : new ArrayList<>(confGraphs)) {
                for (Graph calculatedGraph : new ArrayList<>(calculatedGraphs)) {
                    if (sampleGraph.isIsomorphic(calculatedGraph)) {
                        confGraphs.remove(sampleGraph);
                        calculatedGraphs.remove(calculatedGraph);
                    }
                }
            }

            return new RepDiff(sampleNo, confGraphs, calculatedGraphs);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Uses the configuration sample files from data/configsamples
     * to initialize Configuration-objects and then match the result
     * of Configuration.getGraphs() against the correct set of graphs
     * using isomorphism tests.
     *
     * Counts and Prints the number of Failures
     *
     * See data/configsamples/README.md for info on the sample files.
     *
     * @throws Exception
     */
    @Test
    public void testGetGraphs() throws Exception {
        ArrayList<RepDiff> diffList = new ArrayList<>();

        for (int sampleNo = 0; sampleNo < numSamples; sampleNo++) {
            RepDiff d = testGetGraphsConfigurationSample(sampleNo);
            if (!d.isMatch()) {
                diffList.add(d);
            }
        }

        System.out.println("--- Testing getGraphs() ---");

        if (diffList.size() > 0) {
            System.out.println("Some samples could not be matched.\n");
            System.out.format("%20s%20s%15s\n", "Sample no", "In Sample only", "Internal only");

            for (RepDiff d : diffList) {
                System.out.format("%20s%20d%15d\n", d.sampleNo, d.sampleOnly.size(), d.internalOnly.size());
            }

            System.out.println();
            System.out.format("%10s%10s%20s%15s\n", "Total:",
                            diffList.size(),
                            diffList.stream().map(d -> d.internalOnly.size()).reduce(0, Integer::sum),
                            diffList.stream().map(d -> d.sampleOnly.size()).reduce(0, Integer::sum));
        } else {
            System.out.println("All samples could be matched.");
        }

        Assert.assertTrue(diffList.isEmpty());
    }

    @Test
    public void testGetGraphs1() throws Exception {

    }
}