package org.quelea.data.db.model;

import java.io.Serializable;
import java.util.ArrayList;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import static org.hibernate.type.TypeFactory.serializable;
import org.quelea.data.mediaLoop.MediaFile;

/**
 * MediaLoop table mapping
 *
 * @author tomaszpio@gmail.com
 */
@Entity
@Table(name = "mediaLoops")
public class MediaLoop implements Serializable{

    private static final int STRING_LENGTH = DBConstants.STRING_LENGTH;
    private static final long serialVersionUID = -1860438479077639195L;
    private long id;
    private String title;
    private ArrayList<MediaFile> media;

    public MediaLoop() {
    }

    public MediaLoop(String title, ArrayList<MediaFile> media) {
        this.title = title;
        this.media = media;
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
     * @return the title
     */
    @Column(name = "title", nullable = false, length = STRING_LENGTH)
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the media file list
     *
     * @return the media files
     */
    public ArrayList<MediaFile> getMedia() {
        return media;
    }

    /**
     * Set the media file list
     *
     * @param media the media files
     */
    public void setMedia(ArrayList<MediaFile> media) {
        this.media = media;
    }

}
