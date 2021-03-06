/**
 * Copyright 2012 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera.hadoop.hdfs.nfs;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.hadoop.fs.Path;

public class PathUtils {

  public static String checkPath(String path) {
    if(path.contains(":")) {
      throw new IllegalArgumentException("Colon is disallowed in path names");
    }
    return path;
  }

  public static void ensureDirectoryIsWriteable(File tempDir)
      throws IOException {
    if(tempDir.isFile()) {
      if(!tempDir.delete()) {
        throw new IOException("Cannot delete " + tempDir);
      }
    }
    if(!(tempDir.isDirectory() || tempDir.mkdirs())) {
      throw new IOException("Directory " + tempDir +
          " does not exist or could not be created.");
    }
    File testFile = new File(tempDir, "test-" + UUID.randomUUID());
    try {
      if(testFile.isFile() && !testFile.delete()) {
        throw new IOException("Test file " + testFile + " exists but cannot be deleted");
      }
      if(!testFile.createNewFile()) {
        throw new IOException("Unable to create test file " + testFile);
      }
    } finally {
      if(!testFile.delete()) {
        throw new IOException("Unable to delete test file " + testFile);
      }
    }
  }

  /**
   * Delete a directory and all its contents.  If
   * we return false, the directory may be partially-deleted.
   */
  public static boolean fullyDelete(File dir) {
    if (!fullyDeleteContents(dir)) {
      return false;
    }
    return dir.delete();
  }
  /**
   * Delete the contents of a directory, not the directory itself.  If
   * we return false, the directory may be partially-deleted.
   */
  public static boolean fullyDeleteContents(File dir) {
    boolean deletionSucceeded = true;
    File contents[] = dir.listFiles();
    if (contents != null) {
      for (int i = 0; i < contents.length; i++) {
        if (contents[i].isFile()) {
          if (!contents[i].delete()) {
            deletionSucceeded = false;
            continue; // continue deletion of other files/dirs under dir
          }
        } else {
          //try deleting the directory
          // this might be a symlink
          boolean b = false;
          b = contents[i].delete();
          if (b){
            //this was indeed a symlink or an empty directory
            continue;
          }
          // if not an empty directory or symlink let
          // fullydelete handle it.
          if (!fullyDelete(contents[i])) {
            deletionSucceeded = false;
            continue; // continue deletion of other files/dirs under dir
          }
        }
      }
    }
    return deletionSucceeded;
  }

  public static String realPath(Path path) {
    return path.toUri().getPath();
  }

}
