package app;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import net.coobird.thumbnailator.Thumbnails;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
public class Thumbnail implements RequestHandler<S3Event, String> {
    private final AmazonS3 s3 = new AmazonS3Client();
    private static final String DESTINATION_BUCKET_NAME = "cloud-assign-bucket-2";
    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        try {
            for (S3EventNotification.S3EventNotificationRecord record : s3Event.getRecords()) {
                String srcBucket = record.getS3().getBucket().getName();
                String srcKey = record.getS3().getObject().getKey();

                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                String formattedDate = dateFormat.format(date);

                // Download the source image from S3
                GetObjectRequest getObjectRequest = new GetObjectRequest(srcBucket, srcKey);
                getObjectRequest.putCustomRequestHeader("x-amz-date", formattedDate);
                InputStream objectData = s3.getObject(getObjectRequest).getObjectContent();

                // Create a thumbnail using the Thumbnail library
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Thumbnails.of(objectData)
                        .size(200, 200)
                        .toOutputStream(outputStream);

                // Upload the thumbnail to S3
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(outputStream.size());
                PutObjectRequest putObjectRequest = new PutObjectRequest(DESTINATION_BUCKET_NAME, srcKey.replaceAll("\\.(.*)", "_thumbnail.jpg"), new ByteArrayInputStream(outputStream.toByteArray()), metadata);
                putObjectRequest.putCustomRequestHeader("x-amz-date", formattedDate);
                s3.putObject(putObjectRequest);
            }
            return "Thumbnail creation complete";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
