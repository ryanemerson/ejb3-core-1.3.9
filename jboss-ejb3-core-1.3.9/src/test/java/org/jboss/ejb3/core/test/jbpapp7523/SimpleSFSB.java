package org.jboss.ejb3.core.test.jbpapp7523;

import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.CacheConfig;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
@Stateful
@CacheConfig(maxSize = 1) // passivate on second creation
public class SimpleSFSB {
}
