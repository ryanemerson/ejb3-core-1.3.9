/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.core.test.ejbthree2022.unit;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.Ejb3DescriptorHandler;
import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.common.MockEjb3Deployment;
import org.jboss.ejb3.core.test.ejbthree2022.UnSpecifiedSessionTypeBean;
import org.jboss.ejb3.test.cachepassivation.MockDeploymentUnit;
import org.jboss.metadata.ejb.jboss.JBossAssemblyDescriptorMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.junit.Test;

/**
 * Ejb3DescriptorHandlerTestCase
 * 
 * Tests the fix for https://jira.jboss.org/jira/browse/EJBTHREE-2022
 * 
 * The bug was incorrectly creating a StatefulContainer for any non-stateless session beans.
 * The assumption that any non-stateless session bean is a stateful bean is no longer valid
 * in EJB3.1 with the introduction of Singleton session bean.
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class Ejb3DescriptorHandlerTestCase extends AbstractEJB3TestCase
{

   /**
    *  Tests that the {@link Ejb3DescriptorHandler} does not create a stateful container
    *  for a non-stateless session bean.
    *  
    *  @see https://jira.jboss.org/jira/browse/EJBTHREE-2022
    */
   @Test
   public void testNonStatelessSessionBean() throws Exception
   {
      // create dummy metadata
      JBossMetaData metaData = new JBossMetaData();
      JBossAssemblyDescriptorMetaData assemblyDescriptor = new JBossAssemblyDescriptorMetaData();
      metaData.setAssemblyDescriptor(assemblyDescriptor);

      JBossEnterpriseBeansMetaData enterpriseBeans = new JBossEnterpriseBeansMetaData();
      metaData.setEnterpriseBeans(enterpriseBeans);
      // create a session bean without setting any SessionType (i.e. session type unknown)
      JBossSessionBeanMetaData sessionBeanMetaData = new JBossSessionBeanMetaData();
      sessionBeanMetaData.setEnterpriseBeansMetaData(enterpriseBeans);
      sessionBeanMetaData.setEjbClass(UnSpecifiedSessionTypeBean.class.getName());
      sessionBeanMetaData.setEjbName(UnSpecifiedSessionTypeBean.class.getSimpleName());
      enterpriseBeans.add(sessionBeanMetaData);

      MockEjb3Deployment deployment = new MockEjb3Deployment(new MockDeploymentUnit());
      Ejb3DescriptorHandler handler = new Ejb3DescriptorHandler(deployment, metaData);
      // get containers
      List<Container> containers = handler.getContainers(deployment, new HashMap<String, Container>());

      assertTrue("Unexpectedly found a container from metadata", containers.isEmpty());

   }
}
