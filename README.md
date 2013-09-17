Contacts Hacker
===============

This project is an Android app that changes all of the contacts on a phone to an inputted string.

TODO LIST:
[DONE] Fix main page. The 'Developed by' blurb is off center, and the top paragraph leans to the right.
**Aligned stuff and made sure it wasn't derpy when rotated**

- A progress bar. Even with multi-threading, getting the app to faster than 15 seconds seems to be impossible.
The app makes the screen go black as well while performing tasks, which may be scary to the user and cause panic.
A progress bar indicating either percentage completion or x/y contacts changed will let the user know that the 
app hasn't crashed; it's still working and just a little slow.

- Pure Facebook contacts are not affected by the app. We obviously need to fix this, since it's a part of our 
mission objective.

[IN PROGRESS - JUSTIN] Add more text input fields. The main page has a lot of empty space so why not. If a user enters in more than one 
string, the app will choose randomly among them when it is changing contacts. Empty strings will be ignored.

- Remove the page flicker. It's obvious that the app is awkwardly reloading the main page after the dialog pops up.
