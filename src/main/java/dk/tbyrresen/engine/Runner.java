package dk.tbyrresen.engine;

import dk.tbyrresen.engine.osm.OSMGraph;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Runner {
    public static void main(String[] args) {
//        var node1 = new Node(1);
//        var node2 = new Node(2);
//        var node3 = new Node(3);
//        var node4 = new Node(4);
//        var node5 = new Node(5);
//        var node6 = new Node(6);
//        var node7 = new Node(7);
//        var node8 = new Node(8);
//        var node9 = new Node(9);
//        var node10 = new Node(10);
//        var node11 = new Node(11);
//        var node12 = new Node(12);
//        Set<Node> nodes = new HashSet<>(Arrays.asList(node1, node2, node3, node4, node5, node6, node7, node8, node9, node10, node11, node12));
//
//        var edge12 = new StandardEdge<>(node1, node2);
//        var edge13 = new StandardEdge<>(node1, node3);
//        var edge24 = new StandardEdge<>(node2, node4);
//        var edge25 = new StandardEdge<>(node2, node5);
//        var edge35 = new StandardEdge<>(node3, node5);
//        var edge36 = new StandardEdge<>(node3, node6);
//        var edge47 = new StandardEdge<>(node4, node7);
//        var edge57 = new StandardEdge<>(node5, node7);
//        var edge58 = new StandardEdge<>(node5, node8);
//        var edge68 = new StandardEdge<>(node6, node8);
//        var edge79 = new StandardEdge<>(node7, node9);
//        var edge89 = new StandardEdge<>(node8, node9);
//        var edge910 = new StandardEdge<>(node9, node10);
//        var edge911 = new StandardEdge<>(node9, node11);
//        var edge1012 = new StandardEdge<>(node10, node12);
//        var edge1112 = new StandardEdge<>(node11, node12);
//        Set<Edge<Node>> edges = new HashSet<>(Arrays.asList(edge12, edge13, edge24, edge25, edge35, edge36, edge47, edge57, edge58, edge68, edge79, edge89, edge910, edge911, edge1012, edge1112));

//        var node1 = new Node(1);
//        var node2 = new Node(2);
//        var node3 = new Node(3);
//        var node4 = new Node(4);
//        var node5 = new Node(5);
//        var node6 = new Node(6);
//        var node7 = new Node(7);
//        var node8 = new Node(8);
//        var node9 = new Node(9);
//        var node10 = new Node(10);
//        var node11 = new Node(11);
//        var node12 = new Node(12);
//        Set<Node> nodes = new HashSet<>(Arrays.asList(node1, node2, node3, node4, node5, node6, node7, node8, node9, node10, node11, node12));
//
//        var edge1 = new StandardEdge<>(node1, node2);
//        var edge2 = new StandardEdge<>(node1, node3);
//        var edge3 = new StandardEdge<>(node1, node4);
//        var edge4 = new StandardEdge<>(node2, node3);
//        var edge5 = new StandardEdge<>(node2, node4);
//        var edge6 = new StandardEdge<>(node3, node4);
//        var edge7 = new StandardEdge<>(node3, node5);
//        var edge8 = new StandardEdge<>(node5, node6);
//        //var edge9 = new StandardEdge(node6, node7);
//        var edge10 = new StandardEdge<>(node7, node8);
//        var edge11 = new StandardEdge<>(node8, node9);
//        var edge12 = new StandardEdge<>(node9, node10);
//        var edge13 = new StandardEdge<>(node10, node7);
//        var edge14 = new StandardEdge<>(node6, node11);
//        var edge15 = new StandardEdge<>(node11, node12);
//        var edge16 = new StandardEdge<>(node12, node7);
//        Set<Edge<Node>> edges = new HashSet<>(Arrays.asList(edge1, edge2, edge3, edge4, edge5, edge6, edge7, edge8, edge10, edge11, edge12, edge13, edge14, edge15, edge16));

        var node1 = new Node(1);
        var node2 = new Node(2);
        var node3 = new Node(3);
        var node4 = new Node(4);
        var node5 = new Node(5);
        var node6 = new Node(6);
        var node7 = new Node(7);
        var node8 = new Node(8);
        var node9 = new Node(9);
        var node10 = new Node(10);
        var node11 = new Node(11);
        var node12 = new Node(12);
        var node13 = new Node(13);
        var node14 = new Node(14);
        var node15 = new Node(15);
        var node16 = new Node(16);
        var node17 = new Node(17);
        var node18 = new Node(18);
        var node19 = new Node(19);
        Set<Node> nodes = new HashSet<>(Arrays.asList(node1, node2, node3, node4, node5, node6, node7, node8, node9, node10, node11, node12, node13, node14, node15, node16, node17, node18, node19));

        var edge12 = new StandardEdge<>(node1, node2);
        var edge13 = new StandardEdge<>(node1, node3);
        var edge24 = new StandardEdge<>(node2, node4);
        var edge25 = new StandardEdge<>(node2, node5);
        var edge35 = new StandardEdge<>(node3, node5);
        var edge36 = new StandardEdge<>(node3, node6);
        var edge47 = new StandardEdge<>(node4, node7);
        var edge57 = new StandardEdge<>(node5, node7);
        var edge58 = new StandardEdge<>(node5, node8);
        var edge68 = new StandardEdge<>(node6, node8);
        var edge79 = new StandardEdge<>(node7, node9);
        var edge89 = new StandardEdge<>(node8, node9);
        var edge910 = new StandardEdge<>(node9, node10);
        var edge911 = new StandardEdge<>(node9, node11);
        var edge1012 = new StandardEdge<>(node10, node12);
        var edge1013 = new StandardEdge<>(node10, node13);
        var edge1113 = new StandardEdge<>(node11, node13);
        var edge1114 = new StandardEdge<>(node11, node14);
        var edge1215 = new StandardEdge<>(node12, node15);
        var edge1315 = new StandardEdge<>(node13, node15);
        var edge1316 = new StandardEdge<>(node13, node16);
        var edge1416 = new StandardEdge<>(node14, node16);
        var edge1517 = new StandardEdge<>(node15, node17);
        var edge1617 = new StandardEdge<>(node16, node17);
        var edge418 = new StandardEdge<>(node4, node18);
        var edge1812 = new StandardEdge<>(node18, node12);
        var edge619 = new StandardEdge<>(node6, node19);
        var edge1914 = new StandardEdge<>(node19, node14);
        Set<Edge<Node>> edges = new HashSet<>(Arrays.asList(edge12, edge13, edge24, edge25, edge35, edge36, edge47, edge57, edge58, edge68, edge79, edge89,
                                                            edge910, edge911, edge1012, edge1013, edge1113, edge1114, edge1215, edge1315, edge1316, edge1416, edge1517, edge1617,
                                                            edge418, edge1812, edge619, edge1914));


        var osmGraph = new OSMGraph("monaco-latest.osm");
        var roadNetwork = new StandardGraph<>(osmGraph);
        var cc = GraphUtils.findConnectedComponents(roadNetwork);
        var largestCC = cc.stream().max(Comparator.comparingInt(c -> c.getNodes().size()));
        System.out.println("num cc: " + cc.size());
        System.out.println(largestCC.get().getNodes().size());
        System.out.println(largestCC.get().getEdges().size());
        var ndTree = new NestedDissectionTree<>(largestCC.get(), 0.6);
        var root = ndTree.getRoot();
        var graphback = ndTree.buildGraphFromDissectionNode(root);
        System.out.println(graphback.getNodes().size());
        System.out.println(graphback.getEdges().size());

//        var roadNetwork = new StandardGraph<>(nodes, edges);
//        var ndTree = new NestedDissectionTree<>(roadNetwork, 0.6);
//        for (var dissection : ndTree.getOrderedDissections()) {
//            System.out.println(dissection.getDissectionNodes());
//        }

//        System.out.println(ndTree.getNumDirtyNodes());
//        var newNode1 = new Node(18);
//        var newNode2 = new Node(19);
//        var newNode3 = new Node(20);
//        ndTree.addEdge(new StandardEdge<>(node8, newNode1));
//        for (var dissection : ndTree.getOrderedDissections()) {
//            System.out.println(dissection.getDissectionNodes());
//        }
//        System.out.println(ndTree.getNumDirtyNodes());
//        ndTree.addEdge(new StandardEdge<>(newNode1, newNode2));
//        for (var dissection : ndTree.getOrderedDissections()) {
//            System.out.println(dissection.getDissectionNodes());
//        }
//        System.out.println(ndTree.getNumDirtyNodes());
//        ndTree.addEdge(new StandardEdge<>(newNode2, newNode3));
//        for (var dissection : ndTree.getOrderedDissections()) {
//            System.out.println(dissection.getDissectionNodes());
//        }
//        System.out.println(ndTree.getNumDirtyNodes());
//        ndTree.addEdge(new StandardEdge<>(newNode3, node6));
//        for (var dissection : ndTree.getOrderedDissections()) {
//            System.out.println(dissection.getDissectionNodes());
//        }
//        System.out.println(ndTree.getNumDirtyNodes());
//
//        var root = ndTree.getRoot();
//        var graph = ndTree.buildGraphFromDissectionNode(root);
//        System.out.println(graph.getNodes().size());
//        System.out.println(graph.getEdges().size());
    }
}
