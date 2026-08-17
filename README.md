GAP - Get All Parameters, Links and Words
=========================================

A ZAP 2.17.0 add-on that finds the parameters, links and words that may not be
obvious.

It is a port of the
[`GAP` Burp extension](https://github.com/xnl-h4ck3r/GAP-Burp-Extension) by
[xnl-h4ck3r (@xnl_h4ck3r)](https://github.com/xnl-h4ck3r).

Author: Arkhamahn. Original idea and implementation credit goes to
xnl-h4ck3r and their GAP Burp extension.

Vibecoded with love 🖤

## Building

Requires JDK 17+ and Gradle 8.13+.

```
gradle build
```

The ZAP add-on artifact is produced at
`build/zapAddOn/bin/gap-alpha-1.0.0.zap`. Install it in ZAP via
`Manage Add-ons -> Install...`.