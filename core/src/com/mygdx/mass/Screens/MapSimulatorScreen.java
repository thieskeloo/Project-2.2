package com.mygdx.mass.Screens;

import box2dLight.RayHandler;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.mass.Agents.Agent;
import com.mygdx.mass.Agents.Guard;
import com.mygdx.mass.Agents.Intruder;
import com.mygdx.mass.Algorithms.Explore;
import com.mygdx.mass.Algorithms.PredictionPoint;
import com.mygdx.mass.BoxObject.*;
import com.mygdx.mass.Data.MASS;
import com.mygdx.mass.Graph.Gap;
import com.mygdx.mass.Scenes.MapSimulatorHUD;
import com.mygdx.mass.Scenes.MapSimulatorInfo;
import com.mygdx.mass.Sensors.RayCastField;
import com.mygdx.mass.World.IndividualMap;
import com.mygdx.mass.World.Map;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import static com.mygdx.mass.BoxObject.Door.State.CLOSED;

public class MapSimulatorScreen implements Screen {

    public MASS mass;

    private OrthographicCamera camera;
    private ScreenViewport viewport;

    private SpriteBatch batch;

    private World world;
    private Box2DDebugRenderer debugRenderer;

    private Map map;

    private RayHandler rayHandler;

    private ShapeRenderer shapeRenderer;

    public MapSimulatorHUD hud;
    private MapSimulatorInfo info;

    private InputMultiplexer inputMultiplexer;
    private InputHandler inputHandler;

    private long simulationStep = 0;
    private double simulationTime = 0;

    public MapSimulatorScreen(MASS mass) {
        this.mass = mass;
        this.camera = mass.camera;
        this.viewport = mass.viewport;
        this.batch = mass.batch;
        this.world = mass.world;
        this.debugRenderer = mass.debugRenderer;
        this.map = mass.getMap();
        this.rayHandler = mass.rayHandler;
        this.shapeRenderer = mass.shapeRenderer;

        camera.position.set(map.getWidth()/2,map.getHeight()/2,0.0f);
        viewport.setUnitsPerPixel(1/mass.PPM);

        hud = new MapSimulatorHUD(this);
        info = new MapSimulatorInfo(this, batch);

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(hud.stage);
        inputHandler = new InputHandler();
        inputMultiplexer.addProcessor(inputHandler);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    private float accumulator = 0;
    private int worldSpeedFactor = 20; //how fast the world update per time unit, more steps etc
    //private int unitSpeedFactor = 1; //how fast the agents update per world step, should pretty much always be 1


    public void update(float delta) {
        float worldSpeedCap = 0.20f; // Prevents spiral of death slowdown
        boolean worldSpeedCapReached;
        float timePassed = Math.min(delta, worldSpeedCap);

        // Logic to automatically lower the world speed when heavy lag happens
        worldSpeedCapReached = (delta >= worldSpeedCap ? true : false); // check if the speed cap is reached
        if (worldSpeedCapReached && worldSpeedFactor > 1) {
            int oldSpeedFactor = worldSpeedFactor;
            int newSpeedFactor = (int) Math.ceil(worldSpeedFactor *0.6);
            if (newSpeedFactor == worldSpeedFactor) {
                worldSpeedFactor--;
            }
            else { worldSpeedFactor = newSpeedFactor; }
            System.out.println("World speed cap reached! Lowering the world speed factor by 40% from "+oldSpeedFactor+" to "+worldSpeedFactor);
        }

        try {
            accumulator += timePassed;
            while (accumulator >= MASS.FIXED_TIME_STEP) {
                for (int i = 0; i < worldSpeedFactor; i++) {
                    MapSimulatorInfo.resetRayCounters();
                    for (Guard guard : map.getGuards()) {
                        guard.update(MASS.FIXED_TIME_STEP);
                    }
                    for (Intruder intruder : map.getIntruders()) {
                        intruder.update(MASS.FIXED_TIME_STEP);
                    }

                    world.step(MASS.FIXED_TIME_STEP, 6, 2);
                    simulationStep++;
                    simulationTime = simulationTime + MASS.FIXED_TIME_STEP;
                }
                accumulator -= MASS.FIXED_TIME_STEP;
            }
        } catch (ConcurrentModificationException c){
//            System.out.println(c.getCause());
        }

        camera.update();

        hud.update(delta);
        info.update(delta);
    }

    public void handleInput(float delta) {
        //move camera
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.position.x -= MASS.CAMERA_SPEED * delta;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.position.x += MASS.CAMERA_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.position.y += MASS.CAMERA_SPEED * delta;
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.position.y -= MASS.CAMERA_SPEED * delta;
        }

        //zoom in/out
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            ArrayList<Intruder> intuders = map.getIntruders();
            if(!intuders.isEmpty()){
                Body body = intuders.get(0).getBody();
                body.setTransform(body.getWorldCenter(), (float)((body.getAngle()-(Math.PI)*delta)));
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            ArrayList<Intruder> intuders = map.getIntruders();
            if(!intuders.isEmpty()){
                Body body = intuders.get(0).getBody();
                body.setTransform(body.getWorldCenter(), (float)(body.getAngle()+(Math.PI)*delta));
            }
        }

        boolean pauseHelper = true, isPaused = true;
        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            if (pauseHelper) {
                if (isPaused) {
                    mass.GapNavigationTreeAlgorithm.loadAllAgents();
                    mass.GapNavigationTreeAlgorithm.act();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }
                    if (!map.getAgents().isEmpty()) {
                        boolean toggle = !(map.getAgents().get(0).getGapSensorStatus()); // make sure all agents toggle to the same value
                        for (Agent a : map.getAgents()) {
                            // a.setGapSensorStatus(toggle);

                        }
                    }
                    isPaused = false;
                }
                else isPaused = true;
                pauseHelper = false;
            }
            else pauseHelper = true;

        }
        //add keyboard controls to the first intruder
