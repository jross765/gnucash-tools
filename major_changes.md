# Major Changes

## V. 1.8 &rarr; 1.9
Followed the deprecation of `FixedPointNumber` in the modules
"(Core) API", V. 1.9,
"Specialized Entitites", V. 0.4 and
"API Extended", V. 1.9:

Changed various implementations so that it's (almost) not used any more.

## V. 1.7 &rarr; 1.8
* New tools: 
  * GetTrxList
  * GetTrxSpltList
  * TestSel[Acct|Prc|Sec]
  * TestGetLocale

* Existing tools:
  * Better encapsulation of selection of accounts, securities and prices.
  * GenSec: More optional fields; check whether security with given ISIN already exists.
  * Overall: Small improvements, general maintenance

## V. 1.6 &rarr; 1.7
* New tools: 
  * Dump

* Existing tools:
  * Introduced new (dummy) ID types (cf. module "Base") for type safety and better symmetry with sister project.
  * GetAccountInfo: Now lists newly-introduced account-reconciliation info.

## V. 1.5 &rarr; 1.6
* New tools: 
  * UpdCmdty, GenAcct (finally!), GetPrcList

* Existing tools: 
  * GetJob: removed "employee" code  -- there is no such thing as an "employee job" in GnuCash.
  * For parsing command-line options: Replaced `GnuParser` by `DefaultParser` (the former has been deprecated).
  * Fixed a few small bugs.
  * Generalize and unify interfaces for tools in which a commodity has to be selected.

## V. 1.4 &rarr; 1.5
Created and added a number of tools:

* Package `...get`: Tools for getting information from GnuCash files:
	* package `...get.list`: Simple tools that print an unfiltered list of all entries of a given entity. Rather low-level.
	* package `...get.info`: Simple tools that print the information of one entry of one entity. No bells, no whistles. A little bit of convenience, however, in how selecting the entry (not just by ID).
	* package `...get.sonstige`: Specialized tool (currently, only one) that retrieve specific information from the GnuCash file.

* Package `...gen`: Tools for generating new entries in GnuCash files:
	* package `...gen.simple`: Tools that generate exactly one entry of a given entity, with virtually no business logic involved (i.e., the user provides all data as is). No convenience.
	* package `...gen.complex`: Tools that generate one or more entries of one of more given entities, with business logic involved. Convenience where possible.

* Package `...upd`: Tools for updating entries in GnuCash files:

	Simple tools that update specific fields of one entry of a given entity. As in package `gen.info`: No bells, no whistles. A little bit of convenience, however, in how selecting the entry (not just by ID).
