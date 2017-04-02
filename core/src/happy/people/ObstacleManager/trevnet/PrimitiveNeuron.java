package happy.people.ObstacleManager.trevnet;

public class PrimitiveNeuron extends Neuron {
    private float weight;

    public PrimitiveNeuron() {
        weight = weightRandom();
    }

    public PrimitiveNeuron(PrimitiveNeuron toCopy) {
        weight = toCopy.weight;
    }

    public PrimitiveNeuron(PrimitiveNeuron toCopy, float mutationProbability) {
        if (Math.random() < mutationProbability) {
            weight = weightRandom();
        } else {
            weight = toCopy.weight;
        }
    }

    @Override
    public float calculate(float lastOutput) {
        return normalize(weight);
    }
}
