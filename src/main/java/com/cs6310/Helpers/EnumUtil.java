package com.cs6310.Helpers;


import com.cs6310.Models.Mower.Direction;

public class EnumUtil {
    public static <T extends Enum<?>> T lookup(Class<T> enumType, String name) {
        for (T enumn : enumType.getEnumConstants()) {
            if (enumn.name().equalsIgnoreCase(name)) {
                return enumn;
            }
        }
        return null;
    }

    public static int getXStepValue(Direction direction) {
        if (direction == Direction.North || direction == Direction.South)
            return 0;
        if (direction == Direction.NorthEast || direction == Direction.East || direction == Direction.SouthEast)
            return 1;
        else
            return -1;
    }

    public static int getYStepValue(Direction direction) {
        if (direction == Direction.East || direction == Direction.West)
            return 0;
        if (direction == Direction.NorthEast || direction == Direction.North || direction == Direction.NorthWest)
            return 1;
        else
            return -1;
    }

    public static Direction getDirectionByXandY(int x, int y) {
        if (x == 0) {
            if (y == 1)
                return Direction.North;
            if (y == -1)
                return Direction.South;
        } else if (x == 1) {
            if (y == 0)
                return Direction.East;
            if (y == 1)
                return Direction.NorthEast;
            if (y == -1)
                return Direction.SouthEast;
        } else {
            if (y == 0)
                return Direction.West;
            if (y == 1)
                return Direction.NorthWest;
        }
        return Direction.SouthWest;
    }

}
