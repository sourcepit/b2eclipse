package org.sourcepit.b2eclipse.ui.test;

import org.junit.After;
import org.junit.Before;
import org.sourcepit.b2eclipse.ui.WizardPageOne;

public class WizardPageOneTest
{
   WizardPageOne wizardPageOne;

   @Before
   public void setUp() throws Exception
   {
      wizardPageOne = new WizardPageOne("Module");
      
      
   }

   @After
   public void tearDown() throws Exception
   {
   }

   
}
