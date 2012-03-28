package org.sourcepit.b2eclipse.ui.test;

import junit.textui.TestRunner;

import org.junit.After;
import org.junit.Before;
import org.sourcepit.b2eclipse.ui.B2Wizard;

public class B2WizardTest extends TestRunner
{

   B2Wizard b2Wizard;
   
   @Before
   public void setUp() throws Exception
   {
      b2Wizard = new B2Wizard();
   }

   @After
   public void tearDown() throws Exception
   {
      b2Wizard.dispose();
   }


}
