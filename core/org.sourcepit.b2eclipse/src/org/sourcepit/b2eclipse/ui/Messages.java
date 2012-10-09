/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 */

public class Messages extends NLS
{
   private static final String BUNDLE_NAME = "org.sourcepit.b2eclipse.ui.messages"; //$NON-NLS-1$
   
   public static String msgImportTitle;
   public static String msgImportHeader;
   public static String msgImportSuperscription;

   public static String msgSelectRootRbtn;
   public static String msgSelectWorkspaceRbtn;
   public static String msgSelectRootTt;
   public static String msgSelectWorkspaceTt;
   public static String msgSelectDirTitle;
   public static String msgSelectProjectTitle;
   public static String msgSelectProject;
   public static String msgBrowseBtn;


   public static String msgSelectAllBtn;
   public static String msgSelectAllTt;
   public static String msgDeselectAllBtn;
   public static String msgDeselectAllTt;
   public static String msgRefreshBtn;
   public static String msgRefreshTt;
   public static String msgEasyTt;
   
   public static String msgRestoreTt;
   public static String msgSelectDeselectTt;
   public static String msgAddNewWSTt;
   public static String msgDelWSTt;
   public static String msgDefaultWSName;
   public static String msgToggleModeTt;

   static
   {
      // initialize resource bundle
      NLS.initializeMessages(BUNDLE_NAME, Messages.class);
   }

   private Messages()
   {
   }
}
