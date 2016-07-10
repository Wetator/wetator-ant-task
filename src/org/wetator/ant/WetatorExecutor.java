/*
 * Copyright (c) 2008-2016 wetator.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wetator.ant;

import java.io.File;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wetator.core.WetatorConfiguration;
import org.wetator.core.WetatorEngine;
import org.wetator.exception.InvalidInputException;

/**
 * Separate class that runs WETATOR.
 * This class is the entry point into our own class loader.
 *
 * @author rbri
 */
public class WetatorExecutor {
  private static final Log LOG = LogFactory.getLog(WetatorExecutor.class);

  private File baseDir;
  private String config;
  private final File filesBaseDir;
  private final String[] listOfFiles;
  private final Map<String, String> projectProperties;
  private final Map<String, String> properties;
  private final Writer writer;

  /**
   * Ctor.
   * We have only basic types here because this is called from a different class loader.
   *
   * @param aBaseDir the base dir
   * @param aConfig the wetator config
   * @param aFilesBaseDir the base dir for the files
   * @param aListOfFiles the list of wetator test cases
   * @param aProjectProperties the project properties
   * @param aProperties the properties provided by the ant task
   * @param aWriter the writer for the log output
   */
  public WetatorExecutor(final File aBaseDir, final String aConfig, final File aFilesBaseDir, String[] aListOfFiles,
      final Map<String, String> aProjectProperties, final Map<String, String> aProperties, final Writer aWriter) {
    baseDir = aBaseDir;
    config = aConfig;
    filesBaseDir = aFilesBaseDir;
    listOfFiles = aListOfFiles;
    projectProperties = aProjectProperties;
    properties = aProperties;
    writer = aWriter;
  }

  /**
   * Run the wetator tests.
   *
   * @throws InvalidInputException
   */
  public long[] runWetator() throws InvalidInputException {
    final WetatorEngine tmpWetatorEngine = new WetatorEngine();
    try {
      // configuration is relative to the base dir of the project
      final File tmpConfigFile = new File(baseDir, config);
      tmpWetatorEngine.setConfigFileName(tmpConfigFile.getAbsolutePath());

      final Map<String, String> tmpOurProperties = getPropertiesFromAnt();
      tmpWetatorEngine.setExternalProperties(tmpOurProperties);
      final AntOutProgressListener tmpListener = new AntOutProgressListener(writer);
      tmpWetatorEngine.addProgressListener(tmpListener);
      tmpWetatorEngine.init();

      for (int i = 0; i < listOfFiles.length; i++) {
        final String tmpFileName = listOfFiles[i];
        tmpWetatorEngine.addTestCase(tmpFileName, new File(filesBaseDir, tmpFileName));
      }

      tmpWetatorEngine.executeTests();

      // 0 error count
      // 1 failure count
      // 2 ignored count
      // 3 test run error count
      // 4 test run ignored count
      long[] tmpResult = new long[] { tmpListener.getErrorCount(), tmpListener.getFailureCount(),
          tmpListener.getIgnoredCount(), tmpListener.getTestRunErrorCount(), tmpListener.getTestRunIgnoredCout() };
      return tmpResult;
    } finally {
      tmpWetatorEngine.shutdown();
    }
  }

  /**
   * Reads and returns the properties form ant project and from wetator task.
   *
   * @return a map with properties
   */
  protected Map<String, String> getPropertiesFromAnt() {
    final Map<String, String> tmpOurProperties = new HashMap<String, String>();

    // read the properties from project
    for (final Entry<String, String> tmpEntry : projectProperties.entrySet()) {
      if (tmpEntry.getKey().startsWith(WetatorConfiguration.VARIABLE_PREFIX + WetatorConfiguration.SECRET_PREFIX)) {
        tmpOurProperties.put(tmpEntry.getKey(), tmpEntry.getValue());
        LOG.info("set property '" + tmpEntry.getKey() + "' to '****' (from project)");
      } else if (tmpEntry.getKey().startsWith(WetatorConfiguration.PROPERTY_PREFIX)
          || tmpEntry.getKey().startsWith(WetatorConfiguration.VARIABLE_PREFIX)) {
        tmpOurProperties.put(tmpEntry.getKey(), tmpEntry.getValue());
        LOG.info("set property '" + tmpEntry.getKey() + "' to '" + tmpEntry.getValue() + "' (from project)");
      }
    }

    // read the properties from property sets
    for (final Entry<String, String> tmpEntry : properties.entrySet()) {
      final String tmpName = tmpEntry.getKey();
      if (tmpName != null) {
        if (tmpName.startsWith(WetatorConfiguration.VARIABLE_PREFIX + WetatorConfiguration.SECRET_PREFIX)) {
          tmpOurProperties.put(tmpName, tmpEntry.getValue());
          LOG.info("set property '" + tmpName + "' to '****'");
        } else if (tmpName.startsWith(WetatorConfiguration.PROPERTY_PREFIX)
            || tmpName.startsWith(WetatorConfiguration.VARIABLE_PREFIX)) {
          tmpOurProperties.put(tmpName, tmpEntry.getValue());
          LOG.info("set property '" + tmpName + "' to '" + tmpEntry.getValue() + "'");
        }
      }
    }

    return tmpOurProperties;
  }
}
