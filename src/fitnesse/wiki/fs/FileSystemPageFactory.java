package fitnesse.wiki.fs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import fitnesse.ConfigurationParameter;
import fitnesse.components.ComponentFactory;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wiki.WikiPageFactoryRegistry;
import fitnesse.wikitext.parser.Maybe;
import fitnesse.wikitext.parser.VariableSource;

/**
 * This is the general factory used to load and create wiki pages.
 *
 * For historic reasons it's still called FileSystemPageFactory, although it deals with all
 * file based page types (FileSystemPage, WikiFilePage, ExternalSuitePage).
 */
public class FileSystemPageFactory implements WikiPageFactory, WikiPageFactoryRegistry {
  private final FileSystem fileSystem;
  private final VersionsController versionsController;
  private final List<WikiPageFactory> wikiPageFactories = new ArrayList<>();
  private final InnerFileSystemPageFactory fallbackPageFactory = new InnerFileSystemPageFactory();

  public FileSystemPageFactory() {
    fileSystem = new DiskFileSystem();
    versionsController = new ZipFileVersionsController();
    initializeWikiPageFactories();
  }

  public FileSystemPageFactory(Properties properties) {
    fileSystem = new DiskFileSystem();
    versionsController = new ComponentFactory(properties).createComponent(
            ConfigurationParameter.VERSIONS_CONTROLLER_CLASS, ZipFileVersionsController.class);
    initializeWikiPageFactories();
  }

  public FileSystemPageFactory(FileSystem fileSystem, VersionsController versionsController) {
    this.fileSystem = fileSystem;
    this.versionsController = versionsController;
    initializeWikiPageFactories();
  }

  private void initializeWikiPageFactories() {
    registerWikiPageFactory(new InnerFileSystemPageFactory());
    registerWikiPageFactory(new WikiFilePageFactory());
    registerWikiPageFactory(new ChildWikiFilePageFactory());
    registerWikiPageFactory(new RootWikiFilePageFactory());
    // Note: ExternalSuitePageFactory should be last in line: it traverses the remainder of the tree looking for .html files.
    registerWikiPageFactory(new ExternalSuitePageFactory(fileSystem));
  }

  @Override // from WikiPageFactoryRegistry
  public void registerWikiPageFactory(WikiPageFactory wikiPageFactory) {
    wikiPageFactories.add(wikiPageFactory);
  }

  @Override // from WikiPageFactory
  public boolean supports(File path) {
    for (WikiPageFactory factory : wikiPageFactories) {
      if (factory.supports(path)) {
        return true;
      }
    }
    return false;
  }

  @Override // from WikiPageFactory
  public WikiPage makePage(File path, String pageName, WikiPage parent, VariableSource variableSource) {
    for (WikiPageFactory factory : wikiPageFactories) {
      if (factory.supports(path)) {
        return factory.makePage(path, pageName, parent, variableSource);
      }
    }
    // Treat top level and intermediate empty directories as valid pages
    if (parent == null || (parent instanceof FileBasedWikiPage && fileIsValid(path)))
      return fallbackPageFactory.makePage(path, pageName, parent, variableSource);
    return null;
  }

  private boolean fileIsValid(final File path) {
    return fileSystem.isDirectory(path) && PathParser.isSingleWikiWord(path.getName());
  }

  VersionsController getVersionsController() {
    return versionsController;
  }

  /**
   * This is the class that does the sole handling of FileSystemPages
   */
  protected class InnerFileSystemPageFactory implements WikiPageFactory {

    @Override
    public WikiPage makePage(final File path, final String pageName, final WikiPage parent, final VariableSource variableSource) {
      Maybe<String> rootPath = variableSource.findVariable("FITNESSE_ROOTPATH");
      return new FileSystemPage(path, pageName, parent, null, versionsController,
        new FileSystemSubWikiPageFactory(new File(rootPath.getValue()), fileSystem, variableSource, FileSystemPageFactory.this),
        variableSource);
    }

    @Override
    public boolean supports(File path) {
      String[] children = fileSystem.list(path);
      for (String child : children) {
        if (child.equals(FileSystemPage.contentFilename)) return true;
      }
      return false;
    }
  }

  protected class WikiFilePageFactory implements WikiPageFactory {
    @Override
    public WikiPage makePage(final File path, final String pageName, final WikiPage parent, final VariableSource variableSource) {
      Maybe<String> rootPath = variableSource.findVariable("FITNESSE_ROOTPATH");
      return new WikiFilePage(path, pageName.substring(0, pageName.length() - WikiFilePage.FILE_EXTENSION.length()), parent, null, versionsController,
        new FileSystemSubWikiPageFactory(new File(rootPath.getValue()), fileSystem, variableSource, FileSystemPageFactory.this),
        variableSource);
    }

    @Override
    public boolean supports(File path) {
      return path.getPath().endsWith(WikiFilePage.FILE_EXTENSION) && fileSystem.exists(path) && !fileSystem.isDirectory(path);
    }
  }

  protected class ChildWikiFilePageFactory implements WikiPageFactory {
    @Override
    public WikiPage makePage(final File path, final String pageName, final WikiPage parent, final VariableSource variableSource) {
      Maybe<String> rootPath = variableSource.findVariable("FITNESSE_ROOTPATH");
      return new WikiFilePage(wikiFile(path), pageName, parent, null, versionsController,
        new FileSystemSubWikiPageFactory(new File(rootPath.getValue()), fileSystem, variableSource, FileSystemPageFactory.this),
        variableSource);
    }

    private File wikiFile(final File path) {
      return new File(path.getPath() + WikiFilePage.FILE_EXTENSION);
    }

    @Override
    public boolean supports(File path) {
      File wikiFile = wikiFile(path);
      return fileSystem.exists(wikiFile) && !fileSystem.isDirectory(wikiFile);
    }
  }

  protected class RootWikiFilePageFactory implements WikiPageFactory {
    @Override
    public WikiPage makePage(final File path, final String pageName, final WikiPage parent, final VariableSource variableSource) {
      Maybe<String> rootPath = variableSource.findVariable("FITNESSE_ROOTPATH");
      return new WikiFilePage(new File(path, WikiFilePage.ROOT_FILE_NAME), pageName, parent, null, versionsController,
        new FileSystemSubWikiPageFactory(new File(rootPath.getValue()), fileSystem, variableSource, FileSystemPageFactory.this),
        variableSource);
    }

    @Override
    public boolean supports(File path) {
      File rootWikiFile = new File(path, WikiFilePage.ROOT_FILE_NAME);
      return fileSystem.exists(rootWikiFile) && !fileSystem.isDirectory(rootWikiFile);
    }
  }
}
