
package org.sourcepit.b2eclipse.ui.test;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.junit.After;
import org.junit.Before;
import org.sourcepit.b2eclipse.ui.B2WizardPage;

public class WizardPageOneTest
{
   B2WizardPage wizardPageOne;
   private IStructuredSelection currentSelection;

   @Before
   public void setUp() throws Exception
   {
      wizardPageOne = new B2WizardPage("Module", currentSelection);
   }

   @After
   public void tearDown() throws Exception
   {
   }

}
