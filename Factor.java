
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

// Class to represent a factor for variable elimination
class Factor implements Comparable<Factor>{
    List<String> variables;
    Map<List<String>, Double> values;

    // Constructor for a factor given variables and values
    public Factor(List<String> variables, Map<List<String>, Double> values) {
        this.variables = variables;
        this.values = values;
    }
    // Constructor for a factor from a node and evidence
    public Factor(NodeBase node, Map<String, String> evidence) {
        this.variables = new ArrayList<>(node.parents.stream().map(p -> p.name).collect(Collectors.toList()));
        this.variables.add(node.name);
        this.values = new HashMap<>();

        for (Map.Entry<List<String>, Double> entry : node.cpt.entrySet()) {
            List<String> assignment = new ArrayList<>(entry.getKey());
            boolean consistent = true;
            for (NodeBase parent : node.parents) { // Check evidence against node's parents
                String parentValue = evidence.get(parent.name);
                if (parentValue != null && !assignment.get(variables.indexOf(parent.name)).equals(parentValue)) {
                    consistent = false;
                    break;
                }
            }
            // Add consistent assignments to the factor's values
            if (consistent) {
                if(evidence.get(node.name) != null){
                    if(assignment.get(variables.indexOf(node.name)).equals(evidence.get(node.name))) {
                        this.values.put(assignment, entry.getValue());
                    }
                }
                else{
                    this.values.put(assignment, entry.getValue());
                }
            }

        }
    }

    public boolean contains(String var) {
        return variables.contains(var);
    }


    public static Factor multiply(Factor f1, Factor f2, AtomicInteger mulOpers) {
        // Combine variables from both factors
        List<String> newVariables = new ArrayList<>(f1.variables);
        for (String var : f2.variables) {
            if (!newVariables.contains(var)) {
                newVariables.add(var);
            }
        }

        Map<List<String>, Double> newValues = new HashMap<>();
        for (Map.Entry<List<String>, Double> entry1 : f1.values.entrySet()) {
            for (Map.Entry<List<String>, Double> entry2 : f2.values.entrySet()) {
                List<String> newAssignment = new ArrayList<>(Collections.nCopies(newVariables.size(), ""));
                boolean match = true;
                // Merge assignments from both factors
                for (String var : f1.variables) {
                    newAssignment.set(newVariables.indexOf(var), entry1.getKey().get(f1.variables.indexOf(var)));
                }
                for (String var : f2.variables) {
                    String value = entry2.getKey().get(f2.variables.indexOf(var));
                    if (newAssignment.get(newVariables.indexOf(var)).equals("")) {
                        newAssignment.set(newVariables.indexOf(var), value);
                    } else if (!newAssignment.get(newVariables.indexOf(var)).equals(value)) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    // If assignments are consistent, multiply the values
                    double newValue = entry1.getValue() * entry2.getValue();
                    mulOpers.incrementAndGet();
                    newValues.put(newAssignment, newValue);
                }
            }
        }

        return new Factor(newVariables, newValues);
    }


    public static Factor sumOut(Factor factor, String var, AtomicInteger addOpers) {
        // Create new variables list without the specified variable
        List<String> newVariables = new ArrayList<>(factor.variables);
        newVariables.remove(var);

        Map<List<String>, Double> newValues = new HashMap<>();
        for (Map.Entry<List<String>, Double> entry : factor.values.entrySet()) {
            List<String> newAssignment = new ArrayList<>(entry.getKey());
            newAssignment.remove(factor.variables.indexOf(var));
            // Sum the values for assignments where the variable is summed out
            double newValue = entry.getValue();
            double newEntry = newValues.getOrDefault(newAssignment, 0.0);
            if(newEntry != 0.0)
            {
                newValue = newValues.getOrDefault(newAssignment, 0.0) + entry.getValue();
                addOpers.incrementAndGet();
            }
            newValues.put(newAssignment, newValue);
        }

        return new Factor(newVariables, newValues);
    }

    public double getValue(String queryVar, String queryValue) {
        int index = variables.indexOf(queryVar);
        for (Map.Entry<List<String>, Double> entry : values.entrySet()) {
            if (entry.getKey().get(index).equals(queryValue)) {
                return entry.getValue();
            }
        }
        return 0.0;
    }

    @Override
    public int compareTo(Factor other_factor) {
        // Compare the number of variables in each factor
        switch (Integer.compare(this.variables.size(), other_factor.variables.size())){
            case -1:
                return -1;  // This factor has fewer variables
            case 1:
                return 1;   // This factor has more variables
            case 0:
                break;   // This factor and the other factor have the same number of variables
        }

        // If the number of variables is the same, compare based on the sum of ASCII values of the variable names
        int var_a_ascii = 0;
        int var_b_ascii = 0;
        // Calculate the sum of ASCII values for this factor's variable names
        for(String var : this.variables){
            var_a_ascii += var.charAt(0);
        }
        // Calculate the sum of ASCII values for the other factor's variable names
        for(String var : other_factor.variables){
            var_b_ascii += var.charAt(0);
        }
        if(var_a_ascii > var_b_ascii){
            return -1;
        }
        if(var_a_ascii < var_b_ascii){
            return 1;
        }
        return 0;   // Both factors are considered equal for sorting purposes
    }
}
