#!/bin/bash
aws cloudfront create-invalidation --distribution-id E24WWWWWWWWWWW --paths "/*" --profile sebi-private
