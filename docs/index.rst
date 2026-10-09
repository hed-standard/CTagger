CTagger
=======

.. sidebar:: Quick links
   
   * `HED homepage <https://www.hedtags.org/>`_ 

   * `HED vocabularies <https://www.hedtags.org/hed-schema-browser>`_

   * `HED online tools <https://hedtools.org/hed/>`_

   * `HED browser tools <https://www.hedtags.org/hed-web>`_

   * `HED organization  <https://github.com/hed-standard/>`_  

   * `HED specification <https://www.hedtags.org/hed-specification>`_
   
   * `CTagger releases <https://github.com/hed-standard/ctagger/releases>`_

.. raw:: html

   <div style="background-color: #c62828; color: #ffffff; padding: 1em 1.25em; border-radius: 6px; margin: 1em 0;">
   <strong>CTagger is no longer supported.</strong> This repository will be archived and CTagger will be
   removed from the <a href="https://www.hedtags.org/hed-resources" style="color: #ffffff; text-decoration: underline;">HED resources</a>
   documentation on December 31, 2026. The last release stays available on the
   <a href="https://github.com/hed-standard/ctagger/releases" style="color: #ffffff; text-decoration: underline;">Releases page</a>
   but will not be updated.
   </div>

.. note::

   **Replacements:** HED annotation is moving to `HEDit <https://annotation.garden/hedit/>`_, the
   browser-based annotation tool of the `Annotation Garden Initiative <https://annotation.garden/>`_,
   open infrastructure for collaborative annotation of neuroscience stimuli built on the BIDS and
   HED standards. For editing HED in a code editor, use
   `hed-lsp <https://github.com/hed-standard/hed-lsp>`_, the HED Language Server Protocol extension
   for VS Code, which validates annotations as you type.

Welcome to the CTagger documentation! CTagger is a desktop application for annotating
neuroimaging experiment events using the **Hierarchical Event Descriptor (HED)** standard.
It provides a graphical interface with automatic tag suggestions, validation,
and HED schema browsing capabilities. CTagger can be used as a standalone application 
or integrated into EEGLAB through the HEDTools plugin.

Key features
------------

* **Interactive tagging**: Graphical interface for building HED annotations
* **Autocomplete**: Real-time tag suggestions based on HED schema
* **Schema browser**: Visual exploration of HED tag hierarchies
* **Validation**: Built-in validation against HED schema rules
* **BIDS integration**: Import and export BIDS event files and sidecars
* **Field-level tagging**: Support for categorical and continuous event fields

Guides
-------

.. toctree::
   :maxdepth: 2

   User guide <user_guide>
   CTagger in EEGLAB <ctagger_in_eeglab>


Indices and tables
==================

* :ref:`genindex`
