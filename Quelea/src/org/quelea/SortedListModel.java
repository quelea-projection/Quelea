package org.quelea;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractListModel;

/**
 * A list model that sorts its components.
 * @author Michael
 */
public class SortedListModel extends AbstractListModel {

    private final SortedSet<Object> model;

    /**
     * Create a new sorted list model.
     */
    public SortedListModel() {
        model = new TreeSet<Object>();
    }

    /**
     * @inheritDoc
     */
    public int getSize() {
        return model.size();
    }

    /**
     * @inheritDoc
     */
    public Object getElementAt(int index) {
        return model.toArray()[index];
    }

    /**
     * @inheritDoc
     */
    public void add(Object element) {
        if(model.add(element)) {
            fireContentsChanged(this, 0, getSize());
        }
    }

    /**
     * @inheritDoc
     */
    public void addAll(Object elements[]) {
        Collection<Object> c = Arrays.asList(elements);
        model.addAll(c);
        fireContentsChanged(this, 0, getSize());
    }

    /**
     * @inheritDoc
     */
    public void clear() {
        model.clear();
        fireContentsChanged(this, 0, getSize());
    }

    /**
     * @inheritDoc
     */
    public boolean contains(Object element) {
        return model.contains(element);
    }

    /**
     * @inheritDoc
     */
    public Object firstElement() {
        return model.first();
    }

    /**
     * @inheritDoc
     */
    public Iterator iterator() {
        return model.iterator();
    }

    /**
     * @inheritDoc
     */
    public Object lastElement() {
        return model.last();
    }

    /**
     * @inheritDoc
     */
    public boolean removeElement(Object element) {
        boolean removed = model.remove(element);
        if(removed) {
            fireContentsChanged(this, 0, getSize());
        }
        return removed;
    }


}
