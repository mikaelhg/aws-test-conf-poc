# POC of Spring Cloud AWS testing with mock AWS endpoints

## How to figure out how the AWS client endpoint urls are set

Set a field modification watchpoint on `com.amazonaws.AmazonWebServiceClient.endpoint`.

## Where to hook into `regions.xml` file parsing

Add a file called `src/test/resources/com/amazonaws/regions/override/regions.xml`.

It will be loaded in `com.amazonaws.regions.LegacyRegionXmlMetadataBuilder.loadOverrideMetadataIfExists`.

Just add some nonsense to the file, and a stack trace will present itself.

The parser for `regions.xml` is `com.amazonaws.regions.RegionMetadataParser.internalParse`.



## Relevant issues and documentation:

https://github.com/spring-cloud/spring-cloud-aws/issues/570

https://github.com/aws/aws-sdk-java/issues/842

https://github.com/aws/aws-sdk-java/issues/1766

https://raw.githubusercontent.com/aws/aws-sdk-java/master/aws-java-sdk-core/src/main/resources/com/amazonaws/internal/config/awssdk_config_default.json

https://raw.githubusercontent.com/aws/aws-sdk-java/master/aws-java-sdk-core/src/test/resources/com/amazonaws/regions/fake-regions.xml

https://cloud.spring.io/spring-cloud-static/spring-cloud-aws/2.2.2.RELEASE/reference/html/

https://cloud.spring.io/spring-cloud-static/spring-cloud-aws/2.2.2.RELEASE/reference/html/appendix.html
