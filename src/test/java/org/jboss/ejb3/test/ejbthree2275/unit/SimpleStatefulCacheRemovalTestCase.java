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
package org.jboss.ejb3.test.ejbthree2275.unit;

import java.util.Date;

import javax.ejb.NoSuchEJBException;

import org.jboss.ejb3.cache.simple.SimpleStatefulCache;
import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.stateful.StatefulBeanContext;
import org.jboss.ejb3.test.ejbthree2275.SimpleSFSB;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * SimpleStatefulCacheRemovalTest
 * 
 * Tests the {@link SimpleStatefulCache}
 * <p>
 *  More specifically, tests the fix for https://jira.jboss.org/jira/browse/EJBTHREE-2030, where
 *  a remove() on the cache was *not* checking for passivated sessions.
 * </p>
 * @author Brad Maxwell
 * @version $Revision: $
 */
public class SimpleStatefulCacheRemovalTestCase extends AbstractEJB3TestCase
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(SimpleStatefulCacheRemovalTestCase.class);

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
   public void testRemovalAfterRemovalTimeout() throws Exception
   {
      // create a session
      StatefulBeanContext sfsbContext = cache.create();
      Object sessionId = sfsbContext.getId();
      logger.info("Created StatefulBeanContext with session id " + sessionId);
      // mark it as *not in use* so that it can be passivated
      sfsbContext.setInUse(true);
      // set last used to now
      sfsbContext.lastUsed = new Date().getTime();
      
      // wait for 3 (or more seconds for removal to happen)
      // The SimpleSFSB is configured for a removalTimeoutSeconds of 1 seconds (so that it will be 
      // removed after 1 seconds, idleTimeout is set to 10)
      logger.info("Sleeping for 3 seconds to allow removal thread to remove the bean context with id "
            + sessionId);
      Thread.sleep(3000);
      
      if(! sfsbContext.isRemoved())
      {
    	  Assert.fail("SFSB context should be removed now EJBTHREE-2275");
           
    	  logger.info("Clean up - trying to remove session " + sessionId + " from cache");
    	  cache.remove(sessionId);
    	  logger.info("Successfully removed " + sessionId + " from cache");
      }
   }
}
