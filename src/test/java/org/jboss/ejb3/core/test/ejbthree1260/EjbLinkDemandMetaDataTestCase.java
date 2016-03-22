/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2010, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ejb3.core.test.ejbthree1260;

import org.jboss.beans.metadata.spi.MetaDataVisitor;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.ejb3.dependency.EjbLinkDemandMetaData;
import org.jboss.ejb3.javaee.JavaEEComponent;
import org.jboss.ejb3.javaee.JavaEEComponentHelper;
import org.jboss.ejb3.javaee.JavaEEModule;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Note that this test does not test the speed of EjbLinkDemandMetaData, only
 * its correctness.
 * 
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class EjbLinkDemandMetaDataTestCase
{
   private DependencyItem createDependencyItem() throws MalformedObjectNameException
   {
      JavaEEComponent component = mock(JavaEEComponent.class);
      String ejbName = "Test";
      JavaEEModule module = mock(JavaEEModule.class);
      when(module.getApplication()).thenReturn(null);
      String objectName = JavaEEComponentHelper.createObjectName(module, null, ejbName);
      when(component.createObjectName(null, ejbName)).thenReturn(objectName);

      EjbLinkDemandMetaData demandMetaData = new EjbLinkDemandMetaData(component, ejbName);

      assertEquals(new ObjectName("jboss.j2ee:name=Test,service=EJB3,*"), demandMetaData.getDemand());

      MetaDataVisitor visitor = mock(MetaDataVisitor.class);
      KernelControllerContext context = mock(KernelControllerContext.class);
      when(visitor.getControllerContext()).thenReturn(context);

      demandMetaData.initialVisit(visitor);

      ArgumentCaptor<DependencyItem> argument = ArgumentCaptor.forClass(DependencyItem.class);
      verify(visitor).addDependency(argument.capture());
      return argument.getValue();
   }

   @Test
   public void testNegative() throws Exception
   {
      DependencyItem dependencyItem = createDependencyItem();
      
      JavaEEModule module = mock(JavaEEModule.class);
      when(module.getApplication()).thenReturn(null);
      ControllerContext installedContext = mock(ControllerContext.class);
      String name = JavaEEComponentHelper.createObjectName(module, "SomeUnit", "Test2");
      when(installedContext.getName()).thenReturn(name);
      Set<ControllerContext> installedContexts = new HashSet<ControllerContext>();
      installedContexts.add(installedContext);
      Controller controller = mock(Controller.class);
      when(controller.getContextsByState(ControllerState.INSTALLED)).thenReturn(installedContexts);

      boolean resolved = dependencyItem.resolve(controller);

      assertFalse(resolved);
      assertFalse(dependencyItem.isResolved());
   }
   
   @Test
   public void testPositive() throws Exception
   {
      DependencyItem dependencyItem = createDependencyItem();

      JavaEEModule module = mock(JavaEEModule.class);
      when(module.getApplication()).thenReturn(null);
      ControllerContext installedContext = mock(ControllerContext.class);
      String name = JavaEEComponentHelper.createObjectName(module, "SomeUnit", "Test");
      when(installedContext.getName()).thenReturn(name);
      Set<ControllerContext> installedContexts = new HashSet<ControllerContext>();
      installedContexts.add(installedContext);
      Controller controller = mock(Controller.class);
      when(controller.getContextsByState(ControllerState.INSTALLED)).thenReturn(installedContexts);
      
      boolean resolved = dependencyItem.resolve(controller);

      assertTrue(resolved);
      assertTrue(dependencyItem.isResolved());
   }
}
