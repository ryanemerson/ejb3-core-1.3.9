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
package org.jboss.ejb3.core.test.ejbthree2146.unit;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree2146.AuroraBean;
import org.jboss.ejb3.core.test.ejbthree2146.AuroraLocal;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.statistics.InvocationStatistics;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test whether we can actually get some stats.
 * This is to the contract with ejb3-metrics-deployer.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class WaiterTestCase extends AbstractEJB3TestCase
{
   private static SessionContainer container;

   @AfterClass
   public static void afterClass() throws Exception
   {
      undeployEjb(container);

      AbstractEJB3TestCase.afterClass();
   }

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();
      
      container = deploySessionEjb(AuroraBean.class);
   }

   @Test
   public void testStats() throws Exception
   {
      AuroraLocal bean = lookup("AuroraBean/local", AuroraLocal.class);

      InvocationStatistics stats = container.getInvokeStats();

      // Since we don't have a prince at hand, lets not sleep forever
      bean.sleep(100, MILLISECONDS);
      
      InvocationStatistics.TimeStatistic methodStat = stats.getStats().get("sleep");
      assertNotNull(methodStat);
      long count = methodStat.getCount();
      assertEquals(1, count);
      long maxTime = methodStat.getMaxTime();
      long minTime = methodStat.getMinTime();
      long totalTime = methodStat.getTotalTime();
      assertTrue(maxTime == minTime);
      assertTrue(minTime == totalTime);
   }
}
