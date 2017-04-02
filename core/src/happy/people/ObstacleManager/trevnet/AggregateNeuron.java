package happy.people.ObstacleManager.trevnet;

import java.util.ArrayList;

public class AggregateNeuron extends Neuron {
    public ArrayList<Float> weights;
    private float lastOutputWeight;
    public ArrayList<Neuron> children;

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

    public AggregateNeuron(AggregateNeuron toCopy, float mutationProbability) {
        this.children = new ArrayList<Neuron>();
        this.weights = new ArrayList<Float>();
        for (int i = 0; i < toCopy.children.size(); i++) {
            Neuron n = toCopy.children.get(i);
            if (Math.random() < (mutationProbability / toCopy.children.size())) {
                continue; // don't copy
            }
            if (n instanceof AggregateNeuron) {
                children.add(new AggregateNeuron((AggregateNeuron) n, mutationProbability));
            } else if (n instanceof PrimitiveNeuron) {
                children.add(new PrimitiveNeuron((PrimitiveNeuron) n, mutationProbability));
            } else {
            }

            if (Math.random() < mutationProbability) {
                weights.add(weightRandom());
            } else {
                if (toCopy.weights.size() > i)
                    weights.add(toCopy.weights.get(i));
                else
                    weights.add(weightRandom());
            }
        }

        if (Math.random() < mutationProbability) {
            // add new child
            if (Math.random() < 0.5) {
                // primitive
                children.add(new PrimitiveNeuron());
            } else {
                ArrayList<Neuron> mutateChildren = new ArrayList<Neuron>();
                mutateChildren.add(new PrimitiveNeuron());
                mutateChildren.add(new PrimitiveNeuron());
                mutateChildren.add(new PrimitiveNeuron());
                children.add(new AggregateNeuron(mutateChildren));
            }

            weights.add(weightRandom());
        }

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

    public AggregateNeuron(ArrayList<Neuron> children, ArrayList<Float> weights) {
        this.children = children;
        this.weights = weights;

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
