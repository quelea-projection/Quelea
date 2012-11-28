package org.quelea.data.db.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
/**
  * theme table  mapping
 * @author tomaszpio@gmail.com
 */
@Entity
@Table(name = "theme")
public class Theme {

    private static final int STRING_LENGTH = 255;
    private long id;
    private String name;
    private String fontname;
    private String fontcolour;
    private String backgroundcolour = "";
    private String backgroundvid = "";
    private String backgroundimage = "";
    private TextShadow textShadow = new TextShadow();

    public Theme() {
    }

    public Theme(String name, String fontname, String fontcolour,
            String backgroundcolour, String backgroundvid, String backgroundimage, TextShadow shadow) {
        this.name = name;
        this.fontname = fontname;
        this.fontcolour = fontcolour;
        this.backgroundcolour = backgroundcolour;
        this.backgroundvid = backgroundvid;
        this.backgroundimage = backgroundimage;
        this.textShadow = shadow;
    }

    public Theme(Theme theme) {
        this.name = theme.name;
        this.fontname = theme.fontname;
        this.fontcolour = theme.fontcolour;
        this.backgroundcolour = theme.backgroundcolour;
        this.backgroundvid = theme.backgroundvid;
        this.backgroundimage = theme.backgroundimage;
        this.textShadow = theme.textShadow;
    }

    //private TextShadow textShadow;
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
     * @return the name
     */
    @Column(name = "name", nullable = false)
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the fontname
     */
    @Column(name = "fontname", length = STRING_LENGTH)
    public String getFontname() {
        return fontname;
    }

    /**
     * @param fontname the fontname to set
     */
    public void setFontname(String fontname) {
        this.fontname = fontname;
    }

    /**
     * @return the fontcolour
     */
    @Column(name = "fontcolour", length = STRING_LENGTH)
    public String getFontcolour() {
        return fontcolour;
    }

    /**
     * @param fontcolour the fontcolour to set
     */
    public void setFontcolour(String fontcolour) {
        this.fontcolour = fontcolour;
    }

    /**
     * @return the backgroundcolour
     */
    @Column(name = "backgroundcolor", length = STRING_LENGTH)
    public String getBackgroundcolour() {
        return backgroundcolour;
    }

    /**
     * @param backgroundcolour the backgroundcolour to set
     */
    public void setBackgroundcolour(String backgroundcolour) {
        this.backgroundcolour = backgroundcolour;
    }

    /**
     * @return the backgroundvid
     */
    @Column(name = "backgroundvid", length = STRING_LENGTH)
    public String getBackgroundvid() {
        return backgroundvid;
    }

    /**
     * @param backgroundvid the backgroundvid to set
     */
    public void setBackgroundvid(String backgroundvid) {
        this.backgroundvid = backgroundvid;
    }

    /**
     * @return the backgroundimage
     */
    @Column(name = "backgroundimage", length = STRING_LENGTH)
    public String getBackgroundimage() {
        return backgroundimage;
    }

    /**
     * @param backgroundimage the backgroundimage to set
     */
    public void setBackgroundimage(String backgroundimage) {
        this.backgroundimage = backgroundimage;
    }

    /**
     * @return the shadow
     */
    @ManyToOne(cascade = CascadeType.ALL) 
    public TextShadow getTextShadow() {
        return textShadow;
    }

    /**
     * @param textShadow the shadow to set
     */
    public void setTextShadow(TextShadow textShadow) {
        this.textShadow = textShadow;
    }
}
