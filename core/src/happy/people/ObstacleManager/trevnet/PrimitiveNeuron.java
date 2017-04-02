package happy.people.ObstacleManager.trevnet;

public class PrimitiveNeuron extends Neuron {
    private float weight;

    public PrimitiveNeuron() {
        weight = weightRandom();
    }

    public PrimitiveNeuron(PrimitiveNeuron toCopy) {
        weight = toCopy.weight;
    }

    @Override
    public float calculate(float lastOutput) {
        return normalize(weight);
    }
}
