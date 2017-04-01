package happy.people;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

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
    static final float speed = -0.5f;


    public float time = 0;
    @Override
    public void create () {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.1f, 0.1f, 0.1f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.3f, 0.3f, 0f, 90f, 0f));
        modelBatch = new ModelBatch();

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0, 0, 1.6f);
        cam.lookAt(0,10000,0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();
        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(5f, 5f, 50f,
                new Material(ColorAttribute.createDiffuse(Color.BLUE)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        Color fogColor = new Color(0.01f,0.01f,0.01f,0.97f);
        Model fogModel = modelBuilder.createBox(1000f,10f,1000f, new Material(ColorAttribute.createDiffuse(fogColor)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        fogModel.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        fogInstance = new ModelInstance(fogModel);
        fogInstance.transform.setToTranslation(0,150f,0);
        grass = modelBuilder.createBox(200f,200f,2f, new Material(ColorAttribute.createDiffuse(Color.GREEN)),VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        grassInstance = new ModelInstance(grass);
        // Water
        
        movingModels = new ArrayList<>();
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
    }

    @Override
    public void render () {
        time++;
        //instance.transform.setFromEulerAngles(0,0,time);
        for (ModelInstance instance : movingModels) {
            Vector3 give = new Vector3();
            Vector3 mxyz = instance.transform.getTranslation(give);
            mxyz.y += speed;
            if (mxyz.y > resetDistance) mxyz.y = resetPos;
            if (mxyz.y < resetPos) mxyz.y = resetDistance;
            instance.transform.setToTranslation(mxyz.x,mxyz.y,mxyz.z);
        }

        grassInstance.transform.setToTranslation(0,10,-2);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);
        modelBatch.render(grassInstance, environment);
        for (ModelInstance instance : movingModels) {
            modelBatch.render(instance, environment);
        }
        modelBatch.render(fogInstance,environment);
        modelBatch.end();
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