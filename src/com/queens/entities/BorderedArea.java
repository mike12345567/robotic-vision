package com.queens.entities;

import com.queens.colours.ColourNames;
import com.queens.communications.Jsonifable;
import com.queens.communications.KeyValueObject;
import com.queens.utilities.Utilities;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class BorderedArea implements Jsonifable {
    ColourNames insideColour, borderColour;
    ColouredArea borderArea;
    ColouredArea insideArea;
    boolean isRoughSquare;

    public BorderedArea(boolean isRoughSquare, ColourNames borderColour, ColourNames insideColour) {
        this.isRoughSquare = isRoughSquare;
        this.insideColour = insideColour;
        this.borderColour = borderColour;
    }

    public void update(ArrayList<ColouredArea> currentAreas) {
        if (insideArea != null && borderArea != null) {
            this.insideArea.setInUse(false);
            this.borderArea.setInUse(false);
        }
        if (!currentAreas.contains(insideArea) || !currentAreas.contains(borderArea)) {
            borderArea = null;
            insideArea = null;
        }

        for (ColouredArea area : currentAreas) {
            if ((!isRoughSquare || area.isRoughSquare()) && area.isInUse()) {
                continue;
            }

            ColouredArea otherArea = containsOther(currentAreas, area);
            if (area.getColour() == this.borderColour && otherArea != null) {
                this.borderArea = area;
                this.insideArea = otherArea;
            }
        }

        if (borderArea != null || insideArea != null) {
            this.borderArea.setInUse(true);
            this.insideArea.setInUse(true);
        }
    }

    public ColouredArea getArea() {
        return borderArea;
    }

    public boolean isActive() {
        return this.insideArea != null && this.borderArea != null;
    }

    private ColouredArea containsOther(ArrayList<ColouredArea> currentAreas, ColouredArea toTest) {
        ColourNames lookingFor = toTest.getColour() == borderColour ? insideColour : borderColour;
        for (ColouredArea area : currentAreas) {
            if (area == toTest || Utilities.tooClose(area, toTest) || area.isInUse()) {
                continue;
            }

            if (area.getColour() == lookingFor &&
                    toTest.getBoundingBox().contains(area.getTopCorner()) &&
                    toTest.getBoundingBox().contains(area.getBottomCorner())) {
                return area;
            }
        }
        return null;
    }

    private boolean pointsClose(Point one, Point two, int threshold) {
        return (one.x - threshold < two.x && one.x + threshold > two.x &&
                one.y - threshold < two.y && one.y + threshold > two.y);
    }

    @Override
    public List<KeyValueObject> getKeyValuePairs() {


        return Utilities.getRectObject(borderArea.getPureX(), borderArea.getPureY(),
                borderArea.getWidth(), borderArea.getHeight());
    }
}
