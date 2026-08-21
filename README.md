# GAP

### A ZAP 2.17.0 add-on that finds the parameters, links and words that may not be obvious.

[![PRs welcome](https://img.shields.io/badge/PRs-welcome-5B3AB6)](https://github.com/ArkhaMahn/gap/issues)

---

# GAP — ZAP add-on

A [ZAP](https://www.zaproxy.org/) add-on that brings
[xnl-h4ck3r's GAP Burp extension](https://github.com/xnl-h4ck3r/GAP-Burp-Extension)
to ZAP: select a node in the Sites tree, send it to GAP, and it scans the requests
and responses for **parameters**, **links** and **words** that may not be obvious —
the kind of input surface that passive scanning alone tends to miss.

> **Status: alpha.** Built and verified for ZAP 2.17.0. The extension loads cleanly with no errors. Please report any issues.

---

## Table of Contents

- [Overview](#overview)
- [Requirements](#requirements)
- [What it does](#what-it-does)
- [Build](#build)
- [Install in ZAP](#install-in-zap)
- [Usage](#usage)
- [The GAP tab](#the-gap-tab)
- [Behaviour notes](#behaviour-notes)
- [Development](#development)
- [Credits](#credits)
- [License](#license)

---

## Overview

Good attack-surface discovery is more than crawling. Parameters hidden in JSON bodies,
links only referenced from JavaScript, and words buried in URL paths often never make it
into the Sites tree. GAP digs through the messages you point it at and surfaces them in
one place so you can feed them back into your testing.

## Requirements

- **ZAP 2.17.0** or later.
- **Java 17** or later.

## What it does

- Adds **Send request to Gap** / **Send response to Gap** right-click menu items for
  Sites tree nodes and History entries.
- Extracts **parameters** from query strings, message bodies, multipart form-data,
  cookies and XML structures.
- Extracts **links** found anywhere in responses.
- Extracts **words** from URL paths and content, including optional URL path words.
- Flags **"sus" parameters** — names that look interesting for security testing — and can
  include tentative matches.
- Shows everything in a dedicated **GAP** workbench tab with per-category result tables.

---

## Build

Requires JDK 17+ and [Gradle](https://gradle.org/install/) 8.13+:

```
gradle build
```

The ZAP add-on artifact is produced at: `build/zapAddOn/bin/gap-alpha-1.0.0.zap`

> The `org.zaproxy.add-on` Gradle plugin derives the add-on id from the project directory name, so
> the project folder must be named `gap`.

To run the test suite:

```
gradle test
```

## Install in ZAP

1. Build the `.zap` (above).
2. In ZAP: **File → Load Add-on File…** and select the built `.zap`, OR drop the `.zap` into ZAP's `plugin` directory and restart.
3. Select a node in the Sites tree, right-click, and choose **GAP ▸ Send request/response to Gap**.

---

## Usage

1. Select one or more nodes in the **Sites tree** (or rows in **History**).
2. Right-click and pick **Send request to Gap** or **Send response to Gap**.
3. Switch to the **GAP** tab and watch the results come in.
4. Toggle what GAP looks for with the checkboxes and press the button to re-run.

## The GAP tab

| Control | Purpose |
| ------- | ------- |
| Parameters checkbox group | Choose which parameter sources are scanned: query string, body, multipart, JSON, cookies, XML structures and attributes. |
| Links checkbox | Include links extracted from responses. |
| Words checkbox | Include words harvested from paths/content; optionally include URL path words. |
| Report "sus" params? | Highlight parameter names considered suspicious for security testing. |
| Inc. Tentative? | Include tentative (lower-confidence) findings. |

Results are grouped into separate tables so parameters, links and words can be reviewed
independently.

## Behaviour notes

- GAP only analyses the messages you explicitly send to it — it performs no requests of
  its own and does not modify traffic.
- Analysis runs on a background thread; large subtrees can be cancelled cooperatively.
- Findings accumulate across runs until you clear them.

---

## Development

```
src/main/java/Arkhamahn/gap/
  ExtensionGap.java        # ExtensionAdaptor entry point, options, tab registration
  GapPanel.java            # Workbench tab: option checkboxes + result tables
  GapEngine.java           # Core analysis: parameter/link/word extraction
  GapContext.java          # Invoking-context helper
  GapParam.java            # Parameter result model
  ReqResp.java             # Message wrapper for request/response access
  CancelGapRequested.java  # Cooperative cancellation flag
  popup/                   # "Send request/response to Gap" menu items
```

---

## Credits

- Original idea and implementation:
  [GAP Burp Extension](https://github.com/xnl-h4ck3r/GAP-Burp-Extension) by
  [xnl-h4ck3r (@xnl_h4ck3r)](https://github.com/xnl-h4ck3r).

---

## License

[Apache-2.0](LICENSE) © 2026 Arkhamahn
