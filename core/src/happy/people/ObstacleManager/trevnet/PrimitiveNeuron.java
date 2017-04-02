package happy.people.ObstacleManager.trevnet;

public class PrimitiveNeuron extends Neuron {
    public float weight;

    public PrimitiveNeuron() {
        this.weight = weightRandom();
    }

    public PrimitiveNeuron(PrimitiveNeuron toCopy) {
        this.weight = toCopy.weight;
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
