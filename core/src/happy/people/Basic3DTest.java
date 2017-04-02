package happy.people;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import happy.people.ObstacleManager.ObstacleManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Basic3DTest implements ApplicationListener {

    public Environment environment;
    public PerspectiveCamera cam;
    public Model model;
    public Model grass;
    public ModelInstance grassInstance;
    public ModelInstance waterInstance;
    public ModelInstance fogInstance;
    public ModelBatch modelBatch;
    ArrayList<ModelInstance> movingModels;
    static final float resetDistance = 200;
    static final float resetPos = -10;
    static float speed = 1.0f;
    static final float camheight = 0.5f;
    static final float addheight = 1.0f;
    static final float gravity = 1.3f;
    static final float OBSTACLE_HEIGHT = 2f;
    static final float OBSTACLE_DEPTH = 0.9f;
    static final float SPEED_ADD = 0.0004f;
    static final float BASE_SPEED = 0.4f;
    static final int NETWORKS_PER_EPOCH = 4;
    float baseSpeed;
    float velocity = 0.0f;
    int networkIndex;
    int epoch;
    float distanceTraveled;
    float stressGenerated;
    BitmapFont font; //or use alex answer to use custom font
    boolean gameOn;
    MuseOscServer mos;
    ObstacleManager[] networks;
    float[] rewards;

    ModelBuilder modelBuilder;
    Model hurdleModel;
    Model leftWallModel;
    Model rightWallModel;

    public float time = 0;
    public long lastUpdateTime;
    private final float JUMP_VELOCITY = 0.4f;
    private Batch batchBatch;
    private ArrayList<ModelInstance> hurdles;

    public Basic3DTest() {
        mos = new MuseOscServer();
        mos.start();
        epoch = 0;
        networkIndex = -1;
        networks = new ObstacleManager[NETWORKS_PER_EPOCH];
        rewards = new float[NETWORKS_PER_EPOCH];
        for (int i = 0; i < NETWORKS_PER_EPOCH; i++) {
            do {
                networks[i] = new ObstacleManager();
            } while (networks[i].isBoring());
        }
    }

    @Override
    public void create () {
        reset();
    }

    public void reset() {

        if (networkIndex > -1) {
            rewards[networkIndex] = objectiveFunction(distanceTraveled,stressGenerated);
        }

        distanceTraveled = 0;
        stressGenerated = 0;

        // Manage network updates
        networkIndex++;
        if (networkIndex >= NETWORKS_PER_EPOCH) {
            // evolve
            networkIndex = 0;

            // Apply rewards and sort
            for (int i = 0; i < NETWORKS_PER_EPOCH; i++) {
                networks[i].reward = rewards[i];
            }

            ArrayList<ObstacleManager> forWorking = new ArrayList<ObstacleManager>();
            for (int i = 0; i < NETWORKS_PER_EPOCH; i++) forWorking.add(networks[i]);
            Collections.sort(forWorking);

            // Keep first, evolve first/second, second/third, random fourth.
            ObstacleManager[] newNetworks = new ObstacleManager[NETWORKS_PER_EPOCH];
            do {
                newNetworks[0] = new ObstacleManager(new ObstacleManager(forWorking.get(0), 0.05f));
            } while (newNetworks[0].isBoring());
            for (int i = 0; i < NETWORKS_PER_EPOCH - 1; i++) {
                do {
                    // 50% chance of crossing over, 50% chance of mutation
                    if (Math.random() < 0.5) {
                        // Mutation
                        ObstacleManager mutated = new ObstacleManager(forWorking.get(i), 0.25f);
                        newNetworks[i+1] = mutated;
                    } else {
                        // Crossing over
                        ObstacleManager mutated = new ObstacleManager(forWorking.get(i), forWorking.get(i+1));
                        newNetworks[i+1] = mutated;
                    }
                } while (newNetworks[i+1].isBoring());
            }

            do {
                newNetworks[NETWORKS_PER_EPOCH-1] = new ObstacleManager();
            } while (newNetworks[NETWORKS_PER_EPOCH-1].isBoring());
            networks = newNetworks;

            epoch++;
        }

        modelBuilder = new ModelBuilder();

        // Obstacle models
        hurdleModel = modelBuilder.createBox(3f, 0.9f, OBSTACLE_HEIGHT + 1,
                new Material(ColorAttribute.createReflection(Color.YELLOW)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        leftWallModel = modelBuilder.createBox(1f, 0.9f, OBSTACLE_HEIGHT + 10,
                new Material(ColorAttribute.createReflection(Color.RED)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        rightWallModel = modelBuilder.createBox(1f, 0.9f, OBSTACLE_HEIGHT + 10,
                new Material(ColorAttribute.createReflection(Color.BLUE)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        hurdles = new ArrayList<ModelInstance>();

        baseSpeed = BASE_SPEED;
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.15f, 0.1f, 0.1f, 9f));
        environment.add(new DirectionalLight().set(0.3f, 0.3f, 0.3f, 0f, 90f, 0f));

        modelBatch = new ModelBatch();
        batchBatch = new SpriteBatch();
        font = new BitmapFont();

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0, 0, camheight);
        cam.lookAt(0,10000,0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        // Fog
        Color fogColor = new Color(0.01f,0.01f,0.01f,0.97f);
        Model fogModel = modelBuilder.createBox(1000f,10f,1000f, new Material(ColorAttribute.createDiffuse(fogColor)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        fogModel.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        fogInstance = new ModelInstance(fogModel);
        fogInstance.transform.setToTranslation(0,150f,0);

        // Grass
        grass = modelBuilder.createBox(200f,200f,2f, new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)),VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        grassInstance = new ModelInstance(grass);

        // Skyscrapers
        model = modelBuilder.createBox(5f, 5f, 50f,
                new Material(ColorAttribute.createDiffuse(Color.BLUE)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        movingModels = new ArrayList<ModelInstance>();
        ModelInstance exInstance = new ModelInstance(model);
        exInstance.transform.setToTranslation(5,0,-1);
        movingModels.add(exInstance);
        exInstance = new ModelInstance(model);
        exInstance.transform.setToTranslation(-5,25,-1);
        movingModels.add(exInstance);

        // Make sidewalk
        Model sidewalkModel = modelBuilder.createBox(3f, 4.9f, 1f,
                new Material(ColorAttribute.createReflection(Color.TAN)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        for (float y = resetPos; y < resetDistance; y += 5) {
            ModelInstance sInstance = new ModelInstance(sidewalkModel);
            sInstance.transform.setToTranslation(0,y,-1);
            movingModels.add(sInstance);
        }

        mintObstacleSet(networks[networkIndex].planObstacles(), 30);
    }

    private void mintObstacleSet(int[] obstacles, float initY) {
        float y = initY;
        ModelInstance mInstance;
        for (int i = 0; i < obstacles.length; i++) {
            switch (obstacles[i]) {
                case ObstacleManager.OBSTACLE_NONE: break;
                case ObstacleManager.OBSTACLE_LEFT:
                    mInstance = new ModelInstance(leftWallModel);
                    mInstance.transform.setToTranslation(-1,y,-1);
                    hurdles.add(mInstance);
                    break;
                case ObstacleManager.OBSTACLE_RIGHT:
                    mInstance = new ModelInstance(rightWallModel);
                    mInstance.transform.setToTranslation(1,y,-1);
                    hurdles.add(mInstance);
                    break;
                case ObstacleManager.OBSTACLE_HURDLE:
                    mInstance = new ModelInstance(hurdleModel);
                    mInstance.transform.setToTranslation(0,y,-1);
                    hurdles.add(mInstance);
                    break;
            }
            y += 15;
        }
    }

    @Override
    public void render () {
        float secondsElapsed = Gdx.graphics.getDeltaTime();
        time++;

        if (gameOn) {
            speed = mos.getNormalizedStressVar() + baseSpeed;
            baseSpeed += SPEED_ADD;

            // Shitty physics
            float z = cam.position.z;
            if (z > camheight) {
                // Flying
                velocity -= gravity * secondsElapsed * speed;
                z += velocity * speed;
            } else if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                z = camheight;
                System.out.println("jump");
                velocity = JUMP_VELOCITY;
                z += 0.0001;
            } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                z = camheight;
                System.out.println("right");
                if (cam.position.x < 3f/2) {
                    cam.position.x += 0.1;
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)){
                z = camheight;
                System.out.println("left");
                if (cam.position.x > -3f/2) {
                    cam.position.x -= 0.1;
                }
            } else {
                velocity = 0;
            }

            //cam.rotate(0.5f, 0,1,0);
            if (time % 120 > 60) {
                // cam.position.set(0, 0, camheight + (addheight * (1/60f) * (time % 60)) );
            } else {
                // cam.position.set(0, 0, camheight + addheight - (addheight * (1/60f) * (time % 60)) );
            }
            cam.position.z = z;
            cam.update();

            // Shitty collision detection
            if (z < OBSTACLE_HEIGHT) {
                for (ModelInstance instance : hurdles) {
                    Vector3 give = new Vector3();
                    Vector3 pos = instance.transform.getTranslation(give);
                    if (pos.y <= 0) {
                        if (pos.y + OBSTACLE_DEPTH > 0) {
                            reset();
                            gameOn = false;
                        }
                    }
                }
            }

            //instance.transform.setFromEulerAngles(0,0,time);
            for (ModelInstance instance : movingModels) {
                Vector3 give = new Vector3();
                Vector3 mxyz = instance.transform.getTranslation(give);
                mxyz.y -= speed * 0.3;
                if (mxyz.y > resetDistance) mxyz.y = resetPos;
                if (mxyz.y < resetPos) mxyz.y = resetDistance;
                instance.transform.setToTranslation(mxyz.x,mxyz.y,mxyz.z);
            }

            distanceTraveled += speed;
            stressGenerated += mos.getNormalizedStressVar();

            ArrayList<ModelInstance> copyModels = new ArrayList<ModelInstance>();
            for (ModelInstance instance : hurdles) {
                Vector3 give = new Vector3();
                Vector3 mxyz = instance.transform.getTranslation(give);
                mxyz.y -= speed * 0.3;
                if (mxyz.y >= resetPos) {
                    copyModels.add(instance);
                }
                instance.transform.setToTranslation(mxyz.x,mxyz.y,mxyz.z);
            }
            hurdles = copyModels;
            if (hurdles.size() < 8) {
                mintObstacleSet(networks[networkIndex].planObstacles(), 160);
            }

            grassInstance.transform.setToTranslation(0,10,-2);
            Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

            modelBatch.begin(cam);
            modelBatch.render(grassInstance, environment);
            for (ModelInstance instance : movingModels) {
                modelBatch.render(instance, environment);
            }
            for (ModelInstance instance : hurdles) {
                modelBatch.render(instance, environment);
            }
            modelBatch.render(fogInstance,environment);
            modelBatch.end();
            batchBatch.begin();
            font.draw(batchBatch,"Stress Level: " + mos.getNormalizedStressVar(),25,25);
            font.draw(batchBatch,"Epoch: " + epoch,25,50);
            font.draw(batchBatch,"Network: " + (networkIndex+1) + " / " + NETWORKS_PER_EPOCH,25,75);
            font.draw(batchBatch,"Objective Function: " + objectiveFunction(distanceTraveled,stressGenerated),25,100);
            batchBatch.end();
        } else {
            modelBatch.begin(cam);
            modelBatch.end();
            batchBatch.begin();
            font.draw(batchBatch,"Stress Level: " + mos.getNormalizedStressVar(),Gdx.graphics.getWidth() /4,Gdx.graphics.getWidth() / 4);
            font.draw(batchBatch,"Press space to begin!",Gdx.graphics.getWidth() /2,Gdx.graphics.getWidth() / 2);
            batchBatch.end();
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) gameOn = true;
        }
    }

    private float objectiveFunction(float distanceTraveled, float stressGenerated) {
        return 10 * (1 + stressGenerated) / (distanceTraveled);
    }

    @Override
    public void dispose () {
        modelBatch.dispose();
        model.dispose();
    }

    @Override
    public void resume () {
    }

    @Override
    public void resize (int width, int height) {
    }

    @Override
    public void pause () {
    }
}