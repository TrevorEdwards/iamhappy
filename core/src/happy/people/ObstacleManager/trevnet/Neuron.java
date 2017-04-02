package happy.people.ObstacleManager.trevnet;

public abstract class Neuron {

    public abstract float calculate(float lastOutput);

    public float weightRandom() {
        return (float) Math.random() * 2 - 1;
    }

    public float normalize(float x) {
        if (x > 1) return 1;
        if (x < 0) return 0;
        return x;
    }

    public static Neuron copyDispatch(Neuron target) {
        if (target instanceof AggregateNeuron) {
            return new AggregateNeuron((AggregateNeuron) target);
        } else {
            return new PrimitiveNeuron((PrimitiveNeuron) target);
        }
    }
}
