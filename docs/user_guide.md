# CTagger user guide

This guide takes you through the process of using CTagger to add HED annotations to your neuroimaging experiment events. CTagger can be used as a standalone application or as part of the EEGLAB BIDS data pipeline.

## Loading BIDS event files

From the CTagger launcher window, you can import either BIDS event dictionaries or event spreadsheets to start tagging.

![CTagger Launcher](_static/images/CTaggerLauncher.png)

### Importing event dictionaries (events.json)

If you select **Import BIDS event dictionary**, you'll be prompted to choose an `events.json` file. CTagger will automatically:

- Identify fields with the "Levels" key as categorical columns
- Consider each sub-level as a categorical value for that field
- Treat all other fields as having continuous values

### Importing event spreadsheets (events.tsv)

If you select **Import BIDS event spreadsheet** and choose an `events.tsv` file, CTagger will ask you to specify which fields are categorical.

![Categorical Fields Selection](_static/images/events-tsv-categorical-fields.png)

Once you specify the categorical fields, CTagger will automatically extract the unique categorical values from the file.

## The main tagging interface

After importing, you'll see the main CTagger tagging interface:

![Tagging Interface](_static/images/tagging-interface.png)

### Interface components

1. **Tagging field dropdown**: Toggle between different fields (event columns)

2. **Field levels panel** (left side):

   - For **categorical fields**: Lists all unique values for the selected field
   - For **continuous/value fields**: Shows a single **HED** item where you can specify a template using the `#` symbol

3. **Tag editor panel** (right side):

   - Build your HED annotation here
   - Tags are inserted as you type or select them

### Important usage notes

- **Always select an item** from the Field levels list before building your annotation
- The HED string you create will be associated with the selected item
- If no item is selected, your HED string will not be saved

You are now ready to start tagging!

## Building HED annotations

CTagger provides several features to help you quickly and easily construct HED annotations.

### Tag search and autocomplete

As you type in the tag editor, CTagger compares your input to all schema nodes and displays matching tags in the **Search Results** box below the cursor.

**Navigation**:

- Press the **down-arrow key** to scroll through results
- Press **Enter/Return** to select and insert a tag
- Or click on a tag with your mouse to select it

**Behavior**:

- CTagger inserts the **short-form** tag, replacing your input sequence
- The Search Results box disappears after selection
- Press **Esc** or click outside the box to dismiss it without selecting

Tags are matched case-insensitively against their **long-form** representation.

![Search Animation](_static/images/search.gif)

### HED schema browser

Click the **Show HED schema** button to browse the HED schema hierarchy interactively.

**Features**:

- Expand/collapse nodes that contain children
- Click on any node to insert its **short-form** tag
- For nodes that take values, click the **#** symbol underneath to insert the node name followed by `/`
- After insertion, type the value for that node

![Schema Browser Animation](_static/images/show-schema.gif)

### Tag groups

In HED, tags can be grouped together using parentheses to indicate they modify each other and should be interpreted as a unit.

**Examples**:

- `(Triangle, Green)` describes a green triangle
- Nested groups can describe more complex relationships

**Formatting in CTagger**:

- Use **Newline** and **Tab** keys to format nested groups for readability
- CTagger will automatically strip newlines and tabs from the final HED string
- This ensures compatibility with BIDS and other formats

![Tag Groups Animation](_static/images/tag-group.gif)

### Reviewing tags

At any time during annotation, you can view the current (even if incomplete) long-form HED string:

- Go to **File → Review all tags**

![Review Tags](_static/images/review-all-tags.jpg)

This displays your annotation in the full, long-form format.

## Validation

### Validating individual strings

Click the **Validate string** button to check the currently displayed HED string against the schema rules.

![Validate Button](_static/images/validate.jpg)

CTagger will report any errors and provide guidance on how to fix them.

### Validating all annotations

Click the **Validate all** button to validate the entire event file's annotations at once.

