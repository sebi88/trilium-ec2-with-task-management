# Terraform script

## Contents

- ec2
- dns record
- cloudfront
- backup
- task manager lambda

## Development flow

- push commits to `develop` branch
- apply changes using: https://app.terraform.io/app/sebi_private/workspaces/DEV-trilium-xxx-uk
- check changes on: https://dev-trilium.xxx.uk/
- repeat
- when ready,
  - destroy test environment
  - create PR to merge to `main` branch
- check PR, merge
- apply changes using: https://app.terraform.io/app/sebi_private/workspaces/trilium-xxx-uk
