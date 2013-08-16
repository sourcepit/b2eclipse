/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.ui.installer;

import static org.sourcepit.b2eclipse.ui.installer.Selections.structuredSelection;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.sourcepit.b2eclipse.core.LocatableArtifactsDetector;

import com.google.common.base.Predicate;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;

public class MavenInstallerWizardPage extends WizardPage
{
   private final LocatableArtifactsDetector artifactsDetector;

   private final File parentDir;

   /**
    * Create the wizard.
    */
   public MavenInstallerWizardPage(File parentDir, LocatableArtifactsDetector artifactsDetector)
   {
      super("wizardPage");
      setTitle("Wizard Page title");
      setDescription("Wizard Page description");

      this.artifactsDetector = artifactsDetector;
      this.parentDir = parentDir;
   }

   /**
    * Create contents of the wizard.
    * 
    * @param parent
    */
   public void createControl(Composite parent)
   {
      Composite container = new Composite(parent, SWT.NULL);

      setControl(container);
      container.setLayout(new GridLayout(2, false));

      Label lblNewLabel = new Label(container, SWT.NONE);
      lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel.setText("Maven version:");

      final ComboViewer comboViewer = new ComboViewer(container, SWT.NONE);
      comboViewer.setContentProvider(ArrayContentProvider.getInstance());
      comboViewer.setLabelProvider(new LabelProvider()
      {
         @Override
         public String getText(Object element)
         {
            if (element instanceof Artifact)
            {
               return ((Artifact) element).getVersion();
            }
            return null;
         }
      });
      Combo combo = comboViewer.getCombo();
      combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Composite composite = new Composite(container, SWT.NONE);
      composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
      GridLayout gl_composite = new GridLayout(3, false);
      gl_composite.marginWidth = 0;
      composite.setLayout(gl_composite);

      final Button btnLocationButton = new Button(composite, SWT.CHECK);
      btnLocationButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
      btnLocationButton.setSize(126, 16);
      btnLocationButton.setSelection(true);
      btnLocationButton.setText("Use default location");

      final Label lblNewLabel_1 = new Label(composite, SWT.NONE);
      lblNewLabel_1.setEnabled(false);
      lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel_1.setText("New Label");

      final Text text = new Text(composite, SWT.BORDER);
      text.setEnabled(false);
      text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      final Button btnBrowseButton = new Button(composite, SWT.NONE);
      btnBrowseButton.setEnabled(false);
      btnBrowseButton.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
         }
      });
      btnBrowseButton.setText("Browse...");
      btnLocationButton.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            final boolean enabled = !btnLocationButton.getSelection();
            lblNewLabel_1.setEnabled(enabled);
            text.setEnabled(enabled);
            btnBrowseButton.setEnabled(enabled);
         }
      });

      comboViewer.addSelectionChangedListener(new ISelectionChangedListener()
      {
         public void selectionChanged(SelectionChangedEvent event)
         {
            final Artifact selectedArtifact = Selections.getFirstElement(event, Artifact.class);
            final File file = determineDefaultInstallationDirectory(selectedArtifact);
            text.setText(file.getPath());

            updateArtifact(selectedArtifact);
         }
      });

      text.addModifyListener(new ModifyListener()
      {
         public void modifyText(ModifyEvent e)
         {
            updateInstallationDirectory(text.getText());
         }
      });

      final Predicate<Artifact> artifactFilter = new ArtifactVersionPredicate("[3.0.2,3.1-alpha)");
      final IRunnableWithProgress runnable = new DetectLocatableArtifactsRunnable(artifactsDetector,
         "org.apache.maven", "apache-maven", "zip", "bin", artifactFilter)
      {
         @Override
         protected void detectedLocatableArtifacts(final List<Artifact> artifacts, final Artifact recommended)
         {
            Display.getDefault().asyncExec(new Runnable()
            {
               @Override
               public void run()
               {
                  comboViewer.setInput(artifacts);
                  comboViewer.setSelection(structuredSelection(recommended), true);
               }
            });
         }
      };
      RunnableContexts.run(getContainer(), true, true, runnable);
   }

   private File installationDirectory;

   private Artifact artifact;

   protected void updateInstallationDirectory(String path)
   {
      installationDirectory = new File(path);
   }

   protected void updateArtifact(Artifact artifact)
   {
      this.artifact = artifact;
   }

   public File getInstallationDirectory()
   {
      return installationDirectory;
   }

   public Artifact getArtifact()
   {
      return artifact;
   }

   protected File determineDefaultInstallationDirectory(Artifact artifact)
   {
      final String dirName = artifact.getArtifactId() + "-" + artifact.getVersion();

      File installationDir = new File(parentDir, dirName);

      int i = 1;
      while (installationDir.exists())
      {
         installationDir = new File(parentDir, dirName + " (" + i + ")");
         i++;
      }

      return installationDir;
   }
}
