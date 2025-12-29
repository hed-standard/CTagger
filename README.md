# CTagger

[![Build Status](https://github.com/hed-standard/ctagger/workflows/Build%20and%20Test/badge.svg)](https://github.com/hed-standard/ctagger/actions)
[![Release](https://img.shields.io/github/v/release/hed-standard/ctagger)](https://github.com/hed-standard/ctagger/releases)
[![License](https://img.shields.io/github/license/hed-standard/ctagger)](LICENSE)

![CTagger Interface](assets/images/CTaggerLauncher.png)

CTagger is a desktop application for annotating neuroimaging experiment events using the Hierarchical Event Descriptor (HED) standard. It provides a graphical interface with automatic tag suggestions, validation, and HED schema browsing capabilities.

## About HED

The Hierarchical Event Descriptor (HED) is a standard for describing experiment events in neuroimaging data. HED enables detailed, human-readable, and machine-actionable annotation of events, making shared data analysis-ready. Learn more at [www.hedtags.org](https://www.hedtags.org).

## Documentation

For detailed usage instructions and tutorials, see the CTagger [**user guide](https://www.hedtags.org/CTagger/user_guide.html).

## Download

Download the latest pre-built release from the [Releases page](https://github.com/hed-standard/ctagger/releases).

## Usage

CTagger can be used in two ways:

### Standalone application

Run the downloaded JAR file:

```bash
java -jar CTagger.jar
```

**Requirements:** Java Runtime Environment (JRE) 8 or higher

### EEGLAB plugin

CTagger is integrated into EEGLAB through the [HEDTools plugin](https://www.hedtags.org/hed-resources/HedMatlabTools.html#eeglab-plug-in-integration).

## Building from source

### Prerequisites

- **Java JDK 8 or higher** (tested with JDK 21)
- **Gradle 8.5+** (wrapper included)

### Build instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/hed-standard/ctagger.git
   cd ctagger
   ```

2. Build the JAR:
   ```bash
   ./gradlew jar
   ```
   
   On Windows:
   ```cmd
   gradlew.bat jar
   ```

3. The compiled JAR will be located at:
   ```
   build/libs/CTagger.jar
   ```

### Running locally

After building, run the application:

```bash
java -jar build/libs/CTagger.jar
```

## Development

### Project structure

- `src/main/kotlin/` - Kotlin source files
- `src/main/java/` - Java source files  
- `build.gradle` - Gradle build configuration
- `gradle/` - Gradle wrapper files

### Technology stack

- **Language:** Kotlin 1.9.25 + Java
- **Build System:** Gradle 8.5
- **GUI Framework:** Java Swing
- **Dependencies:** 
  - Gson 2.10.1 (JSON parsing)
  - Jsoup 1.17.2 (HTML parsing)
  - Fuel 2.3.0 (HTTP client)
  - kotlinx-coroutines 1.5.1 (async operations)
  - univocity-parsers 2.9.0 (CSV/TSV parsing)

### Building for development

Clean and build with full output:

```bash
./gradlew clean build
```

Run tests:

```bash
./gradlew test
```

## HED validation

CTagger validates annotations against HED schemas using the [HED validation service](https://hedtools.org/hed/services). The default schema version is 8.4.0, but other versions can be selected from the application menu.

## Contributing

Bug reports and feature requests can be submitted through [GitHub Issues](https://github.com/hed-standard/ctagger/issues).

## License

[**MIT Licence**](LICENSE)


## Support

- **User guide:** [https://www.hedtags.org/ctagger/user_guide.html](https://www.hedtags.org/ctagger/user_guide.html)
- **HED homepage:** [https://www.hedtags.org](https://www.hedtags.org)
- **Issues:** [GitHub Issues](https://github.com/hed-standard/ctagger/issues)
