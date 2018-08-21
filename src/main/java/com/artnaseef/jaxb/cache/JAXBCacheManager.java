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

package com.artnaseef.jaxb.cache;

import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Manager of the JAXB Context cache.  JAXB contexts created here are always cached, and equivalent requests for
 * contexts, using the same method signature and equivalent arguments, return the same JAXB context, which is thread
 * safe.
 *
 * Created by art on 8/20/18.
 */
public interface JAXBCacheManager {

  /**
   * Retrieve the JAXBContext for the given list of classes.  The context is created if a matching context was not
   * previously created and stored in the cache.  Existing contexts are only considered to match if they were created
   * with the same list of classes, using this same method signature.
   *
   * @param classes list of classes contained in the JAXB context that will be returned.
   * @return JAXB Context for the list of classes.
   * @throws JAXBException
   */
  JAXBContext useContext(Class... classes) throws JAXBException;

  /**
   * Retrieve the JAXBContext for the given list of classes, with the specified set of properties.  The context is
   * created if a matching one was not previously created and stored in the cache.  In order to match, an existing
   * context must have the same classes, property names, and property values.  Default property values are not applied
   * in the comparison.  In addition, only contexts created with this method signature will match.
   *
   * @param classes list of classes contained in the JAXB context that will be returned.
   * @param properties set of properties to configure on the context.
   * @return JAXB Context for the list of classes, with the given property settings.
   * @throws JAXBException
   */
  JAXBContext useContext(Class[] classes, Map<String, ?> properties) throws JAXBException;

  /**
   * Retrieve the JAXBContext given the context path (i.e. package name).  The context is created if a matching one was
   * not previously created and stored in the cache.  In order to match, an existing context must have the same context
   * path, and must have been created with the same method signature.
   *
   * @param contextPath package name within which the JAXB context will access classes.
   * @return JAXB context containing the classes declared within the package (following the JAXBContext.newInstance
   * rules).
   * @throws JAXBException
   */
  JAXBContext useContext(String contextPath) throws JAXBException;

  /**
   * Retrieve the JAXBContext given the context path (i.e. package name) and class loader.  The context is created if a
   * matching one was not previously created and stored in the cache.  In order to match, an existing context must have
   * the same context path and class loader, and must have been created with the same method signature.
   *
   * @param contextPath package name within which the JAXB context will access classes.
   * @param classLoader class loader used to load classes and resources within the named package.
   * @return JAXB context containing the classes declared within the package (following the JAXBContext.newInstance
   * rules), and using the given class loader to locate and load classes + resources.
   * @throws JAXBException
   */
  JAXBContext useContext(String contextPath, ClassLoader classLoader) throws JAXBException;


  /**
   * Retrieve the JAXBContext given the context path (i.e. package name), class loader, and properties.  The context is
   * created if a matching one was not previously created and stored in the cache.  In order to match, an existing
   * context must have the same context path, class loader, property names, and property values; in addition, it must
   * have been created with the same method signature.
   *
   * @param contextPath package name within which the JAXB context will access classes.
   * @param classLoader class loader used to load classes and resources within the named package.
   * @param properties set of properties to configure on the context.
   * @return JAXB context containing the classes declared within the package (following the JAXBContext.newInstance
   * rules), and using the given class loader to locate and load classes + resources.
   * @throws JAXBException
   */
  JAXBContext useContext(String contextPath, ClassLoader classLoader, Map<String, ?> properties) throws JAXBException;

}
