/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.PatternFilter;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MKeyBinding;
import org.eclipse.e4.ui.model.application.MModelComponent;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class KeyBindingCommandSelectionDialog extends TitleAreaDialog {
	private MKeyBinding binding;
	private IModelResource resource;
	private TableViewer viewer;

	public KeyBindingCommandSelectionDialog(Shell parentShell, MKeyBinding binding, IModelResource resource) {
		super(parentShell);
		this.binding = binding;
		this.resource = resource;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		getShell().setText("Keybinding Command");
		setTitle("Keybinding-Command");
		setMessage("Select the keybinding");

		Composite container = new Composite(composite, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2, false));

		Label l = new Label(container, SWT.NONE);
		l.setText("Command-Id");

		Text searchText = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		searchText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		l = new Label(container, SWT.NONE);
		viewer = new TableViewer(container);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new LabelProviderImpl());
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		if( resource.getRoot().get(0) instanceof MApplication ) {
			MApplication app = (MApplication) resource.getRoot().get(0);
			viewer.setInput(app.getCommands());
		} else {
			MModelComponent comp = (MModelComponent)resource.getRoot().get(0);
			viewer.setInput(comp.getCommands());	
		}
		

		final PatternFilter filter = new PatternFilter() {
			@Override
			protected boolean isParentMatch(Viewer viewer, Object element) {
				return viewer instanceof AbstractTreeViewer && super.isParentMatch(viewer, element);
			}
		};
		viewer.addFilter(filter);

		searchText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				filter.setPattern(((Text) e.widget).getText());
				viewer.refresh();
			}
		});

		return composite;
	}

	@Override
	protected void okPressed() {
		IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
		if( ! s.isEmpty() ) {
			Command cmd = SetCommand.create(resource.getEditingDomain(), binding, MApplicationPackage.Literals.KEY_BINDING__COMMAND, s.getFirstElement());
			if( cmd.canExecute() ) {
				resource.getEditingDomain().getCommandStack().execute(cmd);
				super.okPressed();
			}
		}
	}
	
	private class LabelProviderImpl extends StyledCellLabelProvider implements ILabelProvider {
		private Styler styler = new Styler() {

			@Override
			public void applyStyles(TextStyle textStyle) {
				textStyle.foreground = getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY);
			}
		};
		
		public void update(final ViewerCell cell) {
			MCommand cmd = (MCommand) cell.getElement();
			
			StyledString styledString = new StyledString();
			if( cmd.getCommandName() != null ) {
				styledString.append(cmd.getCommandName());
			}
			if( cmd.getDescription() != null ) {
				styledString.append(" - " + cmd.getDescription(),styler);
			}
			if( cmd.getId() != null ) {
				styledString.append(" - " + cmd.getId(),styler);
			}
			cell.setText(styledString.getString());
			cell.setStyleRanges(styledString.getStyleRanges());
		}

		public Image getImage(Object element) {
			return null;
		}

		public String getText(Object element) {
			MCommand command = (MCommand) element;
			String s = "";
			if( command.getCommandName() != null ) {
				s += command.getCommandName();
			}
			
			if( command.getDescription() != null ) {
				s += " " + command.getDescription();
			}
			
			if( command.getId() != null ) {
				s += " " + command.getId();
			}
			
			return s;
		}
	}
}