//------------------------------------------------------------------------
// network
//------------------------------------------------------------------------
resource "aws_vpc" "this" {
  cidr_block = "10.0.0.0/16"

  enable_dns_hostnames = true

  tags = {
    Name = "${var.environment_name}-vpc"
  }
}

resource "aws_subnet" "this" {
  vpc_id            = aws_vpc.this.id
  cidr_block        = "10.0.1.0/24"
  availability_zone = var.zone_id

  # map_public_ip_on_launch = true

  tags = {
    Name = "${var.environment_name}-pub-subnet"
  }
}


//------------------------------------------------------------------------
// internet access
//------------------------------------------------------------------------
resource "aws_internet_gateway" "this" {
  vpc_id = aws_vpc.this.id

  tags = {
    Name = "${var.environment_name}-ig"
  }
}

resource "aws_route_table" "this" {
  vpc_id = aws_vpc.this.id

  tags = {
    Name        = "${var.environment_name}-pub-route-table"
    Environment = "${var.environment_name}"
  }
}

resource "aws_route" "this-ig" {
  route_table_id         = aws_route_table.this.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.this.id
}

resource "aws_route_table_association" "this" {
  subnet_id      = aws_subnet.this.id
  route_table_id = aws_route_table.this.id
}


//------------------------------------------------------------------------
// network security
//------------------------------------------------------------------------
resource "aws_security_group" "this" {
  vpc_id      = aws_vpc.this.id
  name_prefix = "${var.environment_name}-sg"

  ingress {
    from_port = 8080
    to_port   = 8080
    protocol  = "tcp"
    # com.amazonaws.global.cloudfront.origin-facing
    prefix_list_ids = ["pl-fab65393"]
    description     = "webapp access for CloudFront"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "allow to anywhere"
  }

  tags = {
    Name = "${var.environment_name}-sg"
  }
}

//------------------------------------------------------------------------
// iam instance profile
//------------------------------------------------------------------------
data "aws_iam_policy_document" "this" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "this" {
  name                = "${var.environment_name}-role"
  assume_role_policy  = data.aws_iam_policy_document.this.json
  managed_policy_arns = ["arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"]
}

resource "aws_iam_instance_profile" "this" {
  name = "${var.environment_name}-profile"
  role = aws_iam_role.this.name
}

//------------------------------------------------------------------------
// instance
//------------------------------------------------------------------------
resource "aws_instance" "this" {
  ami                         = var.ami_id
  disable_api_termination     = true
  instance_type               = var.instance_type
  iam_instance_profile        = aws_iam_instance_profile.this.name
  subnet_id                   = aws_subnet.this.id
  associate_public_ip_address = true
  availability_zone           = var.zone_id

  metadata_options {
    http_endpoint = "enabled"
    http_tokens   = "required"
  }

  root_block_device {
    encrypted = true

    tags = {
      Name = "${var.environment_name}-os"
    }
  }

  tags = {
    Name = "${var.environment_name}"
  }

  vpc_security_group_ids = [aws_security_group.this.id]

  user_data = <<-EOF
    #!/bin/bash
    
    # mounting data disk
    DEV_NAME=`lsblk | grep disk | grep -v nvme0n1 | sed "s/ .*//"`
    DEV_FS=`sudo file -s /dev/$DEV_NAME | sed "s/.*: //"`

    #formad disk, if no fs on it
    if [[ "$DEV_FS" == "data" ]]; then sudo mkfs -t xfs /dev/$DEV_NAME; fi
    
    UUID=`sudo blkid | grep "$DEV_NAME" | sed "s/.*UUID=\"//" | sed "s/\".*//"`
    echo "UUID=$UUID  /opt/trilium-data  xfs  defaults,nofail  0  2" >> /etc/fstab
    sudo mount -a
    sudo chown trilium:trilium /opt/trilium-data

    # starting trilium service
    sudo systemctl enable trilium
    sudo systemctl start trilium
  EOF

}
//------------------------------------------------------------------------
// data disk
//------------------------------------------------------------------------
resource "aws_ebs_volume" "data_volume" {
  availability_zone = var.zone_id
  size              = 2
  encrypted         = true
  type              = "gp3"
  snapshot_id       = var.data_volume_snapshot_id

  tags = {
    Name = "${var.environment_name}-data"
  }
}

resource "aws_volume_attachment" "data_volume_attachment" {
  device_name                    = "/dev/sdh"
  volume_id                      = aws_ebs_volume.data_volume.id
  instance_id                    = aws_instance.this.id
  stop_instance_before_detaching = true
}

//------------------------------------------------------------------------
// Cloudfront
//------------------------------------------------------------------------
resource "aws_cloudfront_distribution" "this" {
  origin {
    domain_name = aws_instance.this.public_dns
    origin_id   = aws_instance.this.public_dns
    custom_origin_config {
      http_port              = 8080
      https_port             = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols   = ["TLSv1.2"]
    }
  }

  enabled         = true
  is_ipv6_enabled = false
  price_class     = "PriceClass_100"

  aliases = ["${var.environment_name}.${var.domain_name}"]

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD", "OPTIONS", "PUT", "POST", "PATCH", "DELETE"]
    cached_methods   = ["GET", "HEAD", "OPTIONS"]
    target_origin_id = aws_instance.this.public_dns

    forwarded_values {
      query_string = true

      headers = ["*"]

      cookies {
        forward = "all"
      }
    }

    viewer_protocol_policy = "redirect-to-https"
  }

  restrictions {
    geo_restriction {
      restriction_type = "whitelist"
      locations        = ["HU", "DE", "AT", "HR", "CZ", "IT", "NL", "PL", "CH", "SE"] 
    }
  }

  viewer_certificate {
    acm_certificate_arn      = var.acm_cert_arm
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2021"
  }
}

//------------------------------------------------------------------------
// DSN record
//------------------------------------------------------------------------
resource "aws_route53_record" "this" {
  zone_id = var.hosted_zone_id
  name    = "${var.environment_name}.${var.domain_name}"
  type    = "A"

  alias {
    name                   = aws_cloudfront_distribution.this.domain_name
    zone_id                = aws_cloudfront_distribution.this.hosted_zone_id
    evaluate_target_health = false
  }
}
