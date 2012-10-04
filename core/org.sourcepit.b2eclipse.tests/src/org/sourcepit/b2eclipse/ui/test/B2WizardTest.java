/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.ui.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import junit.textui.TestRunner;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Test;
import org.sourcepit.b2eclipse.ui.Messages;

public class B2WizardTest extends TestRunner
{
   private final SWTBot bot = new SWTBot();
   private SWTBotShell importShell;
   private SWTBotTree treeViewer;
   private SWTBotTreeItem swtBotFirstTreeItem = null;
   private SWTBotButton easy = null;
   private String projectPath = "target/tmp/";

   public void selectB2Wizard()
   {
      bot.menu("File").menu("Import...").click();
      bot.shell("Import").setFocus();
      bot.tree().select("Other").expandNode("Other").select("Check out b2 Project/s...");
      bot.button("Next >").click();

      importShell = bot.shell(Messages.msgImportTitle);
      importShell.activate();

      treeViewer = bot.tree();

      SWTBotText dirText = bot.textWithTooltip(Messages.msgSelectRootTt);
      dirText.setFocus();
      dirText.setText(projectPath);
   }

   @Test
   public void testButtons()
   {

      selectB2Wizard();

      SWTBotButton selectAll = importShell.bot().button(Messages.msgSelectAllBtn);
      selectAll.click();

      assertTrue(treeViewer.hasItems());
      for (SWTBotTreeItem item : treeViewer.getAllItems())
      {
         assertTrue(item.isChecked());
      }

      deSelectAll();

      SWTBotButton refresh = importShell.bot().button(Messages.msgRefreshBtn);
      refresh.click();

      for (SWTBotTreeItem item : treeViewer.getAllItems())
      {
         assertTrue(treeViewer.hasItems());
         assertFalse(item.isChecked());
      }

      importShell.close();

   }

   @Test
   public void createProject()
   {
      selectB2Wizard();
      deSelectAll();

      if (treeViewer.hasItems())
      {
         swtBotFirstTreeItem = treeViewer.getAllItems()[0].getItems()[0];
         swtBotFirstTreeItem.check();
      }
      finish();
      bot.sleep(1000);

   }

   @Test
   public void createProjects()
   {
      selectB2Wizard();
      deSelectAll();

      if (treeViewer.hasItems())
      {
         swtBotFirstTreeItem = treeViewer.getAllItems()[1].getItems()[0];
         swtBotFirstTreeItem.check();
         swtBotFirstTreeItem = treeViewer.getAllItems()[2].getItems()[0];
         swtBotFirstTreeItem.check();
      }
      finish();
      bot.sleep(1000);

   }

   public void finish()
   {
      SWTBotButton finish = bot.button("Finish");
      finish.click();
   }

   @Test
   public void easyCreateProject()
   {
      selectB2Wizard();
      deSelectAll();

      if (treeViewer.hasItems())
      {
         swtBotFirstTreeItem = treeViewer.getAllItems()[0].getItems()[1];
         swtBotFirstTreeItem.check();
      }
      easy = bot.buttonWithTooltip(Messages.msgEasyTt);
      easy.click();
      bot.sleep(1000);

   }

   @Test
   public void easyCreateProjectToExistingWS()
   {
      selectB2Wizard();
      deSelectAll();
      
      if (treeViewer.hasItems())
      {
         swtBotFirstTreeItem = treeViewer.getAllItems()[1].getItems()[1];
         swtBotFirstTreeItem.check();
      }
      easy = bot.buttonWithTooltip(Messages.msgEasyTt);
      easy.click();
      bot.sleep(1000);
   }

   @Test
   public void openAndCloseWorkingSetDialog()
   {
      selectB2Wizard();
      SWTBotCheckBox projectToWorkingSets = bot.checkBox("Add project to working sets");
      projectToWorkingSets.click();
      SWTBotButton workingSet = bot.button("Select...");
      workingSet.setFocus();
      workingSet.click();
      SWTBotShell workingSetShell = bot.shell("Select Working Sets");
      workingSetShell.activate();
      workingSetShell.close();
      cancel();
      assertTrue(ResourcesPlugin.getWorkspace().getRoot().getProjects().length == 5);
   }

   public void cancel()
   {
      SWTBotButton cancel = bot.button("Cancel");
      cancel.click();
   }

   public void deSelectAll()
   {
      SWTBotButton deSelectAll = importShell.bot().button(Messages.msgDeselectAllBtn);
      deSelectAll.click();
   }
}