# Terrain Generation
Generate terrain and civilizations.
  
Uses the Lightweight Java Game Library.  
### Installation instructions for Development:
#### IntelliJ
1. Clone the repo
2. Import the project from pom.xml
3. Run the class TerrainGen

### JVM Parameters
#### Debug Mode
`-Dme.lucaspickering.terraingen.debug=true`

#### Debug Logging
`-Djava.util.logging.config.file="src/test/resources/logging.properties"`

#### LWJGL Debug Mode
`-Dorg.lwjgl.util.Debug=true`

### Benchmarking
For running in IntelliJ, install the JMH plugin. Then, on Windows, you have to set the TMP
environment variable so that JMH can properly acquire its lock. To do this, edit the configuration
of your benchmark and add the environment variable TMP with a writeable directory as its value.
For example:
`C:\Users\<user>\AppData\Local\Temp`