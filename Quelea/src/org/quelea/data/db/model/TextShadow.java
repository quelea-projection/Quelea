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
    private static final int STRING_LENGTH = DBConstants.STRING_LENGTH;
    private long id;
    private String shadowColor;
    private Double offsetX;
    private Double offsetY;
    private Double radius;
    private Double width;
    private Double spread;
    private Boolean use;

    public TextShadow(String shadowColor, Double offsetX, Double offsetY, Double radius, Double spread, Boolean use) {
        this.shadowColor = shadowColor;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.radius = radius;
        this.width = 2.0;
        this.spread = spread;
        this.use = use;
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
    @Column(name = "shadowcolor", nullable = false, length = STRING_LENGTH)
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
     * @return whether to use a shadow
     */
    @Column(name = "use")
    public Boolean getUse() {
        if(use==null) {
            return true;
        }
        return use;
    }

    /**
     * @param use whether to use this shadow
     */
    public void setUse(Boolean use) {
        this.use = use;
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
    @Column(name = "radius")
    public Double getRadius() {
        if (radius == null) {
            return 2.0;
        }
        return radius;
    }

    /**
     * @param radius the radius to set
     */
    public void setRadius(Double radius) {
        this.radius = radius;
    }

    /**
     * @return the spread
     */
    @Column(name = "spread")
    public Double getSpread() {
        if (spread == null) {
            return 0.0;
        }
        return spread;
    }

    /**
     * @param spread the spread to set
     */
    public void setSpread(Double spread) {
        this.spread = spread;
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
