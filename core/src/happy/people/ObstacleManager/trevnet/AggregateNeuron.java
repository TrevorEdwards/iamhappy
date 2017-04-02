package happy.people.ObstacleManager.trevnet;

import java.util.ArrayList;

public class AggregateNeuron extends Neuron {
    private ArrayList<Float> weights;
    private float lastOutputWeight;
    private ArrayList<Neuron> children;

    public AggregateNeuron(AggregateNeuron toCopy) {
        this.children = new ArrayList<Neuron>();
        for (Neuron n : toCopy.children) {
            if (n instanceof AggregateNeuron) {
                children.add(new AggregateNeuron((AggregateNeuron) n));
            } else if (n instanceof PrimitiveNeuron) {
                children.add(new PrimitiveNeuron((PrimitiveNeuron) n));
            } else {
            }
        }

        this.weights = (ArrayList<Float>) toCopy.weights.clone();
        this.lastOutputWeight = toCopy.lastOutputWeight;
    }

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
