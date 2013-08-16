/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.ui.installer;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class MavenChoserWizardPage extends WizardPage
{

   /**
    * Create the wizard.
    */
   public MavenChoserWizardPage()
   {
      super("wizardPage");
      setTitle("Wizard Page title");
      setDescription("Wizard Page description");
   }

   /**
    * Create contents of the wizard.
    * @param parent
    */
   public void createControl(Composite parent)
   {
      Composite container = new Composite(parent, SWT.NULL);

      setControl(container);
   }

}
