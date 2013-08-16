/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.ui.installer;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

public final class Selections
{
   private Selections()
   {
      super();
   }

   public static StructuredSelection structuredSelection(Object... elements)
   {
      if (elements == null)
      {
         return StructuredSelection.EMPTY;
      }
      else
      {
         return new StructuredSelection(elements);
      }
   }

   public static Object getFirstElement(SelectionChangedEvent event)
   {
      return getFirstElement(event.getSelection());
   }

   public static Object getFirstElement(ISelection selection)
   {
      return ((IStructuredSelection) selection).getFirstElement();
   }

   @SuppressWarnings("unchecked")
   public static <T> T getFirstElement(SelectionChangedEvent event, Class<T> type)
   {
      return (T) getFirstElement(event.getSelection());
   }

   @SuppressWarnings("unchecked")
   public static <T> T getFirstElement(ISelection selection, Class<T> type)
   {
      return (T) ((IStructuredSelection) selection).getFirstElement();
   }
}
