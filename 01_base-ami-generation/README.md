# Base AMI for Trilium

This repository contains the necessary files to create a base Amazon Machine Image (AMI) with the following contents:

- Ansible
- Trilium
- trilium.service file

## Updating and Publishing a New Image

1. **Update Ubuntu Version**: Modify the Ubuntu version in `aws-ubuntu.pkrvars.hcl`. Use the [most recent Ubuntu base image](https://eu-north-1.console.aws.amazon.com/ec2/home?region=eu-north-1#Images:visibility=public-images;search=:ubuntu/images/hvm-ssd/ubuntu-focal-20.04-amd64;ownerAlias=amazon;v=3;$case=tags:false%5C,client:false;$regex=tags:false%5C,client:false).

2. **Update Trilium Version**: Update the Trilium version in `aws-ubuntu.pkrvars.hcl`. Use the [most recent Trilium version](https://github.com/zadam/trilium/releases).

3. **Build the New Image**: Run the following command to build the new image: `packer build --var-file=aws-ubuntu.pkrvars.hcl aws-ubuntu.pkr.hcl`

4. **Update Terraform Parameters**: Modify the parameters in your Terraform files as necessary.

5. **Testing new image**:

    1. Update `ami_id` in  `../03_terraform/variables.tf`
    2. deploy test environment

6. **Clean Up Old AMIs**: Delete old AMIs, but keep the current and previous versions for rollback purposes.
