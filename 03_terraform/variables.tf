variable "environment_name" {
  #"trilium" or "dev-trilium"
  type = string
}

variable "data_volume_snapshot_id" {
  default = null
  type    = string
}

variable "backup_data" {
  default = true
  type    = bool
}

variable "hosted_zone_id" {
  default = "Z0666wwwwwwwwwwwwwwww"
  type    = string
}

variable "domain_name" {
  default = "xxx.uk"
  type    = string
}

variable "ami_id" {
  default = "ami-0e1dewwwwwwwwwwww"
  type    = string
}

variable "instance_type" {
  default = "t3.nano"
  type    = string
}

variable "zone_id" {
  default = "eu-north-1a"
  type    = string
}

variable "acm_cert_arm" {
  default = "arn:aws:acm:us-east-1:718802003352:certificate/5f7b6e55-87f3-4728-wwww-wwwwwwwwwwww"
  type    = string
}

variable "trilium_user" {
  default = "worker"
  type    = string
}

variable "trilium_task_page" {
  default = "share/tasks"
  type    = string
}

variable "recipient" {
  type = string
}

variable "trilium_password" {
  type = string
}
