# Notes on the Module "Tools"

This module is a collection of various ready-to-use tools reading and manipulating 
GnuCash 
XML files. They are, of course, based on the module "Base", "API" and "Extensions", 
but *not* (technically) on "Examples".

You will notice that the tools partially pull on the examples in the "Examples" 
module, and thus we have a *logical* dependency here. Currently, there are many 
similarities between the two modules, but expect those two modules to deviate 
from one another in the course of the future releases to come.

## Major Changes 
### V. 1.6 &rarr; 1.7
* New tools: 
  * Dump

* Existing tools:
  * Introduced new (dummy) ID types (cf. module "Base") for type safety and better symmetry with sister project.
  * GetAccountInfo: Now lists newly-introduced account-reconciliation info.

### V. 1.5 &rarr; 1.6
* New tools: 
  * UpdCmdty, GenAcct (finally!), GetPrcList

* Existing tools: 
  * GetJob: removed "employee" code  -- there is no such thing as an "employee job" in GnuCash.
  * For parsing command-line options: Replaced `GnuParser` by `DefaultParser` (the former has been deprecated).
  * Fixed a few small bugs.
  * Generalize and unify interfaces for tools in which a commodity has to be selected.

### V. 1.4 &rarr; 1.5
Created and added a number of tools:

* Package `...get`: Tools for getting information from GnuCash files:
	* package `...gen.simple`: Tools that generate exactly one entry of a given entity, with virtually no business logic involved (i.e., the user provides all data as is). No convenience.
	* package `...gen.complex`: Tools that generate one or more entries of one of more given entities, with business logic involved. Convenience where possible.

* Package `...gen`: Tools for generating new entries in GnuCash files:
	* package `...gen.list`: Simple tools that print an unfiltered list of all entries of a given entity. Rather low-level.
	* package `...gen.info`: Simple tools that print the information of one entry of one entity. No bells, no whistles. A little bit of convenience, however, in how selecting the entry (not just by ID).
	* package `...gen.sonstige`: Specialized tool (currently, only one) that retrieve specific information from the GnuCash file.

* Package `...upd`: Tools for updating entries in GnuCash files:

	Simple tools that update specific fields of one entry of a given entity. As in package `gen.info`: No bells, no whistles. A little bit of convenience, however, in how selecting the entry (not just by ID).

## Planned
./.

## Known Issues
* The programs that generate new objects (`gen.simple.GenXYZ`) currently only work (reliably) when at least one object of the same type (a customer, say) is already in the file (cf. according note on issue in README file for package "API").
