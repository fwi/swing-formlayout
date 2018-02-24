# Swing FormLayout

A layout-manager for Java Swing components. 

The layout-manager builds on a default height and width of components derived from text-size resulting in symmetric layout of components suitable for forms that are easier on the eye, are resizable and also scale with the choosen default size of the text font.

A form-builder is included to assist in building the hierarchical tree of components that is required to create a resizable form-window. 

A previous version of this layout manager (including some more Swing goodies) can be found at the [HVLayout](https://github.com/fwi/HVLayout) repository.

**Screenshots**

In the `src/test/java` directory you will find a number of runnable classes that demo the form layout-manager. Following screenshots are from the `AddressBookDemo`.

Normal startup, boxes show the hierarchical (vertical and horizontal boxes in boxes) layout:
<br/>![AddressBookDemo-default](https://github.com/fwi/swing-formlayout/raw/master/screenshots/address-book-demo.png)

Clicking the `big font` checkbox and resizing the window to it's minimum size (before scrollbars appear):
<br/>![AddressBookDemo-bigfont](https://github.com/fwi/swing-formlayout/raw/master/screenshots/address-book-demo-big-font-small-window.png)

Cicking the `right to left` button and resizing the window beyond it's minimum size so that scrollbars appear:
<br/>![AddressBookDemo-rtol](https://github.com/fwi/swing-formlayout/raw/master/screenshots/address-book-demo-rtol-scrollbars.png)

Resizing larger:
<br/>![AddressBookDemo-large](https://github.com/fwi/swing-formlayout/raw/master/screenshots/address-book-demo-large.png)

# Building
 
Maven (3.5+) and Java 8 are required.
Open command prompt in project directory and run:
```
mvn clean install
```
A distribution zip-file is created as part of the build (see the `target` directory).
