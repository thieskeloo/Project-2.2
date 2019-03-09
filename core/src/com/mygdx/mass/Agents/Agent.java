package com.mygdx.mass.Agents;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.mass.Map;

public abstract class Agent{

    World world;
    Body body;

    Vector2 position;
    Map map;

    String type;
    int state;
    int count;
    int velocity;

    public String toString() {
        return "Type = "+this.type+" count of Agents = "+this.count;
    }

}
