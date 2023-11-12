Reactive Example
===

Uses Reactive Repo
---
- reactive repo is a naive local json document storage
- not meant for anything outside of this sandbox example
- writes documents on save as separate json files in a directory
- reads all documents on find, optionally filter result set

Uses Reactive Http Clients
---
- based on WebClient
- thing client: make request with one thing
- attach client: make one request per doc

Processing Flow
---
- source of things, docs: existing events in repo
- processing flow:
  - make thing request
    - from thing event payload
    - if fails: skip attach requests
    - if succeeds: continue
  - make attach request
    - from successful thing response and
    - from payload of attach event: docs
    - many requests, one per doc
    - failing attach requests are skipped, ignore error
  - combine thing response, attach response
    - in case of failure write fail event into repo
    - in case of success write success event into repo
    
Usage
---
- To make a thing request fail: use "!" as prefix (see writeExample())
- To make a doc attach fail: suffix doc name with "!" (see writeExample())
- define groupId
