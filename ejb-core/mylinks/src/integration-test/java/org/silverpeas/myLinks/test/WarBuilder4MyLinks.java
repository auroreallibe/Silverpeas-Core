package org.silverpeas.myLinks.test;

import org.silverpeas.test.BasicWarBuilder;
import org.silverpeas.test.WarBuilder;

/**
 * This builder extends the {@link WarBuilder} in order to centralize the definition of common
 * archive part definitions.
 * @author Yohann Chastagnier
 */
public class WarBuilder4MyLinks extends BasicWarBuilder {

  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param classOfTest the class of the test for which a war archive will be build.
   */
  protected <CLASS_TEST> WarBuilder4MyLinks(final Class<CLASS_TEST> classOfTest) {
    super(classOfTest);
  }

  /**
   * Gets an instance of a war archive builder for the specified test class with the common
   * dependencies for publications.
   * @return the instance of the war archive builder.
   */
  public static <T> WarBuilder4MyLinks onWarForTestClass(Class<T> test) {
    WarBuilder4MyLinks warBuilder = new WarBuilder4MyLinks(test);
    warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:lib-core");
    warBuilder.addMavenDependencies("org.apache.tika:tika-core");
    warBuilder.addMavenDependencies("org.apache.tika:tika-parsers");
    warBuilder.createMavenDependenciesWithPersistence("org.silverpeas.core.ejb-core:node");
    warBuilder.createMavenDependencies("org.silverpeas.core.ejb-core:tagcloud");
    warBuilder.createMavenDependencies("org.silverpeas.core.ejb-core:publication");
    warBuilder.createMavenDependencies("org.silverpeas.core.ejb-core:clipboard");
    warBuilder.testFocusedOn(war -> war
        .addPackages(true, "com.silverpeas.myLinks")
        .addAsResource("create-database.sql"));
    warBuilder.addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
    return warBuilder;
  }
}