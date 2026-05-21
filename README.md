# Notes on the Module "Tools"

## What Does It Do?

This module is a collection of various ready-to-use tools reading and manipulating 
GnuCash 
XML files. They are, of course, based on the modules

* Base
* API (Core)
* API Specialized Entities
* API Extensions,

but *not* (technically) on "API Examples".

You will notice, however, that the tools partially pull on the examples in the module 
"API Examples", and thus we have a *logical* dependency here. In earlier versions,
there were many similarities between the two modules, but they have deviated from 
one another since then (and will continue to do so in the course of the future releases 
to come).

## What is This Repo's Relationship with the Other Repos?

* This is a module-level repository which is part of a multi-module project, i.e. it has a parent and several siblings. 

  [Parent](https://github.com/jross765/JGnuCashLibNTools.git)

* Under normal circumstances, you cannot compile it on its own (at least not without further preparation), but instead, you should clone it together with the other repos and use the parent repo's build-script.

* This repository contains no history before V. 1.7 (cf. notes in parent repo).

## Major Changes

Cf. document "[Major Changes](https://github.com/jross765/JGnuCashLibNTools/gnucash-api-tools/major_changes.md)".

## Planned
./.

## Known Issues
* The programs that generate new objects (`gen.simple.GenXYZ`) currently only work (reliably) 
  when at least one object of the same type 
  (a customer, say) 
  is already in the file (cf. according note on issue in README file for package "API (Core)").
