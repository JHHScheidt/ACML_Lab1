package lab1;

import java.util.ArrayList;
import java.util.Random;

/**
 * The general neural network class which can adapt to different learning methods.
 *
 * @author Marciano Geijselaers
 * @author Joshua Scheidt
 */
public class NeuralNet {

    /**
     * The list of vertices.
     * The first arraylist represents the layers of the neural network.
     * The second arraylist represents a list of Vertices of some layer i.
     */
    private ArrayList<ArrayList<Vertex>> vertices;
    /**
     * The list of edges.
     * The first arraylist represents the layers between the vertex layers (meaning edges.size = vertives.size-1).
     * The second arraylist represents a list of edges for some layer i.
     */
    private ArrayList<ArrayList<Edge>> edges;
    /** A random number generator for consistent values */
    private static Random generator = new Random(3);
    /** The constant used for bias vertices */
    private final double BIASVALUE = 1;
    /** The class used for learning the weights */
    private LearningMethod learner;
    /** The training data */
    private ArrayList<ArrayList<double[]>> data;
    /** The array showing which layers need to have a bias node */
    private boolean[] bias;

    /**
     * Constructs a new neural network using the amount of nodes per layer, notion of bias nodes per layer and some weight learning class.
     *
     * @param layers An array showing the number of nodes per layer
     * @param bias A boolean array depiction whether or not a layer needs a bias node
     * @param learner A class used for learning weights
     */
    public NeuralNet(int[] layers, boolean[] bias, LearningMethod learner){
        this.vertices = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.learner = learner;
        this.bias = bias;

        for(int i = 0; i < layers.length; i++){
            vertices.add(new ArrayList<>());
            if(i != layers.length - 1){
                if(bias[i]){
                    vertices.get(i).add(new Vertex(BIASVALUE, true));
                }
            }
            for(int j = 0; j < layers[i]; j++){
                vertices.get(i).add(new Vertex());
            }
        }
        for(int i = 0; i < vertices.size() - 1; i++){
            edges.add(new ArrayList<>());
            for(int j = 0; j < vertices.get(i).size(); j++){
                for(int k = 0; k < vertices.get(i+1).size(); k++){
                    if(!vertices.get(i + 1).get(k).getBias()){
                        Edge temp = new Edge(vertices.get(i).get(j), vertices.get(i+1).get(k), generator.nextDouble()*2);
                        edges.get(i).add(temp);
                        vertices.get(i).get(j).addOutputEdge(temp);
                        vertices.get(i+1).get(k).addInputEdge(temp);
                    }
                }
            }
        }
    }

    /**
     * Sets all the weights of the edges of this neural network using a two-dimensional double array.
     *
     * @param weights 2D array of weights for the edges
     */
    public void setAllWeights(double[][] weights){
        if(weights.length != edges.size()){
            System.out.println("NUMBER OF LAYER FROM WEIGTHS INCORRECT WITH LAYER OF EDGES");
            return;
        }
        for(int i = 0; i < edges.size(); i++){
            if(weights[i].length != edges.get(i).size()){
                System.out.println("WEIGHTS TO EDGES MISMATCH AT LAYER: " + i);
                return;
            }
        }
        for(int i = 0; i < edges.size(); i++){
            for(int j = 0; j < edges.get(i).size(); j++){
                edges.get(i).get(j).setWeight(weights[i][j]);
            }
        }
    }

    /**
     * Sets the training data for the neural network.
     *
     * @param data The training data
     */
    void setData(ArrayList<ArrayList<double[]>> data) {
        this.data = data;
    }

