# AWS Lambda Java Example

Using AWS lambda, write a program to monitor an S3 Uri. Whenever a user uploads data into the S3 storage, the program should capture the details. At the end of the day the program must send out an email to select users displaying the following information

a.       S3 Uri

b.      Object Name

c.       Object Size

d.      Object type

In addition to the above, the program should create a thumbnail and store it in a different uri in case the user uploads an image (.jpg/jpeg/png)


### AWS S3
Create two buckets named "cloud-assign-bucket-1" and "cloud-assignbucket-2" with public access off
### AWS Lambda
Create two method named "s3-thumbnail" and "s3-mail" with java 11 runtime. 
### AWS SNS
Create SNS topic named "s3-mail" of type standard and add email subscribers where you want to receive mails
### AWS EventBridge
Create EventBridge rule named "mail-trigger" and add as trigger to Lambda method "s3-mail". Cron is - 00 15 ? * * *
### AWS IAM
To provide access to lambda methods use below IAM Roles
##### s3-thumbnail ->
{
"Version": "2012-10-17",
"Statement": [
{
"Sid": "getObjectSourceBucket",
"Effect": "Allow",
"Action": "s3:GetObject",
"Resource": "arn:aws:s3:::cloud-assign-bucket-1/*"
},
{
"Sid": " putObjectDestinationBucket ",
"Effect": "Allow",
"Action": "s3:PutObject",
"Resource": "arn:aws:s3:::cloud-assign-bucket-2/*"
}
]
}
##### s3-mail ->
{
"Version": "2012-10-17",
"Statement": [
{
"Sid": "s3Read",
"Effect": "Allow",
"Action": [
"s3-object-lambda:List*",
"s3-object-lambda:Get*",
"s3:Get*",
"s3:List*"
],
"Resource": [
"arn:aws:s3:::cloud-assign-bucket-1",
"arn:aws:s3:::cloud-assign-bucket-1/*"
]
},
{
"Sid": "snsPublish",
"Effect": "Allow",
"Action": "sns:Publish",
"Resource": "arn:aws:sns:us-east-1:726770482895:s3-mail"
}
]
}
