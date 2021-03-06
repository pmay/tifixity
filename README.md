# Tifixity

TIFF Image Data Checksummer

### What does Tifixity do?

Primarily it calculates checksums for the image data portions of a TIFF file, as opposed to the entire TIFF file. This is useful for ensuring that the pixel data remains the same before and after some process.

Tifixity is capable of calculating MD5 checksums for the following data sets:
* Full file
* each subfile's image data
* Non-image data (i.e. remaining data; everything except image data)
* each IFD (i.e. all bytes associated with an IFD's metadata)

## Features and roadmap

### Version 0.3.0
* Supports calculation of full and partial (non-image-data) checksums
* Supports calculation of IFD checksums

### Version 0.2.0
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
$ java -jar target\tifixity-0.3.0-SNAPSHOT.jar src\test\resources\rgbstrips.tiff
...
5478865efdfc945d291b584402d34a33
```

Various command line options are available. Run the jar without a file to get help:
```bash
$ java -jar target\tifixity-0.3.0-SNAPSHOT.jar
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
