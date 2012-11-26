package org.quelea.data.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * textshadow table  mapping
 * @author tomaszpio@gmail.com
 */
@Entity
@Table(name = "textShadow")
public class TextShadow {

    private long id;
    private String shadowColor;
    private Double offsetX;
    private Double offsetY;
    private Double radius;
    private Double width;

    public TextShadow(String shadowColor, Double offsetX, Double offsetY) {
        this.shadowColor = shadowColor;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.radius = 4.0;
        this.width = 4.0;
    }

    public TextShadow() {
    }

    /**
     * @return the id
     */
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the shadowColor
     */
    @Column(name = "shadowcolor", nullable = false)
    public String getShadowColor() {
        return shadowColor;
    }

    /**
     * @param shadowColor the shadowColor to set
     */
    public void setShadowColor(String shadowColor) {
        this.shadowColor = shadowColor;
    }

    /**
     * @return the offsetX
     */
    @Column(name = "offsetx", nullable = false)
    public Double getOffsetX() {
        return offsetX;
    }

    /**
     * @param offsetX the offsetX to set
     */
    public void setOffsetX(Double offsetX) {
        this.offsetX = offsetX;
    }

    /**
     * @return the offsetY
     */
    @Column(name = "offsety", nullable = false)
    public Double getOffsetY() {
        return offsetY;
    }

    /**
     * @param offsetY the offsetY to set
     */
    public void setOffsetY(Double offsetY) {
        this.offsetY = offsetY;
    }

    /**
     * @return the radius
     */
    @Column(name = "radius", nullable = false)
    public Double getRadius() {
        return radius;
    }

    /**
     * @param radius the radius to set
     */
    public void setRadius(Double radius) {
        this.radius = radius;
    }

    /**
     * @return the width
     */
    @Column(name = "width", nullable = false)
    public Double getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(Double width) {
        this.width = width;
    }
}
