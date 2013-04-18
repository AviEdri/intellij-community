package com.intellij.compiler;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.PsiTestUtil;

import java.io.File;

import static com.intellij.util.io.TestFileSystemBuilder.fs;

/**
 * @author nik
 */
public class ModuleCompileScopeTest extends BaseCompilerTestCase {
  @Override
  protected boolean useExternalCompiler() {
    return true;
  }

  public void testCompileFile() {
    VirtualFile a = createFile("src/A.java", "class A{}");
    createFile("src/B.java", "class B{}");
    Module module = addModule("a", a.getParent());
    compile(true, a);
    assertOutput(module, fs().file("A.class"));
    make(module);
    assertOutput(module, fs().file("A.class").file("B.class"));
    assertModulesUpToDate();
  }

  public void testForceCompileUpToDateFile() {
    VirtualFile a = createFile("src/A.java", "class A{}");
    Module module = addModule("a", a.getParent());
    make(module);
    assertOutput(module, fs().file("A.class"));
    final VirtualFile output = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(getOutputDir(module));
    assertNotNull(output);
    final VirtualFile classFile = output.findChild("A.class");
    assertNotNull(classFile);
    deleteFile(classFile);
    make(module);
    assertOutput(module, fs());
    compile(true, a);
    assertOutput(module, fs().file("A.class"));
    assertModulesUpToDate();
  }

  public void testForceCompileUpToDateFileAndDoNotCompileResources() {
    VirtualFile a = createFile("src/A.java", "class A{}");
    VirtualFile res = createFile("src/res.properties", "aaa=bbb");
    Module module = addModule("a", a.getParent());
    make(module);
    assertOutput(module, fs().file("A.class").file("res.properties"));
    final VirtualFile output = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(getOutputDir(module));
    assertNotNull(output);
    final VirtualFile classFile = output.findChild("A.class");
    assertNotNull(classFile);
    final VirtualFile resOutputFile = output.findChild("res.properties");
    assertNotNull(resOutputFile);
    final File resourceOutputIoFile = new File(resOutputFile.getPath());
    final long resStampBefore = resourceOutputIoFile.lastModified();
    deleteFile(classFile);
    make(module);
    assertOutput(module, fs().file("res.properties"));
    compile(true, a);
    assertOutput(module, fs().file("A.class").file("res.properties"));
    final long resStampAfter = resourceOutputIoFile.lastModified();
    assertEquals(resStampBefore, resStampAfter);
    assertModulesUpToDate();
  }

  public void testForceCompileUpToDateFileAndDoNotCompileDependentTestClass() {
    VirtualFile a = createFile("src/A.java", "class A{ public static void foo(int param) {} }");
    VirtualFile b = createFile("testSrc/B.java", "class B { void bar() {A.foo(10);}}");
    Module module = addModule("a", a.getParent(), b.getParent());
    make(module);
    assertOutput(module, fs().file("A.class"), false);
    assertOutput(module, fs().file("B.class"), true);
    final VirtualFile output = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(getOutputDir(module, false));
    assertNotNull(output);
    final VirtualFile testOutput = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(getOutputDir(module, true));
    assertNotNull(testOutput);
    final VirtualFile classFile = output.findChild("A.class");
    assertNotNull(classFile);
    final VirtualFile testClassFile = testOutput.findChild("B.class");
    assertNotNull(testClassFile);
    deleteFile(classFile);
    deleteFile(testClassFile);
    make(module);
    assertOutput(module, fs());
    compile(true, a);
    assertOutput(module, fs().file("A.class"), false);
    assertOutput(module, fs(), true);
    assertModulesUpToDate();
  }

  public void testMakeTwoModules() {
    VirtualFile file1 = createFile("m1/src/A.java", "class A{}");
    Module m1 = addModule("m1", file1.getParent());
    VirtualFile file2 = createFile("m2/src/B.java", "class B{}");
    Module m2 = addModule("m2", file2.getParent());
    make(m1);
    assertOutput(m1, fs().file("A.class"));
    assertNoOutput(m2);
    make(m2);
    assertOutput(m2, fs().file("B.class"));
    assertModulesUpToDate();
  }

  public void testExcludedFile() {
    VirtualFile a = createFile("src/a/A.java", "package a; class A{}");
    createFile("src/b/B.java", "package b; class B{}");
    Module m = addModule("m", a.getParent().getParent());
    PsiTestUtil.addExcludedRoot(m, a.getParent());
    make(m);
    assertOutput(m, fs().dir("b").file("B.class"));

    changeFile(a);
    make(m);
    assertOutput(m, fs().dir("b").file("B.class"));

    rebuild();
    assertOutput(m, fs().dir("b").file("B.class"));
  }

  public void testFileUnderIgnoredFolder() {
    VirtualFile src = createFile("src/A.java", "class A{}").getParent();
    Module m = addModule("m", src);
    make(m);
    assertOutput(m, fs().file("A.class"));

    VirtualFile b = createFile("src/CVS/B.java", "package CVS; class B{}");
    make(m);
    assertOutput(m, fs().file("A.class"));

    changeFile(b);
    make(m);
    assertOutput(m, fs().file("A.class"));

    rebuild();
    assertOutput(m, fs().file("A.class"));
  }
}
