package com.mygdx.mass.Scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.mass.BoxObject.BoxObject;
import com.mygdx.mass.Screens.MapBuilderScreen;
import com.mygdx.mass.Tools.MapFileReader;

import java.util.ArrayList;

public class HUD implements Disposable {

    private MapBuilderScreen mapBuilderScreen;

    private OrthographicCamera camera;
    private ScreenViewport viewport;

    public Stage stage;

    Table table;

    Texture texture;
    TextureRegion textureRegion;
    TextureRegionDrawable textureRegionDrawable;

    private ImageButton wall;
    private ImageButton building;
    private ImageButton door;
    private ImageButton window;
    private ImageButton sentryTower;
    private ImageButton hidingArea;
    private ImageButton targetArea;
    private ImageButton guard;
    private ImageButton intruder;

    private ImageButton load;
    private ImageButton save;
    private ImageButton move;
    private ImageButton delete;
    private ImageButton clear;
    private ImageButton undo;
    private ImageButton redo;
    private ImageButton simulate;
    private ImageButton exit;

    public HUD(final MapBuilderScreen mapBuilderScreen, SpriteBatch batch){
        this.mapBuilderScreen = mapBuilderScreen;

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);

        stage = new Stage(viewport, batch);

        table = new Table();
        table.setFillParent(true);
        table.bottom();

        texture = new Texture(Gdx.files.internal("button.jpg"));
        textureRegion = new TextureRegion(texture);
        textureRegionDrawable = new TextureRegionDrawable(textureRegion);

        wall = new ImageButton(textureRegionDrawable);
        wall.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                mapBuilderScreen.setCurrentState(MapBuilderScreen.State.WALL);
                System.out.println("Current action: Create wall");
            }
        });
        building = new ImageButton(textureRegionDrawable);
        building.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                mapBuilderScreen.setCurrentState(MapBuilderScreen.State.BUILDING);
                System.out.println("Current action: Create building");
            }
        });
        door = new ImageButton(textureRegionDrawable);
        door.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                mapBuilderScreen.setCurrentState(MapBuilderScreen.State.DOOR);
                System.out.println("Current action: Create door");
            }
        });
        window = new ImageButton(textureRegionDrawable);
        window.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                mapBuilderScreen.setCurrentState(MapBuilderScreen.State.WINDOW);
                System.out.println("Current action: Create window");
            }
        });
        sentryTower = new ImageButton(textureRegionDrawable);
        sentryTower.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                mapBuilderScreen.setCurrentState(MapBuilderScreen.State.SENTRY_TOWER);
                System.out.println("Current action: Create sentry tower");
            }
        });
        hidingArea = new ImageButton(textureRegionDrawable);
        hidingArea.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                mapBuilderScreen.setCurrentState(MapBuilderScreen.State.HIDING_AREA);
                System.out.println("Current action: Create hiding area");
            }
        });
        targetArea = new ImageButton(textureRegionDrawable);
        targetArea.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                mapBuilderScreen.setCurrentState(MapBuilderScreen.State.TARGET_AREA);
                System.out.println("Current action: Create targtet area");
            }
        });
        guard = new ImageButton(textureRegionDrawable);
        guard.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                mapBuilderScreen.setCurrentState(MapBuilderScreen.State.GUARD);
                System.out.println("Current action: Create guard");
            }
        });
        intruder = new ImageButton(textureRegionDrawable);
        intruder.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                mapBuilderScreen.setCurrentState(MapBuilderScreen.State.INTRUDER);
                System.out.println("Current action: Create intruder");
            }
        });

        load = new ImageButton(textureRegionDrawable);
        load.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                System.out.println("Current action: Load map");
            }
        });
        save = new ImageButton(textureRegionDrawable);
        save.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                MapFileReader.saveToFile(mapBuilderScreen.mass.getMap());
                System.out.println("Current action: Save map");
            }
        });
        move = new ImageButton(textureRegionDrawable);
        move.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                mapBuilderScreen.setCurrentState(MapBuilderScreen.State.MOVE);
                System.out.println("Current action: Move");
            }
        });
        delete = new ImageButton(textureRegionDrawable);
        delete.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                mapBuilderScreen.setCurrentState(MapBuilderScreen.State.DELETION);
                System.out.println("Current action: Delete");
            }
        });
        clear = new ImageButton(textureRegionDrawable);
        clear.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                System.out.println("Current action: Clear map");
               ArrayList<BoxObject> mapObjectList=  mapBuilderScreen.mass.map.getBoxObjects();

                for(int i=0; i<mapObjectList.size(); i++){
                    mapBuilderScreen.mass.world.destroyBody( mapBuilderScreen.mass.map.getBoxObjects().get(i).getBody());
                }
                mapObjectList.clear();
            }
        });
        undo = new ImageButton(textureRegionDrawable);
        undo.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                System.out.println("Current action: Undo");
            }
        });
        redo = new ImageButton(textureRegionDrawable);
        redo.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                System.out.println("Current action: Redo");
            }
        });
        simulate = new ImageButton(textureRegionDrawable);
        simulate.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                System.out.println("Current action: Simulate");
            }
        });
        exit = new ImageButton(textureRegionDrawable);
        exit.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                System.out.println("Current action: Exit");
            }
        });

        table.add(wall);
        table.add(building);
        table.add(door);
        table.add(window);
        table.add(sentryTower);
        table.add(hidingArea);
        table.add(targetArea);
        table.add(guard);
        table.add(intruder);

        table.row();

        table.add(load).padBottom(10);
        table.add(save).padBottom(10);
        table.add(move).padBottom(10);
        table.add(delete).padBottom(10);
        table.add(clear).padBottom(10);
        table.add(undo).padBottom(10);
        table.add(redo).padBottom(10);
        table.add(simulate).padBottom(10);
        table.add(exit).padBottom(10);

        stage.addActor(table);
    }

    public void update(float dt){

    }

    public void dispose() {
        stage.dispose();
    }

}
