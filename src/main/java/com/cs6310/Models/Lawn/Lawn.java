package com.cs6310.Models.Lawn;

import com.cs6310.Models.Mower.LawnMower;
import com.cs6310.Models.Interfaces.ILawnSquare;

import java.awt.*;
import java.util.ArrayList;

public class Lawn {
    public ILawnSquare[][] lawnState;

    public Lawn(int width, int height, ArrayList<LawnMower> lawnMowers, Point[] craters) {
        lawnState = new ILawnSquare[width][height];

        fillWithGrass();
        placeLawnMowers(lawnMowers);
        placeCraters(craters);
    }

    public Lawn(int width, int height) {
        lawnState = new ILawnSquare[width][height];
    }

    public void updateLawnState(int x, int y, ILawnSquare squareState) {
        if (x >= 0 && x < lawnState.length && y >= 0 && y < lawnState[0].length)
            lawnState[x][y] = squareState;
    }

    public ILawnSquare getSquare(int x, int y) {
        if (x < 0 || x >= lawnState.length || y < 0 || y >= lawnState[0].length)
            return new Fence();
        return lawnState[x][y];
    }

    public int getHeight() {
        if (lawnState == null || lawnState.length == 0)
            return 0;

        return lawnState[0].length;
    }

    public int getWidth() {
        if (lawnState == null)
            return 0;

        return lawnState.length;
    }

    public boolean isCut() {
        for (int i = 0; i < lawnState.length; i++) {
            for (int j = 0; j < lawnState[0].length; j++) {
                // If at least one grass patch is left, it is not done
                if(lawnState[i][j] instanceof Grass) {
                    return false;
                }
            }
        }
        return true;
    }

    public void clearMowerOccupied() {
        for (int i = 0; i < lawnState.length; i++) {
            for (int j = 0; j < lawnState[0].length; j++) {
                // If at least one grass patch is left, it is not done
                if(lawnState[i][j] instanceof MowerOccupied) {
                    lawnState[i][j] = new Empty();
                }
            }
        }
    }

    private void fillWithGrass() {
        for (int i = 0; i < lawnState.length; i++) {
            for (int j = 0; j < lawnState[0].length; j++) {
                lawnState[i][j] = new Grass();
            }
        }
    }

    private void placeLawnMowers(ArrayList<LawnMower> lawnMowers) {
        if (lawnMowers == null || lawnMowers.size() == 0)
            return;

        // mow the grass at the initial location
        for (LawnMower mower : lawnMowers) {
            lawnState[mower.getCurrentPosition().x][mower.getCurrentPosition().y] = new ChargingStation();
        }
    }

    private void placeCraters(Point[] craters) {
        if (craters == null || craters.length == 0)
            return;

        // place a crater at the given location
        for (Point crater : craters) {
            lawnState[crater.x][crater.y] = new Crater();
        }
    }
}
