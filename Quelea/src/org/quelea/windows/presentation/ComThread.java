/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.windows.presentation;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.services.utils.LoggerUtils;

/**
 *
 * @author Michael
 */
public class ComThread {
    
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final ExecutorService comService = Executors.newSingleThreadExecutor();
    
    public static <T> T runAndWait(Callable<T> callable) {
        Future<T> future = comService.submit(callable);
        try {
            return future.get();
        }
        catch(InterruptedException ex) {
            LOGGER.log(Level.WARNING, "Interrupted", ex);
            return null;
        }
        catch(ExecutionException ex) {
            LOGGER.log(Level.WARNING, "COM Error", ex);
            return null;
        }
    }
    
    public static void runAndWait(Runnable runnable) {
        Future<?> future=comService.submit(runnable);
        try {
            future.get();
        }
        catch(InterruptedException ex) {
            LOGGER.log(Level.WARNING, "Interrupted", ex);
        }
        catch(ExecutionException ex) {
            LOGGER.log(Level.WARNING, "COM Error", ex);
        }
    }
    
}
