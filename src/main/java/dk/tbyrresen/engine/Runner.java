package dk.tbyrresen.engine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Runner {
    public static void main(String[] args) {
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
        Set<Node> nodes = new HashSet<>(Arrays.asList(node1, node2, node3, node4, node5, node6, node7, node8, node9, node10, node11, node12));

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
        var edge1112 = new StandardEdge<>(node11, node12);
        Set<Edge<Node>> edges = new HashSet<>(Arrays.asList(edge12, edge13, edge24, edge25, edge35, edge36, edge47, edge57, edge58, edge68, edge79, edge89, edge910, edge911, edge1012, edge1112));

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


        var roadNetwork = new StandardGraph<>(nodes, edges);
        var dissections = NestedDissector.dissect(roadNetwork, 0.2);
        System.out.println("DISSECTIONS:");
        dissections.forEach(System.out::println);
    }
}
