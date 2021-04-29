# info.shillem.xsp
This project targets the XPages environment.\
Although designed for such environment its goal is to make as less use of it as possible while providing a series of utilities that encourage good development paradigms.

## info.shillem.util.xsp
It provides a series of utility classes to better deal with common XPage development.\
It provides a handful of XPage components.
It provides a rewritten `xsp.js` JavaScript library that allows XPages to work even while ditching Dojo support entirely (at that point components that generate Dojo markup won't work).
 
### Dependencies
- `info.shillem.util`

## info.shillem.domino.xsp
It exposes the `info.shillem.domino` OSGi plugin as XPage Library.\
It also provides an additional `lotus.domino.Session` management classes tweaked for this environment.

### Dependencies
- `info.shillem.util`
- `info.shillem.domino`

## info.shillem.sql.xsp
It exposes the `info.shillem.sql` OSGi plugin as XPage Library.

### Dependencies
- `info.shillem.util`
- `info.shillem.sql`

## info.shillem.rest.xsp
It exposes the `info.shillem.rest` OSGi plugin as XPage Library.

### Dependencies
- `info.shillem.rest`
