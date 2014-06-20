package org.quelea.data.db.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.quelea.data.ThemeDTO;

/**
 * theme table mapping
 * <p>
 * @author tomaszpio@gmail.com
 */
@Entity
@Table(name = "theme")
public class Theme {

    private static final int STRING_LENGTH = DBConstants.STRING_LENGTH;
    //Main song theme
    private long id;
    private String name;
    private String fontname;
    private String fontcolour;
    private String translateFontname;
    private String translateFontcolour;
    private String backgroundcolour;
    private String backgroundgrad1;
    private String backgroundgrad2;
    private String backgroundvid;
    private String backgroundimage;
    private Boolean fontBold;
    private Boolean fontItalic;
    private Boolean translateFontBold;
    private Boolean translateFontItalic;
    private Double videoHue;
    private Integer textPosition;
    private Integer textAlignment;
    private TextShadow textShadow = new TextShadow();
    //Bible theme
    private String biblefontname;
    private String biblefontcolour;
    private String biblebackgroundcolour;
    //private String biblebackgroundgrad1;
    //private String biblebackgroundgrad2;
    private String biblebackgroundvid;
    private String biblebackgroundimage;
    private Boolean biblefontBold;
    private Boolean biblefontItalic;
    private Double biblevideoHue;
    private Integer bibletextPosition;
    private Integer bibletextAlignment;
    private TextShadow bibletextShadow = new TextShadow();

    public Theme() {
    }

    public Theme(String name, String fontname, String fontcolour, String translateFontname, String translateFontcolour,
            String backgroundcolour, String backgroundvid, String backgroundimage,
            TextShadow shadow, boolean isFontBold, boolean isFontitalic, boolean isTranslateFontBold, boolean isTranslateFontitalic, double videoHue, int textPosition, int textAlignment,
            String biblefontname, String biblefontcolour, String biblebackgroundcolour, String biblebackgroundvid, String biblebackgroundimage,
            TextShadow bibleshadow, boolean bibleisFontBold, boolean bibleisFontitalic, double biblevideoHue, int bibletextPosition, int bibletextAlignment) {
        
        this.name = name;
        this.fontname = fontname;
        this.fontcolour = fontcolour;
        this.translateFontname = translateFontname;
        this.translateFontcolour = translateFontcolour;
        this.backgroundcolour = backgroundcolour;
        this.backgroundvid = backgroundvid;
        this.backgroundimage = backgroundimage;
        this.textShadow = shadow;
        this.fontBold = isFontBold;
        this.fontItalic = isFontitalic;
        this.translateFontBold = isTranslateFontBold;
        this.translateFontItalic = isTranslateFontitalic;
        this.videoHue = videoHue;
        this.textPosition = textPosition;
        this.textAlignment = textAlignment;
        //Bible
        this.biblefontname = biblefontname;
        this.biblefontcolour = biblefontcolour;
        this.biblebackgroundcolour = biblebackgroundcolour;
        this.biblebackgroundvid = biblebackgroundvid;
        this.biblebackgroundimage = biblebackgroundimage;
        this.bibletextShadow = bibleshadow;
        this.biblefontBold = bibleisFontBold;
        this.biblefontItalic = bibleisFontitalic;
        this.biblevideoHue = biblevideoHue;
        this.bibletextPosition = bibletextPosition;
        this.bibletextAlignment = bibletextAlignment;
    }

