# pgquickreport
PGAdmin III Quick Report Command Line Interface

* Build
```
mvn clean package
```

* Run
```
java -jar target/pgquickreport-1.0-SNAPSHOT.java queryFile1.sql queryFile2.sql
```

* Results
```
$ ls -ls queryFile1.*
-rw-rw-r-- 1 you yourgroup 93652 dez 10 06:56 queryFile1.html
-rw-rw-r-- 1 you yourgroup  3125 dez 10 06:56 queryFile1.sql
```

_Happy Hacking!_
