import java.util.*;

// Class to check independence between nodes using the Bayes Ball algorithm
public class BayesBall {
    private BayesianNetwork bn;

    public BayesBall(BayesianNetwork bn) {
        this.bn = bn;
    }

    // Method to check if two nodes are independent given some evidence
    public boolean areIndependent(String nodeA, String nodeB, Map<String, String> evidence) {
        Set<String> visited = new HashSet<>();
        return !bayesBall(nodeA, nodeB, evidence, visited, "down");
    }
    // Recursive Bayes Ball algorithm
    private boolean bayesBall(String current, String target, Map<String, String> evidence, Set<String> visited, String direction) {
        if (visited.contains(current + direction)) {
            return false;
        }
        visited.add(current + direction);

        NodeBase currentNode = bn.getNode(current);

        if (current.equals(target)) {
            return true;
        }

        if (evidence.containsKey(current)) {
            if (direction.equals("up")) {
                // Traverse to parents if current node is in the evidence and direction is up
                for (NodeBase parent : currentNode.parents) {
                    if (!parent.name.equals(current) && bayesBall(parent.name, target, evidence, visited, "down")) {
                        return true;
                    }
                }
            } else if (direction.equals("down")) {
                return false; // Stop descending if current node is in the evidence and direction is down
            }
        } else {
            if (direction.equals("up")) {
                // Traverse to children if current node is not in the evidence and direction is up
                for (NodeBase child : currentNode.children) {
                    if (bayesBall(child.name, target, evidence, visited, "up")) {
                        return true;
                    }
                }
            } else if (direction.equals("down")) {
                // Traverse to children and parents if current node is not in the evidence and direction is down
                for (NodeBase child : currentNode.children) {
                    if (bayesBall(child.name, target, evidence, visited, "up")) {
                        return true;
                    }
                }
                for (NodeBase parent : currentNode.parents) {
                    if (bayesBall(parent.name, target, evidence, visited, "down")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}