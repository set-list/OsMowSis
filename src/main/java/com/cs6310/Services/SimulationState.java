package com.cs6310.Services;

import com.cs6310.Models.Lawn.Lawn;
import com.cs6310.Models.Mower.LawnMower;
import com.cs6310.Models.Mower.LawnMowerMove;

import java.util.ArrayList;

public class SimulationState {
    public Lawn lawn;
    public ArrayList<LawnMower> mowers;
    public LawnMowerMove lastAction;
    public int maxNumTurns;
    public int turnsTaken;
    public int originalNumOfGrass;
    public int currentMowerIndex;
    public boolean isSimulationComplete;
    public String completeCode;
}
