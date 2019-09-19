package com.cs6310.Models.Lawn;

import com.cs6310.Models.Interfaces.ILawnSquare;

public abstract class NonAccessibleSquare implements ILawnSquare {

    @Override
    public boolean isAccessible() {
        return false;
    }
}
