package com.mygdx.mass.BoxObject;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Filter;
import com.mygdx.mass.Data.MASS;

import static com.mygdx.mass.BoxObject.BoxObject.ObjectType.WALL;

public class Wall extends BoxObject {

    public static final float THICKNESS = 4.0f;

    public Wall (MASS mass, Rectangle rectangle) {
        super(mass, rectangle);
        Filter filter = new Filter();
        filter.categoryBits = WALL_BIT;
        filter.maskBits = GUARD_BIT | INTRUDER_BIT;
        fixture.setFilterData(filter);
        objectType = WALL;
    }

}