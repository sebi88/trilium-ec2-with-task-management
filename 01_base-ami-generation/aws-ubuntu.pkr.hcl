variable "source_ami" {
  type = string
}

variable "trilium_version" {
  type = string
}

variable "instance_type" {
  type = string
}

locals {
  timestamp = regex_replace(timestamp(), "[ TZ:]", "")
}

packer {
  required_plugins {
    amazon = {
      version = ">= 1.2.1"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

source "amazon-ebs" "ubuntu" {
  ami_name      = "trilium-${var.trilium_version}-packer-${local.timestamp}"
  instance_type = "${var.instance_type}"
  region        = "eu-north-1"
  source_ami    = "${var.source_ami}"
  ssh_username  = "ubuntu"
  profile       = "sebi-private"
  temporary_security_group_source_public_ip = true
}

build {
  name = "trilium-packer"
  sources = [
    "source.amazon-ebs.ubuntu"
  ]

  provisioner "shell" {
    environment_vars = [
      "DEBIAN_FRONTEND=noninteractive",
    ]
    inline = [
      "cloud-init status --wait",
      "sudo apt-get update",
      "echo 'debconf debconf/frontend select Noninteractive' | sudo debconf-set-selections",
      "sudo apt-get install software-properties-common --yes --no-install-recommends",
      "sudo add-apt-repository --yes --update ppa:ansible/ansible",
      "sudo apt-get install dialog apt-utils ansible --yes --no-install-recommends",
    ]
  }

  provisioner "ansible-local" {
    playbook_dir    = "./ansible"
    playbook_file   = "./ansible/playbook.yml"
    clean_staging_directory = true
    extra_arguments = ["--extra-vars", "\"trilium_version=${var.trilium_version}\""]
  }
}
