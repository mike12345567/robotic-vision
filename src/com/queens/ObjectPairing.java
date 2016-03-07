package com.queens;

public class ObjectPairing {
    ColouredArea backArea = null;
    ColouredArea frontArea = null;
    boolean ready;

    public ObjectPairing() {
        ready = false;
    }

    public ObjectPairing(ColouredArea backArea, ColouredArea frontArea) {
        this.backArea = backArea;
        this.frontArea = frontArea;
        ready = true;
    }

    public float getRotation() {
        if (ready) {
            double x = backArea.getX() - frontArea.getX();
            double y = backArea.getY() - frontArea.getY();
            double length = Math.sqrt(x * x + y * y);
            if (length != 0) {
                x = x / length;
                y = y / length;
            }

             double degrees = 90.0d - Math.toDegrees( Math.atan2( y, x ) );

            if( degrees < 0.0d )
            {
                degrees += 360.0;
            }
            return (float) degrees;
        } else {
            return 0f;
        }
    }

    public void addBackArea(ColouredArea area) {
        this.backArea = area;
        if (this.frontArea != null) {
            ready = true;
        }
    }

    public void addFrontArea(ColouredArea area) {
        this.frontArea = area;
        if (this.backArea != null) {
            ready = true;
        }
    }

    public void beingRemoved(ColouredArea area) {
        if (this.frontArea != null && this.frontArea == area) {
            this.frontArea = null;
            ready = false;
        }
        if (this.backArea != null && this.backArea == area) {
            this.backArea = null;
            ready = false;
        }
    }
}
