
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class VariableElimination {

    public static String variable_elimination(BayesianNetwork bn, String queryVar, String queryValue, Map<String, String> evidence, List<String> eliminationOrder) {
        List<Factor> factors = initializeFactors(bn, evidence);  // Initialize factors based on the network and evidence
        AtomicInteger mulOpers = new AtomicInteger(0);  // Counter for multiplication operations
        AtomicInteger addOpers = new AtomicInteger(0);  // Counter for addition operations

        BayesBall ball = new BayesBall(bn);
        Iterator<String> iterator = eliminationOrder.iterator();

        // Remove variables from elimination order that are not ancestors of the query variable or are independent of the query variable given the evidence
        while (iterator.hasNext()) {
            String var = iterator.next();;
            if (!isAncestor(var, queryVar, evidence, bn) || ball.areIndependent(queryVar, var, evidence)) {
                factors.removeIf(fac -> fac.contains(var));
                iterator.remove();
            }
        }
        // Process each variable in the elimination order
        for (String var : eliminationOrder) {
            List<Factor> relevantFactors = getRelevantFactors(factors, var);
            factors.removeAll(relevantFactors);
            Factor newFactor = multiplyAndSumOut(relevantFactors, var, mulOpers, addOpers);
            factors.add(newFactor);
        }
        // Multiply all remaining factors to get the final result
        Factor resultFactor = multiplyAllFactors(factors, mulOpers);
        if(resultFactor.variables.size() > 1){
            normalizeFactor(resultFactor,addOpers); // Normalize the result factor if it contains more than one variable
        }


        double result = resultFactor.getValue(queryVar, queryValue);
        // Return the result along with the number of addition and multiplication operations
        return Math.round(result * 100000.0) / 100000.0 + "," + addOpers.get() + "," + mulOpers.get();
    }

    private static List<Factor> initializeFactors(BayesianNetwork bn, Map<String, String> evidence) {
        List<Factor> factors = new ArrayList<>();
        for (NodeBase node : bn.getNodes().values()) {
            Map<String, String> nodeEvidence = new HashMap<>();
            for (NodeBase parent : node.parents) {
                if (evidence.containsKey(parent.name)) {
                    nodeEvidence.put(parent.name, evidence.get(parent.name));
                }
            }
            if (evidence.containsKey(node.name)) {
                nodeEvidence.put(node.name, evidence.get(node.name));
            }
            Factor current_factor = new Factor(node, nodeEvidence);
            if(current_factor.values.size() > 1){
                factors.add(current_factor);  // Add the factor if it has more than one value
            }

        }
        return factors;
    }
    // Get the list of factors that contain the specified variable
    private static List<Factor> getRelevantFactors(List<Factor> factors, String var) {
        List<Factor> relevantFactors = new ArrayList<>();
        for (Factor factor : factors) {
            if (factor.contains(var)) {
                relevantFactors.add(factor);
            }
        }
        Collections.sort(relevantFactors);   // Sort the relevant factors
        return relevantFactors;
    }
    // Multiply all relevant factors and sum out the specified variable
    private static Factor multiplyAndSumOut(List<Factor> factors, String var, AtomicInteger mulOpers, AtomicInteger addOpers) {
        Factor result = factors.get(0);
        for (int i = 1; i < factors.size(); i++) {
            result = Factor.multiply(result, factors.get(i), mulOpers);
        }
        return Factor.sumOut(result, var, addOpers);
    }
    // Multiply all remaining factors
    private static Factor multiplyAllFactors(List<Factor> factors, AtomicInteger mulOpers) {
        Factor resultFactor = factors.get(0);
        for (int i = 1; i < factors.size(); i++) {
            resultFactor = Factor.multiply(resultFactor, factors.get(i), mulOpers);
        }
        return resultFactor;
    }

    private static void normalizeFactor(Factor factor,AtomicInteger addOpers) {
        double total = 0.0;
        for (double val : factor.values.values()) {
            if(total > 0.0)
            {
                addOpers.incrementAndGet();   // Count addition operations
            }
            total += val;
        }
        for (Map.Entry<List<String>, Double> entry : factor.values.entrySet()) {
            factor.values.put(entry.getKey(), entry.getValue() / total);

        }
    }
    // Check if a variable is an ancestor of the query variable or any variable in the evidence
    private static boolean isAncestor(String hidden, String query_var, Map<String, String> evidence, BayesianNetwork bn){
        if(isAncestor(hidden, query_var, bn)){
            return true;
        }
        for(String var : evidence.keySet()){
            if(isAncestor(hidden, var, bn)){
                return true;
            }
        }
        return false;
    }
    // Recursive helper function to check if one variable is an ancestor of another
    private static boolean isAncestor(String hidden, String current, BayesianNetwork bn){
        if(hidden.equals(current)){
            return true;
        }
        NodeBase current_node = bn.getNode(current);
        for(NodeBase parent : current_node.parents){
            if(isAncestor(hidden, parent.name, bn)){
                return true;
            }
        }

        return false;
    }
}
