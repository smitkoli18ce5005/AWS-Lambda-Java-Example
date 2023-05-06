package app.dto;

public class S3ObjectDto {

    String objectName;
    String objectSize;
    String lastModified;
    String s3URI;

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getObjectSize() {
        return objectSize;
    }

    public void setObjectSize(String objectSize) {
        this.objectSize = objectSize;
    }

    public String getS3URI() {
        return s3URI;
    }

    public void setS3URI(String s3URI) {
        this.s3URI = s3URI;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
}
