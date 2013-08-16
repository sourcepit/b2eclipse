/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.b2eclipse.ui.installer;

import static org.sourcepit.common.utils.lang.Exceptions.pipe;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.sourcepit.common.utils.lang.PipedException;

public final class RunnableContexts
{
   private RunnableContexts()
   {
      super();
   }

   public static void run(IRunnableContext runnableContext, boolean fork, boolean cancelable,
      IRunnableWithProgress runnable) throws PipedException
   {
      try
      {
         try
         {
            runnableContext.run(fork, cancelable, runnable);
         }
         catch (InvocationTargetException e)
         {
            throw e.getCause();
         }
      }
      catch (Exception e)
      {
         throw pipe(e);
      }
      catch (Error e)
      {
         throw pipe(e);
      }
      catch (Throwable e)
      {
         throw pipe(new IllegalStateException(e));
      }
   }
}
