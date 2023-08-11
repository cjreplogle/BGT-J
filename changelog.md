## BGT Changelog:

### v0.1 (early June 2023)
 - Existed before this program did. 
 - Generated infill shape of a circle and autofilled it to certain pore size then stacked/rotated/flipped it to make scaffold.
 - Very buggy but used to make some of the first proof of concept scaffolds

### v0.2 (6/27/23)
 - This central program made to connect the two tools (slicer.c + infill.c) and made more user friendly.
 - Some very major bug fixes (it doesn't always segmentation fault now)

### v0.3 (7/7/23)
 - Revamped sectioning system to make customized segments of scaffold more flexible. 
 - Reworked the recursive transform function to be faster and changed the order of operations (axis flip before rotation).
 - Properly documented tesselator (ish)

### v0.4 (7/12/23)
 - Square mode

### v0.5 (7/14/23)
 - Settings implementation (so it doesn't ask every minor detail in the generation sequence)
 - QOL Changes

### v0.6 (7/18/23)
 - File read/write revamp.
 - Configuration saving via .bpc file.

### v0.7 (7/19/23)
 - Ported code to Java.
 - Revamped input listening to include more error handling and generally be more stable.
 - Redid some infill logic for the sake of compatibility with Java libraries.

### v0.8 (8/11/23)
 - Some changes to math to make generated structure better (Have not fully generalized flip functions to all angles as it is beyond my use case, may do this later)
 - Packaged properly(ish)

### Planning to add:
 - GUI (JavaFX)
 - Launch4j or similar direct launch.