//        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
//            ArrayList<Intruder> intruders = map.getIntruders();
//            if(!intruders.isEmpty()) {
//                Body body = intruders.get(0).getBody();
//                float x = (float) Math.cos(body.getAngle()) * Agent.BASE_SPEED*2;
//                float y = (float) Math.sin(body.getAngle()) * Agent.BASE_SPEED*2;
//                body.setLinearVelocity(x, y);
//            }
//        } else {
//            ArrayList<Intruder> intruders = map.getIntruders();
//            if(!intruders.isEmpty()) {
//                Body body = intruders.get(0).getBody();
//                body.setLinearVelocity(0, 0);
//            }
//        }

        if (Gdx.input.isKeyPressed(Input.Keys.I) && mass.PPM < MASS.MAXIMAL_ZOOM) {
            mass.PPM *= 1.01;
            viewport.setUnitsPerPixel(1/mass.PPM);
            viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        } else if (Gdx.input.isKeyPressed(Input.Keys.O) && mass.PPM > MASS.MINIMAL_ZOOM) {
            mass.PPM /= 1.01;
            viewport.setUnitsPerPixel(1/mass.PPM);
            viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        } else if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)){
            System.out.println("Escaping to Main Menu");
            mass.PPM = mass.MINIMAL_ZOOM;
            mass.viewport.setUnitsPerPixel(1/mass.PPM);
            mass.viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
            ((Game) Gdx.app.getApplicationListener()).setScreen(mass.mainMenuScreen);
        }

        //for test purpose
        if(false) {
            if (Gdx.input.justTouched()) {
                for (Agent agent : map.getAgents()) {
                    agent.getRoute().clear();
                    agent.setDestination(inputHandler.toWorldCoordinate(Gdx.input.getX(), Gdx.input.getY()));
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        handleInput(delta);

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling?GL20.GL_COVERAGE_BUFFER_BIT_NV:0)); //anti aliasing

        Gdx.gl.glLineWidth(1);
        debugRenderer.render(world, camera.combined);

        //draw the sprites
        drawSprites();

        //draw the light effects
        rayHandler.setCombinedMatrix(camera);
        rayHandler.updateAndRender();

        //draw the shapes and lines
        shapeRenderer.setProjectionMatrix(camera.combined);
        drawRays();
        drawAgentPaths();
        drawBoxObjects();
        drawAgents();
        drawGapsFromAgents();

        //draw an agent's unexplored location on its local map
//        if (!map.getAgents().isEmpty()) {
//            drawUnexploredPoints(map.getAgents().get(0));
//        }

        if (!map.getGuards().isEmpty() && map.getGuards().get(0).getPredictionModel() != null) { //for the sake of testing
            if (!map.getGuards().isEmpty() && !map.getGuards().get(0).getPredictionModel().guardMoves.isEmpty()) {
                for (PredictionPoint predictionPoint : map.getGuards().get(0).getPredictionModel().guardMoves) {
                    Gdx.gl.glLineWidth(4);
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                    shapeRenderer.setColor(0.0f, 1.0f, 0.0f, 1.0f);
                    shapeRenderer.circle(predictionPoint.getPosition().x, predictionPoint.getPosition().y, 1);
                    shapeRenderer.end();
                }
            }
            if (!map.getGuards().isEmpty() && !map.getGuards().get(0).getPredictionModel().intruderMoves.isEmpty()) {
                for (PredictionPoint predictionPoint : map.getGuards().get(0).getPredictionModel().intruderMoves) {
                    Gdx.gl.glLineWidth(4);
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                    shapeRenderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);
                    shapeRenderer.circle(predictionPoint.getPosition().x, predictionPoint.getPosition().y, 1);
                    shapeRenderer.end();
                }
            }
        }

        batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();

        mass.batch.setProjectionMatrix(info.stage.getCamera().combined);
        info.stage.draw();

        update(delta);
    }

    public void drawUnexploredPoints(Agent agent) {
        Gdx.gl.glLineWidth(4);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);
        for (Vector2 vector2 : agent.getIndividualMap().getUnexploredPlaces()) {
            shapeRenderer.circle(vector2.x, vector2.y, 1);
        }
        shapeRenderer.end();
    }

    private void drawSprites() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.end();
    }

    private void drawAgentPaths() {
        Gdx.gl.glLineWidth(1);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Agent agent : map.getAgents()) {
            if (agent instanceof Guard) {
                switch (((Guard) agent).currentState) {
                    case EXPLORE: {
                        shapeRenderer.setColor(Color.PURPLE);
                        break;
                    }
                    case PATROL: {
                        shapeRenderer.setColor(Color.GREEN);
                        break;
                    }
                    case SEARCH: {
                        shapeRenderer.setColor(Color.YELLOW);
                        break;
                    }
                    case CHASE: {
                        shapeRenderer.setColor(Color.RED);
                        break;
                    }
                }
            } else if (agent instanceof Intruder) {
                shapeRenderer.setColor(Color.WHITE);
//                switch (((Intruder) agent).currentState) {
//                    case SEARCH: {
//                        shapeRenderer.setColor(Color.PURPLE);
//                        break;
//                    }
//                }
            }
            Vector2 start = agent.getBody().getPosition();
            Vector2 end = agent.getDestination();
            if (end != null) {
                shapeRenderer.line(start, end);
            }
            for (Vector2 waypoint : agent.getRoute()) {
                start = end;
                end = waypoint;
                if (agent.getDestination() != null && start != null) {
                    shapeRenderer.line(start, end);
                }
            }
        }
        shapeRenderer.end();
    }

    private void drawBoxObjects() {
        Gdx.gl.glLineWidth(4);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (BoxObject boxObject : map.getBoxObjects()) {
            switch (boxObject.getObjectType()) {
                case WALL:
                    shapeRenderer.setColor(0.50f, 0.50f, 0.50f, 1.00f);
                    break;
                case BUILDING:
                    shapeRenderer.setColor(0.41f, 0.59f, 0.73f, 1.00f);
                    break;
                case DOOR:
                    Door door = (Door) boxObject;
                    if (door.getCurrentState() == CLOSED) {
                        shapeRenderer.setColor(1.00f, 1.00f, 0.00f, 1.00f);
                    } else {
                        shapeRenderer.setColor(0.00f, 1.00f, 0.00f, 1.00f);
                    }
                    break;
                case WINDOW:
                    shapeRenderer.setColor(1.00f, 1.00f, 1.00f, 1.00f);
                    break;
                case SENTRY_TOWER:
                    shapeRenderer.setColor(1.00f, 0.81f, 0.00f, 1.00f);
                    break;
                case HIDING_AREA:
                    shapeRenderer.setColor(0.00f, 0.50f, 0.00f, 1.00f);
                    break;
                case TARGET_AREA:
                    shapeRenderer.setColor(1.00f, 0.45f, 0.45f, 1.00f);
                    break;
            }
            Rectangle rectangle = boxObject.getRectangle();
            shapeRenderer.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        }
        shapeRenderer.end();
    }

    private void drawAgents() {
        Gdx.gl.glLineWidth(4);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Agent agent: map.getAgents()) {
            switch (agent.getAgentType()) {
                case GUARD:
                    shapeRenderer.setColor(0.0f, 0.0f, 1.0f, 1.0f);
                    break;
                case INTRUDER:
                    shapeRenderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);
                    break;
            }
            shapeRenderer.circle(agent.getBody().getPosition().x, agent.getBody().getPosition().y, 1.0f);
        }
        shapeRenderer.end();
    }

