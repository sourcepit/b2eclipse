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
   private String directoryName, comboBoxItems = "";
   private boolean checkButtonSelection = false;
   private IPath projectPath;
   private File workingSetXMLFile;
   private TreeViewerInput treeViewerInput;
   private static final String WORKING_SET_KEY = "WS";


   public B2WizardPage(String name)
   {


      super(name);

      setTitle("Import Modules");
      setDescription("Please specify a project or directory to import. ");


   }


   /**
    * 
    * @return the selected projects in TreeViewer
    */
   public List<File> getSelectedProjects()
   {
      Object[] getCheckedElements = dirTreeViewer.getCheckedElements();
      List<File> getSelectedProjects = new ArrayList<File>();

      for (Object checkedElement : getCheckedElements)
      {
         if (TreeViewerInput.getCategories().contains(checkedElement))
         {
            continue;
         }
         getSelectedProjects.add(new File(checkedElement.toString()));
      }

      return getSelectedProjects;
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
      rBtn1.setText("Select root directory:");
      rBtn1.setSelection(true);

      dirTxt = new Text(modulePageWidgetContainer, SWT.BORDER);
      dirTxt.setLayoutData(gridData);

      dirBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      dirBtn.setText("Browse...");
      dirBtn.setLayoutData(gridData2);

      rBtn2 = new Button(modulePageWidgetContainer, SWT.RADIO);
      rBtn2.setText("Select workspace project:");

      workspaceTxt = new Text(modulePageWidgetContainer, SWT.BORDER);
      workspaceTxt.setLayoutData(gridData);
      workspaceTxt.setEnabled(false);

      workspaceBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      workspaceBtn.setText("Browse...");
      workspaceBtn.setEnabled(false);
      workspaceBtn.setLayoutData(gridData2);

      dirTreeViewer = new CheckboxTreeViewer(modulePageWidgetContainer);
      dirTreeViewer.setContentProvider(new ContentProvider());
      dirTreeViewer.setLabelProvider(new LabelProvider());
      dirTreeViewer.getTree().setLayoutData(gridData3);


      selectAllBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      selectAllBtn.setText("Select All");
      selectAllBtn.setLayoutData(gridData2);

      deselectAllBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      deselectAllBtn.setText("Deselect All");
      deselectAllBtn.setLayoutData(gridData2);

      checkBtn = new Button(modulePageWidgetContainer, SWT.CHECK);
      checkBtn.setText("Select Working Set:");
      checkBtn.setLayoutData(gridData);

      workingSetCombo = new Combo(modulePageWidgetContainer, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.HORIZONTAL
         | SWT.LEFT_TO_RIGHT);
      workingSetCombo.setEnabled(false);
      workingSetCombo.setLayoutData(gridData);

      workingSetBtn = new Button(modulePageWidgetContainer, SWT.PUSH);
      workingSetBtn.setText("Select...");
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
            TreeViewerInput.clearArrayList();

            DirectoryDialog directoryDialog = new DirectoryDialog(dirShell, SWT.OPEN);
            directoryDialog.setText("Directory Selection...");
            directoryName = directoryDialog.open();
            if (directoryName == null)
               return;
            dirTxt.setText(directoryName);
            workspaceTxt.setText("");

            dirTreeViewer.setInput(new TreeViewerInput(new File(directoryName)));

            dirTreeViewer.expandAll();

         }
      });

      workspaceBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            TreeViewerInput.clearArrayList();
            ElementTreeSelectionDialog elementTreeSelectionDialog = new ElementTreeSelectionDialog(dirShell,
               new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
            elementTreeSelectionDialog.setTitle("Project Selection");
            elementTreeSelectionDialog.setMessage("Select a project:");
            elementTreeSelectionDialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
            elementTreeSelectionDialog.open();
            if (elementTreeSelectionDialog.getFirstResult() != null)
            {
               directoryName = String.valueOf(((IResource) elementTreeSelectionDialog.getFirstResult()).getLocation());
               workspaceTxt.setText(directoryName);
               dirTxt.setText("");
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
                  checkComboItem();
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

            for (File projectFile : treeViewerInput.getProjectFileList())
            {
               dirTreeViewer.setSubtreeChecked(projectFile, true);
            }
         }
      });

      deselectAllBtn.addListener(SWT.Selection, new Listener()
      {
         public void handleEvent(Event event)
         {
            setCategoriesUnchecked();
            for (File projectFile : treeViewerInput.getProjectFileList())
            {
               dirTreeViewer.setSubtreeChecked(projectFile, false);
            }
         }
      });

      workingSetCombo.addSelectionListener(new SelectionListener()
      {
         public void widgetSelected(SelectionEvent e)
         {
            checkComboItem();
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
      checkWorkingSetXML();

   }

   public IWorkingSetManager getWorkingSetManager()
   {
      return workingSetManager;

   }

   public IWorkingSet[] getWorkingSet()
   {
      return workingSet;
   }

   private void addItemToCombo()
   {
      if (getWorkingSet() != null)
      {
         if (getWorkingSet().length == 1)
         {
            for (IWorkingSet workingSet : getWorkingSet())
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
               getDialogSettings().getSection(workingSet.getName()).put(WORKING_SET_KEY, workingSet.getName());


            }
            workingSetCombo.setText(getWorkingSet()[0].getName());
         }
         else
         {
            for (IWorkingSet workingSet : getWorkingSet())
            {


               for (int y = 0; y < workingSetCombo.getItemCount(); y++)
               {
                  if (workingSetCombo.getItem(y).equals(comboBoxItems))
                  {
                     return;
                  }

               }

               comboBoxItems = comboBoxItems.concat(workingSet.getName().concat(","));


            }
            if (comboBoxItems.length() != 0)
            {
               comboBoxItems = comboBoxItems.substring(0, comboBoxItems.length() - 1);
               workingSetCombo.add(comboBoxItems);

               getDialogSettings().addNewSection(comboBoxItems);
               getDialogSettings().getSection(comboBoxItems).put(WORKING_SET_KEY, comboBoxItems);
               workingSetCombo.setText(comboBoxItems);
               comboBoxItems = "";
            }
         }


      }

   }

   private void selectWorkingSetSelectionDialog()
   {
      if (getWorkingSet() == null)
      {
         checkComboItem();
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

   private void setCategoriesChecked()
   {
      for (int i = 0; i < TreeViewerInput.getCategories().size(); i++)
      {
         dirTreeViewer.setChecked(TreeViewerInput.getCategories().get(i), true);
      }


   }

   private void setCategoriesUnchecked()
   {
      for (int i = 0; i < TreeViewerInput.getCategories().size(); i++)
      {
         dirTreeViewer.setChecked(TreeViewerInput.getCategories().get(i), false);
      }
   }

   public boolean getCheckButtonSelection()
   {
      return checkButtonSelection;
   }

   public void checkComboItem()
   {
      if (workingSetCombo.getText().contains(","))
      {
         String[] splitItems = workingSetCombo.getText().split(",");
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

   public void checkSectionComma()
   {
      int counter = 0;
      for (IDialogSettings dialogSetting : getDialogSettings().getSections())
      {
         if (getDialogSettings().getSection(dialogSetting.getName()).get(WORKING_SET_KEY).contains(","))
         {
            String[] splitItems = getDialogSettings().getSection(dialogSetting.getName()).get(WORKING_SET_KEY)
               .split(",");
            for (String item : splitItems)
            {

               for (IWorkingSet workingSet : workingSetManager.getWorkingSets())
               {

                  if (item.equals(workingSet.getName()))
                  {
                     counter++;

                  }
               }
            }
            if (counter == 0 || counter == 1)
            {

               removeWorkingSetXMLSection(getDialogSettings().getSection(dialogSetting.getName()).getName());
               counter = 0;
            }
            else
            {
               workingSetCombo.add(getDialogSettings().getSection(dialogSetting.getName()).get(WORKING_SET_KEY));
               workingSetCombo.setText(getDialogSettings().getSection(dialogSetting.getName()).get(WORKING_SET_KEY));
            }

         }

      }

   }

   public void setPath(IPath projectPath)
   {
      this.projectPath = projectPath;
   }

   private IPath getPath()
   {
      return projectPath;
   }

   public void setWorkingSetXML(File workingSetXMLFile)
   {
      this.workingSetXMLFile = workingSetXMLFile;
   }

   public File getWorkingSetXML()
   {
      return workingSetXMLFile;
   }

   private void checkWorkingSetCombo()
   {
      checkWorkingSetComboComma();
      for (String item : workingSetCombo.getItems())
      {
         for (int y = 0; y < workingSetManager.getWorkingSets().length; y++)
         {
            if (item.contains(",") && item.contains(workingSetManager.getWorkingSets()[y].getName()))
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
      for (String wsitem : workingSetCombo.getItems())
      {
         if (wsitem.contains(","))
         {
            String[] splitItems = wsitem.split(",");
            for (String item : splitItems)
            {

               for (int y = 0; y < workingSetManager.getWorkingSets().length; y++)
               {
                  if (item.equals(workingSetManager.getWorkingSets()[y].getName()))
                  {
                     counter++;


                  }
               }
            }
            if (counter == 0 || counter == 1)
            {

               workingSetCombo.remove(wsitem);
            }
            counter = 0;
         }
      }
   }

   private void checkWorkingSetXML()
   {

      checkSectionComma();

      for (IDialogSettings dialogSetting : getDialogSettings().getSections())
      {
         for (int y = 0; y < workingSetManager.getWorkingSets().length; y++)
         {

            if (getDialogSettings().getSection(dialogSetting.getName()).get(WORKING_SET_KEY).contains(",")
               && getDialogSettings().getSection(dialogSetting.getName()).get(WORKING_SET_KEY)
                  .contains(workingSetManager.getWorkingSets()[y].getName()))
            {
               break;
            }

            if (getDialogSettings().getSection(dialogSetting.getName()).get(WORKING_SET_KEY)
               .equals(workingSetManager.getWorkingSets()[y].getName()))
            {

               workingSetCombo.add(getDialogSettings().getSection(dialogSetting.getName()).get(WORKING_SET_KEY));
               workingSetCombo.setText(getDialogSettings().getSection(dialogSetting.getName()).get(WORKING_SET_KEY));


               break;

            }

            else
            {
               if ((y + 1) == workingSetManager.getWorkingSets().length)
               {


                  removeWorkingSetXMLSection(getDialogSettings().getSection(dialogSetting.getName()).getName());
               }

            }


         }
      }
   }

   private void removeWorkingSetXMLSection(String section)
   {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

      try
      {
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document doc = builder.parse(getWorkingSetXML());
         deleteSection(doc, section);
         saveXMLChanges(doc);

      }
      catch (ParserConfigurationException e)
      {
         throw new IllegalStateException(e);
      }
      catch (SAXException e)
      {
         throw new IllegalStateException(e);
      }
      catch (IOException e)
      {
         throw new IllegalStateException(e);
      }
      catch (TransformerException e)
      {
         throw new IllegalStateException(e);
      }

   }

   private void deleteSection(Document doc, String sectionName)
   {
      NodeList nodes = doc.getElementsByTagName("section");

      for (int i = 0; i < nodes.getLength(); i++)
      {

         Element section = (Element) nodes.item(i);

         if (section.getAttribute("name").equals(sectionName))
         {
            section.getParentNode().removeChild(section);
         }

      }
   }

   private void saveXMLChanges(Document doc) throws TransformerException, IOException
   {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      StreamResult result = new StreamResult(new StringWriter());
      DOMSource source = new DOMSource(doc);
      transformer.transform(source, result);

      String xmlString = result.getWriter().toString();
      // System.out.println(xmlString);

      byte buf[] = xmlString.getBytes();

      FileWriter f0 = null;
      try
      {
         f0 = new FileWriter(getWorkingSetXML());
         for (int i = 0; i < buf.length; i++)
         {
            f0.write(buf[i]);
         }
         buf = null;
         getDialogSettings().save(f0);
      }
      finally
      {
         if (f0 != null)
         {
            try
            {
               f0.close();
            }
            catch (IOException e)
            {
            }
         }
      }
   }


}