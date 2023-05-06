package app;

import app.dto.S3ObjectDto;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

// MailTrigger runs based on EventBridge event rule
public class MailTrigger implements RequestHandler<ScheduledEvent, String>{
    private static final String TOPIC_ARN = "arn:aws:sns:us-east-1:726770482895:s3-mail";
    private static final String SUBJECT = "Daily S3 bucket update";
    private static final String BUCKET_NAME = "cloud-assign-bucket-1";

    public String handleRequest(ScheduledEvent event, Context context) {

        System.out.println("Received ScheduledEvent: " + event.toString());

        List<S3ObjectDto> s3ObjectDtoList = getS3ObjectsUpdatedToday();
        sendSNS(s3ObjectDtoList);

        return "Lambda function completed successfully!";
    }

    private List<S3ObjectDto> getS3ObjectsUpdatedToday() {
        List<S3ObjectDto> s3ObjectDtoList = new ArrayList<>();
        AmazonS3 s3 = new AmazonS3Client();

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDate = dateFormat.format(date);

        ListObjectsRequest request = new ListObjectsRequest().withBucketName(BUCKET_NAME);
        request.putCustomRequestHeader("x-amz-date", formattedDate);


        ObjectListing result = s3.listObjects(request);
        for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
            if (objectSummary.getLastModified().getDate() == date.getDate()) {
                Date expiration = new Date();
                long expTimeMillis = expiration.getTime();
                expTimeMillis += 1000 * 60 * 60; // setting expiration of 1 hour for S3 URI
                expiration.setTime(expTimeMillis);

                GeneratePresignedUrlRequest generatePresignedUrlRequest =
                        new GeneratePresignedUrlRequest(BUCKET_NAME, objectSummary.getKey())
                                .withMethod(HttpMethod.GET)
                                .withExpiration(expiration);
                URL s3URI = s3.generatePresignedUrl(generatePresignedUrlRequest);

                S3ObjectDto objectDto = new S3ObjectDto();
                objectDto.setObjectName(objectSummary.getKey());
                DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                objectDto.setObjectSize(decimalFormat.format((double) objectSummary.getSize() / (1024 * 1024))+ " mb");
                objectDto.setS3URI(String.valueOf(s3URI));
                objectDto.setLastModified(String.valueOf(objectSummary.getLastModified()));
                s3ObjectDtoList.add(objectDto);
            }
        }

        return s3ObjectDtoList;
    }

    private void sendSNS(List<S3ObjectDto> s3ObjectDtoList) {
        AmazonSNS snsClient = new AmazonSNSClient();
        PublishRequest request = new PublishRequest();
        request.withTopicArn(TOPIC_ARN);
        request.withSubject(SUBJECT);

        StringBuilder stringBuilder = new StringBuilder();
        String msgPrefix = "List of files uploaded to " + BUCKET_NAME + ":\n---------------------------------------------------------\n\n";
        s3ObjectDtoList.forEach(s3ObjectDto -> stringBuilder
                .append("Object Name: ")
                .append(s3ObjectDto.getObjectName())
                .append("\nLast Modified: ")
                .append(s3ObjectDto.getLastModified())
                .append("\nObject Size: ")
                .append(s3ObjectDto.getObjectSize())
                .append("\nObject S3 URI: ")
                .append(s3ObjectDto.getS3URI())
                .append("\n\n"));

        request.withMessage(msgPrefix+stringBuilder);
        PublishResult result = snsClient.publish(request);

        System.out.println("Sent email message with ID: " + result.getMessageId());
    }
}
