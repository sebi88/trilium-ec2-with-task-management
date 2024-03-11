//------------------------------------------------------------------------
// data disk backup
//------------------------------------------------------------------------
data "aws_iam_policy_document" "data_volume_backup_assume_role" {
  count = var.backup_data ? 1 : 0
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["dlm.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "data_volume_backup" {
  count              = var.backup_data ? 1 : 0
  name               = "${var.environment_name}-dlm-lifecycle-role"
  assume_role_policy = data.aws_iam_policy_document.data_volume_backup_assume_role[0].json
}

data "aws_iam_policy_document" "data_volume_backup" {
  count = var.backup_data ? 1 : 0
  statement {
    effect = "Allow"

    actions = [
      "ec2:CreateSnapshot",
      "ec2:CreateSnapshots",
      "ec2:DeleteSnapshot",
      "ec2:DescribeInstances",
      "ec2:DescribeVolumes",
      "ec2:DescribeSnapshots",
    ]

    resources = ["*"]
  }

  statement {
    effect    = "Allow"
    actions   = ["ec2:CreateTags"]
    resources = ["arn:aws:ec2:*::snapshot/*"]
  }
}

resource "aws_iam_role_policy" "data_volume_backup" {
  count  = var.backup_data ? 1 : 0
  name   = "${var.environment_name}-dlm-lifecycle-policy"
  role   = aws_iam_role.data_volume_backup[0].id
  policy = data.aws_iam_policy_document.data_volume_backup[0].json
}

resource "aws_dlm_lifecycle_policy" "data_volume_backup" {
  count              = var.backup_data ? 1 : 0
  description        = "Backup for ${var.environment_name}-data disk"
  execution_role_arn = aws_iam_role.data_volume_backup[0].arn
  state              = "ENABLED"

  policy_details {
    resource_types = ["VOLUME"]

    target_tags = {
      Name = "${var.environment_name}-data"
    }

    schedule {
      name = "Daily snapshots"

      create_rule {
        interval      = 24
        interval_unit = "HOURS"
        times         = ["23:45"]
      }

      retain_rule {
        count = 7
      }

      tags_to_add = {
        SnapshotCreator = "DLM"
        Name            = "${var.environment_name}-data"
      }
    }
  }

  tags = {
    Name = "DLM ${var.environment_name}-data"
  }

}