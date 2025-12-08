package gov.geoplatform.knowstac.web.controller;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@RestController
public class AwsController extends RunwaySpringController
{
  @GetMapping("aws/download")
  public ResponseEntity<StreamingResponseBody> download(@RequestParam(name = "url") String url) throws IOException
  {
    S3Client s3Client = S3Client.builder().region(Region.US_EAST_1).credentialsProvider(AnonymousCredentialsProvider.create()).build();
    S3Utilities s3Utilities = s3Client.utilities();

    URI uri = URI.create(url);
    S3Uri s3Uri = s3Utilities.parseUri(uri);

    // Region region = s3Uri.region().orElse(null); // Region.US_WEST_1
    String bucket = s3Uri.bucket().orElse(null);
    String key = s3Uri.key().orElse(null);

    GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(key).build();

    final ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(request);

    StreamingResponseBody response = outputStream -> {
      IOUtils.copy(inputStream, outputStream);
    };

    GetObjectResponse objectResponse = inputStream.response();

    String contentType = objectResponse.contentType();
    String disposition = objectResponse.contentDisposition();

    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, contentType);
    headers.set(HttpHeaders.CONTENT_DISPOSITION, disposition);

    return new ResponseEntity<StreamingResponseBody>(response, headers, HttpStatus.OK);
  }
}