    public Theme(Theme theme) {
        this.name = theme.name;
        this.fontname = theme.fontname;
        this.fontcolour = theme.fontcolour;
        this.translateFontname = theme.translateFontname;
        this.translateFontcolour = theme.translateFontcolour;
        this.backgroundcolour = theme.backgroundcolour;
        this.backgroundvid = theme.backgroundvid;
        this.backgroundimage = theme.backgroundimage;
        this.textShadow = theme.textShadow;
        this.fontBold = theme.fontBold;
        this.fontItalic = theme.fontItalic;
        this.translateFontBold = theme.translateFontBold;
        this.translateFontItalic = theme.translateFontItalic;
        this.videoHue = theme.videoHue;
        this.textPosition = theme.textPosition;
        this.textAlignment = theme.textAlignment;
        //Bible
        this.biblefontname = theme.biblefontname;
        this.biblefontcolour = theme.biblefontcolour;
        this.biblebackgroundcolour = theme.biblebackgroundcolour;
        this.biblebackgroundvid = theme.biblebackgroundvid;
        this.biblebackgroundimage = theme.biblebackgroundimage;
        this.bibletextShadow = theme.bibletextShadow;
        this.biblefontBold = theme.biblefontBold;
        this.biblefontItalic = theme.biblefontItalic;
        this.biblevideoHue = theme.biblevideoHue;
        this.bibletextPosition = theme.bibletextPosition;
        this.bibletextAlignment = theme.bibletextAlignment;
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
     * @return the name
     */
    @Column(name = "name", nullable = false, length = STRING_LENGTH)
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "videoHue")
    public Double getVideoHue() {
        if(videoHue == null) {
            return 0.0;
        }
        return videoHue;
    }

    public void setVideoHue(Double videoHue) {
        this.videoHue = videoHue;
    }
    
    @Column(name="textPosition")
    public Integer getTextPosition() {
        if(textPosition==null) {
            return -1;
        }
        return textPosition;
    }
    
    public void setTextPosition(Integer textPosition) {
        this.textPosition = textPosition;
    }
    
    @Column(name="textAlignment")
    public Integer getTextAlignment() {
        if(textAlignment==null) {
            return 0;
        }
        return textAlignment;
    }
    
