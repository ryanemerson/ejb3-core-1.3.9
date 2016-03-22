/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.ejb3.core.test.jbpapp7685.unit;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.Collection;

import javax.ejb.EJBAccessException;
import javax.ejb.Timer;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.jbpapp7685.FakedTimerBean;
import org.jboss.ejb3.core.test.jbpapp7685.FakedTimerBean2;
import org.jboss.ejb3.core.test.jbpapp7685.SecuredBean;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.stateless.StatelessContainer;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class RunAsTimeoutMethodTestCase extends AbstractEJB3TestCase {
    @BeforeClass
    public static void beforeClass() throws Exception {
        AbstractEJB3TestCase.beforeClass();

        deploy("securitymanager-beans.xml");

        final Collection<SessionContainer> containers = deploySessionEjbs(SecuredBean.class, FakedTimerBean.class, FakedTimerBean2.class);
        for (SessionContainer container : containers) {
            container.setJaccContextId("test");
        }
    }

    @Test
    public void testNotAllowed() throws Exception {
        final Timer timer = mock(Timer.class);
        try {
            ((StatelessContainer) container("jboss.j2ee:service=EJB3,name=FakedTimerBean2")).callTimeout(timer);
            fail("Should not have been allowed");
        } catch (EJBAccessException e) {
            // good
        }
    }

    @Test
    public void testTimeout() throws Exception {
        final Timer timer = mock(Timer.class);
        ((StatelessContainer) container("jboss.j2ee:service=EJB3,name=FakedTimerBean")).callTimeout(timer);
    }
}
