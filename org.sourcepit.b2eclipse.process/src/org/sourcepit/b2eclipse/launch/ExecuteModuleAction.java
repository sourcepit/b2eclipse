/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.launch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 */

public class ExecuteModuleAction implements ILaunchShortcut
{

   private List<String> fileList = new ArrayList<String>();


   @Override
   public void launch(ISelection selection, String mode)
   {
      org.sourcepit.b2eclipse.mvn.Activator.getDefault().copy();
      if (selection instanceof IStructuredSelection)
      {
         IStructuredSelection structuredSelection = (IStructuredSelection) selection;
         Object element = structuredSelection.getFirstElement();

         if (element instanceof IAdaptable)
         {
            final IResource selectedResource = (IResource) ((IAdaptable) element).getAdapter(IResource.class);
            String path = null;

            String mvnBatPath = org.sourcepit.b2eclipse.mvn.Activator.getDefault().getMvnPath()
               + "\\b2\\apache-maven-3.0.4\\bin\\mvn.bat clean deploy";

            String mvnPath = org.sourcepit.b2eclipse.mvn.Activator.getDefault().getMvnPath()
               + "\\b2\\apache-maven-3.0.4\\";

            if (selectedResource.getType() == IResource.FILE || selectedResource.getType() == IResource.PROJECT)
            {
               path = getModulePath(new File(selectedResource.getLocation().toOSString()));
            }

            try
            {
               final Map<String, String> envs = new LinkedHashMap<String, String>(System.getenv());
               envs.put("M2_HOME", mvnPath);

               List<String> envp = new ArrayList<String>();
               for (Entry<String, String> entry : envs.entrySet())
               {
                  String name = entry.getKey();
                  envp.add(name + "=" + entry.getValue());
               }

               String[] envpResult = new String[envp.size()];
               envp.toArray(envpResult);

               Runtime runtime = Runtime.getRuntime();
               runtime.exec("cmd.exe /c start " + mvnBatPath, envpResult, new File(path));

            }
            catch (IOException e)
            {
               throw new IllegalStateException(e);
            }
         }
      }
   }

   @Override
   public void launch(IEditorPart editor, String mode)
   {
   }

   private String getModulePath(File file)
   {
      File[] elementList = file.getParentFile().listFiles();
      fileList.clear();

      for (File i : elementList)
      {
         fileList.add(i.getName());
      }

      if (fileList.contains("module.xml"))
      {
         for (File element : elementList)
         {
            if (element.getName().equals("module.xml"))
            {
               return element.getParentFile().getAbsolutePath();
            }
         }
      }
      return getModulePath(file.getParentFile());

   }


}
