# Task manager lambda

## About

Lambda function, that

- downloads 'Tasks' subtree of notes
- parses date pattern from note titles
- sends out node content as e-mail (if date pattern matches for today)

## How to publish new version

1. change code
1. `./gradlew clean build`
1. `cp  build/distributions/java.zip ../terraform/lambda/lambda.zip`
1. change params in terraform
1. apply terraform

# Task Manager Lambda Function

## Overview

This project contains a Lambda function that performs the following operations:

- Downloads the 'Tasks' subtree of notes (need to be shares it in Trilium, and protected with user-password)
- Parses date patterns from note titles.
- Sends out the content of the note as an email if the date pattern matches the current date.

## Deployment Instructions

Follow these steps to publish a new version of the function:

1. Modify the code as necessary.
2. Try out locally
3. Run the command `./gradlew clean build` to build the project.
4. Copy the built distribution to the terraform directory using the command `cp  build/distributions/java.zip ../03_terraform/lambda/lambda.zip`.
5. Apply terraform
