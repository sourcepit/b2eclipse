
package org.sourcepit.b2eclipse.input.node;

import java.util.ArrayList;
import java.util.List;

public class WSNameValidator
{
   private List<String> wsNames;
   
   public WSNameValidator()
   {
      wsNames = new ArrayList<String>();
   }

   public String validate(String name)
   {
      while (wsNames.contains(name))
      {
         name = name + "_rp";
      }
      wsNames.add(name);
      return name;
   }

   public void removeFromlist(String name)
   {
      if (wsNames.contains(name))
      {
         wsNames.remove(name);
      }
   }

   public void clear()
   {
      wsNames.clear();
   }

}
