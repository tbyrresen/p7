package dk.tbyrresen.engine.osm;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;

public class OSMHandler extends DefaultHandler {
    // This is to ensure that we only consider roads (edge types) that we can actually (legally) drive on.
    // Taken from https://wiki.openstreetmap.org/wiki/Key:highway
    private static final Set<String> VALID_ROAD_TYPES = Set.of(
            "motorway",
            "trunk",
            "primary",
            "secondary",
            "tertiary",
            "unclassified",
            "residential",
            "motorway_link",
            "trunk_link",
            "primary_link",
            "secondary_link",
            "tertiary_link",
            "living_street"
    );
    private final Queue<Long> currentWayNodes;
    private boolean isCurrentWayValid;
    private boolean isCurrentElementWay;
    private final OSMGraph osmGraph;

    private OSMHandler(OSMGraph osmGraph) {
        this.osmGraph = osmGraph;
        currentWayNodes = new ArrayDeque<>();
    }

    public static void parseFromXML(OSMGraph osmGraph, String fileName) {
        try {
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxFactory.newSAXParser();
            OSMHandler osmHandler = new OSMHandler(osmGraph);
            saxParser.parse(fileName, osmHandler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    // Note that we simply ignore nodes and instead build these from the parsed ways. This is much cheaper since we
    // only care about the node ids and not lat and lon.
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (qName.equals("way")) {
            isCurrentElementWay = true;
        } else if (isCurrentElementWay && qName.equals("nd")) { // Found node during way building
            // Add node ref to current way nodes
            currentWayNodes.add(Long.parseLong(attributes.getValue("ref")));
        } else if (isCurrentElementWay && qName.equals("tag")) {
            // Check if current way is valid
            var key = attributes.getValue("k");
            var value = attributes.getValue("v");
            if (key.equals("highway")) {
                isCurrentWayValid = VALID_ROAD_TYPES.contains(value);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equals("way") && isCurrentWayValid) {
            osmGraph.addWay(new OSMWay(new ArrayDeque<>(currentWayNodes)));
            currentWayNodes.clear();
            isCurrentElementWay = false;
        }
    }
}
