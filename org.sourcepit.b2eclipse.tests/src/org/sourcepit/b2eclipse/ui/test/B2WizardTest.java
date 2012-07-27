
package org.sourcepit.b2eclipse.ui.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import junit.textui.TestRunner;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcepit.b2eclipse.ui.Messages;

@RunWith(SWTBotJunit4ClassRunner.class)
public class B2WizardTest extends TestRunner
{
   private final SWTBot bot = new SWTBot();
   private SWTBotShell importShell;
   private SWTBotTree treeViewer;
   private SWTBotTreeItem swtBotFirstTreeItem = null;
   private SWTBotButton easy = null;
   private String projectPath = "target/tmp/";

   public void chooseB2Wizard()
   {
      bot.menu("File").menu("Import...").click();
      bot.shell("Import").setFocus();
      bot.tree().select("Other").expandNode("Other").select("Check out b2 Project/s...");
      bot.button("Next >").click();

      importShell = bot.shell(Messages.B2Wizard_1);
      importShell.activate();

      treeViewer = bot.tree();

      SWTBotText dirText = bot.textWithTooltip(Messages.B2WizardPage_17);
      dirText.setFocus();
      dirText.setText(projectPath);
   }

   @Test
   public void testButtons()
   {

      chooseB2Wizard();

      SWTBotButton selectAll = importShell.bot().button(Messages.B2WizardPage_7);
      selectAll.click();

      assertTrue(treeViewer.hasItems());
      for (SWTBotTreeItem item : treeViewer.getAllItems())
      {
         assertTrue(item.isChecked());
      }

      SWTBotButton deSelectAll = importShell.bot().button(Messages.B2WizardPage_8);
      deSelectAll.click();

      for (SWTBotTreeItem item : treeViewer.getAllItems())
      {
         assertFalse(item.isChecked());
      }

      SWTBotButton refresh = importShell.bot().button(Messages.B2WizardPage_16);
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
      chooseB2Wizard();

      if (treeViewer.hasItems())
      {
         swtBotFirstTreeItem = treeViewer.getAllItems()[0].getItems()[0];
         swtBotFirstTreeItem.check();
      }
      finish();

   }

   @Test
   public void copyProject()
   {
      chooseB2Wizard();
      if (treeViewer.hasItems())
      {
         swtBotFirstTreeItem = treeViewer.getAllItems()[0].getItems()[3];
         swtBotFirstTreeItem.check();
      }

      copyMode();

      finish();

   }

   public void finish()
   {
      SWTBotButton finish = bot.button("Finish");
      finish.click();
   }

   @Test
   public void easyCreateProject()
   {
      chooseB2Wizard();
      if (treeViewer.hasItems())
      {
         swtBotFirstTreeItem = treeViewer.getAllItems()[0].getItems()[1];
         swtBotFirstTreeItem.check();
      }
      easy = bot.buttonWithTooltip(Messages.B2WizardPage_19);
      easy.click();

   }

   @Test
   public void easyCreateProjectToExistingWS()
   {
      chooseB2Wizard();

      if (treeViewer.hasItems())
      {
         swtBotFirstTreeItem = treeViewer.getAllItems()[1].getItems()[1];
         swtBotFirstTreeItem.check();
      }
      easy = bot.buttonWithTooltip(Messages.B2WizardPage_19);
      easy.click();
   }

   @Test
   public void easyCopyProject()
   {
      chooseB2Wizard();
      if (treeViewer.hasItems())
      {
         swtBotFirstTreeItem = treeViewer.getAllItems()[0].getItems()[4];
         swtBotFirstTreeItem.check();
      }
      copyMode();
      SWTBotButton easy = bot.buttonWithTooltip(Messages.B2WizardPage_19);
      easy.click();

   }

   public void copyMode()
   {
      SWTBotCheckBox copyMode = bot.checkBox(Messages.B2WizardPage_15);
      copyMode.click();
   }

   @Test
   public void openAndCloseWorkingSetDialog()
   {
      chooseB2Wizard();
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
}
