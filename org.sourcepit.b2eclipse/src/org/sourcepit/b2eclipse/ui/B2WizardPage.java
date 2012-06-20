/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.ui;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.sourcepit.b2eclipse.input.Category;
import org.sourcepit.b2eclipse.input.TreeViewerInput;
import org.sourcepit.b2eclipse.provider.ContentProvider;
import org.sourcepit.b2eclipse.provider.LabelProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Marco Grupe
 */
public class B2WizardPage extends WizardPage
{

   private Text dirTxt, workspaceTxt;
   private Button dirBtn, workspaceBtn, rBtn1, rBtn2, checkBtn, workingSetBtn, selectAllBtn, deselectAllBtn;
   private Shell dirShell;
   private Composite modulePageWidgetContainer;
   private CheckboxTreeViewer dirTreeViewer;
   private Combo workingSetCombo;
   private IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
   private IWorkingSet[] workingSet;
   private IWorkingSetSelectionDialog workingSetSelectionDialog;
   private IWorkingSet workingSetComboItem;
   private String directoryName, comboBoxItems = ""; //$NON-NLS-1$
   private boolean checkButtonSelection = false;
   private IPath projectPath;
   private File workingSetXMLFile;
   private TreeViewerInput treeViewerInput;

   public B2WizardPage(String name)
   {


      super(name);

      setTitle(Messages.B2WizardPage_1);
      setDescription(Messages.B2WizardPage_2);


   }

   /**
    * Create specific controls for the wizard page.
    */
   public void createControl(Composite parent)
   {
      modulePageWidgetContainer = new Composite(parent, SWT.NONE);
      modulePageWidgetContainer.setLayout(new GridLayout(3, false));

      addWidgets();


      if (getPath() != null)
      {
         dirTxt.setText(String.valueOf(getPath()));
         dirTreeViewer.setInput(new TreeViewerInput(new File(String.valueOf(getPath()))));
      }


      dirShell = parent.getShell();

      treeViewerInput = new TreeViewerInput();

      addListener();


      setControl(modulePageWidgetContainer);


      setPageComplete(true);
      checkSection();

   }

   /**
    * add Widgets on Wizard Page
    */
   private void addWidgets()
   {
      GridData gridData = new GridData();
      gridData.horizontalAlignment = SWT.FILL;

      GridData gridData2 = new GridData();
      gridData2.horizontalAlignment = SWT.FILL;
      gridData2.widthHint = 90;
      gridData2.verticalAlignment = SWT.TOP;

      GridData gridData3 = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2);
      gridData3.widthHint = 500;
      gridData3.heightHint = 300;

      rBtn1 = new Button(modulePageWidgetContainer, SWT.RADIO);
      rBtn1.setText(Messages.B2WizardPage_3);
      rBtn1.setSelection(true);

      dirTxt = new Text(modulePageWidgetContainer, SWT.BORDER);
      dirTxt.setLayoutData(gridData);

      dirBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      dirBtn.setText(Messages.B2WizardPage_4);
      dirBtn.setLayoutData(gridData2);

      rBtn2 = new Button(modulePageWidgetContainer, SWT.RADIO);
      rBtn2.setText(Messages.B2WizardPage_5);

      workspaceTxt = new Text(modulePageWidgetContainer, SWT.BORDER);
      workspaceTxt.setLayoutData(gridData);
      workspaceTxt.setEnabled(false);

      workspaceBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      workspaceBtn.setText(Messages.B2WizardPage_6);
      workspaceBtn.setEnabled(false);
      workspaceBtn.setLayoutData(gridData2);

      dirTreeViewer = new CheckboxTreeViewer(modulePageWidgetContainer);
      dirTreeViewer.setContentProvider(new ContentProvider());
      dirTreeViewer.setLabelProvider(new LabelProvider());
      dirTreeViewer.getTree().setLayoutData(gridData3);

      selectAllBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      selectAllBtn.setText(Messages.B2WizardPage_7);
      selectAllBtn.setLayoutData(gridData2);

      deselectAllBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      deselectAllBtn.setText(Messages.B2WizardPage_8);
      deselectAllBtn.setLayoutData(gridData2);

      checkBtn = new Button(modulePageWidgetContainer, SWT.CHECK);
      checkBtn.setText(Messages.B2WizardPage_9);
      checkBtn.setLayoutData(gridData);

      workingSetCombo = new Combo(modulePageWidgetContainer, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.HORIZONTAL
         | SWT.LEFT_TO_RIGHT);
      workingSetCombo.setEnabled(false);
      workingSetCombo.setLayoutData(gridData);

      workingSetBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      workingSetBtn.setText(Messages.B2WizardPage_10);
      workingSetBtn.setEnabled(false);
      workingSetBtn.setLayoutData(gridData);
   }

   /**
    * add Listener to the specific widgets
    */
   private void addListener()
   {
      dirBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            clearArrayList();

            DirectoryDialog directoryDialog = new DirectoryDialog(dirShell, SWT.OPEN);
            directoryDialog.setText(Messages.B2WizardPage_11);
            directoryName = directoryDialog.open();
            if (directoryName == null)
               return;
            dirTxt.setText(directoryName);
            workspaceTxt.setText(""); //$NON-NLS-1$

            dirTreeViewer.setInput(new TreeViewerInput(new File(directoryName)));

            dirTreeViewer.expandAll();

         }
      });

      workspaceBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            clearArrayList();
            ElementTreeSelectionDialog elementTreeSelectionDialog = new ElementTreeSelectionDialog(dirShell,
               new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
            elementTreeSelectionDialog.setTitle(Messages.B2WizardPage_13);
            elementTreeSelectionDialog.setMessage(Messages.B2WizardPage_14);
            elementTreeSelectionDialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
            elementTreeSelectionDialog.open();
            if (elementTreeSelectionDialog.getFirstResult() != null)
            {
               directoryName = String.valueOf(((IResource) elementTreeSelectionDialog.getFirstResult()).getLocation());
               workspaceTxt.setText(directoryName);
               dirTxt.setText(""); //$NON-NLS-1$
               dirTreeViewer.setInput(new TreeViewerInput(new File(directoryName)));
            }
         }
      });

      rBtn1.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            if (rBtn1.isEnabled())
            {
               dirTxt.setEnabled(true);
               dirBtn.setEnabled(true);
               workspaceTxt.setEnabled(false);
               workspaceBtn.setEnabled(false);
            }

         }


      });

      rBtn2.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {

            if (rBtn2.isEnabled())
            {
               workspaceTxt.setEnabled(true);
               workspaceBtn.setEnabled(true);
               dirTxt.setEnabled(false);
               dirBtn.setEnabled(false);
            }

         }


      });


      checkBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (checkBtn.getSelection())
            {
               if (getWorkingSet() == null)
               {
                  addComboItemToWorkingSet();
               }

               checkButtonSelection = true;
               workingSetBtn.setEnabled(true);
               workingSetCombo.setEnabled(true);

            }
            else
            {
               checkButtonSelection = true;
               workingSetBtn.setEnabled(false);
               workingSetCombo.setEnabled(false);
            }
         }
      });


      workingSetBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {

            workingSetSelectionDialog = workingSetManager.createWorkingSetSelectionDialog(dirShell, true);
            if (dirTreeViewer.getCheckedElements().length != 0)
            {
               selectWorkingSetSelectionDialog();
            }
            workingSet = workingSetSelectionDialog.getSelection();

            addItemToCombo();
            checkWorkingSetCombo();

         }
      });

      selectAllBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {

            setCategoriesChecked();
            for (int i = 0; i < getTreeViewerInput().getProjectFileList().size(); i++)
            {

               dirTreeViewer.setSubtreeChecked(getTreeViewerInput().getProjectFileList().get(i), true);

            }

         }


      });

      deselectAllBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            setCategoriesUnchecked();

            for (int i = 0; i < getTreeViewerInput().getProjectFileList().size(); i++)
            {

               dirTreeViewer.setSubtreeChecked(getTreeViewerInput().getProjectFileList().get(i), false);

            }

         }


      });

      workingSetCombo.addSelectionListener(new SelectionListener()
      {
         public void widgetSelected(SelectionEvent e)
         {
            addComboItemToWorkingSet();
         }

         public void widgetDefaultSelected(SelectionEvent e)
         {
         }
      });

      // if a category is checked in the tree, check all its children
      dirTreeViewer.addCheckStateListener(new ICheckStateListener()
      {
         public void checkStateChanged(CheckStateChangedEvent event)
         {
            if (event.getChecked())
            {
               dirTreeViewer.setSubtreeChecked(event.getElement(), true);
            }
            else
            {
               dirTreeViewer.setSubtreeChecked(event.getElement(), false);
            }

         }
      });

   }

   private void addItemToCombo()
   {
      if (getWorkingSet() != null)
      {
         if (getWorkingSet().length == 1)
         {
            for (final IWorkingSet workingSet : getWorkingSet())
            {


               for (int y = 0; y < workingSetCombo.getItemCount(); y++)
               {
                  if (workingSetCombo.getItem(y).equals(workingSet.getName()))
                  {
                     return;
                  }

               }


               workingSetCombo.add(workingSet.getName());

               getDialogSettings().addNewSection(workingSet.getName());


            }
            workingSetCombo.setText(getWorkingSet()[0].getName());
         }
         else
         {
            for (final IWorkingSet workingSet : getWorkingSet())
            {


               for (int y = 0; y < workingSetCombo.getItemCount(); y++)
               {
                  if (workingSetCombo.getItem(y).equals(comboBoxItems))
                  {
                     return;
                  }

               }

               comboBoxItems = comboBoxItems.concat(workingSet.getName().concat(",")); //$NON-NLS-1$


            }
            if (comboBoxItems.length() != 0)
            {
               comboBoxItems = comboBoxItems.substring(0, comboBoxItems.length() - 1);
               workingSetCombo.add(comboBoxItems);

               getDialogSettings().addNewSection(comboBoxItems);

               workingSetCombo.setText(comboBoxItems);
               comboBoxItems = ""; //$NON-NLS-1$
            }
         }


      }

   }

   public void addComboItemToWorkingSet()
   {
      if (workingSetCombo.getText().contains(",")) //$NON-NLS-1$
      {
         String[] splitItems = workingSetCombo.getText().split(","); //$NON-NLS-1$
         workingSet = new IWorkingSet[splitItems.length];
         for (int i = 0; i < splitItems.length; i++)
         {
            workingSetComboItem = workingSetManager.getWorkingSet(splitItems[i]);
            workingSet[i] = workingSetComboItem;
         }
      }
      else
      {
         workingSetComboItem = workingSetManager.getWorkingSet(workingSetCombo.getText());
         workingSet = new IWorkingSet[] { workingSetComboItem };
      }

   }

   private void selectWorkingSetSelectionDialog()
   {
      if (getWorkingSet() == null)
      {
         addComboItemToWorkingSet();
         workingSetSelectionDialog.setSelection(getWorkingSet());
         workingSetSelectionDialog.open();
      }
      else if (workingSetCombo.getText().trim().length() == 0)
      {
         workingSetSelectionDialog.setSelection(null);
         workingSetSelectionDialog.open();
      }
      else if (workingSetComboItem != null || getWorkingSet() != null)
      {
         workingSetSelectionDialog.setSelection(getWorkingSet());
         workingSetSelectionDialog.open();
      }


   }

   /**
    * 
    * @return the selected projects in TreeViewer
    */
   public List<File> getSelectedProjects()
   {
      Object[] getCheckedElements = dirTreeViewer.getCheckedElements();
      ArrayList<File> getSelectedProjects = new ArrayList<File>();

      for (final Object checkedElement : getCheckedElements)
      {
         if (getTreeViewerInput().getCategories().contains(checkedElement))
         {
            continue;
         }
         getSelectedProjects.add(new File(checkedElement.toString()));
      }
      getSelectedProjects.trimToSize();
      return getSelectedProjects;
   }

   public void setPath(IPath projectPath)
   {
      if (projectPath == null)
      {
         throw new IllegalArgumentException();
      }
      else
      {
         this.projectPath = projectPath;
      }

   }

   private IPath getPath()
   {
      return projectPath;
   }

   public void setWorkingSetXML(File workingSetXMLFile)
   {

      if (workingSetXMLFile == null)
      {
         throw new IllegalArgumentException();
      }
      else
      {
         this.workingSetXMLFile = workingSetXMLFile;
      }

   }

   public File getWorkingSetXML()
   {
      return workingSetXMLFile;
   }

   public IWorkingSetManager getWorkingSetManager()
   {
      return workingSetManager;

   }

   public IWorkingSet[] getWorkingSet()
   {
      return workingSet;
   }

   private void setCategoriesChecked()
   {
      if (getTreeViewerInput().getCategories() != null)
      {
         for (final Category category : getTreeViewerInput().getCategories())
         {
            dirTreeViewer.setChecked(category, true);
         }
      }


   }

   private void setCategoriesUnchecked()
   {
      for (final Category category : getTreeViewerInput().getCategories())
      {
         dirTreeViewer.setChecked(category, false);
      }
   }

   public boolean getCheckButtonSelection()
   {
      return checkButtonSelection;
   }

   public void clearArrayList()
   {

      treeViewerInput.clearArrayList();
   }

   private void checkWorkingSetCombo()
   {
      checkWorkingSetComboComma();
      for (final String item : workingSetCombo.getItems())
      {
         for (int y = 0; y < workingSetManager.getWorkingSets().length; y++)
         {
            if (item.contains(",") && item.contains(workingSetManager.getWorkingSets()[y].getName())) //$NON-NLS-1$
            {
               break;
            }
            if (item.equals(workingSetManager.getWorkingSets()[y].getName()))
            {

               break;

            }
            else
            {
               if ((y + 1) == workingSetManager.getWorkingSets().length)
               {

                  workingSetCombo.remove(item);
               }

            }
         }
      }

   }

   private void checkWorkingSetComboComma()
   {
      int counter = 0;
      for (final String wsitem : workingSetCombo.getItems())
      {
         if (wsitem.contains(",")) //$NON-NLS-1$
         {
            String[] splitItems = wsitem.split(","); //$NON-NLS-1$
            for (final String item : splitItems)
            {
               for (final IWorkingSet workingSet : workingSetManager.getWorkingSets())
               {
                  if (item.equals(workingSet.getName()))
                  {
                     counter++;


                  }
               }
            }
            if (splitItems.length != counter)
            {
               System.out.println(splitItems.length + "      " + counter); //$NON-NLS-1$
               workingSetCombo.remove(wsitem);

            }
            counter = 0;

         }
      }
   }

   private void checkSection()
   {

      checkSectionComma();

      for (final IDialogSettings dialogSetting : getDialogSettings().getSections())
      {
         for (int y = 0; y < workingSetManager.getWorkingSets().length; y++)
         {

            if (getDialogSettings().getSection(dialogSetting.getName()).getName().contains(",") //$NON-NLS-1$
               && getDialogSettings().getSection(dialogSetting.getName()).getName()
                  .contains(workingSetManager.getWorkingSets()[y].getName()))
            {
               break;
            }

            if (getDialogSettings().getSection(dialogSetting.getName()).getName()
               .equals(workingSetManager.getWorkingSets()[y].getName()))
            {

               workingSetCombo.add(getDialogSettings().getSection(dialogSetting.getName()).getName());
               workingSetCombo.setText(getDialogSettings().getSection(dialogSetting.getName()).getName());


               break;

            }

            else
            {
               if ((y + 1) == workingSetManager.getWorkingSets().length)
               {


                  removeSection(getDialogSettings().getSection(dialogSetting.getName()).getName());
               }

            }


         }
      }
   }

   public void checkSectionComma()
   {
      int counter = 0;
      for (final IDialogSettings dialogSetting : getDialogSettings().getSections())
      {
         if (getDialogSettings().getSection(dialogSetting.getName()).getName().contains(",")) //$NON-NLS-1$
         {
            String[] splitItems = getDialogSettings().getSection(dialogSetting.getName()).getName().split(","); //$NON-NLS-1$
            for (final String item : splitItems)
            {

               for (final IWorkingSet workingSet : workingSetManager.getWorkingSets())
               {

                  if (item.equals(workingSet.getName()))
                  {
                     counter++;

                  }
               }
            }
            if (splitItems.length != counter)
            {

               removeSection(getDialogSettings().getSection(dialogSetting.getName()).getName());

            }
            else
            {
               workingSetCombo.add(getDialogSettings().getSection(dialogSetting.getName()).getName());
               workingSetCombo.setText(getDialogSettings().getSection(dialogSetting.getName()).getName());
            }
            counter = 0;
         }

      }

   }

   private void removeSection(String sectionName)
   {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();


      try
      {
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document doc = builder.parse(getWorkingSetXML());

         NodeList nodes = doc.getElementsByTagName("section"); //$NON-NLS-1$

         for (int i = 0; i < nodes.getLength(); i++)
         {

            Element rmSection = (Element) nodes.item(i);

            if (rmSection.getAttribute("name").equals(sectionName)) //$NON-NLS-1$
            {
               rmSection.getParentNode().removeChild(rmSection);
            }

         }

         saveXMLChanges(doc);
      }
      catch (ParserConfigurationException e)
      {
         throw new IllegalArgumentException(e);
      }
      catch (SAXException e)
      {
         throw new IllegalArgumentException(e);
      }
      catch (IOException e)
      {
         throw new IllegalArgumentException(e);
      }
      catch (TransformerException e)
      {
         throw new IllegalArgumentException(e);
      }


   }

   private void saveXMLChanges(Document doc) throws TransformerException, IOException
   {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

      StreamResult strmResult = new StreamResult(new StringWriter());
      DOMSource domSource = new DOMSource(doc);
      transformer.transform(domSource, strmResult);

      String xmlData = strmResult.getWriter().toString();

      byte xmlContent[] = xmlData.getBytes("UTF-8"); //$NON-NLS-1$

      FileWriter fileWriter = null;
      try
      {
         fileWriter = new FileWriter(getWorkingSetXML().getName());
         for (final byte data : xmlContent)
         {
            fileWriter.write(data);
         }
         xmlContent = null;

      }
      catch (IOException e)
      {
      }
      finally
      {
         closeWriter(fileWriter);
      }
   }

   private void closeWriter(FileWriter writer)
   {
      if (writer != null)
      {
         try
         {
            writer.close();
         }
         catch (IOException e)
         {
         }
      }
   }

   public TreeViewerInput getTreeViewerInput()
   {
      treeViewerInput = (TreeViewerInput) dirTreeViewer.getInput();
      return treeViewerInput;
   }

}