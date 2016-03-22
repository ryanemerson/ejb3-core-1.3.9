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
package org.jboss.ejb3.core.test.ejbthree2191;

import org.jboss.ejb3.annotation.Pool;

import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Stateless;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
@Stateless
@Local(FailingConstructingLocal.class)
@Pool(value="StrictMaxPool", maxSize=1, timeout=1L)
public class FailingConstructingBean implements FailingConstructingLocal
{
   @PostConstruct
   public void postConstruct()
   {
      throw new RuntimeException("I fail construction purposely");
   }

   @Override
   public void solveAllMyProblems()
   {
      // this actually never happens
      throw new RuntimeException("NYI: org.jboss.ejb3.core.test.ejbthree2191.FailingConstructingBean.solveAllMyProblems");
   }
}
