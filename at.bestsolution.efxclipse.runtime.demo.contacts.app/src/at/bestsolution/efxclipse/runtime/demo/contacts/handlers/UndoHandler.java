/*******************************************************************************
 * Copyright (c) 2009 Siemens AG and others.
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Kai Tödter - initial implementation
 ******************************************************************************/

package at.bestsolution.efxclipse.runtime.demo.contacts.handlers;

import at.bestsolution.efxclipse.runtime.demo.contacts.model.ContactsManager;

import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.emf.common.command.CommandStack;

@SuppressWarnings("restriction")
public class UndoHandler {
	
	@Inject
	ContactsManager contactsManager;
	
	@CanExecute
	public boolean canUndo() {
		CommandStack commandStack = contactsManager.getEditingDomain().getCommandStack();
		return commandStack.canUndo();
	}

	@Execute
	public void undo() {
		CommandStack commandStack = contactsManager.getEditingDomain().getCommandStack();
		if (commandStack.canUndo())
			commandStack.undo();
	}
	
}
