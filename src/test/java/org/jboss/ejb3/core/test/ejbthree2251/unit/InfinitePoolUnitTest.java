/*
* JBoss, Home of Professional Open Source
* Copyright 2011, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.core.test.ejbthree2251.unit;

import junit.framework.Assert;

import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.InfinitePool;
import org.jboss.ejb3.cache.simple.SimpleStatefulCache;
import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree2251.SimpleSFSB;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.stateful.StatefulBeanContext;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * InfinitePoolUnitTest
 * 
 * Tests the {@link InfinitePool}
 * <p>
 *  More specifically, tests the fix for https://jira.jboss.org/jira/browse/EJBTHREE-2251, where
 *  discard() on the InifinitePool does not release the reference.
 * </p>
 * @author Brad Maxwell
 * @version $Revision: $
 */
public class InfinitePoolUnitTest extends AbstractEJB3TestCase
{
   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(InfinitePoolUnitTest.class);

   private static InfinitePool infinitePool;

   /**
    * Container for {@link SimpleSFSB}
    */
   private static SessionContainer container;

   /**
    * A {@link SimpleStatefulCache} for the {@link #container}
    */
   private static SimpleStatefulCache cache;

   /**
    * Deploy the bean, create the container and init/start the cache
    * @throws Exception
    */
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();

      // Deploy the test SLSB
      // EJBTHREE-2275 - test ejb3 with removal timeout < passivation timeout
      container = deploySessionEjb(SimpleSFSB.class);
      cache = new SimpleStatefulCache();
      cache.initialize(container);
      cache.start();

      infinitePool = new InfinitePool();

      // maxsize and timeout are not used in AbstractPool
      infinitePool.initialize(container, 0, 0);
   }

   /**
    * Cleanup
    * @throws Exception
    */
   @AfterClass
   public static void afterClass() throws Exception
   {
      cache.stop();

      // Undeploy the test SLSB
      undeployEjb(container);

      infinitePool.destroy();
      infinitePool = null;

      AbstractEJB3TestCase.afterClass();
   }

   /**
    * Tests that when a {@link StatefulBeanContext} is passivated and later a remove() for that
    * context is invoked, the {@link SimpleStatefulCache} first activates the session and then
    * successfully removes it
    * 
    * @throws Exception
    */
   @Test
   public void testDiscard() throws Exception
   {
      logger.info("Getting an bean context from the InfinitePool, currentSize: " + infinitePool.getCurrentSize());
      BeanContext ctx = infinitePool.get();

      logger.info("currentSize: " + infinitePool.getCurrentSize() + " after get()");

      int sizeBeforeDiscard = infinitePool.getCurrentSize();

      logger.info("Trying to discard the bean contenxt");
      infinitePool.discard(ctx);

      if (infinitePool.getCurrentSize() != (sizeBeforeDiscard - 1))
      {
         Assert.fail("InfinitePool currentSize is: " + infinitePool.getCurrentSize() + " it should be: "
               + (sizeBeforeDiscard - 1));
      }
   }
}