//    public void drawPredictionPoints() {
//        if (map instanceof IndividualMap && ((IndividualMap) map).getAgent() instanceof Guard) {
//            Gdx.gl.glLineWidth(4);
//            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//            shapeRenderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);
//            if (((Guard) ((IndividualMap) map).getAgent()).getPredictionModel() != null) {
//                for (PredictionPoint predictionPoint : ((Guard) ((IndividualMap) map).getAgent()).getPredictionModel().getPredictionPoints()) {
//                    shapeRenderer.circle(predictionPoint.getPosition().x, predictionPoint.getPosition().y, 1);
//                }
//            }
//            shapeRenderer.end();
//        }
//    }

    private void drawRays() {
        Gdx.gl.glLineWidth(0.05f);

        for (Agent agent:map.getAgents()) {
            if(agent.getAllRayCastFields() != null) {
                for (RayCastField rayCastField : agent.getAllRayCastFields()) {
                    if (rayCastField != null) {
                        shapeRenderer.end();
                        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

                        if(rayCastField.getTypeOfField().equalsIgnoreCase("TOWER")) {
                            shapeRenderer.setColor(1.00f, 0.81f, 0.00f, 1.0f);
                        }
                        else if (rayCastField.getTypeOfField().equalsIgnoreCase("BUILDING")) {
                            shapeRenderer.setColor(1.0f,1.0f,1.0f,1.0f);
                        }
                        else if (rayCastField.getTypeOfField().equalsIgnoreCase("AGENT") && agent instanceof Guard) {
                            shapeRenderer.setColor(0.0f, 0.0f, 1.0f, 1.0f);
                        }
                        else if (rayCastField.getTypeOfField().equalsIgnoreCase("AGENT") && agent instanceof Intruder) {
                            shapeRenderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);
                        }
                        else if (rayCastField.getTypeOfField().equalsIgnoreCase("GAP SENSOR")) {
                            shapeRenderer.setColor(0.0f,1.0f,1.0f,1.0f);
                        }

                        Vector2[] beginPointRays = rayCastField.beginPointRay();
                        Vector2[] endPointRays = rayCastField.endPointRay();
                        for (int i = 0; i < beginPointRays.length; i++) {
                            shapeRenderer.line(beginPointRays[i], endPointRays[i]);
                        }
                        shapeRenderer.end();
                    }
                }
            }
            if(agent.getAngleDistanceCloudPoints().size() > 0 && agent.getDrawGapSensor()) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                if (agent.getRayCastFieldGapSensor().getTypeOfField().equalsIgnoreCase("GAP SENSOR")) {
                    shapeRenderer.setColor(0.0f,1.0f,1.0f,1.0f);
                }

                Vector2[] beginPointRays = agent.getRayCastFieldGapSensor().beginPointRay();
                Vector2[] endPointRays = agent.getRayCastFieldGapSensor().endPointRay();
                for (int i = 0; i < beginPointRays.length; i++) {
                    shapeRenderer.line(beginPointRays[i], endPointRays[i]);
                }
                shapeRenderer.end();
            }
        }
        shapeRenderer.end();

    }

    private void drawGapsFromAgents() {
        ArrayList<Gap> gaps = new ArrayList<Gap>();

        for (Agent agent:map.getAgents()) {
            if (agent.getGapSensor() != null) {
                for (java.util.Map.Entry<Float, Gap> e : agent.getGapSensor().getGapList().entrySet()) {
                    gaps.add(e.getValue());
                }
            }
        }

        if (!gaps.isEmpty()) drawGaps(gaps);
    }

    private void drawGaps(ArrayList<Gap> gaps) {

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0 ; i < gaps.size() ; i++) {
            shapeRenderer.setColor(gaps.get(i).getColor());
            Vector2 location = gaps.get(i).getOffsetLocation();
            float x = location.x;
            float y = location.y;
            float lineSize = 0.5f;
            shapeRenderer.line(x - lineSize, y, x + lineSize, y);
            shapeRenderer.line(x, y - lineSize, x, y + lineSize);

        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0 ; i < gaps.size() ; i++) {
            shapeRenderer.setColor(gaps.get(i).getColor());
            Vector2 location = gaps.get(i).getLocation();
            float x = location.x;
            float y = location.y;
            float lineSize = 0.5f;
            shapeRenderer.circle(x, y, lineSize);
        }
        shapeRenderer.end();

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width,height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
//        hud.dispose();
    }

    //This is the part where mouse and keyboard events are being handle
    private class InputHandler implements InputProcessor {

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.SPACE) {
                Explore explore = new Explore();
                for (Agent agent : MASS.map.getAgents()) {
                    explore.start(agent);
                }
            } else if (keycode == Input.Keys.T) {
                if (!mass.getMap().getAgents().isEmpty()) {
                    setMap(mass.getMap().getAgents().get(0).getIndividualMap());
                }
            } else if (keycode == Input.Keys.B) {
                setMap(mass.getMap());
            } else if (keycode == Input.Keys.M) {
                for (Intruder intruder : map.getIntruders()) {
                    intruder.setMoveSpeed(3.0f);
                }
            } else if (keycode == Input.Keys.C) {
//                if (map instanceof IndividualMap && ((IndividualMap) map).getAgent() instanceof Guard) {
//                    ((Guard) ((IndividualMap) map).getAgent()).getPredictionModel().run = true;
//                }
            }
            return true;
        }

        @Override
        public boolean keyUp(int keycode) { return false; }

        @Override
        public boolean keyTyped(char character) { return false; }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            Vector2 coordinates = toWorldCoordinate(screenX, screenY);
            for (int i = 0; i< mass.getMap().getBoxObjects().size(); i++) {
//                System.out.println(mass.getMap().getBoxObjects().get(i).getRectangle().toString());
                if  (coordinates.x < 0 || coordinates.x > 200 || coordinates.y < 0 || coordinates.y > 200){
                    System.out.println("Agent cannot travel to " + coordinates.toString());
                    return true;
                }
            }
            for (Intruder intruder : map.getIntruders()) {
                intruder.goTo(coordinates);
            }
            for (Guard guard : map.getGuards()) {
//                guard.setDestination(null);
//                guard.getRoute().clear();
            }
            return true;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }

        @Override
        public boolean mouseMoved(int screenX, int screenY) { return false; }

        @Override
        public boolean scrolled(int amount) { return false; }

        private Vector2 toWorldCoordinate(int screenX, int screenY) {
            float x = camera.position.x - Gdx.graphics.getWidth() / mass.PPM / 2 + screenX / mass.PPM;
            float y = camera.position.y - Gdx.graphics.getHeight() / mass.PPM / 2 + (Gdx.graphics.getHeight() - screenY) / mass.PPM;
            return new Vector2(x,y);
        }

        private boolean insideMap(Vector2 point) {
            return point.x >= 0 && point.x <= map.getWidth() && point.y >= 0 && point.y <= map.getHeight();
        }
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public int getWorldSpeedFactor() {
        return worldSpeedFactor;
    }

    public void setWorldSpeedFactor(int worldSpeedFactor) {
        this.worldSpeedFactor = worldSpeedFactor;
    }

    public void resetSimulationTimers() {
        simulationTime = 0;
        simulationStep = 0;
    }

    public long getSimulationStep() {
        return simulationStep;
    }

    public double getSimulationTime() {
        return simulationTime;
    }
}
