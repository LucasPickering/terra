# Terrain Generation
Generate terrain and civilizations.
  
Uses the Lightweight Java Game Library.  
## Installation instructions for Development:
#### IntelliJ
1. Clone the repo
2. Import the project from pom.xml
3. Install dependencies (see below)
4. Run the class TerrainGen

## Dependencies
All dependencies are managed by Maven. Most are available online and will be downloaded
automatically, but some need to be built manually. Those are:

* [LP-utils](https://github.com/LucasPickering/LP-utils)
* [noise by flowpowered](https://github.com/flow/noise)

For each of those, clone the git repo and run `mvn clean install` to build and install it.

## JVM Parameters
### Debug Mode
Use `F3` to enable debug mode.

### Debug Logging
`-Djava.util.logging.config.file="src/test/resources/logging.properties"`

### LWJGL Debug Mode
`-Dorg.lwjgl.util.Debug=true`

## Benchmarking
For running in IntelliJ, install the JMH plugin. Then, on Windows, you have to set the TMP
environment variable so that JMH can properly acquire its lock. To do this, edit the configuration
of your benchmark and add the environment variable TMP with a writeable directory as its value
(such as `C:\Users\<user>\AppData\Local\Temp`). In IntelliJ, this can be done in a menu in the Run
Configuration.