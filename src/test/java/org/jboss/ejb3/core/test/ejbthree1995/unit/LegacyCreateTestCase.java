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
package org.jboss.ejb3.core.test.ejbthree1995.unit;

import org.jboss.ejb3.annotation.impl.InitImpl;
import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree1995.LegacyGreeterBean;
import org.jboss.ejb3.core.test.ejbthree1995.LegacyGreeterHome;
import org.jboss.ejb3.core.test.ejbthree1995.LegacyGreeterLocal;
import org.jboss.ejb3.session.SessionContainer;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.Init;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

/**
 * Make sure we call ejbCreate on a session bean implementing SessionBean.
 * 
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class LegacyCreateTestCase extends AbstractEJB3TestCase
{
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();

      SessionContainer container = deploySessionEjb(LegacyGreeterBean.class);

      // normally done in Ejb3DescriptorHandler.addEjb21Annotations
      Method method = LegacyGreeterBean.class.getMethod("ejbCreate", String.class);
      container.getAnnotations().addAnnotation(method, Init.class, new InitImpl());
   }

   @Test
   public void testCreate() throws Exception
   {
      LegacyGreeterHome home = lookup("LegacyGreeterBean/localHome", LegacyGreeterHome.class);

      LegacyGreeterLocal bean = home.create("testCreate");

      String result = bean.sayHi();

      assertEquals("Hi testCreate", result);
   }
}
