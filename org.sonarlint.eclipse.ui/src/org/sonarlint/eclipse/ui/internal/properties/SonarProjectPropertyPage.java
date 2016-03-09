/*
 * SonarLint for Eclipse
 * Copyright (C) 2015-2016 SonarSource SA
 * sonarlint@sonarsource.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarlint.eclipse.ui.internal.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.sonarlint.eclipse.core.internal.resources.SonarLintProject;
import org.sonarlint.eclipse.core.internal.server.IServer;
import org.sonarlint.eclipse.core.internal.server.ServersManager;
import org.sonarlint.eclipse.ui.internal.Messages;
import org.sonarlint.eclipse.ui.internal.server.wizard.NewServerLocationWizard;

/**
 * Property page for projects to view sonar server connection. It store in
 * <project>/.settings/org.sonarlint.eclipse.prefs following properties
 *
 */
public class SonarProjectPropertyPage extends PropertyPage {

  private Button enabledBtn;
  private Text serverIdField;
  private Text moduleKeyField;
  private IServer server;

  public SonarProjectPropertyPage() {
    setTitle(Messages.SonarProjectPropertyPage_title);
    noDefaultButton();
  }

  @Override
  protected Control createContents(Composite parent) {
    if (parent == null) {
      return new Composite(parent, SWT.NULL);
    }

    final SonarLintProject sonarProject = SonarLintProject.getInstance(getProject());

    final Composite container = new Composite(parent, SWT.NULL);
    GridLayout layout = new GridLayout();
    container.setLayout(layout);
    container.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
    layout.numColumns = 2;
    layout.verticalSpacing = 9;

    enabledBtn = new Button(container, SWT.CHECK);
    enabledBtn.setText("Run SonarLint automatically");
    enabledBtn.setSelection(sonarProject.isBuilderEnabled());
    GridData layoutData = new GridData();
    layoutData.horizontalSpan = 2;
    enabledBtn.setLayoutData(layoutData);

    server = ServersManager.getInstance().getServer(sonarProject.getServerId());

    serverIdField = addText(Messages.SonarProjectPropertyBlock_label_serverId, serverName(sonarProject.getServerId()),
      container);
    moduleKeyField = addText(Messages.SonarProjectPropertyBlock_label_key, sonarProject.getModuleKey(), container);

    if (server == null) {
      Link hlink = new Link(container, SWT.NONE);
      hlink.setText("<a>Add SonarQube server '" + sonarProject.getServerId() + "'</a>");
      hlink.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
      GridData gd = new GridData(SWT.LEFT, SWT.FILL, true, false);
      gd.horizontalSpan = 2;
      hlink.setLayoutData(gd);
      hlink.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          NewServerLocationWizard wizard = new NewServerLocationWizard(sonarProject.getServerId());
          WizardDialog wd = new WizardDialog(container.getShell(), wizard);
          if (wd.open() == Window.OK) {
            server = ServersManager.getInstance().getServer(sonarProject.getServerId());
            serverIdField.setText(serverName(sonarProject.getServerId()));
          }
        }
      });
    }

    return container;
  }

  private String serverName(final String serverId) {
    return server != null ? server.getName() : "Unknown server: '" + serverId + "'";
  }

  private static Text addText(String label, String text, Composite container) {
    Label labelField = new Label(container, SWT.NONE);
    labelField.setText(label);

    Text textField = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
    textField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    textField.setText(text);
    return textField;
  }

  @Override
  public boolean performOk() {
    final SonarLintProject sonarProject = SonarLintProject.getInstance(getProject());
    sonarProject.setBuilderEnabled(enabledBtn.getSelection());
    return super.performOk();
  }

  private IProject getProject() {
    return (IProject) getElement().getAdapter(IProject.class);
  }
}
