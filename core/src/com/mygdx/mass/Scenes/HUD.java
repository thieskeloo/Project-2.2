package com.mygdx.mass.Scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.mass.Screens.MapBuilderScreen;

public class HUD implements Disposable {

    private MapBuilderScreen mapBuilderScreen;

    private OrthographicCamera camera;
    private ScreenViewport viewport;

    public Stage stage;

    Table table;

    Texture texture;
    TextureRegion textureRegion;
    TextureRegionDrawable textureRegionDrawable;

    private ImageButton building;
    private ImageButton sentryTower;
    private ImageButton hidingArea;
    private ImageButton targetArea;

    private ImageButton load;
    private ImageButton save;
    private ImageButton clear;
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

        building = new ImageButton(textureRegionDrawable);
        building.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                mapBuilderScreen.setCurrentState(MapBuilderScreen.State.BUILDING);
                System.out.println("Current action: Create building");
            }
        });
        sentryTower = new ImageButton(textureRegionDrawable);
        sentryTower.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                mapBuilderScreen.setCurrentState(MapBuilderScreen.State.SENTRYTOWER);
                System.out.println("Current action: Create sentry tower");
            }
        });
        hidingArea = new ImageButton(textureRegionDrawable);
        hidingArea.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                mapBuilderScreen.setCurrentState(MapBuilderScreen.State.HIDINGAREA);
                System.out.println("Current action: Create hiding area");
            }
        });
        targetArea = new ImageButton(textureRegionDrawable);
        targetArea.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                mapBuilderScreen.setCurrentState(MapBuilderScreen.State.TARGETAREA);
                System.out.println("Current action: Create targtet area");
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
                System.out.println("Current action: Save map");
            }
        });
        clear = new ImageButton(textureRegionDrawable);
        clear.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                System.out.println("Current action: Clear map");
            }
        });
        exit = new ImageButton(textureRegionDrawable);
        exit.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y){
                System.out.println("Current action: Exit");
            }
        });

        table.add(building);
        table.add(sentryTower);
        table.add(hidingArea);
        table.add(targetArea);

        table.row();

        table.add(load).padBottom(10);
        table.add(save).padBottom(10);
        table.add(clear).padBottom(10);
        table.add(exit).padBottom(10);

        stage.addActor(table);
    }

    public void update(float dt){

    }

    public void dispose() {
        stage.dispose();
    }

}
