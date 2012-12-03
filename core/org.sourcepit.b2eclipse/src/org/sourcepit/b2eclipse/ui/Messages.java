/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 * @author WD
 */
public class Messages extends NLS
{
   private static final String BUNDLE_NAME = "org.sourcepit.b2eclipse.ui.messages"; //$NON-NLS-1$

   public static String msgImportTitle;
   public static String msgImportHeader;
   public static String msgImportSuperscription;

   public static String msgSelectRootRbtn;
   public static String msgSelectRootTt;

   public static String msgSelectWorkspaceRbtn;
   public static String msgSelectWorkspaceTt;

   public static String msgSelectDirTitle;
   public static String msgSelectProjectTitle;
   public static String msgSelectProject;
   public static String msgBrowseBtn;

   public static String msgItemModuleAndFolder;
   public static String msgItemOnlyModule;
   public static String msgItemOnlyFolder;

   public static String msgRightHeading;
   public static String msgLeftHeading;

   public static String msgRestoreTt;
   public static String msgSelectDeselectTt;
   public static String msgAddNewWSTt;
   public static String msgDelWSTt;
   public static String msgToggleModeTt;
   public static String msgExpandAllTt;
   public static String msgAddPrefixTt;
   public static String msgToggleNameTt;

   public static String msgInDialogTitle;
   public static String msgInDialogMessage;

   public static String msgDefaultWSName;
   public static String msgTask;
   
   public static String msgModuleProject;
   public static String msgErrorOnProjectCreate;
   public static String msgErrorOnProjectCreateSolution;

   static
   {
      // initialize resource bundle
      NLS.initializeMessages(BUNDLE_NAME, Messages.class);
   }

   private Messages()
   {
   }
}