    /**
     * Starts the learning process for the weights.
     */
    void learn(double learningRate, int iterations) {
        learner.setData(this.data, this.edges, this.vertices);
        learner.learnWeights(learningRate, iterations);
    }
    /**
     * This method calculates the accumulated error for each of the entered data points
     * This has the same structure as the training set
     * @param data The validation data
     * @return Returns a double[] with the accumulated error for each validation instance
     */
    double[] validation(ArrayList<ArrayList<double[]>> data){
        double accuError[] = new double[data.get(0).get(1).length];
        for(int i = 0; i < data.size(); i++){
            double[] result = this.predict(data.get(i).get(0));
            for(int j = 0; j < result.length; j++){
                accuError[i] += Math.abs(result[j] - data.get(i).get(1)[j]);
            }
        }
        return accuError;
    }
    /**
     * Predicts an output using the current weights of the neural network.
     *
     * @param input The input for the prediction
     * @return The output for the prediction
     */
    double[] predict(double[] input) {
        //Set first layer values
        for(int j = 0; j<input.length; j++) {
            vertices.get(0).get(((this.bias[0]?1:0))+j).setValue(input[j]);
        }
        for(int i = 1; i < vertices.size() ; i++) {
            for(int j = ((this.bias[i]?1:0)); j<vertices.get(i).size(); j++) {
                double vertexValue = 0.0;
                for(Edge e : vertices.get(i).get(j).getInputEdges()) {
                    vertexValue += e.getWeight()*e.getVertexInput().getValue();
                }
                vertices.get(i).get(j).setValue(sigmoid(vertexValue));
            }
        }
        double[] result = {0,0,0,0,0,0,0,0};
        for(int i = 0; i<vertices.get(vertices.size()-1).size(); i++) {
            result[i] = vertices.get(vertices.size()-1).get(i).getValue();
        }
        return result;
    }

    /**
     * The sigmoid function needed for calculating activation nodes.
     *
     * @param z The value of the sum of previous activation nodes multiplied with the weights
     * @return The sigmoid value
     */
    private double sigmoid(double z) {
        return 1/(1+Math.exp(-z));
    }

    /**
     * Prints the current data.
     */
    void printCurrentData(){
        for(ArrayList<double[]> currentData : this.data){
            System.out.println("This should print the input: ");
            for(int k = 0; k < currentData.get(0).length; k++){
                System.out.print(currentData.get(0)[k] + "; ");
            }
            System.out.println("");
            System.out.println("This should print the output: ");
            for(int k = 0; k < currentData.get(0).length; k++){
                System.out.print(currentData.get(0)[k] + "; ");
            }
            System.out.println("");
        }
    }
    /**
     * Prints the values on the layer with index l
     * @param l the index of the to be printed layer
     */
    void printLayerValues(int l){
        if((l >= 0) && (l < vertices.size())){
            System.out.println("The current values on the layer: " + l + " are ");
            for(Vertex v : vertices.get(l)){
                System.out.print(v.getValue() + "; ");
            }
            System.out.println("");
        }else{
            System.out.println("The value has to be within the range of 0 to " + Math.decrementExact(vertices.size()));
        }
    }
    /**
     * Prints the current output nodes.
     */
    void printOutputValue(){
        System.out.println("The current values on the output nodes: ");
        for (Vertex v : vertices.get(vertices.size()-1)) {
            System.out.print( v.getValue() +"; ");
        }
        System.out.println("");
    }
    /**
     * This method allows the printing of weights of a specific edge layer
     * @param l the edge layer to be printed
     */
    void printLayerWeights(int l){
        if((l >= 0) && (l < edges.size())){
            System.out.println("The current weights on edge layer: " + l + " are ");
            for(Edge e : edges.get(l)){
                System.out.print(e.getWeight() + "; ");
            }
            System.out.println("");
        }else{
            System.out.println("The value has to be within the range of 0 to " + Math.decrementExact(edges.size()));
        }
    }
    /**
     * Prints the weights of the edges.
     */
    void printWeights(){
        for(int i = 0; i < edges.size(); i++){
            System.out.print("The weights for edge layer: " + i + " are ");
            for(int j = 0; j < edges.get(i).size(); j++){
                System.out.print(edges.get(i).get(j).getWeight() + "; ");
            }
            System.out.println("");
        }
    }

    /**
     * Prints the neural network information.
     */
    void printNet(){
        System.out.println("Number of layers: " + vertices.size());
        System.out.println("Number of connection layers: " + edges.size());
        int numVertices = 0;
        for(ArrayList<Vertex> vertex : vertices){
            numVertices += vertex.size();
        }
        int numEdges = 0;
        for(ArrayList<Edge> edge : edges){
            numEdges += edge.size();
        }
        System.out.println("Number of vertices: " + numVertices);
        System.out.println("Number of edges: " + numEdges);
    }
}
