package org.cytoscape.task;

import org.cytoscape.model.CyColumn;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Core Task API (core-task-api)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

/**
 * A task factory that creates one or more tasks that operate on the specified CyColumn.
 * @CyAPI.Spi.Interface
 * @CyAPI.InModule core-task-api
 */
public interface TableColumnTaskFactory {
	
	/** 
	 * Used to provision this factory with a {@link CyColumn} that will be used to create tasks.
	 * @param column a non-null CyColumn.
	 * @return A TaskIterator object containing one or more {@link org.cytoscape.work.Task} objects to execute.
	 */
	TaskIterator createTaskIterator(CyColumn column);

    /**
     * Returns true if this task factory is ready to produce a TaskIterator.
	 * @param column a non-null CyColumn.
     * @return true if this task factory is ready to produce a TaskIterator.
     */
	boolean isReady(CyColumn column);
	
	/**
     * 
     * @param column
     * @return
     */
    default boolean isOn(CyColumn column) {
    	return false;
    }
}
