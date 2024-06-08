
import java.io.*;
import java.util.*;
// Class to handle input processing and queries
public class Ex1 {

    public static void main(String[] args) throws IOException {
        String strFromUser = "input.txt";
        BufferedWriter output = new BufferedWriter(new FileWriter("output.txt", true));
        readInputFile(strFromUser, output);
        output.close();
    }
    // Reads input file and processes queries
    private static void readInputFile(String inputFilePath, BufferedWriter output) {
        BayesianNetwork network = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.endsWith(".xml")) {
                    network = new BayesianNetwork(line); // Initialize the BayesianNetwork with the parsed nodes
                } else {
                    if (network != null) {
                        processQuery(line, network, output); // Pass the BufferedWriter to processQuery
                    } else {
                        System.err.println("No Bayesian Network loaded. Skipping query.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Processes each query
    private static void processQuery(String line, BayesianNetwork network, BufferedWriter output) {
        try {
            if (line.startsWith("P(")) {
                // Parsing and handling probability query
                String[] elimination_query = line.split(" ");
                elimination_query[0] = elimination_query[0].substring(2, elimination_query[0].length() - 1);
                String[] hidden_vars = elimination_query[1].split("-");

                String[] query = elimination_query[0].split("\\|");
                String[] query_var_value = query[0].split("=");
                String[] evidence_query;
                Map<String, String> evidence = new HashMap<>();
                if (query.length > 1) {
                    evidence_query = query[1].split(",");
                    for (String evi_var : evidence_query) {
                        String[] evidence_var_value = evi_var.split("=");
                        evidence.put(evidence_var_value[0], evidence_var_value[1]);
                    }
                }

                // Calling variable_elimination function
                List<String> hidden_vars_list = new ArrayList<>(Arrays.asList(hidden_vars));
                String resultStr = VariableElimination.variable_elimination(network, query_var_value[0], query_var_value[1], evidence, hidden_vars_list);
                String[] resultParts = resultStr.split(",");
                double result = Double.parseDouble(resultParts[0]);
                int addOpers = Integer.parseInt(resultParts[1]);
                int mulOpers = Integer.parseInt(resultParts[2]);

                // Writing the result to the output file
                output.write(result + "," + addOpers + "," + mulOpers + "\n");

            } else {
                // Assuming 'query' is in the format "query1, query2 | evidence"
                String[] parts = line.split("\\|");
                String[] queryNodes = parts[0].trim().split("-");
                String evidenceString = null;

                if (parts.length > 1) {
                    evidenceString = parts[1].trim();
                }

                if (queryNodes.length != 2) {
                    System.err.println("Query format incorrect. Expected format: nodeA-nodeB | evidence");
                    return;
                }

                String nodeA = queryNodes[0].trim();
                String nodeB = queryNodes[1].trim();

                // Parse evidence string into a map
                Map<String, String> evidence = parseEvidence(evidenceString);

                // Check independence
                BayesBall bayesBall = new BayesBall(network);
                if (bayesBall.areIndependent(nodeA, nodeB, evidence)) {
                    output.write("yes\n");
                } else {
                    output.write("no\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Parses the evidence string into a map
    private static Map<String, String> parseEvidence(String evidenceString) {
        Map<String, String> evidence = new HashMap<>();
        if (evidenceString != null) {
            String[] parts = evidenceString.split(",");
            for (String part : parts) {
                String[] keyValue = part.split("=");
                evidence.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }

        return evidence;
    }
}