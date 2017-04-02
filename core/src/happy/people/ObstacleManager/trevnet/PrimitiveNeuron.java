package happy.people.ObstacleManager.trevnet;

public class PrimitiveNeuron extends Neuron {
    float weight;
    float base;
    float memory;
    float memoryAlpha;

    public PrimitiveNeuron() {
        this.weight = weightRandom();
        this.base = weightRandom();
        this.memory = 0;
        this.memoryAlpha = weightRandom();
    }

    public PrimitiveNeuron(PrimitiveNeuron toCopy) {
        this.weight = toCopy.weight;
        this.base = toCopy.base;
    }

    public PrimitiveNeuron(PrimitiveNeuron toCopy, float mutationProbability) {
        if (Math.random() < mutationProbability) {
            weight = weightRandom();
        } else {
            weight = toCopy.weight;
        }

        if (Math.random() < mutationProbability) {
            base = weightRandom();
        } else {
            base = toCopy.base;
        }

        if (Math.random() < mutationProbability) {
            memoryAlpha = weightRandom();
        } else {
            memoryAlpha = toCopy.memoryAlpha;
        }
    }

    @Override
    public float calculate(float lastOutput) {
        if (Math.random() < memoryAlpha) {
            memory = lastOutput;
        }
        if (Math.random() < memoryAlpha / 3) {
            memory = 0;
        }
        return normalize(weight * lastOutput + (memory + 0.001f) + base );
    }
}
