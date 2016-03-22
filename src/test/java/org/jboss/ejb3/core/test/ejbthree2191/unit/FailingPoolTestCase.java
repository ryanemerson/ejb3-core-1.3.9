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
package org.jboss.ejb3.core.test.ejbthree2191.unit;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree2191.FailingConstructingBean;
import org.jboss.ejb3.core.test.ejbthree2191.FailingConstructingLocal;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.EJBException;

import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class FailingPoolTestCase extends AbstractEJB3TestCase
{
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();
      
      deploySessionEjb(FailingConstructingBean.class);
   }

   @Test
   public void testConstruction() throws Exception
   {
      FailingConstructingLocal bean = lookup("FailingConstructingBean/local", FailingConstructingLocal.class);
      // the first try will succeed
      // the second will fail to acquire a permit, if the bug is present
      for(int i = 0; i < 2; i++)
      {
         try
         {
            bean.solveAllMyProblems();
            fail("I doubt that all my problems are solved.");
         }
         catch(EJBException e)
         {
            assertCause(new RuntimeException("I fail construction purposely"), e);
         }
      }
   }
}
