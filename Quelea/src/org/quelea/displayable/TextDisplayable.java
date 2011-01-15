package org.quelea.displayable;

/**
 * A displayable object that displays text such as liturgy, songs or bible passages.
 * @author Michael
 */
public interface TextDisplayable extends Displayable {

    /**
     * Get the text sections in this displayable.
     * @return the text sections.
     */
    TextSection[] getSections();

}
