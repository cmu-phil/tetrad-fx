package io.github.cmuphil.tetrad.fx.gui;

/**
 * Interface to be implemented by shapes that have a center.
 *
 * @author josephramsey
 */
public interface CenteredShape {
    void setCenterX(double centerX);
    void setCenterY(double centerY);
    double getCenterX();
    double getCenterY();
}
