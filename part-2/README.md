# test.check workshop part 2 codebases

In this section of the workshop you will write properties for one or
more pieces of clojure code. There are a few different options
(described below), each is in its own file (with code already written)
in the `src` directory. There is also a test file already set up for
each in the `test` directory.

## Running the tests

You can run *all* of the tests with:

```
lein test
```

You can also run a single namespace of tests with e.g.,

```
lein test tc-workshop.base64-test
```

## Codebases to test

### Floating-point Midpoint

Test a simple function that

### Change maker

Test a function that counts the ways to express a monetary amount
given a list of denominations.

### base64

Test the (real life) clojure.data.codec code.

### Run-length encoding

Test a naive compression algorithm.
