package happy.people.ObstacleManager.trevnet;

public class PrimitiveNeuron extends Neuron {
    private float weight;

    public PrimitiveNeuron() {
        weight = weightRandom();
    }

    @Override
    public float calculate(float lastOutput) {
        return normalize(weight);
    }
}
