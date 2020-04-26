/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.configurator;

import com.moandjiezana.toml.TomlWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dto.ConfigDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;

/**
 * This is used to replace configurations in deployment.toml
 */
public class DeploymentTomlConfigurator {

  private static final Log log = LogFactory.getLog(DeploymentTomlConfigurator.class);

  private DeploymentTomlConfigurator() {
  }

  /**
   * Configure deployment.toml by getting values from gateway configs
   *
   * @param deploymentTomlFilePath deployment.toml file path
   * @param configToolProperties gateway-config-tool.properties values
   * @param deploymentTomlEntries deployment.toml entries
   * @param onPremiseGatewayConfigs on-premise-gateway.toml configs
   */
  public static void setDeploymentTomlConfigurations(String deploymentTomlFilePath,
      Properties configToolProperties, Map<String, Object> deploymentTomlEntries,
      ConfigDTO onPremiseGatewayConfigs) throws OnPremiseGatewayException {
    Enumeration<String> enumeration = (Enumeration<String>) configToolProperties
        .propertyNames();
    while (enumeration.hasMoreElements()) {
      String varNameInToml = enumeration.nextElement();
      String varNameInGWConfigs = configToolProperties.getProperty(varNameInToml);
      String varValueInToml = getVariableFromConfig(onPremiseGatewayConfigs, varNameInGWConfigs);

      // varName: apim.analytics.enable, key -> apim, remainder -> analytics.enable
      Map<String, Object> tomlTable = deploymentTomlEntries;
      String remainder = varNameInToml;
      String key = null;
      boolean inserted = false;
      while (remainder != null) {
        String[] parts = remainder.split("\\.", 2);
        key = parts[0];
        if (parts.length < 2) {
          remainder = null;
        } else {
          remainder = parts[1];
        }
        if (!tomlTable.containsKey(key)) {
          // We insert new value here if key does not exist in original deployment.toml
          insert(key, remainder, tomlTable, varValueInToml);
          inserted = true;
          break;
        } else if (remainder != null) {
          // api.analytics.enable -> key = apim, remainder = analytics.enable
          tomlTable = (Map<String, Object>) tomlTable.get(key);
        } else {
          break;
        }
      }

      if (!inserted) {
        // remainder = null
        insert(key, remainder, tomlTable, varValueInToml);
      }
    }

    configureTasks(deploymentTomlEntries, onPremiseGatewayConfigs);

    TomlWriter tomlWriter = new TomlWriter();
    StringWriter stringWriter = new StringWriter();
    try {
      tomlWriter.write(deploymentTomlEntries, stringWriter);
    } catch (IOException e) {
      log.error("Error while writing object to TOML", e);
      throw new OnPremiseGatewayException("Error while writing object to TOML", e);
    }
    String deploymentTomlContent = stringWriter.toString();
    if (log.isDebugEnabled()) {
      log.debug("Updated deployment.toml: " + deploymentTomlContent);
    }

    try {
      // Backing up existing deployment.toml
      String backupFile = deploymentTomlFilePath + ".backup";
      if (!Files.exists(Paths.get(backupFile))) {
        Files.copy(Paths.get(deploymentTomlFilePath), Paths.get(backupFile));
      }
      Files.write(Paths.get(deploymentTomlFilePath), deploymentTomlContent.getBytes(),
          StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    } catch (IOException e) {
      log.error("Unable to write to deployment.toml", e);
      throw new OnPremiseGatewayException("Unable to write to deployment.toml", e);
    }
  }

  /**
   * Adding scheduled tasks to deployment.toml
   *
   * @param tomlEntries deployment.toml entries
   * @param gatewayConfigs on-premise-gateway configurations
   */
  private static void configureTasks(Map<String, Object> tomlEntries, ConfigDTO gatewayConfigs) {
    String tasksProperty = ConfigConstants.DEPLOYMENT_TOML_TASKS;
    List<Map<String, Object>> tasks = new LinkedList<Map<String, Object>>();
    tomlEntries.put(tasksProperty, tasks);

    //File Upload Task
    if (gatewayConfigs.isUsage_upload_task_enabled()) {
      Map<String, Object> task = new HashMap<>();
      String className = gatewayConfigs.getUsage_upload_file_data_upload_task_class();
      className = (className != null && !className.isEmpty()) ? className
          : ConfigConstants.DEFAULT_FILE_DATA_UPLOAD_TASK_CLASS;
      task.put(ConfigConstants.DEPLOYMENT_TOML_CLASS, className);
      task.put(ConfigConstants.DEPLOYMENT_TOML_NAME, className);
      task.put(ConfigConstants.DEPLOYMENT_TOML_CRON,
          String.valueOf(gatewayConfigs.getUsage_upload_task_cron()));
      tasks.add(task);
    }
    //File Cleanup Task
    if (gatewayConfigs.isUsage_upload_cleanup_task_enabled()) {
      Map<String, Object> task = new HashMap<>();
      String className = gatewayConfigs.getUsage_upload_cleanup_task_class();
      className = (className != null && !className.isEmpty()) ? className
          : ConfigConstants.DEFAULT_FILE_DATA_CLEANUP_TASK_CLASS;
      task.put(ConfigConstants.DEPLOYMENT_TOML_CLASS, className);
      task.put(ConfigConstants.DEPLOYMENT_TOML_NAME, className);
      task.put(ConfigConstants.DEPLOYMENT_TOML_CRON,
          String.valueOf(gatewayConfigs.getUsage_upload_cleanup_task_cron()));
      tasks.add(task);
    }
    //Throttling Synchronization Task
    if (gatewayConfigs.isThrottling_synchronization_task_enabled()) {
      Map<String, Object> task = new HashMap<>();
      String className = gatewayConfigs.getThrottling_synchronization_task_class();
      className = (className != null && !className.isEmpty()) ? className
          : ConfigConstants.DEFAULT_THROTTLING_SYNC_TASK_CLASS;
      task.put(ConfigConstants.DEPLOYMENT_TOML_CLASS, className);
      task.put(ConfigConstants.DEPLOYMENT_TOML_NAME, className);
      task.put(ConfigConstants.DEPLOYMENT_TOML_CRON,
          String.valueOf(gatewayConfigs.getThrottling_synchronization_task_cron()));
      tasks.add(task);
    }
    //API Update Task
    if (gatewayConfigs.isApi_update_task_enabled()) {
      Map<String, Object> task = new HashMap<>();
      String className = gatewayConfigs.getApi_update_task_class();
      className = (className != null && !className.isEmpty()) ? className
          : ConfigConstants.DEFAULT_API_UPDATE_TASK_CLASS;
      task.put(ConfigConstants.DEPLOYMENT_TOML_CLASS, className);
      task.put(ConfigConstants.DEPLOYMENT_TOML_NAME, className);
      task.put(ConfigConstants.DEPLOYMENT_TOML_CRON,
          String.valueOf(gatewayConfigs.getApi_update_task_cron()));
      tasks.add(task);
    }
  }

  /**
   * Get given variable values from ConfigDTO
   *
   * @param configDTO ConfigDTO obj
   * @param varName name of the variable
   * @return value of the given variable
   */
  private static String getVariableFromConfig(ConfigDTO configDTO, String varName)
      throws OnPremiseGatewayException {
    Method methodToFind = null;
    String postFix = varName.substring(0, 1).toUpperCase()
        .concat(varName.substring(1).toLowerCase());
    String replaceKeyGetMethod = "get".concat(postFix);
    try {
      methodToFind = ConfigDTO.class.getMethod(replaceKeyGetMethod);
    } catch (NoSuchMethodException e) {
      String replaceKeyIsMethod = "is".concat(postFix);
      try {
        methodToFind = ConfigDTO.class.getMethod(replaceKeyIsMethod);
      } catch (NoSuchMethodException e2) {
        log.error("No such method exception for the method " + replaceKeyGetMethod + " or " +
            replaceKeyIsMethod, e);
        throw new OnPremiseGatewayException("Method to extract variable from ConfigDTO not found.",
            e);
      }
    }

    try {
      return methodToFind.invoke(configDTO).toString();
    } catch (IllegalAccessException | InvocationTargetException e) {
      log.error("Exception occurred while invoking the method " + methodToFind, e);
      throw new OnPremiseGatewayException(
          "Exception occurred while invoking the method " + methodToFind, e);
    }
  }

  /**
   * Insert given value to the entries under provided key. Example: apim.analytics.enable
   *
   * @param key Ex: apim
   * @param remainder Ex: analytics.enable
   * @param entries Map of entries from deployment.toml
   * @param value Ex: true
   */
  private static void insert(String key, String remainder, Map<String, Object> entries,
      Object value) {
    if (remainder == null) {
      // Leaf node. Hence inserting value
      entries.put(key, value);
    } else {
      // Intermediate level where key -> apim, remainder -> analytics.enable
      // Add new map under "apim" and recursively call with key -> analytics, remainder -> enable
      HashMap<String, Object> map = new HashMap<>();
      entries.put(key, map);

      String[] parts = remainder.split("\\.", 2);
      key = parts[0];
      if (parts.length < 2) {
        remainder = null;
      } else {
        remainder = parts[1];
      }
      insert(key, remainder, map, value);
    }
  }
}
