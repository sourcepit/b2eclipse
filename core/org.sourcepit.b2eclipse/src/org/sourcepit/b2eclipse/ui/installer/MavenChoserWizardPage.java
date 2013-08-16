/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.ui.installer;

import java.io.File;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.m2e.core.embedder.MavenRuntime;
import org.eclipse.m2e.core.embedder.MavenRuntimeManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.sourcepit.b2eclipse.core.ArtifactResolver;
import org.sourcepit.b2eclipse.core.LocatableArtifactsDetector;
import org.sourcepit.b2eclipse.core.runtime.B2Runtime;
import org.sourcepit.b2eclipse.core.runtime.B2RuntimeUtils;

public class MavenChoserWizardPage extends WizardPage
{
   private final File parentDir;

   private final MavenRuntimeManager mavenRuntimeManager;

   private final LocatableArtifactsDetector artifactsDetector;

   private final ArtifactResolver artifactResolver;

   final List<MavenRuntime> runtimes;

   private TableViewer runtimesViewer;

   /**
    * Create the wizard.
    * 
    * @param artifactsDetector
    */
   public MavenChoserWizardPage(File parentDir, MavenRuntimeManager mavenRuntimeManager,
      LocatableArtifactsDetector artifactsDetector, ArtifactResolver artifactResolver)
   {
      super("wizardPage");
      setTitle("Wizard Page title");
      setDescription("Wizard Page description");

      this.mavenRuntimeManager = mavenRuntimeManager;
      this.artifactsDetector = artifactsDetector;
      this.artifactResolver = artifactResolver;

      this.runtimes = mavenRuntimeManager.getMavenRuntimes();
      this.parentDir = parentDir;
   }

   /**
    * Create contents of the wizard.
    * 
    * @param parent
    */
   public void createControl(final Composite parent)
   {
      Composite container = new Composite(parent, SWT.NULL);

      setControl(container);
      GridLayout gl_container = new GridLayout(2, false);
      gl_container.marginHeight = 0;
      gl_container.marginWidth = 0;
      container.setLayout(gl_container);

      runtimesViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
      runtimesViewer.addSelectionChangedListener(new ISelectionChangedListener()
      {
         public void selectionChanged(SelectionChangedEvent event)
         {
            MavenChoserWizardPage.this.selectionChanged(Selections.getFirstElement(event, MavenRuntime.class));
         }
      });
      runtimesViewer.setContentProvider(ArrayContentProvider.getInstance());
      runtimesViewer.addFilter(new ViewerFilter()
      {
         @Override
         public boolean select(Viewer viewer, Object parentElement, Object element)
         {
            return ((MavenRuntime) element).isEditable();
         }
      });

      Table table = runtimesViewer.getTable();
      table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
      table.setHeaderVisible(true);

      TableViewerColumn tableViewerColumn = new TableViewerColumn(runtimesViewer, SWT.NONE);
      tableViewerColumn.setLabelProvider(new ColumnLabelProvider()
      {
         public Image getImage(Object element)
         {
            return null;
         }

         public String getText(Object element)
         {
            return element == null ? "" : element.toString();
         }
      });
      TableColumn tblclmnNewColumn = tableViewerColumn.getColumn();
      tblclmnNewColumn.setWidth(320);
      tblclmnNewColumn.setText("Maven Installations");

      TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(runtimesViewer, SWT.NONE);
      tableViewerColumn_1.setLabelProvider(new ColumnLabelProvider()
      {
         public Image getImage(Object element)
         {
            return null;
         }

         public String getText(Object element)
         {
            final MavenRuntime runtime = (MavenRuntime) element;
            final List<B2Runtime> b2Runtimes = B2RuntimeUtils.getB2Runtimes(new File(runtime.getLocation()));
            if (!b2Runtimes.isEmpty())
            {
               return b2Runtimes.get(0).getVersion();
            }
            return null;
         }
      });

      TableColumn tblclmnNewColumn_1 = tableViewerColumn_1.getColumn();
      tblclmnNewColumn_1.setWidth(120);
      tblclmnNewColumn_1.setText("B2 Version");

      Composite composite = new Composite(container, SWT.NONE);
      composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
      GridLayout gl_composite = new GridLayout(1, false);
      gl_composite.marginWidth = 0;
      gl_composite.marginHeight = 0;
      composite.setLayout(gl_composite);

      Button btnNewButton = new Button(composite, SWT.NONE);
      btnNewButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      btnNewButton.setText("Browse...");
      btnNewButton.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            DirectoryDialog dlg = new DirectoryDialog(parent.getShell());

            dlg.setText("Maven Installation");
            dlg.setMessage("Select Maven installation directory");

            String dir = dlg.open();
            if (dir == null)
            {
               return;
            }
            addMavenInstallation(new File(dir));
         }
      });


      Button btnNewButton_1 = new Button(composite, SWT.NONE);
      btnNewButton_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      btnNewButton_1.setText("Install...");

      btnNewButton_1.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            MavenInstallerWizard mavenInstaller = new MavenInstallerWizard(parentDir, artifactsDetector,
               artifactResolver);

            WizardDialog dlg = new WizardDialog(parent.getShell(), mavenInstaller);

            if (dlg.open() == Window.OK)
            {
               final File mavenDir = mavenInstaller.getInstallationDirectory();
               addMavenInstallation(mavenDir);
            }
         }
      });

      runtimesViewer.setInput(runtimes);
   }

   protected void selectionChanged(MavenRuntime mavenRuntime)
   {
   }

   void addMavenInstallation(File dir)
   {
      boolean ok = validateMavenInstallation(dir);
      if (ok)
      {
         MavenRuntime runtime = MavenRuntimeManager.createExternalRuntime(dir.getPath());
         if (runtimes.contains(runtime))
         {
            MessageDialog.openError(getShell(), "Maven Installation",
               "The selected Maven install is already registered.");
         }
         else
         {
            final MavenRuntime defaultRuntime = mavenRuntimeManager.getDefaultRuntime();
            runtimes.add(runtime);

            mavenRuntimeManager.setRuntimes(runtimes);
            mavenRuntimeManager.setDefaultRuntime(defaultRuntime);

            runtimesViewer.refresh();
            runtimesViewer.setSelection(new StructuredSelection(runtime), true);
         }
      }
   }

   boolean validateMavenInstallation(File dir)
   {
      if (!dir.isDirectory())
      {
         MessageDialog.openError(getShell(), "Maven Installation", "Select the directory where Maven is installed.");
         return false;
      }
      File binDir = new File(dir, "bin"); //$NON-NLS-1$
      File confDir = new File(dir, "conf"); //$NON-NLS-1$
      File libDir = new File(dir, "lib"); //$NON-NLS-1$
      if (!binDir.exists() || !confDir.exists() || !libDir.exists())
      {
         MessageDialog.openError(getShell(), "Maven Installation",
            "The selected directory is not a valid Maven directory.");
         return false;
      }
      return true;
   }
}
