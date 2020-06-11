# Chip8 Emulgator


![Screenshot of the application](https://repository-images.githubusercontent.com/239994384/6ad99700-9601-11ea-81f2-ce7200aed5e0)

Chip8 Emulator / Interpreter / Virtual Machine written in Java 11 with Swing GUI as front-end

*Warning*: Prototype quality. Needs improvements.

## Prerequisites

* Java Dev Kit 11 installed, with env. variables properly configured:

```
$JAVA_HOME #Linux / Mac

%JAVA_HOME% @REM Windows
```

## Usage

### Installation

Latest package available on [releases](https://github.com/metteo/chip8-swing/releases) page should be unzipped in a favourite program location.

### Execution

Scripts are available in `bin/` directory. When executed without parameters, application launches built in Boot-128 ROM (by David Winter) which allows editing of program RAM. `File -> Open` menu can be used to open a different ROM. It is also possible to pass a single path argument to the script to request ROM load at startup.

## Development

Below instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

[chip8-core](https://github.com/metteo/chip8-core) installed in local maven repository.

### Building

Compilation
```
./mvnw compile
```

Install to local repository

```
./mvnw install
```

Building app package (available in `target/chip8-emulgator`):

```
./mvnw appassembler:assemble
```

## Built With

* [Java 11](https://adoptopenjdk.net/releases.html?variant=openjdk11&jvmVariant=hotspot) - SDK
* [Maven](https://maven.apache.org/) - Dependency Management
* [Chip8 Core](https://github.com/metteo/chip8-core) - Emulator Core
* [Swing](https://docs.oracle.com/javase/tutorial/uiswing/start/about.html) - GUI
* [FlatLaf](https://github.com/JFormDesigner/FlatLaf) - Flat Look & Feel (Swing Theme)

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details

## Acknowledgments

* [r/EmuDev](https://www.reddit.com/r/EmuDev/)
* [Cowgod's Chip-8 Technical Reference v1.0](http://devernay.free.fr/hacks/chip8/C8TECH10.HTM)
* [Mastering Chip-8](http://mattmik.com/files/chip8/mastering/chip8.html)
