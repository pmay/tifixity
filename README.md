# Tifixity

TIFF Image Data Checksummer

### What does Tifixity do?

Checksums the image data portion of a TIFF file, as opposed to the entire TIFF file. Useful 
for ensuring that the pixel data remains the same before and after some process.

## Features and roadmap

### Versino 0.2.0
* Supports multi-image TIFF files
* More robust CLI

### Version 0.1.0

* Initial version

## How to install and use

### Requirements

To install you need:

* Git client
* Java 8
* Maven

### Build instructions

Note: Only tested on windows so far.
To download and build follow these steps:

```bash
$ git clone https://github.com/pmay/Tifixity.git
$ cd Tifixity
$ mvn clean compile install
```

After successful completion, the tifixity jar file will be in the target directory.

### Use

To use the tool, run the jar from the command line (from the Tifixity directory):
```bash
$ java -jar target\tifixity-0.1.0-SNAPSHOT.jar src\test\resources\rgbstrips.tiff
...
5478865efdfc945d291b584402d34a33
```

### Setup IDE

Tifixity was developed with IntelliJ IDEA 15.0.3.

### Troubleshooting

## More information

### Licence

Tifixity is released under [Apache version 2.0 license](LICENSE.txt).

### Contribute

1. [Fork the GitHub project](https://help.github.com/articles/fork-a-repo)
2. Change the code and push into the forked project
3. [Submit a pull request](https://help.github.com/articles/using-pull-requests)
