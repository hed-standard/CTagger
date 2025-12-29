# Introduction to CTagger

## What is HED?

HED (Hierarchical Event Descriptors) is a framework for systematically describing events and experimental metadata in machine-actionable form. HED provides:

- **Controlled vocabulary** for annotating experimental data and events
- **Standardized infrastructure** enabling automated analysis and interpretation
- **Integration** with major neuroimaging standards (BIDS and NWB)

For more information, visit the HED project [homepage](https://www.hedtags.org) and the [resources page](https://www.hedtags.org/hed-resources).

## What is CTagger?

**CTagger** is a desktop application that provides a graphical user interface for creating HED annotations. It helps researchers annotate their experimental events using HED tags through:

- **Interactive tag input** with real-time search and autocomplete
- **Schema browser** for exploring available HED tags
- **Validation** to ensure annotations conform to HED schema rules
- **BIDS support** for importing/exporting event files and sidecars
- **Field-level tagging** for both categorical and continuous event data

CTagger is particularly useful for:
- Researchers new to HED who want visual guidance for tag selection
- Annotating BIDS datasets with complex event structures
- Exploring HED schema hierarchies interactively
- Creating HED annotations without writing code

### Related tools and resources

- **[HED homepage](https://www.hedtags.org)**: Overview and links for HED
- **[HED resources](https://www.hedtags.org/hed-resources)**: Comprehensive tutorials and documentation
- **[HED schema browser](https://www.hedtags.org/hed-schema-browser)**: Browser for HED vocabularies
- **[HED specification](https://www.hedtags.org/hed-specification/)**: Formal specification defining HED annotation rules
- **[HED online tools](https://hedtools.org/hed)**: Web-based tools requiring no installation
- **[Python HEDTools](https://www.hedtags.org/hed-python)**: Python library for HED validation and analysis
- **[MATLAB HEDTools](https://www.hedtags.org/hed-matalb)**: MATLAB library for HED validation and analysis
- **[HED examples](https://github.com/hed-standard/hed-examples)**: Example datasets annotated with HED

- **[HED MATLAB tools](https://www.hedtags.org/hed-matlab)**: MATLAB wrapper for Python tools

## Download and installation

### Requirements

CTagger requires **Java Runtime Environment (JRE) 8 or higher**. Most systems come with Java pre-installed.

To check if Java is installed, run in a terminal:

```bash
java -version
```

You should see output like `java version "1.8.0_211"` or `openjdk version "11.0.11"` or higher.

If Java is not installed, download and install it from:
- [Adoptium](https://adoptium.net/) (recommended)
- [Oracle](https://www.oracle.com/java/technologies/downloads/)

### Standalone installation

1. **Download CTagger.jar** from the [releases page](https://github.com/hed-standard/ctagger/releases)

2. **Run the application** by double-clicking `CTagger.jar`

   **macOS users**: You may need to update Security settings to allow the app to run.

   **Linux users**: You might need to make the jar executable first:
   ```bash
   chmod +x CTagger.jar
   ```

### EEGLAB plugin

CTagger is integrated into EEGLAB through the **HEDTools plugin**:

1. In EEGLAB, go to **File → Manage EEGLAB extensions**
2. Install the **HEDTools** plugin
3. After loading a dataset, HED options appear in the **Edit** menu

For more details, see the [MATLAB HEDTools documentation](https://www.hedtags.org/hed-resources/HedMatlabTools.html#eeglab-plug-in-integration).

## Quick start

1. **Launch CTagger** by running the JAR file
2. **Import a BIDS file**:
   - Import `events.json` sidecar (automatically detects categorical fields)
   - Or import `events.tsv` file (you specify categorical fields)
3. **Select a field** from the "Tagging field" dropdown
4. **Select a value** (for categorical fields) from the "Field levels" panel
5. **Build your HED annotation** using:
   - Type to search for tags (autocomplete suggestions appear)
   - Click "Show HED schema" to browse the tag hierarchy
   - Press Enter to insert selected tags
6. **Validate** your annotations with the "Validate string" button
7. **Save** your work:
   - Copy to clipboard for manual use
   - Save as JSON dictionary for BIDS integration

For detailed usage instructions, see the [User Guide](user_guide.md).

## Building from source

### Prerequisites

- **Java JDK 8 or higher** (tested with JDK 21)
- **Gradle 8.5+** (wrapper included in repository)

### Build instructions

```bash
# Clone the repository
git clone https://github.com/hed-standard/ctagger.git
cd ctagger

# Build the JAR
./gradlew jar

# On Windows:
gradlew.bat jar

# The compiled JAR will be at:
# build/libs/CTagger.jar
```

### Running locally

After building:

```bash
java -jar build/libs/CTagger.jar
```

## Getting help

- **Documentation**: This user guide and the [HED resources page](https://www.hedtags.org/hed-resources)
- **Issues**: Report bugs or request features on [GitHub Issues](https://github.com/hed-standard/ctagger/issues)
- **HED Forum**: Ask questions on the [HED discussions forum](https://github.com/hed-standard/hed-specification/discussions)
- **Email**: Contact the HED team at hed.maintainers@gmail.com
