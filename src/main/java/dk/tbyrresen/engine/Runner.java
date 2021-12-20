package dk.tbyrresen.engine;

import dk.tbyrresen.engine.osm.OSMGraph;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class Runner {
    public static void main(String[] args) {
        Options options = new Options();
        Option roadNetwork = new Option("r", "roadnetwork", true, "name of road network to use");
        roadNetwork.setRequired(true);
        Option flowCutter = new Option("f", "flowcutter", true, "number of flowcutter runs (integer)");
        flowCutter.setRequired(true);
        Option nodesOrEdges = new Option("i", "inserttype", true, "test insertion using 'node' or 'edge'");
        nodesOrEdges.setRequired(true);
        Option numberOfInsertions = new Option("n", "numinsertions", true, "number of insertions to use during testing (integer)");
        numberOfInsertions.setRequired(true);
        Option maxHopDistance = new Option("m", "maxhopdistance", true, "max hop distance if using edge insertion (integer)");
        maxHopDistance.setRequired(false);
        options.addOption(roadNetwork);
        options.addOption(flowCutter);
        options.addOption(nodesOrEdges);
        options.addOption(numberOfInsertions);
        options.addOption(maxHopDistance);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            String roadNetworkInput = cmd.getOptionValue("roadnetwork");
            int flowCutterInput = Integer.parseInt(cmd.getOptionValue("flowcutter"));
            String insertTypeInput = cmd.getOptionValue("inserttype");
            int numInsertionsInput = Integer.parseInt(cmd.getOptionValue("numinsertions"));
            int maxHopDistanceInput = 2;
            if (!insertTypeInput.equals("node") && !insertTypeInput.equals("edge")) {
                System.out.print("Insertion type must be either 'node' or 'edge'");
                System.exit(1);
            }
            if (insertTypeInput.equals("edge")) {
                maxHopDistanceInput = Integer.parseInt(cmd.getOptionValue("maxhopdistance"));
            }

            var osmGraph = new OSMGraph(roadNetworkInput);
            var roadNetworkGraph = new StandardGraph<>(osmGraph);
            var cc = GraphUtils.findConnectedComponents(roadNetworkGraph);
            var largestCC = cc.stream().max(Comparator.comparingInt(c -> c.getNodes().size()));
            System.out.println("LCC nodes: " + largestCC.get().getNodes().size());
            System.out.println("LCC edges: " + largestCC.get().getEdges().size());
            var preProcessedGraph = GraphPreprocessor.getPreProcessedGraph(largestCC.get());
            System.out.println("Preprocessed nodes: " + preProcessedGraph.getNodes().size());
            System.out.println("Preprocessed edges: " + preProcessedGraph.getEdges().size());
            var startTime = System.currentTimeMillis();
            NestedDissectionTree<Node> ndTree = new NestedDissectionTree<>(preProcessedGraph, 0.6, flowCutterInput);
            var endTime = System.currentTimeMillis();
            System.out.println("Root computation time (seconds): " + TimeUnit.MILLISECONDS.toSeconds(endTime - startTime));
            System.out.println("Num ND tree nodes: " + ndTree.getNumNestedDissectionNodes());
            System.out.println("ND tree height: " + ndTree.getHeight());
            var tester = new NestedDissectionTreeTester(preProcessedGraph, ndTree);
            if (insertTypeInput.equals("node")) {
                tester.insertNewNodesAndPrintStatistics(numInsertionsInput);
            } else {
                tester.insertNewEdgesAndPrintStatistics(numInsertionsInput, maxHopDistanceInput);

            }
        } catch (ParseException e) {
            formatter.printHelp("args for running ND-tree testing", options);
        } catch (NumberFormatException e) {
            System.out.println("please provide integer values when required");
        } catch (Exception e) {
            System.out.println("error thrown: " + e.getMessage());
        }
    }
}
