/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.gwt.dev.codeserver;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.json.JsonArray;
import com.google.gwt.dev.json.JsonObject;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.google.gwt.thirdparty.guava.common.collect.Maps;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * An in-memory table of all the outboxes available on this code server. The {@link WebServer}
 * uses this directory to find the {@link Outbox} associated with a URL and to list all the
 * modules on the front page.
 */
class OutboxTable {
  private final Options options;

  /**
   * A map from outbox id (an opaque string) to its outbox.
   */
  private final Map<String, Outbox> outboxes = Maps.newHashMap();

  OutboxTable(Options options) {
    this.options = options;
  }

  /**
   * Adds a {@link Outbox} to the table.
   */
  void addOutbox(Outbox outbox) {
    outboxes.put(outbox.getId(), outbox);
  }

  /**
   * Retrieves an {@link Outbox} corresponding to a given module name.
   * This should be the module name after renaming.
   * TODO: callers should use an Outbox id instead.
   */
  Outbox findByOutputModuleName(String moduleName) {
    for (Outbox box : outboxes.values()) {
      if (box.getOutputModuleName().equals(moduleName)) {
        return box;
      }
    }
    return null;
  }

  /**
   * Returns the list of known module names (after renaming).
   */
  Collection<String> getOutputModuleNames() {
    List<String> result = Lists.newArrayList();
    for (Outbox box : outboxes.values()) {
      result.add(box.getOutputModuleName());
    }
    return result;
  }

  void defaultCompileAll(boolean noPrecompile, TreeLogger logger) throws UnableToCompleteException {
    for (Outbox box: outboxes.values()) {
      box.maybePrecompile(noPrecompile, logger);
    }
  }

  /**
   * Returns a configuration object containing the names of all the modules
   * and warnings to display to the user.
   */
  JsonObject getConfig() {
    JsonObject config = JsonObject.create();
    JsonArray moduleNames = new JsonArray();
    for (String module : getOutputModuleNames()) {
      moduleNames.add(module);
    }
    config.put("moduleNames", moduleNames);
    config.put("warnings", options.getWarningsAsJson());
    return config;
  }
}