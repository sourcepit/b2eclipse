
package org.sourcepit.b2eclipse.input.node;

import java.util.ArrayList;
import java.util.List;

public class WSNameValidator
{
   private static List<String> wsNames = new ArrayList<String>();

   public static String validate(String name)
   {
      while (wsNames.contains(name))
      {
         name = name+"_rp";
      }
      wsNames.add(name);
      return name;
   }

   public static void removeFromlist(String name)
   {
      if (wsNames.contains(name))
      {
         wsNames.remove(name);
      }
   }

}
