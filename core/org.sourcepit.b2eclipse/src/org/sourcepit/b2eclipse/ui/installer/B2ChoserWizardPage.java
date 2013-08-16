/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.ui.installer;

import static org.sourcepit.common.utils.lang.Exceptions.pipe;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.sourcepit.b2eclipse.core.DefaultLocatableArtifactsDetector;
import org.sourcepit.b2eclipse.core.DefaultMavenContext;
import org.sourcepit.b2eclipse.core.LocatableArtifactsDetector;
import org.sourcepit.b2eclipse.core.MavenContext;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;

public class B2ChoserWizardPage extends WizardPage
{
   final LocatableArtifactsDetector artifactsDetector;

   List<ArtifactRepository> artifactRepository;

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

   public void setArtifactRepository(List<ArtifactRepository> artifactRepository)
   {
      this.artifactRepository = artifactRepository;
   }

   public List<ArtifactRepository> getArtifactRepository()
   {
      return artifactRepository;
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

      comboViewer.setContentProvider(new IStructuredContentProvider()
      {
         @Override
         public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
         {
         }

         @Override
         public Object[] getElements(Object inputElement)
         {
            if (inputElement instanceof List)
            {
               return ((List<?>) inputElement).toArray();
            }
            return null;
         }

         @Override
         public void dispose()
         {
         }
      });

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

      IRunnableWithProgress runnable = new IRunnableWithProgress()
      {
         @Override
         public void run(IProgressMonitor monitor)
         {
            final List<Artifact> artifacts = artifactsDetector.detectLocateableArtifacts("org.sourcepit.b2",
               "b2-bootstrapper", "jar", null, getArtifactRepository(), null, monitor);

            Collections.reverse(artifacts);

            final Artifact recommended = determineRecommended(artifacts);

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

      run(runnable);
   }

   void setArtifacts(List<Artifact> artifacts, Artifact recommended)
   {
      comboViewer.setInput(artifacts);
      comboViewer.setSelection(recommended == null ? StructuredSelection.EMPTY : new StructuredSelection(recommended),
         true);
   }

   Artifact determineRecommended(List<Artifact> artifacts)
   {
      for (Artifact artifact : artifacts)
      {
         if (!ArtifactUtils.isSnapshot(artifact.getVersion()))
         {
            return artifact;
         }
      }
      return null;
   }

   void run(IRunnableWithProgress runnable)
   {
      try
      {
         try
         {
            getContainer().run(true, true, runnable);
         }
         catch (InvocationTargetException e)
         {
            throw e.getCause();
         }
      }
      catch (Exception e)
      {
         throw pipe(e);
      }
      catch (Error e)
      {
         throw pipe(e);
      }
      catch (Throwable e)
      {
         throw pipe(new IllegalStateException(e));
      }
   }
}
