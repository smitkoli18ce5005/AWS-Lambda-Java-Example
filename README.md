# Cloud Computing Assignment - BITS Pilani WILP

Using AWS lambda, write a program to monitor an S3 Uri. Whenever a user uploads data into the S3 storage, the program should capture the details. At the end of the day the program must send out an email to select users displaying the following information

a.       S3 Uri

b.      Object Name

c.       Object Size

d.      Object type

In addition to the above, the program should create a thumbnail and store it in a different uri in case the user uploads an image (.jpg/jpeg/png)