

# MicroProfile Parent Repository

The MicroProfile Parent POM provides the required configuration and behavior for all MicroProfile Projects.

## How to use the MicroProfile Parent?

Each MicroProfile project specification expects to follow the folder structure:

- api
- spec
- tck

In the project root POM, use the MicroProfile Parent POM:

```xml
<parent>
    <groupId>org.eclipse.microprofile</groupId>
    <artifactId>microprofile-parent</artifactId>
    <version>${version}</version>
</parent>
```

The MicroProfile Parent POM provides the following capabilities:

- Code Quality Checks, including LICENSE headers in source files, code formatting, imports, and sort optimization and 
Checkstyle validation. Always on by default, can be disabled with `-DskipChecks`

- Automatically generate the source jars

- Automatically generate the Javadocs. Always on by default, can be skipped with `-DskipDocs`

- Automatically generate `pdf` and `html` files from the `src/main/asciidoc` folder.

- Apply the BND configuration if a `bnd.bnd` exists in the root project.

- Handle the required LICENSES to be included in the binaries during development and when performing a release.
  - The `tck` module requires an empty `tck` file in `src/main/resources/META-INF/`
  - The argument `-Drelease.revision=Final`, replaces the Apache Licenses with Eclipse Foundation Licenses required for 
  the final binaries

A MicroProfile BOM TCK is also available to align the testing dependencies for the TCK Modules:

```xml
  <dependencyManagement>
      <dependencies>
          <dependency>
              <groupId>org.eclipse.microprofile</groupId>
              <artifactId>microprofile-tck-bom</artifactId>
              <version>${version}</version>
              <type>pom</type>
              <scope>import</scope>
          </dependency>
      </dependencies>
  </dependencyManagement>
```

The `dependencyManagement` section should be placed in the `tck` POM, to avoid leaking test dependencies to other 
modules.
