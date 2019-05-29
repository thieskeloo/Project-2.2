package com.mygdx.mass.MapToGraph;

import com.badlogic.gdx.math.Intersector;
import com.mygdx.mass.Agents.Agent;
import com.mygdx.mass.BoxObject.BoxObject;
import com.mygdx.mass.BoxObject.Building;
import com.mygdx.mass.BoxObject.SentryTower;
import com.mygdx.mass.BoxObject.Wall;
import com.mygdx.mass.Data.MASS;


import java.util.ArrayList;

public class Graph {
    private ArrayList<Vertex> vertices;
    private ArrayList<Edge> edges;
    private Vertex start;
    private Vertex destination;
    private ArrayList<BoxObject> exploredBuilding;
    private Agent agent;
    public Graph(ArrayList<Vertex> vertices, ArrayList<Edge> edges, Agent agent){
        this.vertices = vertices;
        this.edges = edges;
        this.agent = agent;

    }
    public boolean adjacent(Vertex vertex1, Vertex vertex2){
        for(Edge e: edges){
            if(vertex1.getCoordinates()== e.getVertex1().getCoordinates() && vertex2.getCoordinates() == e.getVertex2().getCoordinates()) return true;
            if(vertex1.getCoordinates()== e.getVertex2().getCoordinates() && vertex2.getCoordinates() == e.getVertex1().getCoordinates()) return true;
        }
        return false;
    }
    public void addVertex(Vertex newVertex){
        vertices.add(newVertex);
    }
    public void addEdge(Edge edge){

        edges.add(edge);

    }

    public void connectVerticesList(ArrayList<Vertex> vertices){
        boolean proceed = false;
        edges.clear();
        BoxObject tmp = null;
        ArrayList<BoxObject> newBoxes = new ArrayList<BoxObject>();
        for(Vertex v1: vertices){
            for(Vertex v2: vertices){
                if(v1!=v2) {
                    tmp = connectVertices(v1,v2);
                    if (tmp==null) {
                        addEdge(new Edge(v1, v2));
                    }else{
                        if(!exploredBuilding.contains(tmp)){
                            newBoxes.add(tmp);
                            exploredBuilding.add(tmp);
                            proceed = true;
                        }
                    }


                }
            }
        }
        while (proceed){

            ArrayList<Vertex> newVertices = new ArrayList<Vertex>();
            for(BoxObject newBox: newBoxes){
              if (newBox != null) newVertices.addAll(add4Corners(newBox));
            }
            newBoxes.clear();

            proceed = false;
            for(Vertex newV: newVertices){
                for(Vertex v: vertices){
                    if(newV != v){
                        tmp = connectVertices(newV, v);
                        if (tmp==null){
                            addEdge(new Edge(newV, v));
                        }else{
                            if(!exploredBuilding.contains(tmp)){
                                newBoxes.add(tmp);
                                exploredBuilding.add(tmp);
                                proceed = true;
                            }

                        }
                    }
                }
            }

        }

    }
    private BoxObject connectVertices(Vertex v1, Vertex v2){
        for(Building building: agent.getIndividualMap().getBuildings()){
            if(Intersector.intersectSegmentRectangle(v1.getCoordinates(),v2.getCoordinates(), building.getRectangle())){
                    return building;
            }
        }
        for(SentryTower sentryTower: agent.getIndividualMap().getSentryTowers()){
            if(Intersector.intersectSegmentRectangle(v1.getCoordinates(),v2.getCoordinates(),sentryTower.getRectangle())){
                    return sentryTower;
            }
        }
        for(Wall wall: agent.getIndividualMap().getWalls()){
            if(Intersector.intersectSegmentRectangle(v1.getCoordinates(),v2.getCoordinates(),wall.getRectangle())){
                return wall;
            }
        }
        return null;
    }
    public ArrayList<Vertex> getPathVertices(Vertex start, Vertex destination) {
//        ArrayList<Vertex> vertices = new ArrayList<Vertex>();
        vertices.clear();
        this.start = start;
        this.destination = destination;
        this.exploredBuilding = new ArrayList<BoxObject>();
        vertices.add(start);
        vertices.add(destination);

        for (Building building : agent.getIndividualMap().getBuildings()) {
            if (Intersector.intersectSegmentRectangle(start.getCoordinates(), destination.getCoordinates(),building.getRectangle())) {
                exploredBuilding.add(building);
                add4Corners(building);
            }
        }

        for (SentryTower sentryTower : agent.getIndividualMap().getSentryTowers()) {
            if (Intersector.intersectSegmentRectangle(start.getCoordinates(), destination.getCoordinates(),sentryTower.getRectangle())) {
                exploredBuilding.add(sentryTower);
                add4Corners(sentryTower);
            }
        }
        for (Wall wall: agent.getIndividualMap().getWalls()){
            if (Intersector.intersectSegmentRectangle(start.getCoordinates(), destination.getCoordinates(), wall.getRectangle())) {
                exploredBuilding.add(wall);
                add4Corners(wall);
            }
        }

        return vertices;
    }

    private <T extends BoxObject> ArrayList<Vertex> add4Corners(T obj){
        ArrayList<Vertex> corners = new ArrayList<Vertex>();

            corners.add(new Vertex(obj.getRectangle().x-((float)0.5),obj.getRectangle().y-((float)0.5)));
            corners.add(new Vertex(obj.getRectangle().x-((float)0.5),obj.getRectangle().y+obj.getRectangle().height+((float)0.5)));
            corners.add(new Vertex(obj.getRectangle().x+obj.getRectangle().width+((float)0.5), obj.getRectangle().y+obj.getRectangle().height+((float)0.5)));
            corners.add(new Vertex(obj.getRectangle().x+obj.getRectangle().width+((float)0.5), obj.getRectangle().y-((float)0.5)));

            vertices.addAll(corners);

        return corners;
    }

    public void connectTSP(Vertex start){
        for(Vertex v: vertices){
            edges.add(new Edge(start,v));
        }


    }
    public ArrayList<Vertex> getVertices(){
        return vertices;
    }
    public ArrayList<Edge> getEdges(){
        return edges;
    }

    public Vertex getStart() {
        return start;
    }
    public Vertex getDestination() {
        return destination;
    }
}
