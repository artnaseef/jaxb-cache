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

import com.artnaseef.jaxb.cache.testmodel1.Model1A;
import com.artnaseef.jaxb.cache.testmodel1.Model1B;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;

import static org.junit.Assert.*;

/**
 * Created by art on 8/20/18.
 */
public class JAXBCacheManagerImplTest {

  private JAXBCacheManagerImpl target = new JAXBCacheManagerImpl();

  private ClassLoader testClassLoader;
  private Map<String, Object> properties1;
  private Map<String, Object> properties2;
  private Map<String, Object> properties3;

  /**
   * Setup common test data and interactions.
   */
  @Before
  public void setupTest() throws Exception {
    this.testClassLoader =
        new URLClassLoader(new URL[]{new URL("http://www.artnaseef.com/nosuchthing")},
                           this.getClass().getClassLoader());

    this.properties1 = new HashMap<>();
    this.properties2 = new HashMap<>();
    this.properties3 = new HashMap<>();

    this.properties1.put("retainReferenceToInfo", Boolean.FALSE);
    this.properties1.put("supressAccessorWarnings", Boolean.FALSE);
    this.properties2.putAll(properties1);
    this.properties3.putAll(properties1);
    this.properties3.put("com.sun.xml.internal.bind.improvedXsiTypeHandling", Boolean.TRUE);
  }

  /**
   * Verify operation of useContext when given a class.
   */
  @Test
  public void testUseContext() throws Exception {
    //
    // Execute
    //
    JAXBContext first = this.target.useContext(Model1A.class);
    JAXBContext second = this.target.useContext(Model1A.class);
    JAXBContext third = this.target.useContext(Model1B.class);

    //
    // Verify
    //
    assertSame(first, second);
    assertNotSame(first, third);
  }

  /**
   * Verify operation of useContext when given an array of classes and properties.
   */
  @Test
  public void testUseContext1() throws Exception {
    //
    // Execute
    //

    // Two calls, exact same arguments - same context expected
    JAXBContext first = this.target.useContext(new Class[]{Model1A.class}, this.properties1);
    JAXBContext firstAgain = this.target.useContext(new Class[]{Model1A.class}, this.properties1);

    // Third call, equivalent arguments, but different property map - same context expected
    JAXBContext second = this.target.useContext(new Class[]{Model1A.class}, this.properties2);

    // Forth call with slight variation of properties - new context expected
    JAXBContext third = this.target.useContext(new Class[]{Model1A.class}, this.properties3);


    //
    // Verify
    //
    assertSame(first, firstAgain);
    assertSame(first, second);
    assertNotSame(first, third);
  }

  /**
   * Verify operation of useContext given a context path
   */
  @Test
  public void testUseContextByPath() throws Exception {
    //
    // Execute
    //
    JAXBContext first = this.target.useContext("com.artnaseef.jaxb.cache.testmodel1");
    JAXBContext second = this.target.useContext("com.artnaseef.jaxb.cache.testmodel1");
    JAXBContext third = this.target.useContext("com.artnaseef.jaxb.cache.testmodel2");


    //
    // Verify
    //
    assertSame(first, second);
    assertNotSame(first, third);
  }

  /**
   * Verify operation of useContext given a context path and class loader
   */
  @Test
  public void testUseContextByPathAndClassLoader() throws Exception {
    //
    // Execute
    //
    JAXBContext first = this.target.useContext("com.artnaseef.jaxb.cache.testmodel1", this.getClass().getClassLoader());
    JAXBContext second = this.target.useContext("com.artnaseef.jaxb.cache.testmodel1", this.getClass().getClassLoader());
    JAXBContext third = this.target.useContext("com.artnaseef.jaxb.cache.testmodel2", this.getClass().getClassLoader());
    JAXBContext forth = this.target.useContext("com.artnaseef.jaxb.cache.testmodel1", testClassLoader);


    //
    // Verify
    //
    assertSame(first, second);
    assertNotSame(first, third);
    assertNotSame(first, forth);
  }

  /**
   * Verify operation of useContext given a context path, class loader, and properties
   */
  @Test
  public void testUseContextByPathClassLoaderAndProperties() throws Exception {
    //
    // Execute
    //
    JAXBContext first = this.target.useContext("com.artnaseef.jaxb.cache.testmodel1", this.getClass().getClassLoader(),
                                               properties1);
    JAXBContext second = this.target.useContext("com.artnaseef.jaxb.cache.testmodel1", this.getClass().getClassLoader(),
                                                properties2);
    JAXBContext third = this.target.useContext("com.artnaseef.jaxb.cache.testmodel2", this.getClass().getClassLoader(),
                                               properties1);

    JAXBContext forth = this.target.useContext("com.artnaseef.jaxb.cache.testmodel1", testClassLoader, properties1);

    JAXBContext fifth = this.target.useContext("com.artnaseef.jaxb.cache.testmodel2", this.getClass().getClassLoader(),
                                               properties3);

    JAXBContext sixth= this.target.useContext("com.artnaseef.jaxb.cache.testmodel2", testClassLoader, properties3);

    //
    // Verify
    //
    assertSame(first, second);
    assertNotSame(first, third);
    assertNotSame(first, forth);
    assertNotSame(third, fifth);
    assertNotSame(third, sixth);
    assertNotSame(fifth, sixth);
  }
}