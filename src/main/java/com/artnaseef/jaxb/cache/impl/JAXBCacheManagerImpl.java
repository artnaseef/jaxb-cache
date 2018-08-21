/*
 * Copyright (c) 2018 Arthur Naseef
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.artnaseef.jaxb.cache.impl;

import com.artnaseef.jaxb.cache.JAXBCacheManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Implementation of the JAXB Cache Manager.
 *
 * WARNING: cached contexts are never released by this implementation.  This is intended for long-lived JAXB contexts,
 * not for large numbers of short-lived contexts.
 *
 * @see com.artnaseef.jaxb.cache.JAXBCacheManager
 *
 * Created by art on 8/20/18.
 */
public class JAXBCacheManagerImpl implements JAXBCacheManager {

  private final Map<Object, JAXBContext> cache = new HashMap<>();
  private final Map<Object, Object> cacheSyncs = new HashMap<>();
  private final Object sync = new Object();

  @Override
  public JAXBContext useContext(Class... classes) throws JAXBException {
    JAXBContext result = this.lookupContext((Class[]) classes);

    if (result == null) {
      result = this.createContext(() -> JAXBContext.newInstance(classes), classes);
    }

    return result;
  }

  @Override
  public JAXBContext useContext(Class[] classes, Map<String, ?> properties) throws JAXBException {
    JAXBContext result = this.lookupContext(classes, properties);

    if (result == null) {
      result = this.createContext(() -> JAXBContext.newInstance(classes, properties), classes, properties);
    }

    return result;
  }

  @Override
  public JAXBContext useContext(String contextPath) throws JAXBException {
    JAXBContext result = this.lookupContext(contextPath);

    if (result == null) {
      result = this.createContext(() -> JAXBContext.newInstance(contextPath), contextPath);
    }

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JAXBContext useContext(String contextPath, ClassLoader classLoader) throws JAXBException {
    JAXBContext result = this.lookupContext(contextPath, classLoader);

    if (result == null) {
      result = this.createContext(() -> JAXBContext.newInstance(contextPath, classLoader), contextPath, classLoader);
    }

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JAXBContext useContext(String contextPath, ClassLoader classLoader, Map<String, ?> properties)
      throws JAXBException {
    JAXBContext result = this.lookupContext(contextPath, classLoader, properties);

    if (result == null) {
      result =
          this.createContext(() -> JAXBContext.newInstance(contextPath, classLoader, properties), contextPath,
                             classLoader, properties);
    }

    return result;
  }


//========================================
// Thread-Safe Operation
//----------------------------------------

  /**
   * Find the existing JAXB context given the arguments used to create the context.
   *
   * @param contextArgs arguments used to create the JAXB context.
   * @return the existing JAXB context, or null if none is currently in the cache.
   */
  private JAXBContext lookupContext(Object... contextArgs) {
    Object key = this.convertArgsToKey(contextArgs);

    synchronized (this.sync) {
      return this.cache.get(key);
    }
  }

  /**
   * Create a new context, in a thread-safe manner in order to avoid duplicates caused by race conditions.
   *
   * @param alloc callback which calls the actual JAXBContext.newInstance() method to allocate the context.
   * @param contextArgs arguments to the callback, which also uniquely identify the JAXB context.
   * @return the new context, if created, or the existing one if another thread wins a race condition and creates the
   * context first.
   * @throws JAXBException
   */
  private JAXBContext createContext(JAXBAllocFunction alloc, Object... contextArgs) throws JAXBException {
    Object cacheSync;
    JAXBContext result;

    Object key = this.convertArgsToKey(contextArgs);
    synchronized (this.sync) {
      // Obtain the synchronization object just for this JAXB context, so we can avoid slowdown when multiple, separate
      //  JAXBContexts are started simultaneously, while still preventing the same context from being created more than
      //  once.
      cacheSync = this.cacheSyncs.get(key);

      if (cacheSync == null) {
        cacheSync = new Object();
        this.cacheSyncs.put(key, cacheSync);
      }
    }

    synchronized (cacheSync) {
      // Make sure this thread is not the loser of a race condition.
      result = this.cache.get(key);
      if (result == null) {
        result = alloc.exec();
        this.cache.put(key, result);
      }
    }

    return result;
  }

  /**
   * Convert the arguments given into a list that can be compared with other, distinct lists containing equivalent
   * contents.  The resulting list can be used as a key for a Map.
   *
   * @param args
   * @return
   */
  private Object convertArgsToKey(Object... args) {
    List<Object> result = new LinkedList<>();
    for (Object oneArg: args) {
      //
      // If the argument itself is an array (e.g. varargs list of classes), convert it to a list as well; otherwise,
      //  two such arrays would fail to by considered equal even when their contents are exactly the same.
      //
      if ((oneArg != null) && (oneArg instanceof Object[])) {
        result.add(Arrays.asList((Object[]) oneArg));
      } else {
        result.add(oneArg);
      }
    }

    return result;
  }

  /**
   * Interface for Lambda use of the actual JAXBContext.newInstance() callbacks.
   */
  private interface JAXBAllocFunction {
    JAXBContext exec() throws JAXBException;
  }
}
