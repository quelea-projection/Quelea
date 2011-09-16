/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea;

import javax.swing.*;
import java.util.*;

/**
 * A list model that sorts its components.
 * @author Michael
 */
public class SortedListModel<E> extends AbstractListModel<E> {

    private final SortedSet<E> model;

    /**
     * Create a new sorted list model.
     */
    public SortedListModel() {
        model = new TreeSet<>();
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
    @SuppressWarnings("unchecked")
    public E getElementAt(int index) {
        return (E)model.toArray()[index];
    }

    /**
     * @inheritDoc
     */
    public void add(E element) {
        if(model.add(element)) {
            fireContentsChanged(this, 0, getSize());
        }
    }

    /**
     * @inheritDoc
     */
    public void addAll(E[] elements) {
        Collection<E> c = Arrays.asList(elements);
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
    public boolean contains(E element) {
        return model.contains(element);
    }

    /**
     * @inheritDoc
     */
    public E firstElement() {
        return model.first();
    }

    /**
     * @inheritDoc
     */
    public Iterator<E> iterator() {
        return model.iterator();
    }

    /**
     * @inheritDoc
     */
    public E lastElement() {
        return model.last();
    }

    /**
     * @inheritDoc
     */
    public boolean removeElement(E element) {
        boolean removed = model.remove(element);
        if(removed) {
            fireContentsChanged(this, 0, getSize());
        }
        return removed;
    }


}