    public void setTextAlignment(Integer textAlignment) {
        this.textAlignment = textAlignment;
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
     * @return the translatefontname
     */
    @Column(name = "translatefontname", length = STRING_LENGTH)
    public String getTranslateFontname() {
        return translateFontname;
    }

    /**
     * @param translateFontname the translatefontname to set
     */
    public void setTranslateFontname(String translateFontname) {
        this.translateFontname = translateFontname;
    }

    /**
     * @return the translatefontcolour
     */
    @Column(name = "translatefontcolour", length = STRING_LENGTH)
    public String getTranslateFontcolour() {
        return translateFontcolour;
    }

    /**
     * @param translateFontcolour the fontcolour to set
     */
    public void setTranslateFontcolour(String translateFontcolour) {
        this.translateFontcolour = translateFontcolour;
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

    /**
     * @return the isFontBold
     */
    @Column(name = "isfontbold")
    public Boolean isFontBold() {
        return fontBold == null ? false : fontBold;
    }

    /**
     * @param isFontBold the isFontBold to set
     */
    public void setFontBold(Boolean isFontBold) {
        this.fontBold = isFontBold;
    }

    /**
     * @return the isFontItalic
     */
    @Column(name = "isfontitalic")
    public Boolean isFontItalic() {
        return fontItalic == null ? false : fontItalic;
    }

    /**
     * @param isFontItalic the isFontItalic to set
     */
    public void setFontItalic(Boolean isFontItalic) {
        this.fontItalic = isFontItalic;
    }

    /**
     * @return the isFontBold
     */
    @Column(name = "istranslatefontbold")
    public Boolean isTranslateFontBold() {
        return translateFontBold == null ? false : translateFontBold;
    }

    /**
     * @param translateFontBold the isFontBold to set
     */
    public void setTranslateFontBold(Boolean translateFontBold) {
        this.translateFontBold = translateFontBold;
    }

    /**
     * @return the isFontItalic
     */
    @Column(name = "istranslatefontitalic")
    public Boolean isTranslateFontItalic() {
        return translateFontItalic == null ? false : translateFontItalic;
    }

    /**
     * @param translateFontItalic the isFontItalic to set
     */
    public void setTranslateFontItalic(Boolean translateFontItalic) {
        this.translateFontItalic = translateFontItalic;
    }
    
    //Bible
    @Column(name = "biblevideoHue")
    public Double getBibleVideoHue() {
        if(biblevideoHue == null) {
            return 0.0;
        }
        return biblevideoHue;
    }

    public void setBibleVideoHue(Double videoHue) {
        this.biblevideoHue = videoHue;
    }
    
    @Column(name="bibletextPosition")
    public Integer getBibleTextPosition() {
        if(bibletextPosition==null) {
            return -1;
        }
        return bibletextPosition;
    }
    
    public void setBibleTextPosition(Integer textPosition) {
        this.bibletextPosition = textPosition;
    }
    
    @Column(name="bibletextAlignment")
    public Integer getBibleTextAlignment() {
        if(bibletextAlignment==null) {
            return 0;
        }
        return bibletextAlignment;
    }
    
    public void setBibleTextAlignment(Integer textAlignment) {
        this.bibletextAlignment = textAlignment;
    }

    /**
     * @return the biblefontname
     */
    @Column(name = "biblefontname", length = STRING_LENGTH)
    public String getBibleFontname() {
        return biblefontname;
    }

    /**
     * @param fontname the biblefontname to set
     */
    public void setBibleFontname(String fontname) {
        this.biblefontname = fontname;
    }

    /**
     * @return the biblefontcolour
     */
    @Column(name = "biblefontcolour", length = STRING_LENGTH)
    public String getBibleFontcolour() {
        return biblefontcolour;
    }

    /**
     * @param fontcolour the biblefontcolour to set
     */
    public void setBibleFontcolour(String fontcolour) {
        this.biblefontcolour = fontcolour;
    }

    /**
     * @return the biblebackgroundcolour
     */
    @Column(name = "biblebackgroundcolor", length = STRING_LENGTH)
    public String getBibleBackgroundcolour() {
        return biblebackgroundcolour;
    }

    /**
     * @param backgroundcolour the biblebackgroundcolour to set
     */
    public void setBibleBackgroundcolour(String backgroundcolour) {
        this.biblebackgroundcolour = backgroundcolour;
    }

    /**
     * @return the biblebackgroundvid
     */
    @Column(name = "biblebackgroundvid", length = STRING_LENGTH)
    public String getBibleBackgroundvid() {
        return biblebackgroundvid;
    }

    /**
     * @param backgroundvid the biblebackgroundvid to set
     */
    public void setBibleBackgroundvid(String backgroundvid) {
        this.biblebackgroundvid = backgroundvid;
    }

    /**
     * @return the biblebackgroundimage
     */
    @Column(name = "biblebackgroundimage", length = STRING_LENGTH)
    public String getBibleBackgroundimage() {
        return biblebackgroundimage;
    }

    /**
     * @param backgroundimage the biblebackgroundimage to set
     */
    public void setBibleBackgroundimage(String backgroundimage) {
        this.biblebackgroundimage = backgroundimage;
    }

    /**
     * @return the Bible shadow
     */
    @ManyToOne(cascade = CascadeType.ALL)
    public TextShadow getBibleTextShadow() {
        return bibletextShadow;
    }

    /**
     * @param textShadow the Bible shadow to set
     */
    public void setBibleTextShadow(TextShadow textShadow) {
        this.bibletextShadow = textShadow;
    }

    /**
     * @return the isBibleFontBold
     */
    @Column(name = "isbiblefontbold")
    public Boolean isBibleFontBold() {
        return biblefontBold == null ? false : biblefontBold;
    }

    /**
     * @param isFontBold the isBibleFontBold to set
     */
    public void setBibleFontBold(Boolean isFontBold) {
        this.biblefontBold = isFontBold;
    }

    /**
     * @return the isBibleFontItalic
     */
    @Column(name = "isbiblefontitalic")
    public Boolean isBibleFontItalic() {
        return biblefontItalic == null ? false : biblefontItalic;
    }

    /**
     * @param isFontItalic the isBibleFontItalic to set
     */
    public void setBibleFontItalic(Boolean isFontItalic) {
        this.biblefontItalic = isFontItalic;
    }

    @Override
    public String toString() {
        return ThemeDTO.getDTO(this).asString();
    }
}
