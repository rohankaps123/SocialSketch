package com.example.kiwitech.socialsketch.DataTypes;

import java.io.Serializable;

/**
 * Represents a Pair
 * @author Rohan Kapoor
 * @since 1.0
 */
public class Pair<X,Y> implements Serializable{
    private X x;
    private Y y;
    public Pair(X x, Y y){
        this.x = x;
        this.y = y;
    }
    public X getX(){ return x; }
    public Y getY(){ return y; }
    public void setX(X x){ this.x = x; }
    public void setY(Y y){ this.y = y; }
}