This checks all field-level annotations you've created across all fields and categorical values.

**Important**: You must fix all validation errors before finishing and saving your annotations.

## Saving your work

Once you've completed your annotations and validation passes without errors:

### Copy to clipboard

Select **Finish → Copy to clipboard** to copy your HED annotations. You can then paste them into another file or application.

### Save as JSON dictionary

Select **Finish → Save to file**:

1. Provide a filename for your JSON file
2. Choose where to save it
3. CTagger saves the annotations in BIDS-compatible JSON format

The JSON dictionary maps field names and categorical values to their HED strings, ready for use with BIDS datasets.

## Tips and best practices

### Value fields with templates

For continuous/value fields (like `duration`, `response_time`), use the `#` placeholder in your template:

```
Duration/# ms
```

This template will be applied with the actual value from each event row substituted for `#`.

### Organizing complex annotations

For complex event structures with many tag groups:

1. Use newlines and tabs to indent nested groups
2. This makes it easier to see the structure while editing
3. CTagger removes formatting when saving, maintaining BIDS compatibility

### Iterative validation

Validate frequently as you build annotations:

- Fix errors early before they compound
- Use validation messages to learn HED rules
- Validate individual strings first, then validate all at the end

### Working with multiple fields

- Complete one field at a time
- Use the field dropdown to switch between fields
- Validate each field's annotations before moving to the next

### Schema versions

- CTagger loads the HED schema from the official repository or local files
- Ensure you're using the appropriate schema version for your dataset
- Schema version should match what's specified in your BIDS dataset

## Keyboard shortcuts

| Action                  | Shortcut                               |
| ----------------------- | -------------------------------------- |
| Insert newline          | Enter (when not selecting from search) |
| Add tab indentation     | Tab                                    |
| Dismiss search results  | Esc                                    |
| Navigate search results | Up/Down arrow keys                     |
| Select from search      | Enter/Return                           |

## Troubleshooting

### Validation errors

**Problem**: Tags are marked as invalid

**Solutions**:

- Check spelling and capitalization (use search/browser)
- Ensure required children are included (consult schema)
- Verify tag groups are properly closed with parentheses
- Check that value fields have appropriate units

### Tags not saving

**Problem**: Built annotations don't appear in output

**Solutions**:

- Ensure an item is selected in the Field levels panel
- Check that you've clicked into the tag editor area
- Verify you're on the correct field in the dropdown

### Schema not loading

**Problem**: Can't see schema tags or autocomplete doesn't work

**Solutions**:

- Check internet connection (if loading from repository)
- Try loading a local schema file
- Restart CTagger
- Check Java version is 8 or higher

### BIDS file import issues

**Problem**: Can't import events.json or events.tsv

**Solutions**:

- Verify file is valid JSON/TSV format
- Check that events.json has proper BIDS structure
- Ensure categorical fields are properly defined with "Levels" key
- Try importing as TSV and manually specifying categorical fields

## Additional resources

- **HED Schema Browser**: [https://www.hedtags.org/hed-schema-browser](https://www.hedtags.org/hed-schema-browser)
- **HED Specification**: [https://www.hedtags.org/hed-specification](https://www.hedtags.org/hed-specification)
- **BIDS Specification**: [https://bids-specification.readthedocs.io](https://bids-specification.readthedocs.io)
- **HED Resources**: [https://www.hedtags.org/hed-resources](https://www.hedtags.org/hed-resources)
- **HED Examples**: [https://github.com/hed-standard/hed-examples](https://github.com/hed-standard/hed-examples)

## Getting help

If you encounter issues or have questions:

- **GitHub Issues**: [https://github.com/hed-standard/ctagger/issues](https://github.com/hed-standard/ctagger/issues)
- **HED Forum**: [https://github.com/hed-standard/hed-specification/discussions](https://github.com/hed-standard/hed-specification/discussions)
- **Email**: hed.maintainers@gmail.com
