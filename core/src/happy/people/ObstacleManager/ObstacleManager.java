package happy.people.ObstacleManager;

import happy.people.ObstacleManager.trevnet.AggregateNeuron;
import happy.people.ObstacleManager.trevnet.Neuron;
import happy.people.ObstacleManager.trevnet.PrimitiveNeuron;

import java.util.ArrayList;

public class ObstacleManager {

    public static void main(String[] args) {
        // test
        ObstacleManager theMan = new ObstacleManager();
        int[] out = theMan.planObstacles();
        for (int i = 0; i < out.length; i++) {
            System.out.println(out[i]);
        }
    }

    public static final int UNIT_SIZE = 10;
    public static final int MAX_OBSTACLE_PER_UNIT = 8;
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

        if (numPlaced == 0) {
            // Corrective obstacle
            obstacles[0] = 2;
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

}