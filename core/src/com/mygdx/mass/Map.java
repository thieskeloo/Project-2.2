package com.mygdx.mass;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.mass.Agents.Agent;
import com.mygdx.mass.Agents.Intruder;
import com.mygdx.mass.Agents.Surveillance;
import com.mygdx.mass.BoxObject.*;

import java.util.ArrayList;

public class Map {

    public static final float WIDTH = 200;
    public static final float HEIGHT = 200;

    private World world;

    private ArrayList<BoxObject> mapObjects;

    private ArrayList<Agent> agents;
    private ArrayList<Surveillance> surveillances;
    private ArrayList<Intruder> intruders;

    public Map(World world) {
        this.world = world;

        mapObjects = new ArrayList<BoxObject>();
        agents = new ArrayList<Agent>();
        surveillances = new ArrayList<Surveillance>();
        intruders = new ArrayList<Intruder>();
    }

    public void addBuilding(Rectangle rectangle) {
        Building building = new Building(world, rectangle);
        mapObjects.add(building);
    }

    public void addSentryTower(Rectangle rectangle) {
        SentryTower sentryTower = new SentryTower(world, rectangle);
        mapObjects.add(sentryTower);
    }

    public void addHidingArea(Rectangle rectangle) {
        HidingArea hidingArea = new HidingArea(world, rectangle);
        mapObjects.add(hidingArea);
    }

    public void addTargetArea(Rectangle rectangle) {
        TargetArea targetArea = new TargetArea(world, rectangle);
        mapObjects.add(targetArea);
    }

    public Surveillance addSurveillance(float x, float y) {
        Surveillance surveillance = new Surveillance();
        surveillances.add(surveillance);
        return surveillance;
    }

    public Intruder addIntruder(float x, float y) {
        Intruder intruder = new Intruder();
        intruders.add(intruder);
        return intruder;
    }

    public ArrayList<BoxObject> getMapObjects() {
        return mapObjects;
    }

    public ArrayList<Agent> getAgents() {
        return agents;
    }

    public ArrayList<Surveillance> getSurveillances() {
        return surveillances;
    }

    public ArrayList<Intruder> getIntruders() {
        return intruders;
    }

}
