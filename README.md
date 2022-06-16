# Active Directory database snapshot generator

## Dependencies

Install [sbt](https://www.scala-sbt.org/)

### Testing

```bash
sbt test
```

### Generating data

```bash
sbt run
```

### Train and SVM classifier on the generated data

```bash
make init && make run
```
