package com.cs6310.Services;

import com.cs6310.Helpers.EnumUtil;
import com.cs6310.Models.Interfaces.ILawnSquare;
import com.cs6310.Models.Lawn.*;
import com.cs6310.Models.Mower.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class SimulationService {

    private Lawn lawn;
    private ArrayList<LawnMower> lawnMowers;
    private LawnMowerMove lastAction;
    private String trackScanResults;
    private String trackMoveCheck;
    private int originalNumberOfGrassSquares;
    private int numberOfTurnsTaken;
    private int activeMowers;
    private int collisionDelay;
    private int indEnergyCap;
    private int maxNumTurns;
    private int currentMowerIndex;
    private boolean isSimulationComplete;
    private String completeCode;

    private String filePath;

    public void readScenario(String fileName) {
        final String DELIMITER = ",";

        try {
            resetState();

            filePath = fileName;

            Resource resource = new ClassPathResource(String.format("test_cases/%s", fileName));

            Scanner takeCommand = new Scanner(resource.getFile());
            String[] tokens;
            int k, lawnWidth, lawnHeight;

            // read in the lawn information
            tokens = takeCommand.nextLine().split(DELIMITER);
            lawnWidth = Integer.parseInt(tokens[0]);
            tokens = takeCommand.nextLine().split(DELIMITER);
            lawnHeight = Integer.parseInt(tokens[0]);

            // read in the lawnmower starting information
            lawnMowers = new ArrayList<>();
            tokens = takeCommand.nextLine().split(DELIMITER);
            int numMowers = Integer.parseInt(tokens[0]);

            // read in collision delay
            tokens = takeCommand.nextLine().split(DELIMITER);
            collisionDelay = Integer.parseInt(tokens[0]);

            // read in the indiv. mower energy capacity
            tokens = takeCommand.nextLine().split(DELIMITER);
            indEnergyCap = Integer.parseInt(tokens[0]);

            // read in location and direction of each mower
            for (k = 0; k < numMowers; k++) {
                tokens = takeCommand.nextLine().split(DELIMITER);
                int mowerX = Integer.parseInt(tokens[0]);
                int mowerY = Integer.parseInt(tokens[1]);
                Direction mowerDirection = EnumUtil.lookup(Direction.class, tokens[2]);

                lawnMowers.add(new LawnMower(mowerX, mowerY, mowerDirection, lawnWidth, lawnHeight, indEnergyCap));
            }
            activeMowers = numMowers;
            // read in the crater information
            tokens = takeCommand.nextLine().split(DELIMITER);
            Point[] craters = new Point[Integer.parseInt(tokens[0])];
            for (k = 0; k < craters.length; k++) {
                tokens = takeCommand.nextLine().split(DELIMITER);
                craters[k] = new Point(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
            }

            // read in max number of sim turns
            tokens = takeCommand.nextLine().split(DELIMITER);
            maxNumTurns = Integer.parseInt(tokens[0]);

            lawn = new Lawn(lawnWidth, lawnHeight, lawnMowers, craters);

            originalNumberOfGrassSquares = lawnWidth * lawnHeight - craters.length;

            takeCommand.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println();
        }
    }

    public SimulationState nextStep(boolean fastFoward) {
        if (isSimulationComplete) {
            return getState();
        }

        if (lawn.isCut()) {
            isSimulationComplete = true;
            completeCode = "No-Grass";
        } else if(numberOfTurnsTaken >= maxNumTurns) {
            isSimulationComplete = true;
            completeCode = "No-Turns-Left";
        } else if(activeMowers == 0) {
            isSimulationComplete = true;
            completeCode = "Mowers-Done";
        }
        else if (fastFoward) {
            for (LawnMower mower: lawnMowers) {
                if(!mower.isDone && !mower.hasCrashed && mower.indEnergyRem > 0){
                    pollMowerForAction(mower);
                    validateMowerAction(mower);
                    displayActionAndResponses();
                    numberOfTurnsTaken++;
                }
            }
            return getState();
        } //else if (!lawnMowers.get(0).isDone() && !lawnMowers.get(0).hasCrashed()) 
         else if (activeMowers > 0){
            LawnMower current = lawnMowers.get(currentMowerIndex);
            if(!current.isDone && !current.hasCrashed && current.indEnergyRem > 0){
                pollMowerForAction(current);
                validateMowerAction(current);
                displayActionAndResponses();
            }
        }

        numberOfTurnsTaken++;
        currentMowerIndex++;

        if (currentMowerIndex >= lawnMowers.size())
            currentMowerIndex = 0;

        return getState();

    }

    public void stop() {
        readScenario(filePath);
    }

    public void printReport() {

        int grassSquaresLeft = 0;
        for(int i = 0; i < lawn.getWidth(); i++) {
            for(int j = 0; j < lawn.getHeight(); j++) {
                ILawnSquare square = lawn.getSquare(i, j);
                if (square instanceof Grass) {
                    grassSquaresLeft++;
                }

            }
        }
        System.out.println(String.format("%d,%d,%d,%d", lawn.getWidth() * lawn.getHeight(), originalNumberOfGrassSquares,
                originalNumberOfGrassSquares - grassSquaresLeft, maxNumTurns - 1));
    }

    public SimulationState getState() {
        SimulationState state = new SimulationState();
        state.lastAction = lastAction;
        state.lawn = lawn;
        state.originalNumOfGrass = originalNumberOfGrassSquares;
        state.mowers = lawnMowers;
        state.maxNumTurns = maxNumTurns;
        state.turnsTaken = numberOfTurnsTaken;
        state.currentMowerIndex = currentMowerIndex;
        state.isSimulationComplete = isSimulationComplete;
        state.completeCode = completeCode;

        return state;
    }

    private void resetState() {
        lawn = null;
        lawnMowers = new ArrayList<>();
        lastAction = null;
        trackScanResults = "";
        trackMoveCheck = "";
        originalNumberOfGrassSquares = 0;
        maxNumTurns = 0;
        collisionDelay = 0;
        indEnergyCap = 0;
        numberOfTurnsTaken = 0;
        currentMowerIndex = 0;
        isSimulationComplete = false;
        completeCode = "";
    }

    private void pollMowerForAction(LawnMower currentMower) {
        if (currentMower.indEnergyRem == 0) {
            currentMower.setDone(true);
            activeMowers--;
            return;
        }

        LinkedList<Point> otherMowerLocations = new LinkedList<>();
        for(int i = 0; i < lawnMowers.size(); i++) {
            if(lawnMowers.get(i).id.equalsIgnoreCase(currentMower.id))
                continue;
            otherMowerLocations.add(lawnMowers.get(i).currentPosition);
        }

        lastAction = currentMower.calculateNextMove(otherMowerLocations);
    }

    private void validateMowerAction(LawnMower mower) {
        int xOrientation, yOrientation;

        if (lastAction.action.equals(MowerAction.Scan)) {
            // in the case of a scan, return the information for the eight surrounding squares
            // always use a northbound orientation
            ILawnSquare[] adjacent = getAdjacentData(mower.getCurrentPosition());
            mower.updateKnownLawn(adjacent);
            trackScanResults = "";
            for (ILawnSquare square : adjacent) {
                trackScanResults += square.getClass().getSimpleName().toLowerCase() + ",";
            }
            trackScanResults = trackScanResults.substring(0, trackScanResults.length() - 1);
            mower.indEnergyRem--;

        } else if (lastAction.action.equals(MowerAction.Move)) {
            // in the case of a move, ensure that the move doesn't cross craters or fences
            xOrientation = EnumUtil.getXStepValue(lastAction.direction);
            yOrientation = EnumUtil.getYStepValue(lastAction.direction);

            // just for this demonstration, allow the mower to change direction
            // even if the move forward causes a crash
            mower.setCurrentDirection(lastAction.direction);

            int newSquareX = mower.getCurrentPosition().x + lastAction.distance * xOrientation;
            int newSquareY = mower.getCurrentPosition().y + lastAction.distance * yOrientation;

            if (lastAction.distance == 0){
                mower.indEnergyRem--;
            } else {
                mower.indEnergyRem -= 2;
            }

            if (newSquareX >= 0 & newSquareX < lawn.getWidth() & newSquareY >= 0 & newSquareY < lawn.getHeight()) {
                // update lawn status
                ILawnSquare newSquare = lawn.getSquare(newSquareX, newSquareY);
                //ILawnSquare currSquare = lawn.getSquare(mower.getCurrentPosition().x, mower.getCurrentPosition().y);
                //will add more func for keeping charging stations in place
                if(newSquare instanceof Grass || newSquare instanceof Empty){
                    lawn.updateLawnState(newSquareX, newSquareY, new Empty());
                    mower.setCurrentPosition(newSquareX, newSquareY);
                    trackMoveCheck = "ok";
                } else if (newSquare instanceof ChargingStation) {
                    mower.indEnergyRem = indEnergyCap;
                    mower.setCurrentPosition(newSquareX, newSquareY);
                    trackMoveCheck = "ok";
                } else if (newSquare instanceof Crater || newSquare instanceof MowerOccupied) {
                    mower.setHasCrashed(true);
                    activeMowers--;
                    trackMoveCheck = "crash";
                    lawn.updateLawnState(mower.getCurrentPosition().x, mower.getCurrentPosition().y, new Crash());
                    mower.setCurrentPosition(newSquareX + 1 * -xOrientation, newSquareY + 1 * -yOrientation);
                }
                
            } else {
                mower.setHasCrashed(true);
                activeMowers--;
                trackMoveCheck = "crash";
            }

        } else if (lastAction.action.equals(MowerAction.TurnOFF)) {
            mower.setDone(true);
            activeMowers--;
            trackMoveCheck = "ok";
        }

        if(mower.indEnergyRem == 0)
            activeMowers--;
    }

    private ILawnSquare[] getAdjacentData(Point position) {
        ILawnSquare[] adjacent = new ILawnSquare[8];
        int counter = 0;
        for (Direction direction: Direction.values()) {
            adjacent[counter] = lawn.getSquare(position.x + EnumUtil.getXStepValue(direction), position.y + + EnumUtil.getYStepValue(direction));
            counter++;
        }

        return adjacent;
    }

    public void displayActionAndResponses() {
        // display the mower's actions
        System.out.print(lastAction.action.toString().toLowerCase());
        if (lastAction.action.equals(MowerAction.Move)) {
            System.out.println("," + lastAction.distance + "," + lastAction.direction.toString().toLowerCase());
        } else {
            System.out.println();
        }

        // display the simulation checks and/or responses
        if (lastAction.action.equals(MowerAction.Move) || lastAction.action.equals(MowerAction.TurnOFF)) {
            System.out.println(trackMoveCheck);
        } else if (lastAction.action.equals(MowerAction.Scan)) {
            System.out.println(trackScanResults);
        } else {
            System.out.println("action not recognized");
        }
    }

    private void renderHorizontalBar(int size) {
        System.out.print(" ");
        for (int k = 0; k < size; k++) {
            System.out.print("-");
        }
        System.out.println();
    }

    private void renderLawn() {
        int i, j;
        int charWidth = 2 * lawn.getWidth() + 2;
        LawnMower mower = lawnMowers.get(0);

        // display the rows of the lawn from top to bottom
        for (j = lawn.getHeight() - 1; j >= 0; j--) {
            renderHorizontalBar(charWidth);

            // display the Y-direction identifier
            System.out.print(j);

            // display the contents of each square on this row
            for (i = 0; i < lawn.getWidth(); i++) {
                System.out.print("|");

                // the mower overrides all other contents
                if (i == mower.getCurrentPosition().x & j == mower.getCurrentPosition().y) {
                    System.out.print("M");
                } else {
                    ILawnSquare currentSquare = lawn.getSquare(i, j);
                    if (currentSquare instanceof Empty) {
                        System.out.print(" ");
                    } else if (currentSquare instanceof Grass) {
                        System.out.print("g");
                    } else if (currentSquare instanceof Crater) {
                        System.out.print("c");
                    }
                }
            }
            System.out.println("|");
        }
        renderHorizontalBar(charWidth);

        // display the column X-direction identifiers
        System.out.print(" ");
        for (i = 0; i < lawn.getWidth(); i++) {
            System.out.print(" " + i);
        }
        System.out.println();

        // display the mower's direction
        System.out.println("dir: " + mower.getCurrentDirection());
        System.out.println();
    }

}