package org.sourcepit.b2eclipse.provider.test;

import static org.junit.Assert.*;


import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sourcepit.b2eclipse.provider.TreeContentProvider;

public class TreeContentProviderTest
{
   
   TreeContentProvider tcp;

   @Before
   public void setUp() throws Exception
   {
     tcp = new TreeContentProvider();
   }

   @After
   public void tearDown() throws Exception
   {
   }

   @Test
   public void testGetElements(){
      
      File[] testProjects = new File[3];
      String userPath =  System.getProperty("user.home");
      testProjects[0] = new File(userPath +"\\Desktop\\b2-examples-0.1.0-alpha-2\\example-modules\\structured-module\\doc\\org.sourcepit.b2.examples.structured.module.doc\\.project");
      testProjects[1] = new File(userPath +"\\Desktop\\b2-examples-0.1.0-alpha-2\\example-modules\\structured-module\\plugins\\org.sourcepit.b2.examples.structured.module\\.project");
      testProjects[2] = new File(userPath +"\\Desktop\\b2-examples-0.1.0-alpha-2\\example-modules\\structured-module\\tests\\org.sourcepit.b2.examples.structured.module.tests\\.project");

      assertArrayEquals(tcp.getElements(new File("C:\\Users\\imm1199\\Desktop\\b2-examples-0.1.0-alpha-2\\example-modules\\structured-module")),testProjects);

      TreeContentProvider.clearArrayList();

      

   }
  

}
