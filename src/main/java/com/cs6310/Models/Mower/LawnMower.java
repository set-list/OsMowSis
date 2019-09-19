package com.cs6310.Models.Mower;

import com.cs6310.Helpers.EnumUtil;
import com.cs6310.Models.Interfaces.ILawnSquare;
import com.cs6310.Models.Lawn.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.UUID;

public class LawnMower {
    public String id;
    public Point currentPosition;
    public Direction currentDirection;
    public boolean hasCrashed;
    public boolean isDone;
    public int indEnergyRem;
    private Lawn knownLawn;
    private boolean isPredictedTurned = false;


    public LawnMower(int x, int y, Direction direction, int lawnWidth, int lawnHeight, int indEnergyCap) {
        currentPosition = new Point(x, y);
        currentDirection = direction;
        indEnergyRem = indEnergyCap;
        knownLawn = new Lawn(lawnWidth, lawnHeight);

        id = UUID.randomUUID().toString();

        //Place this mower on the lawn
        knownLawn.updateLawnState(x, y, new Empty());
    }

    public LawnMowerMove calculateNextMove(LinkedList<Point> otherMowerLocations) {
        if (isPredictedTurned) {
            isPredictedTurned = false;
            return new LawnMowerMove(MowerAction.Move, 1, currentDirection);
        }

        knownLawn.clearMowerOccupied();
        for (Point pt : otherMowerLocations) {
            knownLawn.updateLawnState(pt.x, pt.y, new MowerOccupied());
        }

        // Keep moving in the same direction
        ILawnSquare square = knownLawn.getSquare(currentPosition.x + EnumUtil.getXStepValue(currentDirection), currentPosition.y + EnumUtil.getYStepValue(currentDirection));
        if (square == null)
            return new LawnMowerMove(MowerAction.Scan, 0, currentDirection);
        if (square instanceof Grass) {
            return new LawnMowerMove(MowerAction.Move, 1, currentDirection);
        }

        // If cannot move any further in that direction, turn.
        for (Direction direction: Direction.values()) {
            square = knownLawn.getSquare(currentPosition.x + EnumUtil.getXStepValue(direction), currentPosition.y + EnumUtil.getYStepValue(direction));
            if (square == null)
                return new LawnMowerMove(MowerAction.Scan, 0, direction);
            if (square instanceof Grass) {
                return new LawnMowerMove(MowerAction.Move, 0, direction);
            }
        }

        // Use known lawn to set course for uncut squares
        LawnMowerMove guidedMove = findNextMoveByKnownLawn();
        if(guidedMove != null)
            return guidedMove;

        return new LawnMowerMove(MowerAction.TurnOFF, 0, currentDirection);
    }

    public void updateKnownLawn(ILawnSquare[] adjacent) {
        int counter = 0;
        for (Direction direction: Direction.values()) {
            Point pt = new Point(currentPosition.x + EnumUtil.getXStepValue(direction), currentPosition.y + EnumUtil.getYStepValue(direction));
            knownLawn.updateLawnState(pt.x, pt.y, adjacent[counter]);
            counter++;
        }
    }

    public Point getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int x, int y) {
        currentPosition.x = x;
        currentPosition.y = y;
        knownLawn.updateLawnState(x, y, new MowerOccupied());
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public void setCurrentDirection(Direction newDirection) {
        currentDirection = newDirection;
    }

    public boolean hasCrashed() {
        return hasCrashed;
    }

    public void setHasCrashed(boolean hasCrashed) {
        this.hasCrashed = hasCrashed;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    private LawnMowerMove findNextMoveByKnownLawn() {
        for(int i = 0; i < knownLawn.getWidth(); i++) {
            for(int j = 0; j < knownLawn.getHeight(); j++) {
                ILawnSquare square = knownLawn.getSquare(i, j);
                if (square instanceof Grass) {
                    isPredictedTurned = true;
                    int xStep = Integer.compare(i, currentPosition.x);
                    int yStep = Integer.compare(i, currentPosition.y);
                    Point projectedPosition = new Point(currentPosition.x + xStep, currentPosition.y + yStep);
                    Direction direction = EnumUtil.getDirectionByXandY(xStep, yStep);

                    if (isSquareAccessible(projectedPosition)) {
                        return new LawnMowerMove(MowerAction.Move, 0, direction);
                    }

                }
            }
        }

        for (Direction dir: Direction.values()) {
            Point projectedPosition = new Point(currentPosition.x + EnumUtil.getXStepValue(dir), currentPosition.y + EnumUtil.getYStepValue(dir));
            if (isSquareAccessible(projectedPosition)) {
                return new LawnMowerMove(MowerAction.Move, 0, dir);
            } else {
                return new LawnMowerMove(MowerAction.TurnOFF, 0, currentDirection);
            }
        }

        return null;
    }

    private boolean isSquareAccessible(Point pt) {
        ILawnSquare square = knownLawn.getSquare(pt.x, pt.y);
        return square != null && square.isAccessible();
    }
}
