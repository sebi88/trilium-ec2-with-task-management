//------------------------------------------------------------------------
// ses
//------------------------------------------------------------------------
locals {
  lambda_payload_filename = "./lambda/lambda.zip"
}

resource "aws_ses_domain_identity" "amazonses" {
  domain = "${var.environment_name}.${var.domain_name}"
}

resource "aws_ses_domain_dkim" "amazonses_dkim" {
  domain = "${var.environment_name}.${var.domain_name}"
}

resource "aws_route53_record" "amazonses_dkim_record" {
  count   = 3
  zone_id = var.hosted_zone_id
  name    = "${aws_ses_domain_dkim.amazonses_dkim.dkim_tokens[count.index]}._domainkey.${var.environment_name}.${var.domain_name}"
  type    = "CNAME"
  ttl     = "600"
  records = ["${aws_ses_domain_dkim.amazonses_dkim.dkim_tokens[count.index]}.dkim.amazonses.com"]
}

//------------------------------------------------------------------------
// lambda
//------------------------------------------------------------------------
data "aws_iam_policy_document" "assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}


resource "aws_iam_role" "task_manager_lambda_role" {
  name = "${var.environment_name}_iam_for_lambda"

  assume_role_policy = data.aws_iam_policy_document.assume_role.json


  inline_policy {
    name = "ses_access"

    policy = jsonencode({
      Version = "2012-10-17"
      Statement = [
        {
          Action   = ["ses:SendEmail", "ses:SendRawEmail"]
          Effect   = "Allow"
          Resource = "*"
        },
      ]
    })
  }

  inline_policy {
    name = "logging"
    policy = jsonencode({
      Version : "2012-10-17",
      Statement : [
        {
          Action : [
            "logs:CreateLogStream",
            "logs:PutLogEvents"
          ],
          Effect : "Allow",
          Resource : "arn:aws:logs:*:*:log-group:/aws/lambda/${var.environment_name}_task_manager_lambda:*"
        }
      ]
    })
  }
}

resource "aws_lambda_function" "task_manager_lambda" {
  function_name                  = "${var.environment_name}_task_manager_lambda"
  role                           = aws_iam_role.task_manager_lambda_role.arn
  runtime                        = "java17"
  handler                        = "xxx.uk.tasks.CollectAndSendTasksHandler"
  filename                       = local.lambda_payload_filename
  timeout                        = 60
  source_code_hash               = base64sha256(filebase64(local.lambda_payload_filename))

  environment {
    variables = {
      TRILIUM_URL      = "https://${var.environment_name}.${var.domain_name}/${var.trilium_task_page}"
      TRILIUM_USER     = var.trilium_user
      TRILIUM_PASSWORD = var.trilium_password
      SES_SENDER       = "no-reply@${var.environment_name}.${var.domain_name}"
      SES_RECIPIENT    = var.recipient
    }
  }
}

resource "aws_cloudwatch_log_group" "task_manager_lambda_logs" {
  name              = "/aws/lambda/${var.environment_name}_task_manager_lambda"
  retention_in_days = 14
}

//------------------------------------------------------------------------
// triggering
//------------------------------------------------------------------------
resource "aws_cloudwatch_event_rule" "schedule" {
    name = "${var.environment_name}_schedule"
    //At 04:05 every day
    schedule_expression = "cron(5 4 * * ? *)"
}

resource "aws_cloudwatch_event_target" "schedule_lambda" {
    rule = aws_cloudwatch_event_rule.schedule.name
    target_id = "processing_lambda"
    arn = aws_lambda_function.task_manager_lambda.arn
}

resource "aws_lambda_permission" "allow_events_bridge_to_run_lambda" {
    statement_id = "AllowExecutionFromCloudWatch"
    action = "lambda:InvokeFunction"
    function_name = aws_lambda_function.task_manager_lambda.function_name
    principal = "events.amazonaws.com"
}