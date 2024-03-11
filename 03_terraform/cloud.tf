terraform {
  cloud {
    organization = "sebi_private"
    workspaces {
      name = "DEV-trilium-xxx-uk"
    }
  }
}