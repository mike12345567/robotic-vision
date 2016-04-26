package com.queens.entities;

import com.queens.colours.ColourNames;
import com.queens.communications.IJsonifable;
import com.queens.communications.KeyValueObject;
import com.queens.utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

public class BorderedArea implements IJsonifable {
    ColourNames insideColour, borderColour;
    ColouredArea borderArea;
    ColouredArea insideArea;

    public BorderedArea(ColourNames borderColour, ColourNames insideColour) {
        this.insideColour = insideColour;
        this.borderColour = borderColour;
    }

    public void update(ArrayList<ColouredArea> currentAreas) {
        if (insideArea != null && borderArea != null) {
            this.insideArea.setInUse(false);
            this.borderArea.setInUse(false);
        }
        if (!currentAreas.contains(insideArea) || !currentAreas.contains(borderArea) ||
            !Utilities.colouredAreaContainsOther(borderArea, insideArea)) {
            borderArea = null;
            insideArea = null;
        }

        for (ColouredArea area : currentAreas) {
            if (area.isInUse() || area.getColour() != borderColour) {
                continue;
            }

            ColouredArea otherArea = containsOther(currentAreas, area);
            if (otherArea != null) {
                this.borderArea = area;
                this.insideArea = otherArea;
                break;
            }
        }

        if (borderArea != null && insideArea != null) {
            this.borderArea.setInUse(true);
            this.insideArea.setInUse(true);
        }
    }

    public ColouredArea getArea() {
        return borderArea;
    }

    public int getX() {
        return borderArea.getX();
    }

    public int getY() {
        return borderArea.getY();
    }

    public boolean isActive() {
        return this.insideArea != null && this.borderArea != null;
    }

    private ColouredArea containsOther(ArrayList<ColouredArea> currentAreas, ColouredArea toTest) {
        ColourNames lookingFor = toTest.getColour() == borderColour ? insideColour : borderColour;
        for (ColouredArea area : currentAreas) {
            if (area == toTest || Utilities.tooClose(area, toTest) || area.isInUse() ||
                area.getBoundingBox().size().area() > toTest.getBoundingBox().size().area()) {
                continue;
            }

            if (area.getColour() == lookingFor && Utilities.colouredAreaContainsOther(toTest, area)) {
                return area;
            }
        }
        return null;
    }

    @Override
    public List<KeyValueObject> getKeyValuePairs() {
        return Utilities.getRectObject(borderArea.getPureX(), borderArea.getPureY(),
                borderArea.getWidth(), borderArea.getHeight());
    }
}
