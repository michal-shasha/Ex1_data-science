
import java.util.*;
// Class to represent a node in the Bayesian Network
public class NodeBase {
    String name;
    ArrayList<String> outcomes;
    List<NodeBase> parents;
    List<NodeBase> children;
    Map<List<String>, Double> cpt;  // Conditional probability table

    public NodeBase(String name, ArrayList<String> outcomes) {
        this.name = name;
        this.outcomes =new ArrayList<String>(outcomes);
        this.parents = new ArrayList<>();
        this.children = new ArrayList<>();
        this.cpt = new HashMap<>();
    }
    // Adds a parent node
    public void addParent(NodeBase parent) {
        parents.add(parent);
    }
    // Adds a child node
    public void addChild(NodeBase child) {
        children.add(child);
    }

    // Sets the parents of the node
    public void setParents(BayesianNetwork network, ArrayList<String> givenVars) {
        this.parents = new ArrayList<>();
        for (String varName : givenVars) {
            NodeBase parentNode = network.getNode(varName);
            if (parentNode != null) {
                this.parents.add(parentNode);
            } else {
                throw new IllegalArgumentException("Parent node " + varName + " not found in the network");
            }
        }
    }
    // Sets the conditional probability table
    public void setCPT(double[] table) {
        // Generate all combinations of parent outcomes
        List<List<String>> parentCombinations = generateCombinations(parents);

        // Fill CPT with the probabilities from the table
        int index = 0;
        for (List<String> combination : parentCombinations) {
            for (String outcome : outcomes) {
                List<String> key = new ArrayList<>(combination);
                key.add(outcome);
                cpt.put(key, table[index]);
                index++;
            }
        }
    }
    // Generates all combinations of parent outcomes
    private List<List<String>> generateCombinations(List<NodeBase> parentNodes) {
        if (parentNodes.isEmpty()) {
            return Collections.singletonList(Collections.emptyList());
        }

        NodeBase firstParent = parentNodes.get(0);
        List<NodeBase> restParents = parentNodes.subList(1, parentNodes.size());

        List<List<String>> combinationsWithoutFirst = generateCombinations(restParents);
        List<List<String>> combinations = new ArrayList<>();

        for (String outcome : firstParent.outcomes) {
            for (List<String> combination : combinationsWithoutFirst) {
                List<String> newCombination = new ArrayList<>(combination);
                newCombination.add(0, outcome);
                combinations.add(newCombination);
            }
        }

        return combinations;
    }
}
