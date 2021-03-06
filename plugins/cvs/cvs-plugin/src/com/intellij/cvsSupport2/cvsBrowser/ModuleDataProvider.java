/*
 * Copyright 2000-2011 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.cvsSupport2.cvsBrowser;

import com.intellij.cvsSupport2.connections.CvsEnvironment;
import com.intellij.cvsSupport2.cvsoperations.cvsContent.DirectoryContentProvider;
import com.intellij.cvsSupport2.cvsoperations.cvsContent.GetModuleContentOperation;

/**
 * author: lesya
 */
public class ModuleDataProvider extends AbstractVcsDataProvider{
  public ModuleDataProvider(CvsEnvironment environment) {
    super(environment);
  }

  @Override
  public DirectoryContentProvider createDirectoryContentProvider(String path) {
    return new GetModuleContentOperation(myEnvironment, path);
  }

  @Override
  public AbstractVcsDataProvider getChildrenDataProvider() {
    return new FolderDataProvider(myEnvironment);
  }
}
