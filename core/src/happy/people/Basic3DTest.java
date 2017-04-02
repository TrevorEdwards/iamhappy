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
	static float speed = 1.0f;
	static final float camheight = 0.5f;
	static final float addheight = 1.0f;
	static final float gravity = 1.3f;
	static final float OBSTACLE_HEIGHT = 2f;
	static final float OBSTACLE_DEPTH = 0.9f;
	static final float SPEED_ADD = 0.0004f;
	static final float BASE_SPEED = 0.4f;
	float baseSpeed;
	float velocity = 0.0f;
	BitmapFont font; //or use alex answer to use custom font
	boolean gameOn;
	MuseOscServer mos;

	public float time = 0;
	public long lastUpdateTime;
	private final float JUMP_VELOCITY = 0.4f;
	private Batch batchBatch;
	private ArrayList<ModelInstance> hurdles;

	public Basic3DTest() {
		mos = new MuseOscServer();
		mos.start();
	}
	@Override
	public void create () {
		reset();
	}

	public void reset() {
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

		ModelBuilder modelBuilder = new ModelBuilder();

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

		// Hurdles
		hurdles = new ArrayList<ModelInstance>();
		Model hurdleModel = modelBuilder.createBox(3f, 0.9f, OBSTACLE_HEIGHT + 1,
				new Material(ColorAttribute.createReflection(Color.YELLOW)),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
		for (float y = 30; y < resetDistance; y += 15) {
			ModelInstance sInstance = new ModelInstance(hurdleModel);
			sInstance.transform.setToTranslation(0,y,-1);
			movingModels.add(sInstance);
			hurdles.add(sInstance);
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
			batchBatch.begin();
			font.draw(batchBatch,"Stress Level: " + mos.getNormalizedStressVar(),Gdx.graphics.getWidth() /4,Gdx.graphics.getWidth() / 4);
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