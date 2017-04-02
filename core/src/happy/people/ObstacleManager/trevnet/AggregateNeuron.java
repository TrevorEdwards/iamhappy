package happy.people.ObstacleManager.trevnet;

import java.util.ArrayList;

public class AggregateNeuron extends Neuron {
    private ArrayList<Float> weights;
    private float lastOutputWeight;
    private ArrayList<Neuron> children;

    public AggregateNeuron(ArrayList<Neuron> children) {
        this.children = children;
        this.weights = new ArrayList<Float>();
        for (int i = 0; i < children.size(); i++) {
            weights.add(weightRandom());
        }

        lastOutputWeight = weightRandom();
    }

    @Override
    public float calculate(float lastOutput) {
        float sum = 0;
        for (int i = 0; i < weights.size(); i++) {
            sum += weights.get(i) * children.get(i).calculate(lastOutput);
        }
        sum += lastOutputWeight * lastOutput;
        return normalize(sum);
    }
}
