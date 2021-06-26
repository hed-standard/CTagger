# CTagger
CTagger GUI for annotating datasets using Hierarchical Event Descriptors. 

Both HED-3G schema and the software is in beta mode so bug report is welcomed. 

## Installation
- Check to see that you have Java installed. Linux usually comes with OpenJDK (open source version of JDK) already installed. We have tested up to Java version 11 in Mac and Ubuntu. Executing `java -version` on terminal should return something similar to
`java version "1.8.0_211"` or `openjdk version "11.0.11" 2021-04-20`
    * If Java is not installed, download and install Java Runtime Environment accordingly to your OS: https://www.oracle.com/java/technologies/javase-jre8-downloads.html. You might be asked to create an Oracle account first before you can download. 
* Download [CTagger.jar](https://github.com/hed-standard/CTagger/raw/main/CTagger.jar).
* If you're on macOS you might need to update your Security settings to allow the app to run. On Linux, you might need to make the jar executable first by executing `chmod +x CTagger.jar` while in the directory containing the downloaded CTagger.
* Double click on *CTagger.jar* to run. 

## Quick start
Launching CTagger will bring up the welcome interface. Users have three options to start their tagging:
* **Import BIDS event spreadsheet**: import event structure from BIDS events.tsv file 
* **Import BIDS event dictionary**: import event structure from BIDS events.json file
* **Quick tagging**: construct HED string independent from an event structure
![tagger-launcher](CTaggerLauncher.png)

For more guides and tutorials, check out the [Wiki](https://github.com/hed-standard/CTagger/wiki) page.

## Future roadmap
* Add validation capacity by integrating HED validator web service.
* Add more features for the schema browser, ability to view unit classes, search and scroll to a specific node.
* Add more suggestion features, including:
  * Make use of suggested-tag and related-tag attributes.
  * Auto suggest unit based on unit class.
  * Show users when extension is allowed or not.

* Advanced features:
  * Support setup event for definition and temporal scope.
  * Nested bullet point <-> Nested tag group interface.
