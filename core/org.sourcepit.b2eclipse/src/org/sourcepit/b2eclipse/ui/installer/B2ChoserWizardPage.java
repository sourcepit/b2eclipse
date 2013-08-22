/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.ui.installer;

import static org.sourcepit.b2eclipse.ui.installer.Selections.structuredSelection;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.sourcepit.b2eclipse.core.LocatableArtifactsDetector;

public class B2ChoserWizardPage extends WizardPage
{
   final LocatableArtifactsDetector artifactsDetector;

   private ComboViewer comboViewer;

   /**
    * Create the wizard.
    */
   public B2ChoserWizardPage(LocatableArtifactsDetector artifactsDetector)
   {
      super("wizardPage");
      setTitle("Wizard Page title");
      setDescription("Wizard Page description");
      this.artifactsDetector = artifactsDetector;
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
      container.setLayout(new FillLayout(SWT.HORIZONTAL));

      comboViewer = new ComboViewer(container, SWT.NONE);
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

      comboViewer.addSelectionChangedListener(new ISelectionChangedListener()
      {
         public void selectionChanged(SelectionChangedEvent event)
         {
            B2ChoserWizardPage.this.selectionChanged(Selections.getFirstElement(event, Artifact.class));
         }
      });

      final IRunnableWithProgress runnable = new DetectLocatableArtifactsRunnable(artifactsDetector,
         "org.sourcepit.b2", "b2-bootstrapper", "jar", null, null)
      {
         @Override
         protected void detectedLocatableArtifacts(final List<Artifact> artifacts, final Artifact recommended)
         {
            Display.getDefault().asyncExec(new Runnable()
            {
               @Override
               public void run()
               {
                  setArtifacts(artifacts, recommended);
               }
            });
         }
      };
      RunnableContexts.run(getContainer(), true, true, runnable);
   }

   protected void selectionChanged(Artifact artifact)
   {

   }

   void setArtifacts(List<Artifact> artifacts, Artifact recommended)
   {
      comboViewer.setInput(artifacts);
      comboViewer.setSelection(structuredSelection(recommended));
   }

}
