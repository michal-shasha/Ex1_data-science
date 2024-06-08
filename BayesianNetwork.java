
import java.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;


// Class to represent a Bayesian Network
public class BayesianNetwork {
    private HashMap<String, NodeBase> nodes;

    // Constructor to initialize the Bayesian Network from an XML file
    public BayesianNetwork(String path) {
        this.nodes = new HashMap<String, NodeBase>();
        parseXML(path);
    }
    // Returns all nodes in the network
    public HashMap<String, NodeBase> getNodes() {
        return nodes;
    }
    // Returns a specific node by name
    public NodeBase getNode(String nodeName) {
        return nodes.get(nodeName);
    }
    // Adds a node to the network
    public void addNode(NodeBase nodeBase) {
        this.nodes.put(nodeBase.name, nodeBase);
    }

    // Parses the XML file to construct the network
    public void parseXML(String path) {
        // Creating a new document builder factory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            // Creating a new document builder
            DocumentBuilder builder = factory.newDocumentBuilder();
            try {
                // Parsing the XML file
                Document doc = builder.parse(new File(path));
                doc.getDocumentElement().normalize();
                // Getting the list of variables from the XML document
                NodeList vars = doc.getElementsByTagName("VARIABLE");
                String varName = null;
                ArrayList<String> outcomesList = new ArrayList<>();
                // Iterating through each variable in the XML document
                for (int i = 0; i < vars.getLength(); i++) {
                    Node varNode = vars.item(i);
                    if (varNode.getNodeType() == Node.ELEMENT_NODE) {
                        // Getting the name of the variable
                        Element varElement = (Element) varNode;
                        Node nameNode = varElement.getElementsByTagName("NAME").item(0);
                        if (nameNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element nameElement = (Element) nameNode;
                            varName = nameElement.getTextContent();
                        }
                        // Getting the list of outcomes for the variable
                        NodeList outcomeNodes = varElement.getElementsByTagName("OUTCOME");
                        for (int j = 0; j < outcomeNodes.getLength(); j++) {
                            Node outcomeNode = outcomeNodes.item(j);
                            if (outcomeNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element outcomeElement = (Element) outcomeNode;
                                outcomesList.add(outcomeElement.getTextContent());
                            }
                        }
                        // Adding the variable with its outcomes to the network
                        addNode(new NodeBase(varName, outcomesList));
                        outcomesList.clear();
                    }
                }

                // Getting the list of definitions from the XML document
                NodeList definitions = doc.getElementsByTagName("DEFINITION");
                // Iterating through each definition in the XML document
                for (int i = 0; i < definitions.getLength(); i++) {
                    Node defNode = definitions.item(i);
                    if (defNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element defElement = (Element) defNode;
                        // Getting the name of the variable
                        String forVar = defElement.getElementsByTagName("FOR").item(0).getTextContent();
                        // Getting the list of given variables
                        NodeList givenNodes = defElement.getElementsByTagName("GIVEN");
                        ArrayList<String> givenVars = new ArrayList<>();
                        for (int j = 0; j < givenNodes.getLength(); j++) {
                            Node givenNode = givenNodes.item(j);
                            if (givenNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element givenElement = (Element) givenNode;
                                givenVars.add(givenElement.getTextContent());
                            }
                        }
                        // Getting the conditional probabilities table
                        String tableStr = defElement.getElementsByTagName("TABLE").item(0).getTextContent();
                        String[] tableValues = tableStr.trim().split("\\s+");
                        double[] table = new double[tableValues.length];
                        for (int k = 0; k < tableValues.length; k++) {
                            table[k] = Double.parseDouble(tableValues[k]);
                        }
                        // Adding the parents and CPT to the node
                        NodeBase node = getNode(forVar);
                        if (node != null) {
                            node.setParents(this, givenVars);
                            node.setCPT(table);
                        }
                    }
                }
                // Set children for each node
                for (NodeBase node : nodes.values()) {
                    for (NodeBase parent : node.parents) {
                        parent.addChild(node);
                    }
                }
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
}