package com.cs6310.Models.Mower;

public class LawnMowerMove {
    public int distance;
    public Direction direction;
    public MowerAction action;

    public LawnMowerMove(MowerAction action, int distance, Direction direction) {
        this.action = action;
        this.distance = distance;
        this.direction = direction;
    }
}
