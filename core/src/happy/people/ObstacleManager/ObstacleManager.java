package happy.people.ObstacleManager;

import happy.people.ObstacleManager.trevnet.AggregateNeuron;
import happy.people.ObstacleManager.trevnet.Neuron;
import happy.people.ObstacleManager.trevnet.PrimitiveNeuron;

import java.util.ArrayList;

public class ObstacleManager implements Comparable {

    public float reward;
    boolean boring;

    public static void main(String[] args) {
        // test
        ObstacleManager theMan = new ObstacleManager();
        int[] out = theMan.planObstacles();
        for (int i = 0; i < out.length; i++) {
            System.out.println(out[i]);
        }
    }

    public static final int UNIT_SIZE = 10;
    public static final int MAX_OBSTACLE_PER_UNIT = 10;
    public static final int OBSTACLE_NONE = -1;
    public static final int OBSTACLE_LEFT = 0;
    public static final int OBSTACLE_RIGHT = 1;
    public static final int OBSTACLE_HURDLE = 2;

    private float lastOutput;
    private Neuron topNeuron;

    public int[] planObstacles() {
        int[] obstacles = new int[UNIT_SIZE];
        for (int i = 0; i < UNIT_SIZE; i++) obstacles[i] = -1;
        int numPlaced = 0;
        for (int i = 0; i < UNIT_SIZE; i++) {
            if (numPlaced >= MAX_OBSTACLE_PER_UNIT) break;
            lastOutput = topNeuron.calculate(lastOutput);
            obstacles[i] = neuronToObstacle(lastOutput);
            if (obstacles[i] != -1) numPlaced++;
        }

//        int fixIt = 0;
//        while (numPlaced < MAX_OBSTACLE_PER_UNIT) {
//            // Corrective obstacle
//            obstacles[UNIT_SIZE-1-fixIt] = 2;
//            numPlaced++;
//            fixIt++;
//        }

        int max = -1;
        for (int i = 0; i < obstacles.length; i++) {
            max = max < obstacles[i] ? obstacles[i] : max;
        }

        boring = true;

        for (int i = 0; i < obstacles.length; i++) {
            if ( (obstacles[i] != max) && (obstacles[i] != -1) && (max != -1)) {
                boring = false;
                break;
            }
        }

        return obstacles;
    }

    // Init with a standard net.
    public ObstacleManager() {
        lastOutput = 0;
        ArrayList<Neuron> children = new ArrayList<Neuron>();
        PrimitiveNeuron p1 = new PrimitiveNeuron();
        PrimitiveNeuron p2 = new PrimitiveNeuron();
        PrimitiveNeuron p3 = new PrimitiveNeuron();
        children.add(p1);
        children.add(p2);
        children.add(p3);
        topNeuron = new AggregateNeuron(children);
    }

    // Copy constructor
    public ObstacleManager(ObstacleManager toCopy) {
        lastOutput = 0;
        if (toCopy.topNeuron instanceof AggregateNeuron) {
            topNeuron = new AggregateNeuron((AggregateNeuron) toCopy.topNeuron);
        } else if (toCopy.topNeuron instanceof PrimitiveNeuron) {
            topNeuron = new PrimitiveNeuron((PrimitiveNeuron) toCopy.topNeuron);
        } else {
            topNeuron = null;
        }
    }

    // Mutation constructor
    public ObstacleManager(ObstacleManager toMutate, float mutationProbability) {
        lastOutput = 0;
        if (toMutate.topNeuron instanceof AggregateNeuron) {
            topNeuron = new AggregateNeuron((AggregateNeuron) toMutate.topNeuron, mutationProbability);
        } else if (toMutate.topNeuron instanceof PrimitiveNeuron) {
            topNeuron = new PrimitiveNeuron((PrimitiveNeuron) toMutate.topNeuron, mutationProbability);
        } else {
            topNeuron = null;
        }
    }

    private Neuron dualTraversal(Neuron left, Neuron right) {
        if (right == null) return Neuron.copyDispatch(left);
        if (left == null) return Neuron.copyDispatch(right);

        if (left instanceof  AggregateNeuron && right instanceof  AggregateNeuron) {
            return dualTraversalAgg((AggregateNeuron)left,(AggregateNeuron)right);
        }

        if (left instanceof  PrimitiveNeuron && right instanceof  AggregateNeuron) {
            return dualTraversalLp((PrimitiveNeuron)left,(AggregateNeuron)right, 0.1f);
        }

        if (left instanceof  AggregateNeuron && right instanceof  PrimitiveNeuron) {
            return dualTraversalLp((PrimitiveNeuron)right,(AggregateNeuron)left, 0.9f);
        }

        if (Math.random() < 0.5) {
            return Neuron.copyDispatch(left);
        } else {
            return Neuron.copyDispatch(right);
        }
    }

    private Neuron dualTraversalLp(PrimitiveNeuron left, AggregateNeuron right, float prob) {
        if (Math.random() < prob) {
            // keep right, but add the left
            AggregateNeuron newNeu = new AggregateNeuron(right);
            newNeu.children.add(Neuron.copyDispatch(left));
            newNeu.weights.add(newNeu.weightRandom());
            return newNeu;
        } else {
            return Neuron.copyDispatch(left);
        }
    }

    private Neuron dualTraversalAgg(AggregateNeuron left, AggregateNeuron right) {
        Neuron n;
        if (Math.random() < 0.7) {
            Neuron randomChild = right.children.get((int) Math.floor(Math.random() * right.children.size()));
            return dualTraversal(left, randomChild);
        } else {
            // Combine!
            ArrayList<Float> weights = new ArrayList<Float>();
            ArrayList<Neuron> neurons = new ArrayList<Neuron>();
            for (int i = 0; i < left.children.size(); i++) {
                if (Math.random() < 0.5) {
                    weights.add(left.weights.get(i));
                    neurons.add(Neuron.copyDispatch(left.children.get(i)));
                }
            }

            for (int i = 0; i < right.children.size(); i++) {
                if (Math.random() < 0.5) {
                    weights.add(right.weights.get(i));
                    neurons.add(Neuron.copyDispatch(right.children.get(i)));
                }
            }

            return new AggregateNeuron(neurons, weights);
        }
    }

    // Crossing over constructor
    public ObstacleManager(ObstacleManager oLeft, ObstacleManager oRight) {
        this(oLeft);
        ObstacleManager mutateLeft = new ObstacleManager(oLeft, 0.1f);
        ObstacleManager mutateRight = new ObstacleManager(oRight, 0.1f);
        this.topNeuron = dualTraversal(mutateLeft.topNeuron, mutateRight.topNeuron);
    }

    public int neuronToObstacle(float neuronOut) {
        if (neuronOut < 0.25) {
            return OBSTACLE_NONE;
        } else if (neuronOut < 0.5) {
            return OBSTACLE_LEFT;
        } else if (neuronOut < 0.75) {
            return OBSTACLE_RIGHT;
        } else {
            return OBSTACLE_HURDLE;
        }
    }

    @Override
    public int compareTo(Object other) {
        if (other instanceof ObstacleManager) {
            ObstacleManager om = (ObstacleManager) other;
            if (this.reward > om.reward) {
                return -1;
            }
            if (this.reward == om.reward) {
                return 0;
            }
            return 1;
        }

        return 0;
    }

    public boolean isBoring() {
        planObstacles();
        this.lastOutput = 0;
        return boring;
    }

}
